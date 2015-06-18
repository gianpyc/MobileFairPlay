// IntConstant.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;


/**
 * The IntConstant class represents integer consts expressions that can
 * appear in the program.
 */
public class IntConstant extends ConstExpression {
	//~ Instance fields --------------------------------------------------------

	// data members

	/*
	 * Holds the integer constant of this IntConstant
	 */
	private int intConst;

	/*
	 * Holds the number of bit needed to store this intConst (size)
	 */
	private int size;

	//~ Constructors -----------------------------------------------------------

	/**
	 * Constructs a new IntConstant from a given integer const
	 * @param intConst the given integer constant
	 */
	public IntConstant(int intConst) {
		this.intConst = intConst;

		if (intConst == 0) {
			size = 2;
		} else {
			double a = log2(Math.abs(intConst));
			size = (int) a + 1; // +1 for representing the sign

			if (a > (int) a) { // we need an extra bit to represent intConst
				size++;
			}
		}
	}

	//~ Methods ----------------------------------------------------------------

	/*
	 * private method for calculation the binary logarithm (binary base)
	 * of a double value
	 * @param a number greater than 0.
	 * @return log2 of a
	 */
	public static double log2(double a) {
		return Math.log(a) / logE2;
	}

	/**
	 * Returns the number of bits needed to represent this expression.
	 * @return the number of bits needed to represent this expression.
	 */
	public int size() {
		return size;
	}

	/**
	 * Returns a string representation of the object.
	 * @return a string representation of the object.
	 */
	public String toString() {
		return Integer.toString(intConst);
	}

	/**
	 * Returns the value stored in this IntConstant
	 * @return the value stored in this IntConstant
	 */
	public int value() {
		return intConst;
	}

	/**
	 * Returns Expression that represents the bit at place i of this Expression
	 * @return Expression that represents the bit at place i of this Expression
	 */
	public Expression bitAt(int i) {
		if (size <= i) {
			// sign expantion
			return new BooleanConstant(intConst < 0); // return 1 (true) as sign expension 
		}

		boolean val = ((intConst >> i) & 1) == 1;

		return new BooleanConstant(val);
	}

	//~ Static fields/initializers ---------------------------------------------

	/*
	 * holds the natural logarithm of 2
	 */
	private static double logE2 = Math.log(2);
}
