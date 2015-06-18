// Consts.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;

import java.util.HashMap;


/**
 * The Consts class stores the constants defeined in the program.
 */
public class Consts {
	//~ Methods ----------------------------------------------------------------

	/**
	 * Returns a ConstExpression representing the const of the specified type name, or null if there was no
	 * such type defined for this type name.
	 * @param name the name of the constant whose associated ConstExpression is to be returned.
	 * @return ConstExpression representing the const, or null if there was no such constant defined.
	 */
	public static ConstExpression fromName(String name) {
		return ((ConstExpression) (constsTable.get(name)));
	}

	/**
	 * Associates the specified new constant name with the specified integer constant.
	 * @param newConstName the new constant name with which the specified constant is to be associated.
	 * @param constant the constant to be associated with the specified newConstName.
	 * @throws IllegalArgumentException if the newConstName is already defined.
	 */
	public static void defineName(String newConstName, int constant)
	                       throws IllegalArgumentException
	{
		if (constsTable.containsValue(newConstName)) {
			throw new IllegalArgumentException();
		}

		constsTable.put(newConstName, new IntConstant(constant));
	}

	/**
	 * Associates the specified new boolean constant name with the specified boolean constant.
	 * @param newConstName the new boolean constant name with which the specified constant is to be associated.
	 * @param constant the boolean constant to be associated with the specified newConstName.
	 * @throws IllegalArgumentException if the newConstName is already defined.
	 */
	public static void defineName(String newConstName, boolean constant)
	                       throws IllegalArgumentException
	{
		if (constsTable.containsValue(newConstName)) {
			throw new IllegalArgumentException();
		}

		constsTable.put(newConstName, new BooleanConstant(constant));
	}

	/**
	 * Returns the size of the specified constant in bits.
	 * @param name the constant name.
	 * @return an integer representing size of the const, represented by the given name,
	 *                in bits or -1 if name is not a defined constant.
	 */
	public static int size(String name) {
		if (constsTable.containsValue(name)) {
			return ((ConstExpression) fromName(name)).size();
		}

		return -1;
	}

	//~ Static fields/initializers ---------------------------------------------

	// data members

	/*
	 * holds the constants defined in the program.
	 */
	private static HashMap constsTable = new HashMap();
}
