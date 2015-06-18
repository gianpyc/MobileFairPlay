// ConstExpression.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;


/**
 * The ConstExpression class represents consts expressions that
 * can appear in the program.
 */
public abstract class ConstExpression extends Expression {
	//~ Methods ----------------------------------------------------------------

	/**
	 * Returns the value stored in this constant expression
	 * @return the value stored in this constant expression
	 */
	public abstract int value();
}
