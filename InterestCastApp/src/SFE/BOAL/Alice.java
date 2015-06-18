// Alice.java - Alice's part of the 2-party SFE protocol. 
// Copyright (C) 2004 Dahlia Malkhi, Yaron Sella. 
// See full Copyright license terms in file ../GPL.txt

package SFE.BOAL;

import java.io.*;

import java.math.*;

import java.net.*;

import java.util.*;

import java.util.regex.*;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import SFE.BOAL.*;

import SFE.Compiler.*;

/** Alice
 *  @author: Dahlia Malkhi and Yaron Sella
 */

//---------------------------------------------------------------

/**
 * This class implements Alice - the chooser in the two-party SFE protocol.
 * 
 * @author Dahlia Malkhi and Yaron Sella.
 */
public class Alice {
	private static final int num_of_circuits = 2;
	public Formatter f = null;

	private String message = "";
	private Handler myhandleralice = null;

	public static boolean closeSocketStream = false;

	// ---------------------------------------------------------------

	/**
	 * Alice Constructor
	 * 
	 * @param circuit_filename
	 *            - circuit filename
	 * @param fmt_filename
	 *            - format filename
	 * @param hostname
	 *            - where to find Bob
	 * @param num_iterations
	 *            - how many iterations to do
	 * @param stats
	 *            - print run statistics in the end
	 */
	public Alice(String circuit_filename, String fmt_filename, String sseed,
			BluetoothSocket SocketB, int num_iterations, boolean stats,
			Handler myhandler, String[] tp) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		myhandleralice = myhandler;

		int i, j;
		int ot_type;
		int cc_num;
		int[] bob_io_size = new int[2];
		Parser p = null;
		OT ot;
		BluetoothSocket sock = null;
		ObjectInputStream fromBob = null;
		ObjectOutputStream toBob = null;
		Vector bob_results;
		long sum1 = 0, sum2 = 0, sum3 = 0, sum4 = 0;

		// Preparations
		MyUtil.init(sseed);

		// Connect to Bob
		try {
			// sock = new Socket(hostname, 3496);
			sock = SocketB;
			fromBob = new ObjectInputStream(sock.getInputStream());
			toBob = new ObjectOutputStream(sock.getOutputStream());
		} catch (UnknownHostException e) {
			System.err.println("Alice: Don't know host ");
			System.exit(-1);
		} catch (IOException e) {
			System.err
					.println("Alice: Couldn't get I/O for the connection with Bob");
			e.printStackTrace();
			System.exit(-1);
		}

		MyUtil.sendInt(toBob, (num_iterations << 8) + num_of_circuits, true);
		ot_type = MyUtil.receiveInt(fromBob);
		ot = new OT(ot_type);

		Vector vEncPayload = new Vector(num_of_circuits);
		byte[] EncPayload;
		byte[] SecPayload;
		byte[] InpPayload;
		byte[] OutPayload;
		int EncPayloadSize = 0;
		int SecPayloadSize = 0;
		int InpPayloadSize;
		int OutPayloadSize;
		Circuit c;

		for (i = 0; i < num_iterations; i++) {

			MyUtil.deltaTime(true);

			System.out.println("Iteration no = " + i);
			// Parse the IOformat file and prepare the inputs
			try {
				// Preparations
				FileReader fmt = new FileReader(fmt_filename);
				StreamTokenizer fmtst = new StreamTokenizer(fmt);

				// IO Formatting
				f = new Formatter(fmtst, myhandleralice, tp);
				f.parse();

				// Cleanup
				fmt.close();
			} catch (IOException e) {
				System.err.println("Alice: cannot open/close " + fmt_filename
						+ " - " + e.getMessage());
			} catch (FormatterError e) {
				System.err.println("Alice: parsing " + fmt_filename
						+ " failed.");
			} catch (Exception e) {
				System.err.println("Alice: exception - " + e.getMessage());
			}

			// Parse the circuit file
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
				System.err.println("Alice: cannot open/close "
						+ circuit_filename + " - " + e.getMessage());
				System.exit(1);
			} catch (Exception e) {
				System.err.println("Alice: exception - " + e.getMessage());
				System.exit(1);
			}

			c = p.getCircuit(); // Obtain a circuit object
			f.markIO(c, bob_io_size); // Mark its inputs & outputs
			InpPayloadSize = bob_io_size[0];
			OutPayloadSize = bob_io_size[1];
			c.generateEncCircuit(); // Encrypt it (dummy)
			EncPayloadSize = c.cmeasureEncPayload();
			SecPayloadSize = c.cmeasureSecPayload();

			sum1 += MyUtil.deltaTime(false);

			// Run the SFE protocol
			// ====================

			// Receive encrypted circuits payload from Bob
			for (j = 0; j < num_of_circuits; j++) {
				EncPayload = new byte[EncPayloadSize];
				MyUtil.receiveBytes(fromBob, EncPayload, EncPayloadSize);
				vEncPayload.add(EncPayload);
			}

			// Choose a circuit to evaluate and tell Bob
			cc_num = MyUtil.randomByte();
			if (cc_num < 0)
				cc_num += 256;
			cc_num = cc_num % num_of_circuits;
			System.out.println("Alice: chose circuit number " + cc_num
					+ " for evaluation");
			MyUtil.sendInt(toBob, cc_num, true);

			// Receive encrypted circuits with secrets
			// (except the chosen one) from Bob
			for (j = 0; j < num_of_circuits; j++) {
				if (j != cc_num) {
					EncPayload = (byte[]) vEncPayload.elementAt(j);
					c.cinjectEncPayload(EncPayload);
					SecPayload = new byte[SecPayloadSize];
					MyUtil.receiveBytes(fromBob, SecPayload, SecPayloadSize);
					c.cinjectSecPayload(SecPayload);
					if (!c.isCorrect()) {
						System.err.println("Alice: caught Bob cheating!");
						System.exit(1);
					}
				}
			}

			// Receive Bob's inputs for the chosen circuit and place them in it
			InpPayload = new byte[InpPayloadSize];
			MyUtil.receiveBytes(fromBob, InpPayload, InpPayloadSize);
			f.finjectInpPayload(c, InpPayload, false);

			sum2 += MyUtil.deltaTime(false);

			// Read Alice's inputs
			f.getAliceInput(c, br);

			// OTs - Alice is the chooser +
			// place Alice's inputs in the chosen circuit
			OT.ChooserOTs(c, f, ot, toBob, fromBob);

			sum3 += MyUtil.deltaTime(false);

			c.evalGarbledCircuit(true, false);
			System.out.println("circuit evaluation completed!");

			// Send Bob his garbled results
			OutPayload = f.fextractOutPayload(c, OutPayloadSize, false);
			MyUtil.sendBytes(toBob, OutPayload, true);

			// print Alice's output
			f.getAliceOutput(c);

			sum4 += MyUtil.deltaTime(false);
		}

		if (stats) {
			System.out.println("Initial calculations   [sum1] = "
					+ (float) sum1 / 1000.0);
			System.out.println("Circuits communication [sum2] = "
					+ (float) sum2 / 1000.0);
			System.out.println("Oblivious Transfers    [sum3] = "
					+ (float) sum3 / 1000.0);
			System.out.println("Evaluation & output    [sum4] = "
					+ (float) sum4 / 1000.0);
		}

		// Cleanup

		// if (closeSocketStream==true)
		// {
		//toBob.close();
		//fromBob.close();
		//sock.close();
		//System.out.println("Alice closing socket and stream");
		// }

	}

	// ---------------------------------------------------------------

	/**
	 * This routine is for debugging socket communication
	 */
	public void pongping(ObjectOutputStream toBob, ObjectInputStream fromBob,
			int a) {
		System.out.println("Sending " + a + " to Bob");
		MyUtil.sendInt(toBob, a, true);
		System.out.println("Attempting to read num from Bob");

		int u = MyUtil.receiveInt(fromBob);
		System.out.println("Got Int from Bob " + u);
	}

	// ---------------------------------------------------------------

	public static void aliceUsage(int err_code) {
		System.out.println("Alice activation error code = " + err_code);
		System.out
				.println("Usage: java SFE.BOAL.Alice -e|-c[n]|-r[n] <filename> <seed> <hostname> <num_iterations>");
		System.out.println(" -e = EDIT, -c = COMPILE, -r = RUN, [n] = NoOpt)");
		System.out
				.println(" (<seed> <hostname>, <num_iterations> expected only with -r[n])");
		System.out.println(" Examples: 1. java SFE.Alice -c Maximum.txt");
		System.out
				.println("           2. java SFE.Alice -r Maximum.txt Xb@&5H1m!p sands 100");
		System.exit(1);
	}

	// ---------------------------------------------------------------

	/**
	 * Main program for activating Alice
	 * 
	 * @param args
	 *            - command line arguments. args[0] should be -e, -c[n] or -r[n]
	 *            args[1] should be filename args[2] should be seed for RNG
	 *            args[3] should be hostname (only with -r[n]) args[4] should be
	 *            number of iterations (only with -r[n])
	 * 
	 */

	public static void main(String[] args, BluetoothSocket sockBluetooth,
			Handler myhandler, String[] topic) throws Exception {
		String filename;
		String circ_fname;
		String fmt_fname;
		int num_iterations;
		boolean edit = false;
		boolean compile = false;
		boolean run_stats = false;
		boolean run = false;
		boolean opt = false;

		// Various legality tests on command line parameters

		if ((args.length < 2) || (args.length > 5))
			aliceUsage(1);

		edit = args[0].equals("-e");
		compile = args[0].equals("-c") || args[0].equals("-cn");
		run_stats = args[0].equals("-s");
		run = run_stats || args[0].equals("-r") || args[0].equals("-rn");
		opt = args[0].equals("-r") || args[0].equals("-c")
				|| args[0].equals("-s");

		if (!edit && !compile && !run && !run_stats)
			aliceUsage(2);

		if (run && (args.length < 4))
			aliceUsage(3);

		filename = new String(args[1]);
		if (opt) {
			circ_fname = new String(filename + ".Opt.circuit");
			fmt_fname = new String(filename + ".Opt.fmt");
		} else {
			circ_fname = new String(filename + ".NoOpt.circuit");
			fmt_fname = new String(filename + ".NoOpt.fmt");
		}

		if (compile) {
			File f = new File(filename);

			if (!f.exists()) {
				System.out.println("Input file " + filename + " not found");
				aliceUsage(4);
			}
		}

		if (run) {
			File f1 = new File(circ_fname);
			File f2 = new File(fmt_fname);

			if (!f1.exists() || !f2.exists()) {
				if (!f1.exists())
					System.out.println("Input file " + circ_fname
							+ " not found");
				if (!f2.exists())
					System.out
							.println("Input file " + fmt_fname + " not found");
				aliceUsage(5);
			}
		}

		// Do something (finally...)

		if (compile) {
			File f1 = new File(filename + ((opt) ? ".Opt" : ".NoOpt")
					+ ".circuit");
			File f2 = new File(filename + ((opt) ? ".Opt" : ".NoOpt") + ".fmt");

			// It deletes old compiled files
			if (f1.exists() || f2.exists()) {
				if (f1.delete() && f2.delete())
					System.out.println("Deleting old compiled files!");

			}
			// It compiles new files
			SFECompiler.compile(filename, opt);
		}

		if (run) {
			System.out.println("Running Alice...");
			try {
				if (args.length < 5)
					num_iterations = 1;
				else
					num_iterations = Integer.parseInt(args[4]);

				Alice a = new Alice(circ_fname, fmt_fname, args[2],
						sockBluetooth, num_iterations, run_stats, myhandler,
						topic);

				// System.exit(0);

			} catch (Exception e) {
				System.out.println("Alice's main err: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
