// Parser.java - SFE circuits parser.
// Copyright (C) 2004 Dahlia Malkhi, Yaron Sella. 
// See full copyright license terms in file ../GPL.txt

package SFE.BOAL;

import java.io.*;

import java.math.*;


/** Parser

The straight-line input code with inputs and
generic gate-descriptions has the following format:

<p>
(running line number) [output] input

<p>
or

<p>
(running line number)         [output] gate arity n
                        table [ 0..(2^n)-1 enumeration of 0/1 outputs ]
                        inputs [ line-num1 .. line-numn ]
<br>
                        // outputs for each possible n-bit input
<br>
                        // n inputs, designated by line-number

<p>
Note: a line with a "gate name", of the form:
<br>
        (running line) and line-num1 line-num2
<br>
is simply a macro, that expands into:
<p>
(running line)         gate arity 2
                table [ 0 0 0 1 ]
                inputs [ line-num1 line-num2 ]
<br>
Explanation: The gate has 2 inputs; The output enumeration is:
<br>
        00 : output 0
<br>
        01 : output 0
<br>
        10 : output 0
<br>
        11 : output 1

@author: Dahlia Malkhi and Yaron Sella
 */

//---------------------------------------------------------------   

/**
 * This class parses an input file containing a circuit,
 * and representing it in internal data structure.
 *
 * @author Dahlia Malkhi and Yaron Sella.
 */
public class Parser {
    static final int EOF_INDICATOR = -2; // Input file ended
    static final int WORD_INDICATOR = -1; // Token read was a WORD
    static final int INPUT_LINE = -2; // Line encountered of type input-line
    static final int GATE_LINE = -1; // Line encountered of type gate-line
    private StreamTokenizer st;
    private String s = null; // Stores WORD tokens
    private int line_num = -1; // Current line number
    private int ninputs = 0; // Num of inputs in a gate-line
    private int args_ind = 1;
    private Circuit circuit;

    //---------------------------------------------------------------   

    /**
     * Constructor for class Parser
     *
     * @param st - a StreamTokenizer that this parser operates on.
     */
    public Parser(StreamTokenizer st) {
        this.st = st;

        // The chars '[' ']' may appear in input files to improve 
        // readability for humans, but the Parser simply ignores them.
        st.whitespaceChars('[', '[');
        st.whitespaceChars(']', ']');
        st.slashSlashComments(true);

        // initialize a Circuit object to store information on all gates
        circuit = new Circuit();
    }

    //---------------------------------------------------------------

    /**
     * The parse() method is the main body of the parser.
     * It is called at the beginning of the gate definition file,
     * and it consumes the entire input file while building an
     * internal representation of the input using the Circuit package
     *
     * @exception - ParseError.
     */
    public void parse() throws ParseError {
        boolean out_line;
        int line_type;

        try {
            while (parseLineNum()) {
                out_line = parseOptString("output");
                line_type = parseLineName();

                if (line_type == INPUT_LINE) {
                    // add input entry to straight-line code vector
                    Gate g = circuit.addGate(0, Gate.INP_GATE, out_line);
                } else {
                    parseGate(out_line);
                }
            }
        } catch (Exception e) {
            System.err.println("parse: exception " + e.getMessage());

            return;
        }
    }

    //---------------------------------------------------------------

    /** getCircuit returns the circuit that the parser builds.
     *
     * @return the Circuit built by the parser.
     */
    public Circuit getCircuit() {
        return circuit;
    }

    //---------------------------------------------------------------

    /**
     * Parse line numbers. These should be 0,1,2, etc.
     * The only method that can accept eof.
     *
     * @return true if parsing should continue (a new line encountered).
     * @exception - ParseError (bad line number)
     */
    private boolean parseLineNum() throws ParseError {
        int ln = parseToken(true);

        if (ln == EOF_INDICATOR) {
            return false;
        }

        if (ln != ++line_num) {
            throw new ParseError("parseLineNum : Bad line number");
        }

        //System.out.println("parseLineNum: line number = " + line_num);

        return (true);
    }

    //---------------------------------------------------------------

    /**
     * Parse line names. Current options: 'input' or 'gate'.
     *
     * @return indicator for type of line encountered (input or gate)
     * @exception - ParseError (bad line name)
     */
    private int parseLineName() throws ParseError {
        int rc = parseToken(false);
        boolean inp_line = s.equals("input");
        boolean gate_line = s.equals("gate");

        if ((rc != WORD_INDICATOR) || (!inp_line && !gate_line)) {
            throw new ParseError("parseLineName : Bad line name");
        }

        if (inp_line) {
            //System.out.println("parseLineName: input line " + line_num);
            rc = INPUT_LINE;
        }

        if (gate_line) {
            //System.out.println("parseLineName: gate line " + line_num);
            rc = GATE_LINE;
        }

        return (rc);
    }

    //---------------------------------------------------------------

    /**
     * Parse a gate line (after the keyword gate).
     *
     * @param out_line - true if current line goes to output.
     * @exception - ParseError.
     */
    private void parseGate(boolean out_line) throws ParseError {
        int ninputs;

        parseString("arity");
        ninputs = parseNumInputs();

        Gate cur_gate = circuit.addGate(ninputs, Gate.REG_GATE, out_line);
        parseString("table");
        parseTruthTable(cur_gate);
        parseString("inputs");
        parseInputs(cur_gate);
    }

    //---------------------------------------------------------------

    /**
     * Parse a specific string.
     *
     * @param s1 - string to be parsed.
     * @exception - ParseError (expected string not found)
     */
    private void parseString(String s1) throws ParseError {
        parseToken(false);

        if (!s.equals(s1)) {
            throw new ParseError("parseKWInputs : '" + s1 + "' expected at " +
                line_num);
        }
    }

    //---------------------------------------------------------------

    /**
     * Parse a specific optional string.
     *
     * @param s1 - string to be optionally parsed.
     * @return true if specific optional string was found
     */
    private boolean parseOptString(String s1) throws ParseError {
        parseToken(false);

        boolean rc = s.equals(s1);

        if (!rc) {
            st.pushBack();
        }

        return (rc);
    }

    //---------------------------------------------------------------

    /**
    /** Parse the number of inputs that this gate has.
     *
     * @return - number of inputs that this gate has.
     * @exception - ParseError (bad # of inputs)
     */
    private int parseNumInputs() throws ParseError {
        final int MAX_INS = 4;

        ninputs = parseToken(false);

        if ((ninputs < 0) || (ninputs > MAX_INS)) {
            throw new ParseError("parseNumInputs: Bad # of inputs at " +
                line_num);
        }

        return (ninputs);
    }

    //---------------------------------------------------------------

    /**
     * Parse the line-numbers that give inputs to this gate.
     *
     * @param cur_gate [In/Out] - current gate to be updated.
     * @exception - ParseError (bad input)
     */
    private void parseInputs(Gate cur_gate) throws ParseError {
        for (int i = 0; i < ninputs; i++) {
            int inp_num = parseWireNum();

            // Store that input wire to the proper gate/input
            cur_gate.setInput(i, circuit.getGate(inp_num));
        }
    }

    //---------------------------------------------------------------

    /**
     * Parse one wire-number
     *
     * @return the wire number parsed from input.
     * @exception - ParseError (bad wire #)
     */
    private int parseWireNum() throws ParseError {
        int wire_num = parseToken(false);

        // Verify input line-number makes sense
        if ((wire_num < 0) || (wire_num >= line_num)) {
            throw new ParseError("ParseWireNum: Bad wire # at " + line_num);
        }

        return wire_num;
    }

    //---------------------------------------------------------------

    /**
     * Parse the truth-table that represents the logic of this gate.
     *
     * @param cur_gate [In/Out] - current gate to be updated.
     * @exception - ParseError (bad output, e.g., not 0/1)
     */
    private void parseTruthTable(Gate cur_gate) throws ParseError {
        int out_bit;

        // expecting 2^ninput output bits (0/1)
        for (int i = 0; i < (1 << ninputs); i++) {
            out_bit = parseToken(false);

            if ((out_bit != 0) && (out_bit != 1)) {
                throw new ParseError("ParseOutputs: bad output bit at " +
                    line_num);
            }

            // Set entry in truth-table to true
            if (out_bit == 1) {
                cur_gate.setOutput(i, true);
                //System.out.println("ParseGate: output  (" + i + ") is true");
            }
            // else -- setting to false is not needed, since all 
            // output values are initialized to false when created.
            else {
                //System.out.println("ParseGate: output  (" + i + ") is false");
            }
        }
    }

    //---------------------------------------------------------------

    /**
     * Parse a single token. The only method that activates
     * the StreamTokenizer.
     *
     * @param eof_ok - true if OK to see eof now.
     * @return value of numeric token, or indication of WORD token or eof.
     * @exception - ParseError upon StreamTokenizer/IO problem
     * @side-effect - puts WORD token in string s.
     */
    private int parseToken(boolean eof_ok) throws ParseError {
        int status;
        int rc;

        try {
            status = st.nextToken();
        } catch (Exception e) {
            throw new ParseError("parseToken: st.nextToken failed");
        }

        s = "";

        if (status == StreamTokenizer.TT_EOF) {
            if (!eof_ok) {
                throw new ParseError("parseToken: unexpected eof");
            }

            rc = EOF_INDICATOR;
        } else if (st.ttype == StreamTokenizer.TT_NUMBER) {
            //System.out.println("parseToken: token is number: " + st.nval);
            rc = (int) st.nval;
        } else {
            //System.out.println("parseToken: token is string: " + st.sval);
            s = new String(st.sval);
            rc = WORD_INDICATOR;
        }

        //System.out.println("parseToken: returning rc = " + rc);

        return (rc);
    }

    //---------------------------------------------------------------

    public static void parserUsage() {
        System.out.println("Usage: java SFE.BOAL.Parser <filename> <seed>");
        System.exit(1);
    }

    //---------------------------------------------------------------

    /**
     * A main program for testing the parser in standalone mode.
     * Note - this main calls Formatter.getInput_noOT(), which is
     * an older version of getInput() that does not perform OT.
     *
     * @param args - command line arguments.
     *               args[0] should be circuit filename
     *               args[1] should be format filename
     */
    public static void main(String[] args) {
        Parser p = null; // the parser and builder of the circuit
        Formatter f = null; // the parser and builder of the IO
        BufferedReader br = null; // Reads from stdio
        int[] dumm = new int[2];


	if (args.length != 2) 
            parserUsage();

	String circuit_filename = args[0] + ".Opt.circuit";
	String fmt_filename = args[0] + ".Opt.fmt";
	String sseed = args[1];

        // Parse the circuit file and prepare the circuit
 
        FileReader fr = null;
        StreamTokenizer st = null;

        try {
            // Preparations
            fr = new FileReader(circuit_filename);
            st = new StreamTokenizer(fr);
            MyUtil.init(sseed);

            // Parsing
            p = new Parser(st);
            p.parse();

            // Clean-up
            fr.close();
        } catch (IOException e) {
            System.err.println("main: cannot open/close " + circuit_filename + " - " +
                e.getMessage());
        } catch (ParseError e) {
            System.err.println("main: parsing " + circuit_filename + " failed.");
        } catch (Exception e) {
            System.err.println("main: exception - " + e.getMessage());
        }

        // Parse the IOformat file and prepare the inputs
        //
        FileReader fmt = null;
        StreamTokenizer fmtst = null;

        try {
            // Preparations
            fmt = new FileReader(fmt_filename);
            fmtst = new StreamTokenizer(fmt);

            // IO Formatting
            f = new Formatter(fmtst,null,null);
            f.parse();

            // Clean-up
            fmt.close();
        } catch (IOException e) {
            System.err.println("main: cannot open/close " + fmt_filename + " - " +
                e.getMessage());
        } catch (FormatterError e) {
            System.err.println("main: parsing " + fmt_filename + " failed.");
        } catch (Exception e) {
            System.err.println("main: exception - " + e.getMessage());
        }

        // now do something with the circuit!
        Circuit c = p.getCircuit();
	f.markIO (c, dumm);
        c.generateEncCircuit();

        br = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("start input for Alice");
        f.getAliceInput(c, br); // get Alice's input
        System.out.println("start input for Bob");
        f.getBobInput(c, br); // get Bob's input

        //c.evalCircuit();
        c.evalGarbledCircuit(true, true);

        f.getAliceOutput(c); // print Alice's output
        f.getBobOutput(c); // print Bob's output
    }

    class ParseError extends Exception {
        public ParseError(String s) {
            super(s);
            System.err.println(s);
        }
    }
}
