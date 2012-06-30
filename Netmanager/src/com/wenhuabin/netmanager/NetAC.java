package com.wenhuabin.netmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator; 

import com.wenhuabin.netmanager.R;
import com.wenhuabin.netmanager.AppInfo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
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
import android.view.Menu;
import android.view.MenuItem;


public class NetAC extends Activity implements OnCheckedChangeListener{
	
	private ListView mListView;
    private ApplicationShow mAdapter;
    private List<AppInfo> apps;
    final private int MENU_TRAFFIC1 = Menu.FIRST;
	final private int MENU_TRAFFIC2 = Menu.FIRST + 1;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_list);
        
        mListView = (ListView) findViewById(R.id.mylist);
        apps = Api.loadAppInfomation(this);
        mAdapter = new ApplicationShow(this, apps);
        mListView.setAdapter(mAdapter);
        //View clearButton=findViewById(R.id.clear_button);
        //clearButton.setOnClickListener(this);
        //View applyButton=findViewById(R.id.apply_button);
        //applyButton.setOnClickListener(this);
    }
	/*
	public void onClick(View v){
    	//Intent i;
    	switch(v.getId()){
    	case R.id.clear_button:
    		Api.clearRules(this, true);
    	case R.id.apply_button:
    		Api.Apply(this, true);
    		break;
    	}
	}*/
	
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		final AppInfo app = (AppInfo) buttonView.getTag();
		int i=0;
		if (app != null) {
			switch (buttonView.getId()) {
				case R.id.select_wifi:
					if (app.selected_wifi != isChecked) {
						app.selected_wifi = isChecked;
						if(isChecked)i=1;else i=0;
						Log.v( String.valueOf(app.getUid()),String.valueOf(i));
					}
					break;
				case R.id.select_3g:
					if (app.selected_3g != isChecked) {
						app.selected_3g = isChecked;
						if(isChecked)i=1;else i=0;
						Log.v( String.valueOf(app.getUid()),String.valueOf(i+30));
					}
					break;
			}
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_TRAFFIC1, 0, "Clear");
		menu.add(0, MENU_TRAFFIC2, 0, "Apply");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		switch (item.getItemId()) {
		case MENU_TRAFFIC1:
			Api.clearRules(this, true);
			i=new Intent(this,NetAC.class);
			NetAC.this.finish();
    		startActivity(i);
    		break;
		case MENU_TRAFFIC2:
			Api.Apply(this, true);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public class ApplicationShow extends BaseAdapter{
		private List<AppInfo> apps;
		private LayoutInflater inflater;
		private Context ctx;
    
		public ApplicationShow (Context context, List<AppInfo> infos) {
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
    	
			ViewHolder holder;
			if(convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.netcontrol, null);
				holder.icon = (ImageView) convertView.findViewById(R.id.app_icon);
				holder.name = (TextView) convertView.findViewById(R.id.app_name);
				holder.uid = (TextView) convertView.findViewById(R.id.app_uid);
				holder.box_wifi = (CheckBox) convertView.findViewById(R.id.select_wifi);
				holder.box_3g = (CheckBox) convertView.findViewById(R.id.select_3g);
				holder.box_wifi.setOnCheckedChangeListener(NetAC.this);
				holder.box_3g.setOnCheckedChangeListener(NetAC.this);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.icon.setImageDrawable(apps.get(position).getIcon());
			holder.name.setText(apps.get(position).getName());
			holder.uid.setText(String.valueOf(apps.get(position).getUid()));
        
			final CheckBox box_wifi = holder.box_wifi;
			box_wifi.setTag(apps.get(position));
			box_wifi.setChecked(apps.get(position).selected_wifi);
			final CheckBox box_3g = holder.box_3g;
			box_3g.setTag(apps.get(position));
			box_3g.setChecked(apps.get(position).selected_3g);
		
			return convertView;
		}
	}
    
    private static class ViewHolder {
    	private ImageView icon;
    	private TextView name;
    	private TextView uid;
        private CheckBox box_wifi;
		private CheckBox box_3g;
    }
	
	

}
