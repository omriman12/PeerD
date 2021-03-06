package com.peertv.service.upgrade.client;

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

import com.peertv.lib.tools.ConnectionEvent;
import com.peertv.lib.tools.ConnectionEventListener;
import com.peertv.service.upgrade.server.IUpgrade;
import com.peertv.service.upgrade.server.IUpgradeCallback;

public class UpgradeClient{
	public static final String LOG_TAG = "UpgradeClient";
	public static final String STATUS_LOGIN_NO_USER = "no_user";
	public static final String STATUS_LOGIN_LOGGED_IN = "logged_in";
	public static final String STATUS_LOGIN_CHECKING = "checking";
	public static final String STATUS_LOGIN_FAILED = "failure";
	public static final String STATUS_LOGIN_INITIALIZING = "initializing";
	public static final String STATUS_LOGIN_READY = "ready";
	private static boolean mBind = false;
	private boolean ready = false;
	private static IUpgradeCallback.Stub mUpgradeCallback = null;
	private ArrayList<ConnectionEventListener> _listeners = new ArrayList<ConnectionEventListener>();
	
	/**
	 * Instance of AIDL interface to work with AboutInformationServiceServer methods
	 * When binding with server succeeded, client can get about information. 
	 */
	public IUpgrade mUpgrade = null;
	
	private Context mContext; // context of the apk that will use the Upgrade service
	
	/**
	 * Constructor of AboutInformationServiceClient which communicate with AboutInformationServiceServer
	 * @param	iContext - Activity context which AboutInformationServiceClient can start bind/unbind to service 
	 *			Server through this context.
	 */
	public UpgradeClient(Context iContext, IUpgradeCallback.Stub iUpgradeCallback){
		Log.i(LOG_TAG,"UpgradeClient : Constractor");
		mContext = iContext;
		mUpgradeCallback = iUpgradeCallback;
	}
	
	public UpgradeClient(Context iContext){
		Log.i(LOG_TAG,"UpgradeClient : Constractor");
		mContext = iContext;
		//mUpgradeCallback = null;
	}
	
	public ServiceConnection serviceConnection = new ServiceConnection(){
		/**
		 * When client call binding to service, and service succeeded to bind 
		 * the onServiceConnected callback fired from service to the client.
		 */
		public void onServiceConnected(ComponentName name, IBinder service){
			Log.i(LOG_TAG,"onServiceConnected");
			mUpgrade = IUpgrade.Stub.asInterface(service);
			ready = true;
			try {
					if (mUpgradeCallback!=null){
						mUpgrade.setCallback(mUpgradeCallback);
					}
					fireConnectionEvent();
					
				} catch (RemoteException e) {
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
			mUpgrade = null;
			mBind = false;
			ready = false;
			deleteAllEvents();
		}
	};

	
	public void setUpgradeService(IUpgrade upgrade) {
		Log.i(LOG_TAG,"setUpgradeService");
		this.mUpgrade = upgrade;
	}

	public IUpgrade getUpgradeService() {
		Log.i(LOG_TAG,"getUpgradeService");
		return mUpgrade;
	}

	/**
	 * Client should call this method for initialize binding between client to server service
	 * After success binding, client can start to work with the server by using aboutService instance 
	 * @return true if binding succeeded
	 */
	public boolean startBindService(){
		Log.i(LOG_TAG,"startBindService");
		if (!mBind){
			mBind = mContext.bindService(new Intent(IUpgrade.class.getName()),this.serviceConnection, Context.BIND_AUTO_CREATE);
		}
		return mBind;
	}

	/**
	 * Client should call this method for unBinding between client to server service.
	 * 
	 */
	public void stopBindService(){
		Log.i(LOG_TAG,"stopBindService");
		if (mBind){
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
	
}
