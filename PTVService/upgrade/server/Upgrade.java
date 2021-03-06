package com.peertv.service.upgrade.server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.EventObject;
import java.util.Iterator;
import java.util.Scanner;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

public class Upgrade extends Service {
	
	private static final String LOG_TAG = "Upgrade";
	private static final String UP_TO_DATE="up_to_date";
	private static final String UPGRADE_NEEDED="upgrade_needed";
	private static final String DOWNLOAD_SUCCESS="download_success";
	private static final String CONNECTION_ERROR="connection_error";
	private static final String FAIL_URL_INVALID="fail_url_invalid";
	private static final String FAIL_NET="fail_net";
	private static final String FAIL_No_URL_Repository="fail_no_url_repository";
	private static final String FAIL_DIR_PREMISSION="fail_dir_premission";
	private static final String FAIL_MISSMATCH="fail_missmatch";
	private static final String FAIL_TXT_FILE_FORMAT="fail_txt_file_bad_format";
	private static final String DOWNLOAD_CANCELED="download_canceled";
	
	private static final String DESTENATIONDIR = "/media/";
	private static final String UPDATE_FILE_NAME = "update.zip";
	private static final String VERSION_TXT_FILE_NAME = "version.txt";
	private static int VERSION_TXT_SIZE=75;
	private static final int ONE_DAY = 8640000;
//	public String UPDATE_URL = "";
//	public static final String UPDATE_URL = "http://192.168.2.160/Android/upgrade_test/combined-new.txt";
	private static String UPDATE_URL = "";
	private final File UpdateImg = new File(DESTENATIONDIR + UPDATE_FILE_NAME);
	public static Context mContext = null;	
	private RebootInstall rebootinstall = null;
//	private String AllVersions="http://192.168.2.57/peerglobal/api/vendor/mx3/";
	private String mainRepository="http://192.168.2.54/peerglobal/api/";
//	private String mainRepository="http://192.168.2.160/Android/upgrade_test/xml.xml";
	private String avialibleVersionsURL="";
	private String LatestVersionNumber="";
	private String current_version = "";
	private int j=0;
	private static int State ;
	private DownloadFile mDownloadFile = null;
	private IUpgradeCallback mUpgradeCallback = null;
//	private UpgradeImpl upgradeImpl = null;
	ProgressDialog mProgressDialog;
	
	
	@Override
	public void onCreate(){
		Log.i(LOG_TAG,"onCreate");
		super.onCreate();
		rebootinstall = new RebootInstall(this.getApplicationContext());	
//		upgradeImpl = new UpgradeImpl();
		mContext = this.getApplicationContext();
			
		//------get sharedPreferences
		SharedPreferences upgradePref = mContext.getSharedPreferences("UPGRADE_PREFS", 1);
		upgradePref.edit().putString("mainRepository", mainRepository).commit(); 
		upgradePref.edit().putString("lastDownload", "").commit();
//        upgradePref.edit().putString("downloadStoped", "no").commit();

	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(LOG_TAG,"onBind");
		return new UpgradeImpl();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.i(LOG_TAG,"onStart : ");
		super.onStart(intent, startId);
		mContext = this.getApplicationContext();
		
	}

	private void ActionsAfterVersionTxtDownloaded() { //check version from txt
		Log.i(LOG_TAG,"ActionsAfterVersionTxtDownloaded : ");
		//UpgradeImpl upgradeImpl = new UpgradeImpl();
		try {
			String txtVersion = GetVersionFromFile( DESTENATIONDIR + VERSION_TXT_FILE_NAME);
			if ( txtVersion != null ) {
				if (CheckSameVersion(txtVersion , LatestVersionNumber)){
					try {
//						fireConnectionEvent();
						mUpgradeCallback.onCheckingDone(UPGRADE_NEEDED);
						//upgradeImpl.startDownload();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				else {
					try {
						mUpgradeCallback.onCheckingDone(FAIL_MISSMATCH);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e1) {e1.printStackTrace(); }
	
	}
	
	private static String GetCurrentVersion(){
		Log.i(LOG_TAG,"GetCurrentVersion");
		//return Build.VERSION.INCREMENTAL;
		return "1.0.9";
	}
	
	private String GetVersionFromFile(String file) throws IOException {
		Log.i(LOG_TAG,"GetVersionFromFile");
		String[] anArray = new String[10];
		int i=0;
		File mFile = new File(file);
	    String NL = System.getProperty("line.separator");
	    Scanner scanner = new Scanner(mFile);
	    try {
	      while (scanner.hasNextLine() && i<5){
	    	  anArray[i]=(scanner.nextLine() + NL);
	    	  i++;
	      } 
	    }
	    finally{
	      scanner.close();
	    }
	    
	    String[] temp1 = anArray[1].split("=");

	    if ( !temp1[0].equals("version_number")){
	    	Log.i(LOG_TAG,"GetVersionFromFile: Recieved : Can't read from txt file! bad format");
	    	try {
				mUpgradeCallback.onCheckingDone(FAIL_TXT_FILE_FORMAT);
				return null;
			} catch (RemoteException e) {
				e.printStackTrace();
			}
	    }
	    //String[] temp2 =anArray[1].split("=");
	    String FileVersion = temp1[1];
        Log.i(LOG_TAG,"GetVersionFromFile: Recieved : " + FileVersion);
		return FileVersion;
	  }
	
	private boolean CheckSameVersion(String ver1,String ver2){ // for checking versions from txt file and web are the same
		Log.i(LOG_TAG,"CheckSameVersion");
		int itartor1=0;
		String[] tempVer1 = ver1.split("\\.");
		String[] tempVer2 = ver2.split("\\.");
		
		/*fixing last character in the version string X.X.X*/
		tempVer1[2]=tempVer1[2].split("\\s+")[0];
		tempVer2[2]=tempVer2[2].split("\\s+")[0];
		
		Log.i(LOG_TAG,"CheckSameVersion : BinaryVer = "+tempVer1[0]+"." +tempVer1[1]+"."+tempVer1[2]+ " WebVer = " +tempVer2[0]+"." +tempVer2[1]+"."+tempVer2[2]);
		
		for (itartor1=0;itartor1<=2;itartor1++){
			if(Integer.parseInt(tempVer2[itartor1]) != Integer.parseInt(tempVer1[itartor1])){
				Log.i(LOG_TAG,"CheckSameVersion : version from binary is different to version from web");
				return false;
			}
		}
		Log.i(LOG_TAG,"CheckSameVersion : version from binary is same to version from web");
		return true;
	}
	
	private String CheckIfNewer(String v1,String v2){ // check versions from txt file and web
		Log.i(LOG_TAG,"CheckIfNewer: currect "+ v1 +" latest: " + v2);
		String CheckState="";
		String[] splitedCurrentVersion ,splitedLatestVersionNumber ;
		
		/**check if upgrade needed*/
		splitedCurrentVersion = v1.split("\\.");
		splitedLatestVersionNumber = v2.split("\\.");
		
		/*check if the version format is currect: X.X.X*/
		if ( splitedLatestVersionNumber.length != 3){
	    	Log.i(LOG_TAG,"CheckIfNewer: version recieved is not in the right format - X.X.X");
	    	return "ErrorBadVersionFormat";
	    }
		
		/*fixing last character in the version string X.X.X*/
		splitedCurrentVersion[2]=splitedCurrentVersion[2].split("\\s+")[0];
		splitedLatestVersionNumber[2]=splitedLatestVersionNumber[2].split("\\s+")[0];
		
		for (j=0;j<=2;j++){
			if(Integer.parseInt(splitedLatestVersionNumber[j]) > Integer.parseInt(splitedCurrentVersion[j])){
				Log.i(LOG_TAG,"CheckIfNewer : UpgradeAvialibe");
				CheckState="UpgradeAvialibe";
			}
		}
		if (CheckState != "UpgradeAvialibe"){
			Log.i(LOG_TAG,"CheckIfNewer : UpToDate");
			CheckState="UpToDate";
		}
		
		return CheckState;
	}

	private String getWifiMac() throws RemoteException {
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
	
	private void DownloadUrl(String downloadUrl,int size, String outputName, String SpecialAction){
		Log.i(LOG_TAG,"DownloadUrl");
		
		
//		mProgressDialog = new ProgressDialog(mContext);
//		mProgressDialog.setMessage("A message");
//		mProgressDialog.setIndeterminate(false);
//		mProgressDialog.setMax(100);
//		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		
		Looper.prepare();
		
		mDownloadFile = new DownloadFile();
		mDownloadFile.addEventListener(new upgradeConnectionEventListener(this, SpecialAction));
		mDownloadFile.setParameters(size,outputName,SpecialAction);
		mDownloadFile.execute(downloadUrl);
		
	}
	
	
	/*Inner Classes*/
	
	private class DownloadFile extends AsyncTask<String, Integer, String> {
		public String outputFile="", Action="";
		public int downloadSize = 0, fileLength=0;
		private ArrayList<ConEventListener> _listeners = new ArrayList<ConEventListener>();
		public long total=0;
		public boolean isDownloading=false, downloadingUpdateFlag = false, mStopDownloading = false ;
		private Integer mProgressValue = 0;
		private int filePartDownloaded=0;
		private SharedPreferences upgradePref = mContext.getSharedPreferences("UPGRADE_PREFS", 1);
		private boolean isResumed = false;
		private File file= null;
		
//		@Override
//		protected void onCancelled() {
//			Log.i(LOG_TAG,"DownloadFile: onCancelled : total - " + total);
//			super.onCancelled();
//			isDownloading=false;
//			this.cancel(true);
//		}
		
		
		@Override
	    protected void onProgressUpdate(Integer... progress) {
	    	//Log.i(LOG_TAG,"DownloadFile: onProgressUpdate" );
	        super.onProgressUpdate(progress);
	        //mProgressDialog.setProgress(progress[0]);
	        this.setProgressValue(progress[0]);
	        //Log.i(LOG_TAG,"DownloadFile:doInBackground -  prog: " + progress[0] );
	        //upgradeImp.downloadProgress()
	    }

		@Override
	    public String doInBackground(String... sUrl)  {
			
			file = new File(outputFile);
	        try {
	        	Log.i(LOG_TAG,"DownloadFile:doInBackground: output name - " + outputFile + " | size - " + downloadSize +" | event - " + Action);
	        	
	        	 if(sUrl[0] == null){
	        		 mUpgradeCallback.onCheckingDone(FAIL_URL_INVALID);
	        		 return null;
	        	 }
	        		 
	            URL url = new URL(sUrl[0]);
	            URLConnection connection = url.openConnection();
	            connection.connect();
	             
	            fileLength = connection.getContentLength();
	            filePartDownloaded = (int) file.length();
	            
	            /** check if this is a resumed download or delete old file*/
	            if(file.exists()){
	            	Log.i(LOG_TAG,"DownloadFile:doInBackground: file exists , checking if this is a resumed download ");

	            	if( upgradePref.getString("downloadStoped", "").equals("yes") && upgradePref.getString("lastDownload", "").equals(sUrl[0])){
//	       		        connection.setRequestProperty("Range", "bytes="+(filePartDownloaded)+"-");
//            			isResumed = true;
	            	}
	            	else {
	            		boolean deleteSuccess = file.delete();
	            		if (!deleteSuccess)
	            			Log.i(LOG_TAG, "DownloadFile:doInBackground: - not resumed, failed to delete old " + outputFile);
	            		else 
	            			Log.i(LOG_TAG, "DownloadFile:doInBackground: - not resumed , deleted old " + outputFile);
	            	}
	            }
	            
	            /** download the file */
	            InputStream input = new BufferedInputStream(url.openStream());
	            OutputStream output = (isResumed == false)? new FileOutputStream(outputFile): new FileOutputStream(outputFile,true) ;
	            
	            isDownloading=true;
	            int count,t=0;
	            byte data[] = new byte[1024];
	            
	            if ( Action.equals("DownloadUpdate")){
	            	data = new byte[VERSION_TXT_SIZE];
	            	upgradePref.edit().putString("lastDownload", sUrl[0]).commit();
	            }
		            
		        if (Action.equals("DownloadTxtFile"))
		        	data = new byte[VERSION_TXT_SIZE];
		            
	            if (downloadSize == 0) // download all file
	            	downloadSize=fileLength;

	            
	            while (total < downloadSize && mStopDownloading == false) {
	            	count = input.read(data);
	            	total += count;
	            	
	            	if ( Action.equals("DownloadUpdate")  && downloadingUpdateFlag == false){
	            		if(total >= VERSION_TXT_SIZE){
	            			downloadingUpdateFlag = true;
	            			data = new byte[1000000];
	            		}
	            	}
	            	else
	            	{ 
	            		 output.write(data, 0, count);
	            	}
	            	
	                // publishing the progress....
	                publishProgress((int) (total * 100 / fileLength));
	                this.onProgressUpdate((int) (total * 100 / fileLength));
	                
	                if (t==100){
	                	t=0;
	                	 Log.i(LOG_TAG,"DownloadFile:doInBackground - Downloaded : " + total);
	                }
	               t++;
	            }
	           
	            isDownloading=false;  
	            
	            output.flush();
	            output.close();
	            input.close();
//	            Looper looper = Looper.myLooper();
//	            if (looper!=null){
//	            	looper.quit();
//	            }
		        if (mStopDownloading == true){
		        	upgradePref.edit().putString("downloadStoped", "yes").commit();
		        	Log.i(LOG_TAG,"downloadStoped --stopdownloading--" + upgradePref.getString("downloadStoped", null)); 
		        	mUpgradeCallback.onDownloadDone(DOWNLOAD_CANCELED);
		        }
		        else if ( Action.equals("DownloadUpdate")  ){
		        	upgradePref.edit().putString("downloadStoped", "no").commit();
		        	mUpgradeCallback.onDownloadDone(DOWNLOAD_SUCCESS);
	            }
	            else if ( Action.equals("DownloadTxtFile")  ){
	            	fireConnectionEvent();
	            }
	            
	            
	            
	        } catch (MalformedURLException e){
	        	isDownloading=false;
	        	Log.i(LOG_TAG,"MalformedURLException");
//	        	Looper.myLooper().quit();
	        	try {
					mUpgradeCallback.onCheckingDone(FAIL_URL_INVALID);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
	        }
	        catch (FileNotFoundException e) {
	        	isDownloading=false;
	        	Log.i(LOG_TAG,"FileNotFoundException");
//	        	Looper.myLooper().quit();
	        	try {
					mUpgradeCallback.onCheckingDone(FAIL_DIR_PREMISSION);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
	        }
	        catch (IOException e) {
//		        	Looper.myLooper().quit();
	        		isDownloading=false;
		        	try {
		        		 if ( Action == "DownloadUpdate" ){
		        			 mUpgradeCallback.onCheckingDone(CONNECTION_ERROR);
			            }
		        	} catch (RemoteException e1) {
		        		e1.printStackTrace();
		        	}
	    	} catch (RemoteException e) { 
	    		isDownloading=false;
	    		e.printStackTrace();
	    	}

	        	
	        return null;
	    }
		
		private void setParameters(int downSize, String fName , String Act){
			this.outputFile=fName;
			this.downloadSize = downSize;
			this.Action = Act;
		}
		
		private void setProgressValue(Integer progressValue) {
			//Log.i(LOG_TAG, "setProgressValue: " + progressValue);
			this.mProgressValue = progressValue;
		}

		private Integer getProgressValue() {
			return mProgressValue;
		}
		
		private boolean getIsDownloading() {
			return isDownloading;
		}
		
		private void StopDownloading() {
			Log.i(LOG_TAG, "DownloadFile: StopDownloading - inside");
			this.mStopDownloading = true;
			isDownloading = false;
			//this.cancel(true);
		}
		
		/*EventListener manegment*/
		private synchronized void addEventListener(upgradeConnectionEventListener listener)	{
			Log.i(LOG_TAG, "addEventListener");
			_listeners.add(listener);
		}
		 
		private synchronized void removeEventListener(upgradeConnectionEventListener listener)	{
			Log.i(LOG_TAG, "removeEventListener");
			_listeners.remove(listener);
		}
		
		private synchronized void fireConnectionEvent()	{
		    Log.i(LOG_TAG, "fireConnectionEvent");
			ConEvent event = new ConEvent(this);
		    Iterator i = _listeners.iterator();
		    while(i.hasNext())	{
		    	((ConEventListener) i.next()).handleConnectionEvent(event);
		    }
		  }
		
	}
	
	public class upgradeConnectionEventListener implements ConEventListener{
		String mAction;
		Upgrade mUpgrade;
		
		public upgradeConnectionEventListener(Upgrade upgrade, String action){
			Log.i(LOG_TAG, "upgradeConnectionEventListener - constractor");
			mAction = action;
			mUpgrade = upgrade;
		}
		
		public void handleConnectionEvent(EventObject e) {
			
			if (mAction==null){
				return;
			}
			if(mAction.trim().isEmpty()){
				return;
			}
			
			Log.i(LOG_TAG, "handleConnectionEvent - Action is: " + mAction);
			
			if(mAction.equalsIgnoreCase("DownloadTxtFile")){
				mUpgrade.ActionsAfterVersionTxtDownloaded();
			}
		}
	}
	
	public class UpgradeImpl extends IUpgrade.Stub{
		private String verState="";
		
		private void getUpdateURL(){
			XMLPrhase phraser = new XMLPrhase();
//			LatestVersionNumber = phraser.exec(avialibleVersionsURL);
//			UPDATE_URL ="http://" + phraser.exec(avialibleVersionsURL + "/" + LatestVersionNumber.replaceAll("\\.", "_"));
					
			UPDATE_URL = "http://192.168.2.160/Android/upgrade_test/combined-new.txt";
			LatestVersionNumber="1.0.11";
		}
		
		public String checkForUpgrade() throws RemoteException {
			Log.i(LOG_TAG,"UpgradeImpl: checkForUpgrade");
			SharedPreferences upgradePref = mContext.getSharedPreferences("UPGRADE_PREFS", 1);

			/**get current version*/
			try { current_version=GetCurrentVersion();
			}	catch (Exception e) {e.printStackTrace();}
			
			/**get latest version from web*/
			
			//try {
			if (upgradePref.getString("mainRepository", null) !=null){
//				avialibleVersionsURL=upgradePref.getString("mainRepository", null) + "mac/"+getWifiMac().replaceAll(":", "_");
				avialibleVersionsURL=upgradePref.getString("mainRepository", null) + "mac/00_0A_EB_50";
			}
			else {
				mUpgradeCallback.onCheckingDone(FAIL_No_URL_Repository);
				return "FAIL_No_URL_Repository";
			}
			Log.i(LOG_TAG,"CheckIfUpgradeAvialible: avialible versionsURL - " +avialibleVersionsURL);
			//} catch (RemoteException e) {
			//e.printStackTrace();
			//Log.i(LOG_TAG,"onStart : Chould not get wifi mac address ");}
			
			getUpdateURL();
			Log.i(LOG_TAG,"CheckIfUpgradeAvialible: UPDATE_URL - " +UPDATE_URL);
			
			if(LatestVersionNumber.equalsIgnoreCase("NetFail") || UPDATE_URL.equalsIgnoreCase("NetFail")){
				mUpgradeCallback.onCheckingDone(FAIL_NET);
				return "FAIL_NET";
			}
		
			verState = CheckIfNewer(current_version, LatestVersionNumber);
			if (verState.equalsIgnoreCase("UpgradeAvialibe")){
				/** get latest version from txtfile */
				DownloadUrl(UPDATE_URL,VERSION_TXT_SIZE, DESTENATIONDIR + VERSION_TXT_FILE_NAME , "DownloadTxtFile");
			}
			
			else if (verState.equalsIgnoreCase("ErrorBadVersionFormat")){ 
				mUpgradeCallback.onCheckingDone(FAIL_TXT_FILE_FORMAT);
				return "FAIL_TXT_FILE_FORMAT";
			}
			
			else {
				mUpgradeCallback.onCheckingDone(UP_TO_DATE);
				return "UpToDate"; 
			}
//			UPDATE_URL="http://192.168.2.160/Android/upgrade_test/combined-new.txt";
//			mUpgradeCallback.onCheckingDone(UPGRADE_NEEDED);
			
			return "";
		}

		public void startDownload() throws RemoteException {
			Log.i(LOG_TAG,"UpgradeImpl: startDownload");
			//getUpdateURL(); - for QA testing
			DownloadUrl(UPDATE_URL,0,DESTENATIONDIR + UPDATE_FILE_NAME, "DownloadUpdate");
		}
		
		public void stopDownload() throws RemoteException {
			Log.i(LOG_TAG,"UpgradeImpl: stopDownload");
			if (mDownloadFile!=null){
				mDownloadFile.StopDownloading();	
			}
		}

		public boolean isDownloading() throws RemoteException {
			Log.i(LOG_TAG,"UpgradeImpl: isDownloading");
			if (mDownloadFile!=null){
				return mDownloadFile.getIsDownloading();	
			}
			return false;
		}

		public int downloadProgress() throws RemoteException {
			if (mDownloadFile!=null){
				return mDownloadFile.getProgressValue();
			}
			return 0;
		}

		public void rebootAndUpgrade() throws RemoteException {
			Log.i(LOG_TAG,"UpgradeImpl: rebootAndUpgrade");
			rebootinstall.startInstallPackage(UpdateImg);
		}

		public void setCallback(IUpgradeCallback upgradeCallback) throws RemoteException {
			Log.i(LOG_TAG,"UpgradeImpl: setCallback");
			mUpgradeCallback = upgradeCallback;
		}

	}
	
}
