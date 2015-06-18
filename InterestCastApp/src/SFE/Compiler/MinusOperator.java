// MinusOperator.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;


/**
 * A class that represents minus (-) operator that can be defined in the
 * program.
 */
class MinusOperator extends Operator implements Multi2SingleBit {
	//~ Methods ----------------------------------------------------------------

	/**
	 * Transforms this multibit expression into singlebit statements
	 * and returns the result.
	 * Note: x-y &lt;==&gt; x+(-y).
	 * @param obj the AssignmentStatement that holds this MinusOperator.
	 * @return a BlockStatement containing the result statements.
	 */
	public BlockStatement multi2SingleBit(Object obj) {
		AssignmentStatement as     = ((AssignmentStatement) obj);
		LvalExpression      lhs    = as.getLHS(); //LHS of the param statement
		BinaryOpExpression  rhs    = (BinaryOpExpression) (as.getRHS());
		BlockStatement      result = new BlockStatement();

		Expression          right = rhs.getRight();
		Expression          left  = rhs.getLeft();

		// -y
		// create a temporary lvalue that will hold the mid expression result
		LvalExpression minusRight =
			Function.addTempLocalVar(lhs.getName() + "$minus",
			                         new BusType(right.size()));

		// create an assignment statement for calculating 
		// -y and execute multi2SingleBit transforamtion on it
		AssignmentStatement minusAs =
			new AssignmentStatement(
			// lhs
			minusRight, 
			// rhs
			new UnaryOpExpression(new UnaryMinusOperator(), right));

		//execute multi2SingleBit transforamtion
		result.addStatement(minusAs.multi2SingleBit(null));

		//add the left side  x + (-y)
		//					=====  
		AssignmentStatement finalAs =
			new AssignmentStatement(
			// lhs
			lhs, 
			// rhs
			new BinaryOpExpression(new PlusOperator(), left, minusRight));

		// now add the above statement to the result
		result.addStatement(finalAs.multi2SingleBit(null));

		return result;
	}

	/**
	 * Returns 2 as the arity of this UnaryMinusOperator.
	 * Arity is 1 for unary ops; 2 for binary ops; 3 for ternary ops; 0 for constants
	 * @return 2 as the arity of this UnaryMinusOperator.
	 */
	public int arity() {
		return 2;
	}

	/**
	 * Returns a string representation of the object.
	 */
	public String toString() {
		return "-";
	}

	/**
	 * Returns an int theat represents the priority of the operator
	 * @return an int theat represents the priority of the operator
	 */
	public int priority() {
		return 2;
	}
}
