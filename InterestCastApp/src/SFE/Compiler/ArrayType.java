// ArraType.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;

import java.util.Vector;


/**
 * Class ArraType is used for representing an Array that was defined in
 * the program.
 */
public class ArrayType extends ParentType {
	//~ Instance fields --------------------------------------------------------

	// data members

	/*
	 * Holds the array length
	 */
	private int length;

	/*
	 * the base type of the array
	 */
	private Type baseType;

	//~ Constructors -----------------------------------------------------------

	/**
	 * Constructs a new ArrayType object from a given length and
	 * base type.
	 * @param type the base type of this array.
	 * @param length the length of this array.
	 */
	public ArrayType(Type type, int length) {
		this.length       = length;
		this.baseType     = type;
	}

	//~ Methods ----------------------------------------------------------------

	/**
	 * Returns the length of the this ArrayType in bits.
	 * @return the length of the this ArrayType in bits.
	 */
	public int size() {
		return length * baseType.size();
	}

	/**
	 * Returns the length of this array.
	 * @return the length of this array.
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Returns a string representation of the this ArrayType.
	 * @return the string "array" as the string representation
	 *                 of this ArrayType.
	 */
	public String toString() {
		return "array";
	}

	/**
	 * Returns a String representing this object as it should
	 * appear in the format file.
	 * @return empty string. This method will not be called when
	 * writing the format file.
	 */
	public String toFormat() {
		return "";
	}

	/**
	 * Returns the base type of this ArrayType.
	 * @return the base type of this ArrayType.
	 */
	public Type getBaseType() {
		return baseType;
	}

	/**
	 * Returns a vector of all the derived lvalue (inluding this lvalue).
	 * Basically, the vector will hold this lvalue and all the
	 * entries lvalue.
	 * @param base the lvalue that called the this method (base.type == this)
	 */
	public Vector getDerivedLvalues(Lvalue base) {
		Vector result = new Vector();
		result.add(base);

		// array$ 
		String    arrayTop = base.getName() + "[$]";
		VarLvalue top = new VarLvalue(new Variable(arrayTop, baseType), false);
		result.addAll(baseType.getDerivedLvalues(top));

		for (int i = 0; i < length; i++) {
			ArrayEntryLvalue arrayEntryLval = new ArrayEntryLvalue(base, i);
			result.addAll(baseType.getDerivedLvalues(arrayEntryLval));
		}

		return result;
	}

	/**
	 * Returns a String representing this object as it should appear in
	 * the format file.
	 * @return a String representing this object as it should appear
	 * in the format file.
	 */
	public String toFormat(String parentName, Function function) {
		String str = new String();

		for (int i = 0; i < length; i++) {
			if (baseType instanceof StructType) {
				str += ((StructType) baseType).toFormat(parentName + "[" + i +
				                                        "]", function);
			} else if (baseType instanceof ArrayType) {
				str += ((ArrayType) baseType).toFormat(parentName + "[" + i +
				                                       "]", function);
			} else {
				// get input/ouput and alice/bob
				String params   = parentName + "[" + i + "]";
				String alicebob;

				if (params.startsWith("output.alice")) {
					alicebob = "Alice";
				} else {
					alicebob = "Bob";
				}

				// <Alice|Bob> <Input|Ouput> <type> <prompt(field name)> 
				// <'[' input bits ']'>
				str += (alicebob + " output " + baseType.toFormat() + " \"" +
				params + "\" [ ");

				for (int j = 0; j < baseType.size(); j++) {
					AssignmentStatement s =
						(AssignmentStatement) (Function.getVar(parentName +
						                                       "[" + i + "]$" +
						                                       j)
						                               .getAssigningStatement());
					str += (s.getOutputLine() + " ");
				}

				str += "]\n";
			}
		}

		return str;
	}

	/**
	 * Returns the name of the bit at offset i in the array.
	 * @return the name of the bit at offset i in the array.
	 */
	public String getNameAt(String baseName, int i) {
		int baseSize = baseType.size();
		int entry;
		int offset;

		if (i < (baseSize * length)) {
			entry      = i / baseSize;
			offset     = i % baseSize;
		} else { // sign expation
			entry      = length - 1;
			offset     = baseSize - 1;
		}

		if (baseType.hasDerives()) {
			return ((ParentType) baseType).getNameAt(baseName + "[" + entry +
			                                         "]", offset);
		}

		return baseName + "[" + entry + "]$" + offset;
	}
}
