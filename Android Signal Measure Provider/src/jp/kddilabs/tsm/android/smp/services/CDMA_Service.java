package jp.kddilabs.tsm.android.smp.services;

import java.util.Timer;
import java.util.TimerTask;

import jp.kddilabs.tsm.android.Tools;
import jp.kddilabs.tsm.android.smp.measures.CDMA_Measure;
import jp.kddilabs.tsm.android.smp.providers.CDMA_CSV_Provider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class CDMA_Service extends AsmpService<CDMA_Measure> {

	public final static String NEW_DATA = CDMA_Service.class.getName()
			+ ".NEW_DATA";
	public final static String READ = CDMA_Service.class.getName() + ".READ";
	public final static String CONFIG = CDMA_Service.class.getName()
			+ ".CONFIG";

	private int phone_type;

	private final CDMA_ServiceInterface.Stub reader_binder = new CDMA_ServiceInterface.Stub() {
		@Override
		public final CDMA_Measure getLastValue() throws RemoteException {
			return last_measure;
		}
	};

	private final CDMA_ServiceConfigInterface.Stub config_binder = new CDMA_ServiceConfigInterface.Stub() {

		private boolean csv_recording = false;
		private int delay, period = -1;

		private final boolean isRecording() {
			return csv_recording;
		}

		public boolean isRecording(String format) throws RemoteException {
			if (format.equals(CDMA_CSV_Provider.RECORDING_FORMAT)) {
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
					if (isRecording()) {
						last_measure.neighbours = phone
								.getNeighboringCellInfo();
						sendRefreshNotice(NEW_DATA);
					}
				}
			};
			timer.schedule(timered_task, delay, period);

			return timered_task;
		}

		@Override
		public void setRefreshPeriod(int period) throws RemoteException {
			Log.d(DEBUG_TAG, "CDMA - setting refresh period");
			this.delay = period / 2;
			this.period = period;

			// if we actually cancel a scheduled execution, relaunch it
			if (timered_task != null && timered_task.cancel())
				timered_task = scheduleTimerTask(timer);

			Float p = new Float(period) / 1000;
			msg.toast("CDMA recording period set to " + p.toString()
					+ " seconds");
		}

		@Override
		public void startRecording(String format) {
			Log.d(DEBUG_TAG, "Trying to start the content recorder");
			if (format.equals(CDMA_CSV_Provider.RECORDING_FORMAT)) {
				ContentObserver observer = new ContentObserver(new Handler()) {
					@Override
					public void onChange(boolean moo) {
					}
				};

				// refresh phone type
				phone_type = phone.getPhoneType();
				last_measure.phone_type = phone_type;

				ContentResolver r = getContentResolver();
				r.registerContentObserver(CDMA_CSV_Provider.CONTENT_URI, false,
						observer);
				recorders.put(format, observer);

				sendBroadcast(new Intent()
						.setAction(CDMA_CSV_Provider.START_RECORDING));

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

			if (format.equals(CDMA_CSV_Provider.RECORDING_FORMAT)) {
				ContentResolver r = getContentResolver();
				ContentObserver v = recorders.get(format);
				if (v != null) {
					r.unregisterContentObserver(v);
					recorders.remove(format);
				}
				sendBroadcast(new Intent()
						.setAction(CDMA_CSV_Provider.STOP_RECORDING));
				csv_recording = false;
				// CDMA_CSV_Provider.stopRecording();
			}
		}
	};

	TelephonyManager phone;

	Timer timer = null;
	TimerTask timered_task = null;

	// private final Runnable DataBroadcaster;

	@Override
	public void onCreate() {
		super.init();
		last_measure = new CDMA_Measure();

		// get the telephony manager
		phone = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		phone_type = phone.getPhoneType();
		// phone_type = TelephonyManager.PHONE_TYPE_CDMA;
		last_measure.phone_type = phone_type;

		// Register the callbacks for Signal Strength and Cell Location
		if (phone_type == TelephonyManager.PHONE_TYPE_GSM) {
			Log.d(DEBUG_TAG, "phone type was GSM");
			phone.listen(new PhoneStateListener() {
				@Override
				public synchronized void onCellLocationChanged(
						CellLocation location) {
					GsmCellLocation l = (GsmCellLocation) location;
					last_measure.cid = l.getCid();
					last_measure.lac = l.getLac();
					sendRefreshNotice(NEW_DATA);
				}

				@Override
				public synchronized void onSignalStrengthsChanged(
						SignalStrength ss) {
					last_measure.rssi = Tools.asu2dbm_unitconversion(ss
							.getGsmSignalStrength());
					Log.w(DEBUG_TAG, "CDMAval " + ss.getCdmaDbm());
					if (!ss.isGsm()) {
						last_measure.phone_type = TelephonyManager.PHONE_TYPE_CDMA;
						Log.w(DEBUG_TAG, "IN GSM BUT NOT GSM");
					}
					sendRefreshNotice(NEW_DATA);
				}

			}, PhoneStateListener.LISTEN_CELL_LOCATION
					| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		} else if (phone_type == TelephonyManager.PHONE_TYPE_CDMA) {
			Log.d(DEBUG_TAG, "phone type was CDMA");
			phone.listen(new PhoneStateListener() {
				@Override
				public synchronized void onCellLocationChanged(
						CellLocation location) {

					CdmaCellLocation l = (CdmaCellLocation) location;
					last_measure.cid = l.getBaseStationId();
					last_measure.lac = l.getNetworkId();
					sendRefreshNotice(NEW_DATA);

				}

				@Override
				public synchronized void onSignalStrengthsChanged(
						SignalStrength ss) {
					last_measure.rssi = ss.getCdmaDbm();
					last_measure.cdma_ecio = ss.getCdmaEcio();
					sendRefreshNotice(NEW_DATA);
				}

			}, PhoneStateListener.LISTEN_CELL_LOCATION
					| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		} else {
			Log.d(DEBUG_TAG, "phone type unknown");
		}

		// Launch the task for NeighboringCellInfo
		timer = new Timer(true); // true -> deamon timer thread
	}

	@Override
	public void onDestroy() {
		if (timered_task != null)
			timered_task.cancel();
		timer.purge();
		timer.cancel();
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
}
