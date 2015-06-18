// TimedCommitmentHint.java
//
// class TimedCommitmentHint
// A hint that reduces the time needed to decode a timed commitment by a factor
// of 2.
//
// Created November 20, 2003 by Ori Peleg
//
// $Id: TimedCommitmentHint.java,v 1.5 2004/07/04 19:11:00 orip Exp $
package SFE.TimedCommitments;

import java.math.BigInteger;


class TimedCommitmentHint implements Comparable {
	//~ Instance fields --------------------------------------------------------

	// data members
	final Integer    index;
	final BigInteger value;

	//~ Constructors -----------------------------------------------------------

	/**
	 * package-visible constructor.
	 *
	 * @param index the hint's index, should be in [1, ..., k-1]
	 * @param value the hint's value, see Boneh-Naor's algorithm for details
	 */
	TimedCommitmentHint(Integer index, BigInteger value) {
		this.index     = index;
		this.value     = value;
	}

	/**
	 * Convenience constructor, index can be an int
	 * @see TimedCommitmentHint
	 */
	TimedCommitmentHint(int index, BigInteger value) {
		this(new Integer(index), value);
	}

	//~ Methods ----------------------------------------------------------------

	// compareTo for Comparable interface, for use in a SortedSet
	public int compareTo(Object o) {
		TimedCommitmentHint other = (TimedCommitmentHint) o;

		int                 indexCompare = this.index.compareTo(other.index);

		if (indexCompare != 0) {
			return indexCompare;
		} else {
			return this.value.compareTo(other.value);
		}
	}

	// equals, for use in a SortedSet
	public boolean equals(Object o) {
		TimedCommitmentHint other = (TimedCommitmentHint) o;

		return (this.index.equals(other.index) &&
		       this.value.equals(other.value));
	}
}


// end of file TimedCommitmentHint.java
