// EqualOperator.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;


/**
 * A class for representing == operator expressions that can be defined
 * in the program.
 */
public class EqualOperator extends Operator implements Multi2SingleBit {
	//~ Methods ----------------------------------------------------------------

	/**
	 * Returns a string representation of the object.
	 * @return a string representation of the object.
	 */
	public String toString() {
		return "==";
	}

	/**
	 * Returns 2 as the arity of this PlusOperator.
	 * Arity is 1 for unary ops; 2 for binary ops; 3 for ternary ops;
	 * 0 for constants
	 * @return 2 as the arity of this PlusOperator.
	 */
	public int arity() {
		return 2;
	}

	/**
	 * Transforms this multibit expression into singlebit statements
	 * and adds them to the appropriate function.
	 * Note: x&lt;=y and x &gt;=y &lt;==&gt; x==y.
	 * @param obj the AssignmentStatement that holds this GreaterOperator.
	 */
	public BlockStatement multi2SingleBit(Object obj) {
		AssignmentStatement as     = ((AssignmentStatement) obj);
		LvalExpression      lhs    = as.getLHS(); //LHS of the param statement
		BinaryOpExpression  rhs    = (BinaryOpExpression) (as.getRHS());
		BlockStatement      result = new BlockStatement();

		Expression          right = rhs.getRight();
		Expression          left  = rhs.getLeft();

		// x<=y and x >=y
		LvalExpression lessEqual =
			Function.addTempLocalVar(lhs.getName() + "$lessEqual",
			                         new BooleanType());

		// create an assignment statement for calculating 
		// x<=y and execute multi2SingleBit transforamtion on it
		AssignmentStatement lessEqualAs =
			new AssignmentStatement(
			// lhs
			lessEqual,
			                        
			// rhs
			new BinaryOpExpression(new LessEqualOperator(), left, right));

		//execute multi2SingleBit transforamtion
		result.addStatement(lessEqualAs.multi2SingleBit(null));

		// create an assignment statement for calculating 
		// x>=y and execute multi2SingleBit transforamtion on it
		LvalExpression greaterEqual =
			Function.addTempLocalVar(lhs.getName() + "$greater",
			                         new BooleanType());

		AssignmentStatement greaterEqualAs =
			new AssignmentStatement(
			// lhs
			greaterEqual,
			                        
			// rhs
			new BinaryOpExpression(new GreaterEqualOperator(), left, right));

		//execute multi2SingleBit transforamtion
		result.addStatement(greaterEqualAs.multi2SingleBit(null));

		// assign the and of the results to the final result
		for (int i = 0; i < lhs.size(); i++)
			result.addStatement(new AssignmentStatement(
			// lhs
			lhs.lvalBitAt(i), //currentFunction.fromName(lhs.getName()+"$"+i),
			                                            new BinaryOpExpression(new PrimitiveOperator(PrimitiveOperator.AND_OP),
			                                                                   lessEqual.bitAt(0),
			                                                                   greaterEqual.bitAt(0))));

		return result;
	}

	/**
	 * Returns 1 - The priority of this operator.
	 * @return 1 - The priority of this operator.
	 */
	public int priority() {
		return 1;
	}
}
