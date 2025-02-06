package com.example.findloadsampleproject

import android.app.Dialog
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.findloadsampleproject.databinding.DialogSettingBinding
import com.kakaomobility.knsdk.KNCarFuel
import com.kakaomobility.knsdk.KNCarType
import com.kakaomobility.knsdk.KNCarUsage
import com.kakaomobility.knsdk.KNRoutePriority
import com.kakaomobility.knsdk.common.gps.KATECToWGS84
import com.kakaomobility.knsdk.common.gps.WGS84ToKATEC
import com.kakaomobility.knsdk.common.objects.KNPOI
import com.kakaomobility.knsdk.map.knmaprenderer.objects.KNMapCameraUpdate
import com.kakaomobility.knsdk.map.knmapview.KNMapView
import com.kakaomobility.knsdk.map.uicustomsupport.renewal.KNMapMarker
import com.kakaomobility.knsdk.map.uicustomsupport.renewal.theme.base.KNMapRouteTheme
import com.kakaomobility.knsdk.map.uicustomsupport.renewal.theme.base.KNMapTheme
import com.kakaomobility.knsdk.trip.knrouteconfiguration.KNRouteConfiguration


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
                mapView.mapTheme = KNMapTheme.driveDay()
                mapView.mapTheme = KNMapTheme.searchNight()
            } else {
                // 낮
                mapView.mapTheme = KNMapTheme.driveNight()
                mapView.mapTheme = KNMapTheme.searchDay()
            }
        }

        binding.is3dMatrix.setOnCheckedChangeListener { compoundButton, isNight ->
            if(isNight) {
                // 3D
                mapView.animateCamera(KNMapCameraUpdate.tiltTo(45f), 0, false, false)
            } else {
                // 2D
                mapView.animateCamera(KNMapCameraUpdate.tiltTo(0f), 0, false, false)
            }
        }

        binding.isVisibleTraffic.setOnCheckedChangeListener { compoundButton, turnOn ->
            if(turnOn) {
                // 지도상 교통량 정보 표시
                mapView.isVisibleTraffic = true
            } else {
                // 가리기
                mapView.isVisibleTraffic = false
            }
        }

        binding.addVia.setOnCheckedChangeListener { compoundButton, addVia ->
            if(addVia) {
                // 경유지 추가
                addMarkers()
            } else {
                // 모든 마커를 제거
                mapView.removeMarkersAll()

//                // 아래 코드처럼 하나의 마커를 설정해서 경유지 하나만 제거하는것도 가능! 미성옥이 사라진다
//                val pos1 = WGS84ToKATEC(127.097505, 37.334529)
//                val marker1 = KNMapMarker(pos1.toFloatPoint())
//                mapView.removeMarker(marker1)
            }
        }

        binding.mockPlay.setOnClickListener {
            startMockDrive()
        }

    }

    // 다양한 마커를 추가하는 메소드
    private fun addMarkers(){
        // 리움 미술관
        val pos1 = WGS84ToKATEC(126.999373, 37.538438)
        val marker1 = KNMapMarker(pos1.toFloatPoint())
        //동천집
        val pos2 = WGS84ToKATEC(127.100248, 37.331077)
        val marker2 = KNMapMarker(pos2.toFloatPoint())

        //미성옥
        val pos3 = WGS84ToKATEC(127.097505, 37.334529)
        var miseongokImg = BitmapFactory.decodeResource(context.resources, R.drawable.miseongok)
        val marker3 = KNMapMarker(pos3.toFloatPoint()).also {
            it.icon = miseongokImg
        }


        // 경유지들을 모아놓은 리스트
        val knMapMarkerList = listOf<KNMapMarker>(marker1, marker2, marker3)

        mapView.addMarkers(knMapMarkerList)
    }


    // 모의주행 실행함수
    private fun startMockDrive() {
        val x = FindLoadApplication.knsdk.sharedGpsManager()?.recentGpsData?.pos?.x ?: 0.0
        val y = FindLoadApplication.knsdk.sharedGpsManager()?.recentGpsData?.pos?.y ?: 0.0
        var currentLocWGS = KATECToWGS84(x, y)

        // 예시 목적지는 서울역
        val goalKatec = WGS84ToKATEC(126.972263, 37.556091)
        val startKatec = WGS84ToKATEC(currentLocWGS.x, currentLocWGS.y)

        // 출발지와 목적지를 설정합니다.
        val startPoi = KNPOI("현위치", startKatec.x.toInt(),startKatec.y.toInt(),"현위치")
        val goalPoi = KNPOI("목적지",goalKatec.x.toInt(),goalKatec.y.toInt(),"목적지")

        FindLoadApplication.knsdk.makeTripWithStart(startPoi, goalPoi, null) { mockDriveError, mockTrip ->
            if(mockDriveError != null) {
                Log.v(TAG, "mockDriveError msg : ${mockDriveError.msg} code : ${mockDriveError.code}")
            } else {

                // 속도
                val speed = 70

                // 경로 옵션
                val routeConfig = KNRouteConfiguration(
                    aCarType = KNCarType.KNCarType_2,   // 2종 중형차
                    aFuel = KNCarFuel.KNCarFuel_Gasoline,
                    aUsage = KNCarUsage.KNCarUsage_Default,
                    aUseHipass = true,
                    aCarWidth = -1,
                    aCarHeight = -1,
                    aCarLength = -1,
                    aCarWeight = -1,
                )

                FindLoadApplication.knsdk.sharedGpsManager()?.simulationFromStart(
                    aStart = mockTrip!!.start,
                    aGoal = mockTrip!!.goal,
                    aVias = null,
                    aPriority = KNRoutePriority.KNRoutePriority_Recommand,  // 추천 경로를 기반으로 목적지까지 경로 안내
                    aAvoidOptions = 0,  // KNRouteAvoidOption_None 경로에서 회피 구간 없음으로 설정
                    aRouteConfig = routeConfig,
                    aMaxSpd = speed,
                    aUseSamePace = true
                )

            }
        }



//        // [주행정지]
//        binding.btnFakeDriveStop.setOnClickListener {
//            // routeEnd()
//            DrtApplication.knsdk.sharedGuidance()?.stop()
//            return@setOnClickListener
//        }
//
//        // [주행재개]
//        binding.btnFakeDriveResume.setOnClickListener {
//            viewModel.dispatchPathInfo()
//            return@setOnClickListener
//        }


    }

}