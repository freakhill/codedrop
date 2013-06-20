package jp.kddilabs.tsm.android.smp.viewrefreshers;

import jp.kddilabs.tsm.android.smp.R;
import jp.kddilabs.tsm.android.smp.measures.Location_Measure;
import android.app.Activity;
import android.location.Location;
import android.widget.TextView;

public class LocationViewRefresher extends AsmpViewRefresher<Location_Measure> {

	public LocationViewRefresher(Activity i) {
		super(i);
	}

	public TextView lat, lon, acc, alt, bea, spe, pro;

	private boolean capabilities_set = false;
	private boolean has_lat, has_lon, has_acc, has_alt, has_bea, has_spe;

	@Override
	public void init(final Activity i) {
		lat = (TextView) i.findViewById(R.location.lat);
		lon = (TextView) i.findViewById(R.location.lon);
		acc = (TextView) i.findViewById(R.location.acc);
		alt = (TextView) i.findViewById(R.location.alt);
		bea = (TextView) i.findViewById(R.location.bea);
		spe = (TextView) i.findViewById(R.location.spe);
		pro = (TextView) i.findViewById(R.location.pro);
	}

	@Override
	public void refresh(final Location_Measure obj) {
		Location_Measure data = (Location_Measure) obj;
		Double _lat, _lon, _alt;
		Float _acc, _bea, _spe;

		if (data == null) {
			spamNoData();
			capabilities_set = false;
			return;
		}
		Location l = data.location;

		if (capabilities_set) {
			if (has_lat) {
				_lat = l.getLatitude();
				lat.setText(_lat.toString());
			}
			if (has_lon) {
				_lon = l.getLongitude();
				lon.setText(_lon.toString());
			}
			if (has_acc) {
				_acc = l.getAccuracy();
				acc.setText(_acc.toString() + "m");
			}
			if (has_alt) {
				_alt = l.getAltitude();
				alt.setText(_alt.toString() + "?");
			}
			if (has_bea) {
				_bea = l.getBearing();
				bea.setText(_bea.toString() + "deg");
			}
			if (has_spe) {
				_spe = l.getSpeed();
				spe.setText(_spe.toString() + "m/s");
			}
			
			pro.setText(l.getProvider());

		} else {
			has_lat = true;
			has_lon = true;
			has_acc = l.hasAccuracy();
			has_alt = l.hasAltitude();
			has_bea = l.hasBearing();
			has_spe = l.hasSpeed();
			capabilities_set = true;
			spamNoData();
			refresh(data);
		}
	}

	private void spamNoData() {
		lat.setText("NO DATA");
		lon.setText("NO DATA");
		acc.setText("NO DATA");
		alt.setText("NO DATA");
		bea.setText("NO DATA");
		spe.setText("NO DATA");
		pro.setText("NO DATA");
	}

}
