package com.example.findloadsampleproject

import android.util.Log
import com.kakaomobility.knsdk.common.gps.KNGPSData
import com.kakaomobility.knsdk.common.gps.KNGPSReceiver

class MyGPSReceiver : KNGPSReceiver {
    val TAG = "MyGPSReceiver"
    override fun didReceiveGpsData(aGpsData: KNGPSData) {
        Log.v(TAG, "aGpsData.pos.x : ${aGpsData.pos.x}, aGpsData.pos.y : ${aGpsData.pos.y}")
    }
}