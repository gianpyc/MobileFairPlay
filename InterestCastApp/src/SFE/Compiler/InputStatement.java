// InputStatement.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;

import java.io.PrintWriter;

import java.util.Vector;


/**
 * A class for representing input statement for the final output circuit.
 */
public class InputStatement extends Statement implements OutputWriter, Optimize {
	//~ Instance fields --------------------------------------------------------

	// data members

	/*
	 * Holds the LHS of the assignment.
	 */
	private LvalExpression input;

	/*
	 * The number of this assignment statement line in the ouput circuit.
	 */
	private int outputLine;

	//~ Constructors -----------------------------------------------------------

	/**
	 * Constructs a new InputStatment from a given statement name and
	 * input.
	 * @param input the input LvalExpression stored in this InputStatment.
	 */
	InputStatement(LvalExpression input) {
		this.input = input;
		this.input.setAssigningStatement(this);
	}

	//~ Methods ----------------------------------------------------------------

	/**
	 * Returns a string representation of the object.
	 * @return a string representation of the object.
	 */
	public String toString() {
		return "input " + input + "\n";
	}

	/**
	 * Returns a string representation of this InputStatement's name.
	 * @return a string representation of this InputStatement's name.
	 */
	public String getName() {
		return input.getName();
	}

	/**
	 * Returns the output line in the output circuit of this assignmnet statement.
	 * @return an int that represents the output line in the output circuit of this assignmnet statement.
	 */
	public int getOutputLine() {
		return outputLine;
	}

	/**
	 * Sets the output line of this assignment statement.
	 * @param line the line number in the output.
	 */
	public void setOutputLine(int line) {
		outputLine = line;
	}

	/**
	 *  Prints this AssignmentStatement into the circuit.
	 *  @param circuit the circuit output file.
	 */
	public void toCircuit(PrintWriter circuit) {
		outputLine = Program.getLineNumber();
		circuit.println(outputLine + " input\t\t//" + input.getName());
	}

	/**
	 * Optimizes the InputStatement - phase I
	 */
	public void optimizePhaseI() {
		outputLine = Program.getLineNumber();
	}

	/**
	 * Optimizes the InputStatement - phase II
	 */
	public void optimizePhaseII(Vector newBody) {
		newBody.add(this);
	}

	/**
	 * adds this input statement to the statements being used to calculate
	 * the output circuit.
	 */
	public void buildUsedStatementsHash() {
		Optimizer.putUsedStatement(this);
	}

	/**
	 * Transforms this multibit InputStatement into singlebit statements
	 * and returns the result.
	 * @param obj not needed (null).
	 * @return a BlockStatement containing the result statements.
	 */
	public BlockStatement multi2SingleBit(Object obj) {
		BlockStatement result = new BlockStatement();

		String         inputName = input.getName();

		// the input vector goes into the OutputWriter for writing the
		// format file at the end.
		Vector inputVector = new Vector();

		// first player's name of this input
		if (inputName.startsWith("output$input.alice")) {
			inputVector.add("Alice");
		} else {
			inputVector.add("Bob");
		}

		// remove the "output$" prefix and add to the vector
		inputVector.add(inputName.substring(7));

		for (int i = 0; i < input.size(); i++) {
			InputStatement is =
				new InputStatement(input.lvalBitAt(i) //Function.getVar(inputName+"$"+i)
				);
			result.addStatement(is);
			inputVector.add(is);
		}

		// add the format vector of this input statement to the outputwriter
		OutputWriter.inputFormat.add(inputVector);

		return result;
	}

	/**
	 * Unique vars transformations.
	 */
	public Statement uniqueVars() {
		return this;
	}

	/**
	 * dammy - returns this.
	 */
	public Statement duplicate() {
		return this;
	}
}
