package com.android.BluetoothChat;

import java.io.File;
import java.util.Vector;

import com.android.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;
import com.android.file.*;

public class connectionDetailsActivity extends Activity{


	private TextView deviceName;
	private TextView deviceMacAddress;
	private TextView userName;
	private TextView userSurName;
	private EditText logConnection;
	private ImageView userPic;

	private Read readPersonDetails;
	private friendInfo personDetails;
	private Vector log;
	@Override
	public void onCreate(Bundle savedInstanceState) {          

		super.onCreate(savedInstanceState);    
		setContentView(R.layout.connection_details);
		Bundle bundle = this.getIntent().getExtras();

		String macAddressFile = bundle.getString("macAddressFile");
		String macAddress = bundle.getString("macAddress");


		System.out.println("Filename: "+macAddressFile);
		deviceName = (TextView) findViewById(R.id.TextDeviceName);
		deviceMacAddress = (TextView) findViewById(R.id.textDeviceMac);
		userName = (TextView) findViewById(R.id.textDeviceUsername);
		userSurName = (TextView) findViewById(R.id.textDeviceSurname);
		logConnection = (EditText) findViewById(R.id.editPastConnection);

		//         logConnection.setScroller(new Scroller(this.getBaseContext())); 
		//         //logConnection.setMaxLines(10); 
		//         logConnection.setVerticalScrollBarEnabled(true); 
		//         logConnection.setMovementMethod(new ScrollingMovementMethod()); 

		userPic = (ImageView) findViewById(R.id.imageUserPic);

		userPic.setMaxHeight(100);


		logConnection.setEnabled(false);
		readPersonDetails = new Read();
		try
		{
			personDetails = readPersonDetails.getSingleFriend("/interest/config/friends.txt", macAddress);
			if (personDetails == null) // Otherwise the person is not in the friend list
				personDetails = readPersonDetails.getSingleFriend("/interest/config/Nofriends.txt", macAddress);

			if (!personDetails.getDeviceName().equalsIgnoreCase(""))
				deviceName.setText("> "+personDetails.getDeviceName());

			if (!personDetails.getMacAddress().equalsIgnoreCase(""))
				deviceMacAddress.setText("> "+personDetails.getMacAddress());

			if (personDetails.getName() != null)
				userName.setText("> "+personDetails.getName());

			if (personDetails.getSurname() != null)
				userSurName.setText("> "+personDetails.getSurname());

			if (personDetails.getPic() != null)
			{
				File sd_card = Environment.getExternalStorageDirectory();
				File imgFile = new  File(sd_card,personDetails.getPic());
				if(imgFile.exists())
				{
					Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());       	     
					userPic.setImageBitmap(myBitmap);
				}
			}


			log = readPersonDetails.getConnectionLog("/interest/config/"+macAddressFile+".txt");
			if (log != null)
			{
				String line;
				for (int i=0; i < log.size(); i++)
				{
					line = (String) log.get(i);
					if (line.startsWith(">>>"))
						logConnection.append((String) "\n");
					logConnection.append((String) log.get(i)+"\n");
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();

		}

	}


}
