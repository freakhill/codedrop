package jp.kddilabs.tsm.android.smp.services;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import jp.kddilabs.tsm.android.smp.measures.WiFi_Measure;
import jp.kddilabs.tsm.android.smp.providers.WiFi_CSV_Provider;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class WiFi_Service extends AsmpService<WiFi_Measure> {

	public final static String NEW_DATA = WiFi_Service.class.getName()
			+ ".NEW_DATA";
	public final static String READ = WiFi_Service.class.getName() + ".READ";
	public final static String CONFIG = WiFi_Service.class.getName()
			+ ".CONFIG";

	private final WiFi_ServiceInterface.Stub reader_binder = new WiFi_ServiceInterface.Stub() {
		@Override
		public final WiFi_Measure getLastValue() throws RemoteException {
			return last_measure;
		}
	};

	private final WiFi_ServiceConfigInterface.Stub config_binder = new WiFi_ServiceConfigInterface.Stub() {

		private boolean csv_recording = false;
		private int delay, period = -1;

		private final boolean isRecording() {
			return csv_recording;
		}

		public boolean isRecording(String format) throws RemoteException {
			if (format.equals(WiFi_CSV_Provider.RECORDING_FORMAT)) {
				return csv_recording;
			}
			return false;
		}

		private TimerTask scheduleTimerTask(Timer timer) {
			TimerTask timered_task;

			timer.purge();
			timered_task = new TimerTask() {
				// get the neighbours cell info
				// and send!
				@Override
				public synchronized void run() {
					// with the following expression, we scan only if
					// somebody is up for recording data!
					if (isRecording() && wifi.startScan())
						last_measure.scan_call_date = new Date().getTime();
					// and then now when the scan finish, a
					// WifiManager.SCAN_RESULTS_AVAILABLE_ACTION will be
					// broadcast, and we will catch it in our receiver a bit
					// later and send the data to the MainActivity!
				}
			};
			timer.schedule(timered_task, delay, period);

			return timered_task;
		}

		@Override
		public void setRefreshPeriod(int period) throws RemoteException {
			Log.d(DEBUG_TAG, "WiFi - setting refresh period");
			this.delay = period / 2;
			this.period = period;

			// if we actually cancel a scheduled execution, relaunch it
			if (timered_task != null && timered_task.cancel())
				timered_task = scheduleTimerTask(timer);

			Float p = new Float(period) / 1000;
			msg.toast("WiFi recording period set to " + p.toString()
					+ " seconds");
		}

		@Override
		public void startRecording(String format) {
			Log.d(DEBUG_TAG, "Trying to start the content recorder");

			if (format.equals(WiFi_CSV_Provider.RECORDING_FORMAT)) {
				/* The following lines ensure a provider is there for us */
				ContentObserver observer = new ContentObserver(new Handler()) {
					@Override
					public void onChange(boolean moo) {
					}
				};

				ContentResolver r = getContentResolver();
				r.registerContentObserver(WiFi_CSV_Provider.CONTENT_URI, false,
						observer);
				recorders.put(format, observer);

				sendBroadcast(new Intent()
						.setAction(WiFi_CSV_Provider.START_RECORDING));
				csv_recording = true;
			}

			if (timered_task != null)
				timered_task.cancel();
			timered_task = scheduleTimerTask(timer);
		}

		@Override
		public void stopRecording(String format) {
			Log.d(DEBUG_TAG, "Trying to stop the content recorder");

			if (timered_task != null)
				timered_task.cancel();

			if (format.equals(WiFi_CSV_Provider.RECORDING_FORMAT)) {
				// by removing our content provider we express that we do not
				// need anymore its presence for our app
				ContentResolver r = getContentResolver();
				ContentObserver v = recorders.get(format);
				if (v != null) {
					r.unregisterContentObserver(v);
					recorders.remove(format);
				}

				sendBroadcast(new Intent()
						.setAction(WiFi_CSV_Provider.STOP_RECORDING));
				csv_recording = false;
			}
		}
	};

	WifiManager wifi = null;

	Timer timer = null;
	TimerTask timered_task = null;

	@Override
	public void onCreate() {
		Log.d(DEBUG_TAG, "onCreate");

		super.init();

		last_measure = new WiFi_Measure();

		// get the telephony manager
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		// Register for updates on scan results
		IntentFilter scanFilter = new IntentFilter();
		scanFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// a new scan is up o/
				last_measure.scan_end_event_date = new Date().getTime();
				last_measure.scans = wifi.getScanResults();
				sendRefreshNotice(NEW_DATA);
			}
		}, scanFilter);

		// Launch the task for NeighboringCellInfo
		timer = new Timer(true); // true -> deamon timer thread
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
		return null;
	}

	@Override
	public void onDestroy() {
		if (timered_task != null)
			timered_task.cancel();
		timer.purge();
		timer.cancel();
	}
}
