// OperationExpression.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;

import java.util.Vector;

/**
 * class OperationExpression defines expressions containing operatioins, that can
 * be defined in the program.
 */
public abstract class OperationExpression extends Expression
	implements Multi2SingleBit, OutputWriter
{
	//~ Instance fields --------------------------------------------------------

	/*
	 * Holds the operator of this expression
	 */
	protected Operator op;

	/*
	     * Holds the size of this OperationExpression
	     */
	protected int size;

	//~ Constructors -----------------------------------------------------------

	/**
	     * Constracts a new OperationExpression from a given Operator.
	     * @param op this OperationExpression operator.
	     */
	public OperationExpression(Operator op) {
		this.op     = op;
		size        = 0;
	}

	//~ Methods ----------------------------------------------------------------

	/**
	     * Returns the number of bits needed to represent this expression.
	     * @return the number of bits needed to represent this expression.
	     */
	public int size() {
		return size;
	}

	/**
	 * Returns true if this expression has input pins that share an input.
	 * @return true if this expression has input pins that share an input.
	 */
	public boolean hasSharedInput() {
		return false;
	}

	/**
	      * should be called only in hasSharedInput() is true.
	      */
	public OperationExpression combineSharedInput() {
		return this;
	}

	/**
	     * Returns true iff one of the operand of this operator expression
	     * is a result of an unary operator.
	     * @return true if one of the operand of this operator expression
	     * is a result of an unary operator.
	     */
	public abstract boolean hasUnaryInput();

	/**
	      * Combines an input expression the is the output of an unary operator into
	      * this Operation expression.
	      * this method should be call only if hasUnaryInput() is true.
	      */
	public abstract void combineUnaryInput();

	/**
	     * Return true iff this OperationExpression has a constant input.
	      * @return true iff this OperationExpression has a constant input.
	     */
	public abstract boolean hasConstantInput();

	/**
	      * Combines an input expression that is constant and return the
	      * result expression. This method reduces the
	      * arity of this OperationExpression.
	      * This method should be called only if hasConstantInput is true.
	      * @return the result expression.
	      */
	public abstract OperationExpression combineConstInput();

	/**
	     * Returns true iff this operator expression has at lease two
	     * identical input expression.
	     * @return true iff this operator expression has at lease two
	     * identical input expression.
	     */
	public abstract boolean hasEqualInputs();

	/**
	     * Combines identical input expression an returns the result expression.
	     * The resulting expression has an arity smaller by one then this expression arity.
	     * Note that this method should be called only if hasEqualInputs is true.
	      * @return the result expression.
	     */
	public abstract OperationExpression combineEqualInputs();

	/**
	      * Returns this operator.
	        * @return this operator.
	      */
	public Operator getOperator() {
		return op;
	}

	/**
	       * Sorts the input gates according to their names and returns
	       * the result OperationExpression. This method is used in the optimization
	       * process. (Right is the smallest, left the biggest)
	       * @return the OperationExpression with the sorted inputs.
	       */
	public abstract OperationExpression sortInputs();

	/**
	     * returns the negate gate (OperatorExpression) of this OperatorExpression.
	      * @return the negate gate (OperatorExpression) of this OperatorExpression.
	     */
	public abstract OperationExpression negate();

	/**
	     * Returns an array of the input LvalExpressions of this gate.
	     * This method is used in the second phase of the optimization.
	      * @return an array of the input LvalExpressions of this gate.
	     */
	public abstract Vector getLvalExpressionInputs();

	/**
	     * Returns true if the output of this operation is constant
	     */
	public boolean isConstant() {
		return ((PrimitiveOperator) op).isOne() ||
		       ((PrimitiveOperator) op).isZero();
	}

	/**
	     * Returns an unary op expression with a constant (constant expression).
	     * this method should be called
	     * only if this.isConstant() is true.
	     * @return an unary op expression with a constant (constant expression).
	     */
	public UnaryOpExpression getConstantOutput() {
		if (((PrimitiveOperator) op).isOne()) {
			return new UnaryOpExpression(new PrimitiveOperator(PrimitiveOperator.ID_OP),
			                             new BooleanConstant(true));
		}

		return new UnaryOpExpression(new PrimitiveOperator(PrimitiveOperator.ID_OP),
		                             new BooleanConstant(false));
	}

	/**
	     *
	     */
	public abstract void changeReference(UniqueVariables unique);

	/**
	 * recursivly calculates inner arithmetic expression and inserts them into the
	 * proper function.
	 * @param as the AssignmentStatement that holds this expression (as rhs).
	 * @param result a block statement to insert statments if needed.
	 * @return the new statement to use instead of as.
	 */
	public Expression evaluateExpression(AssignmentStatement as,
	                                     BlockStatement result) {
		LvalExpression lhs = as.getLHS(); //LHS of the param statement

		LvalExpression tmpLvalExp =
			Function.addTempLocalVar(lhs.getName() + "$tmp" + tempLabel,
			                         new BusType(size()));

		// create the assignment statement
		AssignmentStatement tempAs =
			new AssignmentStatement(tmpLvalExp, (OperationExpression) this);

		// evaluate the expression and store it in the tmp lval expression
		result.addStatement(tempAs.multi2SingleBit(null));

		tempLabel++;

		return tmpLvalExp;
	}

	/**
	 * returns true if this gate outputs true of false(constant output).
	 * for any input.
	 * @return true if this gate outputs true of false(constant output).
	 * for any input.
	 */
	public abstract boolean isComplexIDOrNeg();

	/**
	 * Transformas this gate to a simple id or not gate.
	 * should be called only if isComplexIDOrNeg() if true
	 * @return the simplified gate.
	 */
	public abstract OperationExpression simplify();

	/**
	 * returns a replic of this Expression
	 * @return a replic of this Expression
	 */
	public abstract Expression duplicate();
}
