package jp.kddilabs.tsm.android;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Toaster {
	private final static String DEBUG_TAG = "ASMP";

	private Context context;

	public Toaster(Context c) {
		context = c;
	}

	public final void toast(final String s) {
		Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
		Log.i(DEBUG_TAG, s);
	}

	public final void toast_and_debug(String s) {
		Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
		Log.d(DEBUG_TAG, s);
	}

	public final void toast_and_warn(final String s, final Exception e) {
		Toast.makeText(context, s + "\n" + e.getMessage(), Toast.LENGTH_SHORT)
				.show();
		Log.w(DEBUG_TAG, s, e);
	}

	public final void toast_and_err(final String s, final Exception e) {
		Toast.makeText(context, s + "\n" + e.getMessage(), Toast.LENGTH_SHORT)
				.show();
		Log.e(DEBUG_TAG, s, e);
	}

}
