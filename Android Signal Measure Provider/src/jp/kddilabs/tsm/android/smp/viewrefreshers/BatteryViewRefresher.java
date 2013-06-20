package jp.kddilabs.tsm.android.smp.viewrefreshers;

import jp.kddilabs.tsm.android.smp.R;
import jp.kddilabs.tsm.android.smp.measures.Battery_Measure;
import android.app.Activity;
import android.widget.TextView;

public class BatteryViewRefresher extends AsmpViewRefresher<Battery_Measure> {

	public BatteryViewRefresher(Activity i) {
		super(i);
	}

	public TextView level;

	@Override
	public void init(final Activity i) {
		level = (TextView) i.findViewById(R.battery.level);
	}

	@Override
	public void refresh(final Battery_Measure obj) {
		Integer f = Math.round(obj.level * 100);
		level.setText(f.toString() + "%");
	}

}
