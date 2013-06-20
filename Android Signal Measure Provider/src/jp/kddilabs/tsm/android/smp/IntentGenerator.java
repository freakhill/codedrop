package jp.kddilabs.tsm.android.smp;

import java.lang.reflect.Field;

import jp.kddilabs.tsm.android.smp.measures.AsmpMeasure;
import jp.kddilabs.tsm.android.smp.services.AsmpService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class IntentGenerator {
	private final static String DEBUG_TAG = "ASMP";

	private static final Intent xIntent(final Context con,
			Class<? extends AsmpService<? extends AsmpMeasure>> cls,
			final String x) {
		try {
			Field x_field = cls.getDeclaredField(x);
			String action_string = (String) x_field.get(cls);
			return new Intent().setAction(action_string).setClass(con, cls);
		} catch (Exception e) {
			Log.d(DEBUG_TAG, "Reflective discover of the field " + x
					+ " for class " + cls.getName() + " failed", e);
		}
		return null;
	}

	public static final Intent readingIntent(final Context con,
			final Class<? extends AsmpService<? extends AsmpMeasure>> c) {
		return xIntent(con, c, "READ");
	}

	public static final Intent configIntent(final Context con,
			final Class<? extends AsmpService<? extends AsmpMeasure>> c) {
		return xIntent(con, c, "CONFIG");
	}
}
