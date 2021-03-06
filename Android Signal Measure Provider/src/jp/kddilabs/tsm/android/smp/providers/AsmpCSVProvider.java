package jp.kddilabs.tsm.android.smp.providers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import jp.kddilabs.tsm.android.Toaster;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

abstract public class AsmpCSVProvider extends ContentProvider {

	private final static String DEBUG_TAG = "ASMP";
	public static final String RECORDING_FORMAT = "CSV";

	private static final String DIRNAME = "ASMP";

	private String FILENAME_PREFIX = "dummy_";
	private final String FILENAME_SUFFIX = ".csv";

	public final static String START_RECORDING = ".START_RECORDING";
	public final static String STOP_RECORDING = ".STOP_RECORDING";

	private HashMap<String, BufferedWriter> out_writers;
	private HashMap<String, BufferedReader> in_readers;

	private File basedir = null;

	protected int flush_period = 10000; // every 10s we will flush the buffers!

	// structure necessary to flush data every 10 sec :p
	private Timer flushing_timer;

	private Toast start_recording_toast;
	private Toast stop_recording_toast;

	protected boolean recording = false;

	protected Toaster msg;

	private BroadcastReceiver start_recording_receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			initFileManager(FILENAME_PREFIX);
			startRecording();
		}
	};

	private BroadcastReceiver stop_recording_receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			stopRecording();
			stopFileManager();
		}
	};

	protected void init(final String id) {
		setStartNotificationText("Started recording " + id + " data");
		setStopNotificationText("Stopped recording " + id + " data");
	}

	public boolean onCreate() {
		Context c = getContext();
		msg = new Toaster(c);

		if (start_recording_toast == null)
			start_recording_toast = Toast.makeText(c, this.getClass().getName()
					+ " started recording data", Toast.LENGTH_SHORT);
		if (stop_recording_toast == null)
			stop_recording_toast = Toast.makeText(c, this.getClass().getName()
					+ " stopped recording data", Toast.LENGTH_SHORT);

		String start_recording = null;
		String stop_recording = null;

		/*
		 * TECHNICAL PASSAGE THAT EXPLAINS WHY THE UGLY CODE ---- Java does not
		 * allow polymorphic behaviour with member variables so
		 * this.START_RECORDING is early (static) binded to this class's field.
		 * Even if we parametrize the class with the form AsmpCSVProvider<T
		 * extends AsmpCSVProvider<T>> and then implements subclasses as (for
		 * instance) class LOCATION_CSV_Provider extends
		 * AsmpCSVProvider<LOCATION_CSV_Provider> we won't be able to get the
		 * inherited field in the mother class. Java generics are implemented
		 * with type erasure, thus the type of T cannot be used for
		 * early-binding and the type of this (who we would have casted
		 * "(T) this") will be used. That means that we can't access subtype
		 * fields without using (late binded) methods... (btw static methods too
		 * are early binded) ... so if we want to access these fields without
		 * having to manually redefine getters/setters we have to resort to
		 * reflection...
		 * 
		 * you can understand the design behind this because what I'm doing make
		 * inherited classes technically automatically violate LSP (Liskov
		 * Substitution Principle, google it!). Though we would not if
		 * START_RECORDING was not final nor static and if we could freely
		 * modify it in inherited classes' constructors! But constructor things
		 * do not seem to play standard play with android's framework. ----
		 */

		try {
			start_recording = (String) this.getClass()
					.getField("START_RECORDING").get(this);
			stop_recording = (String) this.getClass()
					.getField("STOP_RECORDING").get(this);
		} catch (Exception e) {
			// it's probable you changed the START/STOP_RECORDING
			// fields name and didn't reflect it here
			// where we try to access them in a polymorphic way
			// (this means that if this function is called from
			// an inherited class object, we use this object field
			// and not AsmpCSVProvider's one)
			Log.e(DEBUG_TAG, "virtual constant START&STOP_RECORDING"
					+ " action callback registering -> ", e);
		}

		c.registerReceiver(start_recording_receiver, new IntentFilter(
				start_recording));
		c.registerReceiver(stop_recording_receiver, new IntentFilter(
				stop_recording));

		return true;
	}

	public void setStartNotificationText(String text) {
		start_recording_toast = Toast.makeText(getContext(), text,
				Toast.LENGTH_SHORT);
	}

	public void setStopNotificationText(String text) {
		stop_recording_toast = Toast.makeText(getContext(), text,
				Toast.LENGTH_SHORT);
	}

	public void startRecording() {
		if (!recording) {
			recording = true;
			start_recording_toast.show();
		}
	}

	public void stopRecording() {
		if (recording) {
			recording = false;
			stop_recording_toast.show();
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	public boolean initFileManager(final String prefix) {
		FILENAME_PREFIX = prefix;

		out_writers = new HashMap<String, BufferedWriter>();
		in_readers = new HashMap<String, BufferedReader>();

		// launch the periodic flushing task
		flushing_timer = new Timer();
		if (flushing_timer == null) {
			Log.w(DEBUG_TAG, this.getClass().getName()
					+ "could not create flushing thread");
		} else {
			flushing_timer.schedule(new TimerTask() {
				@Override
				public void run() {
					flush_files();
				}
			}, flush_period / 2, flush_period);
		}
		return makeBasedir() && open_files();
	}

	private void stopFileManager() {
		flushing_timer.cancel();
		close_files();
	}

	protected BufferedWriter getWriter(final String name) {
		if (out_writers.containsKey(name)) {
			BufferedWriter res = out_writers.get(name);
			if (res == null) {
				out_writers.remove(name);
				return getWriter(name);
			}
			return res;
		} else {
			StringBuilder full_filename = new StringBuilder();
			FileWriter fw = null;
			BufferedWriter bw;

			full_filename.append(FILENAME_PREFIX);
			full_filename.append(name);
			full_filename.append(FILENAME_SUFFIX);

			File f = new File(basedir, full_filename.toString());
			try {
				f.createNewFile();
				if (!f.canWrite()) {
					msg.toast_and_debug("cannot write " + f.getAbsolutePath());
					return null;
				}
			} catch (IOException e) {
				Log.e(DEBUG_TAG,
						"IO exception while creating " + f.getAbsolutePath(), e);
			}
			try {
				fw = new FileWriter(f);

			} catch (IOException e) {
				Log.e(DEBUG_TAG, "IO exception while getting FileWriter on "
						+ f.getAbsolutePath(), e);
			}

			bw = new BufferedWriter(fw);
			out_writers.put(name, bw);
			return bw;
		}
	}

	protected BufferedReader getReader(final String name) {
		if (in_readers.containsKey(name)) {
			BufferedReader res = in_readers.get(name);
			if (res == null) {
				in_readers.remove(name);
				return getReader(name);
			}
			return res;
		} else {
			StringBuilder full_filename = new StringBuilder();
			FileReader fr = null;
			BufferedReader br;

			full_filename.append(FILENAME_PREFIX);
			full_filename.append(name);
			full_filename.append(FILENAME_SUFFIX);

			File f = new File(basedir, full_filename.toString());
			try {
				f.createNewFile();
				if (!f.canRead()) {
					msg.toast_and_debug("cannot read " + f.getAbsolutePath());
					return null;
				}
			} catch (IOException e) {
				Log.e(DEBUG_TAG,
						"IO exception while creating " + f.getAbsolutePath(), e);
			}
			try {
				fr = new FileReader(f);

			} catch (IOException e) {
				Log.e(DEBUG_TAG, "IO exception while getting FileReader on "
						+ f.getAbsolutePath(), e);
			}
			br = new BufferedReader(fr);
			in_readers.put(name, br);
			return br;
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		return new DummyCursor();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

	private boolean makeBasedir() {
		basedir = Environment.getExternalStorageDirectory();
		if (!basedir.canWrite()) {
			msg.toast_and_debug("Cannot write on the SD Card!");
			basedir = null;
			return false;
		}

		basedir = new File(basedir, DIRNAME);
		basedir.mkdir();
		return basedir.canWrite();
	}

	private <M extends Flushable> void _flush_files(Collection<M> values) {
		for (M flushable : values)
			if (flushable != null)
				try {
					flushable.flush();
				} catch (IOException e) {
					msg.toast_and_err(
							"couldn't flush file " + flushable.toString(), e);
				}

	}

	private <M extends Closeable> void _close_files(Collection<M> values) {
		for (M closable : values)
			if (closable != null)
				try {
					closable.close();
				} catch (IOException e) {
					msg.toast_and_err(
							"couldn't close file " + closable.toString(), e);
				}

	}

	protected void flush_files() {
		if (out_writers != null)
			_flush_files(out_writers.values());
	}

	protected void close_files() {
		if (out_writers != null) {
			Collection<BufferedWriter> outs = out_writers.values();
			_flush_files(outs);
			_close_files(outs);
		}

		if (in_readers != null) {
			Collection<BufferedReader> ins = in_readers.values();
			_close_files(ins);
		}

		out_writers = new HashMap<String, BufferedWriter>();
		in_readers = new HashMap<String, BufferedReader>();
	}

	abstract protected boolean open_files();

	/*
	 * private boolean reset_files() { close_files(); return open_files(); }
	 * 
	 * protected boolean read_message_exec_order(AsmpMeasure obj) { switch
	 * (obj.msg_to_provider) { case (MessageToProvider.NONE): return true; case
	 * (MessageToProvider.START_NEW_FILES): return reset_files(); default:
	 * return true; } }
	 */
}
