// AssignmentStatement.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;

import java.io.PrintWriter;

import java.util.Vector;


/**
 * A class for representing assignment statements that can be defined
 * in the program.
 */
public class AssignmentStatement extends Statement implements OutputWriter,
                                                              Optimize
{
	//~ Instance fields --------------------------------------------------------

	// data members

	/*
	 * An LvalExpression which is the LHS of the assignment.
	 */
	private LvalExpression lhs;

	/*
	 * An OperationExpression which is the RHS of the assignment expression.
	 */
	private OperationExpression rhs;

	/*
	 * The line number of this assignment statement in the output circuit.
	 * This number may vary form one transformation to another.
	 */
	private int outputLine = -1;

	//~ Constructors -----------------------------------------------------------

	/**
	 * Constructs a new AssignmentStatement from a given lhs and rhs.
	 * @param lhs An LvalExpression which is the LHS of the assignment.
	 * @param rhs An OperationExpression which is the RHS of the assignment
	 * expression.
	 */
	public AssignmentStatement(LvalExpression lhs, OperationExpression rhs) {
		this.lhs     = lhs;
		this.rhs     = rhs;
		this.lhs.setAssigningStatement(this);
	}

	//~ Methods ----------------------------------------------------------------

	/**
	 * Transforms this multibit AssignmentStatement into singlebit Statements
	 * and returns a BlockStatement containing the result.
	 * @param obj not used (null).
	 * @return BlockStatement containing singlebit Statements
	 *                 of this AssignmentStatement.
	 */
	public BlockStatement multi2SingleBit(Object obj) {
		return ((Multi2SingleBit) rhs).multi2SingleBit(this);
	}

	/**
	 * Returns a string representation of this AssignmentStatement.
	 * @return a string representation of this AssignmentStatement.
	 */
	public String toString() {
		return lhs.toString() + '=' + rhs.toString() + "\n";
	}

	/**
	 * Returns this AssignmentStatement's rhs.
	 * @return this AssignmentStatement's rhs.
	 */
	public OperationExpression getRHS() {
		return rhs;
	}

	/**
	 * Returns this AssignmentStatement's lhs.
	 * @return this AssignmentStatement's lhs.
	 */
	public LvalExpression getLHS() {
		return lhs;
	}

	/**
	 * Prints this AssignmentStatement into the circuit.
	 * @param circuit the circuit output file.
	 */
	public void toCircuit(PrintWriter circuit) {
		outputLine = Program.getLineNumber();

		circuit.print(outputLine + " " + ((lhs.isOutput()) ? "output " : ""));
		((OutputWriter) rhs).toCircuit(circuit);
		circuit.println(((lhs.isOutput()) ? ("\t//" + lhs.getName()) : ""));
	}

	/**
	 * Returns an int that represents the line number of this
	 * assignmnet statement in the output circuit.
	 * @return an int that represents the line number of this
	 * assignmnet statement in the output circuit.
	 */
	public int getOutputLine() {
		return outputLine;
	}

	/**
	 * Sets the output line of this assignment statement.
	 * @param line the line number in the output.
	 */
	public void setOutputLine(int line) {
		outputLine = line;
	}

	/**
	 * Optimizes this AssignmentStatement (Phase I): <br>
	 *
	 * Peephole optimization: local simplifications of code, e.g.
	 * (x and true --&gt; x), (x or not x --&gt; true), etc. <br>
	 * Duplicate code removal: a hash table of all values computed in the
	 * circuit is kept. If some value is computed twice, then one of the
	 * duplicates is removed and replaced with direct access to the other
	 * wire.<br>
	 * For more information see documation files.
	 *
	 */
	public void optimizePhaseI() {
		// the statement have to have an output line inorder to be able to sort 
		// input pins and refer to the output gate.
		// NOTE that this assignment of outputLine may not be the final one 
		//  (refer to the optimization phase II and circuit writing).
		// 
		outputLine = Program.getLineNumber();

		boolean statementChanged = true;

		do {
			// tracks if there where changes is this AssignmentStatment
			if (rhs.hasUnaryInput()) // if (this has unary exp as input)
			 {
				rhs.combineUnaryInput();
			} else if (rhs.hasSharedInput()) {
				rhs = rhs.combineSharedInput();
			}
			// NOTE:
			// the following two trasformations can reduce the rhs's arity.
			else if (((OperationExpression) rhs).hasConstantInput())
			//if (this has constant input)
			 {
				//combine the input into the gate;
				rhs = rhs.combineConstInput();
			} else if (rhs.hasEqualInputs()) {
				//combine equal inputs into one.
				rhs = rhs.combineEqualInputs();
			} else {
				statementChanged = false;
			}
		} while (statementChanged); //while (there are changes 
		                            //in this AssignmentStatmenet);

		if (rhs.isComplexIDOrNeg()) {
			rhs = rhs.simplify();
		}

		if (rhs.isConstant()) { // if the optimized 
			                    // gate returns a constant result
			rhs = rhs.getConstantOutput();
		}

		rhs.sortInputs();

		if (Optimizer.containsGateNegGate(rhs)) {
			rhs = Optimizer.getReference(rhs);
		} else {
			Optimizer.addGate(rhs, lhs);
		}
	}

	/**
	 * Second phase of the optimization: Dead code elimination.
	 * This statement adds to the new function-body, only if
	 * it is relevant the computation of the output
	 * pins of the circuit ( acording to a list of the needed statements that
	 * was assembled in earlier stage ).
	 * @param newBody the new function-body.
	 */
	public void optimizePhaseII(Vector newBody) {
		if (Optimizer.isUsed(this)) {
			newBody.add(this);
		}
	}

	/**
	 * Adds this AssignmentStatement to the list of statements that are
	 * needed to compute the output pins of the output circuit, if this
	 * AssigmentStatement IS indeed needed. The list is stored in
	 * Optimizer data structure.
	 */
	public void buildUsedStatementsHash() {
		if (lhs.isOutput()) {
			Optimizer.putUsedStatement(this);
		}

		if (Optimizer.isUsed(this)) {
			Vector v = rhs.getLvalExpressionInputs();

			for (int i = 0; i < v.size(); i++) {
				Statement as =
					((LvalExpression) (v.elementAt(i))).getAssigningStatement();
				Optimizer.putUsedStatement(as);
			}
		}
	}

	/**
	 * Unique vars transformations.
	 */
	public Statement uniqueVars() {
		rhs.changeReference(Function.getVars());

		// get the last referance existing
		lhs = Function.getVar(lhs);

		if (lhs.getAssigningStatement() != this) {
			Function.addVar(lhs); //.getName(), lhs.getType(), lhs.isOutput());

			// get the new ref to lhs
			lhs = Function.getVar(lhs);

			lhs.setAssigningStatement(this);
		}

		return this;
	}

	/**
	 * Returns true iff rhs if an UnaryOpExpression.
	 * @return true iff rhs if an UnaryOpExpression.
	 */
	public boolean hasUnaryOperator() {
		return rhs instanceof UnaryOpExpression;
	}

	/**
	 * Returns a replica this statement.
	 * @return a replica this statement.
	 */
	public Statement duplicate() {
		return new AssignmentStatement(lhs /*.duplicate() no need*/,
		                               (OperationExpression) rhs.duplicate());
	}
}
