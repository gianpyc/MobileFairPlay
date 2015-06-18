// DataSerializer.java
//
// $Id: DataSerializer.java,v 2.0 2004/06/15 14:13:33 ysella Exp $
package SFE.BOAL;

import java.util.BitSet;
import java.util.Vector;


/** DataSerializer
 *  @author: Ori Peleg
 */

//---------------------------------------------------------------

/**
 * Serialize and de-serialize Alice's and Bob's data
 *
 * @author Ori Peleg
 */
class DataSerializer {
    /**
     * Encode the data for Bob in a BitSet.
     *
     * @return the encoded data
     */
    static BitSet serializeDataForBob(Vector data) {
        return new BitSet();
    }

    /**
     * Decode the data for Bob from a BitSet.
     *
     * @return the decoded data
     */
    static Vector deSerializeDataForBob() {
        return new Vector();
    }
}


// End of file DataSerializer.java
