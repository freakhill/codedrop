package jp.kddilabs.tsm.android.smp.services;

import jp.kddilabs.tsm.android.smp.measures.Location_Measure;
import jp.kddilabs.tsm.android.smp.providers.Location_CSV_Provider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class Location_Service extends AsmpService<Location_Measure> {

	public final static String NEW_DATA = Location_Service.class.getName()
			+ ".NEW_DATA";
	public final static String READ = Location_Service.class.getName()
			+ ".READ";
	public final static String CONFIG = Location_Service.class.getName()
			+ ".CONFIG";

	public final static String DEFAULT_PROVIDER = LocationManager.GPS_PROVIDER;
	public final static int DEFAULT_PERIOD = 10000; // 10sec

	private LocationListener location_listener;

	private final Location_ServiceInterface.Stub reader_binder = new Location_ServiceInterface.Stub() {
		@Override
		public final Location_Measure getLastValue() throws RemoteException {
			return last_measure;
		}
	};

	private final Location_ServiceConfigInterface.Stub config_binder = new Location_ServiceConfigInterface.Stub() {

		private boolean csv_recording = false;
		private String provider = DEFAULT_PROVIDER;
		private int period = DEFAULT_PERIOD;

		private final boolean isRecording() {
			return csv_recording;
		}

		public final boolean isRecording(String format) throws RemoteException {
			if (format.equals(Location_CSV_Provider.RECORDING_FORMAT)) {
				return csv_recording;
			}
			return false;
			// could be rewritten in a big return X && Y but the compiler
			// will do it for us
		}

		@Override
		public void setProvider(String provider) throws RemoteException {
			LocationManager loc_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			loc_manager.removeUpdates(location_listener);
			this.provider = provider;

			if (isRecording())
				loc_manager.requestLocationUpdates(provider, period, 0,
						location_listener);

			msg.toast("Location provider set to " + provider);
		}

		@Override
		public void setRefreshPeriod(int period) throws RemoteException {
			LocationManager loc_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			loc_manager.removeUpdates(location_listener);
			this.period = period;

			if (isRecording())
				loc_manager.requestLocationUpdates(provider, period, 0,
						location_listener);

			Float p = new Float(period) / 1000;
			msg.toast("Location recording period set to " + p.toString()
					+ " seconds");
		}

		@Override
		public void startRecording(String format) {
			Log.d(DEBUG_TAG, "Trying to start the content recorder");

			if (format.equals(Location_CSV_Provider.RECORDING_FORMAT)) {
				ContentObserver observer = new ContentObserver(new Handler()) {
					@Override
					public void onChange(boolean moo) {
					}
				};

				ContentResolver r = getContentResolver();
				r.registerContentObserver(Location_CSV_Provider.CONTENT_URI,
						false, observer);
				recorders.put(format, observer);
				sendBroadcast(new Intent()
						.setAction(Location_CSV_Provider.START_RECORDING));
				csv_recording = true;
			}

			LocationManager loc_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			loc_manager.requestLocationUpdates(provider, period, 0,
					location_listener);
		}

		@Override
		public void stopRecording(String format) {
			Log.d(DEBUG_TAG, "Trying to stop the content recorder");

			LocationManager loc_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			loc_manager.removeUpdates(location_listener);

			if (format.equals(Location_CSV_Provider.RECORDING_FORMAT)) {
				ContentResolver r = getContentResolver();
				ContentObserver v = recorders.get(format);
				if (v != null) {
					r.unregisterContentObserver(v);
					recorders.remove(format);
				}
				sendBroadcast(new Intent()
						.setAction(Location_CSV_Provider.STOP_RECORDING));
				csv_recording = false;
			}
		}
	};

	@Override
	public void onCreate() {
		super.init();
		last_measure = new Location_Measure();

		location_listener = new LocationListener() {

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				String append;
				switch (status) {
				case LocationProvider.OUT_OF_SERVICE:
					append = "OUT OF SERVICE";
					break;
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
					append = "TEMPORARILY UNAVAILABLE";
					break;
				case LocationProvider.AVAILABLE:
					append = "AVAILABLE";
					break;
				default:
					append = "UNKNOWN";
				}
				Log.i(DEBUG_TAG, "Location provider status - " + append);
			}

			@Override
			public void onProviderEnabled(String provider) {
				Log.w(DEBUG_TAG, "Location provider - " + provider
						+ " - enabled");
				msg.toast("Location provider - " + provider + " - enabled");
			}

			@Override
			public void onProviderDisabled(String provider) {
				Log.w(DEBUG_TAG, "Location provider - " + provider
						+ " - disabled");
				msg.toast("Location provider - " + provider + " - disabled");
				last_measure.date = 0;
			}

			@Override
			public synchronized void onLocationChanged(Location location) {
				last_measure.location = location;
				sendRefreshNotice(NEW_DATA);
			}
		};
	}

	@Override
	public final IBinder onBind(Intent intent) {
		String action = intent.getAction();
		if (action.equals(READ)) {
			return reader_binder;
		}
		if (action.equals(CONFIG)) {
			return config_binder;
		}
		Log.w(DEBUG_TAG, "Strange intent sent to Location_Service : " + action);
		return null;
	}
}
