package com.peertv.service.upgrade.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;


public class XMLPrhase {

	public static final String LOG_TAG = "XMLPrhase";
	public static  String URL = "";
	private HttpResponse HttpResponse= null;
	private String Version_Url="";
	private String[] result=new String[10];
	private int j=0,i=0;;
	
	public String exec(String url){
		Log.i(LOG_TAG,"exec: Phrasing " + url);
		URL=url;
		HttpResponse=getResponse();
		if(HttpResponse == null){
			Log.i(LOG_TAG,"HttpResponse: response from url - NetFail ");
			return "NetFail";
		}
		try {
			Version_Url=parseLink(HttpResponse.getEntity());
		} catch (IOException e) {e.printStackTrace();}
		Log.i(LOG_TAG,"exec: Xml Phrased: " + Version_Url);
		return Version_Url;
	}
	
	private HttpResponse getResponse(){
		Log.i(LOG_TAG,"HttpResponse: Getting response from url");
    	final HttpGet get = new HttpGet(URL);
    	HttpParams httpParameters = new BasicHttpParams();
    	HttpConnectionParams.setConnectionTimeout(httpParameters, 60000);
    	HttpConnectionParams.setSoTimeout(httpParameters, 60000);
        HttpClient client = new DefaultHttpClient(httpParameters);
        if (get != null){
				try {
					return client.execute(get);
				} catch (ClientProtocolException e) {e.printStackTrace();
				} catch (IOException e) {e.printStackTrace();}
				//Log.i(LOG_TAG,"HttpResponse: ---------------------------2");
        }
		return null;
	}
	
	private String parseLink(HttpEntity entity) throws IOException {
    	Log.i(LOG_TAG,"parseLink: Phrasing the XML file");
    	i=0;

    	InputStream mInputStream = entity.getContent();
    	InputStreamReader inputReader = new InputStreamReader(mInputStream);
    	
    	try {
    		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
    		factory.setNamespaceAware(false);
    		XmlPullParser xpp = factory.newPullParser();
    		
    		xpp.setInput(inputReader);
    		
    		int eventType = xpp.getEventType();
    		String startName = null;
    		String startValue = null;
    		
    		while (eventType != XmlPullParser.END_DOCUMENT) {
    			if (eventType == XmlPullParser.START_TAG) {
    				startName = xpp.getName();
    			}
    			else if (eventType == XmlPullParser.TEXT ){
    				startValue = xpp.getText();
    				if ("version".equals(startName)){
    					result[i]=startValue;
    					i++;
    				}
    			}
    			eventType = xpp.next();
    		}
    	} catch (XmlPullParserException e) {Log.e(LOG_TAG, "could not parse video feed", e);
    	} catch (IOException e) {Log.e(LOG_TAG, "could not process video stream", e);}
    	
   	
    	if (result[0].equals(null)){ // if nothing recieved
    		return "";
    	}
    	if(i==1){ // if the version url recieved
			return result[0];
		}
    	return getLatestVersion(result,i);
    }
	
	String getLatestVersion(String[] vers, int count) { // checks which of the versions avialivle is the latest one
		Log.i(LOG_TAG,"getLatestVersion "); 
		String CheckLatestVersion=vers[0];
		
    	while (count>0){
    		Log.i(LOG_TAG,"getLatestVersion: : ver " +count+": " + vers[count]); 
    		String[] temp = vers[count-1].split("\\."); // count-1 because at the end i=i+1
    		String[] tempLatestVersion = CheckLatestVersion.split("\\.");
    		for (j=0;j<=2;j++){
    			if(Integer.parseInt(tempLatestVersion[j]) < Integer.parseInt(temp[j])){
    				CheckLatestVersion=vers[count-1];
        			break;	
    			}
    		}
    		count--;
    	}
    	
    	
    	Log.i(LOG_TAG,"getLatestVersion: latest version to download is: " + CheckLatestVersion);
    	
    	return CheckLatestVersion;
		
	}

}
