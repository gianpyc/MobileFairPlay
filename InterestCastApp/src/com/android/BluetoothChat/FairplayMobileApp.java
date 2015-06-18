package com.android.BluetoothChat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.io.InputStream;
import java.io.OutputStream;

import com.android.R;
import com.android.file.Read;
import com.android.file.Write;
import com.android.tab.About_us;
import com.android.tab.TabBarManger;
import com.android.tab.TabInteressi;

import SFE.BOAL.Alice;
import SFE.BOAL.Bob;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
//import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.res.AssetManager;
import com.android.profile.*;

public class FairplayMobileApp extends Activity {
	private static ProgressDialog pd;
	/* Get Default Adapter */
	//private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter(); 

	/* request BT enable */
	private static final int REQUEST_ENABLE = 0x1;
	/* request BT discover */
	private static final int REQUEST_DISCOVERABLE = 0x2;

	private static int countCompiling = 0;
	
	private File sd = Environment.getExternalStorageDirectory();
	private File f = new File(sd, "/interest/config/interessi.txt");

	public void openMyDialog(View view) {
		showDialog(10);
	}

	public int i = 0;
	public static Vector interessi = new Vector();
	String separatore = "=";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.benvenuto);
		creaDir();
		
		//copyFileToCompile();
		copyFileToCompile("InterestPlus4for.txt.Opt.circuit",getResources().openRawResource(R.raw.interestplus4fortxtoptcircuit));
		copyFileToCompile("InterestPlus4for.txt.Opt.fmt",getResources().openRawResource(R.raw.interestplus4fortxtoptfmt));
		
		if (f.exists()) {
			interessi = Read.getInterest("/interest/config/interessi.txt");
		

		}
		
		if (countCompiling == 0) {
			try {
				System.out.println("Starti compiling...!!! ");
				long startTime = System.currentTimeMillis();
//				Bob.main(new String[] {
//						"-c",
//						Environment.getExternalStorageDirectory().getPath()
//								+ "/interest/config/InterestPlus4for.txt" },
//						null, null, null);
//				File fileSourceCompiling = new File(Environment.getExternalStorageDirectory()
//						.getPath() + "/interest/config/" + "InterestPlus4for.txt");
//				if (fileSourceCompiling.exists())
//				{
//					System.out.println("Deleting source file! ");
//					fileSourceCompiling.delete();
//				}
				
				countCompiling++;
				long endTime = System.currentTimeMillis();
				System.out.println("Tempo Bob = " + (endTime - startTime));
				long time = endTime - startTime;
//				TextView view = (TextView) findViewById(R.id.time);
//				view.setText("Time required to start: " + time + " millisec");
			} catch (Exception e) {
				System.out.println("Cannot compiling actors" + e);
			}

		}

		Button chat = (Button) findViewById(R.id.chat);
		chat.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// showDialog(1);
				sd = Environment.getExternalStorageDirectory();
				f = new File(sd, "/interest/config/interessi.txt");
				if (f.exists()) {
				
					startActivity(new Intent(FairplayMobileApp.this,
							BluetoothChat.class));

				}
				else
					startActivity(new Intent(FairplayMobileApp.this, TabInteressi.class));
				
			}

		});

		Button configurazione = (Button) findViewById(R.id.config);
		configurazione.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				startActivity(new Intent(FairplayMobileApp.this, TabInteressi.class));
			}

		});
		
		Button userProfile = (Button) findViewById(R.id.userProfile);
		userProfile.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				startActivity(new Intent(FairplayMobileApp.this, ProfileActivity.class));
			}

		});
		
		
		Button aboutus = (Button) findViewById(R.id.aboutus);
		aboutus.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				startActivity(new Intent(FairplayMobileApp.this, About_us.class));
			}

		});
	}

	private void creaDir() {
		try {
			String sd = Environment.getExternalStorageDirectory().getPath()
					+ "/interest/";
			File folderConfig = new File(sd, "config");
			File folderSFE = new File(sd, "sfe");
			File folderFILES = new File(sd, "files");

			if (!folderConfig.exists())
			{
				folderConfig.mkdirs();
				startActivity(new Intent(FairplayMobileApp.this,
						TabInteressi.class));
			}
			
			if (!folderSFE.exists())
				folderSFE.mkdirs();
			
			if (!folderFILES.exists())
				folderFILES.mkdirs();
				

			
		} catch (Exception e) {
			System.err.println();
		}
	}

	private void copyFileToCompile() {
		try {
			// Open your local db as the input stream
			InputStream myInput = getResources().openRawResource(
					R.raw.interestplus4for);

			// Path to the just created empty db
			String outFileName = Environment.getExternalStorageDirectory()
					.getPath() + "/interest/config/" + "InterestPlus4for.txt";

			// Open the empty db as the output stream
			OutputStream myOutput = new FileOutputStream(outFileName);

			// transfer bytes from the inputfile to the outputfile
			byte[] buffer = new byte[1024];
			int length;
			System.out.println("Entro su crea file");
			while ((length = myInput.read(buffer)) > 0) {
				System.out.println("buffer: " + buffer);
				myOutput.write(buffer, 0, length);
			}

			// Close the streams
			myOutput.flush();
			myOutput.close();
			myInput.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void copyFileToCompile(String filenameToCompile, InputStream is) {
		try {
			
			// Open your local db as the input stream
			InputStream myInput = is; 

			// Path to the just created empty db
			String outFileName = Environment.getExternalStorageDirectory()
					.getPath() + "/interest/config/" + filenameToCompile;

			// Open the empty db as the output stream
			OutputStream myOutput = new FileOutputStream(outFileName);

			// transfer bytes from the inputfile to the outputfile
			byte[] buffer = new byte[1024];
			int length;
			System.out.println("Entro su crea file");
			while ((length = myInput.read(buffer)) > 0) {
				System.out.println("buffer: " + buffer);
				myOutput.write(buffer, 0, length);
			}

			// Close the streams
			myOutput.flush();
			myOutput.close();
			myInput.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}