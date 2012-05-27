package com.wenhuabin.netmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wenhuabin.netmanager.R;
import com.wenhuabin.netmanager.ApplicationShow;
import com.wenhuabin.netmanager.ApplicationInfo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class NetAC extends Activity{
	
	private ListView mListView;
    private ApplicationShow mAdapter;
    private List<ApplicationInfo> apps;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_list);
        
        mListView = (ListView) findViewById(R.id.mylist);
        //mListView.setOnItemClickListener(this);
        apps = loadAppInfomation(this);
        mAdapter = new ApplicationShow(this, apps);
        mListView.setAdapter(mAdapter);
    }
	
	private List<ApplicationInfo> loadAppInfomation(Context context) {
        List<ApplicationInfo> apps = new ArrayList<ApplicationInfo>();
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = pm.queryIntentActivities(intent, 0);
        Collections.sort(infos, new ResolveInfo.DisplayNameComparator(pm));
        if(infos != null) {
            apps.clear();
            for(int i=0; i<infos.size(); i++) {
                ApplicationInfo app = new ApplicationInfo();
                ResolveInfo info = infos.get(i);
                app.setName(info.loadLabel(pm).toString());
                app.setIcon(info.loadIcon(pm));
                app.setIntent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
                apps.add(app);
            }
        }
        return apps;
    }

}
