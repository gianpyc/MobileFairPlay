// TimedCommitmentHelpers.java
//
// Helper functions
// Created November  5, 2003 by Ori Peleg
//
// $Id: TimedCommitmentHelpers.java,v 1.9 2004/07/04 19:11:00 orip Exp $
package SFE.TimedCommitments;

import java.math.BigInteger;

import java.util.Random;


class TimedCommitmentHelpers {
	//~ Methods ----------------------------------------------------------------

	public static boolean booleanLSB(BigInteger b) {
		return b.testBit(0);
	}

	// return a large prime who's value is 3 (mod 4)
	public static BigInteger generate3Mod4ValuePrime(int nbits) {
		BigInteger result;

		do {
			result = BigInteger.probablePrime(nbits, rand);
		} while (! result.mod(FOUR).equals(THREE));

		return result;
	}

	public static BigInteger randomInteger(int nbits) {
		return new BigInteger(nbits, rand);
	}

	public static BigInteger getPrimeMultiples(int n, int B) {
		BigInteger result = ONE;

		for (int num = 2; num < B; ++num) {
			boolean isPrime = true;

			for (int i = 2; i < num; ++i) {
				if ((num % i) == 0) {
					isPrime = false;

					break;
				}
			}

			if (isPrime) {
				// multiply result by num^n
				BigInteger temp = new BigInteger(Integer.toString(num));
				temp       = temp.pow(n);
				result     = result.multiply(temp);
			}
		}

		return result;
	}

	//~ Static fields/initializers ---------------------------------------------

	private static Random           rand  = new Random();
	private static final BigInteger ONE   = BigInteger.valueOf(1);
	private static final BigInteger THREE = BigInteger.valueOf(3);
	private static final BigInteger FOUR  = BigInteger.valueOf(4);
}


// end of TimedCommitmentHelpers.java
