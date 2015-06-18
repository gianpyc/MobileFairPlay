// IntType.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;


/**
 * A class representing the integer primitive type of an arbitrary length.
 * An object of type Int contains a single field whose type
 * is int and represents the
 * int's length. An Int type of one bit is actualy a boolean variable.
 */
public class IntType extends Type {
	//~ Instance fields --------------------------------------------------------

	// data members

	/*
	 * Holds the length of this Int type
	 */
	private int length;

	//~ Constructors -----------------------------------------------------------

	/**
	 * Constructs an IntType object of a given length.
	 * @param length the Int type length.
	 */
	public IntType(int length) {
		this.length = length;
	}

	//~ Methods ----------------------------------------------------------------

	/**
	 * Returns the length of this Int type in bits.
	 * @return the length of this Int type in bits.
	 */
	public int size() {
		return length;
	}

	/**
	 * Returns a string representation of the object.
	 * @return a string representation of the object.
	 */
	public String toString() {
		return "Int<" + length + ">";
	}

	/**
	 * Returns a string representation of the object for the format file.
	 * @return a string representation of the object for the format file.
	 */
	public String toFormat() {
		return "integer";
	}
}
