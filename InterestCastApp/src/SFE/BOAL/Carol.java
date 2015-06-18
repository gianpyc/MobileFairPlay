// Carol.java - measuring WAN communication speed versus Dave.
// Copyright (C) 2004 Dahlia Malkhi, Yaron Sella. 
// See full copyright license terms in file ../GPL.txt

package SFE.BOAL;

import java.io.*;

import java.math.*;

import java.net.*;

import java.util.regex.*;

import SFE.BOAL.*;

import SFE.Compiler.*;


/** Carol
 *  @author: Dahlia Malkhi and Yaron Sella
 */

//---------------------------------------------------------------

/**
 * This class implements Carol - intended for measuring WAN 
 * communication speed versus Dave, where Carol is the client
 * who connects to Dave (like Alice), and Dave is the server
 * who performs accept (like Bob).
 *
 * @author Dahlia Malkhi and Yaron Sella.
 */
public class Carol {

    //---------------------------------------------------------------

    /**
     * Carol Constructor
     *
     * @param args - command line arguments.
     *  args[0] - message length in bytes
     *  args[1] - number of iterations to perform
     *  args[2] - host name where Dave is
     */
    public Carol (String[] args) throws Exception {
        Socket sock = null;
        ObjectInputStream fromDave = null;
        ObjectOutputStream toDave = null;
        int mess_len = Integer.parseInt(args[0]);
        int num_iterations = Integer.parseInt (args[1]);
        int i;
        byte[] mess = new byte[mess_len];

        // Connect to Dave
        try {
            sock = new Socket(args[2], 3496);
            fromDave = new ObjectInputStream(sock.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Carol: Don't know host " + args[2]);
            System.exit(-1);
        } catch (IOException e) {
            System.err.println(
                "Carol: Couldn't get I/O for the connection with Dave");
            System.exit(-1);
        }

        for (i = 0 ; i < num_iterations ; i++)
           MyUtil.receiveBytes(fromDave, mess, mess_len);
        System.out.println("Done!");

        // Cleanup
        fromDave.close();
        sock.close();
    }

    //---------------------------------------------------------------
 
    public static void carolUsage() {
        System.out.println("Usage: java SFE.BOAL.Carol <mess_len> <num_iter> <hostname");
        System.out.println("(message length is measured in bytes)");
        System.out.println("Example: java SFE.BOAL.Carol 10000 100 sands.cs.huji.ac.il") ;
        System.exit(1);
    }

    //---------------------------------------------------------------

    /**
     * Main program for activating Carol
     *
     * @param args - command line arguments.
     *  args[0] - message length in bytes
     *  args[1] - number of iterations to perform
     *  args[2] - host name where Dave is
     */
    public static void main(String[] args) throws Exception {

	// Various legality tests on command line parameters

        if (args.length != 3) carolUsage();

        System.out.println("Running Carol...");
        try {
            Carol c = new Carol(args);
        } catch (Exception e) {
            System.out.println("Carol's main err: " + e.getMessage());
            e.printStackTrace();
	}
    }
}
