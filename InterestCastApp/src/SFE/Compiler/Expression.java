// Expression.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;


/**
 * Abstract class for representing expressions that can be defined
 * in the program.
 */
public abstract class Expression {
	//~ Methods ----------------------------------------------------------------

	/**
	 * Returns the number of bits needed to represent this expression.
	 * @return the number of bits needed to represent this expression.
	 */
	public abstract int size();

	/**
	 * This method should be overriden by subclasses that can return a single bit
	 * from their expression. This implementations returns null.
	 * @return null
	 */
	public Expression bitAt(int i) {
		return null;
	}

	/**
	 * Recursivly calculates inner arithmetic expression. This implementation
	 * returns this. Expressions that return something more complicated
	 * should override this method.
	 * @param as the AssignmentStatement that holds this expression (as rhs).
	 * @param result the BlockStatement to hold the result in.
	 * @return the result expression.
	 */
	public Expression evaluateExpression(AssignmentStatement as,
	                                     BlockStatement result) {
		return this;
	}

	/**
	 * Returns this expression. Expression are not duplicated.
	 * @return this expression. Expression are not duplicated.
	 */
	public Expression duplicate() {
		return this;
	}

	/**
	 * Returns true if this exression has input pin that share an input.
	 * this method returns false. Expressions that something better to say,
	 * should override this method.
	 * @return false.
	 */
	public boolean hasSharedInput(Expression exp) {
		return false;
	}

	//~ Static fields/initializers ---------------------------------------------

	/*
	 * Holds the temp tag. This datamemeber is used by evaluateExpression, where
	 * a temp. variable is constructed.
	 */
	protected static int tempLabel = 0;
}
