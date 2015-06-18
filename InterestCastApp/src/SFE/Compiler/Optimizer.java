// Optimizer.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;

import java.util.HashMap;


/**
 * Optimizer holds all the data structures needed for the optimization process.
 */
public class Optimizer {
	//~ Methods ----------------------------------------------------------------

	/**
	 * Associates the specified gate with it's circuit representation (String) in this map.
	 * NOTE: this method shold be called only if containsGate() and containsNegGate
	 * are false.
	 * @param gate the gate (OperationExpression) to be added.
	 * @param holdingExpression the lValexpression that hold the result of the gate.
	 */
	public static void addGate(OperationExpression gate,
	                           LvalExpression holdingExpression) {
		OperationExpression neg = gate.negate();

		// if gate is Unary gate the I don't want to get a referance to it the 
		// the future only to Binary and Trinary gates
		if (gate instanceof UnaryOpExpression) {
			return;
		}

		// imidiately add the reference to the gate and it nagation so when the time 
		// will come to get the reference everything will be prepared
		gates.put(gate.toString(),
		          new UnaryOpExpression(new PrimitiveOperator(PrimitiveOperator.ID_OP),
		                                holdingExpression));
		gates.put(neg.toString(),
		          new UnaryOpExpression(new PrimitiveOperator(PrimitiveOperator.NOT_OP),
		                                holdingExpression));
	}

	/**
	 * Returns true if optimizer contains a mapping for the specified gate.
	 * @param gate the gate (OperationExpression) whose presence in the optimizer is to be tested.
	 * @return true if optimizer contains a mapping for the specified gate.
	 */
	public static boolean containsGateNegGate(OperationExpression gate) {
		return gates.containsKey(gate.toString());
	}

	/**
	 * Returns an UnaryOpExpression containing the ID_OP or NOT_OP to
	 * the specified gate. NOTE: this method should be call only if
	 * containsGateNegGate() if true.
	 * @param gate The circuit representation (String) of the gate (OperatorExpression) whose reference it to be returned.
	 * @return an UnaryOpExpression containing the ID_OP or NOT_OP to
	 * the specified gate.
	 */
	public static UnaryOpExpression getReference(OperationExpression gate) {
		return (UnaryOpExpression) (gates.get(gate.toString()));
	}

	/**
	 * Adds an AssignmentStatement to the usage data structure.
	 * @param as the AssignmentStatement to be added.
	 */
	public static void putUsedStatement(Statement s) {
		usedStatements.put(s, null);
	}

	/**
	 * returns true is s is needed to calculate the output pins of the circuit.
	 * @param s the tested statement
	 * @return true is s is needed to calculate the output pins of the circuit.
	 */
	public static boolean isUsed(Statement s) {
		return usedStatements.containsKey(s);
	}

	//~ Static fields/initializers ---------------------------------------------

	private static HashMap gates = new HashMap();

	/*
	 * This data structure holds all the statements and their
	 * expressions (which expression is being assigned and using
	 * which expressions).
	 * At the second phase of the optimization algorithm, the optimizer
	 * will remove all the statements that are being used in order to
	 * compute the programs output. Afterwards the statement that will be left
	 * in usage table can be removed from the program.
	 */
	private static HashMap usedStatements = new HashMap();
}
