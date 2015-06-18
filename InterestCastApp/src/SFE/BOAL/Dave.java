// Dave.java - measuring LAN/WAN communication speed versus Carol.
// Copyright (C) 2004 Dahlia Malkhi, Yaron Sella. 
// See full copyright license terms in file ../GPL.txt

package SFE.BOAL;

import java.io.*;

import java.math.*;

import java.net.*;

import java.util.regex.*;

import SFE.BOAL.*;

import SFE.Compiler.*;


/** Dave
 *  @author: Dahlia Malkhi and Yaron Sella
 */

//---------------------------------------------------------------   

/**
 * This class implements Dave - intended for measuring LAN/WAN 
 * communication speed versus Carol, where Dave is the server
 * who performs accept (like Bob), and Carol is the client
 * who connects to Dave (like Alice). 
 *
 * @author Dahlia Malkhi and Yaron Sella.
 */
public class Dave {

    //---------------------------------------------------------------   

    /**
     * Dave Constructor
     *
     * @param args - command line arguments.
     *  args[0] - message length in bytes
     *  args[1] - number of iterations to perform
     */
    public Dave (String args[]) throws IOException {
        ServerSocket SS = null;
        Socket sock = null;
        ObjectInputStream fromCarol = null;
        ObjectOutputStream toCarol = null;
        int mess_len = Integer.parseInt(args[0]);
        int num_iterations = Integer.parseInt (args[1]);
        int i;
	byte[] dumm = new byte[mess_len];

        // Establish connection with Carol
        try {
            SS = new ServerSocket(3496); // create a server socket
            System.out.println("Dave: waiting for Carol to connect");
            sock = SS.accept(); // Accept Carol on this socket
            toCarol = new ObjectOutputStream(sock.getOutputStream());
        } catch (IOException e) {
            System.out.println(
                "Dave: establishing connection with Carol failed: " +
                e.getMessage());
            System.exit(-1);
        }
     
        for (i = 0 ; i < num_iterations ; i++) {
	   MyUtil.sendBytes (toCarol, dumm, true);
           toCarol.reset();
        }

	toCarol.close();
	sock.close();
	SS.close();
    }

    //---------------------------------------------------------------
 
    public static void daveUsage() {
        System.out.println("Usage: java SFE.BOAL.Dave <mess_len> <num_iter>");
        System.out.println("(message length is measured in bytes)");
        System.out.println("Example: java SFE.BOAL.Dave 10000 100") ;
        System.exit(1);
    }

    //---------------------------------------------------------------

    /**
     * Main program for activating Dave
     *
     * @param args - command line arguments.
     *  args[0] - message length in bytes
     *  args[1] - number of iterations to perform
     */
    public static void main(String[] args) throws Exception {


	// Various legality tests on command line parameters

        if (args.length != 2)
            daveUsage();

        System.out.println("Running Dave...");

        try {
             Dave d = new Dave(args);
        } catch (Exception e) {
             System.out.println("Dave's main err: " + e.getMessage());
             e.printStackTrace();
        }
    }
}

