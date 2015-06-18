// Function.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;

import java.io.PrintWriter;

import java.util.HashMap;
import java.util.Vector;


/**
 * A class for representing a function that can be defined
 * in the program.
 */
public class Function implements OutputWriter, Optimize, Multi2SingleBit {
	//~ Instance fields --------------------------------------------------------

	//data members

	/*
	 * Holds the name of the function.
	 */
	private String name;

	/*
	 * Holds the returned type of the function.
	 */
	private Type returnType;

	/*
	 * Holds the body of this function (statements to be carried out).
	 */
	private Vector body;

	/*
	 * local parameters used only when a call is made
	 */
	public Vector parameters;

	/*
	 * holds the LvalExpression returned from this functin.
	 *
	 */
	public LvalExpression functionResult;

	//~ Constructors -----------------------------------------------------------

	/**
	     * Constructs a new Function from a given name, returned type
	     * @param name the name of this function.
	     * @param returnType the type of this function's returned value.
	     */
	public Function(String name, Type returnType) {
		this.name           = name;
		this.returnType     = returnType;
		currentFunction     = this;

		this.body           = new Vector();
		this.parameters     = new Vector();

		// add the function as a local var
		boolean isOutput = false;

		if (name.equals("output")) {
			isOutput = true;
		}

		vars.addVar( /*currentFunction.getName()*/
		name + "$" + name, returnType, 
		/*isParameter=*/ false, /*isOutput=*/
		isOutput);

		functionResult = vars.getVar(name + "$" + name);
	}

	//~ Methods ----------------------------------------------------------------

	/**
	 * Returns all variables and their references in all scopes.
	 * @return UniqueVariables all variables and their references in all scopes.
	 */
	public static UniqueVariables getVars() {
		return vars;
	}

	/**
	* Adds a parameter to this function.
	* Note that the function adds the LvalExpression for this parameter.
	* @param name the name of the new parameter
	* @param type the type of the new parameter
	*/
	public void addParameter(String name, Type type) {
		vars.addVar(this.name + "$" + name, type, 
		/*isParameter=*/ true, /*isOutput=*/
		            false);

		parameters.add(vars.getVar(this.name + "$" + name));
	}

	/**
	 * Adds a local variable that was defined to this function.
	 * Note that the function adds the LvalExpression for this parameter.
	 * @param name the name of the new local variable
	 * @param type the type of the new local variable
	 */
	public static void addVar(String name, Type type) {
		vars.addVar(currentFunction.getName() + "$" + name, type, 
		/*isParameter=*/ false, /*isOutput=*/
		            false);
	}

	/**
	* Adds a local variable that was defined to this functioni, from a
	* given LvalExpression.
	* @param exp the given expression.
	*/
	public static void addVar(LvalExpression exp) {
		vars.addVar(exp.getName(), exp.getType(), 
		/*isParameter=*/ false, /*isOutput=*/
		            exp.isOutput());

		// if old lhs is output pin then set it to be non output
		exp.notOutput();
	}

	/**
	     * Adds a temporary local varivable as single bit LvalExpression
	     * from a given varname and type and returns the LvalExpression that
	     * hold the whole (original) variable. NOTE: this method is used during
	     * multi2singlebit trasformation where temp. vars are needed.
	     * @param name the name of the temp. variable.
	     * @param type the type of the temp. variable.
	     * @return the LvalExpression that represents the variable.
	     */
	public static LvalExpression addTempLocalVar(String name, Type type) {
		LvalExpression lvalExp = vars.getVar(name);

		if (lvalExp == null) {
			Lvalue lvalue = new VarLvalue(new Variable(name, type), false);
			vars.add(lvalue, false);
			lvalExp = vars.getVar(name);

			// add only the bit variables
			Vector derivedLvalues = lvalue.getDerivedLvalues();

			for (int i = 0; i < derivedLvalues.size(); i++) {
				lvalue = (Lvalue) derivedLvalues.elementAt(i);

				if (! (lvalue.hasDerives())) {
					int lvalSize = lvalue.size();

					// add bits only
					for (int j = 0; j < lvalSize; j++) {
						vars.add(new BitLvalue(lvalue, j), false);
					}
				}
			}
		}

		return lvalExp;
	}

	/**
	     * Returns a string representation of the object.
	      * @return a string representation of the object.
	     */
	public String toString() {
		String str = new String("function " + returnType + " " + name + "\n");

		for (int i = 0; i < body.size(); i++)
			str += ((Statement) (body.elementAt(i))).toString();

		return str;
	}

	/**
	     * Return the name of the function.
	     * @return the name of the function.
	     */
	public String getName() {
		return name;
	}

	/**
	       * Adds a statement to this function.
	       * @param statement the new statement.
	       */
	public void addStatement(Statement statement) {
		body.add(statement);
	}

	/**
	 * Adds a statement to this function.
	 * @param statement the new statement.
	 */
	public void addStatements(Vector statements) {
		body.addAll(statements);
	}

	/**
	 * Returns the LvalExpression from a given
	 * parameter or a local variable name. If the given
	 * name does not exists, the returned value will be null.
	 * @param name the function, parameter or a local variable name.
	 * @return the LvalExpression representing the given name or
	 * null if the given name does not exists.
	 */
	public static LvalExpression getVar(String name) {
		// in case the name represents the name of a localvar 
		// parameter or the function name OR
		// the name does not exists which is this case the return value
		// will be null
		return (LvalExpression) (vars.getVar(currentFunction.getName() + "$" +
		                                     name));
	}

	/**
	 * this method is used to get the last referance existing
	 * (unique var)
	 */
	public static LvalExpression getVar(LvalExpression lval) {
		// in case the name represents the name of a localvara
		// parameter or the function name OR
		// the name does not exists which is this case the return value
		// will be null
		return (LvalExpression) (vars.getVar(lval.getName()));
	}

	/**
	 * Returns the bit LvalExpression from a given Lvalexpression and the bit
	 * number.
	 * @param exp the Lvalexpression
	 * @param i the bit number to be returned.
	 * @return LvalExpression holding the ith bit of exp.
	 */
	public static LvalExpression getVarBitAt(LvalExpression exp, int i) {
		String expName;
		Type   type = exp.getType();

		if (type.hasDerives()) {
			expName = ((ParentType) type).getNameAt(exp.getName(), i);
		} else {
			if (exp.size() <= i) {
				// sign expantion
				i = exp.size() - 1;
			}

			expName = exp.getName() + "$" + i;
		}

		// in case the name represents the name of a localvar parameter
		// or the function name OR
		// the name does not exists which is this case the return value
		// will be null
		return (LvalExpression) (vars.getVar(expName));
	}

	/**
	     * Transfroms multibit statements in the function into single
	     * bit statements.
	     * @param obj not needed (null).
	     * @return null.
	     */
	public BlockStatement multi2SingleBit(Object obj) {
		currentFunction = this;

		Vector oldBody = body;
		body = new Vector();

		for (int i = 0; i < oldBody.size(); i++) {
			Statement s = ((Statement) (oldBody.elementAt(i)));
			body.add(((Multi2SingleBit) s).multi2SingleBit(null));
		}

		oldBody.clear();

		return null;
	}

	/*
	     * Adds the input statements into the output function.
	     */
	public void addInputStatements() {
		Vector parameters = vars.getParameters();

		// run over all the parameters LvaluesExpressions
		for (int i = 0; i < parameters.size(); i++) {
			LvalExpression parameterLvalExp =
				(LvalExpression) (parameters.elementAt(i));
			String         parameterName = parameterLvalExp.getName();

			if (! parameterName.startsWith(name + "$")) {
				continue;
			}

			// insert input statement at the start of the function
			InputStatement is = new InputStatement(parameterLvalExp);
			body.add(0, is);
		}

		//for 
	}

	// end method

	/**
	 * Prints this AssignmentStatement into the circuit.
	 *  @param circuit the circuit output file.
	 */
	public void toCircuit(PrintWriter circuit) {
		currentFunction = this;

		for (int i = 0; i < body.size(); i++)
			((OutputWriter) (body.elementAt(i))).toCircuit(circuit);
	}

	/**
	 * Returns a String representing this object as it should
	 * appear in the format file.
	 * @return a String representing this object as it should appear in the
	 * format file.
	 */
	public String toFormat() {
		currentFunction = this;

		String str = new String();

		// print the inputs form the OutputWriter
		for (int i = 0; i < OutputWriter.inputFormat.size(); i++) {
			Vector inputVector =
				(Vector) (OutputWriter.inputFormat.elementAt(i));
			str += ((String) (inputVector.elementAt(0)) + " input integer \"" +
			(String) (inputVector.elementAt(1)) + "\" [ ");

			for (int j = 2; j < inputVector.size(); j++) {
				InputStatement is = (InputStatement) (inputVector.elementAt(j));
				str += (is.getOutputLine() + " ");
			}

			str += "]\n";
		}

		// add output statements
		StructType output = (StructType) (Type.fromName("Output"));
		str += output.toFormat("output", this);

		return str;
	}

	/**
	 * Optimizes this function - phase I.
	 * Run phase I on all the functions statements.
	 */
	public void optimizePhaseI() {
		currentFunction = this;

		for (int i = 0; i < body.size(); i++)

			// Block/Assignment/Input Statement
			((Optimize) (body.elementAt(i))).optimizePhaseI();
	}

	/**
	 * creates a list of the needed statements in this functions and removes
	 * all unneeded statements according to this list.
	 * @param newBody newBody is always null (needed for other classes).
	 */
	public void optimizePhaseII(Vector newBody) {
		currentFunction = this;

		buildUsedStatementsHash();

		// Build new body that uses only the used statements
		Vector oldBody = body;
		body = new Vector();

		for (int i = 0; i < oldBody.size(); i++) {
			Optimize s = ((Optimize) (oldBody.elementAt(i)));
			s.optimizePhaseII(body);
		}

		oldBody.clear();
	}

	/**
	 * create the list of all the needed statements to calculate the circuit.
	 * this list is used in the optimization.
	 */
	public void buildUsedStatementsHash() {
		for (int i = body.size() - 1; i >= 0; i--)

			// Block/Assignment/Input Statement
			((Optimize) (body.elementAt(i))).buildUsedStatementsHash();
	}

	/**
	 * Unique vars transformations.
	 */
	public void uniqueVars() {
		currentFunction = this;

		for (int i = 0; i < body.size(); i++)
			body.setElementAt(((Statement) (body.elementAt(i))).uniqueVars(), i);
	}

	/**
	 * Returns the size of the value returned by the function in bits.
	 * @return the size of the value returned by the function in bits.
	 */
	public int size() {
		return returnType.size();
	}

	/**
	 * Called when begining a new scope (for example: if, for)
	 */
	public static void pushScope() {
		vars.pushScope();
	}

	/**
	 * Called when ending the current scope
	 * @return HashMap that holds the variables of the scope
	 * and their references
	 */
	public static HashMap popScope() {
		return vars.popScope();
	}

	/**
	     * returns the arguments of this function.
	     * @return the arguments of this function.
	     */
	public Vector getArguments() {
		return parameters;
	}

	/**
	     * returns the body of this function
	     * @return the body of this function
	     */
	public Vector getBody() {
		return body;
	}

	//~ Static fields/initializers ---------------------------------------------

	/*
	 * Holds all variables and their references in all scopes.
	 */
	private static UniqueVariables vars = new UniqueVariables();

	/*
	 * Holds the current function that curetly being parsed.
	 */
	public static Function currentFunction;
}
