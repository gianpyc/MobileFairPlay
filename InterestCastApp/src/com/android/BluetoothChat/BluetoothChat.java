/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

import java.text.SimpleDateFormat;
import java.util.Vector;

import com.android.R;

import com.android.file.*;
import com.android.tab.*;

import SFE.BOAL.Alice;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ProgressBar;

import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.view.ViewGroup;
import java.io.IOException;
import java.sql.Timestamp;
/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends Activity {
	public static int i;
	// Debugging
	private static final String TAG = "BluetoothChat";
	private static final boolean D = true;
	private String deviceMAC;
	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_POPINPUT = 6;
	public static final int VINCITORE = 7;
	public static final int PERDENTE = 8;
	public static final int TOPIC = 9;
	public static final int SFIDATO = 10;
	public static final int SEND = 11;
	public static final int TIMERUN = 12;
	public static final int MESSAGE_DEVICE_MAC = 13;
	public static final int FILECOPIED = 14;
	public static final int SETTEXTSENDRECEIVED = 15;
	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	public static final String DEVICE_MAC = "device_mac";
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	public static Context context;
	
	/*
	 * numero inviato public static String messagechat; public String getmessage
	 * (){ return message;}
	 */
	public String TopicSent;

	public String handshake = "<>";

	// Layout Views
	private TextView mTitle;
	private ListView mConversationView;
	private EditText mOutEditText;
	private Button mSendButton;
	private ProgressBar mProgressBarFile;
	
	private TextView mSendReceiveFile;
	
	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	// private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;
	
	//List in the home of devices that are friends
	private ListView pairedListView = null;
	

	//list of my friends loaded from a file
	private Vector PeopleList = null;
	private ArrayAdapter<String> mListFriendsArrayAdapter;
	
	private boolean showPopUp = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");
		context = getBaseContext();
		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			ensureDiscoverable();
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
//				onDestroy();
//				setupChat();
				//mChatService.start();
				mChatService.start();
			
				//ensureDiscoverable();
			}
		}
	}

	public static String message;

	public void setupChat() {
		Log.d(TAG, "setupChat()");
		// startActivity(new Intent( BluetoothChat.this,BobActivity.class));
		// Initialize the array adapter for the conversation thread
		// mConversationArrayAdapter = new ArrayAdapter<String>(this,
		// R.layout.message);
		// mConversationView = (ListView) findViewById(R.id.in);
		// mConversationView.setAdapter(mConversationArrayAdapter);

		// Initialize the compose field with a listener for the return key
		// mOutEditText = (EditText) findViewById(R.idEdit.numberDecimal);
		// mOutEditText.setOnEditorActionListener(mWriteListener);

		// Initialize the send button with a listener that for click events
		// mSendButton = (Button) findViewById(R.id.button_send);
		// startActivity(new Intent( BluetoothChat.this, BobActivity.class));
		// mSendButton.setOnClickListener(new OnClickListener() {
		// public void onClick(View v) {
		// showDialog(1);

		// check selezionato per avviare la sfida sull'interesse invio
		// attraverso la socket

		// TopicSent=TabSfida.getopic();

		// });

		// messagechat=getmessage();

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler, this
				.getIntent().getIntExtra("type", 0));

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
		ensureDiscoverable();
		mProgressBarFile = (ProgressBar) findViewById(R.id.progressSendFile);
		mSendReceiveFile = (TextView) findViewById(R.id.SendReceiveFile);
		mProgressBarFile.setVisibility(ProgressBar.INVISIBLE);
		mListFriendsArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);
		
		
		
		
		
		ListView friendsListView = (ListView) findViewById(R.id.list_friends_devices);
		friendsListView.setOnItemClickListener(mDeviceDetailsClickListener);
		
		
	
		
		Read read = new Read();
		PeopleList = new Vector();
		PeopleList = read.readFriends("/interest/config/friends.txt");
		friendsListView.setAdapter(mListFriendsArrayAdapter);
		
		for (int i=0 ; i < PeopleList.size(); i++)
		{
			mListFriendsArrayAdapter.add((String)PeopleList.get(i));
		
		}
// It read also no friends devices
		
		PeopleList = read.readFriends("/interest/config/Nofriends.txt");
		friendsListView.setAdapter(mListFriendsArrayAdapter);
		for (int i=0 ; i < PeopleList.size(); i++)
		{
			mListFriendsArrayAdapter.add((String)PeopleList.get(i));
		}
		
	}

	
	// The on-click listener for all devices in the ListViews
	private OnItemClickListener mDeviceDetailsClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

			try
			{
				//it gets the text selected
				String textSelected = ((TextView) v).getText().toString();

				//it filters only the macAddress
				String macAddress = textSelected.split(" ")[1];
				macAddress.replace("\n", "");
				//It removes all ":" char
				String macAddressFile = macAddress.replace(":", "");
				Bundle bundle = new Bundle();
				//At this point it passes the macAddressFile to the new activity
				bundle.putString("macAddress", macAddress);
				bundle.putString("macAddressFile", macAddressFile);

				Intent friendDetailsActivity = new Intent(BluetoothChat.this, connectionDetailsActivity.class);
				friendDetailsActivity.putExtras(bundle);
				startActivity(friendDetailsActivity);

			}
			catch (Exception e) {
				e.printStackTrace();

			}


		}

	};
	
	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 600);
			startActivity(discoverableIntent);
		}
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	public void sendMessageB(String topic) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Check that there's actually something to send
		if (topic.length() > 0) {
			// if(topic.contains("Fumetti")){
			// topic="Fumetti";
			// }
			// if(topic.contains("Calcio"))
			// {
			// topic="Calcio";
			// }
			// if(topic.contains("Moto"))
			// {
			// topic="Moto";
			// }
			// if(topic.contains("Auto"))
			// {
			// topic="Auto";
			// }
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = topic.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
			// mOutEditText.setText(mOutStringBuffer);
		}

	}

	/*
	 * // The action listener for the EditText widget, to listen for the return
	 * TextView.OnEditorActionListener() { public boolean
	 * onEditorAction(TextView view, int actionId, KeyEvent event) { // If the
	 * action is a key-up event on the return key, send the message if (actionId
	 * == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
	 * String message = view.getText().toString(); sendMessage(message); } if(D)
	 * Log.i(TAG, "END onEditorAction"); return true; } };
	 */
	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Vector macvector;
			Vector readmacvector2;
			Vector readmacvector;
			// final AlertDialog.Builder alert = new AlertDialog.Builder(this);
			// final EditText input = new EditText(this);
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
				

					// mConversationArrayAdapter.clear();
					break;
				case BluetoothChatService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				System.out.println("Bob topic selezionati" + writeMessage);
				/*
				 * Dialog locationError2 = new
				 * AlertDialog.Builder(BluetoothChat.this)
				 * .setIcon(0).setTitle("").setPositiveButton( R.string.ok,
				 * null).setMessage(
				 * "Il tuo rivale ti sfida su"+writeMessage).create();
				 * locationError2.show();
				 */

				// mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:
				// byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = (String) msg.obj;

				if (showPopUp == true)
				{
					System.out.println("il tuo messaggio è" + readMessage);
					Dialog locationError3 = new AlertDialog.Builder(
							BluetoothChat.this).setIcon(0).setTitle("")
							.setPositiveButton(R.string.ok, null)
							.setMessage("Topics required by " + mConnectedDeviceName +": " + readMessage)
							.create();
					locationError3.show();
				}
				// mConversationArrayAdapter.add(mConnectedDeviceName+":  " +
				// readMessage);
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				// deviceMAC = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_DEVICE_MAC:
				// save the connected device's name

				deviceMAC = msg.getData().getString(DEVICE_MAC);

				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_LONG)
						.show();
				break;
			case VINCITORE:
				// byte[] topic = (byte[]) msg.obj;

				mChatService.mConnectedThread.setAreFriend(true);
				mChatService.mConnectedThread.setProgressBarAndTextView(mProgressBarFile);
				mProgressBarFile.setVisibility(ProgressBar.VISIBLE);
				String topicMsg = (String) msg.obj;
				if (showPopUp == true)
				{
					Dialog locationError = new AlertDialog.Builder(
							BluetoothChat.this)
					.setIcon(0)
					.setTitle("")
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

						// do something when the button is clicked
						public void onClick(DialogInterface arg0, int arg1) {
							onDestroy();
							finish();
						}
					})
					.setMessage(
							"You got similar interest with " + mConnectedDeviceName)
							.create();
					locationError.show();
				}

				String friend = "Device_friend=" + deviceMAC;
				System.out.println("device friend =" + deviceMAC);

				Read read = new Read("/interest/config/mac.txt");
				readmacvector = new Vector();
				readmacvector = read.readmac("/interest/config/mac.txt");
				
				
				
				if (!(mConnectedDeviceName.equalsIgnoreCase("")) && (!deviceMAC.equalsIgnoreCase("")))
				{
					macvector = new Vector();
					java.util.Date date= new java.util.Date();
				//	friendInfo friendToWrite = new friendInfo(mConnectedDeviceName,deviceMAC,(new Timestamp(date.getTime())));
					macvector.add(mConnectedDeviceName + "=" + deviceMAC);
					
					//This check is done on mac.txt file
					boolean macalreadyadded = false;
					for (int i = 0; i < readmacvector.size(); i++) {
						if (deviceMAC.equalsIgnoreCase((String) readmacvector
								.get(i))) {

							macalreadyadded = true;
						}
					}

					if (!macalreadyadded) {
						Write macfile = new Write(macvector,
								"/interest/config/mac.txt", true);
						
						
						
					}
				
					
//					Write friendfile = new Write();
//					if (!read.findMacFriend("/interest/config/friends.txt",deviceMAC)) {
//		
//						friendfile.writeSingleFriend(friendToWrite,"/interest/config/friends.txt", true);
//					}
//					else
//					{
//						friendfile.replaceSingleFriend(friendToWrite,"/interest/config/friends.txt");
//					}

				}
				break;
			case PERDENTE:
			
				mChatService.mConnectedThread.setAreFriend(false);
				mChatService.mConnectedThread.setProgressBarAndTextView(mProgressBarFile);
				mProgressBarFile.setVisibility(ProgressBar.INVISIBLE);
				
				if (showPopUp == true)
				{
					String topicMsg1 = (String) msg.obj;
					Dialog locationError2 = new AlertDialog.Builder(
							BluetoothChat.this)
					.setIcon(0)
					.setTitle("")
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

						// do something when the button is clicked
						public void onClick(DialogInterface arg0, int arg1) {
							onDestroy();
							finish();
						}
					})
					.setMessage(
							"You did NOT get similar with "
							+ mConnectedDeviceName).create();
					locationError2.show();
				}
				// String friend="Device_friend="+deviceMAC;
				// Writemac macwriter = new
				// Writemac(friend,"/interest/config/mac.txt");
				Read reader = new Read("/interest/config/mac.txt");
				readmacvector2 = new Vector();
				readmacvector2 = reader.readmac("/interest/config/mac.txt");

				boolean macremove = false;
				for (int i = 0; i < readmacvector2.size(); i++) {
					if (deviceMAC.equalsIgnoreCase((String) readmacvector2
							.get(i))) {
						readmacvector2.remove(i);
						macremove = true;
					}
				}

				if (macremove) {

					Write macfile = new Write(readmacvector2,
							"/interest/config/mac.txt", false);
				}
				
				String fileNoFriendMac = deviceMAC;
				String fileNoFriendDeviceName = mConnectedDeviceName;
				String fileNoFriendMacCleaned = fileNoFriendMac.replace(":", "");
				//System.out.println("Mac: "+mmSocket.getRemoteDevice().getAddress()+" Mac: "+macAddressConnectedWith);
				java.util.Date date= new java.util.Date();

				String timestamp = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss").format(new Timestamp(date.getTime()));
				String connection = ">>> Connection did: ";
				connection = connection.concat(timestamp);
				connection = connection.concat("\nYou did NOT get similar with "+ mConnectedDeviceName);
				
				friendInfo NofriendToWrite = new friendInfo(fileNoFriendDeviceName,fileNoFriendMac,timestamp);
				
				Vector logTemp = new Vector();
				logTemp.add(connection);
				
				Write FilesReceivedFile = new Write(logTemp,"/interest/config/"+fileNoFriendMacCleaned+".txt",true);
				
				
				Write friendfile = new Write();
				if (!reader.findMacFriend("/interest/config/Nofriends.txt",fileNoFriendMac)) {

					friendfile.writeSingleFriend(NofriendToWrite,"/interest/config/Nofriends.txt", true);
					
				}
				else
				{
					friendfile.replaceSingleFriend(NofriendToWrite,"/interest/config/Nofriends.txt");
				}
				
				friendfile.removeSinglePerson(NofriendToWrite,"/interest/config/friends.txt");
				
				onDestroy();
				setupChat();
				mChatService.start();
				
				break;
			case TOPIC:
				// String tmp = null;
				Intent myIntent = new Intent(BluetoothChat.this, TabSfida.class);
				// myIntent.putExtra("handler", mHandler);
				startActivityForResult(myIntent, SEND);

				break;
			case SEND:

				String topicMessage = (String) (msg.obj);
				System.out.println("Sending: " + topicMessage);
				sendMessageB(topicMessage);

				break;

			case TIMERUN:
				String timerun = (String) (msg.obj);
				if (showPopUp == true)
				{
					Dialog locationError4 = new AlertDialog.Builder(
							BluetoothChat.this).setIcon(0).setTitle("")
							.setPositiveButton(R.string.ok, null)
							.setMessage("Time needed to run: " + timerun + " sec.").create();
					locationError4.show();
				}
				break;
			case FILECOPIED:
				String filecopeid = (String) (msg.obj);
//				Dialog locationError5 = new AlertDialog.Builder(
//						BluetoothChat.this).setIcon(0).setTitle("")
//						.setPositiveButton(R.string.ok, null).setMessage(filecopeid).create();
//				locationError5.show();
				
				Dialog locationError = new AlertDialog.Builder(
						BluetoothChat.this)
				.setIcon(0)
				.setTitle("")
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

					// do something when the button is clicked
					public void onClick(DialogInterface arg0, int arg1) {
//						onDestroy();
//						finish();
						//The following three methods are called to reactive the bluetooth for listening to incoming connection
						mSendReceiveFile.setText("");
						onDestroy();
						setupChat();
						mChatService.start();
					}
				})
				.setMessage(filecopeid).create();
				locationError.show();
				
				
				
				break;
			case SETTEXTSENDRECEIVED:
				String fileSendReceived = (String) (msg.obj);
				mSendReceiveFile.setText(fileSendReceived);
				
				break;	
				
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				deviceMAC = address;
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				mChatService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occured
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();

			}

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scan:
			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		}
		return false;
	}

}
