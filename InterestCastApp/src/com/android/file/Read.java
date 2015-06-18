package com.android.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import SFE.BOAL.*;

import java.io.BufferedWriter;
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
import java.lang.reflect.Array;
import java.util.Vector;

import com.android.tab.TabInteressi;

import android.content.Context;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import java.sql.Timestamp;

public class Read {

	public Vector valori = new Vector();
	public int i = 0;
	public String line;
	public StringBuilder lettura;
	final String separatore = "=";
	public String[] tmp = new String[2];
	File file;
	
	
	public Read() {
		
	}
	
	public Read(String filename) {
		// String readString;
		File sdcard = Environment.getExternalStorageDirectory();

		// Get the text file
		file = new File(sdcard, filename);

		// Read text from file
		lettura = new StringBuilder();

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));

			while ((line = br.readLine()) != null) {
				// getinteressi();
				// valori[i] = line;
				
				try
				{
					tmp = line.split(separatore);
					valori.add(tmp[1]);
				}
				catch (Exception e) {
					System.out.println("The file: "+ filename +" is corrupted - Error Reading it");
					// You'll need to add proper error handling here
				}
				// System.out.println("Valore: " +tmp[1] + " - i: " + i);
				i++;

			}
			System.out.println("size:" + valori.size());
		} catch (IOException e) {
			// You'll need to add proper error handling here
		}

	}

	public Vector getReadInterest() {
		return valori;
	}

	public String getReadWithInterest(String topic) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));

			while ((line = br.readLine()) != null) {
				// getinteressi();
				// valori[i] = line;
				tmp = line.split(separatore);
				if (tmp[0].equalsIgnoreCase(topic))
					return tmp[1];
				// System.out.println("Valore: " +tmp[1] + " - i: " + i);
				i++;

			}
		} catch (IOException e) {
			// You'll need to add proper error handling here
		}
		return "null";
	}

	public static Vector getInterest(String filename_interest) {
		Vector interest = new Vector();
		int y = 0;
		String linea;
		File fileinterest;
		File sdcard2 = Environment.getExternalStorageDirectory();
		fileinterest = new File(sdcard2, filename_interest);
		String[] temporaneo = new String[2];
		final String separ = "=";

		// Get the text file

		try {
			BufferedReader br = new BufferedReader(new FileReader(fileinterest));

			while ((linea = br.readLine()) != null) {
				try
				{
					temporaneo = linea.split(separ);
					interest.add(temporaneo[0]);
					y++;
				}
				catch (Exception e) {
					System.out.println("The file: "+ filename_interest +" is corrupted - Error Reading it inside the function getInterest");
					// You'll need to add proper error handling here
				}

			}
		} catch (IOException e) {
			// You'll need to add proper error handling here
		}
		interest.removeElement(interest.lastElement());
		return interest;
	}

	public Vector readFriends(String fileFriend) {
		Vector friend = new Vector();
		int j = 0;
		String riga;
		File filemacaddress;
		File sd_card = Environment.getExternalStorageDirectory();
		filemacaddress = new File(sd_card, fileFriend);
		String temp;
		final String separ = ";";
		String[] tmp = new String[2];

		// Get the text file

		try {
			BufferedReader br = new BufferedReader(new FileReader(
					filemacaddress));

			while ((riga = br.readLine()) != null) {
				try
				{
					tmp = riga.split(separ);
					if (fileFriend.contains("Nofriends.txt"))
						friend.add("NoFriend-Mac: "+tmp[1]+" \nTime: "+tmp[2]);
					else
						friend.add("Friend-Mac: "+tmp[1]+" \nTime: "+tmp[2]);
					//mac.add(tmp[1]);
					j++;

					//System.out.println("ecco l'amico" + tmp[2]);
				}
				catch (Exception e) {
					System.out.println("The file: "+ fileFriend +" is corrupted - Error Reading it inside the function readmac");
					// You'll need to add proper error handling here
				}
			}
		} catch (IOException e) {
			// You'll need to add proper error handling here
		}

		return friend;

	}
	
	

	
	

	public boolean findMacFriend(String filemac, String mac) {

		int j = 0;
		String riga, macRead;

		File filemacaddress;
		File sd_card = Environment.getExternalStorageDirectory();
		filemacaddress = new File(sd_card, filemac);

		final String separ = ";";
		String[] tmp = new String[2];

		// Get the text file

		try {
			BufferedReader br = new BufferedReader(new FileReader(
					filemacaddress));

			while ((riga = br.readLine()) != null) {
				try
				{
					tmp = riga.split(separ);
					macRead = tmp[1];
					if (macRead.equalsIgnoreCase(mac))
						return true;
					j++;
					//System.out.println("ecco l'amico" + tmp[1]);
				}
				catch (Exception e) {
					System.out.println("The file: "+ filemac +" is corrupted - Error Reading it inside the function readmac");
					// You'll need to add proper error handling here
				}
			}
		} catch (IOException e) {
			// You'll need to add proper error handling here
		}
		return false;

	}
	
	public Vector readmac(String filemac) {
		Vector mac = new Vector();
		int j = 0;
		String riga;
		File filemacaddress;
		File sd_card = Environment.getExternalStorageDirectory();
		filemacaddress = new File(sd_card, filemac);
		String temp;
		final String separ = "=";
		String[] tmp = new String[2];

		// Get the text file

		try {
			BufferedReader br = new BufferedReader(new FileReader(
					filemacaddress));

			while ((riga = br.readLine()) != null) {
				try
				{
					tmp = riga.split(separ);
					mac.add(tmp[1]);
					j++;

					System.out.println("ecco l'amico" + tmp[1]);
				}
				catch (Exception e) {
					System.out.println("The file: "+ filemac +" is corrupted - Error Reading it inside the function readmac");
					// You'll need to add proper error handling here
				}
			}
		} catch (IOException e) {
			// You'll need to add proper error handling here
		}

		return mac;

	}
	
	
	public friendInfo getSingleFriend(String file, String mac) {

		int j = 0;
		String riga, macRead;

		File filemacaddress;
		File sd_card = Environment.getExternalStorageDirectory();
		filemacaddress = new File(sd_card, file);

		final String separ = ";";
		String[] tmp;

		// Get the text file

		try {
			BufferedReader br = new BufferedReader(new FileReader(
					filemacaddress));

			while ((riga = br.readLine()) != null) {
				try
				{
					tmp = riga.split(separ);
					macRead = tmp[1];
					if (macRead.equalsIgnoreCase(mac))
					{
						friendInfo friend = new friendInfo(tmp[0],tmp[1],tmp[2]);
						if (tmp.length > 4)
							friend.setName(tmp[3]);
						if (tmp.length >= 5)
							friend.setSurname(tmp[4]);
						if (tmp.length == 6)
							friend.setPic(tmp[5]);
						return friend;
					}
					j++;
					//System.out.println("ecco l'amico" + tmp[1]);
				}
				catch (Exception e) {
					System.out.println("The file: "+ file +" is corrupted - Error Reading it inside the function readmac");
					// You'll need to add proper error handling here
				}
			}
		} catch (IOException e) {
			// You'll need to add proper error handling here
		}
		return null;

	}
	
	
	public Vector getConnectionLog(String filename) {
		Vector log = new Vector();	
		String linea;	
		File fileinterest;
		File sdcard2 = Environment.getExternalStorageDirectory();
		fileinterest = new File(sdcard2, filename);

			// Get the text file

		try {
			BufferedReader br = new BufferedReader(new FileReader(fileinterest));

			while ((linea = br.readLine()) != null) {
				try
				{
					
					log.add(linea);
				
				}
				catch (Exception e) {
					System.out.println("The file: "+ filename +" is corrupted - Error Reading it inside the function getInterest");
					// You'll need to add proper error handling here
				}

			}
		} catch (IOException e) {
			// You'll need to add proper error handling here
		}
		
		return log;
	}
	
	
}
