package com.peertv.service.upgrade.server;


interface IUpgradeCallback
{
	String onCheckingDone(String status);
	String onDownloadDone(String status);

}