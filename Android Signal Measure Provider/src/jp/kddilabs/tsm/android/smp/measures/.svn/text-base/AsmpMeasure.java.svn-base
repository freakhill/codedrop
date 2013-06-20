package jp.kddilabs.tsm.android.smp.measures;

import android.os.Parcel;
import android.os.Parcelable;

abstract public class AsmpMeasure implements Parcelable {
	protected final static String DEBUG_TAG = "ASMP";
	public long date;

	// how to read and write the data
	@Override
	synchronized public void writeToParcel(Parcel writer, int arg1) {
		writer.writeLong(date);
	}

	protected void readFromParcel(Parcel reader) {
		date = reader.readLong();
	}
}
