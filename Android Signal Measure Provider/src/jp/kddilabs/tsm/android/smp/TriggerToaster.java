package jp.kddilabs.tsm.android.smp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;

import jp.kddilabs.tsm.android.smp.measures.AsmpMeasure;
import android.os.IInterface;
import android.util.Log;

//shameful
//had to code that quickly and ignore commentaries...

abstract public class TriggerToaster {
	public final static String DEBUG_TAG = "ASMP";

	public class TriggerToasterToken {
		// can only be created inside TriggerToaster
		// you get some fine access control with this
		public boolean isValid() {
			return true;
		}
		// here is the trick, a private constructor
		private TriggerToasterToken() {
		}
	}

	private LinkedList<MainActivityDataReceiver<? extends AsmpMeasure, ? extends IInterface>> receivers;
	private HashMap<Class<? extends AsmpMeasure>, Method> overload_vtable;

	final private TriggerToasterToken tok = new TriggerToasterToken();

	public TriggerToaster(Class<? extends AsmpMeasure>... classes) {
		receivers = new LinkedList<MainActivityDataReceiver<? extends AsmpMeasure, ? extends IInterface>>();
		overload_vtable = new HashMap<Class<? extends AsmpMeasure>, Method>();
		for (Class<? extends AsmpMeasure> c : classes) {
			try {
				overload_vtable.put(c, this.getClass().getDeclaredMethod(
						"testData", c));
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

	protected <A extends AsmpMeasure, B extends IInterface> void addTriger(
			MainActivityDataReceiver<A, B> rec) {
		MainActivityDataReceiver<A, B>.TriggerControlToken trigtok = rec
				.getTriggerControl(tok);
		if (receivers.contains(rec))
			return;
		trigtok.addTrigger(this);
		receivers.add(rec);
	}

	private <A extends AsmpMeasure, B extends IInterface> void removeTrigger(
			MainActivityDataReceiver<A, B>.TriggerControlToken trigtok) {
		trigtok.removeTrigger(this);
	}

	protected <A extends AsmpMeasure, B extends IInterface> void removeTriger(
			MainActivityDataReceiver<A, B> rec) {
		MainActivityDataReceiver<A, B>.TriggerControlToken trigtok = rec
				.getTriggerControl(tok);
		removeTrigger(trigtok);
	}

	public void clear() {
		for (MainActivityDataReceiver<? extends AsmpMeasure, ? extends IInterface> receiver : receivers)
			removeTriger(receiver);
		receivers = new LinkedList<MainActivityDataReceiver<? extends AsmpMeasure, ? extends IInterface>>();
	}

	public <M extends AsmpMeasure> void testData(M data) {
		try {
			Method m = overload_vtable.get(data.getClass());
			if (m != null)
				m.invoke(this, data);
			else
				Log.e(DEBUG_TAG, this.getClass().toString()
						+ ".testdata called with a "
						+ data.getClass().toString()
						+ " parameter not found in overload_vtable",
						new Exception(
								"Would Have Brought a NullPointerException"));

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
}
