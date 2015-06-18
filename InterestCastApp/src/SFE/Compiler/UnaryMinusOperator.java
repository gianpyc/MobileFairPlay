// UnaryMinusOperator.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;


/**
 *
 */
class UnaryMinusOperator extends Operator implements Multi2SingleBit {
	//~ Methods ----------------------------------------------------------------

	/**
	 * Transforms this multibit expression into singlebit statements
	 * and return the result.
	 * Note: -y is (!y)+1.
	 * @param obj the AssignmentStatement that holds this UnaryMinusOperator.
	 * @return a BlockStatement containing the result statements
	 */
	public BlockStatement multi2SingleBit(Object obj) {
		AssignmentStatement as     = ((AssignmentStatement) obj);
		LvalExpression      lhs    = as.getLHS(); //LHS of the param statement
		UnaryOpExpression   rhs    = (UnaryOpExpression) (as.getRHS());
		BlockStatement      result = new BlockStatement();

		// !y
		// create a temporary lvalue that will hold the mid expression result
		LvalExpression notMid =
			Function.addTempLocalVar(lhs.getName() + "$not", lhs.getType());

		// create an assignment statement for calculating 
		// !y and execute multi2SingleBit transforamtion on it
		AssignmentStatement notAs =
			new AssignmentStatement(
			// lhs
			notMid,
			                        
			// rhs
			new UnaryOpExpression(new PrimitiveOperator(PrimitiveOperator.NOT_OP),
			                      rhs.getMiddle()));

		//execute multi2SingleBit transforamtion
		result.addStatement(notAs.multi2SingleBit(null));

		// add one to the previous result	 (!y) +1
		//										  ===
		AssignmentStatement plusOneAs =
			new AssignmentStatement(
			// lhs
			lhs,
			                        
			// rhs
			new BinaryOpExpression(new PlusOperator(), notMid,
			                       new IntConstant(1)));

		// now add the above statement to the result
		result.addStatement(plusOneAs.multi2SingleBit(null));

		return result;
	}

	/**
	 * Returns 1 as the arity of this UnaryMinusOperator.
	 * Arity is 1 for unary ops; 2 for binary ops; 3 for ternary ops; 0 for constants
	 * @return 1 as the arity of this UnaryMinusOperator.
	 */
	public int arity() {
		return 1;
	}

	/**
	 * Returns a string representation of the object.
	 * @return a string representation of the object.
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
