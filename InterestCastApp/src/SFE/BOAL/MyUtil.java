// MyUtil.java - General services required by other classes.
// Copyright (C) 2004 Dahlia Malkhi, Yaron Sella. 
// See full copyright license terms in file ../GPL.txt

package SFE.BOAL;

import java.io.*;

import java.math.*;

import java.net.*;

import java.security.*;

import java.util.*;


/**
 * This class initializes general services required by other classes,
 * namely a hash-function and an RNG.
 *
 * @author Dahlia Malkhi and Yaron Sella
 */

//---------------------------------------------------------------
public class MyUtil {
    public static MessageDigest md = null;
    public static SecureRandom random = null;
    private static final int Psize = 1024;
    private static final int Psize8 = Psize/8;
    private static final int Qsize = 160;
    private static long rec_time = 0;
    private static long rec_heapsize = 0;
    private static final Runtime rtm = Runtime.getRuntime();
    private static final BigInteger p = new BigInteger(
            "4f26aae8704873bcebfbb7cf0c490575093dad0a60b575b7b025b746fca43dcd" +
            "983275e8f0b72cf92d403450e5b0bfad9f540e3c71de07724956028b4b378e91" +
            "98e8faed0541c79430cd24e8c1e414dfe7824335368993e22400117a85b7c50d" +
            "7f5cd557076cac3e0afaa26cf5dba7b5e043cbdb02eccba3ea1b41d2e5100c5",
            +16);
    private static final BigInteger q = new BigInteger("e5dd551b16375da4f47aaba7f2272556ae0fcc47",
            16);
    private static final BigInteger g = new BigInteger(
            "12b49a27df027dab8492525e74e674702e50662b42ff4d8ac6adcbead05288cd" +
            "adcd6adeabb16f2cebe6cd6bd26ac0e52e21ca081ec70e2bc5be0c50cc81921e" +
            "219f5b23775cae3e55c841d2fb473faff4ebd1d586933f0e4c7778274068661d" +
            "faebe435fed927443e58d3e1b672b9000ca4921b1d493924606ff340080dc2e",
            16);
    private static final BigInteger C = new BigInteger(
            "10962444c6591427c4156e9c41d6b9e8b4f6c6b201657f49870671de2825d842" +
            "67ad7697b04f36e58701d944583119f401ca203eb70c130686b90ebf967f32b5" +
            "642e936c0332660aaabe5387d18376b651cb2a77d906537e8064e50976511ed5" +
            "621d0891b12642c86e5ed23eb3ac8802c45340163a10bfce9978473152a4b5e",
            16);

    //---------------------------------------------------------------

    /**
     * Initialize
     */
    public static void init(String seed) {
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (Exception e) {
        	System.err.println("init: cannot instantiate SHA-1: " + e.getMessage());
        }

	md.update(seed.getBytes());
        random = new SecureRandom (md.digest());
    }

    //---------------------------------------------------------------

    /**
     * hash
     */
    public static byte[] hash (byte[] inp, int no_out_bytes) {
        byte[] res = new byte[no_out_bytes];
        byte[] full_res;

        md.reset();
	md.update(inp);
	full_res = md.digest();

	for (int i = 0 ; i < no_out_bytes ; i++)
           res[i] = full_res[i];

	return (res);
    }

    //---------------------------------------------------------------

    /**
     * Generate one random byte
     *
     * @return - a single random byte.
     */
    public static byte randomByte() {
        return ((byte) (random.nextInt() & 0xff));
    }

    //---------------------------------------------------------------

    /**
     * Generate random bytes
     *
     * @param result - a byte array for generated random bytes.
     */
    public static void randomBytes(byte[] result) {
        random.nextBytes(result);
    }

    //---------------------------------------------------------------

    /**
     * encArrays - Encrypt plain-text array with key array by XOR
     *
     * @param plain - plain text byte array.
     * @param key - key byte array.
     * @return a byte array containing XOR of plain & key.
     */
    public static byte[] encArrays(byte[] plain, byte[] key) {
        return (xorArrays(plain, key));
    }

    //---------------------------------------------------------------

    /**
     * decArrays - Decrypt cipher-text array with key array by XOR
     *
     * @param cipher - cipher text byte array.
     * @param key - key byte array.
     * @return a byte array containing XOR of cipher & key.
     */
    public static byte[] decArrays(byte[] cipher, byte[] key) {
        return (xorArrays(cipher, key));
    }

    //---------------------------------------------------------------

    /**
     * xorArrays - XOR one byte-array with another
     *
     * @param inp1 - 1st byte array to be XORed.
     * @param inp2 - 2nd byte array to be XORed.
     * @return XOR of inp1 & inp2 (byte byte) in a byte array.
     */
    public static byte[] xorArrays(byte[] inp1, byte[] inp2) {
        byte[] result = new byte[inp1.length];

        for (int i = 0; i < inp1.length; i++)
            result[i] = (byte) (inp1[i] ^ inp2[i]);

        return (result);
    }

    //---------------------------------------------------------------

    /**
     * toHexStr - turn a byte to a hex String (for debugging)
     *
     * @param b - a byte.
     * @return String containing the byte in hex
     */
    public static String toHexStr(byte b) {
        String[] s = {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d",
            "e", "f"
        };

        return (new String(s[(b >> 4) & 0x0f] + s[b & 0x0f]));
    }

    //---------------------------------------------------------------

    /**
     * toHexString - turn byte array to a hex String
     * (for debugging purposes)
     *
     * @param arr - byte array.
     * @return String containing the byte array in hex
     */
    public static String toHexString(byte[] arr) {
        String s = new String("");

        for (int i = 0; i < arr.length; i++)
            s = s + toHexStr(arr[i]);

        return (s);
    }

    //---------------------------------------------------------------

    /**
     * Convert int to a byte array.
     *
     * @param in An int to be converted to a byte array.
     * @return a byte array containing the int.
     */
    public static byte[] toByteArray (int in) {
        byte[] b = new byte[4];

	b[0] = (byte) (in         & 0xff);
	b[1] = (byte) ((in >>  8) & 0xff);
	b[2] = (byte) ((in >> 16) & 0xff);
	b[3] = (byte) ((in >> 24) & 0xff);

	return (b);
    }

    //---------------------------------------------------------------

    /**
     * pathFile - concatenate $rundir to a filename
     *
     * @param fname - file name to concatenate to.
     * @return String containing $rundir + fname
     */
    public static String pathFile(String fname) {
        return (System.getProperty("rundir") + fname);
    }

    //---------------------------------------------------------------

    /**
     * deltaTime - profiling tool for measuring time spent in 
     * different program sections, returns the delta time 
     * (in milli-secs) from previous call to this routine.
     *
     * @param reset - reset timing measurements.
     * @return long - delta time (in milli-secs) from previous call.
     */
    public static long deltaTime (boolean reset_time) {

        long delta;
	long current_time = Calendar.getInstance().getTimeInMillis();

	if (reset_time) {
	   delta = 0;
           rec_time = current_time;
        } else {
	   delta = current_time - rec_time;
	   rec_time = current_time;
	}

        return (delta);
    }

    //---------------------------------------------------------------

    /**
     * deltaHeap - tool for measuring size of heap,
     * returns the heap size delta (in bytes) from 
     * previous call to this routine.
     *
     * @return long - delta heap size (in bytes) from previous call.
     */
    public static long deltaHeap () {
        long delta = 0;
        long current_heapsize = rtm.totalMemory()-rtm.freeMemory();

	if (rec_heapsize != 0)
	   delta = current_heapsize - rec_heapsize;
	rec_heapsize = current_heapsize;

        return (delta);
    }

    //---------------------------------------------------------------

    /**
     * EG_genPublic: generate a public El-Gamal encryption key
     *
     * @param selector a 0/1 value selecting which of the two public
     * keys is real and which is 'fake'
     * @param x The El-Gamal private key
     * @return A public El-Gamal encryption key
     */
    public static BigInteger EG_genPublic(int selector, BigInteger x) {
        BigInteger[] pub_keys = new BigInteger[2];

        // A real public key whose DL is known
        pub_keys[selector] = g.modPow(x, p); // g^x mod p

        // A 'fake' public key whose DL is not known
        pub_keys[1-selector] = pub_keys[selector].modInverse(p); // g^(-x) mod p
        pub_keys[1-selector] = C.multiply(pub_keys[1-selector]).mod(p);

        return (pub_keys[0]);
    }

    //---------------------------------------------------------------

    /**
     * EG_deduce: Deduce one El-Gamal public key from another
     *
     * @param pub_key0 An EG public key
     * @return A deduced EG public key
     */
    public static BigInteger EG_deduce(BigInteger pub_key0) {
        return (C.multiply(pub_key0.modInverse(p))).mod(p); 
    }

    //---------------------------------------------------------------

    /**
     * EG_encrypt: Perform El-Gamal encryption
     *
     * @param y El-Gamal public key
     * @param r El-Gamal random secret exponent
     * @param packed_code data to encrypt
     * @return BigInteger containing the ciphertext
     */
    public static BigInteger EG_encrypt(BigInteger y, BigInteger r,
        byte[] packed_code) {
        int i;
        int len = Psize8 - 1;
        byte[] padded_mess = new byte[len];
        BigInteger t; // temporary variable
        BigInteger enc_mess; // result

        // Padding the message & converting to a positive BigInteger
        random.nextBytes(padded_mess);

        for (i = 0; i < packed_code.length; i++)
            padded_mess[i] = packed_code[i];

        System.out.println("EG_encrypt encrypting: " +
            MyUtil.toHexString(padded_mess));

        t = new BigInteger(1, padded_mess);

        // Calculating M*y^r mod p
        enc_mess = y.modPow(r, p); // y^r mod p
        enc_mess = (enc_mess.multiply(t)).mod(p); // M*y^r mod p

        return (enc_mess);
    }

    //---------------------------------------------------------------

    /**
     * EG_decrypt: Perform El-Gamal decryption
     *
     * @param gr g^r part of El-Gamal ciphertext
     * @param yrm (y^r)*M part of El-Gamal ciphertext
     * @return Byte-array with the clear text
     */
    public static byte[] EG_decrypt(BigInteger gr, BigInteger yrm, BigInteger x) {
        BigInteger grx = gr;
        BigInteger res;

        // El-Gamal decryption:  m = (y^r * m) * g^(-rx) mod p (y=g^x mod p)
        grx = grx.modPow(x, p); // g^(rx) mod p
        grx = grx.modInverse(p); // g^(-rx) mod p
        res = (yrm.multiply(grx)).mod(p); // m = (y^r * m) * g^(-rx) mod p

        return (res.toByteArray());
    }

    //---------------------------------------------------------------

    /**
     * EG_pow: Perform modular exponentiation g^pow mod p
     *
     * @param power to exponentiate to
     * @return BigInteger containing the result
     */
    public static BigInteger EG_pow(BigInteger pow) {
        BigInteger res = g.modPow(pow, p);

        return (res);
    }

    //---------------------------------------------------------------

    /**
     * EG_randExp: generate an EG random exponent
     *
     * @return BigInteger containing the result
     */
    public static BigInteger EG_randExp() {
        BigInteger res = new BigInteger(Qsize - 1, random);

        return (res);
    }

    //---------------------------------------------------------------
    
    /**
     * rand: generate an random number of max size
     *
     * @return BigInteger containing the result
     */
    public static BigInteger rand(int size) {
        BigInteger res = new BigInteger(size - 1, random);
	
        return (res);
    }

    //---------------------------------------------------------------

    /**
     * EG_g: return the EG generator g
     *
     * @return BigInteger containing g
     */
    public static BigInteger EG_g() {

        return (g);
    }

    //---------------------------------------------------------------

    /**
     * BigInt2FixedBytes: converts a BigInteger (smaller than the 
     * El-Gamal prime) into a fixed size byte array.
     */
    public static void BigInt2FixedBytes (BigInteger num, byte[] res) {
        byte[] temp = num.toByteArray() ;
	int j;

	res[0] = (byte) (temp.length);
	for (j = 0 ; j < temp.length; j++)
           res[j+1] = temp[j];
    }

    //---------------------------------------------------------------
    
    /**
     * FixedBytes2BigInt: converts a fixed size byte array to a 
     * BigInteger (smaller than the El-Gamal prime)
     */
    public static BigInteger FixedBytes2BigInt (byte[] ba) {

        int j, len = ba[0];
	if (len < 0) len += 256;
        byte[] temp = new byte[len];

	for (j = 0; j < len; j++)
            temp[j] = ba[j+1];

        BigInteger num = new BigInteger (temp);

	return num;
    }


    //---------------------------------------------------------------

    /**
     * BigInts2FixedBytes: converts an array of BigIntegers (smaller 
     * than the El-Gamal prime) into a fixed size byte array.
     */
    public static void BigInts2FixedBytes (BigInteger[] nums, byte[] res) {
	int i, j, k, start_pos;

	for (i = start_pos = 0 ; i < nums.length; i++) {

           byte[] temp = nums[i].toByteArray() ;

	   res[start_pos] = (byte) (temp.length);
	   for (j = 0, k = start_pos+1 ; j < temp.length; j++, k++)
              res[k] = temp[j];
	   start_pos += 129;
	}
    }
 
    //---------------------------------------------------------------
    
    /**
     * FixedBytes2BigInts: converts a fixed size byte array to an 
     * array of BigIntegers (smaller than the El-Gamal prime)
     */
    public static BigInteger[] FixedBytes2BigInts (byte[] ba, int pos,
		                                   int how_many) {
        int i, j, k, start_pos = pos;
	BigInteger[] nums = new BigInteger[how_many];

	for (i = 0; i < how_many; i++) {

           int len = ba[start_pos];
	   if (len < 0) len += 256;
           byte[] temp = new byte[len];
            
	   for (j = 0, k = start_pos+1; j < len; j++, k++)
              temp[j] = ba[k];
	   start_pos += 129;

	   nums[i] = new BigInteger(temp);
	}

	return nums;
    }

    //---------------------------------------------------------------

    /**Obsolete 
     * sendCircuit - send a Circuit object via ObjectOutputStream
     *
     * @param oos - ObjectOutputStream to send the object on.
     * @param c - object to send.
     *
    public static void sendCircuit(ObjectOutputStream oos, Circuit c) {
        try {
            oos.writeObject(c);
        } catch (IOException e) {
            System.out.println("sendCircuit failed: " + e.getMessage());
            System.exit(-1);
        }
    }
    */

    //---------------------------------------------------------------

    /** Obsolete
     * receiveCircuit - receive a Circuit object via ObjectInputStream
     *
     * @param ois - ObjectInputStream to receive the object on.
     * @return Circuit that was received.
     *
    public static Circuit receiveCircuit(ObjectInputStream ois) {
        Circuit c = new Circuit();

        try {
            c = (Circuit) ois.readObject();
        } catch (IOException e) {
            System.out.println("receiveCircuit failed (IOException): " +
                e.getMessage());
            System.exit(-1);
        } catch (Exception e) {
            System.out.println("receiveCircuit failed (Exception): " +
                e.getMessage());
            System.exit(-1);
        }

        return (c);
    }
    */

    //---------------------------------------------------------------

    /**
     * sendOTMESS - send an OTMESS object via ObjectOutputStream
     *
     * @param oos - ObjectOutputStream to send the object on.
     * @param otmess - object to send.
     */
    public static void sendOTMESS(ObjectOutputStream oos, OTMESS otmess) {
        try {
            oos.writeObject(otmess);
            oos.flush();
        } catch (IOException e) {
            System.out.println("sendOTMESS failed: " + e.getMessage());
            System.exit(-1);
        }
    }

    //---------------------------------------------------------------

    /**
     * receiveOTMESS - receive an OTMESS object via ObjectInputStream
     *
     * @param ois - ObjectInputStream to receive the object on.
     * @return OTMESS that was received.
     */
    public static OTMESS receiveOTMESS(ObjectInputStream ois) {
        OTMESS o = new OTMESS();

        try {
            o = (OTMESS) ois.readObject();
        } catch (IOException e) {
            System.out.println("receiveOTMESS failed (IOException): " +
                e.getMessage());
            System.exit(-1);
        } catch (Exception e) {
            System.out.println("receiveOTMESS failed (Exception): " +
                e.getMessage());
            System.exit(-1);
        }

        return (o);
    }

    //---------------------------------------------------------------

    /**
     * sendInt - send an int via ObjectOutputStream
     *
     * @param oos - ObjectOutputStream to send the object on.
     * @param k - int to send.
     * @param flush - whether to flus or not.
     */
    public static void sendInt(ObjectOutputStream oos, int k, boolean flush) {
        try {
            oos.writeInt(k);
            if (flush) oos.flush();
        } catch (IOException e) {
            System.out.println("sendInt failed: " + e.getMessage());
            System.exit(-1);
        }
    }

    //---------------------------------------------------------------

    /**
     * receiveInt - receive an int via ObjectInputStream
     *
     * @param ois - ObjectInputStream to receive the object on.
     * @return int that was received.
     */
    public static int receiveInt(ObjectInputStream ois) {
        int k = -1;

        try {
            k = ois.readInt();
        } catch (IOException e) {
            System.out.println("receiveInt failed (IOException): " +
                e.getMessage());
            System.exit(-1);
        } catch (Exception e) {
            System.out.println("receiveInt failed (Exception): " +
                e.getMessage());
            System.exit(-1);
        }

        return (k);
    }

    //---------------------------------------------------------------

    /**
     * sendBytes - send an array of bytes via ObjectOutputStream
     *
     * @param oos - ObjectOutputStream to send the object on.
     * @param a - array of bytes to send.
     * @param flush - whether to flus or not.
     */
    public static void sendBytes (ObjectOutputStream oos, byte[] a, boolean flush) {
        try {
            oos.write(a);
            if (flush) oos.flush();
            //oos.reset();
        } catch (IOException e) {
            System.out.println("sendBytes failed: " + e.getMessage());
            System.exit(-1);
        }
    }

    //---------------------------------------------------------------

    /**
     * receiveBytes - receive an array of bytes via ObjectInputStream
     *
     * @param ois - ObjectInputStream to receive the object on.
     * @return array of bytes that was received.
     */
    public static void receiveBytes(ObjectInputStream ois, byte[] in, int len) {

        int bytes2read = len, read_bytes, offset=0;

        try {
            do {
               read_bytes = ois.read(in, offset, bytes2read);
               offset += read_bytes;
               bytes2read -= read_bytes;
            } while (bytes2read > 0) ;
        } catch (IOException e) {
            System.out.println("receiveBytes failed (IOException): " +
                e.getMessage());
            System.exit(-1);
        } catch (Exception e) {
            System.out.println("receiveBytes failed (Exception): " +
                e.getMessage());
            System.exit(-1);
        }
    }

    //---------------------------------------------------------------

    /**
     * sendVector - send a Vector object via ObjectOutputStream
     *
     * @param oos - ObjectOutputStream to send the object on.
     * @param v - object to send.
     */
    public static void sendVector(ObjectOutputStream oos, Vector v) {
        try {
            oos.writeObject(v);
            oos.flush();
        } catch (IOException e) {
            System.out.println("sendVector failed: " + e.getMessage());
            System.exit(-1);
        }
    }

    //---------------------------------------------------------------

    /**
     * receiveVector - receive a Vector object via ObjectInputStream
     *
     * @param ois - ObjectInputStream to receive the object on.
     * @return Vector that was received.
     */
    public static Vector receiveVector(ObjectInputStream ois) {
        Vector v = new Vector();

        try {
            v = (Vector) ois.readObject();
        } catch (IOException e) {
            System.out.println("receiveVector failed (IOException): " +
                e.getMessage());
            System.exit(-1);
        } catch (Exception e) {
            System.out.println("receiveVector failed (Exception): " +
                e.getMessage());
            System.exit(-1);
        }

        return (v);
    }

    //---------------------------------------------------------------
    //                           RSA
    
    // -------------------- The 1024 bit RSA ------------------------//
    private static BigInteger privateKey = new BigInteger
    ("300464048963647257095758148947902431492279676640331272812158666"+
     "849076304636267017640423121846658234732222856342524846263197701"+
     "304771182452929358180319121594710312180376142148847928910314754"+
     "353185467742792084025089320569856874561244961035916598785842030"+
     "17571919774902733393787251195353043015954040506774672833");
    private static BigInteger publicKey = new BigInteger("65537");
    private static BigInteger p_prime = new BigInteger
    ("110139630993968966731826811311719827102632065142628065059426291"+
     "767311428134725136349417583795219201784587307909733310828116221"+
     "81424890976329488302146126481");
    private static BigInteger q_prime = new BigInteger
    ("736293558194494003920099038960829833695568030033357284030877613"+
     "860039771051818487329480585898749127550609479775885125617535495"+
     "3519708457791032444288619063");
    private static BigInteger phi = new BigInteger
    ("810951008027779848788596565669989360543181499381327346441415144"+
     "934021652950623158516613546514473302431664992015486815317979892"+
     "529890000181930291864902984595689388409740187299606569351589574"+
     "830933036795130747498240622773524009065163949073835192102286843"+
     "40771390589234842205281075759404183351312904731590961760");
    public static int key_size = 1024;
    public static BigInteger modulus = new BigInteger
    ("810951008027779848788596565669989360543181499381327346441415144"+
     "934021652950623158516613546514473302431664992015486815317979892"+
     "529890000181930291864902984779458375223158554423443284559392385"+
     "303121904941094540960754675926839414305070934156200834487380957"+
     "94736216177967024544268052894348782785433425478025707303");
    public static BigInteger dp = new BigInteger
    ("107445674473479285274465040426072225246497069637438129781792888"+
     "564494695307467794793226174563430496466054365379875329882124334"+
     "73415307073571410121143940833");
    public static BigInteger dq = new BigInteger
    ("677670493564454946403656772984394994257945404940447045416459508"+
     "223198177366596568918732005749830609041079286671675769292523666"+
     "6941655774074145688802435467");
    public static BigInteger pinverseq = new BigInteger
    ("586096997554104605165075848118414400474308063415844375175121089"+
     "156633108342541359281735810334526628982367493049781111204859277"+
     "3743157385614294956699858998");
        
    /**
     * newRSA - receive the sixe of the new modulus
     *          creates new RSA key, prints it and EXITS!!!
     *
     * @param N - the size in bits of the new modulus
     * @return Vector that was received.
     */
    public static void newRSA(int N) {
	System.out.println(modulus.bitLength());
	do {
	    key_size = N;
	    p_prime = BigInteger.probablePrime(N/2, random);
	    q_prime = BigInteger.probablePrime(N/2, random);
	    phi = (p_prime.subtract(BigInteger.ONE)).multiply(q_prime.subtract(BigInteger.ONE));
	    modulus    = p_prime.multiply(q_prime);                            
	    publicKey  = new BigInteger("1");     // common value in practice = 2^16 + 1
	    privateKey = publicKey.modInverse(phi);
	}
	while (((phi.gcd(publicKey)).compareTo(BigInteger.ONE)) != 0);
	dp = privateKey.mod(p_prime.subtract(BigInteger.ONE));
	dq = privateKey.mod(q_prime.subtract(BigInteger.ONE));
	pinverseq = p_prime.modInverse(q_prime);
		
	System.out.println("Please update the following data in MyUtil");
	System.out.println("privateKey: "+privateKey);
	System.out.println("publicKey:  "+publicKey);
	System.out.println("p_prime:    "+p_prime);
	System.out.println("q_prime:    "+q_prime);
	System.out.println("phi:        "+phi);
	System.out.println("key_size:   "+key_size);
	System.out.println("modulus:    "+modulus);
	System.out.println("dp:         "+dp);
	System.out.println("dq:         "+dq);
	System.out.println("pinverseq:  "+pinverseq);
       	System.out.println("Update the keys and re-run the program");
	System.exit(0);
    }

    public static BigInteger decrypt(BigInteger c) {
	BigInteger cDp = c.modPow(dp, p_prime);
	BigInteger cDq = c.modPow(dq, q_prime);
	BigInteger u = ((cDq.subtract(cDp)).multiply(pinverseq)).remainder(q_prime);
	if (u.compareTo(BigInteger.ZERO) < 0) u = u.add(q_prime);
	return cDp.add(u.multiply(p_prime));
    }

    public static BigInteger encrypt(BigInteger m) {
	return m.modPow(publicKey,modulus);
    }

    public static BigInteger pad(BigInteger bi){
	byte[] output = new byte[MyUtil.key_size/8];
	byte[] bi_byte_array = bi.toByteArray();
	byte[] r = new byte[MyUtil.key_size/8 - (bi.toByteArray()).length - 3];
	
	MyUtil.randomBytes(r);
	output[0]=0;
	output[1]=2;
	for (int j = 0 ; j < r.length; j++)
	    output[j+2] = r[j]; 
	output[2+r.length]=0;
	for (int j = 0 ; j < bi_byte_array.length; j++)
	    output[3+r.length+j] = bi_byte_array[j]; 

	return (new BigInteger(output));
    }
}
