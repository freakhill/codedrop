package jp.kddilabs.tsm.android.smp.services;

import java.util.Date;
import java.util.HashMap;

import jp.kddilabs.tsm.android.Toaster;
import jp.kddilabs.tsm.android.smp.measures.AsmpMeasure;
import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.util.Log;

abstract public class AsmpService<M extends AsmpMeasure> extends Service {
	protected static final String DEBUG_TAG = "ASMP";
	protected M last_measure;

	protected Toaster msg;

	private String classname = null;

	protected HashMap<String, ContentObserver> recorders = null;

	protected void init() {
		// creating the structure that will hold the content providers
		// that will record our data!
		recorders = new HashMap<String, ContentObserver>();
		classname = this.getClass().getName();

		msg = new Toaster(getApplicationContext());
	}

	@Override
	public final void onStart(Intent intent, int start_id) {
		Log.d(DEBUG_TAG, classname + " - onStart");
	}

	protected synchronized void sendRefreshNotice(final String Action) {
		// approximative date when the measure was taken
		// don't forget this is a multitasking machine with
		// 100ms GC's running on...
		last_measure.date = new Date().getTime();
		Log.d(DEBUG_TAG, classname + " - " + Action);
		// write the collected data into the last_measure obj
		sendBroadcast(new Intent().setAction(Action));
	}
}
