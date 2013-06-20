package jp.kddilabs.tsm.android.smp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.util.Log;

abstract public class AsmpServiceConnection implements ServiceConnection {

	private final static String DEBUG_TAG = "ASMP";

	BroadcastReceiver receiver;
	Context context;

	public AsmpServiceConnection(Context c, BroadcastReceiver r) {
		super();
		receiver = r;
		context = c;
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.d(DEBUG_TAG, name.flattenToString() + " service disconnection");
		if (receiver != null)
			context.unregisterReceiver(receiver);
	}

}
