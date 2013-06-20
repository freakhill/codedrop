package jp.kddilabs.tsm.android.smp;

import java.lang.reflect.Method;
import java.util.LinkedList;

import jp.kddilabs.tsm.android.smp.TriggerToaster.TriggerToasterToken;
import jp.kddilabs.tsm.android.smp.measures.AsmpMeasure;
import jp.kddilabs.tsm.android.smp.viewrefreshers.AsmpViewRefresher;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IInterface;
import android.util.Log;

//shameful
//lack commentaries on the TriggerToaster part because of the lack of time

public class MainActivityDataReceiver<M extends AsmpMeasure, B extends IInterface>
		extends BroadcastReceiver {

	private final static String DEBUG_TAG = "ASMP";

	private B binder;
	private AsmpViewRefresher<M> view;

	private Method getLastValue;

	private LinkedList<TriggerToaster> triggers;

	public MainActivityDataReceiver(B binder, AsmpViewRefresher<M> view) {
		super();
		this.binder = binder;
		this.view = view;
		triggers = new LinkedList<TriggerToaster>();
		// horrible horrible hack to get getLastValue with
		// generated stubs
		try {
			getLastValue = binder.getClass().getMethod("getLastValue");
		} catch (Exception e) {
			Log.e(DEBUG_TAG, "Reflective discovery of getLastValue failed!", e);
		}
	}

	// With this construction, we ensure that only TriggerToasters can
	// add triggers
	public class TriggerControlToken implements TriggerControl {
		public void addTrigger(TriggerToaster t) {
			triggers.add(t);
		}

		public void removeTrigger(TriggerToaster t) {
			triggers.remove(t);
		}

		public TriggerControlToken(TriggerToasterToken t) {
			/*
			 * will throw a null pointer exception if you try to pass a null
			 * value, so you won't get your access!
			 */
			t.isValid();
		}
	}

	public TriggerControlToken getTriggerControl(TriggerToasterToken t) {
		return new TriggerControlToken(t);
	}

	public MainActivityDataReceiver(B binder, AsmpViewRefresher<M> view,
			boolean sticky) {
		super();
		this.binder = binder;
		this.view = view;
		triggers = new LinkedList<TriggerToaster>();
		// horrible horrible hack to get getLastValue with
		// generated stubs
		try {
			getLastValue = binder.getClass().getMethod("getLastValue");
			if (sticky) {
				Log.d(DEBUG_TAG, "Sticky first refresh for binder "
						+ binder.toString());
				onReceive(null, null);
			}
		} catch (Exception e) {
			Log.e(DEBUG_TAG, "Reflective discovery of getLastValue failed!", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Context context, Intent intent) {
		M data;
		try {
			// use the binder to get the data
			data = (M) getLastValue.invoke(binder);
			// modify the ui
			view.refresh(data);
			for (TriggerToaster t : triggers) {
				t.testData(data);
			}
		} catch (Exception e) {
			Log.e(DEBUG_TAG, "Remote call of getLastValue failed!", e);
		}

	}
}
