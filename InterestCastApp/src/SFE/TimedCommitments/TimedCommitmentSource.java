// TimedCommitmentSource.java
//
// class TimedCommitmentSource
// The source of a timed commitment, used to generate timed commitments.
//
// Created November 20, 2003 by Ori Peleg
//
// $Id: TimedCommitmentSource.java,v 1.11 2004/07/04 19:11:01 orip Exp $
package SFE.TimedCommitments;

import java.math.BigInteger;

import java.util.BitSet;
import java.util.Vector;


class TimedCommitmentSource {
	//~ Instance fields --------------------------------------------------------

	// data members
	private final BigInteger P;
	private final BigInteger Q;
	private final BigInteger N;
	private final BigInteger phiN;
	private final int        k;
	private final int        n;
	private final BigInteger g;
	private final BitSet     data;
	private final BitSet     S;
	private final Vector     W;
	private final Vector     hints;

	//~ Constructors -----------------------------------------------------------

	public TimedCommitmentSource(BitSet data, int strength) {
		this(data, strength, CLASS_DEFAULT_n, CLASS_DEFAULT_prime_nbits);
	}

	public TimedCommitmentSource(BitSet data, int strength, int n,
	                             int prime_nbits) {
		this.k        = strength;
		this.n        = n;
		this.data     = data;

		// filling P, Q, N, and phiN
		this.P         = TimedCommitmentHelpers.generate3Mod4ValuePrime(prime_nbits);
		this.Q         = TimedCommitmentHelpers.generate3Mod4ValuePrime(prime_nbits);
		this.N         = P.multiply(Q);
		this.phiN      = P.subtract(ONE).multiply(Q.subtract(ONE));
		this.g         = calculateG(prime_nbits);
		this.S         = calculateS();
		this.W         = calculateW();
		this.hints     = calculateHints();
	}

	//~ Methods ----------------------------------------------------------------

	public TimedCommitment generateCommitment() {
		return new TimedCommitment(N, g, n, k, W, S);
	}

	public int numHints() {
		return hints.size();
	}

	public TimedCommitmentHint generateHint(int i) {
		assert (1 <= i) && (i <= k);

		TimedCommitmentHint hint = (TimedCommitmentHint) hints.elementAt(i - 1);
		assert hint.index.intValue() == i;

		return hint;
	}

	/**
	 * @return g, as per Boneh-Naor's algorithm
	 */
	private BigInteger calculateG(int prime_nbits) {
		final int        B = TimedCommitmentConstants.B;
		final BigInteger h =
			TimedCommitmentHelpers.randomInteger(prime_nbits * 2).mod(N);

		return h.modPow(TimedCommitmentHelpers.getPrimeMultiples(n, B), N);
	}

	/**
	 * @return the data encrypted by the key K, as per Boneh-Naor's algorithm
	 */
	private BitSet calculateS() {
		BitSet S = (BitSet) data.clone();
		S.xor(calculateK());

		return S;
	}

	/**
	 * @return the key K (the tail of a BBS-generator sequence), as per
	 * Boneh-Naor's algorithm
	 */
	private BitSet calculateK() {
		BitSet     K = new BitSet(data.size());

		BigInteger temp2 = TWO.pow(k).subtract(BigInteger.valueOf(data.size()));
		BigInteger temp3 = TWO.modPow(temp2, phiN);
		BigInteger v     = g.modPow(temp3, N);

		for (int i = 0; i < K.size(); ++i) {
			K.set(K.size() - i - 1, TimedCommitmentHelpers.booleanLSB(v));
			v = v.modPow(TWO, N);
		}

		return K;
	}

	/**
	 * @return the vector W=[g^2, g^4, g^16, ..., g^(2^(2^k))], as per Boneh-Naor's algorithm
	 */
	private Vector calculateW() {
		Vector result = new Vector();

		// when we start, power = 2 = 2^(2^0) [mod phiN]
		BigInteger power = TWO;

		for (int i = 1; i <= k; i++) {
			power = power.modPow(TWO, phiN);
			result.addElement(g.modPow(power, N));
		}

		return result;
	}

	private Vector calculateHints() {
		Vector result = new Vector();

		// The first hint is the one-before-last element of W.
		// Note that it is already included in the first timed commitment.
		final TimedCommitmentHint firstHint =
			new TimedCommitmentHint(1, (BigInteger) W.elementAt(W.size() - 2));

		result.addElement(firstHint);

		BigInteger currentHintValue = firstHint.value;

		for (int i = 2; i <= k; i++) {
			currentHintValue = powTwoTwo(currentHintValue, k - i);

			TimedCommitmentHint currentHint =
				new TimedCommitmentHint(i, currentHintValue);
			result.addElement(currentHint);
			assert currentHint.equals(result.lastElement());
		}

		return result;
	}

	/**
	 * @return num ^ (2^(2^i)) [mod N]
	 *
	 * Calculated by num ^ ( 2^(2^i) [mod phi(N)] ) [mod N]
	 */
	public /*private*/ BigInteger powTwoTwo(BigInteger num, int i) {
		//BigInteger power = TWO.modPow( TWO.pow(i), phiN );
		BigInteger power = TWO.modPow(ONE.shiftLeft(i), phiN);

		return num.modPow(power, N);
	}

	//~ Static fields/initializers ---------------------------------------------

	// class constants
	private static final int        CLASS_DEFAULT_n           = 50;
	private static final int        CLASS_DEFAULT_prime_nbits = 512;
	private static final BigInteger ONE                       = BigInteger.ONE;
	private static final BigInteger TWO                       =
		BigInteger.valueOf(2);
}


// end of file TimedCommitmentSource.java
