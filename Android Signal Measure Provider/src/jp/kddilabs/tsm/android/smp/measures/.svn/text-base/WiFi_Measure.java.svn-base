package jp.kddilabs.tsm.android.smp.measures;

import java.lang.reflect.Field;
import java.util.List;

import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class WiFi_Measure extends AsmpMeasure implements Parcelable {

	// blaaablaaablaaa
	public static final Parcelable.Creator<WiFi_Measure> CREATOR = new Parcelable.Creator<WiFi_Measure>() {

		@Override
		public WiFi_Measure createFromParcel(Parcel source) {
			return new WiFi_Measure(source);
		}

		@Override
		public WiFi_Measure[] newArray(int size) {
			return new WiFi_Measure[size];
		}
	};
	private static Parcelable.Creator<ScanResult> SR_CREATOR = null;

	public long scan_call_date;
	public long scan_end_event_date;
	public List<ScanResult> scans;

	public WiFi_Measure() {

	}

	@SuppressWarnings("unchecked")
	public WiFi_Measure(Parcel source) {
		if (SR_CREATOR == null) {
			// horrible horrible hack to get the creator DAMN!
			try {
				Field SR_CREATOR_field;
				Class<ScanResult> ScanResult_class;
				ScanResult_class = (Class<ScanResult>) Class
						.forName(ScanResult.class.getName());
				SR_CREATOR_field = ScanResult_class.getDeclaredField("CREATOR");
				SR_CREATOR_field.setAccessible(true);
				SR_CREATOR = (Creator<ScanResult>) SR_CREATOR_field.get(source);
			} catch (ClassNotFoundException e) {
				Log.d(DEBUG_TAG, e.getMessage());
				e.printStackTrace();
			} catch (SecurityException e) {
				Log.d(DEBUG_TAG, e.getMessage());
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				Log.d(DEBUG_TAG, e.getMessage());
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				Log.d(DEBUG_TAG, e.getMessage());
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				Log.d(DEBUG_TAG, e.getMessage());
				e.printStackTrace();
			}
		}

		super.readFromParcel(source);
		scan_call_date = source.readLong();
		scan_end_event_date = source.readLong();
		source.readTypedList(scans, SR_CREATOR);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeLong(scan_call_date);
		dest.writeLong(scan_end_event_date);
		dest.writeTypedList(scans);
	}

}
