package com.example.findloadsampleproject

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.findloadsampleproject.databinding.ActivityMainBinding
import com.kakao.sdk.common.KakaoSdk

import com.kakao.sdk.v2.common.BuildConfig.VERSION_NAME
import com.kakaomobility.knsdk.KNLanguageType
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.common.gps.KNGPSData
import com.kakaomobility.knsdk.common.gps.KNGPSReceiver
import com.kakaomobility.knsdk.common.objects.KNError_Code_C103
import com.kakaomobility.knsdk.common.objects.KNError_Code_C302
import com.kakaomobility.knsdk.common.util.DoublePoint
import com.kakaomobility.knsdk.common.util.FloatPoint
import com.kakaomobility.knsdk.guidance.knlteuploader.KNLTEReceiver

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermission()
//        KNSDK.apply {
//            // 콘텍스트 등록 및 DB, 파일 등의 저장 경로 설정
//            install(application, "$filesDir/KNSample")
//
//        }


        binding.knnNaviView.setOnClickListener {
            knsdkAuth("KNNaviView")
        }

        // 서울역
        binding.knnMapView.setOnClickListener {
            knsdkAuth("KNMapView")
        }
    }

    // GPS 위치 권한을 확인
    fun checkPermission() {
        when {
            checkSelfPermission(
                //Manifest.permission.ACCESS_FINE_LOCATION
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED -> {
                // GPS 퍼미션 체크
                gpsPermissionCheck()
            }

            else -> {
                Toast.makeText(this, "필요한 권한들 다 승인됨",Toast.LENGTH_SHORT).show()
            }
        }

    }



    /**
     * GPS 위치 권한을 요청합니다.
     */
    fun gpsPermissionCheck() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            1234)
    }

    //길찾기 SDK 인증을 진행
    fun knsdkAuth(where : String) {


        // 앱 초기화
        FindLoadApplication.knsdk.apply {



            //public final fun initializeWithAppKey(aAppKey: kotlin.String, aClientVersion: kotlin.String, aCsId: kotlin.String? = COMPILED_CODE, aAppUserId: kotlin.String? = COMPILED_CODE, aLangType: com.kakaomobility.knsdk.KNLanguageType = COMPILED_CODE, aCompletion: ((com.kakaomobility.knsdk.common.objects.KNError?) -> kotlin.Unit)?): kotlin.Unit { /* compiled code */ }

            initializeWithAppKey(
                aAppKey = "b054286bb5f77d026e19a3f47557f6b4",
                aClientVersion = VERSION_NAME,
                aCsId = "1171176",
                //aUserKey = "1171176",
                aAppUserId = "xcv",
                aLangType = KNLanguageType.KNLanguageType_KOREAN,
                aCompletion = {
                    if (it != null) {
                        when (it.code) {
                            KNError_Code_C302 -> {
                                Log.v(TAG, "code ${it.code}, message ${it.msg} tagMsg : ${it.tagMsg}")
                                Log.v(TAG,"디버그 키 : ${KakaoSdk.keyHash}")
                            }

                            KNError_Code_C103 -> {
                                Log.v(TAG, "code ${it.code}, message ${it.msg} tagMsg : ${it.tagMsg}")
                            }

                            else -> {
                                Log.v(TAG, "error Occured another reason. error msg : ${it.msg}, code ${it.code}")
                            }
                        }
                    } else {
                        if (where == "KNNaviView") {
                            // KNNaviView 예제로 이동
                            var intent = Intent(this@MainActivity, KNNaviView::class.java)
                            this@MainActivity.startActivity(intent)
                            finish()
                        } else {
                            // KNMapView 예제로 이동
                            var intent = Intent(this@MainActivity, NaviActivity::class.java)
                            this@MainActivity.startActivity(intent)
                            finish()
                        }

                    }
                })
        }

    }
}