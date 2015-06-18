// Operator.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;


/**
 * Abstract class for representing an operator in the program.
 * All operators have a name (whose interpretation depends on
 * the subclass), as well as abstract functions for defining
 * the semantics of the particular operator subclass.
 */
public abstract class Operator {
	//~ Methods ----------------------------------------------------------------

	/**
	 * Returns the arity of the operator
	 * 1 for unary ops; 2 for binary ops; 3 for ternary ops; 0 for constants
	 * @return the arity of the operator
	 */
	public abstract int arity();

	/**
	 * Returns an int theat represents the priority of the operator
	 * @return an int theat represents the priority of the operator
	 */
	public abstract int priority();
}
