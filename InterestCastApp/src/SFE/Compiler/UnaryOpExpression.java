// UnaryOpExpression.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;

import java.io.PrintWriter;

import java.util.Vector;


/**
 * A class for representing unary operator expressions that can be defined
 * in the program.
 */
public class UnaryOpExpression extends OperationExpression {
	//~ Instance fields --------------------------------------------------------

	// data members

	/*
	 * Hold the input of this expression.
	 */
	private Expression middle;

	//~ Constructors -----------------------------------------------------------

	/**
	 * Constructs a new UnaryOpExpression from a given oparator and input.
	 * @param op the unary operator.
	 * @param middle the input.
	 */
	public UnaryOpExpression(Operator op, Expression middle) {
		super(op);
		this.middle     = middle;

		// assigning the size of this UnaryOpExpression
		size = middle.size();
	}

	//~ Methods ----------------------------------------------------------------

	/**
	 * Returns an array of the input LvalExpressions of this gate.
	 * This method is used in the second phase of the optimization.
	 * @return an array of the input LvalExpressions of this gate.
	 */
	public Vector returnInputs() {
		Vector result = new Vector();

		if (middle instanceof LvalExpression) {
			result.add(middle);
		}

		return result;
	}

	/**
	 * Returns a string representation of the object.
	 *
	 *
	 * Returns a string representing this object as it appear at the
	 * output circuit.
	 * @return a string representing this object as it appear at the
	 * output circuit.
	 */
	public String toString() {
		if (((PrimitiveOperator) op).isID() &&
			    middle instanceof ConstExpression) {
			return "gate arity 0 table [" + ((OutputWriter) middle).toString() +
			       "] inputs []";
		}

		return ((OutputWriter) op).toString() + " inputs [ " +
		       ((OutputWriter) middle).toString() + " ]";
	}

	/**
	 * Transforms this multibit expression into singlebit statements
	 * and adds them to the appropriate function.
	 * @param obj the AssignmentStatement that holds this UnaryOpExpression.
	 */
	public BlockStatement multi2SingleBit(Object obj) {
		AssignmentStatement as     = ((AssignmentStatement) obj);
		LvalExpression      lhs    = as.getLHS(); //LHS of the param statement
		BlockStatement      result = new BlockStatement();

		middle = middle.evaluateExpression(as, result);

		if (op instanceof PrimitiveOperator) {
			for (int i = 0; i < lhs.size(); i++) {
				// see the AS of LHS before adding new AS
				AssignmentStatement newAS =
					new AssignmentStatement(lhs.lvalBitAt(i), //currentFunction.fromName(lhs.getName()+"$"+i),
					                        new UnaryOpExpression(op,
					                                              middle.bitAt(i)));
				result.addStatement(newAS);
			}
		} else { // op is arithmetic unary operator
			result.addStatement(((Multi2SingleBit) op).multi2SingleBit(obj));
		}

		return result;
	}

	/**
	 * return the input of the expression
	 * @return Expression the input of the unary expression
	 */
	public Expression getMiddle() {
		return middle;
	}

	/**
	 * Returns a string representing this object as it appear at the
	 * output circuit.
	 * @return a string representing this object as it appear at the
	 * output circuit.
	 */
	public void toCircuit(PrintWriter circuit) {
		if (((PrimitiveOperator) op).isID() &&
			    middle instanceof ConstExpression) {
			circuit.print("gate arity 0 table [");
			((OutputWriter) middle).toCircuit(circuit);
			circuit.print("] inputs []");
		} else {
			((OutputWriter) op).toCircuit(circuit);
			circuit.print(" inputs [ ");
			((OutputWriter) middle).toCircuit(circuit);
			circuit.print(" ]");
		}
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

		if (middle instanceof LvalExpression &&
			    ((LvalExpression) middle).hasUnaryInput()) {
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
		if (((LvalExpression) middle).unaryInputIsNotResult()) {
			// input is result of Not gate - combine in to this gate
			op = ((PrimitiveOperator) op).negMid();
		}

		// input is result of ID gate - no changes to this gate is needed
		middle = ((LvalExpression) middle).getMiddleOfUnaryInput();
	}

	/**
	 * Checks if the input is constant and the unary operator
	 * is not
	 * @return true if the input is constant and the unary operator is not
	 */
	public boolean hasConstantInput() {
		return middle instanceof BooleanConstant &&
		       ((PrimitiveOperator) op).isNot();
	}

	/**
	 * Combines an input expression that is constant and return the
	 * result expression. This method reduces the
	 * arity of this OperationExpression.
	 * This method should be called only if hasConstantInput is true.
	 * @return the result expression.
	 */
	public OperationExpression combineConstInput() {
		// note that op is NOT_ID operator
		if (((BooleanConstant) middle).getConst()) { // true const

			return new UnaryOpExpression(new PrimitiveOperator(PrimitiveOperator.ID_OP),
			                             new BooleanConstant(false));
		}

		// else
		return new UnaryOpExpression(new PrimitiveOperator(PrimitiveOperator.ID_OP),
		                             new BooleanConstant(true));
	}

	/**
	 * Returns false as there is only one input to UnaryOpExpression.
	 * @return false.
	 */
	public boolean hasEqualInputs() {
		return false;
	}

	/**
	 * Returns this expression since there is nothig to do. See hasEqualInputs() notes.
	 * @return this.
	 */
	public OperationExpression combineEqualInputs() {
		return this;
	}

	/**
	 * Sorts the input gates according to their names and returns
	 * the result OperationExpression. This method is used in the optimization
	 * process. In UnaryOpExpression there is nothing to sort since it
	 * has only one input.
	 * @return the OperationExpression with the sorted inputs (this).
	 */
	public OperationExpression sortInputs() {
		return this;
	}

	/**
	 * returns the negate gate (OperatorExpression) of this OperatorExpression.
	 * @return the negate gate (OperatorExpression) of this OperatorExpression.
	 */
	public OperationExpression negate() {
		return new UnaryOpExpression(((PrimitiveOperator) op).negOut(), middle);
	}

	/**
	 * Returns an array of the input LvalExpressions of this gate.
	 * This method is used in the second phase of the optimization.
	 * @return an array of the input LvalExpressions of this gate.
	 */
	public Vector getLvalExpressionInputs() {
		Vector result = new Vector();

		if (middle instanceof LvalExpression) {
			result.add(middle);
		}

		return result;
	}

	public void changeReference(UniqueVariables unique) {
		if (middle instanceof LvalExpression) {
			middle = unique.getVar(((LvalExpression) middle).getName());
		}
	}

	/**
	 * cannot happen
	 */
	public boolean isComplexIDOrNeg() {
		return false;
	}

	/**
	 * never called
	 */
	public OperationExpression simplify() {
		return this;
	}

	/**
	 * returns a replica of this expression
	 * @return a replica of this expression
	 */
	public Expression duplicate() {
		return new UnaryOpExpression(op, middle.duplicate());
	}
}
