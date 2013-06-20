package jp.kddilabs.tsm.android.smp.viewrefreshers;

import java.util.List;

import jp.kddilabs.tsm.android.smp.R;
import jp.kddilabs.tsm.android.smp.measures.WiFi_Measure;
import android.app.Activity;
import android.net.wifi.ScanResult;
import android.widget.TextView;

public class WiFiViewRefresher extends AsmpViewRefresher<WiFi_Measure> {
	public WiFiViewRefresher(Activity i) {
		super(i);
	}

	public TextView dur, number, ssids, caps, levels;

	public void init(final Activity i) {
		dur = (TextView) i.findViewById(R.wifi.dur);
		number = (TextView) i.findViewById(R.wifi.number);
		ssids = (TextView) i.findViewById(R.wifi.ssids);
		caps = (TextView) i.findViewById(R.wifi.caps);
		levels = (TextView) i.findViewById(R.wifi.levels);
	}

	@Override
	public void refresh(final WiFi_Measure obj) {
		Long _dur;
		List<ScanResult> scans;
		StringBuilder _ssids, _caps, _levels, _number;
		WiFi_Measure data = (WiFi_Measure) obj;

		_dur = data.scan_end_event_date - data.scan_call_date;
		scans = data.scans;

		// duration
		dur.setText(_dur.toString() + "ms");

		// number
		_number = new StringBuilder();
		_number.append("(");
		_number.append(scans.size());
		_number.append(" found)");
		number.setText(_number.toString());

		// ssids caps and levels!
		_ssids = new StringBuilder();
		_caps = new StringBuilder();
		_levels = new StringBuilder();
		for (ScanResult scan : scans) {
			_ssids.append(scan.SSID).append('\n');
			// _caps.append(scan.capabilities).append('\n');
			_levels.append(scan.level).append('\n');
		}
		ssids.setText(_ssids.toString());
		caps.setText(_caps.toString());
		levels.setText(_levels.toString());
	}
}
