// Statement.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;


/**
 * Abstract class for representing statements that can be defined
 * in the program.
 */
public abstract class Statement implements Multi2SingleBit {
	//~ Methods ----------------------------------------------------------------

	// data members

	/**
	 * Unique vars transformations.
	 */
	public abstract Statement uniqueVars();

	/**
	 * This function returns always false (except for assignment statement)
	 * @return false
	 */
	public boolean hasUnaryOperator() {
		return false;
	}

	/**
	 * returns a replica of this statement.
	 * @return a replica of this statement.
	 */
	public abstract Statement duplicate();
}
