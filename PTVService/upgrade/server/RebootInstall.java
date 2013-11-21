package com.peertv.service.upgrade.server;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.os.RecoverySystem;
import android.util.Log;

public class RebootInstall{
	private static final String LOG_TAG = "RebootInstall";
	private Context mContext = null;
	
	public RebootInstall(Context context){
		Log.i(LOG_TAG, "Constractor");
		this.mContext = context;
	}
	
	public void startInstallPackage(File firmwareFile){
		try {
			Log.i(LOG_TAG, "Start install");
			RecoverySystem.installPackage(mContext, firmwareFile);
		} catch (IOException e) {
			e.printStackTrace();
		}                    
	}
	
}
		
