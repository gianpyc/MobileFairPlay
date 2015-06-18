/*
 *
a * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.BluetoothChat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.UUID;

import com.android.R;
import com.android.file.Read;
import com.android.file.Write;
import com.android.file.friendInfo;
import com.android.tab.TabSfida;

import SFE.BOAL.Alice;
import SFE.BOAL.Bob;
import android.R.string;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;

import java.util.Vector;
import java.io.FileInputStream;
import java.io.File;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.sql.Timestamp;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.text.SimpleDateFormat;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for incoming
 * connections, a thread for connecting with a device, and a thread for
 * performing data transmissions when connected.
 */
public class BluetoothChatService {
	// Debugging
	public static final String TAG = "BluetoothChatService";
	public static final boolean D = true;

	// Name for the SDP record when creating server socket
	public static final String NAME = "BluetoothChat";

	public static boolean autoPairing = false;
	
	public static Object sync;
	// Unique UUID for this application
	public static final UUID MY_UUID = UUID
			.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

	public final BluetoothAdapter mAdapter;
	public final Handler mHandler;
	public AcceptThread mAcceptThread;
	public ConnectThread mConnectThread;
	public ConnectedThread mConnectedThread;
	public int mState, type;
	private String message;

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming
	// connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
	// connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
	// This variable specifies is the devices is running the Alice's role
	public int isalice = 0;
	
	private String macAddressConnectedWith;

	/**
	 * Constructor. Prepares a new BluetoothChat session.
	 * 
	 * @param context
	 *            The UI Activity Context
	 * @param handler
	 *            A Handler to send messages back to the UI Activity
	 */
	public BluetoothChatService(Context context, Handler handler, int t) {
		sync = new Object();
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
		mHandler = handler;
		type = t;
	}

	/**
	 * Set the current state of the chat connection
	 * 
	 * @param state
	 *            An integer defining the current connection state
	 */
	public synchronized void setState(int state) {
		if (D)
			Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;

		// Give the new state to the Handler so the UI Activity can update
		mHandler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE, state, -1)
				.sendToTarget();
	}

	/**
	 * Return the current connection state.
	 */
	public synchronized int getState() {
		return mState;
	}

	/**
	 * Start the chat service. Specifically start AcceptThread to begin a
	 * session in listening (server) mode. Called by the Activity onResume()
	 */
	public synchronized void start() {
		if (D)
			Log.d(TAG, "start");

		// Cancel any thread attempting to make a connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to listen on a BluetoothServerSocket
		if (mAcceptThread == null) {
			mAcceptThread = new AcceptThread();
			mAcceptThread.start();
		}
		setState(STATE_LISTEN);
	}

	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 * 
	 * @param device
	 *            The BluetoothDevice to connect
	 */
	public synchronized void connect(BluetoothDevice device) {
		if (D)
			Log.d(TAG, "connect to: " + device);

		// Cancel any thread attempting to make a connection
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
		setState(STATE_CONNECTING);
	}

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * 
	 * @param socket
	 *            The BluetoothSocket on which the connection was made
	 * @param device
	 *            The BluetoothDevice that has been connected
	 */
	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device) {
		if (D)
			Log.d(TAG, "connected");

		// Cancel the thread that completed the connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Cancel the accept thread because we only want to connect to one
		// device
		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}

		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(socket, mHandler);
		mConnectedThread.start();

		// Send the name of the connected device back to the UI Activity
		Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(BluetoothChat.DEVICE_NAME, device.getName());
		msg.setData(bundle);
		mHandler.sendMessage(msg);
		
		// Send the MAC-address of the connected device back to the UI Activity
		Message msg1 = mHandler.obtainMessage(BluetoothChat.MESSAGE_DEVICE_MAC);
		bundle = new Bundle();
		bundle.putString(BluetoothChat.DEVICE_MAC, device.getAddress());
		msg1.setData(bundle);
		mHandler.sendMessage(msg1);
		
		macAddressConnectedWith = device.getAddress();
		setState(STATE_CONNECTED);
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		if (D)
			Log.d(TAG, "stop");
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}
		setState(STATE_NONE);
	}

	/*
	 * public void setMessageChatService(String msg) { message = msg; }
	 */
	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 * 
	 * @param out
	 *            The bytes to write
	 * @see ConnectedThread#write(byte[])
	 */
	public void write(byte[] out) {
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) {
			if (mState != STATE_CONNECTED)
				return;
			r = mConnectedThread;
		}
		// Perform the write unsynchronized
		r.write(out);
		System.out.println("Sending write: " + new String(out));
		// r.setMessageConnected(message);
	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	public void connectionFailed() {
		setState(STATE_LISTEN);

		// Send a failure message back to the Activity
		Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(BluetoothChat.TOAST, "Unable to connect device");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	public void connectionLost() {
		setState(STATE_LISTEN);

		// Send a failure message back to the Activity
		Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(BluetoothChat.TOAST, "Device connection was lost");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	/**
	 * This thread runs while listening for incoming connections. It behaves
	 * like a server-side client. It runs until a connection is accepted (or
	 * until cancelled). It is always run by Bob, which is the server
	 */
	public class AcceptThread extends Thread {
		// The local server socket
		public final BluetoothServerSocket mmServerSocket;

		public AcceptThread() {
			BluetoothServerSocket tmp = null;
			autoPairing = true;
			// Create a new listening server socket
			try {
				tmp = mAdapter
						.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
			} catch (IOException e) {
				Log.e(TAG, "listen() failed", e);
			}
			mmServerSocket = tmp;

		}

		public void run() {
			if (D)
				Log.d(TAG, "BEGIN mAcceptThread" + this);
			setName("AcceptThread");
			BluetoothSocket socket = null;

			// Listen to the server socket if we're not connected
			while (mState != STATE_CONNECTED) {
				try {
					// This is a blocking call and will only return on a
					// successful connection or an exception
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					Log.e(TAG, "accept() failed", e);
					break;
				}

				
				// The variable is isAlice is set to 0 because this role is taken by Bob
				isalice = 0;
				
				// If a connection was accepted
				if (socket != null) {

					synchronized (BluetoothChatService.this) {
						switch (mState) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							// Situation normal. Start the connected thread.
							connected(socket, socket.getRemoteDevice());

							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							// Either not ready or already connected. Terminate
							// new socket.
							try {
								socket.close();
							} catch (IOException e) {
								Log.e(TAG, "Could not close unwanted socket", e);
							}
							break;
						}
					}
				}
			}
			if (D)
				Log.i(TAG, "END mAcceptThread");
		}

		public void cancel() {
			if (D)
				Log.d(TAG, "cancel " + this);
			try {
				mmServerSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of server failed", e);
			}
		}
	}

	/**
	 * This thread runs while attempting to make an outgoing connection with a
	 * device. It runs straight through; the connection either succeeds or
	 * fails. The connection is always run by the device that wants to connect to another one. For this reason it takes the Alice's role
	 */
	public class ConnectThread extends Thread {
		public final BluetoothSocket mmSocket;
		public final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			mmDevice = device;
			BluetoothSocket tmp = null;

			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				Log.e(TAG, "create() failed", e);
			}
			mmSocket = tmp;

		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectThread");
			setName("ConnectThread");

			// Always cancel discovery because it will slow down a connection
			mAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				mmSocket.connect();
				// chiamata Alice
				isalice = 1;
			} catch (IOException e) {
				connectionFailed();
				// Close the socket
				try {
					mmSocket.close();
				} catch (IOException e2) {
					Log.e(TAG,
							"unable to close() socket during connection failure",
							e2);
				}
				// Start the service over to restart listening mode
				BluetoothChatService.this.start();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (BluetoothChatService.this) {
				mConnectThread = null;
			}

			// Start the connected thread
			connected(mmSocket, mmDevice);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}

	/**
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions. It is both run by Bob and Alice
	 */
	public class ConnectedThread extends Thread {
		public final BluetoothSocket mmSocket;
		public final InputStream mmInStream;
		public final OutputStream mmOutStream;
		
		private final String SFE_MESSAGE = "SFE";
		private final String FILE_MESSAGE = "FILE";
		private final String FILE_ACK = "FILEACK";
		private final String USER_PIC_ACK = "UPICACK";
		private final String FILE_SEND_END = "FEND";
		private final String USER_DETAILS = "USER";
		private final String USER_PIC = "UPIC";
		
		private String[] topicChallengeAlice;
		
		// This is the number of challenges run by Bob and Alice
		static final int iterazioni = 4;
		
		// This vector contains the topics chosen by BOb to challenge Alce
		private String[] topicChallengeBob = new String[iterazioni];
		 //This is the vector that containes all possible topics used to do the challanges
		private Vector topicvector = new Vector();
		
		//This is the string containing all topics that Bob sends to Alice
		private String sendTopic = "";

		private String message;
		public String separatore = ";";
		
		//It is the charather that splits the right topics stream with trash data
		public String handshake = "<>";
		
		private int tempInt = 0;
		
		// It checks if the interest cast has been done
		private boolean interestCastDid = false;
		
		//It says if the protocol, considering also sending files procedure, is completed
		private boolean endProtocol = false;
		
		File dirFiles = new File(Environment.getExternalStorageDirectory().getPath()+"/interest/files");
		File[] filesIntoFiles;
		Vector filesToSend = new Vector();
		
		// This string is sent when all files have been sent to the other device
		String filesEndHeader = handshake + FILE_SEND_END + handshake + "$$$$$$$$$$$$$$$$$$$$" + handshake;
		
		byte[] tempBuffer;
		
		String nameFileToWrite;
		
		//This variable counts all files received by Alice
		private int alicefileReceived = 0;
		
		// This variable specifies whether Bob and alice after the protocol result friends or not
		private boolean areFriend = false;
		
		private ProgressBar prograssBarFile;
		
		//Here it collectes all file received from Bob in order to write them into a file
		private Vector listFileReceived = new Vector();
		private Vector listFileSent = new Vector();
		
		private int StepCompleted = 0;
		private String Name, Surname;
		
		private boolean receiveOtherUserInfo = false;
		private SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(BluetoothChat.context);	
		
		
		public ConnectedThread(BluetoothSocket socket, Handler mHandler) {
			Log.d(TAG, "create ConnectedThread");
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
				
				// Initialising the topics vecotr
				Collections.addAll(topicvector, "Music", "Cinema", "Sport",
						"Book", "Animals", "Cars", "MotorBikes", "Technology",
						"Televison", "Travelling");

			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created", e);
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
			
			
		}

		private SecureRandom random = new SecureRandom();

		public String nextSessionId() {
			return new BigInteger(4, random).toString(32);
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectedThread");
			byte[] buffer = new byte[1024];
			int bytes;
			String headerReceived = "";
			
			try {
				/*
				 * if ((message == null)) {
				 * System.out.println("ERROR: message is null");
				 * //System.exit(-1); } else {
				 */
				switch (isalice) {
				case 0:
					// This part is run by Bob
					System.out.println("STATO CONNESSO BOB!!! ");


					// synchronized (sync) {
					// System.out.println("READY " + TabSfida.isReady());
					// while (!TabSfida.isReady()) {
					// System.out.println("WAIT");
					// sync.wait();
					// System.out
					// .println("thread svegliato, valore ready: "
					// + TabSfida.isReady());
					// }
					//
					// }

					// This method randomnly selects the topics used to challenge BOb 	
					setopic();
					
					System.out.println("BOB Primo Topic: "+ topicChallengeBob[0]);
					
					//This is the header of the packet
					sendTopic = handshake + SFE_MESSAGE + handshake;
					// It creates a single string to send to Alice
					for (int i = 0; i < topicChallengeBob.length; i++) {
						sendTopic = sendTopic + topicChallengeBob[i]
								+ separatore;
					}

					sendTopic = sendTopic + handshake + " ";
					
					// It pops up a window with the topic selected
					mHandler.obtainMessage(BluetoothChat.SEND,
							sendTopic.length(), -1, sendTopic).sendToTarget();
					

					//It creates a random string to be used by Bob main
					String randombob = nextSessionId();
					
					Thread.sleep(300);
					
					long startTime = System.currentTimeMillis();
				
					//Running Bob...
					Bob.main(new String[] {
							"-r",
							Environment.getExternalStorageDirectory().getPath()
									+ "/interest/config/InterestPlus4for.txt",
							randombob, "4" }, mmSocket, mHandler,
							topicChallengeBob);
					
					
					long endTime = System.currentTimeMillis();				
					// It calculates the execution time					
					long time = endTime - startTime;
					//Thread.sleep(300);
					System.out.println("Tempo Bob = " + time);
					
					//It pops up the Bob execution time
					mHandler.obtainMessage(BluetoothChat.TIMERUN,
							("" + time).length(), -1, ("" + time))
							.sendToTarget();

					sendTopic = "";
					
					if (this.areFriend == true)
					{	
						// It gets all files in the directory /files/...
						filesIntoFiles = dirFiles.listFiles();
						
						// This procedure does not consider all hidden files into the direcotry
						for (int k=0; k < filesIntoFiles.length ; k++)
						{

							if (!filesIntoFiles[k].isHidden())
								filesToSend.add(filesIntoFiles[k].getName());
						}
						int fileSent = 0;

						if (filesToSend.size() > 0)
						{
							//sendFileNew((String) filesToSend.get(fileSent));

							sendFileNewWithSize("/interest/files/", (String) filesToSend.get(fileSent), false);

							while ((true) && (endProtocol == false)) {
								try {
									// Read from the InputStream
									if (receiveOtherUserInfo == false)									
										bytes = mmInStream.read(buffer);
									else
										bytes = mmInStream.read(buffer,0,(handshake + FILE_MESSAGE + handshake + "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" + handshake + "$$$$$$$$$$" + handshake).length());
									byte bufferReceived[] = new byte[1024];
									int len;
									String fileBufferMessage; //= new String(bufferReceived, 0, bytes);
									
									String readMessage = new String(buffer, 0, bytes);
									System.out.println("Buffer got: "+readMessage);
									
									String[] tmp = readMessage.split(handshake);

									//This variable will contain the cleaned stream
									String bufferCleaned = "";
									System.out.println("Bob buffer lungo: "+ tmp.length);

									// The stream has arrived in the form <>HEADER<>topic1;topic2;topic3;...<>
									//Only the middle part is used because it contains all topics

									headerReceived = tmp[1];


									if (headerReceived.equalsIgnoreCase(FILE_ACK))
									{
										fileSent++; 
										if ( fileSent < filesToSend.size())  // It checks if all files in the directory has been sent
										{
											System.out.println("Bob: ACK received");
											System.out.println("Bob: Sending new file: "+(String) filesToSend.get(fileSent));

											//sendFileNew((String) filesToSend.get(fileSent));
											sendFileNewWithSize("/interest/files/", (String) filesToSend.get(fileSent),false);
										}
										else
										{
											StepCompleted = 1;
										}
										
										if (StepCompleted == 1)//It sends name and surname to the other device
										{
											String nameComplete = "";
											if (settings.getString("profile_name","").equalsIgnoreCase("") || (settings.getString("profile_name","") == null))
												nameComplete = "NoName ";
											else
												nameComplete = settings.getString("profile_name","") + " ";
												
											if (settings.getString("profile_surname","").equalsIgnoreCase("") || (settings.getString("profile_surname","") == null))
												nameComplete = nameComplete.concat("NoSurname");
											else
												nameComplete = nameComplete.concat(settings.getString("profile_surname",""));
											
											String tmpNameComplete = nameComplete;
											for (int i= tmpNameComplete.length(); tmpNameComplete.length() < 50; i++)
												tmpNameComplete = tmpNameComplete.concat("$");
											System.out.println("Stringa lunga: "+nameComplete.length());
											
											String sendUserInfo = handshake + USER_DETAILS + handshake + tmpNameComplete + handshake + "$$$$$$$$$$" + handshake ;

											System.out.println("Buffer User Info: "+sendUserInfo.length());
											mmOutStream.write(sendUserInfo.getBytes(),0,sendUserInfo.length());
											StepCompleted = 2;
										}
										if (StepCompleted == 2)//It sends name and surname to the other device
										{
											
											System.out.println("Bob: Sending new file: "+(String) "mario.jpeg");

											//sendFileNew((String) filesToSend.get(fileSent));
											sendFileNewWithSize(null,settings.getString("profile_img",""),true);
											
											receiveOtherUserInfo = true;
									
										}
			
									}
									else if (headerReceived.equalsIgnoreCase(USER_DETAILS))
									{
										String tmpNameCompleted = tmp[2];
										
										tmpNameCompleted = tmpNameCompleted.replace("$", "");
										
										Name = tmpNameCompleted.split(" ")[0];
										Surname = tmpNameCompleted.split(" ")[1];
										
										System.out.println("Name received: "+ Name + " Surname: "+ Surname);
										
									}			
									else if (headerReceived.equalsIgnoreCase(USER_PIC))
									{
										nameFileToWrite = tmp[2];
										String sizeFileToWrite = tmp[3];

										nameFileToWrite = nameFileToWrite.replace("$", "");
										sizeFileToWrite = sizeFileToWrite.replace("$", "");
										System.out.println("Filename to write: "+ nameFileToWrite + "with length: "+ sizeFileToWrite);
										
										this.receiveFileNewWithSize("/interest/config/",nameFileToWrite, sizeFileToWrite,true,true);
										
									}	
									else if (headerReceived.equalsIgnoreCase(FILE_SEND_END))
									{
										mmOutStream.write(filesEndHeader.getBytes(),0,filesEndHeader.length());
										endProtocol = true;

										String fileSentTo;

										if (fileSent > 1)
										{
											fileSentTo = fileSent+" out of "+filesToSend.size()+" files have been sent";
											mHandler.obtainMessage(BluetoothChat.FILECOPIED,(fileSentTo).length(), -1, fileSentTo).sendToTarget();
										}
										else
										{
											fileSentTo = fileSent+" out of "+filesToSend.size()+" file has been sent";
											mHandler.obtainMessage(BluetoothChat.FILECOPIED,(fileSentTo).length(), -1, fileSentTo).sendToTarget();
										}
										
										String fileFriendMac = mmSocket.getRemoteDevice().getAddress();
										String fileFriendDeviceName = mmSocket.getRemoteDevice().getName();
										String fileFriendMacCleaned = fileFriendMac.replace(":", "");
										//System.out.println("Mac: "+mmSocket.getRemoteDevice().getAddress()+" Mac: "+macAddressConnectedWith);
										java.util.Date date= new java.util.Date();

										String timestamp = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss").format(new Timestamp(date.getTime()));
										String connection = ">>> Connection did: ";
										connection = connection.concat(timestamp);
										connection = connection.concat("\n>>>File Sent:");
										listFileSent.add(0,connection);
										listFileSent.remove(listFileSent.lastElement()); //it removes last element because it is the image of the user profile
										Write FilesSentFile = new Write(listFileSent,"/interest/config/"+fileFriendMacCleaned+".txt",true);

										Read read = new Read("/interest/config/mac.txt");
										friendInfo friendToWrite = new friendInfo(fileFriendDeviceName,fileFriendMac,timestamp);
										//Write FilesReceivedFile = new Write(listFileReceived,"/interest/config/"+macAddressConnectedWith+".txt", true)
										if (Name != null)
											friendToWrite.setName(Name);
										
										if (Surname != null)
											friendToWrite.setSurname(Surname);
										
										//SISTEMARE QUESTAAAAAA
										if (listFileReceived.size() == 1)
											friendToWrite.setPic("/interest/config/"+(String)listFileReceived.lastElement());
										
										Write friendfile = new Write();
										if (!read.findMacFriend("/interest/config/friends.txt",fileFriendMac)) {

											friendfile.writeSingleFriend(friendToWrite,"/interest/config/friends.txt", true);
										}
										else
										{
											friendfile.replaceSingleFriend(friendToWrite,"/interest/config/friends.txt");
										}
										friendfile.removeSinglePerson(friendToWrite,"/interest/config/Nofriends.txt");
									}

								} catch (Exception e) {
									e.printStackTrace();
									Log.e(TAG, "disconnected", e);
									connectionLost();
									break;
								}
							}
						}
						else
						{
							// If there are no files Bob sends the files end information
							mmOutStream.write(filesEndHeader.getBytes(),0,filesEndHeader.length());
							endProtocol = true;

							String fileSentTo = "No files to send";
							mHandler.obtainMessage(BluetoothChat.FILECOPIED,(fileSentTo).length(), -1, fileSentTo).sendToTarget();
							
							String fileFriendMac = mmSocket.getRemoteDevice().getAddress();
							String fileFriendDeviceName = mmSocket.getRemoteDevice().getName();
							String fileFriendMacCleaned = fileFriendMac.replace(":", "");
							//System.out.println("Mac: "+mmSocket.getRemoteDevice().getAddress()+" Mac: "+macAddressConnectedWith);
							java.util.Date date= new java.util.Date();

							String timestamp = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss").format(new Timestamp(date.getTime()));
							String connection = ">>> Connection did: ";
							connection = connection.concat(timestamp);
							connection = connection.concat("\nNo files received");
							listFileSent.add(0,connection);
							
							Write FilesSentFile = new Write(listFileSent,"/interest/config/"+fileFriendMacCleaned+".txt",true);

							Read read = new Read("/interest/config/mac.txt");
							friendInfo friendToWrite = new friendInfo(fileFriendDeviceName,fileFriendMac,timestamp);
							//Write FilesReceivedFile = new Write(listFileReceived,"/interest/config/"+macAddressConnectedWith+".txt", true)
							if (Name != null)
								friendToWrite.setName(Name);
							
							if (Surname != null)
								friendToWrite.setSurname(Surname);
							
							//SISTEMARE QUESTAAAAAA
							if (listFileReceived.size() == 1)
								friendToWrite.setPic("/interest/config/"+(String)listFileReceived.lastElement());
							
							Write friendfile = new Write();
							if (!read.findMacFriend("/interest/config/friends.txt",fileFriendMac)) {

								friendfile.writeSingleFriend(friendToWrite,"/interest/config/friends.txt", true);
							}
							else
							{
								friendfile.replaceSingleFriend(friendToWrite,"/interest/config/friends.txt");
							}
							friendfile.removeSinglePerson(friendToWrite,"/interest/config/Nofriends.txt");
							
						}
					}
					
					break;

				case 1:
					// This part is run by Alice
					System.out.println("STATO CONNESSO ALICE!!! ");

					String topicInput = "";
					// Keep listening to the InputStream while connected
					
					
					while ((true) && (endProtocol == false)) {
						
						try {
							if (interestCastDid == false)
							{
								// Read from the InputStream
								bytes = mmInStream.read(buffer);
							}
							else 
							{
								// Read from the InputStream
								//System.out.println("Lunghezza del buffer: "+(handshake + FILE_MESSAGE + handshake).length());
								if (areFriend == true)
									bytes = mmInStream.read(buffer,0,(handshake + FILE_MESSAGE + handshake + "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" + handshake + "$$$$$$$$$$" + handshake).length());
								else 
								{
									break;
								}
								//bytes = mmInStream.read(buffer,0,8);
							}
							// It creates the string containing all topics chosen by Bob
							String readMessage = new String(buffer, 0, bytes);
							
							System.out.println("Alice buffer sporco: "	+ readMessage);
							// The streaming is split
							String[] tmp = readMessage.split(handshake);
							
							//This variable will contain the cleaned stream
							String bufferCleaned = "";
							System.out.println("Alice buffer lungo: "+ tmp.length);
							
							// The stream has arrived in the form <>HEADER<>topic1;topic2;topic3;...<>
							//Only the middle part is used because it contains all topics
							 
//							if (tmp.length == 4)
//							{
								headerReceived = tmp[1];
							//}
							if (headerReceived.equalsIgnoreCase(SFE_MESSAGE))
							{

								bufferCleaned = tmp[2];


								System.out.println("Arriva: " + bufferCleaned);

								// Send the obtained bytes to the UI Activity
								mHandler.obtainMessage(BluetoothChat.MESSAGE_READ,
										bufferCleaned.length(), -1, bufferCleaned)
										.sendToTarget();

								//TopicInput is like: topic1;topic2;topic3;
								topicInput = bufferCleaned;

								System.out.println("Alice buffer: " + topicInput);

								// For each array's cell a tipic is inserted
								topicChallengeAlice = topicInput.split(this.separatore);
								// IF there are topics, than Alice will start
								if (topicChallengeAlice != null) {


									startTime = System.currentTimeMillis();

									//Random String used by Alice
									String randomalice = nextSessionId();

									//Running Alice
									Alice.main(
											new String[] {
													"-r",
													Environment
													.getExternalStorageDirectory()
													.getPath()
													+ "/interest/config/InterestPlus4for.txt",
													randomalice, "127.0.0.1" },
													mmSocket, mHandler, topicChallengeAlice);

									endTime = System.currentTimeMillis();					

									time = endTime - startTime;

									System.out.println("Tempo Alice = "	+ time);

									//It pops up the execution time
									mHandler.obtainMessage(BluetoothChat.TIMERUN,("" + time).length(), -1, ("" + time)).sendToTarget();

									// if (i == (topicChallengeAlice.length -1))
									// {
									// //After closing Stramn we need to reinitiate
									// the variable to flase for further interaction
									// Alice.closeSocketStream = false;
									// }
									// }

									System.out.println("First part completed");
									interestCastDid = true;
								}
							}
							else if (headerReceived.equalsIgnoreCase(FILE_MESSAGE))
							{
								try
								{
									nameFileToWrite = tmp[2];
									String sizeFileToWrite = tmp[3];

									nameFileToWrite = nameFileToWrite.replace("$", "");
									sizeFileToWrite = sizeFileToWrite.replace("$", "");
									System.out.println("Filename to write: "+ nameFileToWrite + "with length: "+ sizeFileToWrite);
									
									this.receiveFileNewWithSize("/interest/files/",nameFileToWrite, sizeFileToWrite,false,false);
																		

	
								}
								catch (Exception e){}
							}
							else if (headerReceived.equalsIgnoreCase(USER_DETAILS))
							{
								String tmpNameCompleted = tmp[2];
								
								tmpNameCompleted = tmpNameCompleted.replace("$", "");
								
								Name = tmpNameCompleted.split(" ")[0];
								Surname = tmpNameCompleted.split(" ")[1];
								
								System.out.println("Name received: "+ Name + " Surname: "+ Surname);
								
							}			
							else if (headerReceived.equalsIgnoreCase(USER_PIC))
							{
								nameFileToWrite = tmp[2];
								String sizeFileToWrite = tmp[3];

								nameFileToWrite = nameFileToWrite.replace("$", "");
								sizeFileToWrite = sizeFileToWrite.replace("$", "");
								System.out.println("Filename to write: "+ nameFileToWrite + "with length: "+ sizeFileToWrite);

								// QUI ALICE MANCA PICACK CHE BOB NN SA INTERPRETARE
								this.receiveFileNewWithSize("/interest/config/",nameFileToWrite, sizeFileToWrite,true,false);
								String nameComplete = "";
								
								if (settings.getString("profile_name","").equalsIgnoreCase("") || (settings.getString("profile_name","") == null))
									nameComplete = "NoName ";
								else
									nameComplete = settings.getString("profile_name","") + " ";
									
								if (settings.getString("profile_surname","").equalsIgnoreCase("") || (settings.getString("profile_surname","") == null))
									nameComplete = nameComplete.concat("NoSurname");
								else
									nameComplete = nameComplete.concat(settings.getString("profile_surname",""));
								
								// nameComplete = settings.getString("profile_name","") + " "+ settings.getString("profile_surname","");
								String tmpNameComplete = nameComplete;
								for (int i= tmpNameComplete.length(); tmpNameComplete.length() < 50; i++)
									tmpNameComplete = tmpNameComplete.concat("$");
								System.out.println("Stringa lunga: "+nameComplete.length());

								String sendUserInfo = handshake + USER_DETAILS + handshake + tmpNameComplete + handshake + "$$$$$$$$$$" + handshake ;

								System.out.println("Buffer User Info: "+sendUserInfo.length());
								mmOutStream.write(sendUserInfo.getBytes());
								mmOutStream.flush();
								System.out.println("Alice: Sending new file: "+(String) "mario.jpeg");

								//sendFileNew((String) filesToSend.get(fileSent));
								
								
								//String pathImage = settings.getString("profile_img","").replace(oldChar, newChar)
								sendFileNewWithSize(null,settings.getString("profile_img",""),true);
								Thread.sleep(500);
								mmOutStream.write(filesEndHeader.getBytes(),0,filesEndHeader.length());


							}	
							else if (headerReceived.equalsIgnoreCase(FILE_SEND_END)) // If all files have been sent
							{
								try {
									
									endProtocol = true;
									String fileCopied;
									
									
									if (alicefileReceived > 1)
									{
										fileCopied = alicefileReceived+ " files have been received";
										mHandler.obtainMessage(BluetoothChat.FILECOPIED,(fileCopied).length(), -1, fileCopied).sendToTarget();
									}
									else
									{
										fileCopied = alicefileReceived+ " files has been received";
										mHandler.obtainMessage(BluetoothChat.FILECOPIED,(fileCopied).length(), -1, fileCopied).sendToTarget();
									}
									
									String fileFriendMac = mmSocket.getRemoteDevice().getAddress();
									String fileFriendDeviceName = mmSocket.getRemoteDevice().getName();
									String fileFriendMacCleaned = fileFriendMac.replace(":", "");
									//System.out.println("Mac: "+mmSocket.getRemoteDevice().getAddress()+" Mac: "+macAddressConnectedWith);
									java.util.Date date= new java.util.Date();

									String timestamp = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss").format(new Timestamp(date.getTime()));
									String connection = ">>> Connection did: ";
									connection = connection.concat(timestamp);
									if (alicefileReceived == 0)
										connection = connection.concat("\nNo Files Received");
									else
										connection = connection.concat("\n>>>File Received:");
									

									Read read = new Read("/interest/config/mac.txt");
									friendInfo friendToWrite = new friendInfo(fileFriendDeviceName,fileFriendMac,timestamp);
									//Write FilesReceivedFile = new Write(listFileReceived,"/interest/config/"+macAddressConnectedWith+".txt", true)
									if (Name != null)
										friendToWrite.setName(Name);
									
									if (Surname != null)
										friendToWrite.setSurname(Surname);
									
									if (listFileReceived.size() > (alicefileReceived)) //This means that last Element is the UserPic
										friendToWrite.setPic("/interest/config/"+(String)listFileReceived.lastElement());
									
									listFileReceived.add(0,connection);
									
									if (alicefileReceived > 0) // It does not consider the user Pic in the log
										listFileReceived.remove(listFileReceived.lastElement());
									
									Write FilesReceivedFile = new Write(listFileReceived,"/interest/config/"+fileFriendMacCleaned+".txt",true);
									
									Write friendfile = new Write();
									if (!read.findMacFriend("/interest/config/friends.txt",fileFriendMac)) {

										friendfile.writeSingleFriend(friendToWrite,"/interest/config/friends.txt", true);
									}
									else
									{
										friendfile.replaceSingleFriend(friendToWrite,"/interest/config/friends.txt");
									}
									friendfile.removeSinglePerson(friendToWrite,"/interest/config/Nofriends.txt");
								} catch (Exception e) {
									e.printStackTrace();
									Log.e(TAG, "disconnected", e);
									connectionLost();
									break;
								}

							}
							

						} catch (Exception e) {
							e.printStackTrace();
							Log.e(TAG, "disconnected", e);
							connectionLost();
							break;
						}
					}
					
					
					
					break;
				}
				connectionLost();
				
				

				// Keep listening to the InputStream while connected

				// while ((true) && (interest)) {
				// try {
				// // Read from the InputStream
				// bytes = mmInStream.read(buffer);
				//
				// } catch (IOException e) {
				// Log.e(TAG, "disconnected", e);
				// connectionLost();
				// break;
				// }
				// }
			} catch (Exception e) {
				Log.e(TAG, "Error Running Alice or Bob", e);
				e.printStackTrace();
			}
			autoPairing = false;

		}
		
		public void setAreFriend(boolean b)
		{
			this.areFriend = b;
		}
		
		
	
		public void receiveFileNewWithSize(String path, String nomefile, String filesize, Boolean pic, Boolean sendEndSession)  {
			String sendingACK = handshake + FILE_ACK + handshake;
			String sendingPIC_ACK = handshake + USER_PIC_ACK + handshake;
			int current = 0;
			int bytesRead = 0;
			int progress = 0;
			
			
			try
			{
				File file;
				
					do
					{
						file = new File(Environment.getExternalStorageDirectory().getPath()+ path+nomefile);

						if (file.exists())
						{
							int mid= nomefile.lastIndexOf(".");
							String fname=nomefile.substring(0,mid);
							String ext=nomefile.substring(mid+1,nomefile.length());
							fname = fname.concat("_N.");
							nomefile = fname;
							nomefile = nomefile.concat(ext);
							//nomefile = nomefile.copyValueOf("_LAST".g, nomefile.length()-4, "_LAST".length());
						}


						file = new File(Environment.getExternalStorageDirectory().getPath()+ path +nomefile);

					} while (file.exists());
				

				BufferedOutputStream bufferedOutput = new BufferedOutputStream(new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+ path +nomefile));
				

				//FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+ "/interest/files/"+nomeFile);
				
				int fileLength = Integer.parseInt(filesize);
				
				byte[] mybytearray = new byte[fileLength];
				
				

				bytesRead = mmInStream.read(mybytearray, 0, mybytearray.length);
				
				System.out.println("Alice Buffer letto: "+ new String(mybytearray, 0, mybytearray.length));

				current = bytesRead;
				
	        	
	        	progress = ((current * 100) / fileLength);
	        	
	        	mHandler.obtainMessage(BluetoothChat.SETTEXTSENDRECEIVED,("Receiving "+ nomefile + " ("+progress +" %)").length(), -1, "Receiving "+ nomefile + " ("+progress +" %)").sendToTarget();
	        	prograssBarFile.setProgress((int) progress);
	        	
	        	do {
	        		
	        		if (mmInStream.available() > 0)
	        		{
	        			
	        			bytesRead = mmInStream.read(mybytearray, current, (mybytearray.length-current));
	        			
	        			if (bytesRead >= 0) 
	        				current += bytesRead;
	        		}
	        		else
	        			bytesRead = -1;

	        		progress = ((current * 100) / fileLength);
	        		mHandler.obtainMessage(BluetoothChat.SETTEXTSENDRECEIVED,("Receiving "+ nomefile + " ("+progress +" %)").length(), -1, "Receiving "+ nomefile + " ("+progress +" %)").sendToTarget();
	        		prograssBarFile.setProgress((int) progress);

	        	} while ((bytesRead > -1) || (current < fileLength));

				bufferedOutput.write(mybytearray, 0 , current);
				bufferedOutput.flush();

				bufferedOutput.close();

			
				System.out.println("File closed");



				//It sends the ACK after receiving the file
				if ((pic == false) && (sendEndSession == false))
				{
					this.write(sendingACK.getBytes());
					alicefileReceived++;
				}
				if ((pic == true) && (sendEndSession == false))
					//this.write(sendingPIC_ACK.getBytes());
				if ((pic == true) && (sendEndSession == true))
					this.write(filesEndHeader.getBytes());
				
				listFileReceived.add(nomefile);
				

			} catch (Exception e) {
				Log.e(TAG, "Error receiving a file", e);
				e.printStackTrace();
			}
		}
		
		
		
		public void sendFileNewWithSize(String Path, String filename, Boolean userPic)
		{
	
		
			if (filename.length() < 50)
			{
				try
				{
					File myFile;
					if (Path != null)
						myFile = new File(Environment.getExternalStorageDirectory().getPath()+ Path +filename);
					else
					{
						File pathImg = new File(filename);
						String name = pathImg.getName();
						String localPath = pathImg.getAbsolutePath().substring(0,pathImg.getAbsolutePath().lastIndexOf(File.separator));
						
						System.out.println("Name: "+name+" Path:" + localPath);
						myFile = new File(localPath + File.separator + name);
						filename = name;
					}
					
					String tmpFileName = filename;
					String tmpFileSize;
					
					prograssBarFile.setProgress(0);

					// The filename field contains at maximum filename long 50 char with the extention
					for (int i= tmpFileName.length(); tmpFileName.length() < 50; i++)
						tmpFileName = tmpFileName.concat("$");
					System.out.println("Stringa lunga: "+filename.length());


				
					tmpFileSize = ""+ (int) myFile.length();
					// It check whether the file is bigger than 100KB
					//if ((int) myFile.length() < 100000)
					{
						System.out.println("File size lungo prima: "+tmpFileSize.length());

						// The filesize field contains at maximum file of dimension long 9999999999 bit, mainly less than 10GBl
						if (tmpFileSize.length() <= 10)
							for (int i= tmpFileSize.length(); tmpFileSize.length() < 10; i++)
								tmpFileSize = tmpFileSize.concat("$");
						System.out.println("File size lungo dopo: "+tmpFileSize.length());
						// dimension 50 bytes
						String sendingTest = "";
						if (userPic == false)
							sendingTest = handshake + FILE_MESSAGE + handshake + tmpFileName + handshake + tmpFileSize + handshake;
						else if (userPic == true)
						{
							sendingTest = handshake + USER_PIC + handshake + tmpFileName + handshake + tmpFileSize + handshake;
							//mmOutStream.flush();
						}
						
						System.out.println("Buffer lungo: "+sendingTest.length());
						mmOutStream.write(sendingTest.getBytes(),0,sendingTest.length());
						
						
//						byte[] mybytearray = new byte[(int) myFile.length()];
						BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
//
//						bis.read(mybytearray, 0, mybytearray.length);
//
//						mmOutStream.write(mybytearray, 0, mybytearray.length);
						
						int bufferSize = 1024;
				        byte[] buffer = new byte[bufferSize];

				          // we need to know how may bytes were read to write them to the byteBuffer
				          int len = 0;
				          long count = 0;
				          long progress = 0;
				          long fileLength = myFile.length();
				          
				          while ((len = bis.read(buffer)) != -1) {
				        	  mmOutStream.write(buffer, 0, len);
				        	  count = count + len;
				        	  progress = ((count* 100) / fileLength);
				        	  
				        	  mHandler.obtainMessage(BluetoothChat.SETTEXTSENDRECEIVED,("Sending "+ filename + " ("+progress +" %)").length(), -1, "Sending "+ filename + " ("+progress +" %)").sendToTarget();
				        	  prograssBarFile.setProgress((int) progress);
				          }

						//System.out.println("Bob Buffer scritto: "+ new String(mybytearray, 0, mybytearray.length) + " length: "+ mybytearray.length);

						mmOutStream.flush();
						listFileSent.add((String)filename);
					}
//					else
//					{
//						System.out.println("ERROR: filensize bigger than 100KB");
//					}	

				} catch (Exception e) {
					Log.e(TAG, "Error sending files", e);
					e.printStackTrace();
				}
			}
			else
			{
				System.out.println("ERROR: filename too long");
			}
		}
		
	
	

		
		public void setProgressBarAndTextView(ProgressBar pb)
		{
			prograssBarFile = pb;
		}

		
		// This methods randomically selects a number of different topic used by Bob to challenge Alice
		public void setopic() {
			Vector tmp = new Vector();

			boolean inserted = false;
			int i = 0, j = 0;
			// for(int j=0; j < 2; j++)
			System.out.println("Set: ");
			
			try {

				while (j < iterazioni) {
					int y = (int) (Math.random() * (10 - 1));
					for (i = 0; i < tmp.size(); i++) {
						System.out.println("Dentro il for i:" + i);
						if ((Integer) tmp.get(i) == y) {
							inserted = true;
						}

					}

					if ((inserted == false) || (tmp.size() == 0)) {

						topicChallengeBob[j] = (String) topicvector.get(y);
						System.out.println("insert: "
								+ (String) topicvector.get(y));
						j++;
						tmp.add(y);
					}
					inserted = false;

				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			for (i = 0; i < topicChallengeBob.length; i++) {
				System.out.println("Bob Topic " + i + " :"
						+ topicChallengeBob[i]);
			}
			// return topicChallengeBob;
		}

		/**
		 * Write to the connected OutStream.
		 * 
		 * @param buffer
		 *            The bytes to write
		 */
		public void write(byte[] buffer) {
			
			try {
				mmOutStream.write(buffer);

				// Share the sent message back to the UI Activity
				mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1,
						buffer).sendToTarget();

			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
			}
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}
}
