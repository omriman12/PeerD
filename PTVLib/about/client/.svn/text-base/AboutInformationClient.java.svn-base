package com.peertv.service.about.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.peertv.service.about.server.IAboutInformation;
import com.peertv.service.settings.server.ISettingsService;

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
	public class AboutInformationClient{
	public static final String LOG_TAG = "AboutInformationServiceClient";
	private static boolean mBind = false;
	
	/**
	 * Instance of AIDL interface to work with AboutInformationServiceServer methods
	 * When binding with server succeeded, client can get about information. 
	 */
	public IAboutInformation aboutService = null;
	
	private Context mContext;
	
	/**
	 * Constructor of AboutInformationServiceClient which communicate with AboutInformationServiceServer
	 * @param	iContext - Activity context which AboutInformationServiceClient can start bind/unbind to service 
	 *			Server through this context.
	 */
	public AboutInformationClient(Context iContext){
		mContext = iContext;
	}
	
	protected ServiceConnection serviceConnection = new ServiceConnection(){
		/**
		 * When client call binding to service, and service succeeded to bind 
		 * the onServiceConnected callback fired from service to the client.
		 */
		public void onServiceConnected(ComponentName name, IBinder service){
			Log.i(LOG_TAG,"onServiceConnected");
			aboutService = IAboutInformation.Stub.asInterface(service);
		}
		
		/**
		 * When extreme critical issue happen (service crashed/killed)
		 * onServiceDisconnected callback fire.
		 * Note: This callback doesn't fire when client called to unBined.
		 */
		public void onServiceDisconnected(ComponentName name){	
			Log.i(LOG_TAG,"onServiceDisconnected");
			aboutService = null;
			mBind = false;
		}
	};

	
	public void setSettingsService(IAboutInformation aboutInformationService) {
		Log.i(LOG_TAG,"setSettingsService");
		this.aboutService = aboutInformationService;
	}

	public IAboutInformation getSettingsService() {
		//Log.i(LOG_TAG,"getSettingsService");
		return aboutService;
	}

	/**
	 * Client should call this method for initialize binding between client to server service
	 * After success binding, client can start to work with the server by using aboutService instance 
	 * @return true if binding succeeded
	 */
	public boolean startBindService(){
		if (!mBind){
			Log.i(LOG_TAG,"startBindService");
			mBind = mContext.bindService(new Intent(IAboutInformation.class.getName()),this.serviceConnection, Context.BIND_AUTO_CREATE);
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
	}
	
	/**
	 * Check if client bind to service 
	 * @return true if bind is active
	 */
	public boolean isBind(){
		return mBind;
	}
}
