package com.wenhuabin.netmanager;

//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;

import android.app.Activity;
//import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import java.io.DataOutputStream;

//import android.content.ComponentName;
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.content.pm.ResolveInfo;
//import android.widget.AdapterView;
//import android.widget.ListView;

public class NetmanagerActivity extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        String apkRoot="chmod 777 "+getPackageCodePath();//SD卡分区路径，也可能是mmcblk1随系统版本定，当前程序路径请用getPackageCodePath();  
        RootCmd(apkRoot);  
        
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
        	case R.id.exit_button:
        		finish();
        		break;
        	}
    	
    }
    
    public boolean RootCmd(String cmd){  
        Process process = null;  
        DataOutputStream os = null;  
        try{  
            process = Runtime.getRuntime().exec("su");  
            os = new DataOutputStream(process.getOutputStream());  
            os.writeBytes(cmd+ "\n");  
            os.writeBytes("exit\n");  
            os.flush();  
            process.waitFor();  
        } catch (Exception e) {  
            return false;  
        } finally {  
            try {  
                if (os != null)   {  
                    os.close();  
                }  
                process.destroy();  
            } catch (Exception e) {  
            }  
        }  
        return true;  
    }  
    
}

