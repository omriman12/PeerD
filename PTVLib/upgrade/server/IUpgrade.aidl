package com.peertv.service.upgrade.server;

import com.peertv.service.upgrade.server.IUpgradeCallback;
interface IUpgrade
{
	String checkForUpgrade();
	void startDownload();
	void stopDownload();
	boolean isDownloading();
	int downloadProgress();
	void rebootAndUpgrade();	
	void setCallback(IUpgradeCallback upgradeCallback);
}