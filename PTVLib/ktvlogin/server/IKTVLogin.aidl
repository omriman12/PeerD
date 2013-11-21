package com.peertv.service.ktvlogin.server;

import com.peertv.service.ktvlogin.server.IKTVLoginCallback;
interface IKTVLogin
{
	String getUserId();
	String getPassword();
	String getStatus();
	String startLogin(String userId, String password);
	void setCallback(IKTVLoginCallback ktvLoginCallback);
	void setUserId(String userId);
	void setPassword(String password);
	void setLogout();
}