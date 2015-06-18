// Bob.java - Bob's part of the 2-party SFE protocol.
// Copyright (C) 2004 Dahlia Malkhi, Yaron Sella.
// See full copyright license terms in file ../GPL.txt

package SFE.BOAL;

//import org.apache.log4j.*;

import java.io.*;

import java.math.*;

import java.net.*;

import java.util.*;

import java.util.regex.*;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import SFE.BOAL.*;

import SFE.Compiler.*;


/** Bob
 *  @author: Dahlia Malkhi and Yaron Sella
 */

//---------------------------------------------------------------   

/**
 * This class implements Bob - the sender in the two-party
 * SFE protocl.
 *
 * @author Dahlia Malkhi and Yaron Sella.
 */
public class Bob {
    //private static final Logger logger = Logger.getLogger(Bob.class);
    Formatter f;
    private String message = "";
    private Handler myhandlerbob=null;
    public static boolean closeSocketStream = false;
    //---------------------------------------------------------------   

    /**
     * Bob Constructor
     *
     * @param circuit_filename - circuit filename
     * @param fmt_filename - format filename
     * @param sot_type - type of OT to perform (String)
     */
    public Bob(String circuit_filename, String fmt_filename, String sseed, String sot_type, BluetoothSocket sockBluetooth, Handler myhandler, String[] tp)
        throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        myhandlerbob=myhandler;
        int i, j;
        int num_of_iterations;
        int num_of_circuits;
        int ot_type = Integer.parseInt(sot_type);
	int cc_num;
        int[] bob_io_size = new int[2];
        Parser p = null; // the parser and builder of the circuit
        OT ot;
     //   ServerSocket SS = null;
        BluetoothSocket sock = null;
        ObjectInputStream fromAlice = null;
        ObjectOutputStream toAlice = null;
        Vector bob_results;

        // Preparations
        MyUtil.init(sseed);

        // Establish connection with Alice
        try {
            //SS = new ServerSocket(3496); // create a server socket
        	
            //logger.info("Bob: waiting for Alice to connect");
            System.out.println("Bob: waiting for Alice to connect");
            
            //sock = SS.accept(); // Accept Alice on this socket
            sock = sockBluetooth;
            
            toAlice = new ObjectOutputStream(sock.getOutputStream());
            fromAlice = new ObjectInputStream(sock.getInputStream());
            
            
        } catch (IOException e) {
            System.out.println(
                "Bob: establishing connection with Alice failed: " +
                e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        MyUtil.sendInt(toAlice, ot_type, true);
        ot = new OT(ot_type);
        int aint = MyUtil.receiveInt(fromAlice);
	num_of_circuits = aint & 0xff ;
	num_of_iterations = aint >> 8 ;

        Vector vEncPayload = new Vector (num_of_circuits);
        Vector vSecPayload = new Vector (num_of_circuits);
        byte[] EncPayload;
        byte[] SecPayload;
        byte[] InpPayload;
        byte[] OutPayload;
        int EncPayloadSize=0;
        int SecPayloadSize=0;
        int InpPayloadSize;
        int OutPayloadSize;

        Circuit c;

        for (i = 0; i < num_of_iterations; i++) {
            System.out.println("Iteration no = " + i);
            // Parse the IOformat file and prepare the inputs
            try {
                // Preparations
                FileReader fmt = new FileReader(fmt_filename);
                StreamTokenizer fmtst = new StreamTokenizer(fmt);

                // IO Formatting
                f = new Formatter(fmtst, myhandlerbob,tp);
                f.parse();

                // Cleanup
                fmt.close();
            } catch (IOException e) {
            	System.out.println("Bob: cannot open/close " + fmt_filename + " - " +
                    e.getMessage());
            } catch (FormatterError e) {
            	System.out.println("Bob: parsing " + fmt_filename + " failed.");
            } catch (Exception e) {
            	System.out.println("Bob: exception - " + e.getMessage());
            }

            // Parse circuit file
            try {
                // Preparations
                FileReader fr = new FileReader(circuit_filename);
                StreamTokenizer st = new StreamTokenizer(fr);

                // Parsing
                p = new Parser(st);
                p.parse();

                // Cleanup
                fr.close();
            } catch (IOException e) {
            	System.out.println("Bob: cannot open/close " + circuit_filename +
                    " - " + e.getMessage());
                System.exit(1);
            } catch (Exception e) {
            	System.out.println("Bob: exception - " + e.getMessage());
                System.exit(1);
            }

            c = p.getCircuit();        // Obtain a circuit object
	    f.markIO (c, bob_io_size); // Mark its inputs & outputs
            InpPayloadSize = bob_io_size[0];
            OutPayloadSize = bob_io_size[1];

	    // Run the SFE protocol
	    // ====================

            // Repeatedly encrypt circuit, extract data from it and save
	    for (j = 0; j < num_of_circuits; j++) {

               c.generateEncCircuit(); // Encrypt it

               if (j == 0) { // Compute sizes only on first iteration
                  EncPayloadSize = c.cmeasureEncPayload();
                  SecPayloadSize = c.cmeasureSecPayload();
               }

               EncPayload = c.cextractEncPayload(EncPayloadSize);
               vEncPayload.add (EncPayload);
               MyUtil.sendBytes(toAlice, EncPayload, false);
               SecPayload = c.cextractSecPayload(SecPayloadSize);
               vSecPayload.add (SecPayload);
	    }
	    toAlice.flush();

	    // Receive cc_num from Alice
            cc_num = MyUtil.receiveInt(fromAlice);

            // Send encrypted circuits secrets (except the chosen one) to Alice
	    for (j = 0; j < num_of_circuits; j++) {
               if (j != cc_num) {
                  SecPayload = (byte[]) vSecPayload.elementAt(j);
                  MyUtil.sendBytes (toAlice, SecPayload, false);
	       }
	    }
	    toAlice.flush();

            // Upload the secrets of the chosen circuit
            SecPayload = (byte[]) vSecPayload.elementAt(cc_num);
            c.cinjectSecPayload(SecPayload);

	    // Read Bob's inputs + update circuit accordingly
            f.getBobInput(c, br); 

            // Send Bob's inputs (garbled) to Alice
            InpPayload = f.fextractInpPayload(c, InpPayloadSize, false);
            MyUtil.sendBytes(toAlice, InpPayload, true);

            // OTs - Bob is the sender
            OT.SenderOTs(c, f, ot, toAlice, fromAlice);

	    // Get Bob garbled results from Alice & print them
            OutPayload = new byte[OutPayloadSize];
            MyUtil.receiveBytes (fromAlice, OutPayload, OutPayloadSize);
            f.finjectOutPayload (c, OutPayload, false);
            f.getBobOutput(c);
        }

        // Cleanup
      //  SS.close();
//	    if (closeSocketStream == true)
//	    {
//	    	toAlice.close();
//	        fromAlice.close();
//	    	sock.close();
	    	//System.out.println("Bob closing socket and stream");
//	    }
        //myhandlerbob.notify();
    }

    //---------------------------------------------------------------


	/**
     * This routine is for debugging socket communication
     */
    public void pingpong(ObjectOutputStream toAlice,
        ObjectInputStream fromAlice, int a) {
        System.out.println("Attempting to read num from Alice");

        int u = MyUtil.receiveInt(fromAlice);
        System.out.println("Got Int from Alice " + u);
        System.out.println("Sending " + a + " to Alice");
        MyUtil.sendInt(toAlice, a, true);
    }

    //---------------------------------------------------------------
 
    public static void bobUsage(int err_code) {
        System.out.println("Bob activation error code = " + err_code);
        System.out.println("Usage: java SFE.BOAL.Bob -e|-c[n]|-r[n] <filename> <seed> <ot_type>");
        System.out.println(" -e = EDIT, -c = COMPILE, -r = RUN, [n] = NoOpt)");
        System.out.println(" (<seed>, <ot_type> expected only with -r[n])") ;
        System.out.println(" Examples: 1. java SFE.Bob -c Maximum.txt");
        System.out.println("           2. java SFE.Bob -r Maximum.txt bQ91:d_aV!|l 4");
        System.exit(1);
    }

    //---------------------------------------------------------------

    /**
     * Main program for activating Bob
     *
     * @param args - command line arguments.
     *               args[0] should be -e, -c[n], -r[n]
     *               args[1] should be filename
     *               args[2] should be ot_type (only with -r[n])
     */
    public static void main(String[] args, BluetoothSocket sockBluetooth,Handler myhandler, String[] topic) throws Exception {
        String filename;
        String circ_fname;
        String fmt_fname;
	boolean edit = false;
	boolean compile = false;
	boolean run = false;
	boolean opt = false;

        // Load logging configuration file
	//PropertyConfigurator.configure("/sdcard/fairplay/SFE_logcfg.lcf");

	// Various legality tests on command line parameters

        if ((args.length < 2) || (args.length > 4))
            bobUsage(1);

	edit = args[0].equals("-e");
	compile = args[0].equals("-c") || args[0].equals("-cn");
	run = args[0].equals("-r") || args[0].equals("-rn");
	opt = args[0].equals("-r") || args[0].equals("-c");

	if (!edit && !compile && !run)
            bobUsage(2);

	if (run && (args.length < 4))
            bobUsage(3);

        filename = new String(args[1]);
	if (opt) {
           circ_fname = new String(filename + ".Opt.circuit");
           fmt_fname = new String(filename + ".Opt.fmt");
	}
	else {
           circ_fname = new String(filename + ".NoOpt.circuit");
           fmt_fname = new String(filename + ".NoOpt.fmt");
	}

	if (compile) {
            File f = new File(filename);

            if (!f.exists()) {
                System.out.println("Input file " + filename + " not found");
                bobUsage(4);
	    }
         }

         if (run) {
             File f1 = new File(circ_fname);
             File f2 = new File(fmt_fname);

             if (!f1.exists() || !f2.exists()) {
                if (!f1.exists())
                   System.out.println("Input file " + circ_fname + " not found");
                if (!f2.exists())
                   System.out.println("Input file " + fmt_fname + " not found");
                bobUsage(5);
	     }
         }

         // Do something (finally...)


         if (compile) {
        	 File f1 = new File(circ_fname);
             File f2 = new File(fmt_fname);
             
             //It deletes old compiled files
             if (f1.exists() || f2.exists())
             {
            	 f1.delete();
            	 f2.delete();
             }
             
             //It compiles new files
            SFECompiler.compile(filename, opt);
         }

         if (run) {
            System.out.println("Running Bob...");
            try {
            	Bob b = new Bob(circ_fname, fmt_fname, args[2], args[3], sockBluetooth, myhandler, topic);
            	
            	// System.exit(0);
            } catch (Exception e) {
                System.out.println("Bob's main err: " + e.getMessage());
                e.printStackTrace();
            }
         }
    }
}
