package com.peertv.service.ktvlogin.server;

import android.database.ContentObserver;
import android.os.Handler;

public class QueryContentObserver extends ContentObserver{

	private boolean mIsCheckPassword;
	public QueryContentObserver(Handler handler) {
		super(handler);
		// TODO Auto-generated constructor stub
	}
	public QueryContentObserver(Handler handler, boolean checkPassword) {
		super(handler);
		mIsCheckPassword = checkPassword;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onChange(boolean selfChange) {
		// TODO Auto-generated method stub
		super.onChange(selfChange);
		if (mIsCheckPassword){
			KTVLogin.checkPassword();
		}
		else{
			KTVLogin.changeToReady();
		}
	}

	
}
