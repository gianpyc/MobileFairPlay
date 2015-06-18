// BinaryOpExpression.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;

import java.io.PrintWriter;

import java.util.Vector;


/**
 * A class for representing binary operation expressions that can be defined
 * in the program.
 */
public class BinaryOpExpression extends OperationExpression {
	//~ Instance fields --------------------------------------------------------

	// data members

	/*
	 * The left input pin of this BinaryOpExpression
	 */
	private Expression left;

	/*
	 * The right input pin of this BinaryOpExpression
	 */
	private Expression right;

	//~ Constructors -----------------------------------------------------------

	/**
	 * Constructs a new BinaryOpExpression from a given oparator and inputs.
	 * @param op the binary operator.
	 * @param left the left input pin.
	 * @param right the right input pin.
	 */
	public BinaryOpExpression(Operator op, Expression left, Expression right) {
		super(op);
		this.left      = left;
		this.right     = right;

		// assigning the size of this BinaryOpExpression
		size = (left.size() > right.size()) ? left.size() : right.size();

		if (op instanceof PlusOperator || op instanceof MinusOperator) {
			size++;
		}
	}

	//~ Methods ----------------------------------------------------------------

	/**
	 * Returns a string representation of the object.
	 * @return a string representation of the object.
	 */
	public String toString() {
		return ((OutputWriter) op).toString() + " inputs [ " +
		       ((OutputWriter) right).toString() + " " +
		       ((OutputWriter) left).toString() + " ]";
	}

	/**
	 * Transforms this multibit BinaryOpExpression into singlebit Statements
	 * and returns a BlockStatement containing the result.
	 * @param obj the AssignmentStatement that holds this BinaryOpExpression.
	 * @return BlockStatement containing singlebit Statements of this
	 *                 BinaryOpExpression.
	 */
	public BlockStatement multi2SingleBit(Object obj) {
		AssignmentStatement as     = ((AssignmentStatement) obj);
		LvalExpression      lhs    = as.getLHS(); //LHS of the param statement
		BlockStatement      result = new BlockStatement();

		// evaluate the input pins 
		left      = left.evaluateExpression(as, result);
		right     = right.evaluateExpression(as, result);

		// Two options:
		//   a) op is oprimitive operator: 
		if (op instanceof PrimitiveOperator) {
			for (int i = 0; i < lhs.size(); i++)
				result.addStatement(new AssignmentStatement(lhs.lvalBitAt(i),
				                                            new BinaryOpExpression(op,
				                                                                   left.bitAt(i),
				                                                                   right.bitAt(i))));
		}
		//    b) op is an arithmetic operator (i.e. +,-,>,<,...)
		else {
			result.addStatement(((Multi2SingleBit) op).multi2SingleBit(as));
		}

		return result;
	}

	/**
	 * Returns the right input pin.
	 * @return the right input pin.
	 */
	public Expression getRight() {
		return right;
	}

	/**
	 * Returns the left input pin.
	 * @return the left input pin.
	 */
	public Expression getLeft() {
		return left;
	}

	/**
	 * Writes this Expression to the output circuit.
	 * @param circuit the output circuit file.
	 */
	public void toCircuit(PrintWriter circuit) {
		((OutputWriter) op).toCircuit(circuit);
		circuit.print(" inputs [ ");
		((OutputWriter) right).toCircuit(circuit);
		circuit.print(" ");
		((OutputWriter) left).toCircuit(circuit);
		circuit.print(" ]");
	}

	/**
	 * Returns true iff one of the operands of this operator expression
	 * is a result of an unary operator.
	 * @return true if one of the operands of this operator expression
	 * is a result of an unary operator.
	 */
	public boolean hasUnaryInput() {
		if (left instanceof LvalExpression &&
			    ((LvalExpression) left).hasUnaryInput()) {
			return true;
		}

		if (right instanceof LvalExpression &&
			    ((LvalExpression) right).hasUnaryInput()) {
			return true;
		}

		return false;
	}

	/**
	 * Combines an input expression that is the output of an unary operator into
	 * this Operation expression.
	 * NOTE: this method should be call only if hasUnaryInput() is true.
	 */
	public void combineUnaryInput() {
		if (left instanceof LvalExpression &&
			    ((LvalExpression) left).hasUnaryInput()) {
			if (((LvalExpression) left).unaryInputIsNotResult()) {
				// input is result of Not gate - combine in to this gate
				op = ((PrimitiveOperator) op).negLeft();
			}

			// input is result of ID gate - no changes to this gate is needed
			left = ((LvalExpression) left).getMiddleOfUnaryInput();
		}

		if (right instanceof LvalExpression &&
			    ((LvalExpression) right).hasUnaryInput()) {
			if (((LvalExpression) right).unaryInputIsNotResult()) {
				// input is result of Not gate - combine in to this gate
				op = ((PrimitiveOperator) op).negRight();
			}

			// input is result of ID gate - no changes to this gate is needed
			right = ((LvalExpression) right).getMiddleOfUnaryInput();
		}
	}

	/**
	 * Return true iff this OperationExpression has a constant input.
	 * @return true iff this OperationExpression has a constant input.
	 */
	public boolean hasConstantInput() {
		if (left instanceof BooleanConstant) {
			return true;
		}

		return right instanceof BooleanConstant;
	}

	/**
	 * Combines an input expression that is constant and return the
	 * result expression. This method reduces the
	 * arity of this OperationExpression.
	 * This method should be called only if hasConstantInput is true.
	 * @return the result expression.
	 */
	public OperationExpression combineConstInput() {
		Operator newOp;

		if (left instanceof BooleanConstant) {
			if (((BooleanConstant) left).getConst()) { // true const
				newOp = ((PrimitiveOperator) op).oneLeft();
			} else {
				newOp = ((PrimitiveOperator) op).zeroLeft();
			}

			return new UnaryOpExpression(newOp, right);
		}

		// right IS instanceof BooleanConstant
		if (((BooleanConstant) right).getConst()) { // true const
			newOp = ((PrimitiveOperator) op).oneRight();
		} else {
			newOp = ((PrimitiveOperator) op).zeroRight();
		}

		return new UnaryOpExpression(newOp, left);
	}

	/**
	 * Returns true iff this operator expression has at lease two
	 * identical input expression.
	 * @return true iff this operator expression has at lease two
	 * identical input expression.
	 */
	public boolean hasEqualInputs() {
		return left == right;
	}

	/**
	 * Combines identical input expression an returns the result expression.
	 * The resulting expression has an arity smaller by one then this
	 * expression arity.
	 * Note that this method should be called only if hasEqualInputs is true.
	 * @return the result expression.
	 */
	public OperationExpression combineEqualInputs() {
		Operator newOp = ((PrimitiveOperator) op).equalLeftRight();

		return new UnaryOpExpression(newOp, left);
	}

	/**
	 * Sorts the input gates according to their names and returns
	 * the result OperationExpression. This method is used in the optimization
	 * process.
	 * @return the OperationExpression with the sorted inputs.
	 */
	public OperationExpression sortInputs() {
		String leftStr  = left.toString();
		String rightStr = right.toString();

		if (leftStr.compareTo(rightStr) < 0) { // if left < right 

			Operator newOp = ((PrimitiveOperator) op).switchRightLeft();

			// switch expressions and return result
			return new BinaryOpExpression(newOp, right, left);
		}

		return this;
	}

	/**
	 * returns the negate gate (OperatorExpression) of this OperatorExpression.
	 * @return the negate gate (OperatorExpression) of this OperatorExpression.
	 */
	public OperationExpression negate() {
		return new BinaryOpExpression(((PrimitiveOperator) op).negOut(), left,
		                              right);
	}

	/**
	 * Returns an array of the input LvalExpressions of this gate.
	 * This method is used in the second phase of the optimization.
	 * @return an array of the input LvalExpressions of this gate.
	 */
	public Vector getLvalExpressionInputs() {
		Vector result = new Vector();

		if (left instanceof LvalExpression) {
			result.add(left);
		}

		if (right instanceof LvalExpression) {
			result.add(right);
		}

		return result;
	}

	/**
	 * Changes references of variables to the last place they were changed
	 * @param unique holds all the variables and their references
	 */
	public void changeReference(UniqueVariables unique) {
		if (left instanceof LvalExpression) {
			left = unique.getVar(((LvalExpression) left).getName());
		}

		if (right instanceof LvalExpression) {
			right = unique.getVar(((LvalExpression) right).getName());
		}
	}

	/**
	 * returns true if this gate outputs true of false(constant output).
	 * for any input.
	 * @return true if this gate outputs true of false(constant output).
	 * for any input.
	 */
	public boolean isComplexIDOrNeg() {
		return ((PrimitiveOperator) op).isComplexIDOfLeft() ||
		       ((PrimitiveOperator) op).isComplexIDOfRight() ||
		       ((PrimitiveOperator) op).isComplexNotOfLeft() ||
		       ((PrimitiveOperator) op).isComplexNotOfRight();
	}

	/**
	 * Transformas this gate to a simple id or not gate.
	 * should be called only if isComplexIDOrNeg() if true
	 * @return the simplified gate.
	 */
	public OperationExpression simplify() {
		if (((PrimitiveOperator) op).isComplexIDOfLeft()) {
			return new UnaryOpExpression(new PrimitiveOperator(PrimitiveOperator.ID_OP),
			                             left);
		}

		if (((PrimitiveOperator) op).isComplexNotOfLeft()) {
			return new UnaryOpExpression(new PrimitiveOperator(PrimitiveOperator.NOT_OP),
			                             left);
		}

		if (((PrimitiveOperator) op).isComplexIDOfRight()) {
			return new UnaryOpExpression(new PrimitiveOperator(PrimitiveOperator.ID_OP),
			                             right);
		}

		if (((PrimitiveOperator) op).isComplexNotOfRight()) {
			return new UnaryOpExpression(new PrimitiveOperator(PrimitiveOperator.NOT_OP),
			                             right);
		}

		return this; //dammy
	}

	/**
	 * returns a replica of this BinaryOpExpression.
	 * @return a replica of this BinaryOpExpression.
	 */
	public Expression duplicate() {
		return new BinaryOpExpression(op, left.duplicate(), right.duplicate());
	}

	/**
	 * Returns true if the gates input pins share an input pin.
	 * @return true if the gates input pins share an input pin.
	 */
	public boolean hasSharedInput() {
		return left.hasSharedInput(right) || right.hasSharedInput(left);
	}

	/**
	 * Combines the gates that share an input pin into one gate.
	 * should be called only in hasSharedInput() is true.
	 * @return the combined gate (OperationExpression).
	 */
	public OperationExpression combineSharedInput() {
		BinaryOpExpression lowerGate;
		PrimitiveOperator  gateOp;
		PrimitiveOperator  newOp;

		if (left.hasSharedInput(right)) {
			lowerGate =
				(BinaryOpExpression) ((AssignmentStatement) ((LvalExpression) left).getAssigningStatement()).getRHS();

			gateOp = (PrimitiveOperator) lowerGate.getOperator();

			if (right == lowerGate.getRight()) {
				return new BinaryOpExpression(((PrimitiveOperator) op).combineRightNRight(gateOp),
				                              lowerGate.getLeft(), right);
			}

			// else right == lowerGate.getLeft())
			return new BinaryOpExpression(((PrimitiveOperator) op).combineRightNLeft(gateOp),
			                              lowerGate.getRight(), right);
		}

		// right.hasSharedInput(left)
		lowerGate =
			(BinaryOpExpression) ((AssignmentStatement) ((LvalExpression) right).getAssigningStatement()).getRHS();

		gateOp = (PrimitiveOperator) lowerGate.getOperator();

		if (left == lowerGate.getRight()) {
			return new BinaryOpExpression(((PrimitiveOperator) op).combineLeftNRight(gateOp),
			                              left, lowerGate.getLeft());
		}

		// else left == lowerGate.getLeft())
		return new BinaryOpExpression(((PrimitiveOperator) op).combineLeftNLeft(gateOp),
		                              left, lowerGate.getRight());
	}
}
