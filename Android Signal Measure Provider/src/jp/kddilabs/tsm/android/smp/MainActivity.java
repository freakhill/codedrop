package jp.kddilabs.tsm.android.smp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import jp.kddilabs.tsm.android.Toaster;
import jp.kddilabs.tsm.android.smp.measures.AsmpMeasure;
import jp.kddilabs.tsm.android.smp.measures.Battery_Measure;
import jp.kddilabs.tsm.android.smp.measures.CDMA_Measure;
import jp.kddilabs.tsm.android.smp.measures.Location_Measure;
import jp.kddilabs.tsm.android.smp.measures.WiFi_Measure;
import jp.kddilabs.tsm.android.smp.providers.AsmpCSVProvider;
import jp.kddilabs.tsm.android.smp.providers.Battery_CSV_Provider;
import jp.kddilabs.tsm.android.smp.providers.CDMA_CSV_Provider;
import jp.kddilabs.tsm.android.smp.providers.Location_CSV_Provider;
import jp.kddilabs.tsm.android.smp.providers.WiFi_CSV_Provider;
import jp.kddilabs.tsm.android.smp.services.Battery_Service;
import jp.kddilabs.tsm.android.smp.services.Battery_ServiceConfigInterface;
import jp.kddilabs.tsm.android.smp.services.Battery_ServiceInterface;
import jp.kddilabs.tsm.android.smp.services.CDMA_Service;
import jp.kddilabs.tsm.android.smp.services.CDMA_ServiceConfigInterface;
import jp.kddilabs.tsm.android.smp.services.CDMA_ServiceInterface;
import jp.kddilabs.tsm.android.smp.services.Location_Service;
import jp.kddilabs.tsm.android.smp.services.Location_ServiceConfigInterface;
import jp.kddilabs.tsm.android.smp.services.Location_ServiceInterface;
import jp.kddilabs.tsm.android.smp.services.WiFi_Service;
import jp.kddilabs.tsm.android.smp.services.WiFi_ServiceConfigInterface;
import jp.kddilabs.tsm.android.smp.services.WiFi_ServiceInterface;
import jp.kddilabs.tsm.android.smp.viewrefreshers.AsmpViewRefresher;
import jp.kddilabs.tsm.android.smp.viewrefreshers.BatteryViewRefresher;
import jp.kddilabs.tsm.android.smp.viewrefreshers.CdmaViewRefresher;
import jp.kddilabs.tsm.android.smp.viewrefreshers.LocationViewRefresher;
import jp.kddilabs.tsm.android.smp.viewrefreshers.WiFiViewRefresher;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.Toast;

/*
 * Main entrypoint of the application!
 * 
 * @author Johan Gall
 * @author xjo-gall@tsm.kddilabs.jp
 * @version 0.10
 */
public class MainActivity extends TabActivity implements
		OnSharedPreferenceChangeListener {

	private static final String DEBUG_TAG = "ASMP";

	public class MeasurePanel<M extends AsmpMeasure, S extends IInterface, SC extends IInterface, V extends AsmpViewRefresher<M>> {
		public S binder = null;
		public SC config = null;
		public V view = null;
		public ServiceConnection sc = null;
		public ServiceConnection config_sc = null;
		public MainActivityDataReceiver<M, S> receiver;

		public void stop() {
			if (sc != null)
				unbindService(sc);
			if (config_sc != null)
				unbindService(config_sc);
			if (receiver != null)
				getApplicationContext().unregisterReceiver(receiver);

		}
	}

	private Toaster msg;

	private MeasurePanel<CDMA_Measure, CDMA_ServiceInterface, CDMA_ServiceConfigInterface, CdmaViewRefresher> cdma = new MeasurePanel<CDMA_Measure, CDMA_ServiceInterface, CDMA_ServiceConfigInterface, CdmaViewRefresher>();
	private MeasurePanel<Location_Measure, Location_ServiceInterface, Location_ServiceConfigInterface, LocationViewRefresher> location = new MeasurePanel<Location_Measure, Location_ServiceInterface, Location_ServiceConfigInterface, LocationViewRefresher>();
	private MeasurePanel<WiFi_Measure, WiFi_ServiceInterface, WiFi_ServiceConfigInterface, WiFiViewRefresher> wifi = new MeasurePanel<WiFi_Measure, WiFi_ServiceInterface, WiFi_ServiceConfigInterface, WiFiViewRefresher>();
	private MeasurePanel<Battery_Measure, Battery_ServiceInterface, Battery_ServiceConfigInterface, BatteryViewRefresher> battery = new MeasurePanel<Battery_Measure, Battery_ServiceInterface, Battery_ServiceConfigInterface, BatteryViewRefresher>();

	private HashMap<String, Method> preference_op_dispatcher;

	public class InnerTabManager {
		public TabHost tabs;

		public InnerTabManager() {
			tabs = getTabHost();
		}

		public void add(String indicator, int id) {
			tabs.addTab(tabs.newTabSpec(indicator + "_tab")
					.setIndicator(indicator).setContent(id));

			tabs.setCurrentTab(0);
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// creating the tabs
		// TabHost tabs = getTabHost();
		InnerTabManager tabs = new InnerTabManager();

		tabs.add("CDMA", R.id.cdmalayout);
		tabs.add("WiFi", R.id.wifilayout);
		tabs.add("Location", R.id.locationlayout);
		tabs.add("Battery", R.id.batterylayout);

		msg = new Toaster(getApplicationContext());
		t1 = new CdmaWifiRssiCompareTriggerToaster(msg);

		// app preference management block
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		Prefs.setSharedPreferences(sp);
		// setting the preferencelistener!
		initSharedPreferenceListener();
		sp.registerOnSharedPreferenceChangeListener(this);

		// Initialize the tabs' content managers
		cdma.view = new CdmaViewRefresher(this);
		location.view = new LocationViewRefresher(this);
		wifi.view = new WiFiViewRefresher(this);
		battery.view = new BatteryViewRefresher(this);

		Log.i(DEBUG_TAG, "MainActivity will connect to services");

		// big block of initializing connection to remote services
		// and if it succeeds initializing listeners to broadcast
		// msgs from those services!
		// Service: I HAVE NEW DATA!
		// This: WOOT! I USE MY REMOTE CONTROL TO GET YOUR DATA!
		// this is so big because of Java's inner classes and
		// anonymous classes (and design API)
		// but it is logically quite simple (from a high point of view
		// and if you don't search too much :p).

		bind_cdma_service();
		bind_location_service();
		bind_wifi_service();
		bind_battery_service();
	}

	protected void onStart() {
		super.onStart();
	}

	protected void onDestroy() {
		Log.i(DEBUG_TAG, "CDMA stop");
		cdma.stop();
		Log.i(DEBUG_TAG, "Location stop");
		location.stop();
		Log.i(DEBUG_TAG, "WiFi stop");
		wifi.stop();
		Log.i(DEBUG_TAG, "Battery stop");
		battery.stop();
		super.onDestroy();
	}

	private CdmaWifiRssiCompareTriggerToaster t1;

	private void bind_cdma_service() {
		cdma.sc = new AsmpServiceConnection(this, cdma.receiver) {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(DEBUG_TAG, "CDMA Service connected");
				cdma.binder = CDMA_ServiceInterface.Stub.asInterface(service);
				if (cdma.binder != null) {
					// Register to receive cdma service
					// new_data_broadcast
					cdma.receiver = new MainActivityDataReceiver<CDMA_Measure, CDMA_ServiceInterface>(
							cdma.binder, cdma.view);
					t1.cdma(cdma.receiver);
					getApplicationContext().registerReceiver(cdma.receiver,
							new IntentFilter(CDMA_Service.NEW_DATA));
				} else
					Log.e(DEBUG_TAG, "cdma.binder null");

			}
		};
		bindService(IntentGenerator.readingIntent(this, CDMA_Service.class),
				cdma.sc, BIND_AUTO_CREATE);
		// Bind to the CDMA service config
		cdma.config_sc = new AsmpServiceConnection(this, null) {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(DEBUG_TAG, "CDMA Service config connected");
				cdma.config = CDMA_ServiceConfigInterface.Stub
						.asInterface(service);
				// little default refresh rate for tests
				if (cdma.config != null)
					try {
						cdma.config.setRefreshPeriod(Prefs
								.getInt(Prefs.PERIOD_CDMA_ID));
						if (Prefs.getBoolean(Prefs.RECORD_CDMA_ID)) {
							cdma.config
									.startRecording(CDMA_CSV_Provider.RECORDING_FORMAT);
						}
					} catch (RemoteException e) {
						Log.e(DEBUG_TAG, "Remote exception on cdma.config use",
								e);
					}
				else
					Log.e(DEBUG_TAG, "could not create cdma.config");
			}
		};
		bindService(IntentGenerator.configIntent(this, CDMA_Service.class),
				cdma.config_sc, BIND_AUTO_CREATE);
	}

	private void bind_location_service() {
		location.sc = new AsmpServiceConnection(this, location.receiver) {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(DEBUG_TAG, "Location Service connected");
				location.binder = Location_ServiceInterface.Stub
						.asInterface(service);
				// Register to receive location service
				// new_data_broadcast
				if (location.binder != null) {
					location.receiver = new MainActivityDataReceiver<Location_Measure, Location_ServiceInterface>(
							location.binder, location.view);
					getApplicationContext().registerReceiver(location.receiver,
							new IntentFilter(Location_Service.NEW_DATA));
				} else
					Log.e(DEBUG_TAG, "location.binder null");
			}
		};
		bindService(
				IntentGenerator.readingIntent(this, Location_Service.class),
				location.sc, BIND_AUTO_CREATE);
		// Bind to the Location service config
		location.config_sc = new AsmpServiceConnection(this, null) {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(DEBUG_TAG, "Location Service config connected");
				location.config = Location_ServiceConfigInterface.Stub
						.asInterface(service);
				if (location.config != null) {
					if (Prefs.getBoolean(Prefs.RECORD_LOCATION_ID))
						try {
							location.config.setRefreshPeriod(Prefs
									.getInt(Prefs.PERIOD_LOCATION_ID));
							location.config
									.startRecording(Location_CSV_Provider.RECORDING_FORMAT);
						} catch (RemoteException e) {
							Log.e(DEBUG_TAG, "remote exception", e);
						}
				} else
					Log.e(DEBUG_TAG, "location.config null");
			}
		};
		bindService(IntentGenerator.configIntent(this, Location_Service.class),
				location.config_sc, BIND_AUTO_CREATE);
	}

	private void bind_wifi_service() {
		wifi.sc = new AsmpServiceConnection(this, wifi.receiver) {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(DEBUG_TAG, "WiFi Service connected");
				wifi.binder = WiFi_ServiceInterface.Stub.asInterface(service);
				// Register to receive cdma service
				// new_data_broadcast
				if (wifi.binder != null) {
					wifi.receiver = new MainActivityDataReceiver<WiFi_Measure, WiFi_ServiceInterface>(
							wifi.binder, wifi.view);
					t1.wifi(wifi.receiver);
					getApplicationContext().registerReceiver(wifi.receiver,
							new IntentFilter(WiFi_Service.NEW_DATA));
				}
			}
		};
		bindService(IntentGenerator.readingIntent(this, WiFi_Service.class),
				wifi.sc, BIND_AUTO_CREATE);
		// Bind to the WiFi service config
		wifi.config_sc = new AsmpServiceConnection(this, null) {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(DEBUG_TAG, "WiFi Service config connected");
				wifi.config = WiFi_ServiceConfigInterface.Stub
						.asInterface(service);
				// little default refresh rate for tests
				if (wifi.config != null)
					try {
						wifi.config.setRefreshPeriod(Prefs
								.getInt(Prefs.PERIOD_WIFI_ID));
						if (Prefs.getBoolean(Prefs.RECORD_WIFI_ID)) {
							wifi.config
									.startRecording(WiFi_CSV_Provider.RECORDING_FORMAT);
						}
					} catch (RemoteException e) {
						Log.e(DEBUG_TAG,
								"remote exception on using wifi.config", e);
					}
				else
					Log.e(DEBUG_TAG, "wifi.config null");
			}
		};
		bindService(IntentGenerator.configIntent(this, WiFi_Service.class),
				wifi.config_sc, BIND_AUTO_CREATE);
	}

	private void bind_battery_service() {
		battery.sc = new AsmpServiceConnection(this, battery.receiver) {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(DEBUG_TAG, "Battery Service connected");
				battery.binder = Battery_ServiceInterface.Stub
						.asInterface(service);
				// Register to receive battery service
				// new_data_broadcast
				if (battery.binder != null) {
					battery.receiver = new MainActivityDataReceiver<Battery_Measure, Battery_ServiceInterface>(
							battery.binder, battery.view, true);
					getApplicationContext().registerReceiver(battery.receiver,
							new IntentFilter(Battery_Service.NEW_DATA));
				} else
					Log.e(DEBUG_TAG, "battery.binder null");
			}
		};
		bindService(IntentGenerator.readingIntent(this, Battery_Service.class),
				battery.sc, BIND_AUTO_CREATE);
		// Bind to the Location service config
		battery.config_sc = new AsmpServiceConnection(this, null) {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(DEBUG_TAG, "Battery Service config connected");
				battery.config = Battery_ServiceConfigInterface.Stub
						.asInterface(service);
				if (battery.config != null) {
					if (Prefs.getBoolean(Prefs.RECORD_BATTERY_ID))
						try {
							battery.config
									.startRecording(Battery_CSV_Provider.RECORDING_FORMAT);
						} catch (RemoteException e) {
							Log.e(DEBUG_TAG, "remote exception", e);
						}
				} else
					Log.e(DEBUG_TAG, "battery.config null");
			}
		};
		bindService(IntentGenerator.configIntent(this, Battery_Service.class),
				battery.config_sc, BIND_AUTO_CREATE);
	}

	/*
	 * @SuppressWarnings({ "unchecked", "unused" }) private void test() { try {
	 * Class cls = Class.forName("android.os.SystemProperties"); Method get_prop
	 * = cls.getDeclaredMethod("get", String.class, String.class); String val =
	 * (String) get_prop.invoke(cls, "ro.carrier", "default"); // String val2 =
	 * (String) get_prop.invoke(cls, "ro.phonenumber", // "default");
	 * TelephonyManager lol = (TelephonyManager)
	 * getSystemService(Context.TELEPHONY_SERVICE); Log.d(DEBUG_TAG,
	 * "sysprop start"); Log.d(DEBUG_TAG, "ro.carrier - " + val);
	 * Log.d(DEBUG_TAG, "ro.phonenumber - " + lol.getLine1Number());
	 * Log.d(DEBUG_TAG, "sysprop end"); } catch (IllegalArgumentException e1) {
	 * e1.printStackTrace(); } catch (IllegalAccessException e1) {
	 * e1.printStackTrace(); } catch (InvocationTargetException e1) {
	 * e1.printStackTrace(); } catch (SecurityException e1) {
	 * e1.printStackTrace(); } catch (NoSuchMethodException e1) {
	 * e1.printStackTrace(); } catch (ClassNotFoundException e1) {
	 * e1.printStackTrace(); } }
	 */
	private void initSharedPreferenceListener() {
		// getting all the methods starting by pref_ and making them public
		// at runtime :p
		// also, put them in the dispatcher dictionnary associated to
		// what follow pref_ (in the name of the function) as key.
		preference_op_dispatcher = new HashMap<String, Method>();

		for (Method m : MainActivity.class.getDeclaredMethods()) {
			String method_name = m.getName();
			if (method_name.startsWith("pref_")) {
				m.setAccessible(true);
				preference_op_dispatcher.put(method_name.substring(5), m);
				Log.d(DEBUG_TAG,
						"preference_op_dispatcher key = "
								+ method_name.substring(5) + " method = "
								+ m.toString());
			}
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// traditional hashmap dispatcher :p
		// a lot quicker than a if key is this do that else ... chain
		// (we trade with init time)
		Method meth = preference_op_dispatcher.get(key);
		if (meth != null) {
			// meth = preference_op_dispatcher.get(key);
			try {
				meth.invoke(this);
			} catch (InvocationTargetException e) {
				Log.e(DEBUG_TAG,
						"onSharedPreferenceChanged InvocationTargetException key = "
								+ key, e.getTargetException());
				Toast.makeText(getApplicationContext(),
						"Preference modification failed! key = " + key,
						Toast.LENGTH_SHORT).show();
			} catch (IllegalArgumentException e) {
				Log.e(DEBUG_TAG,
						"onSharedPreferenceChanged IllegalArgumentException key = "
								+ key, e);
				Toast.makeText(
						getApplicationContext(),
						"Preference modification failed!\nIllegalArgumentException\nkey = "
								+ key, Toast.LENGTH_SHORT).show();
			} catch (IllegalAccessException e) {
				Log.e(DEBUG_TAG,
						"onSharedPreferenceChanged IllegalAccessException key = "
								+ key, e);
				Toast.makeText(
						getApplicationContext(),
						"Preference modification failed!\nIllegalAccessException\nkey = "
								+ key, Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(getApplicationContext(),
					"The preference key " + key + " is not managed yet!",
					Toast.LENGTH_SHORT).show();
		}
	}

	// callbacks when preferences change
	// name = pref_+preference key

	// suppresswarning is necessary so that eclipse won't cry
	// because at compile time no one call these functions
	// they are discovered at runtime then used to set preferences
	@SuppressWarnings("unused")
	private void pref_record_cdma() {
		try {
			if (Prefs.getBoolean(Prefs.RECORD_CDMA_ID))
				cdma.config.startRecording(AsmpCSVProvider.RECORDING_FORMAT);
			else
				cdma.config.stopRecording(AsmpCSVProvider.RECORDING_FORMAT);
		} catch (RemoteException e) {
			msg.toast_and_warn(
					"'Start (or Stop) cdma recording' remote call failed", e);
		}
	}

	@SuppressWarnings("unused")
	private void pref_record_wifi() {
		try {
			if (Prefs.getBoolean(Prefs.RECORD_WIFI_ID))
				wifi.config.startRecording(AsmpCSVProvider.RECORDING_FORMAT);
			else
				wifi.config.stopRecording(AsmpCSVProvider.RECORDING_FORMAT);
		} catch (RemoteException e) {
			msg.toast_and_warn(
					"'Start (or Stop) cdma recording' remote call failed", e);
		}
	}

	@SuppressWarnings("unused")
	private void pref_record_location() {
		try {
			if (Prefs.getBoolean(Prefs.RECORD_LOCATION_ID))
				location.config
						.startRecording(AsmpCSVProvider.RECORDING_FORMAT);
			else
				location.config.stopRecording(AsmpCSVProvider.RECORDING_FORMAT);
		} catch (RemoteException e) {
			msg.toast_and_warn(
					"'Start (or Stop) location recording' remote call failed",
					e);
		}
	}

	@SuppressWarnings("unused")
	private void pref_record_battery() {
		if (battery.config == null) {
			msg.toast_and_warn(
					"Activity failed to connect to the battery recording config service!",
					new Exception(""));
			return;
		}
		try {
			if (Prefs.getBoolean(Prefs.RECORD_BATTERY_ID))
				battery.config.startRecording(AsmpCSVProvider.RECORDING_FORMAT);
			else
				battery.config.stopRecording(AsmpCSVProvider.RECORDING_FORMAT);
		} catch (RemoteException e) {
			msg.toast_and_warn(
					"'Start (or Stop) battery recording' remote call failed", e);
		}
	}

	@SuppressWarnings("unused")
	private void pref_period_cdma() {
		try {
			cdma.config.setRefreshPeriod(Prefs.getInt(Prefs.PERIOD_CDMA_ID));
		} catch (RemoteException e) {
			msg.toast_and_warn("Setting of cdma refresh period failed", e);
		}
	}

	@SuppressWarnings("unused")
	private void pref_period_wifi() {
		try {
			wifi.config.setRefreshPeriod(Prefs.getInt(Prefs.PERIOD_WIFI_ID));
		} catch (RemoteException e) {
			msg.toast_and_warn("Setting of wifi refresh period failed", e);
		}
	}

	@SuppressWarnings("unused")
	private void pref_period_location() {
		try {
			location.config.setRefreshPeriod(Prefs
					.getInt(Prefs.PERIOD_LOCATION_ID));
		} catch (RemoteException e) {
			msg.toast_and_warn("Setting of location refresh period failed", e);
		}
	}

	@SuppressWarnings("unused")
	private void pref_provider_location() {
		try {
			location.config.setProvider(Prefs
					.getString(Prefs.PROVIDER_LOCATION_ID));
		} catch (RemoteException e) {
			msg.toast_and_warn("Setting of location provider failed", e);
		}
	}

	private final static int MENU_RECORD_ITEM = 0;
	// private final static int MENU_VISUALIZE_ITEM = 1;
	private final static int MENU_SETTINGS_ITEM = 2;

	private MenuItem record_menuitem = null;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();

		record_menuitem = menu.add(0, MENU_RECORD_ITEM, 0, "Record")
				.setIcon(android.R.drawable.btn_star_big_off)
				.setCheckable(true);
		// menu.add(0, MENU_VISUALIZE_ITEM, 0, "Visualize").setIcon(
		// android.R.drawable.ic_menu_preferences)
		// .setIntent(new Intent(this, SettingsActivity.class));
		menu.add(0, MENU_SETTINGS_ITEM, 0, "Settings")
				.setIcon(android.R.drawable.ic_menu_preferences)
				.setIntent(new Intent(this, SettingsActivity.class));

		return true;
	}

	private void displayAsRecording() {
		record_menuitem.setChecked(true);
		record_menuitem.setIcon(android.R.drawable.btn_star_big_on);
		record_menuitem.setTitle("Recording");
	}

	private void displayAsNotRecording() {
		record_menuitem.setChecked(false);
		record_menuitem.setIcon(android.R.drawable.btn_star_big_off);
		record_menuitem.setTitle("Record");
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		try {
			if (wifi.config.isRecording("CSV")
					|| cdma.config.isRecording("CSV")
					|| location.config.isRecording("CSV")
					|| battery.config.isRecording("CSV")) {
				displayAsRecording();
			} else {
				displayAsNotRecording();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// nothing more than an if, I put the structure for possible modifi
		// cations later
		switch (item.getItemId()) {
		case MENU_RECORD_ITEM:
			if (!item.isChecked()) {
				displayAsRecording();
				try {
					if (Prefs.getBoolean(Prefs.RECORD_CDMA_ID))
						cdma.config
								.startRecording(AsmpCSVProvider.RECORDING_FORMAT);
					if (Prefs.getBoolean(Prefs.RECORD_WIFI_ID))
						wifi.config
								.startRecording(AsmpCSVProvider.RECORDING_FORMAT);
					if (Prefs.getBoolean(Prefs.RECORD_LOCATION_ID))
						location.config
								.startRecording(AsmpCSVProvider.RECORDING_FORMAT);
					if (Prefs.getBoolean(Prefs.RECORD_BATTERY_ID))
						battery.config
								.startRecording(AsmpCSVProvider.RECORDING_FORMAT);
				} catch (RemoteException e) {
					msg.toast_and_err("'Start ? recording' remote call failed",
							e);
				}
			} else {
				displayAsNotRecording();
				try {
					cdma.config.stopRecording(AsmpCSVProvider.RECORDING_FORMAT);
					wifi.config.stopRecording(AsmpCSVProvider.RECORDING_FORMAT);
					location.config
							.stopRecording(AsmpCSVProvider.RECORDING_FORMAT);
					battery.config
							.stopRecording(AsmpCSVProvider.RECORDING_FORMAT);
				} catch (RemoteException e) {
					msg.toast_and_err("'Stop ? recording' remote call failed",
							e);
				}
			}
		}
		return false;
	}
}