package com.wenhuabin.netmanager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator; 

import com.wenhuabin.netmanager.R;
import com.wenhuabin.netmanager.AppInfo;
import com.wenhuabin.netmanager.NetAC.ApplicationShow;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ListAdapter;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;

public class TrafficStat extends Activity{
	
	private ListView tListView;
    private AppShow tAdapter;
    private List<AppInfo> apps;
    final private int MENU_TRAFFIC1 = Menu.FIRST;
	final private int MENU_TRAFFIC2 = Menu.FIRST + 1;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.traffic);
        
        tListView = (ListView) findViewById(R.id.traffic_list);
        apps = Api.loadAppInfomation(this);
        int r = Api.ReadTraffic(this);
        //if(r>0)Log.v("READ", String.valueOf(r));
        //else if (r == -1)Log.v("READ", String.valueOf(r));
       // else Log.v("read",String.valueOf(r));
        Api.Traffic(this);
        tAdapter = new AppShow(this, apps);
        tListView.setAdapter(tAdapter);
        /*
        View RefreshButton=findViewById(R.id.refresh_button);
        RefreshButton.setOnClickListener(this);
        View SaveButton=findViewById(R.id.save_button);
        SaveButton.setOnClickListener(this);*/
    }
	/*
	public void onClick(View v){
    	//Intent i;
    	switch(v.getId()){
    	case R.id.save_button:
    		if(Api.saveTraffic(this))Log.v("clicktraffic", "true");
    		break;
    	case R.id.refresh_button:
    		Api.Traffic(this);
    		break;
    	}
	}*/
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_TRAFFIC1, 0, "Save");
		menu.add(0, MENU_TRAFFIC2, 0, "Refresh");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		switch (item.getItemId()) {
		case MENU_TRAFFIC1:
    		if(Api.saveTraffic(this))Log.v("clicktraffic", "true");
    		break;
		case MENU_TRAFFIC2:
			i=new Intent(this,TrafficStat.class);
			TrafficStat.this.finish();
    		startActivity(i);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public class AppShow extends BaseAdapter{
		private List<AppInfo> apps;
		private LayoutInflater inflater;
		private Context ctx;
    
		public AppShow (Context context, List<AppInfo> infos) {
			this.apps = infos;
			ctx = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
    
		@Override
		public int getCount(){
			return apps.size();
		}
    
		@Override
		public Object getItem(int position) {
			return position;
		}
		
		@Override
		public long getItemId(int position) {
        return position;
		}
    
		@Override
		public View getView (int position, View convertView, ViewGroup parent) {
    	
			TrafficHolder holder;
			if(convertView == null) {
				holder = new TrafficHolder();
				convertView = inflater.inflate(R.layout.trafficlist, null);
				holder.icon = (ImageView) convertView.findViewById(R.id.application_icon);
				holder.name = (TextView) convertView.findViewById(R.id.application_name);
				holder.uload = (TextView) convertView.findViewById(R.id.application_TxBytes);
				holder.dload = (TextView) convertView.findViewById(R.id.application_RxBytes);
				holder.sload = (TextView) convertView.findViewById(R.id.application_ToBytes);
				
				convertView.setTag(holder);
			} else {
				holder = (TrafficHolder) convertView.getTag();
			}
			holder.icon.setImageDrawable(apps.get(position).getIcon());
			holder.name.setText(apps.get(position).getName());
			int index=String.valueOf(apps.get(position).getTOUL()).indexOf(".");
			holder.uload.setText(String.valueOf(apps.get(position).getTOUL()).substring(0, index+2));
			index=String.valueOf(apps.get(position).getTODL()).indexOf(".");
			holder.dload.setText(String.valueOf(apps.get(position).getTODL()).substring(0, index+2));
			index=String.valueOf(apps.get(position).getTOSum()).indexOf(".");
			holder.sload.setText(String.valueOf(apps.get(position).getTOSum()).substring(0, index+2));
			
			return convertView;
		}
	}
    
    private static class TrafficHolder {
    	private ImageView icon;
    	private TextView name;
    	private TextView uload;
    	private TextView dload;
    	private TextView sload;
        
    }

}
