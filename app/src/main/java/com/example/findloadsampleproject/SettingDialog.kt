package com.example.findloadsampleproject

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.findloadsampleproject.databinding.DialogSettingBinding


class SettingDialog : DialogFragment() {
    private lateinit var binding : DialogSettingBinding
    private val TAG = "SettingDialog"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogSettingBinding.inflate(layoutInflater)
        val view = binding.root


        binding.dayNightSettingSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
            if(isChecked) {
                Log.v(TAG, "isChecked")
            } else {
                Log.v(TAG, "!isChecked")
            }
        }

        return view
    }


}