// LvalExpression.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;

import java.io.PrintWriter;


/**
 * The LvalExpression class represents an Expression that can
 * appear as LHS in the program.
 */
public class LvalExpression extends Expression implements OutputWriter {
	//~ Instance fields --------------------------------------------------------

	// data members

	/*
	 * Holds the lvalue of this Expression
	 */
	private Lvalue    lvalue;
	private Statement assigningStatement = null;

	//~ Constructors -----------------------------------------------------------

	/**
	 * LvalExpression constractor
	 * @param lvalue
	 */
	public LvalExpression(Lvalue lvalue) {
		this.lvalue = lvalue;
	}

	//~ Methods ----------------------------------------------------------------

	/**
	     * Returns the number of bits needed to represent this expression.
	     * @return the number of bits needed to represent this expression.
	     */
	public int size() {
		return lvalue.size();
	}

	/**
	     * Returns a string representation of the object.
	     * @return a string representation of the object.
	     */
	public String toString() {
		if (assigningStatement instanceof InputStatement) {
			return Integer.toString(((InputStatement) assigningStatement).getOutputLine());
		}

		return Integer.toString(((AssignmentStatement) assigningStatement).getOutputLine());
	}

	/**
	     * Returns this Lavlue.
	     * @return this Lavlue.
	     */
	public Lvalue getLvalue() {
		return lvalue;
	}

	/**
	     * Returns Expression that represents the bit at place i of this Expression
	      * @return Expression that represents the bit at place i of this Expression
	     */
	public Expression bitAt(int i) {
		return Function.getVarBitAt(this, i);
	}

	/**
	     * Returns LvalExpression that represents the bit at place i of this Expression
	      * @return LvalExpression that represents the bit at place i of this Expression
	     */
	public LvalExpression lvalBitAt(int i) {
		return (LvalExpression) bitAt(i);
	}

	/**
	     * Returns the name of this LvalExpression's lvalue.
	     * @return a string representing this LvalExpression's lvalue.
	     */
	public String getName() {
		return lvalue.getName();
	}

	/**
	     * Returns the Type of this LvalExpression's lvalue.
	     * @return the Type of this LvalExpression's lvalue.
	     */
	public Type getType() {
		return lvalue.getType();
	}

	/**
	      * Returns true if the this expression is a part out the circuit's output.
	        * @return true if the this expression is a part out the circuit's output.
	      */
	public boolean isOutput() {
		return lvalue.isOutput();
	}

	/**
	     * Set the reference to this expressionn assigning statement, Which can be either AssignmentStatement
	     * or InputStatement.
	     * @param as the assigning statement.
	     */
	public void setAssigningStatement(Statement as) {
		if (this.assigningStatement == null) {
			this.assigningStatement = as;
		}
	}

	/**
	     * Returns the assigning statement of this lvalexpression.
	      * @return the assigning statement of this lvalexpression.
	     */
	public Statement getAssigningStatement() {
		return assigningStatement;
	}

	/**
	 * Prints this AssignmentStatement into the circuit.
	* @param circuit the circuit output file.
	*/
	public void toCircuit(PrintWriter circuit) {
		if (assigningStatement instanceof InputStatement) {
			circuit.print(((InputStatement) assigningStatement).getOutputLine());
		} else {
			circuit.print(((AssignmentStatement) assigningStatement).getOutputLine());
		}
	}

	/**
	     * sets this LvalExpression as a pin that is not an output of this
	     * circuit.
	     */
	public void notOutput() {
		lvalue.notOutput();
	}

	/**
	     * Returns true if this LvalExpression is a result of an unary operator.
	     * @return true if this LvalExpression is a result of an unary operator.
	     */
	public boolean hasUnaryInput() {
		return assigningStatement.hasUnaryOperator();
	}

	/**
	     * Returns true if this expression is a result of unary not expression.
	     * Note: this method should be called only if hasUnaryInput() is true.
	     */
	public boolean unaryInputIsNotResult() {
		UnaryOpExpression unaryInput =
			(UnaryOpExpression) (((AssignmentStatement) assigningStatement).getRHS());

		PrimitiveOperator unaryInputOp =
			(PrimitiveOperator) (unaryInput.getOperator());

		return unaryInputOp.isNot();
	}

	/**
	     * Returns the input of the unary gate that this expression is its result.
	     * Note: this method should be called only if hasUnaryInput() is true.
	     */
	public Expression getMiddleOfUnaryInput() {
		UnaryOpExpression unaryInput =
			(UnaryOpExpression) (((AssignmentStatement) assigningStatement).getRHS());

		return unaryInput.getMiddle();
	}

	/**
	 * Returns true if this expression has input pins that share an input.
	 * @param exp
	 * @return true if this expression has input pins that share an input.
	 */
	public boolean hasSharedInput(Expression exp) {
		if (assigningStatement instanceof InputStatement) {
			return false;
		}

		OperationExpression gate =
			(OperationExpression) (((AssignmentStatement) assigningStatement).getRHS());

		return (gate.getLvalExpressionInputs().contains(exp)) &&
		       (gate instanceof BinaryOpExpression);
	}
}
