// PlusOperator.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;


/**
 * A class for representing binary + operator expressions that can be defined
 * in the program.
 */
public class PlusOperator extends Operator implements Multi2SingleBit {
	//~ Methods ----------------------------------------------------------------

	/**
	 * Returns a string representation of the object.
	 */
	public String toString() {
		return "+";
	}

	/**
	 * Returns 2 as the arity of this PlusOperator.
	 * Arity is 1 for unary ops; 2 for binary ops; 3 for ternary ops;
	 0 for constants
	 * @return 2 as the arity of this PlusOperator.
	 */
	public int arity() {
		return 2;
	}

	/**
	 * Transforms this multibit expression into singlebit statements
	 * and returns the result.
	 * @param obj the AssignmentStatement that holds this PlusOperator.
	 * @return a BlockStatement containing the result statements.
	 */
	public BlockStatement multi2SingleBit(Object obj) {
		AssignmentStatement as     = ((AssignmentStatement) obj);
		LvalExpression      lhs    = as.getLHS(); //LHS of the param statement
		BinaryOpExpression  rhs    = (BinaryOpExpression) (as.getRHS());
		BlockStatement      result = new BlockStatement();

		Expression          right = rhs.getRight();
		Expression          left  = rhs.getLeft();

		// create a temporary lvalue for the temp xor operation
		LvalExpression tmp =
			Function.addTempLocalVar(lhs.getName() + "$tmp", lhs.getType());

		// create a temporary lvalue for the carry 
		LvalExpression carry =
			Function.addTempLocalVar(lhs.getName() + "$carry", lhs.getType());

		tmp.setAssigningStatement(as);
		carry.setAssigningStatement(as);

		// first carray	
		result.addStatement(new AssignmentStatement(
		// lhs
		carry.lvalBitAt(0), //currentFunction.fromName(carry.getName()+"$"+0), 
		                                            
		// rhs
		new TrinaryOpExpression(new PrimitiveOperator(PrimitiveOperator.MAJ_OP),
		                        left.bitAt(0), right.bitAt(0),
		                        new BooleanConstant(false))));

		// start calculating x+y - the first xor
		result.addStatement(new AssignmentStatement(
		// lhs
		lhs.lvalBitAt(0), //currentFunction.fromName(lhs.getName()+"$"+0),
		                                            
		// rhs
		new BinaryOpExpression(new PrimitiveOperator(PrimitiveOperator.XOR_OP),
		                       left.bitAt(0), right.bitAt(0))));

		// the size of the loop
		int size = (lhs.size() < rhs.size()) ? lhs.size() : rhs.size();

		for (int j = 1; j < size; j++) {
			// tmp result
			result.addStatement(new AssignmentStatement(
			// lhs
			tmp.lvalBitAt(j), //currentFunction.fromName(tmp.getName()+"$"+j),
			                                            
			// rhs
			new BinaryOpExpression(new PrimitiveOperator(PrimitiveOperator.XOR_OP),
			                       left.bitAt(j), right.bitAt(j))));

			// next carray
			result.addStatement(new AssignmentStatement(
			// lhs
			carry.lvalBitAt(j), //currentFunction.fromName(carry.getName()+"$"+j), 
			                                            
			// rhs
			new TrinaryOpExpression(new PrimitiveOperator(PrimitiveOperator.MAJ_OP),
			                        left.bitAt(j), right.bitAt(j),
			                        carry.bitAt(j - 1))));

			// result
			result.addStatement(new AssignmentStatement(
			// lhs
			lhs.lvalBitAt(j), //currentFunction.fromName(lhs.getName()+"$"+j),
			                                            
			// rhs
			new BinaryOpExpression(new PrimitiveOperator(PrimitiveOperator.XOR_OP),
			                       tmp.bitAt(j), carry.bitAt(j - 1))));
		}

		// end for
		for (int i = size; i < lhs.size(); i++)
			result.addStatement(new AssignmentStatement(
			// lhs
			lhs.lvalBitAt(i), //currentFunction.fromName(lhs.getName()+"$"+i),
			                                            
			// rhs
			new UnaryOpExpression(new PrimitiveOperator(PrimitiveOperator.ID_OP),
			                      carry.bitAt(size - 1))));

		return result;
	}

	/**
	 * Returns an int that represents the priority of the operator
	 * @return an int that represents the priority of the operator
	 */
	public int priority() {
		return 2;
	}
}
