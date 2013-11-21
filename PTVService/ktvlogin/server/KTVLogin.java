package com.peertv.service.ktvlogin.server;



import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;


public class KTVLogin extends Service{

	public static final String LOG_TAG = "KTVLoginService";
	public static final String STATUS_LOGIN_NO_USER = "no_user";
	public static final String STATUS_LOGIN_LOGGED_IN = "logged_in";
	public static final String STATUS_LOGIN_CHECKING = "checking";
	public static final String STATUS_LOGIN_FAILED = "failure";
	public static final String STATUS_LOGIN_INITIALIZING = "initializing";
	public static final String STATUS_LOGIN_READY = "ready";
	public static final String PREFS_NAME = "LoginKartina";
	public static final Long KARTINA_TIMEOUT_FAIL_LOGIN = 600001L;
	public static final String KTV_AUTHORITY = "com.peertv.provider.ktv.ktvprovider";
	public static final String KTV_Settings_LOGIN_KARTINA = "login";
	public static final String KTV_Settings_CUSTOMER = "customer";
	public static final String KTV_Settings_LOGIN_RESULT = "login_result";
	public static final Uri GET_LOGIN_KARTINA_URI = Uri.parse("content://" + KTV_AUTHORITY + "/" + KTV_Settings_LOGIN_KARTINA);
	public static Long KARTINA_FAILURE_PREIOD_TIME = 300000L; 
	
	public static Context mContext = null;
	public static ContentResolver mContentResolver = null;
	public static Handler	mHandler = null;
	private HandlerThread mHandlerThread = null;
	public static QueryContentObserver mObserver = null;
	public static Cursor mCursor = null;
	public static HandlerThread mUpdateStatusHandlerThread = null;
	public static UpdateStatusHandler mUpdateStatusHandler = null;
	
	public static IKTVLoginCallback  mKartinaLoginCallback = null;
	
	private static String mStatusLogin = "";
	public static SharedPreferences mLoginKartinaPrefs = null;
	public static Integer mFailureTime = 0;
	public static Long timestampFail = 0L;
	public static Long lastFailureTimestamp = 0L;
	public static boolean mFirstRunLocked = false;
	
	public KTVLogin(){
		super();
		Log.i(LOG_TAG, "Constractor");
		mUpdateStatusHandlerThread = new HandlerThread("UpdateStatus");
		mUpdateStatusHandlerThread.start();
		Looper iServiceLooper = mUpdateStatusHandlerThread.getLooper();
		mUpdateStatusHandler = new UpdateStatusHandler(iServiceLooper);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mHandlerThread = new HandlerThread("KTVLoginServiceObserver");
//		mHandlerThread.setPriority(Process.THREAD_PRIORITY_DEFAULT);
        mHandlerThread.start();
		Looper looper = mHandlerThread.getLooper();
        mHandler = new Handler(looper);
	}

	public class KTVLoginImpl extends IKTVLogin.Stub{

		public String getStatus() throws RemoteException {
			if (mLoginKartinaPrefs==null){
				mLoginKartinaPrefs = mContext.getSharedPreferences(PREFS_NAME, MODE_WORLD_READABLE);
			}
			mStatusLogin = mLoginKartinaPrefs.getString("kartinaLoginStatus", "");
			if (!mFirstRunLocked && mStatusLogin.equalsIgnoreCase("failure_3")){
				startCheckLock();
			}
			return mStatusLogin;
		}
		
		/*private void setStatus(String status){
			if (mLoginKartinaPrefs==null){
				mLoginKartinaPrefs = mContext.getSharedPreferences(PREFS_NAME, MODE_WORLD_WRITEABLE);
			}
			SharedPreferences.Editor editor = mLoginKartinaPrefs.edit();
			editor.putString("kartinaLoginStatus", status);
			editor.commit();
			mStatusLogin = status;
			
		}*/
		
		public String startLogin(String userId, String password)
				throws RemoteException {
			if (getLocalIpAddress()!=null){
				if (System.currentTimeMillis() > (timestampFail + KARTINA_TIMEOUT_FAIL_LOGIN)){
					if (System.currentTimeMillis() > lastFailureTimestamp + KARTINA_FAILURE_PREIOD_TIME){
						mFailureTime = 0;
					}
					if (mFailureTime<=3){
						if (userId!=null && !userId.trim().isEmpty() && 
							password!=null && !password.trim().isEmpty()){
							this.setUserId(userId);
							this.setPassword(password);
							Uri uri = Uri.parse("content://" + KTV_AUTHORITY + "/" + 
									KTV_Settings_LOGIN_KARTINA + "/" + KTV_Settings_CUSTOMER + "/" + password + "/" + userId);
							
							if (null != uri){
								mObserver = new QueryContentObserver(mHandler, true);
								mCursor =  mContentResolver.query(uri, null, null, null, null);
							}
							else{
								mCursor = null;
							}
							if (mCursor != null){
								setStatus(STATUS_LOGIN_CHECKING);
								mKartinaLoginCallback.statusChange(STATUS_LOGIN_CHECKING);
								mCursor.registerContentObserver(mObserver);
			//					checkPassword();
							}
						
						}//end of username/password check
					}
				}
			}
			return null;
		}

		public void setCallback(IKTVLoginCallback ktvLoginCallback)
				throws RemoteException {
			// TODO Auto-generated method stub
			mKartinaLoginCallback = ktvLoginCallback;
		}

		public void setUserId(String userId) throws RemoteException {
			if (mLoginKartinaPrefs==null){
				mLoginKartinaPrefs = mContext.getSharedPreferences(PREFS_NAME, MODE_WORLD_WRITEABLE);
			}
			SharedPreferences.Editor editor = mLoginKartinaPrefs.edit();
			editor.putString("kartinaLoginUser", userId);
			editor.commit();
		}

		public void setPassword(String password) throws RemoteException {
			if (mLoginKartinaPrefs==null){
				mLoginKartinaPrefs = mContext.getSharedPreferences(PREFS_NAME, MODE_WORLD_WRITEABLE);
			}
			SharedPreferences.Editor editor = mLoginKartinaPrefs.edit();
			editor.putString("kartinaLoginPass", password);
			editor.commit();
		}

		public void setLogout() throws RemoteException {
			if (mLoginKartinaPrefs==null){
				mLoginKartinaPrefs = mContext.getSharedPreferences(PREFS_NAME, MODE_WORLD_WRITEABLE);
			}
			SharedPreferences.Editor editor = mLoginKartinaPrefs.edit();
			editor.remove("kartinaLoginUser");
			editor.remove("kartinaLoginPass");
			editor.remove("kartinaLoginStatus");
			editor.commit();
		}

		public String getUserId() throws RemoteException {
			if (mLoginKartinaPrefs==null){
				mLoginKartinaPrefs = mContext.getSharedPreferences(PREFS_NAME, MODE_WORLD_READABLE);
			}
			String userName = mLoginKartinaPrefs.getString("kartinaLoginUser", "");
			return userName;
		}
		
		public String getPassword() throws RemoteException {
			if (mLoginKartinaPrefs==null){
				mLoginKartinaPrefs = mContext.getSharedPreferences(PREFS_NAME, MODE_WORLD_READABLE);
			}
			String password = mLoginKartinaPrefs.getString("kartinaLoginPass", "");
			return password;
		}
		
	}
	
	public static void checkPassword(){
		if (mCursor == null){
    		return;
    	}
		mCursor.unregisterContentObserver(mObserver);
    	mCursor = mContentResolver.query(GET_LOGIN_KARTINA_URI, null, null, null, null);
    	String result = null;
    	if (mCursor.getCount()>0){
    		mCursor.moveToFirst();
    		int loginColumn = mCursor.getColumnIndex(KTV_Settings_LOGIN_RESULT);
    		result = mCursor.getString(loginColumn);
    		try {
    		 if (result != null){//if login succeeded then need to preloading
    			 	mFailureTime = 0;
    			 	setStatus(STATUS_LOGIN_INITIALIZING);
					mKartinaLoginCallback.statusChange(STATUS_LOGIN_INITIALIZING);
					
					mObserver = new QueryContentObserver(mHandler, false);
					mCursor =  mContentResolver.query(Uri.parse("content://" + KTV_AUTHORITY + "/" + "channel_list"), null, null, null, null);
					
					if (mCursor != null){
						mCursor.registerContentObserver(mObserver);
					}
    		 }
    		 else{
    			 mStatusLogin = getStatus();
    			 mFailureTime = (mFailureTime % 3) + 1;
    			 setStatus(STATUS_LOGIN_FAILED + "_" + mFailureTime.toString());
    			 mKartinaLoginCallback.statusChange(STATUS_LOGIN_FAILED  + mFailureTime.toString());
    			 lastFailureTimestamp = System.currentTimeMillis();
    			 	if (mFailureTime==3){
    			 		startCheckLock();
    			 	}
    		 }
    		} catch (RemoteException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
	}
	
	public static void startCheckLock(){
		timestampFail = System.currentTimeMillis();
 		mUpdateStatusHandler.removeMessages(0);
 		Message msg = mUpdateStatusHandler.obtainMessage(0, mKartinaLoginCallback);
 		mUpdateStatusHandler.sendMessageDelayed(msg, KARTINA_TIMEOUT_FAIL_LOGIN);
 		mFirstRunLocked = true;
	}
	
	public static void changeToReady(){
		setStatus(STATUS_LOGIN_READY);
		try{
			mKartinaLoginCallback.statusChange(STATUS_LOGIN_READY);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (mCursor!=null){
			mCursor.close();
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		mContext = this.getApplicationContext();
		mContentResolver = this.getContentResolver();
		mLoginKartinaPrefs = mContext.getSharedPreferences(PREFS_NAME, 1);
		refreshStatus();
		return new KTVLoginImpl();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}
	
	public void refreshStatus(){
		if (mLoginKartinaPrefs==null){
			mLoginKartinaPrefs = mContext.getSharedPreferences(PREFS_NAME, MODE_WORLD_READABLE);
		}
		mStatusLogin = mLoginKartinaPrefs.getString("kartinaLoginStatus", "");
		String userId = mLoginKartinaPrefs.getString("kartinaLoginUser", "");
		if (userId.trim().isEmpty() && mStatusLogin.trim().isEmpty()){
			setStatus(STATUS_LOGIN_NO_USER);
		}
		else{
			mCursor = mContentResolver.query(GET_LOGIN_KARTINA_URI, null, null, null, null);
	    	String result = null;
	    	if (mCursor!=null && mCursor.getCount()>0){
	    		mCursor.moveToFirst();
	    		int loginColumn = mCursor.getColumnIndex(KTV_Settings_LOGIN_RESULT);
	    		result = mCursor.getString(loginColumn);
				if (result != null){
//				 	 setStatus(STATUS_LOGIN_LOGGED_IN);
				 	 setStatus(STATUS_LOGIN_READY);
				}
	    	}
		}
	}
	
	public static void setStatus(String status){
		if (mLoginKartinaPrefs==null){
			mLoginKartinaPrefs = mContext.getSharedPreferences(PREFS_NAME, MODE_WORLD_WRITEABLE);
		}
		SharedPreferences.Editor editor = mLoginKartinaPrefs.edit();
		editor.putString("kartinaLoginStatus", status);
		editor.commit();
		mStatusLogin = status;
		
	}
	
	public static String getStatus(){
		if (mLoginKartinaPrefs==null){
			mLoginKartinaPrefs = mContext.getSharedPreferences(PREFS_NAME, MODE_WORLD_READABLE);
		}
		String status = mLoginKartinaPrefs.getString("kartinaLoginStatus", "");
		if (!mFirstRunLocked && status.equalsIgnoreCase("failure_3")){
			startCheckLock();
		}
		return status;
	}

	public static String getLocalIpAddress(){
		try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                    	return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.i(LOG_TAG, ex.toString());
        }
        return null;
	}
}
