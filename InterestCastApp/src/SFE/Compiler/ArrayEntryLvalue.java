// ArrayEntryLvalue.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;

/**
 * ArrayEntryLvalue represent as array entry l-value that can be defined
 * in a program. The ArrayEntryLvalue class extends the Lvalue class.
 */
public class ArrayEntryLvalue extends Lvalue {
	//~ Instance fields --------------------------------------------------------

	//data members

	/*
	 * The array Lvalue (lvalue has ArrayType type)
	 */
	private Lvalue array;

	/*
	 * The offset int the array.
	 */
	private int index;

	//~ Constructors -----------------------------------------------------------

	/**
	 * Constructs a new ArrayEntryLvalue from a given lvalue and index
	 * in the array.
	 * @param array the array's l-value.
	 * @param index the index of this ArrayEntryLvalue in the array.
	 */
	public ArrayEntryLvalue(Lvalue array, int index) {
		this.array        = array;
		this.index        = index;
		this.isOutput     = array.isOutput();
	}

	//~ Methods ----------------------------------------------------------------

	/**
	 * Returns the Type of this LArrayEntryLvalue object.
	 * @return the Type of this LArrayEntryLvalue object.
	 */
	public Type getType() {
		ArrayType arrayType = (ArrayType) (array.getType());

		return arrayType.getBaseType();
	}

	/**
	 * Returns the size of this ArrayEntryLvalue object in bits.
	 * @return an integer representing size of this ArrayEntryLvalue
	 *                 object in bits.
	 */
	public int size() {
		return getType().size();
	}

	/**
	 * Returns the name of the this ArrayEntryLvalue.
	 * @return a string representing this ArrayEntryLvalue's name.
	 */
	public String getName() {
		return array.getName() + "[" + index + "]";
	}

	/**
	 * Returns a string representation of this ArrayEntryLvalue.
	 */
	public String toString() {
		return array.getName() + "[" + index + "]";
	}

	/**
	 * Returns true is this ArrayEntryLvalue is a part out the circuit's output.
	 * @return true is this ArrayEntryLvalue is a part out the circuit's output.
	 */
	public boolean isOutput() {
		return isOutput;
	}

	/**
	 * Marks this ArrayEntryLvalue's pin as not output.
	 * This ArrayEntryLvalue is not an output pin the the result circuit.
	 */
	public void notOutput() {
		this.isOutput = false;
	}
}
