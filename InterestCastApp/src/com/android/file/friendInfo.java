package com.android.file;

import java.sql.Timestamp;

public class friendInfo {
	
	private String deviceName;
	private String macAddress;
	private String lastConnection;
	private String name;
	private String surname;
	private String pic;
	
	public friendInfo(String device, String macAddr, String lastConn)
	{
		deviceName = device;
		macAddress = macAddr;
		lastConnection = lastConn;
	}
	
	
	public String getDeviceName()
	{
		return deviceName;
	}
	
	public String getMacAddress()
	{
		return macAddress;
	}
	
	public String getLastConnection()
	{
		return lastConnection;
	}
	
	public void setName(String UserName)
	{
		name = UserName;
	}
	
	public void setSurname(String UserSurname)
	{
		surname = UserSurname;
	}
	
	public void setPic(String UserPicture)
	{
		pic = UserPicture;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getSurname()
	{
		return surname;
	}
	
	public String getPic()
	{
		return pic;
	}
	
}
