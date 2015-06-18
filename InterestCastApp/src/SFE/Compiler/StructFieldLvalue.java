// StructFieldLvalu.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;


/**
 * The VarLvalue class extends the Lvalue class, and can be
 * when a variable is used as Lvalue.
 */
public class StructFieldLvalue extends Lvalue {
	//~ Instance fields --------------------------------------------------------

	//data members

	/*
	 * The base Lvalue (lvalue has StructType type)
	 */
	private Lvalue base;

	/*
	 * The name of the field in the base's Struct that this
	 * StructFieldLvalue represents.
	 */
	private String field;

	//~ Constructors -----------------------------------------------------------

	/**
	 * Constructs a new StructFieldLvalue from a given lvalue and fild name.
	 * @param base the type of the field
	 * @param field field name
	 */
	public StructFieldLvalue(Lvalue base, String field) {
		this.base         = base;
		this.field        = field;
		this.isOutput     = base.isOutput();
	}

	//~ Methods ----------------------------------------------------------------

	/**
	 * Returns the Type of this Lvalue object.
	 * @return the Type of this lvalue object.
	 */
	public Type getType() {
		StructType baseType = (StructType) (base.getType());

		return baseType.fromFieldName(field);
	}

	/**
	 * Returns the size of this Lvalue object in bits.
	 * @return an integer representing size of this lvalue object in bits.
	 */
	public int size() {
		return getType().size();
	}

	/**
	 * Returns the name of the lvalue of this object.
	 * @return a string representing this lvalue's name.
	 */
	public String getName() {
		return base.getName() + "." + field;
	}

	/**
	 * Returns a string representation of the object.
	 * @return a string representation of the object.
	 */
	public String toString() {
		return base.getName() + "." + field;
	}

	/**
	 * Returns true is this variable is a part out the circuit's output.
	 * @return true is this variable is a part out the circuit's output.
	 */
	public boolean isOutput() {
		return isOutput;
	}

	/**
	 * Sets this struct as not output of the circuit
	 */
	public void notOutput() {
		this.isOutput = false; /*base.notOutput(); */
	}
}
