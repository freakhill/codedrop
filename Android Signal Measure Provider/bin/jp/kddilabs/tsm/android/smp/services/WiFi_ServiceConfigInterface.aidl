package jp.kddilabs.tsm.android.smp.services;

interface WiFi_ServiceConfigInterface {
    void setRefreshPeriod(in int period);
    void startRecording(in String format);
    void stopRecording(in String format);
    boolean isRecording(in String format);
}