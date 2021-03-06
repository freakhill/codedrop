package jp.kddilabs.tsm.android.smp.providers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;

import jp.kddilabs.tsm.android.StringTools;
import jp.kddilabs.tsm.android.smp.AsmpServiceConnection;
import jp.kddilabs.tsm.android.smp.IntentGenerator;
import jp.kddilabs.tsm.android.smp.measures.Location_Measure;
import jp.kddilabs.tsm.android.smp.services.Location_Service;
import jp.kddilabs.tsm.android.smp.services.Location_ServiceInterface;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class Location_CSV_Provider extends AsmpCSVProvider {

	private static final String DEBUG_TAG = "ASMP";

	public final static Uri CONTENT_URI = Uri
			.parse("content://jp.kddilabs.tsm.android.smp.providers.location/");

	private BufferedWriter out = null;
	private BufferedReader in = null;

	private static final String LOCATION_CSV_HEADER = "id,date,idate,latitude,longitude,altitude,accuracy,bearing,speed,provider\n";

	public final static String START_RECORDING = Location_CSV_Provider.class
			.getName() + AsmpCSVProvider.START_RECORDING;
	public final static String STOP_RECORDING = Location_CSV_Provider.class
			.getName() + AsmpCSVProvider.STOP_RECORDING;

	private Location_ServiceInterface location_binder;

	private int current_id = 0;

	private BroadcastReceiver fresh_data = new BroadcastReceiver() {
		@Override
		public synchronized void onReceive(Context context, Intent intent) {
			if (!recording || location_binder == null)
				return;
			// add the fresh data to our CSV file
			try {
				String scan_string;

				Location_Measure data = location_binder.getLastValue();

				Integer id;
				Long date, idate;
				Double lat, lon, alt;
				Float acc, bea, spe;
				String provider;
				Location loc;

				loc = data.location;

				date = data.date;
				idate = loc.getTime();
				lat = loc.getLatitude();
				lon = loc.getLongitude();
				alt = loc.getAltitude();
				acc = loc.getAccuracy();
				bea = loc.getBearing();
				spe = loc.getSpeed();
				provider = loc.getProvider();

				id = current_id;

				scan_string = StringTools.join_footer(",", "\n", id.toString(),
						date.toString(), idate.toString(), lat.toString(),
						lon.toString(), alt.toString(), acc.toString(),
						bea.toString(), spe.toString(), provider);

				out.write(scan_string);

				current_id++;
			} catch (RemoteException e) {
				Log.d(DEBUG_TAG, "Couldn't get Location Service's last value");
				e.printStackTrace();
			} catch (IOException e) {
				Log.d(DEBUG_TAG, "Couldn't write data in the log CSV file");
				e.printStackTrace();
			}
		}
	};

	@Override
	public boolean onCreate() {
		super.init("Location");
		super.onCreate();

		current_id = 0;

		if (!super.initFileManager("Location_")) {
			Log.d(DEBUG_TAG,
					"couldn't initialize Location_CSV_Provider file manager");
			return false;
		}

		Context mContext = getContext();
		ServiceConnection connection_to_location = new AsmpServiceConnection(
				mContext, fresh_data) {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(DEBUG_TAG, "CDMA Service connected");
				location_binder = Location_ServiceInterface.Stub
						.asInterface(service);
				// Register to receive cdma service
				// new_data_broadcast
				if (location_binder == null) {
					close_files();
					return;
				}
				getContext().registerReceiver(fresh_data,
						new IntentFilter(Location_Service.NEW_DATA));
			}
		};

		// Bind to the CDMA service
		mContext.bindService(IntentGenerator.readingIntent(mContext,
				jp.kddilabs.tsm.android.smp.services.Location_Service.class),
				connection_to_location, Service.BIND_AUTO_CREATE);

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
			out.write(LOCATION_CSV_HEADER);
			flush_files();
		} catch (IOException e) {
			Log.d(DEBUG_TAG,
					"Location_CSV_Provider Couldn't write in one file!");
			close_files();
			return false;
		}

		return true;
	}
}
