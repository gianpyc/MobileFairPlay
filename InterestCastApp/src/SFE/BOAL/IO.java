// IO.java - IO object describes an interaction with Alice or Bob for I/O.
// Copyright (C) 2004 Dahlia Malkhi, Yaron Sella. 
// See full copyright license terms in file ../GPL.txt

package SFE.BOAL;

import java.io.*;

import java.util.*;


/** IO
    An IO object describes an interaction with Alice or Bob for input/output.

@author: Dahlia Malkhi and Yaron Sella
 */
public class IO implements Serializable {
    static final int MAX_LINES = 512; // Max # of lines per IO format
    private boolean is_input = false;
    private boolean is_alice = false;
    private String prefix = null;
    private int[] lines = new int[MAX_LINES];
    private int nlines = 0;

    //---------------------------------------------------------------   

    /**
     * Constructor for class IO
     *
     * @param is_alice - boolean indicating if this is IO format for Alice or
     * Bob.
     */
    public IO(boolean is_alice) {
        this.is_alice = is_alice;
    }

    //---------------------------------------------------------------   

    /**
     * setInputFlag: set the input/output indicator flag
     *
     * @param is_input - boolean indicating wether this IO is for input or
     * output.
     */
    public void setInputFlag(boolean is_input) {
        this.is_input = is_input;
    }

    //---------------------------------------------------------------   

    /**
     * isAlice: get the Alice/Bob indicator boolean flag
     *
     * @return a boolean indicating whether this is Alice or Bob.
     */
    public boolean isAlice() {
        return this.is_alice;
    }

    //---------------------------------------------------------------   

    /**
     * isInput: get the input/output indicator boolean flag
     *
     * @return a boolean indicating whether this is an input or output IO.
     */
    public boolean isInput() {
        return this.is_input;
    }

    //---------------------------------------------------------------   

    /**
     * setPrefix: set the prefix string
     *
     * @param str - the prefix string.
     */
    public void setPrefix(String str) {
        this.prefix = str;
    }

    //---------------------------------------------------------------   

    /**
     * getPrefix: get the prefix string
     *
     * @return the prefix string.
     */
    public String getPrefix() {
        return this.prefix;
    }

    //---------------------------------------------------------------   

    /**
     * addLinenum: add a line-number for the list of gate-wires that
     * this IO object is responsible for.
     *
     * @param line_num - the line number
     */
    public void addLinenum(int line_num) {
        assert ((nlines + 1) < MAX_LINES) : "Only " + MAX_LINES +
        "line numbers allowed per IO format line";

        lines[nlines++] = line_num;
    }

    //---------------------------------------------------------------   

    /**
     * getLinenum: get the line number of gate wire at the specified index.
     *
     * @param index the index of the IO bit we are intersted in
     * @return the line number of the gate wire for the desingated IO bit
     */
    public int getLinenum(int index) {
        return lines[index];
    }

    //---------------------------------------------------------------   

    /**
     * getNLines: get the number of lines specified for this IO.
     *
     * @return the number of lines.
     */
    public int getNLines() {
        return nlines;
    }
}
