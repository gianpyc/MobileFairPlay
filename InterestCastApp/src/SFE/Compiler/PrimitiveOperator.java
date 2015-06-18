// PrimitiveOperator.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;

import java.io.PrintWriter;


/**
 * The PrimitiveOperator class represents a primitive operator in the program.
 * A PrimitiveOperator contains from one up to three input bits and one output bit.
 */
public class PrimitiveOperator extends Operator implements OutputWriter {
	//~ Instance fields --------------------------------------------------------

	// data members
	public int operator;

	/*
	 * an array holding the truth table of the operator
	 */
	private boolean[] truthTable;

	//~ Constructors -----------------------------------------------------------

	/**
	 * Construct a new PrimitiveOperator from a given int that
	 * represents the operator.
	 * @param truthTable the int that represents the truth table.
	 * @param midSize the size of the operand in case of unary operator.
	 */
	public PrimitiveOperator(int truthTable) throws IllegalArgumentException {
		this(PRIMITIVE_TRUTH_TABLES[truthTable]);
		operator = truthTable;
	}

	/**
	 * Construct a new PrimitiveOperator from a given truth table.
	 * @param truthTable the truth table that repesents the operator.
	 */
	public PrimitiveOperator(boolean[] truthTable)
	                  throws IllegalArgumentException
	{
		// check the length validity of the truth table
		if ((truthTable.length != 8) && (truthTable.length != 4) &&
			    (truthTable.length != 2)) {
			throw new IllegalArgumentException();
		}

		this.truthTable = truthTable;
	}

	//~ Methods ----------------------------------------------------------------

	/**
	 * Creates a new PrimitiveOperator, from this PrimitiveOpertor,
	 * that represents the complete boolean function of this PrimitiveOpertor.
	 * @return PrimitiveOperator that represents the complete boolean function of this PrimitiveOpertor.
	 */
	public PrimitiveOperator negOut() {
		boolean[] newTruthTable = new boolean[truthTable.length];

		for (int i = 0; i < truthTable.length; i++)
			newTruthTable[i] = ! truthTable[i];

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * Creates a new PrimitiveOperator from this PrimitiveOpertor's truth table when the
	 * left input bit is negated.
	 * @return The newly created PrimitiveOperator.
	 */
	public PrimitiveOperator negLeft() {
		boolean[] newTruthTable = new boolean[truthTable.length];

		if (truthTable.length == 4) {
			newTruthTable[0]     = truthTable[2]; // 00->10
			newTruthTable[1]     = truthTable[3]; // 01->11
			newTruthTable[2]     = truthTable[0]; // 10->00
			newTruthTable[3]     = truthTable[1]; // 11->01
		} else { // length == 8 
			newTruthTable[0]     = truthTable[4]; // 000->100
			newTruthTable[1]     = truthTable[5]; // 001->101
			newTruthTable[2]     = truthTable[6]; // 010->110
			newTruthTable[3]     = truthTable[7]; // 011->111
			newTruthTable[4]     = truthTable[0]; // 100->000
			newTruthTable[5]     = truthTable[1]; // 101->001
			newTruthTable[6]     = truthTable[2]; // 110->010
			newTruthTable[7]     = truthTable[3]; // 111->011
		}

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * Creates a new PrimitiveOperator from this PrimitiveOpertor's truth table when the
	 * middle input bit is negated.
	 * @return The newly created PrimitiveOperator.
	 */
	public PrimitiveOperator negMid() {
		boolean[] newTruthTable = new boolean[truthTable.length];

		if (truthTable.length == 2) {
			newTruthTable[0]     = truthTable[1]; // 0->1
			newTruthTable[1]     = truthTable[0]; // 1->0 
		} else { // length == 8 
			newTruthTable[0]     = truthTable[2]; // 000->010
			newTruthTable[1]     = truthTable[3]; // 001->011
			newTruthTable[2]     = truthTable[0]; // 010->000
			newTruthTable[3]     = truthTable[1]; // 011->001
			newTruthTable[4]     = truthTable[6]; // 100->110
			newTruthTable[5]     = truthTable[7]; // 101->111
			newTruthTable[6]     = truthTable[4]; // 110->100
			newTruthTable[7]     = truthTable[5]; // 111->101
		}

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * Creates a new PrimitiveOperator from this PrimitiveOpertor's truth table when the
	 * right input bit is negated.
	 * @return The newly created PrimitiveOperator.
	 */
	public PrimitiveOperator negRight() {
		boolean[] newTruthTable = new boolean[truthTable.length];

		if (truthTable.length == 4) {
			newTruthTable[0]     = truthTable[1]; // 00->01
			newTruthTable[1]     = truthTable[0]; // 01->00
			newTruthTable[2]     = truthTable[3]; // 10->11
			newTruthTable[3]     = truthTable[2]; // 11->10
		} else { // length == 8 
			newTruthTable[0]     = truthTable[1]; // 000->001
			newTruthTable[1]     = truthTable[0]; // 001->000
			newTruthTable[2]     = truthTable[3]; // 010->011
			newTruthTable[3]     = truthTable[2]; // 011->010
			newTruthTable[4]     = truthTable[5]; // 100->101
			newTruthTable[5]     = truthTable[4]; // 101->100
			newTruthTable[6]     = truthTable[7]; // 110->111
			newTruthTable[7]     = truthTable[6]; // 111->110
		}

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * Creates a new PrimitiveOperator from this PrimitiveOpertor's truth table where the
	 * right input bit is the constant zero (false).
	 * @return The newly created PrimitiveOperator.
	 */
	public PrimitiveOperator zeroRight() {
		boolean[] newTruthTable = new boolean[truthTable.length / 2];

		// note that the right bit is alway the LSB i.e. bit #0
		for (int i = 0; i < newTruthTable.length; i++)
			newTruthTable[i] = truthTable[i << 1]; // i << 1 ==> i0

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * Creates a new PrimitiveOperator from this PrimitiveOpertor's truth table where the
	 * right input bit is the constant one (true).
	 * @return The newly created PrimitiveOperator.
	 */
	public PrimitiveOperator oneRight() {
		boolean[] newTruthTable = new boolean[truthTable.length / 2];

		// note that the right bit is alway the LSB i.e. bit #0
		for (int i = 0; i < newTruthTable.length; i++)
			newTruthTable[i] = truthTable[(i << 1) + 1]; // (i << 1)+1 ==> (i0)+1 ==>i1

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * Creates a new PrimitiveOperator from this PrimitiveOpertor's truth table where the
	 * left input bit is the constant zero (false).
	 * @return The newly created PrimitiveOperator.
	 */
	public PrimitiveOperator zeroLeft() {
		boolean[] newTruthTable = new boolean[truthTable.length / 2];

		// note that the left bit is alway the MSB i.e. bit #1 (binary) or #2 (trinary)
		// either way copy the fist half of the truthtable
		for (int i = 0; i < newTruthTable.length; i++)
			newTruthTable[i] = truthTable[i];

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * Creates a new PrimitiveOperator from this PrimitiveOpertor's truth table where the
	 * left input bit is the constant one (true).
	 * @return The newly created PrimitiveOperator.
	 */
	public PrimitiveOperator oneLeft() {
		boolean[] newTruthTable = new boolean[truthTable.length / 2];

		// note that the left bit is alway the MSB i.e. bit #1 (binary) or #2 (trinary)
		// either way copy the second half of the truthtable
		for (int i = 0; i < newTruthTable.length; i++)
			newTruthTable[i] = truthTable[i + newTruthTable.length];

		return new PrimitiveOperator(newTruthTable);
	}

	/* Creates a new PrimitiveOperator from this PrimitiveOpertor's truth table where the
	 * middle input bit is the constant zero (false).
	 * Note that this method will be called only for trinary gates (in the optimization stage)
	 * @return The newly created PrimitiveOperator.
	 */
	public PrimitiveOperator zeroMid() {
		boolean[] newTruthTable = new boolean[4];

		newTruthTable[0]     = truthTable[0]; // 000
		newTruthTable[1]     = truthTable[1]; // 001
		newTruthTable[2]     = truthTable[4]; // 100
		newTruthTable[3]     = truthTable[5]; // 101

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * Creates a new PrimitiveOperator from this PrimitiveOpertor's truth table where the
	 * middle input bit is the constant one (true).
	 * Note that this method will be called only for trinary gates (in the optimization stage)
	 * @return The newly created PrimitiveOperator.
	 */
	public PrimitiveOperator oneMid() {
		boolean[] newTruthTable = new boolean[4];

		newTruthTable[0]     = truthTable[2]; // 010
		newTruthTable[1]     = truthTable[3]; // 011
		newTruthTable[2]     = truthTable[6]; // 110
		newTruthTable[3]     = truthTable[7]; // 111

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * Creates a new PrimitiveOperator from this PrimitiveOpertor's truth table where the
	 * left and the right inputs are assumed to be equal. Note: the two privious input will be
	 * the left input of the result (or midle if the combination yields an unary  operator).
	 */
	public PrimitiveOperator equalLeftRight() {
		boolean[] newTruthTable = new boolean[truthTable.length / 2];

		if (newTruthTable.length == 2) {
			// binary to unary
			newTruthTable[0]     = truthTable[0]; // 00
			newTruthTable[1]     = truthTable[3]; // 11
		} else {
			// trinary to binary
			newTruthTable[0]     = truthTable[0]; // 000
			newTruthTable[1]     = truthTable[2]; // 010
			newTruthTable[2]     = truthTable[5]; // 101
			newTruthTable[3]     = truthTable[7]; // 111
		}

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * Creates a new PrimitiveOperator from this PrimitiveOpertor's truth table where the
	 * left and the middle inputs are assumed to be equal. Note: the two privious input will be
	 * the left input of the result. also note that method is called only for trinary operator.
	 */
	public PrimitiveOperator equalLeftMid() {
		boolean[] newTruthTable = new boolean[4];

		// trinary to binary
		newTruthTable[0]     = truthTable[0]; // 000
		newTruthTable[1]     = truthTable[1]; // 001
		newTruthTable[2]     = truthTable[6]; // 110
		newTruthTable[3]     = truthTable[7]; // 111

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * Creates a new PrimitiveOperator from this PrimitiveOpertor's truth table where the
	 * right and the middle inputs are assumed to be equal. Note: the two privious input will be
	 * the right input of the result. also note that method is called only for trinary operator.
	 */
	public PrimitiveOperator equalRightMid() {
		boolean[] newTruthTable = new boolean[4];

		// trinary to binary
		newTruthTable[0]     = truthTable[0]; // 000
		newTruthTable[1]     = truthTable[3]; // 011
		newTruthTable[2]     = truthTable[4]; // 100
		newTruthTable[3]     = truthTable[7]; // 111

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * Creates a new PrimitiveOperator from this PrimitiveOpertor's truth table where the
	 * left anright inputs are switched.
	 */
	public PrimitiveOperator switchRightLeft() {
		boolean[] newTruthTable = new boolean[truthTable.length];

		if (newTruthTable.length == 4) {
			// binary operator
			newTruthTable[0]     = truthTable[0]; // 00
			newTruthTable[1]     = truthTable[2]; // 10
			newTruthTable[2]     = truthTable[1]; // 01
			newTruthTable[3]     = truthTable[3]; // 11
		} else {
			// trinary operator
			newTruthTable[0]     = truthTable[0]; // 000
			newTruthTable[1]     = truthTable[4]; // 100
			newTruthTable[2]     = truthTable[2]; // 010
			newTruthTable[3]     = truthTable[6]; // 110
			newTruthTable[4]     = truthTable[1]; // 001
			newTruthTable[5]     = truthTable[5]; // 101
			newTruthTable[6]     = truthTable[3]; // 011
			newTruthTable[7]     = truthTable[7]; // 111
		}

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * Creates a new PrimitiveOperator from this PrimitiveOpertor's truth table where the
	 * left and middle inputs are switched.
	 * Note that method is called only for trinary operator.
	 */
	public PrimitiveOperator switchLeftMid() {
		boolean[] newTruthTable = new boolean[8];

		// trinary operator
		newTruthTable[0]     = truthTable[0]; // 000
		newTruthTable[1]     = truthTable[1]; // 001
		newTruthTable[2]     = truthTable[4]; // 100
		newTruthTable[3]     = truthTable[5]; // 101
		newTruthTable[4]     = truthTable[2]; // 010
		newTruthTable[5]     = truthTable[3]; // 011
		newTruthTable[6]     = truthTable[6]; // 110
		newTruthTable[7]     = truthTable[7]; // 111

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * Creates a new PrimitiveOperator from this PrimitiveOpertor's truth table where the
	 * right and middle inputs are switched.
	 * Note that method is called only for trinary operator.
	 */
	public PrimitiveOperator switchRightMid() {
		boolean[] newTruthTable = new boolean[8];

		// trinary operator
		newTruthTable[0]     = truthTable[0]; // 000
		newTruthTable[1]     = truthTable[2]; // 010
		newTruthTable[2]     = truthTable[1]; // 001
		newTruthTable[3]     = truthTable[3]; // 011
		newTruthTable[4]     = truthTable[4]; // 100
		newTruthTable[5]     = truthTable[6]; // 110
		newTruthTable[6]     = truthTable[5]; // 101
		newTruthTable[7]     = truthTable[7]; // 111

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * Returns the arity of the operator
	 * 1 for unary ops; 2 for binary ops; 3 for terinary ops;
	 * @return the arity of the operator
	 */
	public int arity() {
		if (truthTable.length == 8) {
			return 3;
		} else if (truthTable.length == 4) {
			return 2;
		}

		return 1;
	}

	/**
	 * Returns an int theat represents the priority of the operator
	 * @return an int theat represents the priority of the operator
	 */
	public int priority() {
		return 0;
	}

	/**
	 * Returns a string representing this object as it appear at the
	 * output circuit.
	 * @return a string representing this object as it appear at the
	 * output circuit.
	 */
	public void toCircuit(PrintWriter circuit) {
		circuit.print("gate arity " + arity() + " table [ ");

		for (int i = 0; i < truthTable.length; i++)
			circuit.print(((truthTable[i]) ? '1' : '0') + " ");

		circuit.print("]");
	}

	/**
	 * Returns a string representation of the object.
	 */
	public String toString() {
		String str = "gate arity " + arity() + " table [ ";

		for (int i = 0; i < truthTable.length; i++)
			str += (((truthTable[i]) ? '1' : '0') + " ");

		str += "]";

		return str;
	}

	/**
	 * Returns true if this operator is unary operator and is the identity function.
	 * @return true if this operator is unary operator and is the identity function.
	 */
	public boolean isID() {
		return (truthTable.length == 2) &&
		       (truthTable[0] == PRIMITIVE_TRUTH_TABLES[ID_OP][0]) &&
		       (truthTable[1] == PRIMITIVE_TRUTH_TABLES[ID_OP][1]);
	}

	/**
	 * Returns true if this operator is unary operator and is the booelan function NOT.
	 * @return true if this operator is unary operator and is the booelan function NOT.
	 */
	public boolean isNot() {
		return (truthTable.length == 2) &&
		       (truthTable[0] == PRIMITIVE_TRUTH_TABLES[NOT_OP][0]) &&
		       (truthTable[1] == PRIMITIVE_TRUTH_TABLES[NOT_OP][1]);
	}

	/**
	 * Returns true if this operator is unary operator and is the booelan function output is constant one.
	 * @return true if this operator is unary operator and is the booelan function output is constant one.
	 */
	public boolean isOne() {
		for (int i = 0; i < truthTable.length; i++)
			if (truthTable[i] == false) {
				return false;
			}

		return true;
	}

	/**
	 * Returns true if this operator is unary operator and is the booelan function output is constant one.
	 * @return true if this operator is unary operator and is the booelan function output is constant one.
	 */
	public boolean isZero() {
		for (int i = 0; i < truthTable.length; i++)
			if (truthTable[i] == true) {
				return false;
			}

		return true;
	}

	/**
	 * returns true if gate is Trinary or Binary and ID on the left pin.
	 * @return true if gate is Trinary or Binary and ID on the left pin.
	 */
	public boolean isComplexIDOfLeft() {
		if (truthTable.length == 4) {
			return (truthTable[0] == false) && (truthTable[1] == false) &&
			       (truthTable[2] == true) && (truthTable[3] == true);
		}

		//else
		return (truthTable[0] == false) && (truthTable[1] == false) &&
		       (truthTable[2] == false) && (truthTable[3] == false) &&
		       (truthTable[4] == true) && (truthTable[5] == true) &&
		       (truthTable[6] == true) && (truthTable[7] == true);
	}

	/**
	 * returns true if gate is Trinary or Binary and ID on the left pin.
	 * Trinary or Binary gates.
	 * @return true if gate is Trinary or Binary and ID on the left pin.
	 */
	public boolean isComplexNotOfLeft() {
		if (truthTable.length == 4) {
			return (truthTable[0] == true) && (truthTable[1] == true) &&
			       (truthTable[2] == false) && (truthTable[3] == false);
		}

		//else
		return (truthTable[0] == true) && (truthTable[1] == true) &&
		       (truthTable[2] == true) && (truthTable[3] == true) &&
		       (truthTable[4] == false) && (truthTable[5] == false) &&
		       (truthTable[6] == false) && (truthTable[7] == false);
	}

	/**
	 * same as isComplexIDOfLeft but on right pin
	 * @return same as isComplexIDOfLeft but on right pin
	 */
	public boolean isComplexIDOfRight() {
		if (truthTable.length == 4) {
			return (truthTable[0] == false) && (truthTable[1] == true) &&
			       (truthTable[2] == false) && (truthTable[3] == true);
		}

		//else
		return (truthTable[0] == false) && (truthTable[1] == true) &&
		       (truthTable[2] == false) && (truthTable[3] == true) &&
		       (truthTable[4] == false) && (truthTable[5] == true) &&
		       (truthTable[6] == false) && (truthTable[7] == true);
	}

	/**
	 * same as isComplexNotOfLeft but on right pin
	 * @return same as isComplexNotOfLeft but on right pin
	 */
	public boolean isComplexNotOfRight() {
		if (truthTable.length == 4) {
			return (truthTable[0] == true) && (truthTable[1] == false) &&
			       (truthTable[2] == true) && (truthTable[3] == false);
		}

		//else
		return (truthTable[0] == true) && (truthTable[1] == false) &&
		       (truthTable[2] == true) && (truthTable[3] == false) &&
		       (truthTable[4] == true) && (truthTable[5] == false) &&
		       (truthTable[6] == true) && (truthTable[7] == false);
	}

	/**
	 * same as isComplexIDOfLeft but on right pin
	 * @return same as isComplexIDOfLeft but on right pin
	 */
	public boolean isComplexIDOfMiddle() {
		return (truthTable[0] == false) && (truthTable[1] == false) &&
		       (truthTable[2] == true) && (truthTable[3] == true) &&
		       (truthTable[4] == false) && (truthTable[5] == false) &&
		       (truthTable[6] == true) && (truthTable[7] == true);
	}

	/**
	 *  same as isComplexNotOfLeft but on middle pin
	 * @return same as isComplexNotOfLeft but on midle pin
	 */
	public boolean isComplexNotOfMiddle() {
		return (truthTable[0] == true) && (truthTable[1] == true) &&
		       (truthTable[2] == false) && (truthTable[3] == false) &&
		       (truthTable[4] == true) && (truthTable[5] == true) &&
		       (truthTable[6] == false) && (truthTable[7] == false);
	}

	/**
	 * combines this op and other op on the right pin and returns the result.
	 * @param other the op.
	 * @return the result gate.
	 */
	public PrimitiveOperator combineRightNRight(PrimitiveOperator other) {
		boolean[] newTruthTable = new boolean[4];

		// the other gate is on the left side - on the MSB
		newTruthTable[0]     = truthTable[((other.truthTable[0]) ? 2 : 0) | 0]; //00
		newTruthTable[1]     = truthTable[((other.truthTable[1]) ? 2 : 0) | 1]; //01
		newTruthTable[2]     = truthTable[((other.truthTable[2]) ? 2 : 0) | 0]; //10
		newTruthTable[3]     = truthTable[((other.truthTable[3]) ? 2 : 0) | 1]; //11

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * same as combineRightNRight but  on the this right and other left pin
	 * @param other the op.
	 * @return the result gate.
	 */
	public PrimitiveOperator combineRightNLeft(PrimitiveOperator other) {
		boolean[] newTruthTable = new boolean[4];

		// the other gate is on the left side - on the MSB
		newTruthTable[0]     = truthTable[((other.truthTable[0]) ? 2 : 0) | 0]; //00 -->00
		newTruthTable[1]     = truthTable[((other.truthTable[2]) ? 2 : 0) | 1]; //01 -->10
		newTruthTable[2]     = truthTable[((other.truthTable[1]) ? 2 : 0) | 0]; //10 -->01
		newTruthTable[3]     = truthTable[((other.truthTable[3]) ? 2 : 0) | 1]; //11 -->11

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * same as combineRightNRight but  on the this left and other right pins
	 * @param other the op.
	 * @return the result gate.
	 */
	public PrimitiveOperator combineLeftNRight(PrimitiveOperator other) {
		boolean[] newTruthTable = new boolean[4];

		// the other gate is on the left side - on the LSB
		newTruthTable[0]     = truthTable[((other.truthTable[0]) ? 1 : 0) | 0]; //00
		newTruthTable[1]     = truthTable[((other.truthTable[2]) ? 1 : 0) | 2]; //01 -->10
		newTruthTable[2]     = truthTable[((other.truthTable[1]) ? 1 : 0) | 0]; //10 -->01
		newTruthTable[3]     = truthTable[((other.truthTable[3]) ? 1 : 0) | 2]; //11

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	  * same as combineRightNRight but  on the this left and other left pins
	  * @param other the op.
	  * @return the result gate.
	  */
	public PrimitiveOperator combineLeftNLeft(PrimitiveOperator other) {
		boolean[] newTruthTable = new boolean[4];

		// the other gate is on the left side - on the LSB
		newTruthTable[0]     = truthTable[((other.truthTable[0]) ? 1 : 0) | 0]; //00
		newTruthTable[1]     = truthTable[((other.truthTable[1]) ? 1 : 0) | 0]; //01 
		newTruthTable[2]     = truthTable[((other.truthTable[2]) ? 1 : 0) | 2]; //10 
		newTruthTable[3]     = truthTable[((other.truthTable[3]) ? 1 : 0) | 2]; //11

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * same as combineRightNRight but  on the this right and other right pins
	  * @param other the op.
	  * @return the result gate.
	  */
	public PrimitiveOperator combineRightNRightOnLeft(PrimitiveOperator other) {
		boolean[] newTruthTable = new boolean[8];

		// the other gate is on the left side - on the MSB
		newTruthTable[0]     = truthTable[((other.truthTable[0]) ? 4 : 0) | 0]; //000
		newTruthTable[1]     = truthTable[((other.truthTable[1]) ? 4 : 0) | 1]; //001 
		newTruthTable[2]     = truthTable[((other.truthTable[0]) ? 4 : 0) | 2]; //010 
		newTruthTable[3]     = truthTable[((other.truthTable[1]) ? 4 : 0) | 3]; //011
		newTruthTable[4]     = truthTable[((other.truthTable[2]) ? 4 : 0) | 0]; //100
		newTruthTable[5]     = truthTable[((other.truthTable[3]) ? 4 : 0) | 1]; //101 
		newTruthTable[6]     = truthTable[((other.truthTable[2]) ? 4 : 0) | 2]; //110 
		newTruthTable[7]     = truthTable[((other.truthTable[3]) ? 4 : 0) | 3]; //111

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * same as combineRightNRightOnLeft but on appropraite pins
	  * @param other the op.
	  * @return the result gate.
	  */
	public PrimitiveOperator combineRightNLeftOnLeft(PrimitiveOperator other) {
		boolean[] newTruthTable = new boolean[8];

		// the other gate is on the left side - on the MSB
		newTruthTable[0]     = truthTable[((other.truthTable[0]) ? 4 : 0) | 0]; //000
		newTruthTable[1]     = truthTable[((other.truthTable[2]) ? 4 : 0) | 1]; //001 
		newTruthTable[2]     = truthTable[((other.truthTable[0]) ? 4 : 0) | 2]; //010 
		newTruthTable[3]     = truthTable[((other.truthTable[2]) ? 4 : 0) | 3]; //011
		newTruthTable[4]     = truthTable[((other.truthTable[1]) ? 4 : 0) | 0]; //100
		newTruthTable[5]     = truthTable[((other.truthTable[3]) ? 4 : 0) | 1]; //101 
		newTruthTable[6]     = truthTable[((other.truthTable[1]) ? 4 : 0) | 2]; //110 
		newTruthTable[7]     = truthTable[((other.truthTable[3]) ? 4 : 0) | 3]; //111

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * same as combineRightNRightOnLeft but on appropraite pins
	 * @param other the op.
	 * @return the result gate.
	 */
	public PrimitiveOperator combineMidNRightOnLeft(PrimitiveOperator other) {
		boolean[] newTruthTable = new boolean[8];

		// the other gate is on the left side - on the MSB
		newTruthTable[0]     = truthTable[((other.truthTable[0]) ? 4 : 0) | 0]; //000
		newTruthTable[1]     = truthTable[((other.truthTable[0]) ? 4 : 0) | 1]; //001 
		newTruthTable[2]     = truthTable[((other.truthTable[1]) ? 4 : 0) | 2]; //010 
		newTruthTable[3]     = truthTable[((other.truthTable[1]) ? 4 : 0) | 3]; //011
		newTruthTable[4]     = truthTable[((other.truthTable[2]) ? 4 : 0) | 0]; //100
		newTruthTable[5]     = truthTable[((other.truthTable[2]) ? 4 : 0) | 1]; //101 
		newTruthTable[6]     = truthTable[((other.truthTable[3]) ? 4 : 0) | 2]; //110 
		newTruthTable[7]     = truthTable[((other.truthTable[3]) ? 4 : 0) | 3]; //111

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * same as combineRightNRightOnLeft but on appropraite pins
	 * @param other the op.
	 * @return the result gate.
	 */
	public PrimitiveOperator combineMidNLeftOnLeft(PrimitiveOperator other) {
		boolean[] newTruthTable = new boolean[8];

		// the other gate is on the left side - on the MSB
		newTruthTable[0]     = truthTable[((other.truthTable[0]) ? 4 : 0) | 0]; //000
		newTruthTable[1]     = truthTable[((other.truthTable[0]) ? 4 : 0) | 1]; //001 
		newTruthTable[2]     = truthTable[((other.truthTable[2]) ? 4 : 0) | 2]; //010 
		newTruthTable[3]     = truthTable[((other.truthTable[2]) ? 4 : 0) | 3]; //011
		newTruthTable[4]     = truthTable[((other.truthTable[1]) ? 4 : 0) | 0]; //100
		newTruthTable[5]     = truthTable[((other.truthTable[1]) ? 4 : 0) | 1]; //101 
		newTruthTable[6]     = truthTable[((other.truthTable[3]) ? 4 : 0) | 2]; //110 
		newTruthTable[7]     = truthTable[((other.truthTable[3]) ? 4 : 0) | 3]; //111

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * same as combineRightNRightOnLeft but on appropraite pins
	 * @param other the op.
	 * @return the result gate.
	 */
	public PrimitiveOperator combineRightNRightOnMid(PrimitiveOperator other) {
		boolean[] newTruthTable = new boolean[8];

		newTruthTable[0]     = truthTable[((other.truthTable[0]) ? 2 : 0) | 0]; //000
		newTruthTable[1]     = truthTable[((other.truthTable[1]) ? 2 : 0) | 1]; //001 
		newTruthTable[2]     = truthTable[((other.truthTable[2]) ? 2 : 0) | 0]; //010 
		newTruthTable[3]     = truthTable[((other.truthTable[3]) ? 2 : 0) | 1]; //011
		newTruthTable[4]     = truthTable[((other.truthTable[0]) ? 2 : 0) | 4]; //100
		newTruthTable[5]     = truthTable[((other.truthTable[1]) ? 2 : 0) | 5]; //101 
		newTruthTable[6]     = truthTable[((other.truthTable[2]) ? 2 : 0) | 4]; //110 
		newTruthTable[7]     = truthTable[((other.truthTable[3]) ? 2 : 0) | 5]; //111

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * same as combineRightNRightOnLeft but on appropraite pins
	 * @param other the op.
	 * @return the result gate.
	 */
	public PrimitiveOperator combineRightNLeftOnMin(PrimitiveOperator other) {
		boolean[] newTruthTable = new boolean[8];

		newTruthTable[0]     = truthTable[((other.truthTable[0]) ? 2 : 0) | 0]; //000
		newTruthTable[1]     = truthTable[((other.truthTable[2]) ? 2 : 0) | 1]; //001 
		newTruthTable[2]     = truthTable[((other.truthTable[1]) ? 2 : 0) | 0]; //010 
		newTruthTable[3]     = truthTable[((other.truthTable[3]) ? 2 : 0) | 1]; //011
		newTruthTable[4]     = truthTable[((other.truthTable[0]) ? 2 : 0) | 4]; //100
		newTruthTable[5]     = truthTable[((other.truthTable[2]) ? 2 : 0) | 5]; //101 
		newTruthTable[6]     = truthTable[((other.truthTable[1]) ? 2 : 0) | 4]; //110 
		newTruthTable[7]     = truthTable[((other.truthTable[3]) ? 2 : 0) | 5]; //111

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * same as combineRightNRightOnLeft but on appropraite pins
	 * @param other the op.
	 * @return the result gate.
	 */
	public PrimitiveOperator combineLeftNRightOnMid(PrimitiveOperator other) {
		boolean[] newTruthTable = new boolean[8];

		newTruthTable[0]     = truthTable[((other.truthTable[0]) ? 2 : 0) | 0]; //000
		newTruthTable[1]     = truthTable[((other.truthTable[0]) ? 2 : 0) | 1]; //001 
		newTruthTable[2]     = truthTable[((other.truthTable[2]) ? 2 : 0) | 0]; //010 
		newTruthTable[3]     = truthTable[((other.truthTable[2]) ? 2 : 0) | 1]; //011
		newTruthTable[4]     = truthTable[((other.truthTable[1]) ? 2 : 0) | 4]; //100
		newTruthTable[5]     = truthTable[((other.truthTable[1]) ? 2 : 0) | 5]; //101 
		newTruthTable[6]     = truthTable[((other.truthTable[3]) ? 2 : 0) | 4]; //110 
		newTruthTable[7]     = truthTable[((other.truthTable[3]) ? 2 : 0) | 5]; //111

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * same as combineRightNRightOnLeft but on appropraite pins
	 * @param other the op.
	 * @return the result gate.
	 */
	public PrimitiveOperator combineLeftNLeftOnMid(PrimitiveOperator other) {
		boolean[] newTruthTable = new boolean[8];

		newTruthTable[0]     = truthTable[((other.truthTable[0]) ? 2 : 0) | 0]; //000
		newTruthTable[1]     = truthTable[((other.truthTable[0]) ? 2 : 0) | 1]; //001 
		newTruthTable[2]     = truthTable[((other.truthTable[1]) ? 2 : 0) | 0]; //010 
		newTruthTable[3]     = truthTable[((other.truthTable[1]) ? 2 : 0) | 1]; //011
		newTruthTable[4]     = truthTable[((other.truthTable[2]) ? 2 : 0) | 4]; //100
		newTruthTable[5]     = truthTable[((other.truthTable[2]) ? 2 : 0) | 5]; //101 
		newTruthTable[6]     = truthTable[((other.truthTable[3]) ? 2 : 0) | 4]; //110 
		newTruthTable[7]     = truthTable[((other.truthTable[3]) ? 2 : 0) | 5]; //111

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	  * same as combineRightNRightOnLeft but on appropraite pins
	  * @param other the op.
	  * @return the result gate.
	  */
	public PrimitiveOperator combineMidNRightOnRight(PrimitiveOperator other) {
		boolean[] newTruthTable = new boolean[8];

		// the other gate is on the left side - on the LSB
		newTruthTable[0]     = truthTable[((other.truthTable[0]) ? 1 : 0) | 0]; //000
		newTruthTable[1]     = truthTable[((other.truthTable[2]) ? 1 : 0) | 0]; //001 
		newTruthTable[2]     = truthTable[((other.truthTable[1]) ? 1 : 0) | 2]; //010 
		newTruthTable[3]     = truthTable[((other.truthTable[3]) ? 1 : 0) | 2]; //011
		newTruthTable[4]     = truthTable[((other.truthTable[0]) ? 1 : 0) | 4]; //100
		newTruthTable[5]     = truthTable[((other.truthTable[2]) ? 1 : 0) | 4]; //101 
		newTruthTable[6]     = truthTable[((other.truthTable[1]) ? 1 : 0) | 6]; //110 
		newTruthTable[7]     = truthTable[((other.truthTable[3]) ? 1 : 0) | 6]; //111

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * same as combineRightNRightOnLeft but on appropraite pins
	 * @param other the op.
	 * @return the result gate.
	 */
	public PrimitiveOperator combineMidNLeftOnRight(PrimitiveOperator other) {
		boolean[] newTruthTable = new boolean[8];

		// the other gate is on the left side - on the LSB
		newTruthTable[0]     = truthTable[((other.truthTable[0]) ? 1 : 0) | 0]; //000
		newTruthTable[1]     = truthTable[((other.truthTable[1]) ? 1 : 0) | 0]; //001 
		newTruthTable[2]     = truthTable[((other.truthTable[2]) ? 1 : 0) | 2]; //010 
		newTruthTable[3]     = truthTable[((other.truthTable[3]) ? 1 : 0) | 2]; //011
		newTruthTable[4]     = truthTable[((other.truthTable[0]) ? 1 : 0) | 4]; //100
		newTruthTable[5]     = truthTable[((other.truthTable[1]) ? 1 : 0) | 4]; //101 
		newTruthTable[6]     = truthTable[((other.truthTable[2]) ? 1 : 0) | 6]; //110 
		newTruthTable[7]     = truthTable[((other.truthTable[3]) ? 1 : 0) | 6]; //111

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * same as combineRightNRightOnLeft but on appropraite pins
	 * @param other the op.
	 * @return the result gate.
	 */
	public PrimitiveOperator combineLeftNRightOnRight(PrimitiveOperator other) {
		boolean[] newTruthTable = new boolean[8];

		// the other gate is on the left side - on the LSB
		newTruthTable[0]     = truthTable[((other.truthTable[0]) ? 1 : 0) | 0]; //000
		newTruthTable[1]     = truthTable[((other.truthTable[2]) ? 1 : 0) | 0]; //001 
		newTruthTable[2]     = truthTable[((other.truthTable[0]) ? 1 : 0) | 2]; //010 
		newTruthTable[3]     = truthTable[((other.truthTable[2]) ? 1 : 0) | 2]; //011
		newTruthTable[4]     = truthTable[((other.truthTable[1]) ? 1 : 0) | 4]; //100
		newTruthTable[5]     = truthTable[((other.truthTable[3]) ? 1 : 0) | 4]; //101 
		newTruthTable[6]     = truthTable[((other.truthTable[1]) ? 1 : 0) | 6]; //110 
		newTruthTable[7]     = truthTable[((other.truthTable[3]) ? 1 : 0) | 6]; //111

		return new PrimitiveOperator(newTruthTable);
	}

	/**
	 * same as combineRightNRightOnLeft but on appropraite pins
	 * @param other the op.
	 * @return the result gate.
	 */
	public PrimitiveOperator combineLeftNLeftOnRight(PrimitiveOperator other) {
		boolean[] newTruthTable = new boolean[8];

		// the other gate is on the left side - on the LSB
		newTruthTable[0]     = truthTable[((other.truthTable[0]) ? 1 : 0) | 0]; //000
		newTruthTable[1]     = truthTable[((other.truthTable[1]) ? 1 : 0) | 0]; //001 
		newTruthTable[2]     = truthTable[((other.truthTable[0]) ? 1 : 0) | 2]; //010 
		newTruthTable[3]     = truthTable[((other.truthTable[1]) ? 1 : 0) | 2]; //011
		newTruthTable[4]     = truthTable[((other.truthTable[2]) ? 1 : 0) | 4]; //100
		newTruthTable[5]     = truthTable[((other.truthTable[3]) ? 1 : 0) | 4]; //101 
		newTruthTable[6]     = truthTable[((other.truthTable[2]) ? 1 : 0) | 6]; //110 
		newTruthTable[7]     = truthTable[((other.truthTable[3]) ? 1 : 0) | 6]; //111

		return new PrimitiveOperator(newTruthTable);
	}

	//~ Static fields/initializers ---------------------------------------------

	/**
	 * An integer to send with the constructor to construct a not operator.
	 */
	public static final int NOT_OP = 0;

	/**
	 * An integer to send with the constructor to construct an and operator.
	 */
	public static final int AND_OP = 1;

	/**
	 * An integer to send with the constructor to construct an or operator.
	 */
	public static final int OR_OP = 2;

	/**
	 * An integer to send with the constructor to construct an nand operator.
	 */
	public static final int NAND_OP = 3;

	/**
	 * An integer to send with the constructor to construct an nor operator.
	 */
	public static final int NOR_OP = 4;

	/**
	 * An integer to send with the constructor to construct an andn operator.
	 */
	public static final int ANDN_OP = 5;

	/**
	 * An integer to send with the constructor to construct an orn operator.
	 */
	public static final int ORN_OP = 6;

	/**
	 * An integer to send with the constructor to construct a xor operator.
	 */
	public static final int XOR_OP = 7;

	/**
	 * An integer to send with the constructor to construct an eq operator.
	 */
	public static final int EQ_OP = 8;

	/**
	 * An integer to send with the constructor to construct a mux operator.
	 */
	public static final int MUX_OP = 9;

	/**
	 * An integer to send with the constructor to construct a nmux operator.
	 */
	public static final int NMUX_OP = 10;

	/**
	 * An integer to send with the constructor to construct a muxn operator.
	 */
	public static final int MUXN_OP = 11;

	/**
	 * An integer to send with the constructor to construct a nmuxn operator.
	 */
	public static final int NMUXN_OP = 12;

	/**
	 * An integer to send with the constructor to construct a maj operator.
	 */
	public static final int MAJ_OP = 13;

	/**
	 * An integer to send with the constructor to construct a nmaj operator.
	 */
	public static final int NMAJ_OP = 14;

	/**
	 * An integer to send with the constructor to construct a majn operator.
	 */
	public static final int MAJN_OP = 15;

	/**
	 * An integer to send with the constructor to construct a nmajn operator.
	 */
	public static final int NMAJN_OP = 16;

	/**
	 * An integer to send with the constructor to construct a nmajn operator.
	 */
	public static final int ID_OP = 17;

	/*
	 * Holds all the promitives operator's truth table
	 */
	private static final boolean[][] PRIMITIVE_TRUTH_TABLES =
	{
	    { true, false }, //not
	{ false, false, false, true }, //and
	{ false, true, true, true }, //or
	{ true, true, true, false }, //nand
	{ true, false, false, false }, //nor
	{ false, false, true, false }, //andn
	{ true, false, true, true }, //orn
	{ false, true, true, false }, //xor
	{ true, false, false, true }, //eq
	{ false, true, false, true, false, false, true, true }, //mux
	{ true, false, true, false, true, true, false, false }, //nmux
	{ false, true, false, true, true, true, false, false }, //muxn
	{ true, false, true, false, false, false, true, true }, //nmuxn
	{ false, false, false, true, false, true, true, true }, //maj
	{ true, true, true, false, true, false, false, false }, //nmaj
	{ false, false, true, false, true, false, true, true }, //majn
	{ true, true, false, true, false, true, false, false }, //nmajn
	{ false, true // id
	}
	};
}
