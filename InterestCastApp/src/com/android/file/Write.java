package com.android.file;

import java.io.BufferedWriter;
import java.sql.Timestamp;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;
import java.util.Vector;

public class Write {
	// public File sd = Environment.getExternalStorageDirectory();
	// public File f = new File(sd, "/interest/config/interessi.txt");
	File file;

		
	public Write ()
	{
		
	}
	
	public Write(Vector Data, String filename, boolean append) {
		File sdcard = Environment.getExternalStorageDirectory();

		// Get the text file
		file = new File(sdcard, filename);
		FileWriter fw = null;

		BufferedWriter bw = null;
		try {
			fw = new FileWriter(file, append);

			System.out.println("scrittura avvenuta");
			bw = new BufferedWriter(fw);
			for (int i = 0; i < Data.size(); i++) {
				bw.write((String) Data.get(i));
				bw.newLine();
			}
			
			bw.close();
			fw.close();

		} catch (IOException e) {
			e.printStackTrace();

		}
	}
	
	public void writeSingleFriend(friendInfo Data, String filename, boolean append) {
		File sdcard = Environment.getExternalStorageDirectory();

		// Get the text file
		file = new File(sdcard, filename);
		FileWriter fw = null;

		BufferedWriter writer = null;
		try {
			fw = new FileWriter(file, append);

			System.out.println("scrittura avvenuta");
			writer = new BufferedWriter(fw);
			
			writer.write((String) Data.getDeviceName());
			writer.write(";");
			writer.write((String) Data.getMacAddress());
			writer.write(";");
			writer.write(((String) Data.getLastConnection()));
			
			if (Data.getName() != null)
			{
				writer.write(";");
				writer.write((String) Data.getName());					
			}
			if (Data.getSurname() != null)
			{
				writer.write(";");
				writer.write((String) Data.getSurname());
			}
			if (Data.getPic() != null)
			{
				writer.write(";");
				writer.write((String) Data.getPic());
			}
			writer.newLine();	

			writer.close();
			fw.close();

		} catch (IOException e) {
			e.printStackTrace();

		}
	}
	
	public void replaceSingleFriend(friendInfo Data, String filename) {
		File sdcard = Environment.getExternalStorageDirectory();

		// Get the text file
		file = new File(sdcard, filename);
		File inputFile = new File(sdcard, filename);
		File tempFile = new File(sdcard, "myTempFile.txt");
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

			String macAddresslineToRemove = (String) Data.getMacAddress();
			String currentLine;

			while((currentLine = reader.readLine()) != null) {
				// trim newline when comparing with lineToRemove
				String macAddressLine = currentLine.split(";")[1];
				
				if(macAddressLine.equals(macAddresslineToRemove)) 
				{
					writer.write((String) Data.getDeviceName());
					writer.write(";");
					writer.write((String) Data.getMacAddress());
					writer.write(";");
					writer.write((String) Data.getLastConnection());
				
					if (Data.getName() != null)
					{
						writer.write(";");
						writer.write((String) Data.getName());					
					}
					if (Data.getSurname() != null)
					{
						writer.write(";");
						writer.write((String) Data.getSurname());
					}
					if (Data.getPic() != null)
					{
						writer.write(";");
						writer.write((String) Data.getPic());
					}
					writer.newLine();
				}
				else
				{
					writer.write(currentLine);
					writer.newLine();
				}
			}

			boolean successful = tempFile.renameTo(inputFile);

			reader.close();
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();

		}
	}
	
	public void removeSinglePerson(friendInfo Data, String filename) {
		File sdcard = Environment.getExternalStorageDirectory();

		// Get the text file
		file = new File(sdcard, filename);
		File inputFile = new File(sdcard, filename);
		File tempFile = new File(sdcard, "myTempFile.txt");
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

			String macAddresslineToRemove = (String) Data.getMacAddress();
			String currentLine;

			while((currentLine = reader.readLine()) != null) {
				// trim newline when comparing with lineToRemove
				String macAddressLine = currentLine.split(";")[1];
				
				if(macAddressLine.equals(macAddresslineToRemove)) 
				{
					continue;
				}
				else
				{
					writer.write(currentLine);
					writer.newLine();
				}
			}

			boolean successful = tempFile.renameTo(inputFile);

			reader.close();
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();

		}
	}

	// public boolean getexits() {
	// if (f.exists()) {
	// return true;
	// } else {
	// return false;
	// }
	// }

}
