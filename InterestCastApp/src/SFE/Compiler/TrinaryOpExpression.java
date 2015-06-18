// TrinaryOpExpression.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;

import java.io.PrintWriter;

import java.util.Vector;


/**
 * A class for representing trinary operator expressions that can be defined
 * in the program.
 */
public class TrinaryOpExpression extends OperationExpression {
	//~ Instance fields --------------------------------------------------------

	// data members

	/*
	 * Hold the left input of this expression
	 */
	private Expression left;

	/*
	 * Hold the middle input of this expression
	 */
	private Expression middle;

	/*
	 * Hold the right input of this expression
	 */
	private Expression right;

	//~ Constructors -----------------------------------------------------------

	/**
	 * Constructs a new TrinaryOpExpression from a given oparator and inputs.
	 * @param op the trinary operator.
	 * @param left the left input.
	 * @param middle the left input.
	 * @param right the right input.
	 */
	public TrinaryOpExpression(Operator op, Expression left, Expression middle,
	                           Expression right) {
		super(op);
		this.left       = left;
		this.middle     = middle;
		this.right      = right;

		// assigning the size of this UnaryOpExpression
		size     = (left.size() > right.size()) ? left.size() : right.size();
		size     = (size > middle.size()) ? size : middle.size();
	}

	//~ Methods ----------------------------------------------------------------

	/**
	 * Returns a string representation of the object.
	 * Returns a string representing this object as it appear at the
	 * output circuit.
	 * @return a string representing this object as it appear at the
	 * output circuit.
	 */
	public String toString() {
		return ((OutputWriter) op).toString() + " inputs [ " +
		       ((OutputWriter) right).toString() + " " +
		       ((OutputWriter) middle).toString() + " " +
		       ((OutputWriter) left).toString() + " ]";
	}

	/**
	 * Transforms this multibit expression into singlebit statements
	 * and returns the result
	 * @param obj the AssignmentStatement that holds this TrinaryOpExpression.
	 * @return a BlockStatement containing the result statements
	 */
	public BlockStatement multi2SingleBit(Object obj) {
		AssignmentStatement as     = ((AssignmentStatement) obj);
		LvalExpression      lhs    = as.getLHS(); //LHS of the param statement
		BlockStatement      result = new BlockStatement();

		left       = left.evaluateExpression(as, result);
		middle     = middle.evaluateExpression(as, result);
		right      = right.evaluateExpression(as, result);

		for (int i = 0; i < lhs.size(); i++)
			result.addStatement(new AssignmentStatement(lhs.lvalBitAt(i), //currentFunction.fromName(lhs.getName()+"$"+i),
			                                            new TrinaryOpExpression(op,
			                                                                    left.bitAt(i),
			                                                                    middle.bitAt(i),
			                                                                    right.bitAt(i))));

		return result;
	}

	/**
	 * Prints this AssignmentStatement into the circuit.
	 * @param circuit the circuit output file.
	 */
	public void toCircuit(PrintWriter circuit) {
		((OutputWriter) op).toCircuit(circuit);
		circuit.print(" inputs [ ");
		((OutputWriter) right).toCircuit(circuit);
		circuit.print(" ");
		((OutputWriter) middle).toCircuit(circuit);
		circuit.print(" ");
		((OutputWriter) left).toCircuit(circuit);
		circuit.print(" ]");
	}

	/**
	 * Returns true iff one of the operand of this operator expression
	 * is a result of an unary operator.
	 * @return true if one of the operand of this operator expression
	 * is a result of an unary operator.
	 */
	public boolean hasUnaryInput() {
		Statement  s;
		Expression sRHS; // RHS of s

		if (left instanceof LvalExpression &&
			    ((LvalExpression) left).hasUnaryInput()) {
			return true;
		}

		if (middle instanceof LvalExpression &&
			    ((LvalExpression) middle).hasUnaryInput()) {
			return true;
		}

		if (right instanceof LvalExpression &&
			    ((LvalExpression) right).hasUnaryInput()) {
			return true;
		}

		return false;
	}

	/**
	 * Combined an input expression the is the output of an unary operator into
	 * this Operation expression.
	 * this method should be call only if hasUnaryInput() is true.
	 */
	public void combineUnaryInput() {
		/***** Take care of left *****/
		if (left instanceof LvalExpression &&
			    ((LvalExpression) left).hasUnaryInput()) {
			if (((LvalExpression) left).unaryInputIsNotResult()) {
				// input is result of Not gate - combine in to this gate
				op = ((PrimitiveOperator) op).negLeft();
			}

			// input is result of ID gate - no changes to this gate is needed
			left = ((LvalExpression) left).getMiddleOfUnaryInput();
		}

		/***** Take care of middle *****/
		if (middle instanceof LvalExpression &&
			    ((LvalExpression) middle).hasUnaryInput()) {
			if (((LvalExpression) middle).unaryInputIsNotResult()) {
				// input is result of Not gate - combine in to this gate
				op = ((PrimitiveOperator) op).negMid();
			}

			// input is result of ID gate - no changes to this gate is needed
			middle = ((LvalExpression) middle).getMiddleOfUnaryInput();
		}

		/***** Take care of right *****/
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

		if (middle instanceof BooleanConstant) {
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

			return new BinaryOpExpression(newOp, middle, right);
		}

		if (middle instanceof BooleanConstant) {
			if (((BooleanConstant) middle).getConst()) { // true const
				newOp = ((PrimitiveOperator) op).oneMid();
			} else {
				newOp = ((PrimitiveOperator) op).zeroMid();
			}

			return new BinaryOpExpression(newOp, left, right);
		}

		// right IS instanceof BooleanConstant
		if (((BooleanConstant) right).getConst()) { // true const
			newOp = ((PrimitiveOperator) op).oneRight();
		} else {
			newOp = ((PrimitiveOperator) op).zeroRight();
		}

		return new BinaryOpExpression(newOp, left, middle);
	}

	/**
	 * Returns true iff this operator expression has at lease two
	 * identical input expression.
	 * @return true iff this operator expression has at lease two
	 * identical input expression.
	 */
	public boolean hasEqualInputs() {
		return (left == right) || (middle == right) || (left == middle);
	}

	/**
	 * Combines identical input expression an returns the result expression.
	 * The resulting expression has an arity smaller by one then this expression arity.
	 * Note that this method should be called only if hasEqualInputs is true.
	 * @return the result expression.
	 */
	public OperationExpression combineEqualInputs() {
		Operator newOp;

		if (left == right) {
			newOp = ((PrimitiveOperator) op).equalLeftRight();

			return new BinaryOpExpression(newOp, left, middle);
		}

		if (middle == right) {
			newOp = ((PrimitiveOperator) op).equalRightMid();

			return new BinaryOpExpression(newOp, left, middle);
		}

		// left == middle
		newOp = ((PrimitiveOperator) op).equalLeftMid();

		return new BinaryOpExpression(newOp, left, right);
	}

	/**
	 * Sorts the input gates according to their names and returns
	 * the result OperationExpression. This method is used in the optimization
	 * process.
	 * @return the OperationExpression with the sorted inputs.
	 */
	public OperationExpression sortInputs() {
		PrimitiveOperator newOp = (PrimitiveOperator) op;
		Expression        newR = right;
		Expression        newL = left;
		Expression        newM = middle;
		Expression        tmp;

		/****
		 * bubble sort
		 ****/
		if ((newR.toString()).compareTo(newM.toString()) > 0) {
			newOp     = newOp.switchRightMid();
			tmp       = newR;
			newR      = newM;
			newM      = tmp;
		}

		if ((newM.toString()).compareTo(newL.toString()) > 0) {
			newOp     = newOp.switchLeftMid();
			tmp       = newL;
			newL      = newM;
			newM      = tmp;
		}

		// seconsd iteration...
		if ((newR.toString()).compareTo(newM.toString()) > 0) {
			newOp     = newOp.switchRightMid();
			tmp       = newR;
			newR      = newM;
			newM      = tmp;
		}

		return new TrinaryOpExpression(newOp, newL, newM, newR);
	}

	/**
	 * returns the negate gate (OperatorExpression) of this OperatorExpression.
	 * @return the negate gate (OperatorExpression) of this OperatorExpression.
	 */
	public OperationExpression negate() {
		return new TrinaryOpExpression(((PrimitiveOperator) op).negOut(), left,
		                               middle, right);
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

		if (middle instanceof LvalExpression) {
			result.add(middle);
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

		if (middle instanceof LvalExpression) {
			middle = unique.getVar(((LvalExpression) middle).getName());
		}

		if (right instanceof LvalExpression) {
			right = unique.getVar(((LvalExpression) right).getName());
		}
	}

	/**
	 * returns true if this gate is a id gate or neg gate of one
	 * of the inputs
	 */
	public boolean isComplexIDOrNeg() {
		return ((PrimitiveOperator) op).isComplexIDOfLeft() ||
		       ((PrimitiveOperator) op).isComplexIDOfRight() ||
		       ((PrimitiveOperator) op).isComplexIDOfMiddle() ||
		       ((PrimitiveOperator) op).isComplexNotOfLeft() ||
		       ((PrimitiveOperator) op).isComplexNotOfRight() ||
		       ((PrimitiveOperator) op).isComplexNotOfMiddle();
	}

	/**
	 * if isComplexIDOrNeg() is true, replaces the compex gate with
	 * the appropriate simple primitive gate.
	 * should be called only if isComplexIDOrNeg() if true
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

		if (((PrimitiveOperator) op).isComplexIDOfMiddle()) {
			return new UnaryOpExpression(new PrimitiveOperator(PrimitiveOperator.ID_OP),
			                             middle);
		}

		if (((PrimitiveOperator) op).isComplexNotOfMiddle()) {
			return new UnaryOpExpression(new PrimitiveOperator(PrimitiveOperator.NOT_OP),
			                             middle);
		}

		return this; //dammy
	}

	/**
	 * returns a replica of this expression
	 * @return a replica of this expression
	 */
	public Expression duplicate() {
		return new TrinaryOpExpression(op, left.duplicate(),
		                               middle.duplicate(), right.duplicate());
	}

	/**
	 * Returns if two legs of the trinary gate shares the same input.
	 * @return true if two legs of the trinary gate shares the same input.
	 */
	public boolean hasSharedInput() {
		return left.hasSharedInput(right) || left.hasSharedInput(middle) ||
		       middle.hasSharedInput(left) || middle.hasSharedInput(right) ||
		       right.hasSharedInput(left) || right.hasSharedInput(middle);
	}

	/**
	 * if hasSharedInput() is true, combine the gates into one simple gate.
	 * and returns it.
	 * should be called only in hasSharedInput() is true.
	 * @return the simplified gate.
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
				return new TrinaryOpExpression(((PrimitiveOperator) op).combineRightNRightOnLeft(gateOp),
				                               lowerGate.getLeft(), middle,
				                               right);
			}

			// else right == lowerGate.getLeft())
			return new TrinaryOpExpression(((PrimitiveOperator) op).combineRightNLeftOnLeft(gateOp),
			                               lowerGate.getRight(), middle, right);
		} else if (left.hasSharedInput(middle)) {
			lowerGate =
				(BinaryOpExpression) ((AssignmentStatement) ((LvalExpression) left).getAssigningStatement()).getRHS();

			gateOp = (PrimitiveOperator) lowerGate.getOperator();

			if (middle == lowerGate.getRight()) {
				return new TrinaryOpExpression(((PrimitiveOperator) op).combineMidNRightOnLeft(gateOp),
				                               lowerGate.getLeft(), middle,
				                               right);
			}

			// else right == lowerGate.getLeft())
			return new TrinaryOpExpression(((PrimitiveOperator) op).combineMidNLeftOnLeft(gateOp),
			                               lowerGate.getRight(), middle, right);
		}
		/*******/
		else if (middle.hasSharedInput(right)) {
			lowerGate =
				(BinaryOpExpression) ((AssignmentStatement) ((LvalExpression) middle).getAssigningStatement()).getRHS();

			gateOp = (PrimitiveOperator) lowerGate.getOperator();

			if (right == lowerGate.getRight()) {
				return new TrinaryOpExpression(((PrimitiveOperator) op).combineRightNRightOnMid(gateOp),
				                               left, lowerGate.getLeft(), right);
			}

			// else right == lowerGate.getLeft())
			return new TrinaryOpExpression(((PrimitiveOperator) op).combineRightNLeftOnMin(gateOp),
			                               left, lowerGate.getRight(), right);
		} else if (middle.hasSharedInput(left)) {
			lowerGate =
				(BinaryOpExpression) ((AssignmentStatement) ((LvalExpression) middle).getAssigningStatement()).getRHS();

			gateOp = (PrimitiveOperator) lowerGate.getOperator();

			if (left == lowerGate.getRight()) {
				return new TrinaryOpExpression(((PrimitiveOperator) op).combineLeftNRightOnMid(gateOp),
				                               left, lowerGate.getLeft(), right);
			}

			// else left == lowerGate.getLeft())
			return new TrinaryOpExpression(((PrimitiveOperator) op).combineLeftNLeftOnMid(gateOp),
			                               left, lowerGate.getRight(), right);
		}
		/***************/
		else if (right.hasSharedInput(middle)) {
			lowerGate =
				(BinaryOpExpression) ((AssignmentStatement) ((LvalExpression) right).getAssigningStatement()).getRHS();

			gateOp = (PrimitiveOperator) lowerGate.getOperator();

			if (middle == lowerGate.getRight()) {
				return new TrinaryOpExpression(((PrimitiveOperator) op).combineMidNRightOnRight(gateOp),
				                               left, middle, lowerGate.getLeft());
			}

			// else middle == lowerGate.getLeft())
			return new TrinaryOpExpression(((PrimitiveOperator) op).combineMidNLeftOnRight(gateOp),
			                               left, middle, lowerGate.getRight());
		} else if (right.hasSharedInput(left)) {
			lowerGate =
				(BinaryOpExpression) ((AssignmentStatement) ((LvalExpression) right).getAssigningStatement()).getRHS();

			gateOp = (PrimitiveOperator) lowerGate.getOperator();

			if (left == lowerGate.getRight()) {
				return new TrinaryOpExpression(((PrimitiveOperator) op).combineLeftNRightOnRight(gateOp),
				                               left, middle, lowerGate.getLeft());
			}

			// else left == lowerGate.getLeft())
			return new TrinaryOpExpression(((PrimitiveOperator) op).combineLeftNLeftOnRight(gateOp),
			                               left, middle, lowerGate.getRight());
		}

		return null; //dammy
	}
}
