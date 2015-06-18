// OutputWriter.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;

import java.io.PrintWriter;

import java.util.Vector;


/**
 * The OutputWriter represents the classes that produce the compiler's output.
 */
interface OutputWriter {
	//~ Methods ----------------------------------------------------------------

	/**
	 * Prints this Object into the circuit.
	 * @param circuit the circuit output file.
	 */
	abstract void toCircuit(PrintWriter circuit);

	//~ Static fields/initializers ---------------------------------------------

	static Vector inputFormat = new Vector();
}
