package jp.kddilabs.tsm.android.smp.services;

import jp.kddilabs.tsm.android.smp.measures.Location_Measure;

interface Location_ServiceInterface {
    Location_Measure getLastValue();
}