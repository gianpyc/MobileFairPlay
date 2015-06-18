// Program.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;

import java.io.PrintWriter;

import java.util.Vector;


/**
 * A class that represents the program. It holds all the inforamtion needed
 * for the program.
 */
public class Program {
	//~ Instance fields --------------------------------------------------------

	String name;

	//~ Constructors -----------------------------------------------------------

	public Program(String name) {
		this.name     = name;
		functions     = new Vector();
		resetCounter();
	}

	//~ Methods ----------------------------------------------------------------

	/**
	 * Adds a new function to this program.
	 * @param function the new function to add.
	 */
	public void addFunction(Function function) {
		functions.add(function);

		if (function.getName().equals("output")) {
			Program.output = function;
		}
	}

	/**
	 * Returns a string representation of the object.
	 * @return a string representation of the object.
	 */
	public String toString() {
		String str = new String("program " + name + " {\n");

		str += output.toString();

		str += "}\n";

		return str;
	}

	/**
	 * Transfroms multibit statements in the program into
	 * single bit statements.
	 * NOTE: this class is the only class that implements multi2SingleBit()
	 * methos without implementing Multi2SingleBit inteface.
	 */
	public void multi2SingleBit() {
		System.out.println("Performing multi-to-single-bit transformation.");

		// reset the circuitLineCounter 
		resetCounter();

		// add inpus statements
		output.addInputStatements();

		// replace all variables and parameters with singlebitVariables
		Function.getVars().multi2SingleBit();

		output.multi2SingleBit(null);

		System.out.println("Transformation finished.");
	}

	/**
	 * Returns a string representing this object as it appear at the
	 * output circuit.
	 * @return a string representing this object as it appear at the
	 * output circuit.
	 */
	public void toCircuit(PrintWriter circuit, boolean opt) {
		System.out.println("Writing to circuit file.");

		// assgn new line numbers to the program (in case it was optimized)
		resetCounter();

		// start with the two constant gate false and true.
		if (! opt) {
			circuit.println("0 gate arity 0 table [0] inputs [] // false");
			circuit.println("1 gate arity 0 table [1] inputs [] //true");
			circuitLineCounter = 2;
		}

		output.toCircuit(circuit);

		System.out.println("Completed.");
	}

	/**
	 * Returns a current line number in the program. This method is called the the
	 * statements that a line number is to be assigned to them.
	 * @return  a current line number in the program.
	 */
	public static int getLineNumber() {
		return circuitLineCounter++;
	}

	/**
	 * Returns a String representing this object as it should appear in the format file.
	 * @return a String representing this object as it should appear in the format file.
	 */
	public String toFormat() {
		System.out.println("Writing to format file.");

		String str = output.toFormat();

		System.out.println("Completed.");

		return str;
	}

	/**
	 * Optimizes the program.
	 */
	public void optimize() {
		resetCounter();

		System.out.println("Program Optimization: Phase I.");

		// Phase I
		output.optimizePhaseI();

		System.out.println("Program Optimization: Phase II.");

		// Phase II
		output.optimizePhaseII(null);

		System.out.println("Optimization finished.");
	}

	/**
	 * Unique vars transformations.
	 */
	public void uniqueVars() {
		System.out.println("Unique vars transformations.");

		/*for (int i=0; i < functions.size(); i++)
		  ((Function)(functions.elementAt(i))).uniqueVars();*/
		output.uniqueVars();
		System.out.println("Unique vars transformations finished.");
	}

	/**
	 * resets the line counter
	 */
	private void resetCounter() {
		circuitLineCounter = 0;
	}

	public static Function functionFromName(String name) {
		for (int i = 0; i < functions.size(); i++) {
			Function f = (Function) functions.elementAt(i);

			if (f.getName().equals(name)) {
				return f;
			}
		}

		return null;
	}

	//~ Static fields/initializers ---------------------------------------------

	/*
	 * Holds the functions defined in the program
	 */
	private static Vector functions;

	/**
	 * Hold a refernece to the output function
	 */
	public static Function output;
	private static int     circuitLineCounter; //0 and 1 are reserved for 
}
