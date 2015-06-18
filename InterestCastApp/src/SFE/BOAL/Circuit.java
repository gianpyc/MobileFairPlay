// Circuit.java - data structures and methods used for garbeled circuit preparation.
// Copyright (C) 2004 Dahlia Malkhi, Yaron Sella. 
// See full copyright license terms in file ../GPL.txt

package SFE.BOAL;

import java.io.*;

import java.math.*;

import java.security.*;

import java.util.*;


/**

This class holds the data structures and methods used for garbeled circuit
preparation.

<p> For the l'th gate, we generate garbeled output description values <br>
U(l,0) U(l,1) C(l) <br>

Where U(l,0) is an 79-bit random number representing the output value 0 of the
l'th fate.  Likewise, U(l,1) represents 1 on this wire; and C(l) is one bit
representing a permutation on {0,1}.

<p> Note: an input line is simply a gate with no input or computation.

<p> Output description values for all lines are kept in a vector of gates
implemented by the Gates class.

<p> Let an input line l be as follows:

<p> l gate n i1 .. in o0 o1 .. o((2^n)-1) <br>
                // outputs of inputs 00...0 , 00..01 through 11..11

<p> Let F be a strong hash function such as SHA-1.  Let || denote a
concatanation operation.

<p> We then generate a garbeled circuit for line l as follows:

<br> l gate n i1 .. in [enumeration of (2^n-1) garbeled outputs]

<p> In the output above, we first repeat inputs, so that the computing-side
will know where to take them from

<p> We then output garbeled output values, enumerated by the permuted input
values.

<p> That is, starting from (00..00) = (c1c2..cn), let: <br> vk = (C(ik) XOR ck)
, k=1..n <br> let j be the binary index corresponding to (v1 v2.. vn)

<p> We encode the output value oj as follows:

<br> ( U(l,oj) || ( oj xor C(l) ) ) xor F( U(i1,v1) || U(i2,v2) || ... ||
U(in,vn))

<p> This continues until we enumerate all (2^n)-1 combination of (c1 c2 .. cn)

@author Dahlia Malkhi and Yaron Sella
*/

//---------------------------------------------------------------
public class Circuit implements Serializable {
    Vector circuit = new Vector(1000, 1000);

    //---------------------------------------------------------------

    /**
     * Add a new gate with the specified number of input wires to
     * the gates vector.
     *
     * @param n_ins the number of input wires of the gate.
     * @param type the type of gate (e.g., regular/input/output)
     * @return the new gate.
     */
    public Gate addGate(int n_ins, int type, boolean out_gate) {
        int csize = circuit.size();
        Gate newgate = new Gate(csize, n_ins, type, out_gate);
        circuit.add(newgate);
        System.out.println("add gate #" + csize + " with " + n_ins +
            " inputs");

        return newgate;
    }

    //---------------------------------------------------------------

    /**
     * Get a gate from a circuit
     */
    Gate getGate(int gate_num) {
        assert ((gate_num >= 0) && (gate_num < circuit.size())) : "getGate: bad gate_num = " +
        gate_num;

        return (Gate) circuit.elementAt(gate_num);
    }

    //---------------------------------------------------------------

    /**
     * Turn all gates in a circuit to encrypted gates
     */
    public void generateEncCircuit() {

        for (int i = 0; i < circuit.size(); i++)
            getGate(i).generateEncGate(); 
    }

    //---------------------------------------------------------------

    /**
     * Measure size of encrypted payload in an encrypted circuit
     *
     * @return int - circuit's encrypted payload size in bytes
     */
    public int cmeasureEncPayload () {
        int total_size = 0;

        for (int i = 0; i < circuit.size(); i++)
            total_size += getGate(i).gmeasureEncPayload (); 

        return (total_size);
    }

    //---------------------------------------------------------------

    /**
     * Extract encrypyed payload from an encrypted circuit (for the 
     * purpose of communicating it in minimal overhead).
     *
     * @param total_size total size of the circuit's payload in bytes.
     * @return a byte array with the relevant information.
     */
    public byte[] cextractEncPayload (int total_size) {
        int i, j, k = 0 ;
        byte[] small_byte_arr;
        byte[] big_byte_arr = new byte[total_size];

        // Extract relevant data from all the circuit's gates,
        // and store it in the result array

        for (i = 0; i < circuit.size(); i++) {
            small_byte_arr = getGate(i).gextractEncPayload();
            if (small_byte_arr != null) {
               for (j = 0; j < small_byte_arr.length ; j++, k++)
                  big_byte_arr[k] = small_byte_arr[j];
            }
	}

        return (big_byte_arr);
    }
 
    //---------------------------------------------------------------

    /**
     * Inject encrypyed payload into a circuit.
     *
     * @param info byte array with data to inject
     */
    public void cinjectEncPayload(byte[] info) {
        int i, consumed_bytes, k;

        for (i = k = 0; i < circuit.size(); i++) {
            Gate g = getGate(i) ;
            consumed_bytes = g.ginjectEncPayload(info, k);
            k += consumed_bytes;
	}
    }
 
    //---------------------------------------------------------------

    /**
     * Measure size of secret payload in an encrypted circuit
     *
     * @return int - circuit's secret payload size in bytes
     */
    public int cmeasureSecPayload () {

        return (circuit.size() * getGate(0).gmeasureSecPayload());
    }

    //---------------------------------------------------------------

    /**
     * Extract secret payload from an encrypted circuit (for the 
     * purpose of communicating it in minimal overhead).
     *
     * @param total_size total size of the circuit's payload in bytes.
     * @return a byte array with the relevant information.
     */
    public byte[] cextractSecPayload (int total_size) {
        int i, j, k = 0 ;
        byte[] small_byte_arr;
        byte[] big_byte_arr = new byte[total_size];

        // Extract relevant data from all the circuit's gates,
        // and store it in the result array

        for (i = 0; i < circuit.size(); i++) {
            small_byte_arr = getGate(i).gextractSecPayload();
            if (small_byte_arr != null) {
               for (j = 0; j < small_byte_arr.length ; j++, k++)
                  big_byte_arr[k] = small_byte_arr[j];
            }
	}

        return (big_byte_arr);
    }
 
    //---------------------------------------------------------------

    /**
     * Inject secret payload into a circuit.
     *
     * @param info byte array with data to inject
     */
    public void cinjectSecPayload(byte[] info) {
        int i, consumed_bytes, k;

        for (i = k = 0; i < circuit.size(); i++) {
            Gate g = getGate(i) ;
            consumed_bytes = g.ginjectSecPayload(info, k);
            k += consumed_bytes;
	}
    }
 
    //---------------------------------------------------------------

    /**
     * Extract input payload from an encrypted circuit (for the 
     * purpose of communicating it in minimal overhead).
     *
     * @param total_size total size of the circuit's payload in bytes.
     * @param alice_inputs true for alice, false for bob 
     * @return a byte array with the relevant information.
     *
    public byte[] cextractInpPayload (int total_size, boolean alice_inputs) {
        int i, j, k = 0 ;
        byte[] small_byte_arr;
        byte[] big_byte_arr = new byte[total_size];

        // Extract relevant data from all the circuit's gates,
        // and store it in the result array

        for (i = 0; i < circuit.size(); i++) {
            small_byte_arr = getGate(i).gextractInpPayload(alice_inputs);
            if (small_byte_arr != null) {
               for (j = 0; j < small_byte_arr.length ; j++, k++)
                  big_byte_arr[k] = small_byte_arr[j];
            }
	}

        return (big_byte_arr);
    }
    */
 
    //---------------------------------------------------------------

    /**
     * Inject input payload into a circuit.
     *
     * @param alice_inputs true for alice, false for bob 
     * @param info byte array with data to inject
     *
    public void cinjectInpPayload(byte[] info, boolean alice_inputs) {
        int i, consumed_bytes, k;

        for (i = k = 0; i < circuit.size(); i++) {
            Gate g = getGate(i) ;
            consumed_bytes = g.ginjectInpPayload(info, k, alice_inputs);
            k += consumed_bytes;
	}
    }
    */

    //---------------------------------------------------------------
 
    /**
     * Verify that an exposed garbled circuit is correct
     * (done by Alice in order to prevent cheating by Bob).
     *
     * @return true if the circuit is correct.
     */
     public boolean isCorrect () {

	 for (int i = 0; i < circuit.size(); i++) {
		 	System.out.println("isCorrect: checking gate in line number " + i);
            if (!getGate(i).isCorrect())  {
		    getGate(i).printGate();
		    return false;
	    }
	 }

	 return true;
     }

    //---------------------------------------------------------------

    /**
     * Evaluate a circuit
     */
    public void evalCircuit() {
    	System.out.println("evalCircuit: having " + circuit.size() + " gates");

        for (int i = 0; i < circuit.size(); i++) {
        	System.out.println("evalCircuit: evaluating gate in line number " + i);
            getGate(i).evalGate();
        }
    }

    //---------------------------------------------------------------

    /**
     * Evaluate a garbled circuit
     *
     * @param alice_interpret - interpret Alice garbled outputs
     * @param bob_interpret - interpret Bob garbled outputs
     */
    public void evalGarbledCircuit(boolean alice_interpret, boolean bob_interpret) {
    	System.out.println("evalGarbledCircuit: having " + circuit.size() + " gates");

        for (int i = 0; i < circuit.size(); i++) {
        	System.out.println("evalGarbledCircuit: evaluating gate in line number " +
                i);
            getGate(i).evalGarbledGate(alice_interpret, bob_interpret);
        }
    }

    //---------------------------------------------------------------

    /**
     * For debugging purposes.
     */
    public void printCircuit() {
        for (int i = 0; i < circuit.size(); i++) {
            System.out.print("Gate number " + i + ":");
	    Gate g = getGate(i);
	    if (g == null) 
               System.out.println("null");
	    else
               g.printGate();
	}
    }

    //---------------------------------------------------------------
 
    /** Obsolete
     * Clean all the gates in the circuit from secret information.
     *
     * @return a circuit that contains the secret, cleaned information.
     *
    public Circuit cleanSecretInfo() {
        Circuit c = new Circuit();

        for (int i = 0; i < this.circuit.size(); i++)
	{
            Gate g = this.getGate(i);
	    Gate g1 = g.cleanGate();
            c.circuit.add(g1);
	}

        return (c);
    }
    */
 
    //---------------------------------------------------------------
 
    /** Obsolete
     * Restore the secret data of a circuit from a given circuit.
     *
     * @param c a circuit to restore the secret data from.
     *
    public void restoreSecretInfo(Circuit c) {

        for (int i = 0; i < circuit.size(); i++)
            getGate(i).restoreGate(c.getGate(i)) ;
    }
    */
 
    //---------------------------------------------------------------

    /** Obsolete
     * Copy garbled inputs of either Bob or Alice from one circuit 
     * to another
     *
     * @param c1 a circuit to copy the garbled inputs from.
     * @param alice_inputs true for alice, false for bob 
     *
    public void copyGarbledInputs(Circuit c1, boolean alice_inputs) {

        for (int i = 0; i < this.circuit.size(); i++) {
            Gate g = this.getGate(i);
	    if (!g.isInput()) continue;
	    if (alice_inputs && g.isBobInput()) continue;
	    if (!alice_inputs && g.isAliceInput()) continue;
            Gate g1 = c1.getGate(i);
	    if ((g.isBobInput() && !g1.isBobInput()) ||
	        (g.isAliceInput() && !g1.isAliceInput())) {
               logger.error("copyGarbledInputs: circuits inconsistency in line number " + i);
	       System.exit(-1);
	    }
	    g.copyGarbledInfo (g1);
	}
    }
    */

    //---------------------------------------------------------------
 
    /** Obsolete
     * Verify that two circuits are logically equivalent
     *
     * @param gc a garbled circuit with inputs to compare to.
     * @return true if the circuits are logically equivalent.
     *
     public boolean isEquivalent(Circuit gc) {

	 if (circuit.size() != gc.circuit.size()) return false;

	 for (int i = 0; i < circuit.size(); i++)
            if (!getGate(i).isEquivGate(gc.getGate(i))) return false;

	 return true;
     }
     */
}
