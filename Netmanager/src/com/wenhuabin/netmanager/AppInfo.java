package com.wenhuabin.netmanager;

import android.content.ComponentName;
import android.graphics.drawable.Drawable;

public class AppInfo {
	int uid;
	String name;
	ComponentName intent;
	Drawable icon;
	boolean selected_wifi;
	boolean selected_3g;
	double ex_upload;
	double ex_download;
	double upload;
	double download;
	
	public void AppInit()
	{
		//selected_wifi=true;
		ex_upload=0.0;
		ex_download=0.0;
		upload=0.0;
		download=0.0;
	}
	
	public String getName () {
	    return name;
	}

	public int getUid () {
	    return uid;
	}

	public boolean getWifi () {
	    return selected_wifi;
	}

	public boolean get3g () {
	    return selected_3g;
	}
	
	public ComponentName getIntent () {
	     return intent;
	}
	

	public Drawable getIcon () {
		return icon;
	}
	
	public double getUL () {
	    return upload;
	}

	public double getDL () {
	    return download;
	}

	public double getTOUL () {
	    return ex_upload+upload;
	}
	
	public double getTODL () {
	    return ex_download+download;
	}
	
	public double getTOSum () {
		double x=ex_upload+upload +ex_download+download;
	    return x;
	}
	
	public void setUid (int uid) {
	    this.uid = uid;
	}
	
	public void setName (String name) {
	    this.name = name;
	}
	
	public void setIcon (Drawable icon) {
		this.icon = icon;
	}
	
	public void setWifi (boolean selected_wifi) {
	    this.selected_wifi = selected_wifi;
	}
	
	public void set3g (boolean selected_3g) {
	    this.selected_3g = selected_3g;
	}
	
	public void setUL (double uload) {
	    this.upload = uload;
		//this.upload = 5.5;
	}
	
	public void setDL (double dload) {
	    this.download = dload;
		//this.download = 3.5;
	}
	
	public void setIntent (ComponentName intent) {
	    this.intent = intent;
	}
	
	public void setEx (double a,double b)
	{
		ex_upload = a;
		ex_download = b;
	}

}
