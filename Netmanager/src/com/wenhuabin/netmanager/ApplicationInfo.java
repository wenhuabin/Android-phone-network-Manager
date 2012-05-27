package com.wenhuabin.netmanager;

import android.content.ComponentName;
import android.graphics.drawable.Drawable;


public class ApplicationInfo 
{
	String name;
	ComponentName intent;
	Drawable icon;
   
	public String getName () {
	    return name;
	}

	public void setName (String name) {
	    this.name = name;
	}

	public ComponentName getIntent () {
	     return intent;
	}
	public void setIntent (ComponentName intent) {
	    this.intent = intent;
	}

	public Drawable getIcon () {
		return icon;
	}
	
	public void setIcon (Drawable icon) {
		this.icon = icon;
	}

}
