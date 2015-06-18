// BitLvalue.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;


/**
 * This class represents a bit of a VarLvalue
 */
public class BitLvalue extends Lvalue {
	//~ Instance fields --------------------------------------------------------

	// data member

	/*
	 * Holds the bit in the Lvalue that this bit lvalue represents.
	 */
	private int    bit;
	private Lvalue base;

	//~ Constructors -----------------------------------------------------------

	/**
	 * Constructs a new BitLvalue from a given VarLvalue and the offset in it.
	 * @param varLvalue the given VarLvalue.
	 * @param bit the bit offset in the VarLvalue.
	 */
	public BitLvalue(Lvalue base, int bit) {
		this.base         = base;
		this.bit          = bit;
		this.isOutput     = base.isOutput();
	}

	//~ Methods ----------------------------------------------------------------

	/**
	 * Returns 1 as size of this BitLvalue object in bits.
	 * @return 1 as size of this BitLvalue object in bits.
	 */
	public int size() {
		return 1;
	}

	/**
	 * Returns a string representation of this BitLvalue.
	 * @return a string representation of this BitLvalue.
	 */
	public String toString() {
		return base.getName() + "$" + bit;
	}

	/**
	 * Returns a string representation of this BitLvalue's name.
	 * @return a string representation of this BitLvalue's name.
	 */
	public String getName() {
		return base.getName() + "$" + bit;
	}

	/**
	 * Returns the base Type of this BitLvalue.
	 * @return the base Type of this BitLvalue.
	 */
	public Type getType() {
		return base.getType();
	}
}
