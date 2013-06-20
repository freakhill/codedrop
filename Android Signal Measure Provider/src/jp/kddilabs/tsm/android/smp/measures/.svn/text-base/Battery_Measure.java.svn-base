package jp.kddilabs.tsm.android.smp.measures;

import android.os.Parcel;
import android.os.Parcelable;

public class Battery_Measure extends AsmpMeasure implements Parcelable {

	// blaaablaaablaaa
	public static final Parcelable.Creator<Battery_Measure> CREATOR = new Parcelable.Creator<Battery_Measure>() {

		@Override
		public Battery_Measure createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new Battery_Measure(source);
		}

		@Override
		public Battery_Measure[] newArray(int size) {
			// TODO Auto-generated method stub
			return new Battery_Measure[size];
		}
	};

	public float level;

	public Battery_Measure() {

	}

	public Battery_Measure(Parcel source) {
		readFromParcel(source);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel writer, int flags) {
		super.writeToParcel(writer, flags);
		writer.writeFloat(level);
	}

	protected void readFromParcel(Parcel source) {
		super.readFromParcel(source);
		level = source.readFloat();
	}

}
