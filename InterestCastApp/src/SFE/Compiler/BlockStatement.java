// BlockStatement.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;

import java.io.PrintWriter;

import java.util.Vector;


/**
 * A class for representing a block of statements that can be defined
 * in the program.
 */
public class BlockStatement extends Statement implements OutputWriter, Optimize {
	//~ Instance fields --------------------------------------------------------

	// data members

	/*
	 * Holds the statements defined in the block.
	 */
	private Vector statementsVector;

	//~ Constructors -----------------------------------------------------------

	/**
	 * Constructs a new BlockStatement.
	 */
	public BlockStatement() {
		statementsVector = new Vector();
	}

	//~ Methods ----------------------------------------------------------------

	/**
	 * Appends the specified statement to the end of this BlockStatement.
	 * @param statement a statement to be appended to this BlockStatement.
	 */
	void addStatement(Statement statement) {
		statementsVector.add(statement);
	}

	/**
	 * adds a given vector of statements into the block.
	 * @param v the given vector of statements.
	 */
	void addStatements(Vector v) {
		statementsVector.addAll(v);
	}

	/**
	 * Returns a string representation of the BlockStatement.
	 * @return a string representation of the BlockStatement.
	 */
	public String toString() {
		String str = new String();
		str += "{";

		for (int i = 0; i < statementsVector.size(); i++)
			str += ((Statement) (statementsVector.elementAt(i))).toString();

		str += "}";

		return str;
	}

	/**
	 * Transforms this multibit statements in this BlockStatement
	 * into singlebit statements and returns the result.
	 * @param obj not needed (null).
	 * @return a BlockStatement containing the result of this transformation.
	 */
	public BlockStatement multi2SingleBit(Object obj) {
		BlockStatement result = new BlockStatement();

		for (int i = 0; i < statementsVector.size(); i++) {
			Multi2SingleBit statement =
				((Multi2SingleBit) (statementsVector.elementAt(i)));
			result.addStatement(statement.multi2SingleBit(null));
		}

		return result;
	}

	/**
	 * Writes this BlockStatement to the output circuit.
	 * @param circuit the output circuit file.
	 */
	public void toCircuit(PrintWriter circuit) {
		for (int i = 0; i < statementsVector.size(); i++)
			((OutputWriter) (statementsVector.elementAt(i))).toCircuit(circuit);
	}

	/**
	 * Unique vars transformations.
	 */
	public Statement uniqueVars() {
		for (int i = 0; i < statementsVector.size(); i++)
			statementsVector.setElementAt(((Statement) statementsVector.elementAt(i)).uniqueVars(),
			                              i);

		return this;
	}

	/**
	 * Optimizes this BlockStatment.
	 * runs the first optimization phaze on each of the statements in this
	 * BlockStatement.
	 */
	public void optimizePhaseI() {
		for (int i = 0; i < statementsVector.size(); i++)
			((Optimize) (statementsVector.elementAt(i))).optimizePhaseI();
	}

	/**
	 * executes optimizePhaseII() on each of the statements in
	 * this BlockStatement.
	 */
	public void optimizePhaseII(Vector newBody) {
		for (int i = 0; i < statementsVector.size(); i++) {
			Optimize s = ((Optimize) (statementsVector.elementAt(i)));
			s.optimizePhaseII(newBody);
		}
	}

	/**
	 * Executes buildUsedStatementsHash() for each statement in
	 * the BlockStatement.
	 */
	public void buildUsedStatementsHash() {
		for (int i = statementsVector.size() - 1; i >= 0; i--)

			// Block/Assignment/Input Statement
			((Optimize) (statementsVector.elementAt(i))).buildUsedStatementsHash();
	}

	/**
	 * returns a duplica of this BlockStatement.
	 */
	public Statement duplicate() {
		BlockStatement result = new BlockStatement();

		for (int i = 0; i < statementsVector.size(); i++)
			result.addStatement(((Statement) statementsVector.elementAt(i)).duplicate());

		return result;
	}
}
