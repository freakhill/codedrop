package jp.kddilabs.tsm.android.smp;

import jp.kddilabs.tsm.android.Toaster;
import jp.kddilabs.tsm.android.smp.measures.CDMA_Measure;
import jp.kddilabs.tsm.android.smp.measures.WiFi_Measure;
import android.net.wifi.ScanResult;
import android.os.IInterface;

// shameful
// had to code that quickly and ignore commentaries...

public class CdmaWifiRssiCompareTriggerToaster extends TriggerToaster {

	private int cdma_rssi = -200;
	private int wifi_rssi = -200;

	private String bssid;
	private String ssid;

	private boolean first_compare_done = false;
	private boolean cdma_is_best = false;

	private Toaster t;

	@SuppressWarnings("unchecked")
	public CdmaWifiRssiCompareTriggerToaster(Toaster lolz) {
		super(WiFi_Measure.class, CDMA_Measure.class);
		t = lolz;
	}

	public CdmaWifiRssiCompareTriggerToaster wifi(
			MainActivityDataReceiver<WiFi_Measure, ? extends IInterface> rec) {
		addTriger(rec);
		return this;
	}

	public CdmaWifiRssiCompareTriggerToaster cdma(
			MainActivityDataReceiver<CDMA_Measure, ? extends IInterface> rec) {
		addTriger(rec);
		return this;
	}

	public void testData(WiFi_Measure data) {
		for (ScanResult scan : data.scans) {
			if (scan.level > wifi_rssi) {
				wifi_rssi = scan.level;
				bssid = scan.BSSID;
				ssid = scan.SSID;
			}
		}
		compare();
	}

	public void testData(CDMA_Measure data) {
		cdma_rssi = data.rssi;
		compare();
	}

	private void compare() {
		if (!first_compare_done) {
			if (cdma_rssi == -200 || wifi_rssi == -200)
				return;
			if (wifi_rssi > cdma_rssi) {
				t.toast("WiFi stronger\nbssid -> " + bssid + "\nssid -> "
						+ ssid);
				cdma_is_best = false;
			} else {
				t.toast("CDMA stronger");
				cdma_is_best = true;
			}
			first_compare_done = true;
			return;
		}
		if (cdma_is_best) {
			if (wifi_rssi > cdma_rssi) {
				t.toast("WiFi stronger\nbssid -> " + bssid + "\nssid -> "
						+ ssid);
				cdma_is_best = false;
			}
		} else {
			if (wifi_rssi <= cdma_rssi) {
				t.toast("CDMA stronger");
				cdma_is_best = true;
			}
		}
	}

}