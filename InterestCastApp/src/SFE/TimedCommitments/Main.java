// Main.java
//
// Main class for timed commitments check (runs commiter and verifier)
//
// Created November 16, 2003 by Ori Peleg
//
// $Id: Main.java,v 1.6 2004/07/04 19:06:00 orip Exp $
package SFE.TimedCommitments;

import java.util.BitSet;


class Main {
	//~ Methods ----------------------------------------------------------------

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: Main numOfHints");
			System.exit(1);
		}

		// Informative output
		System.out.println("Using K=" + K);

		// Create
		BitSet message = generateRandomBitSet(64);

		// Informative output
		System.out.println("\nOriginal Message:\nM=" + bitSetString(message));

		// Creating commitment source
		System.out.print("Creating commitment source...");

		TimedCommitmentSource source = new TimedCommitmentSource(message, K);
		System.out.println(" done.");

		// Create commitment
		TimedCommitment commitment = source.generateCommitment();

		// Send hints
		System.out.print("Sending hints...");

		int numHints = Integer.parseInt(args[0]);
		assert (0 <= numHints) && (numHints <= K);

		for (int i = 2; i <= numHints; ++i) {
			commitment.addHint(source.generateHint(i));
		}

		System.out.println(" done.");

		// Decode message
		System.out.print("Decoding message...");

		BitSet decodedMessage = commitment.decodeMessage();
		System.out.println(" done.");

		// Informative output
		System.out.println();
		System.out.println("Decoded Message:");
		System.out.println("M=" + bitSetString(decodedMessage));

		// Verify correctness
		boolean success = message.equals(decodedMessage);

		// Informative output
		System.out.println();
		System.out.println("===");

		if (success) {
			System.out.println("Success, messages match.");
		} else {
			System.out.println("Failure, messages don't match!");
		}

		System.out.println("===");
	}

	private static String bitSetString(BitSet bs) {
		String result = "";

		for (int i = 0; i < bs.size(); ++i) {
			if (bs.get(i)) {
				result = result + "1";
			} else {
				result = result + "0";
			}
		}

		return result;
	}

	private static BitSet generateRandomBitSet(int length) {
		BitSet bs = new BitSet(length);

		for (int i = 0; i < length; ++i) {
			if (Math.random() < 0.5) {
				bs.set(i);
			} else {
				bs.clear(i);
			}
		}

		return bs;
	}

	//~ Static fields/initializers ---------------------------------------------

	// Value of k to use
	private static final int K = 80;
}


// end of Main.java
