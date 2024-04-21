package teste;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CashSSE {
	
	private static final String hmac_alg = "HmacSHA1";
	private static final String cipher_alg = "AES";
	private static final SecureRandom rndGenerator = new SecureRandom();
	private static Mac hmac;
	private static Cipher aes;
	private static IvParameterSpec iv;	//fixed iv for simplicity
	
	private static Server server;
	private static Client client;

	
	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		hmac = Mac.getInstance(hmac_alg);
		aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
		
		System.out.println("Starting up the Health Record DB...");
		client = new Client();
		server = new Server();
		
		
		System.out.println("Finished! Populating the DB...");
		
		client.update("bob", 1);
		client.update("cancer", 1);
		client.update("liver", 1);
		
		client.update("alice", 2);
		client.update("avc", 2);
		client.update("heart", 2);
		
		client.update("charlie", 3);
		client.update("trauma", 3);
		client.update("brain", 3);
		client.update("liver", 3);
		
		System.out.println("Finished! Searching for bob:");
		List<Integer> ids = client.search("bob");
		for(Integer id: ids)
			System.out.println(id);
		
		System.out.println("Finished! Searching for liver:");
		ids = client.search("liver");
		for(Integer id: ids)
			System.out.println(id);
	}
	

	private static class Client {

		
		private HashMap<String,Integer> counters;
		private SecretKeySpec sk;
		
		public Client() throws NoSuchAlgorithmException, NoSuchPaddingException {
			byte[] sk_bytes = new byte[20];
			byte[] iv_bytes = new byte[16];
			rndGenerator.nextBytes(sk_bytes);
			rndGenerator.nextBytes(iv_bytes);
			sk = new SecretKeySpec(sk_bytes, hmac_alg);
			iv = new IvParameterSpec(iv_bytes);
			counters = new HashMap<>(100);
		}
		
		public void update(String keyword, int docId) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
			String keyword1 = keyword+"1";
			String keyword2 = keyword+"2";
			hmac.init(sk);
			byte[] k1 = hmac.doFinal(keyword1.getBytes());
			byte[] k2 = hmac.doFinal(keyword2.getBytes());
			
			Integer c = counters.get(keyword);
			if (c == null)
				c = 0;
			hmac.init(new SecretKeySpec(k1, hmac_alg));
			byte[] index_label = hmac.doFinal( ByteBuffer.allocate(4).putInt(c).array() );
			
			aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(Arrays.copyOfRange(k2, 0, 16), cipher_alg), iv);
			byte[] index_value = aes.doFinal( ByteBuffer.allocate(4).putInt(docId).array() );
			
			server.update(new ByteArray(index_label), new ByteArray(index_value));
			counters.put(keyword, c+1);
		}
		
		public List<Integer> search(String keyword) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
			String keyword1 = keyword+"1";
			String keyword2 = keyword+"2";
			hmac.init(sk);
			byte[] k1 = hmac.doFinal(keyword1.getBytes());
			byte[] k2 = hmac.doFinal(keyword2.getBytes());
			
			return server.search(k1,k2);
		}
		
	}
	
	private static class Server {
		
		private Map<ByteArray,ByteArray> index;
		
		public Server() {
			index = new HashMap<ByteArray,ByteArray>(1000);
		}
		
		public void update (ByteArray label, ByteArray value) {
			index.put(label, value);
		}
		
		public List<Integer> search (byte[] k1, byte[] k2) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
			int c = 0;
			List<Integer> results = new LinkedList<Integer>();
			
			hmac.init(new SecretKeySpec(k1, hmac_alg));
			aes.init(Cipher.DECRYPT_MODE, new SecretKeySpec(Arrays.copyOfRange(k2, 0, 16), cipher_alg), iv);
			
			for (;;) {
				byte[] index_label = hmac.doFinal( ByteBuffer.allocate(4).putInt(c).array() );
				ByteArray index_value = index.get(new ByteArray(index_label));
				if (index_value == null)
					break;
				byte[] docId = aes.doFinal(index_value.getArr());
				results.add( ByteBuffer.wrap(docId).getInt() );
				c++;
			}
			
			return results;
		}
		
	}

	private static class ByteArray {
		
		private byte[] arr;

		public ByteArray(byte[] array) {
			arr = array;
		}
		
		public byte[] getArr() {
			return arr;
		}
		
		@Override
	    public boolean equals(Object obj) {
	        return obj instanceof ByteArray && Arrays.equals(arr, ((ByteArray)obj).getArr());
	    }
		
	    @Override
	    public int hashCode() {
	        return Arrays.hashCode(arr);
	    }
	}
	
}
