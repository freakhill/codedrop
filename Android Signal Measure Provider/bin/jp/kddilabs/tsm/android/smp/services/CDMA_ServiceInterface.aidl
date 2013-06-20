package jp.kddilabs.tsm.android.smp.services;

import jp.kddilabs.tsm.android.smp.measures.CDMA_Measure;

interface CDMA_ServiceInterface {
    CDMA_Measure getLastValue();
}