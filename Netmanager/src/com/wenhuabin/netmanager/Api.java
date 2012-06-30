package com.wenhuabin.netmanager;

import com.wenhuabin.netmanager.R;
import com.wenhuabin.netmanager.AppInfo;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;


import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.net.TrafficStats;

public class Api {
	
	public static final String PREFS_NAME 		= "netmanagerPrefs";
	public static final String PREF_3G_UIDS		= "BlockUids3G";
	public static final String PREF_WIFI_UIDS	= "BlockUidsWifi";
	public static final String PREF_UIDS		= "Uid";
	public static final String PREF_UP		    = "Upload";
	public static final String PREF_DOWN		= "Download";
	
	
	private static final String SCRIPT_FILE     = "netmanager.sh";
	
	
	private static boolean hasroot = false;
	public static List<AppInfo> applications = null;
	
	
	private static String scriptHeader(Context ctx) {
		final String dir = ctx.getDir("bin",0).getAbsolutePath();
		final String myiptables = dir + "/iptables_armv5";
		return "" +
			"IPTABLES=iptables\n" +
			"BUSYBOX=busybox\n" +
			"GREP=grep\n" +
			"ECHO=echo\n" +
			"# Try to find busybox\n" +
			"if " + dir + "/busybox_g1 --help >/dev/null 2>/dev/null ; then\n" +
			"	BUSYBOX="+dir+"/busybox_g1\n" +
			"	GREP=\"$BUSYBOX grep\"\n" +
			"	ECHO=\"$BUSYBOX echo\"\n" +
			"elif busybox --help >/dev/null 2>/dev/null ; then\n" +
			"	BUSYBOX=busybox\n" +
			"elif /system/xbin/busybox --help >/dev/null 2>/dev/null ; then\n" +
			"	BUSYBOX=/system/xbin/busybox\n" +
			"elif /system/bin/busybox --help >/dev/null 2>/dev/null ; then\n" +
			"	BUSYBOX=/system/bin/busybox\n" +
			"fi\n" +
			"# Try to find grep\n" +
			"if ! $ECHO 1 | $GREP -q 1 >/dev/null 2>/dev/null ; then\n" +
			"	if $ECHO 1 | $BUSYBOX grep -q 1 >/dev/null 2>/dev/null ; then\n" +
			"		GREP=\"$BUSYBOX grep\"\n" +
			"	fi\n" +
			"	# Grep is absolutely required\n" +
			"	if ! $ECHO 1 | $GREP -q 1 >/dev/null 2>/dev/null ; then\n" +
			"		$ECHO The grep command is required. DroidWall will not work.\n" +
			"		exit 1\n" +
			"	fi\n" +
			"fi\n" +
			"# Try to find iptables\n" +
			"# Added if iptables binary already in system then use it, if not use implemented one\n" + 
			"if ! command -v iptables &> /dev/null; then\n" +
			"if " + myiptables + " --version >/dev/null 2>/dev/null ; then\n" +
			"	IPTABLES="+myiptables+"\n" +
			"fi\nfi\n" +
			"";
	}
	
	private static void copyRawFile(Context ctx, int resid, File file, String mode) throws IOException, InterruptedException
	{
		final String abspath = file.getAbsolutePath();
		// Write the iptables binary
		final FileOutputStream out = new FileOutputStream(file);
		final InputStream is = ctx.getResources().openRawResource(resid);
		byte buf[] = new byte[1024];
		int len;
		while ((len = is.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		out.close();
		is.close();
		// Change the permissions
		Runtime.getRuntime().exec("chmod "+mode+" "+abspath).waitFor();
	}
	
	public static boolean assertBinaries(Context ctx, boolean showErrors) {
		boolean changed = false;
		try {
			// Check iptables_armv5
			File file = new File(ctx.getDir("bin",0), "iptables_armv5");
			if (!file.exists() || file.length()!=198652) {
				copyRawFile(ctx, R.raw.iptables_armv5, file, "755");
				changed = true;
			}
			// Check busybox
			file = new File(ctx.getDir("bin",0), "busybox_g1");
			if (!file.exists()) {
				copyRawFile(ctx, R.raw.busybox_g1, file, "755");
				changed = true;
			}
			if (changed) {
				Toast.makeText(ctx, R.string.toast_bin_installed, Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			if (showErrors) alert(ctx, "Error installing binary files: " + e);
			return false;
		}
		return true;
	}
	
	public static void alert(Context ctx, CharSequence msg) {
    	if (ctx != null) {
        	new AlertDialog.Builder(ctx)
        	.setNeutralButton(android.R.string.ok, null)
        	.setMessage(msg)
        	.show();
    	}
    }
	
	public static List<AppInfo> loadAppInfomation(Context context) {
		//if all applications are not load before,load first
		if(applications!=null)
		{
			return applications;
		}
		//read rules of uids which are not allowed to acess 2G/3G and wifi
		final SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		final String savedUids_wifi = prefs.getString(PREF_WIFI_UIDS, "");
		final String savedUids_3g = prefs.getString(PREF_3G_UIDS, "");
		int selected_wifi[] = new int[0];
		int selected_3g[] = new int[0];
		//rules of uids which are not allowed to acess 2G/3G
		if (savedUids_wifi.length() > 0) {
			final StringTokenizer tok = new StringTokenizer(savedUids_wifi, "|");
			selected_wifi = new int[tok.countTokens()];
			for (int i=0; i<selected_wifi.length; i++) {
				final String uid = tok.nextToken();
				if (!uid.equals("")) {
					try {
						selected_wifi[i] = Integer.parseInt(uid);
					} catch (Exception ex) {
						selected_wifi[i] = -1;
					}
				}
			}
			Arrays.sort(selected_wifi);
		}
		//rules of uids which are not allowed to acess wifi
		if (savedUids_3g.length() > 0) {
			final StringTokenizer tok = new StringTokenizer(savedUids_3g, "|");
			selected_3g = new int[tok.countTokens()];
			for (int i=0; i<selected_3g.length; i++) {
				final String uid = tok.nextToken();
				if (!uid.equals("")) {
					try {
						selected_3g[i] = Integer.parseInt(uid);
					} catch (Exception ex) {
						selected_3g[i] = -1;
					}
				}
			}
			Arrays.sort(selected_3g);
		}
		//load all applications and check the rules save before
		PackageManager packageManager = context.getPackageManager();
		List<ApplicationInfo> applicationInfos = packageManager.getInstalledApplications(0); 
		applications = new ArrayList<AppInfo>();
        Iterator<ApplicationInfo> iterator = applicationInfos.iterator();  
        while(iterator.hasNext()){  
            ApplicationInfo applicationInfo = iterator.next();
            AppInfo app = new AppInfo();
            app.AppInit();
            //String packageName = applicationInfo.packageName;// 包名  
            app.setName(packageManager.getApplicationLabel(applicationInfo).toString());//获取label  
            app.setIcon(packageManager.getApplicationIcon(applicationInfo));
            
            app.setUid(applicationInfo.uid);
            if(app.getUid()<10000)continue;
            //Log.v(String.valueOf(app.getUid()), String.valueOf(applicationInfo.FLAG_SYSTEM));
            if (Arrays.binarySearch(selected_wifi, app.getUid()) >= 0) {
				app.setWifi(true);
			}
            else app.setWifi(false);
			if (Arrays.binarySearch(selected_3g, app.getUid()) >= 0) {
				app.set3g(true);
			}
			else app.set3g(false);
            Log.v("Uid", String.valueOf(app.getUid()));
            applications.add(app);
        }  
        Log.v("appsSize",String.valueOf(applications.size()));
        return applications;
    }
	
	public static boolean Apply(Context context, boolean showErrors) {
		if (context == null) {
			return false;
		}
		saveRules(context);
		
		final SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		final String savedUids_wifi = prefs.getString(PREF_WIFI_UIDS, "");
		final String savedUids_3g = prefs.getString(PREF_3G_UIDS, "");
		final List<Integer> uids_wifi = new LinkedList<Integer>();
		if (savedUids_wifi.length() > 0) {
			// Check applications disallowed on wifi
			final StringTokenizer tok = new StringTokenizer(savedUids_wifi, "|");
			while (tok.hasMoreTokens()) {
				final String uid = tok.nextToken();
				if (!uid.equals("")) {
					try {
						uids_wifi.add(Integer.parseInt(uid));
					} catch (Exception ex) {
					}
				}
			}
		}
		final List<Integer> uids_3g = new LinkedList<Integer>();
		if (savedUids_3g.length() > 0) {
			// Check applications disallowed on 2G/3G
			final StringTokenizer tok = new StringTokenizer(savedUids_3g, "|");
			while (tok.hasMoreTokens()) {
				final String uid = tok.nextToken();
				if (!uid.equals("")) {
					try {
						uids_3g.add(Integer.parseInt(uid));
					} catch (Exception ex) {
					}
				}
			}
		}
		return applyRules(context, uids_wifi, uids_3g, showErrors);
	}
	
	private static boolean applyRules(Context context, List<Integer> uidsWifi, List<Integer> uids3g, boolean showErrors) {
		if (context == null) {
			return false;
		}
		assertBinaries(context, showErrors);
		final String ITFS_WIFI[] = {"tiwlan+", "wlan+", "eth+", "ra+"};
		final String ITFS_3G[] = {"rmnet+","pdp+","ppp+","uwbr+","wimax+","vsnet+","ccmni+","usb+"};

    	final StringBuilder script = new StringBuilder();
		try {
			int exitcode;
			script.append(scriptHeader(context));
			script.append("" +
				"$IPTABLES --version || exit 1\n" +
				"# Create the droidwall chains if necessary\n" +
				"$IPTABLES -L droidwall >/dev/null 2>/dev/null || $IPTABLES --new droidwall || exit 2\n" +
				"$IPTABLES -L droidwall-3g >/dev/null 2>/dev/null || $IPTABLES --new droidwall-3g || exit 3\n" +
				"$IPTABLES -L droidwall-wifi >/dev/null 2>/dev/null || $IPTABLES --new droidwall-wifi || exit 4\n" +
				"$IPTABLES -L droidwall-reject >/dev/null 2>/dev/null || $IPTABLES --new droidwall-reject || exit 5\n" +
				"# Add droidwall chain to OUTPUT chain if necessary\n" +
				"$IPTABLES -L OUTPUT | $GREP -q droidwall || $IPTABLES -A OUTPUT -j droidwall || exit 6\n" +
				"# Flush existing rules\n" +
				"$IPTABLES -F droidwall || exit 7\n" +
				"$IPTABLES -F droidwall-3g || exit 8\n" +
				"$IPTABLES -F droidwall-wifi || exit 9\n" +
				"$IPTABLES -F droidwall-reject || exit 10\n" +
			"");
			script.append("" + "# Create the reject rule (log disabled)\n" +
				"$IPTABLES -A droidwall-reject -j REJECT || exit 11\n" + "");
			
			script.append("# Main rules (per interface)\n");
			for (final String itf : ITFS_3G) {
				script.append("$IPTABLES -A droidwall -o ").append(itf).append(" -j droidwall-3g || exit\n");
			}
			for (final String itf : ITFS_WIFI) {
				script.append("$IPTABLES -A droidwall -o ").append(itf).append(" -j droidwall-wifi || exit\n");
			}
			
			script.append("# Filtering rules\n");
			final String targetRule = "droidwall-reject";
			
			// When "white listing" wifi, we need to ensure that the dhcp and wifi users are allowed
			int uid = android.os.Process.getUidForName("dhcp");
			if (uid != -1) {
				script.append("# dhcp user\n");
				script.append("$IPTABLES -A droidwall-wifi -m owner --uid-owner ").append(uid).append(" -j RETURN || exit\n");
			}
			uid = android.os.Process.getUidForName("wifi");
			if (uid != -1) {
				script.append("# wifi user\n");
				script.append("$IPTABLES -A droidwall-wifi -m owner --uid-owner ").append(uid).append(" -j RETURN || exit\n");
			}
			/* release/block individual applications on this interface */
			for (int i=0;i < uids3g.size();i++) {
				if (uid >= 0) script.append("$IPTABLES -A droidwall-3g -m owner --uid-owner ").append(uids3g.get(i)).append(" -j ").append(targetRule).append(" || exit\n");
			}
			
			/* release/block individual applications on this interface */
			for (int i=0;i < uidsWifi.size();i++) {
				if (uid >= 0) script.append("$IPTABLES -A droidwall-wifi -m owner --uid-owner ").append(uidsWifi.get(i)).append(" -j ").append(targetRule).append(" || exit\n");
			}
			
	    	final StringBuilder res = new StringBuilder();
			exitcode = runScriptAsRoot(context, script.toString(), res);
			if (showErrors && exitcode != 0) {
				String msg = res.toString();
				Log.e("Netmanager", msg);
				// Remove unnecessary help message from output
				if (msg.indexOf("\nTry `iptables -h' or 'iptables --help' for more information.") != -1) {
					msg = msg.replace("\nTry `iptables -h' or 'iptables --help' for more information.", "");
				}
				alert(context, "Error applying iptables rules. Exit code: " + exitcode + "\n\n" + msg.trim());
			} else {
				return true;
			}
		} catch (Exception e) {
			if (showErrors) alert(context, "error refreshing iptables: " + e);
		}
		return false;
    }


	public static void saveRules(Context context) {
		final SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		final List<AppInfo>  apps = loadAppInfomation(context);
		//save uids that's not allowed to acess the 2G/3G and wifi by SharedPreferences
		final StringBuilder newuids_wifi = new StringBuilder();
		final StringBuilder newuids_3g = new StringBuilder();
		for (int i=0; i<apps.size(); i++) {
			if (apps.get(i).getWifi()) {
				if (newuids_wifi.length() != 0) newuids_wifi.append('|');
				newuids_wifi.append(apps.get(i).getUid());
			}
			if (apps.get(i).get3g()) {
				if (newuids_3g.length() != 0) newuids_3g.append('|');
				newuids_3g.append(apps.get(i).getUid());
			}
		}
		
		final Editor edit = prefs.edit();
		edit.putString(PREF_WIFI_UIDS, newuids_wifi.toString());
		edit.putString(PREF_3G_UIDS, newuids_3g.toString());
		edit.commit();
    }
	
	public static boolean saveTraffic(Context context) {
		final SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		final List<AppInfo>  apps = loadAppInfomation(context);
		//save traffic data by SharedPreferences
		final StringBuilder uid = new StringBuilder();
		final StringBuilder uload = new StringBuilder();
		final StringBuilder dload = new StringBuilder();
		for (int i=0; i<apps.size(); i++) {
			//apps.get(i).save_all();
			if(uid.length()!=0)uid.append('|');
			uid.append(apps.get(i).getUid());
			
			if(uload.length()!=0)uload.append('|');
			uload.append(apps.get(i).getTOUL());
			
			if(dload.length()!=0)dload.append('|');
			dload.append(apps.get(i).getTODL());
		}
		final Editor edit = prefs.edit();
		edit.putString(PREF_UIDS, uid.toString());
		edit.putString(PREF_UP, uload.toString());
		edit.putString(PREF_DOWN, dload.toString());
		edit.commit();
		Log.v("savetraffic", "ddddff");
		return true;
    }
	
	public static int ReadTraffic(Context context) {
		final SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		final List<AppInfo>  apps = loadAppInfomation(context);
		final List<tempAppinfo> tempapps = new ArrayList<tempAppinfo>();
		//read store traffic data by SharedPreferences
		final String uid = prefs.getString(PREF_UIDS, "");
		final String uload = prefs.getString(PREF_UP, "");
		final String dload = prefs.getString(PREF_DOWN, "");
		int check=0;
		int saved_uid[] = new int[0];
		int saved_uid_temp[] = new int[0];
		int change[] = new int[0];
		//read all uids and sort for biarry search later
		if (uid.length()>0) {
			final StringTokenizer tok1 = new StringTokenizer(uid, "|");
			saved_uid = new int[tok1.countTokens()];
			saved_uid_temp = new int[tok1.countTokens()];
			change = new int[tok1.countTokens()];
			check=tok1.countTokens();
			for (int i=0; i<saved_uid.length; i++) {
				final String temp = tok1.nextToken();
				tempAppinfo ta= new tempAppinfo();
				if (!temp.equals("")) {
					try {
						saved_uid[i] = Integer.parseInt(temp);
						saved_uid_temp[i] = saved_uid[i];
						ta.uid=saved_uid[i];
						tempapps.add(ta);
					} catch (Exception ex) {
						saved_uid[i]=-1;
					}
				}
			}
			Arrays.sort(saved_uid);
			for(int i=0;i != change.length;i++)
			{
				change[Arrays.binarySearch(saved_uid, saved_uid_temp[i])] = i;
			}
		}
		//read all upload traffic store before
		if (uload.length()>0) {
			final StringTokenizer tok2 = new StringTokenizer(uload, "|");
			if(check != tok2.countTokens()) return -1;
			for (int i=0; i< check; i++) {
				final String temp = tok2.nextToken();
				if (!temp.equals("")) {
					try {
						tempapps.get(i).upload=Double.parseDouble(temp);
					} catch (Exception ex) {
					}
				}
			}
		}
		//read all download traffic store before
		if (dload.length()>0) {
			final StringTokenizer tok3 = new StringTokenizer(dload, "|");
			if(check != tok3.countTokens()) return -1;
			for (int i=0; i<check; i++) {
				final String temp = tok3.nextToken();
				if (!temp.equals("")) {
					try {
						tempapps.get(i).download=Double.parseDouble(temp);
					} catch (Exception ex) {
					}
				}
			}
		}
		
		int k=0;
		for(int i = 0;i != apps.size();i++)
		{
			if ((k=Arrays.binarySearch(saved_uid, apps.get(i).getUid())) >= 0) {
				apps.get(i).setEx(tempapps.get(change[k]).upload,tempapps.get(change[k]).download);
			}
		}
		
		return check;
    }
	
	
	public static void Traffic(Context context)
	{
		//if all applictions are not load yet,load first
		if(applications == null)
			applications =loadAppInfomation(context);
		Iterator<AppInfo> iterator = applications.iterator();
		double temp_upload=0,temp_download=0;
		while(iterator.hasNext())
		{
			AppInfo temp = iterator.next();
			//confingure traffic(both 2G/3G and wifi) after phone on boot by uid
			temp_upload = TrafficStats.getUidTxBytes(temp.getUid()) / 1024.0;
			temp_download = TrafficStats.getUidRxBytes(temp.getUid()) / 1024.0;
			if(temp_upload<=0)temp_upload=0;
			if(temp_download<=0)temp_download=0;
			temp.setUL(temp_upload);
			temp.setDL(temp_download);
		}
	}
    
    public static boolean hasRootAccess(final Context ctx, boolean showErrors) {
		if (hasroot) return true;
		final StringBuilder res = new StringBuilder();
		try {
			// Run an empty script just to check root access
			if (runScriptAsRoot(ctx, "exit 0", res) == 0) {
				hasroot = true;
				return true;
			}
		} catch (Exception e) {
		}
		if (showErrors) {
			alert(ctx, "Could not acquire root access.\n" +
				"You need a rooted phone to run Netmanager.\n\n" +
				"If this phone is already rooted, please make sure Netmanager has enough permissions to execute the \"su\" command.\n" +
				"Error message: " + res.toString());
		}
		return false;
	}
    
	public static int runScript(Context ctx, String script, StringBuilder res, long timeout, boolean asroot) {
		final File file = new File(ctx.getDir("bin",0), SCRIPT_FILE);
		final ScriptRunner runner = new ScriptRunner(file, script, res, asroot);
		runner.start();
		try {
			if (timeout > 0) {
				runner.join(timeout);
			} else {
				runner.join();
			}
			if (runner.isAlive()) {
				// Timed-out
				runner.interrupt();
				runner.join(150);
				runner.destroy();
				runner.join(50);
			}
		} catch (InterruptedException ex) {}
		return runner.exitcode;
	}
    
	public static int runScriptAsRoot(Context ctx, String script, StringBuilder res, long timeout) {
		return runScript(ctx, script, res, timeout, true);
    }
    
	public static int runScriptAsRoot(Context ctx, String script, StringBuilder res) throws IOException {
		return runScriptAsRoot(ctx, script, res, 40000);
	}
    
	public static int runScript(Context ctx, String script, StringBuilder res) throws IOException {
		return runScript(ctx, script, res, 40000, false);
	}
	
	public static void saveRules(Context ctx,List<AppInfo> apps) {
		final SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, 0);
		// Builds a pipe-separated list of names
		final StringBuilder newuids_wifi = new StringBuilder();
		final StringBuilder newuids_3g = new StringBuilder();
		for (int i=0; i<apps.size(); i++) {
			if (apps.get(i).selected_wifi) {
				if (newuids_wifi.length() != 0) newuids_wifi.append('|');
				newuids_wifi.append(apps.get(i).getUid());
			}
			if (apps.get(i).selected_3g) {
				if (newuids_3g.length() != 0) newuids_3g.append('|');
				newuids_3g.append(apps.get(i).getUid());
			}
		}
		
		final Editor edit = prefs.edit();
		edit.putString(PREF_WIFI_UIDS, newuids_wifi.toString());
		edit.putString(PREF_3G_UIDS, newuids_3g.toString());
		edit.commit();
    }
	
	
	
	
	public static boolean clearRules(Context context, boolean showErrors) {
    	final StringBuilder res = new StringBuilder();
		try {
			assertBinaries(context, showErrors);
	    	final StringBuilder script = new StringBuilder();
	    	script.append(scriptHeader(context));
	    	script.append("" +
					"$IPTABLES -F droidwall\n" +
					"$IPTABLES -F droidwall-reject\n" +
					"$IPTABLES -F droidwall-3g\n" +
					"$IPTABLES -F droidwall-wifi\n" +
	    			"");
			int code = runScriptAsRoot(context, script.toString(), res);
			if (code == -1) {
				if (showErrors) alert(context, "Error purging iptables. exit code: " + code + "\n" + res);
				return false;
			}
			Iterator<AppInfo> iterator = applications.iterator();
			//double temp_upload=0,temp_download=0;
			while(iterator.hasNext())
			{
				AppInfo temp = iterator.next();
				temp.set3g(false);
				temp.setWifi(false);
			}
			saveRules(context);
			return true;
		} catch (Exception e) {
			if (showErrors) alert(context, "Error purging iptables: " + e);
			return false;
		}
    }
	
	private static final class ScriptRunner extends Thread {
		private final File file;
		private final String script;
		private final StringBuilder res;
		private final boolean asroot;
		public int exitcode = -1;
		private Process exec;
		
		public ScriptRunner(File file, String script, StringBuilder res, boolean asroot) {
			this.file = file;
			this.script = script;
			this.res = res;
			this.asroot = asroot;
		}
		@Override
		public void run() {
			try {
				file.createNewFile();
				final String abspath = file.getAbsolutePath();
				// make sure we have execution permission on the script file
				Runtime.getRuntime().exec("chmod 777 "+abspath).waitFor();
				// Write the script to be executed
				final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file));
				if (new File("/system/bin/sh").exists()) {
					out.write("#!/system/bin/sh\n");
				}
				out.write(script);
				if (!script.endsWith("\n")) out.write("\n");
				out.write("exit\n");
				out.flush();
				out.close();
				if (this.asroot) {
					// Create the "su" request to run the script
					exec = Runtime.getRuntime().exec("su -c "+abspath);
				} else {
					// Create the "sh" request to run the script
					exec = Runtime.getRuntime().exec("sh "+abspath);
				}
				final InputStream stdout = exec.getInputStream();
				final InputStream stderr = exec.getErrorStream();
				final byte buf[] = new byte[8192];
				int read = 0;
				while (true) {
					final Process localexec = exec;
					if (localexec == null) break;
					try {
						// get the process exit code - will raise IllegalThreadStateException if still running
						this.exitcode = localexec.exitValue();
					} catch (IllegalThreadStateException ex) {
						// The process is still running
					}
					// Read stdout
					if (stdout.available() > 0) {
						read = stdout.read(buf);
						if (res != null) res.append(new String(buf, 0, read));
					}
					// Read stderr
					if (stderr.available() > 0) {
						read = stderr.read(buf);
						if (res != null) res.append(new String(buf, 0, read));
					}
					if (this.exitcode != -1) {
						// finished
						break;
					}
					// Sleep for the next round
					Thread.sleep(50);
				}
			} catch (InterruptedException ex) {
				if (res != null) res.append("\nOperation timed-out");
			} catch (Exception ex) {
				if (res != null) res.append("\n" + ex);
			} finally {
				destroy();
			}
		}
		
		public synchronized void destroy() {
			if (exec != null) exec.destroy();
			exec = null;
		}
	}
	
	//class used to store traffic used before phone on boot
	private static class tempAppinfo {
		int uid;
		double upload;
		double download;
    }

}
