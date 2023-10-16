import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;
import java.util.Arrays;

public class ModernCiphers {

    public static byte[] xorBytes(byte[] arr1, byte[] arr2){
        assert arr1.length == arr2.length;
        byte[] result = new byte[arr1.length];
        for (int i = 0; i < arr1.length; i++)
            result[i] = (byte) (arr1[i] ^ arr2[i]);
        return result;
    }
    public static void main(String[] args) throws NoSuchAlgorithmException {
         // Add Bouncy Castle as a security provider
        Security.insertProviderAt(new BouncyCastleProvider(), 1);

        //Algorithms
        SecureRandom det_prng = SecureRandom.getInstance("SHA1PRNG");
        SecureRandom prob_prng = SecureRandom.getInstanceStrong();
        HMac hmacSha1 = new HMac(new SHA1Digest());
        SecureRandom secureRandom = new SecureRandom();
        byte[] seed = secureRandom.generateSeed(16); // Change the seed length as needed

        
        det_prng.setSeed(seed);
        prob_prng.setSeed(seed);

        int det_random = det_prng.nextInt();
        int prob_random = prob_prng.nextInt();
        System.out.println("Deterministic Algortihm: " + det_random);
        System.out.println("Probabilistic Algorithm: " + prob_random);
        
        byte[] message = "carro".getBytes();

        byte[] pad = new byte[message.length];
        det_prng.nextBytes(pad);
        System.out.println("Pad: " + Arrays.toString(pad));

        byte[] cipher_text = xorBytes(pad,message);
        System.out.println("Cipher text: " + Arrays.toString(cipher_text));

        hmacSha1.init(new KeyParameter(pad));
        hmacSha1.update(message,0,message.length);
        byte[] resultSHA1 = new byte[hmacSha1.getMacSize()];
        hmacSha1.doFinal(resultSHA1, 0);
        for (byte b : resultSHA1) {
            System.out.printf("%02x", b);
        }
        
        
    }
}
