package jp.kddilabs.tsm.android.smp.viewrefreshers;

import jp.kddilabs.tsm.android.smp.measures.AsmpMeasure;
import android.app.Activity;

public abstract class ViewRefresher<M extends AsmpMeasure> {
	public ViewRefresher(final Activity i) {
		init(i);
	}

	public abstract void init(final Activity i);

	public abstract void refresh(final M obj);
}
