// IfStatement.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;

import java.util.HashMap;
import java.util.Iterator;


/**
 * A class for representing if statement that can be defined
 * in the program.
 */
public class IfStatement extends Statement {
	//~ Instance fields --------------------------------------------------------

	// data members

	/*
	 * Holds the condition of the if statement.
	 */
	private Expression condition;

	/*
	 * Holds the block of the if statement.
	 */
	private Statement thenBlock;

	/*
	 * Holds the else block of the if statement.
	 */
	private Statement elseBlock;

	//~ Constructors -----------------------------------------------------------

	/**
	 * Construct a new if statement.
	 * @param condition the condition of the if statement.
	 * @param thenBlock the block of the if statement.
	 * @param elseBlock the block of the else statement.
	 */
	public IfStatement(Expression condition, Statement thenBlock,
	                   Statement elseBlock) {
		this.condition     = condition;
		this.thenBlock     = thenBlock;
		this.elseBlock     = elseBlock;
	}

	//~ Methods ----------------------------------------------------------------

	/**
	 * Unique vars transformations.
	 */
	public Statement uniqueVars() {
		//start new scope
		Function.pushScope();

		BlockStatement newBlock = new BlockStatement();

		//unique vars transformations on the if block
		newBlock.addStatement(thenBlock.uniqueVars());

		//end the scope
		HashMap endedScope = Function.popScope();

		//update the vars from the previuos scope
		Iterator it = endedScope.keySet().iterator();

		while (it.hasNext()) {
			String lvalName = (String) (it.next());

			//lval in if block
			LvalExpression lvalInIf = (LvalExpression) endedScope.get(lvalName);

			//lval before if block
			LvalExpression lvalBeforeIf = Function.getVar(lvalInIf);
			Function.addVar(lvalInIf);

			//the var gets mux between its lval in if block and its lval
			//before if block
			newBlock.addStatement(new AssignmentStatement(Function.getVar(lvalInIf),
			                                              new TrinaryOpExpression(new PrimitiveOperator(PrimitiveOperator.MUX_OP),
			                                                                      condition,
			                                                                      lvalInIf,
			                                                                      lvalBeforeIf)));
		}

		return newBlock;
	}

	/**
	 * Transforms this multibit AssignmentStatement into singlebit statements
	 * and returns the result.
	 * @param obj not needed (null).
	 * @return a BlockStatement containing the result statements.
	 */
	public BlockStatement multi2SingleBit(Object obj) {
		BlockStatement result = new BlockStatement();

		// create a temp var that holds the condition result
		LvalExpression conditionResult =
			Function.addTempLocalVar("conditionResult" + (conditionIndex++),
			                         new BooleanType());

		// create the assignment statement that assings the result
		AssignmentStatement conditionResultAs =
			new AssignmentStatement(conditionResult,
			                        new BinaryOpExpression(new NotEqualOperator(),
			                                               condition,
			                                               new BooleanConstant(false)));

		// evaluate the condition and stores it in the conditionResult
		result.addStatement(conditionResultAs.multi2SingleBit(null));

		// add the if statement
		result.addStatement(new IfStatement(conditionResult.bitAt(0),
		                                    thenBlock.multi2SingleBit(null),
		                                    null));

		// prepare else block
		if (elseBlock != null) {
			// create a temp var that holds the condition result
			LvalExpression notConditionResult =
				Function.addTempLocalVar("conditionResult" +
				                         (conditionIndex++), new BooleanType());

			// create the assignment statement that assings the result
			AssignmentStatement notConditionResultAs =
				new AssignmentStatement(notConditionResult,
				                        new UnaryOpExpression(new PrimitiveOperator(PrimitiveOperator.NOT_OP),
				                                              conditionResult));

			// evaluate the condition and stores it in the conditionResult
			result.addStatement(notConditionResultAs.multi2SingleBit(null));

			// add the if statement
			result.addStatement(new IfStatement(notConditionResult.bitAt(0),
			                                    elseBlock.multi2SingleBit(null),
			                                    null));
		}

		return result;
	}

	/**
	 * Returns a replica of this IfStatement.
	 * @return a replica of this IfStatement.
	 */
	public Statement duplicate() {
		return new IfStatement(condition.duplicate(), thenBlock.duplicate(),
		                       (elseBlock == null) ? null : elseBlock.duplicate());
	}

	/**
	 * Returns a string representation of this IfStatement.
	 * @return a string representation of this IfStatement.
	 */
	public String toString() {
		return "IF (" + condition + ")\nTHEN\n" + thenBlock + "ELSE\n" +
		       elseBlock;
	}

	//~ Static fields/initializers ---------------------------------------------

	/*
	 * For unique var names.
	 */
	private static int conditionIndex = 0;
}
