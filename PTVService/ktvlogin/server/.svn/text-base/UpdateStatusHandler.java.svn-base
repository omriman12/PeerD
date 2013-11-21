package com.peertv.service.ktvlogin.server;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

public class UpdateStatusHandler extends Handler{

	public UpdateStatusHandler(Looper looper){
		super(looper);
	}

	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleMessage(msg);
		
		IKTVLoginCallback iKTVLoginCallback = (IKTVLoginCallback)msg.obj;
		
		if(msg.what == 0){
			if (iKTVLoginCallback!=null){
				KTVLogin.setStatus(KTVLogin.STATUS_LOGIN_NO_USER);
				try {
					iKTVLoginCallback.statusChange(KTVLogin.STATUS_LOGIN_NO_USER);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	}
	
	
}
