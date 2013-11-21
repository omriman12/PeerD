package com.peertv.service.about.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.util.Log;
public class AboutInformation extends Service{

	public static final String LOG_TAG = "AboutInformationService";
	public static Context mContext = null;
	public class AboutInformationImpl extends IAboutInformation.Stub{
	
		public String getVersion() throws RemoteException {
			return Build.VERSION.INCREMENTAL;
		}

		public String getStbId() throws RemoteException {
			String androidId = null;
			if (mContext!=null){
			androidId = Secure.getString(mContext.getContentResolver(),
	                Secure.ANDROID_ID);
			}
			if (androidId!=null && androidId.length()>10){
				return androidId.toUpperCase().substring(0, 10); 
			}
			return androidId!=null ? androidId.toUpperCase() : "";
		}

		public String getLanMac() throws RemoteException {
			String macAddress = getMacAddress();
			return (macAddress!=null ? macAddress : "");
		}

		public String getIpAddress() throws RemoteException {
			try {
	            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	                NetworkInterface intf = en.nextElement();
	                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                    InetAddress inetAddress = enumIpAddr.nextElement();
	                    if (!inetAddress.isLoopbackAddress()) {
//	                    	 Log.i(LOG_TAG, inetAddress.getHostAddress().toString());
	                    	return inetAddress.getHostAddress().toString();
	                    }
	                }
	            }
	        } catch (SocketException ex) {
	            Log.i(LOG_TAG, ex.toString());
	        }
	        return "";
		}

		public String getWifiMac() throws RemoteException {
			/*WifiManager wimanager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
			String address= wimanager.getConnectionInfo().getMacAddress();
			return (address!=null ? address.toUpperCase() : "");*/
			 try {
			        return loadFileAsString("/sys/class/net/wlan0/address")
			            .toUpperCase().substring(0, 17);
			    } catch (IOException e) {
			        e.printStackTrace();
			        return null;
			    }
			
		}
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.i(LOG_TAG,"onBind");
		mContext = this.getApplicationContext();
		return new AboutInformationImpl();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(LOG_TAG,"onUnbind");
		return super.onUnbind(intent);
	}
	
	/*
	 * Load file content to String
	 */
	private static String loadFileAsString(String filePath) throws java.io.IOException{
	    StringBuffer fileData = new StringBuffer(1000);
	    BufferedReader reader = new BufferedReader(new FileReader(filePath));
	    char[] buf = new char[1024];
	    int numRead=0;
	    while((numRead=reader.read(buf)) != -1){
	        String readData = String.valueOf(buf, 0, numRead);
	        fileData.append(readData);
	    }
	    reader.close();
	    return fileData.toString();
	}

	/*
	 * Get the STB MacAddress
	 */
	public String getMacAddress(){
	    try {
	        return loadFileAsString("/sys/class/net/eth0/address")
	            .toUpperCase().substring(0, 17);
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
}
