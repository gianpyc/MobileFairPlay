// Optimize.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;

import java.util.Vector;


/**
 * Optimize inteface represents classes that has a role in the optimizing
 */
interface Optimize {
	//~ Methods ----------------------------------------------------------------

	/**
	 * run Optimiztion phase I on this object
	 */
	public void optimizePhaseI();

	/**
	 * Execute optimization's phase II on this object
	 */
	public void optimizePhaseII(Vector newBody);

	/**
	 * uses this object to build a list of statements needed to calculate
	 * the output pins of the circuit.
	 */
	public void buildUsedStatementsHash();
}
