package com.example.findloadsampleproject

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.findloadsampleproject.databinding.DialogSettingBinding
import com.kakaomobility.knsdk.map.knmapview.KNMapView
import com.kakaomobility.knsdk.map.uicustomsupport.renewal.theme.base.KNMapTheme


class SettingDialog(private val mapView : KNMapView, context : Context) : Dialog(context) {
    private lateinit var binding : DialogSettingBinding
    private val TAG = "SettingDialog"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.dayNightSettingSwitch.setOnCheckedChangeListener { compoundButton, isNight ->
            if(isNight) {
                // 밤
                mapView.mapTheme = KNMapTheme.driveNight()
            } else {
                // 낮
                mapView.mapTheme = KNMapTheme.driveDay()
            }
        }

    }
}