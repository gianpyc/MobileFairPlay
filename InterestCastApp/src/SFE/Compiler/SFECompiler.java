// SFECompiler.java.
// Copyright (C) 2004 Naom Nisan, Ziv Balshai, Amir Levy.
// See full copyright license terms in file ../GPL.txt

package SFE.Compiler;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.text.ParseException;

import java.util.EmptyStackException;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;


/**
 * The SFECompiler class takes an input stream and checks if it is compatible
 * with the predefined language.
 * It uses the class Tokenizer that gives tokens and their values.
 */
public class SFECompiler {
	//~ Instance fields --------------------------------------------------------

	/*
	 * Gives tokens and their values
	 */
	private Tokenizer tokenizer;

	//~ Constructors -----------------------------------------------------------

	/**
	 * Creates a tokenizer that parses the given stream.
	 * @param file a FileReader object providing the input stream.
	 */
	public SFECompiler(FileReader file) {
		tokenizer = new Tokenizer(file);
	}

	//~ Methods ----------------------------------------------------------------

	/*
	 * Advances one token
	 * @param error error message
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private void advance(String error) throws IOException, ParseException {
		if (! tokenizer.hasMoreTokens()) {
			throw new ParseException(error, tokenizer.lineNumber());
		}

		tokenizer.advance();
	}

	/**
	 * Gets the next token from the stream and checks if it is from the
	 * expected type.
	 * @param tokenType the expected type.
	 * @param error string of the error message which will be send with ParseException.
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private void getNextToken(int tokenType, String error)
	                   throws ParseException, IOException
	{
		if (tokenizer.hasMoreTokens()) {
			tokenizer.advance();

			//checks if its the expected type
			if (tokenizer.tokenType() == tokenType) {
				return;
			}
		}

		throw new ParseException(error, tokenizer.lineNumber());
	}

	/**
	 * Gets the next token from the stream, checks first if it is a keyword and
	 * then if its the the expected keyword.
	 * @param keywordType the expected keyword type.
	 * @param error string of the error message which will be send with ParseException.
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private void getKeyword(int keywordType, String error)
	                 throws ParseException, IOException
	{
		getNextToken(Tokenizer.KEYWORD, error);

		//checks if its the expected keyword
		if (tokenizer.keyword() != keywordType) {
			throw new ParseException(error, tokenizer.lineNumber());
		}
	}

	/**
	 * Gets the next token from the stream, checks first if it is a symbol and
	 * then if its the the expected symbol.
	 * @param symbol the expected symbol.
	 * @param error string of the error message which will be send with ParseException.
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private void getSymbol(char symbol, String error)
	                throws ParseException, IOException
	{
		getNextToken(Tokenizer.SYMBOL, error);

		if (tokenizer.symbol() != symbol) {
			throw new ParseException(error, tokenizer.lineNumber());
		}
	}

	/**
	 * Compiles the const expression:
	 *        (&lt;const&gt; | &lt;number&gt;) ((+ | -) (&lt;const&gt; | &lt;number&gt;))*
	 * @return int the const value
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private int compileConstValue() throws ParseException, IOException {
		int  val          = 0;
		int  tmp;
		char lastOperator = '+';

		do {
			switch (tokenizer.tokenType()) {
				case Tokenizer.IDENTIFIER:

					ConstExpression constExpr =
						Consts.fromName(tokenizer.getIdentifier());
					tmp = constExpr.value();

					break;

				case Tokenizer.INT_CONST:
					tmp = tokenizer.intVal();

					break;

				default:
					throw new ParseException("const or number is expected" +
					                         tokenizer.symbol(),
					                         tokenizer.lineNumber());
			}

			switch (lastOperator) {
				case '+':
					val += tmp;

					break;

				case '-':
					val -= tmp;

					break;
			}

			advance("program not ended");

			if ((tokenizer.tokenType() != Tokenizer.SYMBOL) ||
				    ((tokenizer.symbol() != '+') &&
				    (tokenizer.symbol() != '-'))) {
				break;
			}

			lastOperator = tokenizer.symbol();
			advance("program not ended");
		} while (true);

		return val;
	}

	/**
	 * Compiles the all program:
	 *        program &lt;program-name&gt; {
	 *                &lt;type declarations&gt;
	 *                &lt;function declarations&gt;
	 *        }
	 * @return Program data structure that holds all the declarations and statements
	 *                        of the program
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	public Program compileProgram() throws ParseException, IOException {
		// program
		getKeyword(Tokenizer.KW_PROGRAM, "program is excepted");

		// <program-name>
		getNextToken(Tokenizer.IDENTIFIER,
		             "program name is excepted after program");

		Program program = new Program(tokenizer.getIdentifier());

		// {
		getSymbol('{',
		          "{ is excepted after program name " +
		          tokenizer.getIdentifier());

		advance("program not ended");

		// <type declarations>
		compileTypeDeclarations(program);

		// <function declarations>
		compileFunctionDeclarations(program);

		// }
		if ((tokenizer.tokenType() != Tokenizer.SYMBOL) ||
			    (tokenizer.symbol() != '}')) {
			throw new ParseException("} is excepted in the end of program",
			                         tokenizer.lineNumber());
		}

		return program;
	}

	/**
	 * Compiles the type declarations:
	 *        ((const &lt;const-name&gt; = &lt;const-value&gt;;) | (type &lt;type-name&gt; = &lt;data-type&gt;;))*
	 * @param Program data structure that holds all the declarations and statements
	 *                        of the program
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private void compileTypeDeclarations(Program program)
	                              throws ParseException, IOException
	{
		while (tokenizer.tokenType() == Tokenizer.KEYWORD) {
			// type or const
			switch (tokenizer.keyword()) {
				case Tokenizer.KW_TYPE:
					compileType();

					break;

				case Tokenizer.KW_CONST:
					compileConst();

					break;

				default:
					return;
			}

			// ;
			if ((tokenizer.tokenType() != Tokenizer.SYMBOL) ||
				    (tokenizer.symbol() != ';')) {
				throw new ParseException("; is excepted", tokenizer.lineNumber());
			}

			advance("program not ended");
		}
	}

	/**
	 * Compiles the type declaration:
	 * &lt;type-name&gt; = &lt;data-type&gt;;
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private void compileType() throws ParseException, IOException {
		// <type-name>
		getNextToken(Tokenizer.IDENTIFIER, "type name is excepted after type");

		String typeName = tokenizer.getIdentifier();

		// =
		getSymbol('=', "= is excepted after type name " + typeName);
		advance("type name is excepted");

		// <data-type>
		Type.defineName(typeName, compileDataType());
	}

	/**
	 * Compiles the const declaration:
	 * &lt;const-name&gt; = &lt;const-value&gt;;
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private void compileConst() throws ParseException, IOException {
		// <const-name>
		getNextToken(Tokenizer.IDENTIFIER, "const name is excepted after const");

		String constName = tokenizer.getIdentifier();

		// =
		getSymbol('=',
		          "= is excepted after const name " +
		          tokenizer.getIdentifier());

		advance("program not ended");

		// <const-value>
		Consts.defineName(constName, compileConstValue());
	}

	/**
	 * Compiles the data type:
	 *        (Int&lt;bits&gt; | Boolean | &lt;type-name&gt;) (&lt;array&gt;)*
	 * @return Type one of the data structure types
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private Type compileDataType() throws ParseException, IOException {
		Type newType;

		// (Int or Boolean) or <type-name>
		switch (tokenizer.tokenType()) {
			case Tokenizer.KEYWORD:
				newType = compileKnownType();

				break;

			case Tokenizer.IDENTIFIER:
				newType = Type.fromName(tokenizer.getIdentifier());

				if (newType == null) {
					throw new ParseException("Unknown type " +
					                         tokenizer.getIdentifier(),
					                         tokenizer.lineNumber());
				}

				advance("Program not ended");

				break;

			default:
				throw new ParseException("type name is excepted",
				                         tokenizer.lineNumber());
		}

		// <array>
		return compileArray(newType);
	}

	/**
	 * Compiles the known types:
	 *                Int, Boolean, StructType, Enum
	 * @return Type one of the data structure types
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private Type compileKnownType() throws ParseException, IOException {
		Type type;

		switch (tokenizer.keyword()) {
			case Tokenizer.KW_INT:

				// <bits>
				getSymbol('<', "< is excepted after Int");
				advance("Program not ended");

				int bits = compileConstValue(); //compileBits();

				if ((tokenizer.tokenType() != Tokenizer.SYMBOL) ||
					    (tokenizer.symbol() != '>')) {
					throw new ParseException("> is excepted at the end of Int declaration",
					                         tokenizer.lineNumber());
				}

				advance("Program not ended");
				type = new IntType(bits);

				break;

			case Tokenizer.KW_BOOLEAN:
				type = new BooleanType();
				advance("Program not ended");

				break;

			case Tokenizer.KW_STRUCT:

				// {
				getSymbol('{', "{ is excepted after StructType");
				type = new StructType();
				advance("Program not ended");

				// <fields>
				compileStructFields((StructType) type);

				// }
				if ((tokenizer.tokenType() != Tokenizer.SYMBOL) ||
					    (tokenizer.symbol() != '}')) {
					throw new ParseException("} is excepted in the end of struct fields",
					                         tokenizer.lineNumber());
				}

				advance("Program not ended");

				break;

			case Tokenizer.KW_ENUM:

				// {
				getSymbol('{', "{ is excepted after StructType");

				int index = 0;
				advance("Program not ended");

				while ((tokenizer.tokenType() != Tokenizer.SYMBOL) ||
					       ! ((tokenizer.symbol() == '}') && (index > 0))) {
					if (tokenizer.tokenType() != Tokenizer.IDENTIFIER) {
						throw new ParseException("identifier is excepted in enum",
						                         tokenizer.lineNumber());
					}

					// <const-value>
					Consts.defineName(tokenizer.getIdentifier(), index);
					index++;
					advance("Program not ended");

					if ((tokenizer.tokenType() != Tokenizer.SYMBOL) ||
						    ! ((tokenizer.symbol() == '}') ||
						    (tokenizer.symbol() == ','))) {
						throw new ParseException("} or , is excepted in enum",
						                         tokenizer.lineNumber());
					}

					if (tokenizer.symbol() == ',') {
						advance("Program not ended");
					}
				}

				//calculate number of bits, +1 for rounding
				type = new IntType((int) (IntConstant.log2(index) + 1));
				advance("Program not ended");

				break;

			default:
				throw new ParseException(tokenizer.getKeyword() +
				                         " is not a supported type",
				                         tokenizer.lineNumber());
		}

		return type;
	}

	/**
	 * Compiles the array syntax:
	 *        ('[' &lt;const-value&gt; ']')*
	 * @param base the base type of the array
	 * @return Type array type
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private Type compileArray(Type base) throws ParseException, IOException {
		// [
		while ((tokenizer.tokenType() == Tokenizer.SYMBOL) &&
			       (tokenizer.symbol() == '[')) {
			advance("program not ended");

			int length = compileConstValue();

			base = new ArrayType(base, length);

			// ]
			if ((tokenizer.tokenType() != Tokenizer.SYMBOL) ||
				    (tokenizer.symbol() != ']')) {
				throw new ParseException("] is excepted", tokenizer.lineNumber());
			}

			advance("program not ended");
		}

		// the next token after array
		return base;
	}

	/**
	 * Compiles the function declarations:
	 *        (
	 *                function &lt;data-type&gt; &lt;function-name&gt; ( &lt;arguments-list&gt; ) {
	 *                        &lt;var declarations&gt;
	 *                        &lt;function body&gt;
	 *                }
	 *        )*
	 * @param Program data structure that holds all the declarations and statements
	 *                        of the program
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private void compileFunctionDeclarations(Program program)
	                                  throws ParseException, IOException
	{
		// function
		while ((tokenizer.tokenType() == Tokenizer.KEYWORD) &&
			       (tokenizer.keyword() == Tokenizer.KW_FUNCTION)) {
			advance("program not ended");

			// <data-type>
			Type returnType = compileDataType();

			// <function-name>
			if (tokenizer.tokenType() != Tokenizer.IDENTIFIER) {
				throw new ParseException("function name is excepted after type",
				                         tokenizer.lineNumber());
			}

			new Function(tokenizer.getIdentifier(), returnType);

			// (
			getSymbol('(',
			          "( is excepted after function name " +
			          tokenizer.getIdentifier());

			advance("program not ended");

			// <arguments-list>
			compileArgumentsList();

			// )
			if ((tokenizer.tokenType() != Tokenizer.SYMBOL) ||
				    (tokenizer.symbol() != ')')) {
				throw new ParseException(") is excepted in the end of arguments",
				                         tokenizer.lineNumber());
			}

			// {	
			getSymbol('{', "{ is excepted after )");

			advance("program not ended");

			// <var declarations>
			compileVarDeclarations();

			// <function body>
			compileFunctionBody();

			// }
			if ((tokenizer.tokenType() != Tokenizer.SYMBOL) ||
				    (tokenizer.symbol() != '}')) {
				throw new ParseException("} is excepted at end of function",
				                         tokenizer.lineNumber());
			}

			advance("program not ended");

			program.addFunction(Function.currentFunction);
		}
	}

	/**
	 * Compiles the arguments list:
	 *        ( &lt;data-type&gt; &lt;argument-name&gt; (,&lt;data-type&gt; &lt;argument-name&gt;)* )?
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private void compileArgumentsList() throws ParseException, IOException {
		//empty arguments list
		if ((tokenizer.tokenType() == Tokenizer.SYMBOL) &&
			    (tokenizer.symbol() == ')')) {
			return;
		}

		do {
			// <data-type>		
			Type type = compileDataType();

			// <argument-name>
			if (tokenizer.tokenType() != Tokenizer.IDENTIFIER) {
				throw new ParseException("argument name is excepted after type",
				                         tokenizer.lineNumber());
			}

			Function.currentFunction.addParameter(tokenizer.getIdentifier(),
			                                      type);

			advance("program not ended");

			// ,
			if ((tokenizer.tokenType() != Tokenizer.SYMBOL) ||
				    (tokenizer.symbol() != ',')) {
				break;
			}

			advance("program not ended");
		} while (true);
	}

	/**
	 * Compiles the fields in StructType:
	 *        ( &lt;data-type&gt; &lt;field-name&gt; (,&lt;data-type&gt; &lt;field-name&gt;)* )?
	 * @param StructType data structure that holds fields
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private void compileStructFields(StructType structType)
	                          throws ParseException, IOException
	{
		do {
			// <data-type>		
			Type type = compileDataType();

			// <field-name>
			if (tokenizer.tokenType() != Tokenizer.IDENTIFIER) {
				throw new ParseException("field name is excepted after type",
				                         tokenizer.lineNumber());
			}

			structType.addField(tokenizer.getIdentifier(), type);

			advance("program not ended");

			// ,
			if ((tokenizer.tokenType() != Tokenizer.SYMBOL) ||
				    (tokenizer.symbol() != ',')) {
				break;
			}

			advance("program not ended");
		} while (true);
	}

	/**
	 * Compiles the variables declarations:
	 *        ( var &lt;variables-list&gt; )*
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private void compileVarDeclarations() throws ParseException, IOException {
		//empty arguments list
		while ((tokenizer.tokenType() == Tokenizer.KEYWORD) &&
			       (tokenizer.keyword() == Tokenizer.KW_VAR)) {
			advance("program not ended");
			compileVariablesList();
		}
	}

	/**
	 * Compiles the variables list:
	 *        &lt;data-type&gt; &lt;variable-name&gt; (,&lt;variable-name&gt;)* ;
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private void compileVariablesList() throws ParseException, IOException {
		// <data-type>		
		Type type = compileDataType();

		do {
			// <variable-name>
			if (tokenizer.tokenType() != Tokenizer.IDENTIFIER) {
				throw new ParseException("variable name is excepted after type",
				                         tokenizer.lineNumber());
			}

			Function.addVar(tokenizer.getIdentifier(), type);

			advance("program not ended");

			if ((tokenizer.tokenType() != Tokenizer.SYMBOL) ||
				    ! ((tokenizer.symbol() == ';') ||
				    (tokenizer.symbol() == ','))) {
				throw new ParseException(", or ; is expected",
				                         tokenizer.lineNumber());
			}

			if (tokenizer.symbol() == ';') {
				break;
			}

			advance("program not ended");
		} while (true);

		advance("program not ended");
	}

	/**
	 * Compiles the function body:
	 *        ( &lt;statement&gt; )*
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private void compileFunctionBody() throws ParseException, IOException {
		while ((tokenizer.tokenType() != Tokenizer.SYMBOL) ||
			       (tokenizer.symbol() != '}'))
			Function.currentFunction.addStatement(compileStatement());
	}

	/**
	 * Compiles the statement:
	 *        &lt;if-statement&gt; | &lt;for-statement&gt; | &lt;var-name&gt; '=' &lt;expression&gt;; | &lt;block&gt;
	 * @return Statement data structure that holds statement
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private Statement compileStatement() throws ParseException, IOException {
		BlockStatement statement = null;

		switch (tokenizer.tokenType()) {
			case Tokenizer.IDENTIFIER:

				//Assignment statement
				Vector expressions = new Vector();
				Vector lengths = new Vector();

				// <var-name>
				statement = new BlockStatement();

				String varName =
					compileLHS(null, expressions, lengths, statement);

				// =
				if ((tokenizer.tokenType() != Tokenizer.SYMBOL) ||
					    (tokenizer.symbol() != '=')) {
					throw new ParseException("= is excepted after variable",
					                         tokenizer.lineNumber());
				}

				advance("program not ended");

				// <expression>
				Expression expr = compileExpression(false, statement);

				//must be an OperationExpression
				if (! (expr instanceof OperationExpression)) {
					expr =
						new UnaryOpExpression(new PrimitiveOperator(PrimitiveOperator.ID_OP),
						                      expr);
				}

				if (expressions.isEmpty()) {
					statement.addStatement(new AssignmentStatement(Function.getVar(varName),
					                                               (OperationExpression) expr));
				} else {
					statement.addStatement(arrayStatment(varName, expressions,
					                                     lengths,
					                                     (OperationExpression) expr));
				}

				// ;
				if ((tokenizer.tokenType() != Tokenizer.SYMBOL) ||
					    (tokenizer.symbol() != ';')) {
					throw new ParseException("; is expected in end of command",
					                         tokenizer.lineNumber());
				}

				advance("program not ended");

				break;

			case Tokenizer.KEYWORD:

				//if or for
				switch (tokenizer.keyword()) {
					case Tokenizer.KW_IF:
						statement = compileIf();

						break;

					case Tokenizer.KW_FOR:
						statement = compileFor();

						break;

					default:
						throw new ParseException(tokenizer.getKeyword() +
						                         " is not a supported command",
						                         tokenizer.lineNumber());
				}

				break;

			case Tokenizer.SYMBOL:

				if (tokenizer.symbol() != '{') {
					throw new ParseException("unexpected symbol " +
					                         tokenizer.symbol(),
					                         tokenizer.lineNumber());
				}

				advance("program not ended");

				// <block>	
				statement = new BlockStatement();

				while ((tokenizer.tokenType() != Tokenizer.SYMBOL) ||
					       (tokenizer.symbol() != '}')) {
					((BlockStatement) statement).addStatement(compileStatement());
				}

				advance("program not ended");

				break;

			default:
				throw new ParseException("identifier or if or for is expected",
				                         tokenizer.lineNumber());
		}

		return statement;
	}

	/**
	 * Merges splitted array variable into statement(left side variable).
	 * @param varName variable name, $ is the splitted places
	 * @param expressions splitted expressions
	 * @param lengths limit of each index in the array
	 * @param rhs right side expression
	 * @return Statement data structure that holds statement
	 */
	private Statement arrayStatment(String varName, Vector expressions,
	                                Vector lengths, OperationExpression rhs) {
		int[] indexes = new int[lengths.size()];

		for (int i = 0; i < lengths.size(); i++)
			indexes[i] = 0;

		BlockStatement statement      = new BlockStatement();
		String[]       varNameSplited = varName.split("\\$");

		int            lengthOfLast =
			((Integer) lengths.elementAt(lengths.size() - 1)).intValue();

		do {
			String str = new String();

			for (int i = 0; i < indexes.length; i++)
				str += (varNameSplited[i] + indexes[i]);

			if (varNameSplited.length > indexes.length) {
				str += varNameSplited[varNameSplited.length - 1];
			}

			BinaryOpExpression op =
				new BinaryOpExpression(new EqualOperator(),
				                       new IntConstant(indexes[0]),
				                       (Expression) expressions.elementAt(0));

			for (int i = 1; i < indexes.length; i++) {
				BinaryOpExpression op2 =
					new BinaryOpExpression(new EqualOperator(),
					                       new IntConstant(indexes[i]),
					                       (Expression) expressions.elementAt(i));
				op = new BinaryOpExpression(new PrimitiveOperator(PrimitiveOperator.AND_OP),
					                        op, op2);
			}

			statement.addStatement(new IfStatement(op,
			                                       new AssignmentStatement(Function.getVar(str),
			                                                               rhs),
			                                       null));
		} while (advanceIndexes(indexes, lengths));

		return statement;
	}

	/**
	 * Advances the indexes of an array variable.
	 * @param indexes indeces of the array variable.
	 * @param lengths limit of each index in the array
	 * @return true if all indeces get to the limit.
	 */
	private boolean advanceIndexes(int[] indexes, Vector lengths) {
		int i = indexes.length - 1;

		while (i >= 0) {
			indexes[i] =
				(indexes[i] + 1) % ((Integer) lengths.elementAt(i)).intValue();

			if (indexes[i] == 0) {
				i--;
			} else {
				return true;
			}
		}

		return false;
	}

	/**
	 * Compiles for statement:
	 *        for '(' &lt;identifier&gt; '=' &lt;from-value&gt; to &lt;to-value&gt; ')'
	 *                        &lt;statement&gt;
	 * @return BlockStatement data structure that holds for statement
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private BlockStatement compileFor() throws ParseException, IOException {
		// (
		getSymbol('(', "( is excepted after for");

		// <identifier>
		getNextToken(Tokenizer.IDENTIFIER, "identifier is excepted after (");

		// <var-name>
		Vector         expressions = new Vector();
		Vector         lengths = new Vector();
		BlockStatement block   = new BlockStatement();
		String         varName = compileLHS(null, expressions, lengths, block);

		// =
		if ((tokenizer.tokenType() != Tokenizer.SYMBOL) ||
			    (tokenizer.symbol() != '=')) {
			throw new ParseException("= is excepted after variable",
			                         tokenizer.lineNumber());
		}

		advance("program not ended");

		//<from-value>
		int from = compileConstValue();

		// to
		if ((tokenizer.tokenType() != Tokenizer.KEYWORD) ||
			    (tokenizer.keyword() != Tokenizer.KW_TO)) {
			throw new ParseException("to is expected", tokenizer.lineNumber());
		}

		advance("program not ended");

		//<to-value>
		int to = compileConstValue();

		// )
		if ((tokenizer.tokenType() != Tokenizer.SYMBOL) ||
			    (tokenizer.symbol() != ')')) {
			throw new ParseException(") is expected", tokenizer.lineNumber());
		}

		advance("program not ended");

		Statement forBlock = compileStatement();

		//duplicating the for block to-from+1 times
		for (; from <= to; from++) {
			//index not array
			if (expressions.isEmpty()) {
				block.addStatement(new AssignmentStatement(Function.getVar(varName),
				                                           new UnaryOpExpression(new PrimitiveOperator(PrimitiveOperator.ID_OP),
				                                                                 new IntConstant(from))));
			} else {
				block.addStatement(arrayStatment(varName, expressions, lengths,
				                                 new UnaryOpExpression(new PrimitiveOperator(PrimitiveOperator.ID_OP),
				                                                       new IntConstant(from))));
			}

			block.addStatement(forBlock.duplicate());
		}

		return block;
	}

	/**
	 * Compiles if statement:
	 *        if '(' &lt;boolean-expr&gt; ')' &lt;statement&gt;
	 *                (else &lt;statement&gt;)?
	 * @return BlockStatement data structure that holds if statement
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private BlockStatement compileIf() throws ParseException, IOException {
		// (
		getSymbol('(', "( is excepted after if");

		BlockStatement block = new BlockStatement();

		//we don't need advance, the expression need to take (
		// <boolean-expr>
		Expression condition = compileExpression(false, block);

		Statement  thenBlock = compileStatement();
		Statement  elseBlock = null;

		// else
		if ((tokenizer.tokenType() == Tokenizer.KEYWORD) &&
			    (tokenizer.keyword() == Tokenizer.KW_ELSE)) {
			advance("program not ended");

			elseBlock = compileStatement();
		}

		block.addStatement(new IfStatement(condition, thenBlock, elseBlock));

		return block;
	}

	/**
	 * Compiles  left value:
	 *        &lt;identifier&gt; ( '[' &lt;expression&gt; ']' | '.'&lt;var-name&gt;)?
	 * @param varName variable name
	 * @param block holds a block of statements
	 * @return LvalExpression data structure of left value or null if identifier does not exists.
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private LvalExpression compileLvalExpression(String varName,
	                                             BlockStatement block)
	                                      throws ParseException, IOException
	{
		String fieldName = tokenizer.getIdentifier();

		if (varName == null) {
			varName = fieldName;
		} else {
			varName += ('.' + fieldName);
		}

		advance("program not ended");

		if (tokenizer.tokenType() == Tokenizer.SYMBOL) {
			switch (tokenizer.symbol()) {
				case '(':

					/* we have already passed a struct or an array - no '(' should be here!!! */
					if (varName.equals(fieldName)) {
						return null;
					}

					throw new ParseException("Unexpected '(' sign",
					                         tokenizer.lineNumber());

				case '[':

					while ((tokenizer.tokenType() == Tokenizer.SYMBOL) &&
						       (tokenizer.symbol() == '[')) {
						advance("program not ended");

						// <expression>
						Expression index = compileExpression(false, block);

						// ]
						if ((tokenizer.tokenType() == Tokenizer.SYMBOL) &&
							    (tokenizer.symbol() == ']')) {
							advance("program not ended");
						} else {
							throw new ParseException("] is expected",
							                         tokenizer.lineNumber());
						}

						LvalExpression base = Function.getVar(varName + "[$]");
						Function.addVar("tmp" + labelIndex, base.getType());

						LvalExpression lval =
							Function.getVar("tmp" + labelIndex);
						int            len =
							((ArrayType) Function.getVar(varName).getType()).getLength();

						for (int i = 0; i < len; i++) {
							BinaryOpExpression condition =
								new BinaryOpExpression(new EqualOperator(),
								                       new IntConstant(i),
								                       index.duplicate());
							AssignmentStatement ifBody =
								new AssignmentStatement(lval,
								                        new UnaryOpExpression(new PrimitiveOperator(PrimitiveOperator.ID_OP),
								                                              Function.getVar(varName +
								                                                              "[" +
								                                                              i +
								                                                              "]")));
							block.addStatement(new IfStatement(condition,
							                                   ifBody, null));
						}

						varName = "tmp" + labelIndex;
						labelIndex++;
					}

					if ((tokenizer.tokenType() == Tokenizer.SYMBOL) &&
						    (tokenizer.symbol() == '.')) {
						advance("program not ended");

						return compileLvalExpression(varName, block);
					}

					break;

				case '.':

					// <identifier>
					getNextToken(Tokenizer.IDENTIFIER,
					             " identifier is excepted after .");

					// <var-name>
					return compileLvalExpression(varName, block);
			}
		}

		return Function.getVar(varName);
	}

	/**
	 * Compiles  left expression:
	 *        &lt;identifier&gt; ( '[' &lt;expression&gt; ']' | '.'&lt;var-name&gt;)?
	 * @param varName
	 * @param expressions splited array expressions
	 * @param length limit of each array index
	 * @param block holds statements
	 * @return String variable name splitted with $ instead of []
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private String compileLHS(String varName, Vector expressions,
	                          Vector lengths, BlockStatement block)
	                   throws ParseException, IOException
	{
		String fieldName = tokenizer.getIdentifier();

		if (varName == null) {
			varName = fieldName;
		} else {
			varName += ('.' + fieldName);
		}

		advance("program not ended");

		if (tokenizer.tokenType() == Tokenizer.SYMBOL) {
			switch (tokenizer.symbol()) {
				case '[':

					while ((tokenizer.tokenType() == Tokenizer.SYMBOL) &&
						       (tokenizer.symbol() == '[')) {
						advance("program not ended");

						// <expression>
						expressions.add(compileExpression(false, block));

						lengths.add(new Integer(((ArrayType) (Function.getVar(varName)
						                                              .getType())).getLength()));

						// ]
						if ((tokenizer.tokenType() == Tokenizer.SYMBOL) &&
							    (tokenizer.symbol() == ']')) {
							advance("program not ended");
						} else {
							throw new ParseException("] is expected",
							                         tokenizer.lineNumber());
						}

						varName += "[$]";
					}

					if ((tokenizer.tokenType() == Tokenizer.SYMBOL) &&
						    (tokenizer.symbol() == '.')) {
						advance("program not ended");

						return compileLHS(varName + ".", expressions, lengths,
						                  block);
					}

					break;

				case '.':

					// <identifier>
					getNextToken(Tokenizer.IDENTIFIER,
					             " identifier is excepted after .");

					// <var-name>
					return compileLHS(varName, expressions, lengths, block);
			}
		}

		return varName;
	}

	/**
	 * Compiles the expression:
	 *        ('~' | '-')? &lt;term&gt; (&lt;operator&gt; &lt;expression&gt;)?
	 * @param isArgument if it is an argument
	 * @param block holds statements
	 * @return Expression data structure of expression
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private Expression compileExpression(boolean isArgument,
	                                     BlockStatement block)
	                              throws ParseException, IOException
	{
		Expression exp = null;

		try {
			Stack operators = new Stack();
			Stack operands = new Stack();

			do {
				while (tokenizer.tokenType() == Tokenizer.SYMBOL) {
					switch (tokenizer.symbol()) {
						case '~':
							insertNewOperator(operators, operands,
							                  new PrimitiveOperator(PrimitiveOperator.NOT_OP));

							break;

						case '-':
							insertNewOperator(operators, operands,
							                  new UnaryMinusOperator());

							break;

						case '(':
							operators.push(null);

							break;

						default:
							throw new ParseException("~ or - or ( is expected",
							                         tokenizer.lineNumber());
					}

					advance("program not ended");
				}

				// <term>
				operands.push(compileTerm(block));

				// <operator>
				if (tokenizer.tokenType() != Tokenizer.SYMBOL) {
					throw new ParseException("Must be ) or ; or , or operator after term",
					                         tokenizer.lineNumber());
				}

				//flag for dealing with expression: ((...))
				boolean stackUpdated = false;

				while (tokenizer.symbol() == ')') {
					//there isn't ( for the last ) in argumnet list
					if (isArgument && operators.empty()) {
						break;
					}

					if ((operators.peek() == null) && (operands.empty()) &&
						    ! stackUpdated) {
						throw new ParseException("Empty expression ()",
						                         tokenizer.lineNumber());
					}

					stackUpdated = true;

					//until (
					while (operators.peek() != null)
						updateStacks(operators, operands);

					//pop (
					operators.pop();
					advance("program not ended");

					if ((tokenizer.tokenType() != Tokenizer.SYMBOL) ||
						    (tokenizer.symbol() == '{')) {
						break;
					}
				}

				if ((tokenizer.tokenType() != Tokenizer.SYMBOL) ||
					    (tokenizer.symbol() == ';') ||
					    (tokenizer.symbol() == '{') ||
					    (tokenizer.symbol() == ']') ||
					    (isArgument &&
					    ((tokenizer.symbol() == ',') ||
					    (tokenizer.symbol() == ')')))) {
					break;
				}

				compileOperator(operators, operands);
			} while (true);

			while (! operators.empty())
				updateStacks(operators, operands);

			exp = (Expression) operands.pop();

			if (! operands.empty()) {
				throw new ParseException("Error in expression, not enougth operators",
				                         tokenizer.lineNumber());
			}
		} catch (EmptyStackException ese) {
			throw new ParseException("Error in expression",
			                         tokenizer.lineNumber());
		}

		return exp;
	}

	/**
	 * Compiles the term:
	 *        &lt;var-name&gt; | &lt;number&gt; | &lt;identifier&gt; '(' (&lt;expression&gt; (',' &lt;expression&gt;)*)? ')'
	 * @param block holds statements
	 * @return Expression data structure of expression
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private Expression compileTerm(BlockStatement block)
	                        throws ParseException, IOException
	{
		Expression expression = null;

		switch (tokenizer.tokenType()) {
			case Tokenizer.IDENTIFIER:

				String identifier = tokenizer.getIdentifier();

				/* in case the identifier is a defined const */
				expression = Consts.fromName(identifier);

				if (expression != null) {
					advance("program not ended");

					break; // break the switch
				}

				/* in case expression is a variable (lvalue) */
				expression = compileLvalExpression(null, block);

				if (expression != null) {
					break; // break the switch
				}

				if ((tokenizer.tokenType() != Tokenizer.SYMBOL) ||
					    (tokenizer.symbol() != '(')) {
					throw new ParseException("error in term: no const, var, function",
					                         tokenizer.lineNumber());
				}

				advance("program not ended");

				Function calledFunc = Program.functionFromName(identifier);
				int      i = 0;

				//adding arguments assigning statements
				for (Enumeration e = calledFunc.getArguments().elements();
					     e.hasMoreElements() &&
					     ! ((tokenizer.tokenType() == Tokenizer.SYMBOL) &&
					     (tokenizer.symbol() == ')')); i++) {
					block.addStatement(new AssignmentStatement((LvalExpression) e.nextElement(),
					                                           new UnaryOpExpression(new PrimitiveOperator(PrimitiveOperator.ID_OP),
					                                                                 compileExpression(true,
					                                                                                   block))));

					if ((tokenizer.tokenType() == Tokenizer.SYMBOL) &&
						    (tokenizer.symbol() == ',')) {
						advance("program not ended");
					}
				}

				if (i != calledFunc.getArguments().size()) {
					throw new ParseException("Number of argument not match",
					                         tokenizer.lineNumber());
				}

				//adding function body
				block.addStatements(calledFunc.getBody());
				expression = calledFunc.functionResult;
				advance("program not ended");

				break;

			case Tokenizer.INT_CONST:
				expression = new IntConstant(tokenizer.intVal());
				advance("program not ended");

				break;

			case Tokenizer.KEYWORD:

				switch (tokenizer.keyword()) {
					case Tokenizer.KW_TRUE:
						expression = new BooleanConstant(true);

						break;

					case Tokenizer.KW_FALSE:
						expression = new BooleanConstant(false);

						break;

					default:
						throw new ParseException("Unexcepted keyword " +
						                         tokenizer.getKeyword(),
						                         tokenizer.lineNumber());
				}

				advance("program not ended");

				break;

			default:
				throw new ParseException("number or identifier is excepted",
				                         tokenizer.lineNumber());
		}

		return expression;
	}

	/**
	 * Pops operator and operand/s and push back a new operand which
	 * is the expression of them.
	 * @param operators stack of operators
	 * @param operands stack of operands
	 * @throws EmptyStackException if trying to pop from empty stack.
	 */
	private void updateStacks(Stack operators, Stack operands)
	                   throws EmptyStackException
	{
		Operator   operator = (Operator) operators.pop();
		Expression operand = (Expression) operands.pop();

		//if unary operator
		if ((operator instanceof UnaryMinusOperator) ||
			    ((operator instanceof PrimitiveOperator) &&
			    (((PrimitiveOperator) operator).operator == PrimitiveOperator.NOT_OP))) {
			operands.push(new UnaryOpExpression(operator, operand));
		} else { //pop another operand for binary operator
			operands.push(new BinaryOpExpression(operator,
			                                     (Expression) operands.pop(),
			                                     operand));
		}
	}

	/**
	 * Inserts a new operator to operators stack.
	 * If there are operators with higher priority call updateStacks
	 * with them.
	 * @param operators stack of operators
	 * @param operands stack of operands
	 * @param operator operator
	 * @throws EmptyStackException if trying to pop from empty stack.
	 */
	private void insertNewOperator(Stack operators, Stack operands,
	                               Operator operator)
	                        throws EmptyStackException
	{
		while (! operators.empty() && (operators.peek() != null) &&
			       (operator.priority() <= ((Operator) operators.peek()).priority()))
			updateStacks(operators, operands);

		operators.push(operator);
	}

	/**
	 * Compiles the binary operator.
	 * @param operators stack of operators
	 * @param operands stack of operands
	 * @throws IOException - if an I/O error occurs.
	 * @throws ParseException - if a parsing error occurs.
	 */
	private void compileOperator(Stack operators, Stack operands)
	                      throws ParseException, IOException
	{
		boolean getNextToken = true;

		switch (tokenizer.symbol()) {
			case '+':
				insertNewOperator(operators, operands, new PlusOperator());

				break;

			case '-':
				insertNewOperator(operators, operands, new MinusOperator());

				break;

			case '|':
				insertNewOperator(operators, operands,
				                  new PrimitiveOperator(PrimitiveOperator.OR_OP));

				break;

			case '&':
				insertNewOperator(operators, operands,
				                  new PrimitiveOperator(PrimitiveOperator.AND_OP));

				break;

			case '^':
				insertNewOperator(operators, operands,
				                  new PrimitiveOperator(PrimitiveOperator.XOR_OP));

				break;

			case '<':
				advance("program not ended");

				if ((tokenizer.tokenType() == Tokenizer.SYMBOL) &&
					    (tokenizer.symbol() == '=')) {
					insertNewOperator(operators, operands,
					                  new LessEqualOperator());
				} else {
					insertNewOperator(operators, operands, new LessOperator());
					getNextToken = false;
				}

				break;

			case '>':
				advance("program not ended");

				if ((tokenizer.tokenType() == Tokenizer.SYMBOL) &&
					    (tokenizer.symbol() == '=')) {
					insertNewOperator(operators, operands,
					                  new GreaterEqualOperator());
				} else {
					insertNewOperator(operators, operands, new GreaterOperator());
					getNextToken = false;
				}

				break;

			case '=':
				advance("program not ended");

				if ((tokenizer.tokenType() == Tokenizer.SYMBOL) &&
					    (tokenizer.symbol() == '=')) {
					insertNewOperator(operators, operands, new EqualOperator());
				} else {
					throw new ParseException("There should be = after =",
					                         tokenizer.lineNumber());
				}

				break;

			case '!':
				advance("program not ended");

				if ((tokenizer.tokenType() == Tokenizer.SYMBOL) &&
					    (tokenizer.symbol() == '=')) {
					insertNewOperator(operators, operands,
					                  new NotEqualOperator());
				} else {
					throw new ParseException("There should be = after !",
					                         tokenizer.lineNumber());
				}

				break;

			default:
				throw new ParseException("Must be operator or ; or , or { or ]",
				                         tokenizer.lineNumber());
		}

		if (getNextToken) {
			advance("program not ended");
		}
	}

	/**
	 * A test program
	 */
	public static void compile(String fileName, boolean opt)
	                    throws IOException
	{
		Program     program = null;

		FileReader  file = new FileReader(fileName);

		SFECompiler compiler = new SFECompiler(file);

		try {
			program = compiler.compileProgram();
		} catch (ParseException pe) {
			System.err.println("Error in line " + pe.getErrorOffset() + ": " +
			                   pe.getMessage());
			System.exit(1);
		}

		file.close();

		System.out.println("Program compiled.");

		// transformations
		program.multi2SingleBit();

		// unique vars transformations
		program.uniqueVars();

		//Optimization
		if (opt) {
			program.optimize();
		}

		// write circuit
		PrintWriter circuit =
			new PrintWriter(new FileWriter(fileName +
			                               ((opt) ? ".Opt" : ".NoOpt") +
			                               ".circuit"));

		program.toCircuit(circuit, opt);
		circuit.close();

		// write Format file
		PrintWriter fmt =
			new PrintWriter(new FileWriter(fileName +
			                               ((opt) ? ".Opt" : ".NoOpt") +
			                               ".fmt"));
		fmt.println(program.toFormat());
		fmt.close();
	}

	/**
	 * A test program
	 */
	public static void main(String[] args) throws IOException {
		Program program = null;

		if ((args.length != 1) &&
			    ((args.length != 2) || ! args[0].equals("-no-opt"))) {
			System.err.println("Usage: java SFECompiler [-no-opt] <file>");
			System.exit(1);
		}

		String  fileName;
		boolean opt = true;

		if (args.length == 2) {
			fileName     = args[1];
			opt          = false;
		} else {
			fileName = args[0];
		}

		compile(fileName, opt);
	}

	//~ Static fields/initializers ---------------------------------------------

	/*
	 * label for temporary variables
	 */
	static int labelIndex = 0;
}
