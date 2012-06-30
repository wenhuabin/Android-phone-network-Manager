package com.wenhuabin.netmanager;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;



import android.content.Context;

public class NetmanagerActivity extends Activity implements OnClickListener{
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
      //set up click listeners for all buttons
        View netAcessButton=findViewById(R.id.net_button);
        netAcessButton.setOnClickListener(this);
        View sumButton=findViewById(R.id.sum_button);
        sumButton.setOnClickListener(this);
        View aboutButton=findViewById(R.id.about_button);
        aboutButton.setOnClickListener(this);
        View exitButton=findViewById(R.id.exit_button);
        exitButton.setOnClickListener(this);
	}
        
        public void onClick(View v){
        	Intent i;
        	switch(v.getId()){
        	case R.id.about_button:
        		i=new Intent(this,About.class);
        		startActivity(i);
        		break;
        	case R.id.net_button:
        		i=new Intent(this,NetAC.class);
        		startActivity(i);
        		break;
        	case R.id.sum_button:
        		i=new Intent(this,TrafficStat.class);
        		startActivity(i);
        		break;
        	case R.id.exit_button:
        		finish();
        		break;
        	}
    	
    }
    
}

