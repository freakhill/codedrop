package jp.kddilabs.tsm.android.smp.services;

interface Location_ServiceConfigInterface {
    void setProvider(in String provider);
    void setRefreshPeriod(in int period);
    void startRecording(in String format);
    void stopRecording(in String format);
    boolean isRecording(in String format);
}