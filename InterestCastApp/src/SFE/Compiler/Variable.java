// Variable.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;


/**
 * A type representing a variable in the program. A variable is composed of its type and name.
 */
public class Variable {
	//~ Instance fields --------------------------------------------------------

	// data members

	/*
	 * The name of the variable.
	 */
	private String name;

	/*
	 * The type of the variable.
	 */
	private Type type;

	//~ Constructors -----------------------------------------------------------

	/**
	 * Constructs a new variable object of a given name and type.
	 * @param name the new variale name
	 * @param type the variable's type
	 */
	public Variable(String name, Type type) {
		this.name     = name;
		this.type     = type;
	}

	//~ Methods ----------------------------------------------------------------

	/**
	 * Returns the Type of this varable.
	 * @return the Type of this varable.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Returns a string representing the name of this variable.
	 * @return a string representing the name of this variable.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns an int representing the size of this variable object in bits.
	 * @return an int representing the size of this variable object in bits.
	 */
	public int size() {
		return type.size();
	}

	/**
	 * Returns a string representation of the object.
	 * @return a string representation of the object.
	 */
	public String toString() {
		return type.toString() + " " + name;
	}
}
