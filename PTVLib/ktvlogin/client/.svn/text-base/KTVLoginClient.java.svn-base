package com.peertv.service.ktvlogin.client;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.peertv.provider.ktv.KTV;
import com.peertv.lib.tools.*;
import com.peertv.service.ktvlogin.server.IKTVLogin;
import com.peertv.service.ktvlogin.server.IKTVLoginCallback;

	/**
	 * About Information Service Client class responsible for communicate with AboutInformation Service Server
	 * which return about information.
	 * Any new client (Activity) how want to use AboutInformationClient need to
	 * send his Context for initialize and call to startBindig method for communication 
	 * with AboutInformationServer
	 * @see #AboutInformationClient(Context)
	 * @see #startBindService()
	 * @author Ido Ozdova
	 * @version 1.0
	 * @since 1.0
	 * 
	 */
	public class KTVLoginClient{
	public static final String LOG_TAG = "KTVLoginClient";
	public static final String STATUS_LOGIN_NO_USER = "no_user";
	public static final String STATUS_LOGIN_LOGGED_IN = "logged_in";
	public static final String STATUS_LOGIN_CHECKING = "checking";
	public static final String STATUS_LOGIN_FAILED = "failure";
	public static final String STATUS_LOGIN_INITIALIZING = "initializing";
	public static final String STATUS_LOGIN_READY = "ready";
	private static boolean mBind = false;
	private boolean ready = false;
	private static IKTVLoginCallback.Stub mKTVLoginCallback = null;
	private SharedPreferences loginKartina = null;
	
	private ArrayList<ConnectionEventListener> _listeners = new ArrayList<ConnectionEventListener>();
	
	/**
	 * Instance of AIDL interface to work with AboutInformationServiceServer methods
	 * When binding with server succeeded, client can get about information. 
	 */
	public IKTVLogin mKTVLogin = null;
	
	private Context mContext;
	
	/**
	 * Constructor of AboutInformationServiceClient which communicate with AboutInformationServiceServer
	 * @param	iContext - Activity context which AboutInformationServiceClient can start bind/unbind to service 
	 *			Server through this context.
	 */
	public KTVLoginClient(Context iContext, IKTVLoginCallback.Stub iKtvLoginCallback){
		mContext = iContext;
		mKTVLoginCallback = iKtvLoginCallback;
	}
	
	public KTVLoginClient(Context iContext){
		mContext = iContext;
		mKTVLoginCallback = null;
	}
	
	public ServiceConnection serviceConnection = new ServiceConnection(){
		/**
		 * When client call binding to service, and service succeeded to bind 
		 * the onServiceConnected callback fired from service to the client.
		 */
		public void onServiceConnected(ComponentName name, IBinder service){
			Log.i(LOG_TAG,"onServiceConnected");
			mKTVLogin = IKTVLogin.Stub.asInterface(service);
			ready = true;
			try {
					if (mKTVLoginCallback!=null){
						mKTVLogin.setCallback(mKTVLoginCallback);
					}
					fireConnectionEvent();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
				
		}
		
		/**
		 * When extreme critical issue happen (service crashed/killed)
		 * onServiceDisconnected callback fire.
		 * Note: This callback doesn't fire when client called to unBined.
		 */
		public void onServiceDisconnected(ComponentName name){	
			Log.i(LOG_TAG,"onServiceDisconnected");
			mKTVLogin = null;
			mBind = false;
			ready = false;
			deleteAllEvents();
		}
	};

	
	public void setKTVLoginService(IKTVLogin kartinaLogin) {
//		Log.i(LOG_TAG,"setKTVLoginService");
		this.mKTVLogin = kartinaLogin;
	}

	public IKTVLogin getKTVLoginService() {
//		Log.i(LOG_TAG,"setKTVLoginService");
		return mKTVLogin;
	}

	/**
	 * Client should call this method for initialize binding between client to server service
	 * After success binding, client can start to work with the server by using aboutService instance 
	 * @return true if binding succeeded
	 */
	public boolean startBindService(){
		if (!mBind){
			Log.i(LOG_TAG,"startBindService");
			mBind = mContext.bindService(new Intent(IKTVLogin.class.getName()),this.serviceConnection, Context.BIND_AUTO_CREATE);
		}
		return mBind;
	}

	/**
	 * Client should call this method for unBinding between client to server service.
	 * 
	 */
	public void stopBindService(){
		if (mBind){
			Log.i(LOG_TAG,"stopBindService");
			mContext.unbindService(this.serviceConnection);
		}
		mBind = false;
		ready = false;
	}
	
	/**
	 * Check if client bind to service 
	 * @return true if bind is active
	 */
	public boolean isBind(){
		return mBind;
	}
	
	public void startLoginClient(String userId, String password){
		if ((userId!=null && !userId.trim().isEmpty() &&
				userId.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))&& 
				(password!= null  &&!password.trim().isEmpty())&&
				password.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")){
			loginKartina = mContext.getSharedPreferences(KTV.Settings.PREFS_NAME, 1);
			if (loginKartina!= null){
				SharedPreferences.Editor editor = loginKartina.edit();
				editor.putString("kartinaLoginUser", userId);
				editor.putString("kartinaLoginPass", password);
				editor.commit();
				if (mKTVLogin != null){
					try {
						mKTVLogin.startLogin(userId, password);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	public boolean isReady() {
		return ready;
	}
	
	public synchronized void addEventListener(ConnectionEventListener listener)	{
		    _listeners.add(listener);
	}
	 
	public synchronized void removeEventListener(ConnectionEventListener listener)	{
	    _listeners.remove(listener);
	}
	
	private synchronized void fireConnectionEvent()	{
	    ConnectionEvent event = new ConnectionEvent(this);
	    Iterator i = _listeners.iterator();
	    while(i.hasNext())	{
	    	((ConnectionEventListener) i.next()).handleConnectionEvent(event);
	    }
	  }
	
	private synchronized void deleteAllEvents()	{
	    _listeners.clear();
	  }
	
	/*public void setLogout(){
		loginKartina = mContext.getSharedPreferences(KTV.Settings.PREFS_NAME, 2);
		SharedPreferences.Editor editor = loginKartina.edit();
		editor.remove("kartinaLoginUser");
		editor.remove("kartinaLoginPass");
		editor.commit();
	}
	
	public String getUserName(){
		loginKartina = mContext.getSharedPreferences(KTV.Settings.PREFS_NAME, 1);
		if (loginKartina!=null){
			String userName = loginKartina.getString("kartinaLoginUser", "");
			return userName;
		}
		return "";
	}
	
	public String getPassword(){
		loginKartina = mContext.getSharedPreferences(KTV.Settings.PREFS_NAME, 1);
		if (loginKartina!=null){
			String password = loginKartina.getString("kartinaLoginPass", "");
			return password;
		}
		return "";
	}*/
}
