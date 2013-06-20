package jp.kddilabs.tsm.android.smp.services;

interface Battery_ServiceConfigInterface {
    void startRecording(in String format);
    void stopRecording(in String format);
    boolean isRecording(in String format);
}