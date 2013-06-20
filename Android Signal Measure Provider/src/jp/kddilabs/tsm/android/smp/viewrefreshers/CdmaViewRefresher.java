package jp.kddilabs.tsm.android.smp.viewrefreshers;

import java.util.List;

import jp.kddilabs.tsm.android.Tools;
import jp.kddilabs.tsm.android.smp.R;
import jp.kddilabs.tsm.android.smp.measures.CDMA_Measure;
import android.app.Activity;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.widget.TextView;

public class CdmaViewRefresher extends AsmpViewRefresher<CDMA_Measure> {
	public CdmaViewRefresher(Activity i) {
		super(i);
	}

	public TextView phone_type, cid, lac, rnc, rssi, cdma_ecio, cids, rxls,
			neigh_len;

	@Override
	public void init(final Activity i) {
		phone_type = (TextView) i.findViewById(R.cdma.phonetype);
		cid = (TextView) i.findViewById(R.cdma.cid);
		lac = (TextView) i.findViewById(R.cdma.lac);
		rnc = (TextView) i.findViewById(R.cdma.rnc);
		rssi = (TextView) i.findViewById(R.cdma.rxl);
		cdma_ecio = (TextView) i.findViewById(R.cdma.cdma_ecio);
		cids = (TextView) i.findViewById(R.cdma.neighbours_cid);
		rxls = (TextView) i.findViewById(R.cdma.neighbours_rxl);
		neigh_len = (TextView) i.findViewById(R.cdma.neighbours_number);
	}

	@Override
	public void refresh(final CDMA_Measure obj) {
		int phonetype;
		Integer _rssi, _cid, _lac, _rnc, _ecio;
		List<NeighboringCellInfo> neighbours;
		StringBuilder _cids, _rxls, _nlen;
		CDMA_Measure data = (CDMA_Measure) obj;
		phonetype = data.phone_type;
		_rssi = data.rssi;
		_cid = Tools.shortcid_packeddataextraction(data.cid);
		_lac = data.lac;
		_rnc = data.rnc;
		_ecio = data.cdma_ecio;
		neighbours = data.neighbours;

		// Phone Type
		phone_type.setText(Tools.phonetype_to_string(data.phone_type));

		// Cell ID
		cid.setText(Tools.cid_to_string(_cid));

		// Location Area Code
		lac.setText(Tools.cid_to_string(_lac));

		// RSSI && CDMA's Ec/Io
		if (phonetype == TelephonyManager.PHONE_TYPE_GSM) {
			rssi.setText(Tools.rssidbm_gsm_to_string(_rssi));
			cdma_ecio.setText("Invalid for GSM");
		} else {
			rssi.setText(_rssi.toString() + " dBm");
			cdma_ecio.setText(_ecio.toString() + " dB*10");
		}

		// Radio Network Controller
		rnc.setText(_rnc.toString());

		// Neighbours
		if (neighbours == null)
			return;
		_cids = new StringBuilder();
		_rxls = new StringBuilder();
		_nlen = new StringBuilder();
		for (NeighboringCellInfo info : neighbours) {
			if (info == null)
				continue;
			_cid = Tools.shortcid_packeddataextraction(info.getCid());
			_rssi = Tools.asu2dbm_unitconversion(info.getRssi());
			_cids.append(
					Tools.cid_to_string(_cid)
							+ Tools.networktype_to_string(info.getNetworkType()))
					.append('\n');
			_rxls.append(Tools.rssidbm_gsm_to_string(_rssi)).append('\n');
		}
		cids.setText(_cids.toString());
		rxls.setText(_rxls.toString());
		_nlen.append("(").append(neighbours.size()).append(" detected)");
		neigh_len.setText(_nlen.toString());
	}
}