package jp.kddilabs.tsm.android.smp.providers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import jp.kddilabs.tsm.android.StringTools;
import jp.kddilabs.tsm.android.Tools;
import jp.kddilabs.tsm.android.smp.AsmpServiceConnection;
import jp.kddilabs.tsm.android.smp.IntentGenerator;
import jp.kddilabs.tsm.android.smp.measures.CDMA_Measure;
import jp.kddilabs.tsm.android.smp.services.CDMA_Service;
import jp.kddilabs.tsm.android.smp.services.CDMA_ServiceInterface;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.NeighboringCellInfo;
import android.util.Log;

public class CDMA_CSV_Provider extends AsmpCSVProvider {

	public final static Uri CONTENT_URI = Uri
			.parse("content://jp.kddilabs.tsm.android.smp.providers.cdma/");

	private static final String DEBUG_TAG = "ASMP";

	private BufferedWriter out = null;
	private BufferedWriter neighbours_out = null;
	private BufferedReader in = null;
	private BufferedReader neighbours_in = null;

	private static final String CDMA_CSV_HEADER = "id,date,rssi,cid,lac,rnc,cdma_ecio,phone_type\n";
	private static final String CDMA_NEIGHBOURS_CSV_HEADER = "id,date,measure_id,cid,rssi\n";

	public final static String START_RECORDING = CDMA_CSV_Provider.class
			.getName()
			+ AsmpCSVProvider.START_RECORDING;
	public final static String STOP_RECORDING = CDMA_CSV_Provider.class
			.getName()
			+ AsmpCSVProvider.STOP_RECORDING;

	private CDMA_ServiceInterface cdma_binder;

	private int current_measure_id = 0;
	private int current_neighbour_id = 0;

	private BroadcastReceiver fresh_data = new BroadcastReceiver() {
		@Override
		public synchronized void onReceive(Context context, Intent intent) {
			if (!recording || cdma_binder == null)
				return;
			// add the fresh data to our CSV file
			try {
				StringBuilder neighbours_string = new StringBuilder();

				CDMA_Measure data = cdma_binder.getLastValue();

				Integer rssi, cid, lac, rnc, id, nid, ecio;
				Long time;
				List<NeighboringCellInfo> neighbours;

				rssi = data.rssi;
				cid = Tools.shortcid_packeddataextraction(data.cid);
				lac = data.lac;
				rnc = data.rnc;
				time = data.date;
				neighbours = data.neighbours;
				id = current_measure_id;
				nid = current_neighbour_id;
				ecio = data.cdma_ecio;

				// the cdma measure itself
				out.write(StringTools.join_footer(",", "\n", id.toString(),
						time.toString(), rssi.toString(), cid.toString(), lac
								.toString(), rnc.toString(), ecio.toString(),
						Tools.phonetype_to_string(data.phone_type))); // byte[]
				current_measure_id++;

				// now the neighbours
				for (NeighboringCellInfo info : neighbours) {
					String cell_string;
					cid = info.getCid();
					rssi = Tools.asu2dbm_unitconversion(info.getRssi());
					cell_string = StringTools.join_footer(",", "\n", nid
							.toString(), time.toString(), id.toString(), cid
							.toString(), rssi.toString());
					neighbours_string.append(cell_string);
					nid++;
				}
				neighbours_out.write(neighbours_string.toString());

				current_neighbour_id = nid;
			} catch (RemoteException e) {
				Log.d(DEBUG_TAG, "Couldn't get CDMA Service's last value");
				e.printStackTrace();
			} catch (IOException e) {
				Log.d(DEBUG_TAG, "Couldn't write data in the log CSV file");
				e.printStackTrace();
			}
		}
	};

	@Override
	public boolean onCreate() {
		super.init("CDMA");
		super.onCreate();

		current_measure_id = 0;
		current_neighbour_id = 0;

		if (!super.initFileManager("CDMA_")) {
			Log.d(DEBUG_TAG,
					"couldn't initialize CDMA_CSV_Provider file manager");
			return false;
		}

		Context mContext = getContext();
		ServiceConnection connection_to_cdma = new AsmpServiceConnection(
				mContext, fresh_data) {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(DEBUG_TAG, "CDMA Service connected");
				cdma_binder = CDMA_ServiceInterface.Stub.asInterface(service);
				// Register to receive cdma service
				// new_data_broadcast
				if (cdma_binder == null) {
					close_files();
					return;
				}
				getContext().registerReceiver(fresh_data,
						new IntentFilter(CDMA_Service.NEW_DATA));
			}
		};

		// Bind to the CDMA service
		mContext.bindService(IntentGenerator.readingIntent(mContext,
				jp.kddilabs.tsm.android.smp.services.CDMA_Service.class),
				connection_to_cdma, Service.BIND_AUTO_CREATE);

		return true;
	}

	@Override
	protected boolean open_files() {
		// Create the file where we'll put our data
		// for now the name of the file will be the date :p
		String name = new Long(new Date().getTime()).toString();
		out = getWriter(name);
		in = getReader(name);

		if (out == null || in == null) {
			Log.d(DEBUG_TAG, "could not create file");
			return false;
		}

		name = "neighbours_" + name;
		neighbours_out = getWriter(name);
		neighbours_in = getReader(name);

		if (neighbours_out == null || neighbours_in == null) {
			Log.d(DEBUG_TAG, "could not create file");
			return false;
		}

		try {
			out.write(CDMA_CSV_HEADER);
			neighbours_out.write(CDMA_NEIGHBOURS_CSV_HEADER);
			flush_files();
		} catch (IOException e) {
			Log.d(DEBUG_TAG, "CDMA_CSV_Provider Couldn't write in one file!");
			close_files();
			return false;
		}

		return true;
	}
}
