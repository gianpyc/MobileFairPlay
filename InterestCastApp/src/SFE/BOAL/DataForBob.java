// DataForBob.java
//
// $Id: DataForBob.java,v 2.0 2004/06/15 14:13:33 ysella Exp $
package SFE.BOAL;

import java.util.BitSet;
import java.util.Vector;


/** DataForBob
 *  @author: Ori Peleg
 */

//---------------------------------------------------------------

/**
 * Data Alice wants to send to Bob.
 *
 * @author Ori Peleg
 */
class DataForBob {
    /**
     * Encode the data for Bob in a BitSet.
     *
     * @param data a vector of byte arrays
     * @return the serialized data
     */
    BitSet serialize(Vector data) {
        return new BitSet();
    }

    /**
     * bit-to-bool conversion
     *
     * @return true if input is 1, false if input is 0
     */
    private static boolean bitToBool(int bit) {
        switch (bit) {
        case 0:
            return false;

        case 1:
            return true;

        default:
            assert false;

            return false;
        }
    }

    private static BitSet byteArrayToBitSet(byte[] array) {
        BitSet result = new BitSet(array.length * 8);

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < 8; j++) {
                int bitIndex = (i * 8) + j;
                int bit = (array[i] >> j) & 1;

                result.set(bitIndex, bit);
            }
        }

        return result;
    }

    private static byte[] bitSetToByteArray(BitSet bits) {
        assert (bits.size() % 8) == 0 : "can only handle octect-streams";

        int resultSize = bits.size() / 8;
        byte[] result = new byte[resultSize];

        for (int i = 0; i < result.length; i++) {
            result[i] = bitSetToByte(bits.get(i * 8, (i + 1) * 8));
        }

        return result;
    }

    /**
     * Convert the first 8 bits in a bitset to a byte.
     *
     * @param bits a BitSet with at least 8 bits
     * @return the BitSet's first 8 bits as a byte
     */
    private static byte bitSetToByte(BitSet bits) {
        assert bits.size() >= 8 : "not long enough to contain an octet";

        byte result = 0;

        for (int i = 0; i < 8; i++) {
            result |= boolToBit(bits.get(i));
            result <<= 1;
        }

        return result;
    }

    /**
     * bool-to-bit conversion
     *
     * @return 1 if input is true, 0 if false
     */
    private static int boolToBit(boolean bool) {
        if (bool) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Decode the data for Bob from a BitSet.
     *
     * @param serializedData the serialized data
     * @return the decoded data (a vector of byte arrays)
     */
    Vector deserialize(BitSet serializedData) {
        return new Vector();
    }
}


// End of file DataForBob.java
