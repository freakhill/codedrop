package jp.kddilabs.tsm.android;

import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;

public class Tools {
	// Why such an insignificant function?
	// Now I can easily track where asu2dbm conversions are made in the code
	// + the compiler will inline it (if decent...) so I don't care!
	public final static int asu2dbm_unitconversion(final int asu) {
		return asu * 2 - 113;
	}

	public final static String rssidbm_gsm_to_string(final Integer dbm) {
		switch (dbm) {
		case (-113):
			return "-113 or less dBm";
		case (85):
			return "Unknown or undetectable";
		default:
			if (dbm < -51)
				return dbm.toString() + " dBm";
			return "-51 or more dBm";
		}
	}

	public final static int shortcid_packeddataextraction(final int longcid) {
		return longcid & 0xffff;
	}

	public final static String cid_to_string(final Integer cid) {
		return cid == NeighboringCellInfo.UNKNOWN_CID ? "UNKNOWN VALUE" : cid
				.toString();
	}

	public final static String phonetype_to_string(final int phone_type) {
		switch (phone_type) {
		case (TelephonyManager.PHONE_TYPE_CDMA):
			return "CDMA";
		case (TelephonyManager.PHONE_TYPE_GSM):
			return "GSM";
		default:
			return "UNKNOWN";
		}
	}

	public static String networktype_to_string(int networkType) {
		switch (networkType) {
		case TelephonyManager.NETWORK_TYPE_CDMA:
			return "CDMA";
		case TelephonyManager.NETWORK_TYPE_EDGE:
			return "EDGE";
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return "EVDO_0";
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return "EVD0_A";
		case TelephonyManager.NETWORK_TYPE_GPRS:
			return "GPRS";
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			return "HDSPA";
		case TelephonyManager.NETWORK_TYPE_HSPA:
			return "HSPA";
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			return "HSUPA";
		case TelephonyManager.NETWORK_TYPE_UMTS:
			return "UMTS";
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			return "UNKNOWN";
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return "1xRTT";
		default:
			return "INCORRECT NETWORK TYPE";
		}
	}
}
