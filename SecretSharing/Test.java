import java.math.BigInteger;

public class Test {

    private static BigInteger calculatePoint(BigInteger x, BigInteger[] polynomial) {
        BigInteger sum = BigInteger.valueOf(0);
		for( int i = 0; i < polynomial.length; i++){
			sum = sum.add(polynomial[i].multiply(x.pow(i))) ; 
		}
		return sum;
	}
    public static void main(String[] args){
        BigInteger[] polynomial = new BigInteger[2];
        polynomial[0] = BigInteger.valueOf(5);
        polynomial[1] = BigInteger.valueOf(3);

        BigInteger x = BigInteger.valueOf(2);

        BigInteger big = Test.calculatePoint(x, polynomial);
        System.out.println(big);
    }
    
}
