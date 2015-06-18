// BooleanType.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;


/**
 * A class representing the boolean primitive type
 * that can be defined in the program.
 */
public class BooleanType extends Type {
	//~ Methods ----------------------------------------------------------------

	/**
	 * Returns the length of the this Boolean type in bits, which is 1.
	 * @return the length of the this Int type in bits.
	 */
	public int size() {
		return 1;
	}

	/**
	 * Returns a string representation of the object.
	 * @return a string representation of the object.
	 */
	public String toString() {
		return "Boolean";
	}

	/**
	 * Returns a string representation of the object for the format file.
	 * @return a string representation of the object for the format file.
	 */
	public String toFormat() {
		return "integer";
	}
}
