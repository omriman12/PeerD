package com.peertv.service.upgrade.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UpgradeReceiver extends BroadcastReceiver {

	private static final String LOG_TAG = "UpgradeReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(LOG_TAG, "onReceive");
//		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
//			Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())	||
//			Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())||
//			Intent.ACTION_PACKAGE_CHANGED.equals(intent.getAction()) ||
//			Intent.ACTION_PACKAGE_INSTALL.equals(intent.getAction())
//				) {
	      context.startService(new Intent(context, Upgrade.class));    
//	    }
	}
}
