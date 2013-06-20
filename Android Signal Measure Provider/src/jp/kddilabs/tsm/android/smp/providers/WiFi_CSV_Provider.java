package jp.kddilabs.tsm.android.smp.providers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import jp.kddilabs.tsm.android.StringTools;
import jp.kddilabs.tsm.android.smp.AsmpServiceConnection;
import jp.kddilabs.tsm.android.smp.IntentGenerator;
import jp.kddilabs.tsm.android.smp.measures.WiFi_Measure;
import jp.kddilabs.tsm.android.smp.services.WiFi_Service;
import jp.kddilabs.tsm.android.smp.services.WiFi_ServiceInterface;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class WiFi_CSV_Provider extends AsmpCSVProvider {

	private static final String DEBUG_TAG = "ASMP";

	public final static Uri CONTENT_URI = Uri
			.parse("content://jp.kddilabs.tsm.android.smp.providers.wifi/");

	private BufferedWriter out = null;
	private BufferedReader in = null;

	private static final String WIFI_CSV_HEADER = "id,date,pre_scan_date,post_scan_date,SSID,freq,level,capabilities,BSSID\n";

	public final static String START_RECORDING = WiFi_CSV_Provider.class
			.getName()
			+ AsmpCSVProvider.START_RECORDING;
	public final static String STOP_RECORDING = WiFi_CSV_Provider.class
			.getName()
			+ AsmpCSVProvider.STOP_RECORDING;

	private WiFi_ServiceInterface wifi_binder;

	private int current_id = 0;

	private BroadcastReceiver fresh_data = new BroadcastReceiver() {
		@Override
		public synchronized void onReceive(Context context, Intent intent) {
			if (!recording || wifi_binder == null)
				return;
			// add the fresh data to our CSV file
			try {
				StringBuilder scans_string = new StringBuilder();
				String scan_string;

				WiFi_Measure data = wifi_binder.getLastValue();

				Integer id, freq, level;
				Long date, predate, postdate;
				List<ScanResult> scans;

				date = data.date;
				predate = data.scan_call_date;
				postdate = data.scan_end_event_date;
				scans = data.scans;
				id = current_id;

				// scans
				for (ScanResult scan : scans) {
					freq = scan.frequency;
					level = scan.level;

					scan_string = StringTools.join_footer(",", "\n", id
							.toString(), date.toString(), predate.toString(),
							postdate.toString(), scan.SSID, freq.toString(),
							level.toString(), scan.capabilities, scan.BSSID);

					scans_string.append(scan_string);
					id++;
				}

				out.write(scans_string.toString());

				current_id = id;
			} catch (RemoteException e) {
				Log.d(DEBUG_TAG, "Couldn't get WiFi Service's last value");
				e.printStackTrace();
			} catch (IOException e) {
				Log.d(DEBUG_TAG, "Couldn't write data in the log CSV file");
				e.printStackTrace();
			}
		}
	};

	@Override
	public boolean onCreate() {
		super.init("WiFi");
		super.onCreate();

		current_id = 0;

		if (!super.initFileManager("WiFi_")) {
			Log.d(DEBUG_TAG,
					"couldn't initialize WiFi_CSV_Provider file manager");
			return false;
		}

		Context mContext = getContext();
		ServiceConnection connection_to_wifi = new AsmpServiceConnection(
				mContext, fresh_data) {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(DEBUG_TAG, "CDMA Service connected");
				wifi_binder = WiFi_ServiceInterface.Stub.asInterface(service);
				// Register to receive cdma service
				// new_data_broadcast
				if (wifi_binder == null) {
					close_files();
					return;
				}
				getContext().registerReceiver(fresh_data,
						new IntentFilter(WiFi_Service.NEW_DATA));
			}
		};

		// Bind to the CDMA service
		mContext.bindService(IntentGenerator.readingIntent(mContext,
				jp.kddilabs.tsm.android.smp.services.WiFi_Service.class),
				connection_to_wifi, Service.BIND_AUTO_CREATE);

		return true;
	}

	@Override
	protected boolean open_files() {
		// Create the file where we'll put our data
		// for now the name of the file will be the date :p
		String name = new Long(new Date().getTime()).toString();

		out = getWriter(name);
		in = getReader(name);

		if (out == null || in == null)
			return false;

		try {
			out.write(WIFI_CSV_HEADER);
			flush_files();
		} catch (IOException e) {
			Log.d(DEBUG_TAG, "WiFi_CSV_Provider Couldn't write in one file!");
			close_files();
			return false;
		}
		return true;
	}
}
