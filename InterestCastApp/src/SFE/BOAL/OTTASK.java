// OTTASK.java - Describe several Oblivious Transfer tasks.
// Copyright (C) 2004 Dahlia Malkhi, Yaron Sella. 
// See full copyright license terms in file ../GPL.txt

package SFE.BOAL;

import java.util.*;


class OTTASK {
    int ot_id;
    int total;
    int selected;
    Vector elements_values = new Vector(10, 10);
    byte[] transferred_value = null;

    public OTTASK(int ot_id, int total, int selected) {
        this.ot_id = ot_id;
        this.total = total;
        this.selected = selected;
    }

    public OTTASK(int ot_id, int total) {
        this.ot_id = ot_id;
        this.total = total;
        this.selected = -1;
    }

    //---------------------------------------------------------------

    /**
     * addElement: add an element to the vector of elements values
     *
     * @param value value to add (byte array).
     */
    public void addElement(byte[] value) {
        elements_values.add(value);
    }

    //---------------------------------------------------------------

    /**
     * getElement: get an element from the vector of elements values
     *
     * @param index index of element to get.
     * @return value (byte array).
     */
    public byte[] getElement(int index) {
        return ((byte[]) elements_values.elementAt(index));
    }

    //---------------------------------------------------------------

    /**
     * setTransValue: set the transferred value
     *
     * @param value set the transferred value to it.
     */
    public void setTransValue(byte[] value) {
        transferred_value = new byte[value.length];

        for (int i = 0; i < value.length; i++)
            transferred_value[i] = value[i];
    }
}
