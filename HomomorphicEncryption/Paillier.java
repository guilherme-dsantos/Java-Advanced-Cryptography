
import java.math.BigInteger;
import java.security.SecureRandom;

public class Paillier {
	private static final SecureRandom rndGenerator = new SecureRandom();
	
	//parameters:
	private static final BigInteger q = BigInteger.probablePrime(2048, rndGenerator);
	private static final BigInteger p = BigInteger.probablePrime(2048, rndGenerator);
	
	
	//for testing purposes only
//	private static final BigInteger q = new BigInteger("11");
//	private static final BigInteger p = new BigInteger("17");

	
	//public and private keys:
	
	private static final BigInteger sk = (p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE)));			// the private key sk = (p-1).(q-1)
	private static final BigInteger pk = p.multiply(q);		// the public key pk = p.q
	
	
	public static void main (String[] args) {
		testEncDec();
		testHom();
	}
	
	private static void testEncDec() {
		System.out.println("========");
		System.out.println("Test Encryption and Decryption");
		System.out.println("========");
		
//		BigInteger m1 = new BigInteger("175");
		BigInteger m1 = new BigInteger(q.bitLength() - 1, rndGenerator);
		
		BigInteger c = encrypt(m1);
		BigInteger m2 = decrypt(c);
		
		System.out.println("original message: "+m1);
		System.out.println("ciphertext: "+c);
		System.out.println("decrypted message: "+m2);
		System.out.println();
	}
	
	private static void testHom() {
		System.out.println("========");
		System.out.println("Test Homomorphic Operation");
		System.out.println("========");
		BigInteger m1 = new BigInteger("65");
		BigInteger m2 = new BigInteger("32");
		
		BigInteger cipher1 = encrypt(m1);
		BigInteger cipher2 = encrypt(m2);
		BigInteger cipher_times = eval(cipher1,cipher2);
		
		BigInteger m_times = decrypt(cipher_times);
		
		System.out.println("plaintext message 1: "+m1);
		System.out.println("plaintext message 2: "+m2);
		System.out.println("plaintext addition: "+m1.add(m2));
		System.out.println("decryption of the added ciphertexts: "+m_times);
		System.out.println();
	}
	
	/**
	 * Encrypts a message m with the public key n, using a random value r.
	 * 
	 * @param m the message
	 * @return ciphertext c = ( (1+n)^m mod n^2 . r^n mod n^2 ) mod n^2
	 */
	private static BigInteger encrypt(BigInteger m) {
		BigInteger r = new BigInteger(q.bitLength() - 1, rndGenerator);
		BigInteger aux = (BigInteger.ONE.add(pk)).modPow(m, pk.pow(2));
		BigInteger aux2 = r.modPow(pk, pk.pow(2));
		return (aux.multiply(aux2)).mod(pk.pow(2));
		
	}
	
	/**
	 * Decrypts ciphertext c with the private key sk.
	 * 
	 * @param c the ciphertext
	 * @return plaintext m = ( ( (c^sk mod n^2)-1) / (n) . (sk^-1 mod n) ) mod n
	 */
	private static BigInteger decrypt(BigInteger c) {
		BigInteger aux= (c.modPow(sk, pk.pow(2)).subtract(BigInteger.ONE)).divide(pk);
		return (aux.multiply(sk.modPow(BigInteger.valueOf(-1), pk))).mod(pk);
	}
	
	/**
	 * Performs the homomorphic operation of this scheme, i.e., multiplies the
	 *  ciphertexts which results in the addition of the plaintexts.
	 * 
	 * @param cipher1
	 * @param cipher2
	 * @return ciphertext c = ( cipher1 . cipher2 ) mod n^2
	 */
	private static BigInteger eval(BigInteger cipher1, BigInteger cipher2) {
		return (cipher1.multiply(cipher2)).mod(pk.pow(2));

	}
	
}
