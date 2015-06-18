//  LessEqualOperator.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;


/**
 * A class for representing &lt;= operator expressions that can be defined
 * in the program.
 */
public class LessEqualOperator extends Operator implements Multi2SingleBit {
	//~ Methods ----------------------------------------------------------------

	/**
	 * Returns a string representation of the object.
	 */
	public String toString() {
		return "<=";
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
	 * and returns the result.
	 * @param obj the AssignmentStatement that holds this GreaterOperator.
	 * @return a BlockStatement contating the result statements.
	 */
	public BlockStatement multi2SingleBit(Object obj) {
		AssignmentStatement as     = ((AssignmentStatement) obj);
		LvalExpression      lhs    = as.getLHS(); //LHS of the param statement
		BinaryOpExpression  rhs    = (BinaryOpExpression) (as.getRHS());
		BlockStatement      result = new BlockStatement();

		Expression          right = rhs.getRight();
		Expression          left  = rhs.getLeft();

		// calculate   x<=y <==> !(x>y)
		LvalExpression greater =
			Function.addTempLocalVar(lhs.getName() + "$greater",
			                         new BooleanType());

		// create an assignment statement for calculating 
		// x>y and execute multi2SingleBit transforamtion on it
		AssignmentStatement greaterAs =
			new AssignmentStatement(
			// lhs
			greater, 
			// rhs
			new BinaryOpExpression(new GreaterOperator(), left, right));

		//execute multi2SingleBit transforamtion
		result.addStatement(greaterAs.multi2SingleBit(null));

		// assign the negative of the most result bit to the result
		for (int i = 0; i < lhs.size(); i++)
			result.addStatement(new AssignmentStatement(
			// lhs
			lhs.lvalBitAt(i), //currentFunction.fromName(lhs.getName()+"$"+i),
			                                            new UnaryOpExpression(new PrimitiveOperator(PrimitiveOperator.NOT_OP),
			                                                                  greater.bitAt(0))));

		return result;
	}

	/**
	 * Returns an int theat represents the priority of the operator
	 * @return an int theat represents the priority of the operator
	 */
	public int priority() {
		return 1;
	}
}
