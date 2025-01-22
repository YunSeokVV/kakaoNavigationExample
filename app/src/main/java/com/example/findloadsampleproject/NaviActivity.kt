package com.example.findloadsampleproject

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.findloadsampleproject.databinding.ActivityNaviBinding
import com.kakaomobility.knsdk.KNCarFuel
import com.kakaomobility.knsdk.KNCarType
import com.kakaomobility.knsdk.KNCarUsage
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.viewModels
import java.io.File
import com.kakaomobility.knsdk.KNRouteAvoidOption
import com.kakaomobility.knsdk.KNRoutePriority
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.common.gps.KATECToWGS84
import com.kakaomobility.knsdk.common.gps.KNGPSData
import com.kakaomobility.knsdk.common.gps.KNGPSReceiver
import com.kakaomobility.knsdk.common.gps.KNLocationReceiver
import com.kakaomobility.knsdk.common.gps.WGS84ToKATEC
import com.kakaomobility.knsdk.common.objects.KNError
import com.kakaomobility.knsdk.common.objects.KNPOI
import com.kakaomobility.knsdk.common.util.DoublePoint
import com.kakaomobility.knsdk.common.util.FloatPoint
import com.kakaomobility.knsdk.common.util.IntPoint
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_CitsGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_GuideStateDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_LocationGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_RouteGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_SafetyGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_VoiceGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuideRouteChangeReason
import com.kakaomobility.knsdk.guidance.knguidance.citsguide.KNGuide_Cits
import com.kakaomobility.knsdk.guidance.knguidance.KNDriveGuidance
import com.kakaomobility.knsdk.guidance.knguidance.common.KNLocation
import com.kakaomobility.knsdk.guidance.knguidance.locationguide.KNGuide_Location
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.KNGuide_Route
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.objects.KNMultiRouteInfo
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.objects.KNRoadInfo
import com.kakaomobility.knsdk.guidance.knguidance.safetyguide.KNGuide_Safety
import com.kakaomobility.knsdk.guidance.knguidance.safetyguide.objects.KNSafety
import com.kakaomobility.knsdk.guidance.knguidance.voiceguide.KNGuide_Voice
import com.kakaomobility.knsdk.guidance.knguidance.voiceguide.KNVoiceCode
import com.kakaomobility.knsdk.guidance.knguidance.voiceguide.KNVoiceDist
import com.kakaomobility.knsdk.map.knmapimage.KNMapImage
import com.kakaomobility.knsdk.map.knmaprenderer.objects.KNMapCameraUpdate
import com.kakaomobility.knsdk.map.knmapview.KNMapView
import com.kakaomobility.knsdk.map.knmapview.idl.KNMapRouteEventListener
import com.kakaomobility.knsdk.map.knmapview.idl.KNMapViewEventListener
import com.kakaomobility.knsdk.map.knmapview.idl.KNMarkerEventListener
import com.kakaomobility.knsdk.map.uicustomsupport.renewal.KNMapMarker
import com.kakaomobility.knsdk.map.uicustomsupport.renewal.theme.base.KNMapRouteTheme
import com.kakaomobility.knsdk.map.uicustomsupport.renewal.theme.base.KNMapTheme
import com.kakaomobility.knsdk.map.uicustomsupport.renewal.theme.base.entity.KNRouteColors
import com.kakaomobility.knsdk.trip.knrouteconfiguration.KNRouteConfiguration
import com.kakaomobility.knsdk.trip.kntrip.knroute.KNRoute
import java.io.FileOutputStream

class NaviActivity : AppCompatActivity() {
    // 현재 사용자의 시점이 1인칭인지 3인칭인지 판별해주는 변수
    var _userPOV = MutableLiveData<Int>(1)
    val userPOV :LiveData<Int>  = _userPOV
    val viewModel by viewModels<NaviViewModel>()

    lateinit var userTBTIcon : Bitmap

    // 각종 시스템 정보를 표현할지 여부
    var infoVisibility = false

    // 현재 시스템의 각종 정보를 표현해주기 위한 문자열
    var _currentSpeed = MutableLiveData<String>()
    val currentSpeed : LiveData<String> = _currentSpeed

    var currentZoom = 0f

    val TAG = "NaviActivity"
    private lateinit var binding : ActivityNaviBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNaviBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FindLoadApplication.knsdk.isShowBuilding = true

        initMapView(binding.mapView)

        binding.mapView.userLocation?.icon

        binding.setting.setOnClickListener {
            val dialog = SettingDialog(binding.mapView, this)
            dialog.show()
        }

        initMapEventListener()
        userPOV.observe(this, { POV ->
            binding.userPOV.setText("${POV}인칭 시점")
            if(POV == 1) {
                // 1인칭 시점인 경우
                // 줌설정이 너무 크거나 작지않게 다시 재설정해준다
                binding.mapView.moveCamera(KNMapCameraUpdate.zoomTo(2.5f), false, false)
                binding.btnCurrentLocation.visibility = View.GONE
            } else if(POV == 3) {
                // 3인칭 시점인 경우
                binding.btnCurrentLocation.visibility = View.VISIBLE
            }
        })

        currentSpeed.observe(this, { speed ->
            binding.currentSpeed.setText("현재 속도 :${currentSpeed.value}")

        })

        viewModel.currentSafetyCode.observe(this, { info ->
            binding.knSafetyCode.setText("현재 경로 안내 표시 정보 : ${info}")
        })

        viewModel.currentTrafficSpd.observe(this, { info ->
            binding.trafficSpd.setText("경로 내 현재 위치한 도로의 주행 속도 정보 : ${info}")
        })

        viewModel.roadName.observe(this, { road ->
            binding.roadName.setText("현재 주행중인 도로명 : ${road}")
        })

        viewModel.roadType.observe(this, { roadType ->
            binding.roadType.setText("현재 주행중인 도로타입 : ${roadType}")
        })

        // 1인칭 시점으로 다시 전환
        binding.btnCurrentLocation.setOnClickListener {
            _userPOV.value = 1
        }

        // 줌인
        binding.zoomIn.setOnClickListener {
            currentZoom -= 0.5f
            binding.mapView.animateCamera(KNMapCameraUpdate.zoomTo(currentZoom), 0, false, false)
        }

        // 줌아웃
        binding.zoomOut.setOnClickListener {
            currentZoom += 0.5f
            binding.mapView.animateCamera(KNMapCameraUpdate.zoomTo(currentZoom), 0, false, false)
        }

        binding.imageView.setOnClickListener { visibility ->
            infoVisibility = !infoVisibility
            if(infoVisibility) {
                binding.infoLayout.visibility = View.VISIBLE
            } else {
                binding.infoLayout.visibility = View.GONE
            }
        }


    }

    private fun initMapEventListener() {
        binding.mapView.mapViewEventListener = object : KNMapViewEventListener {
            override fun onBearingChanging(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                bearing: Float
            ) {
                val tmp = 3
            }

            override fun onBearingEnded(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                bearing: Float
            ) {
                val tmp = 3
            }

            override fun onBearingStarted(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                bearing: Float
            ) {
                val tmp = 3
            }

            override fun onCameraAnimationCanceled(
                mapView: KNMapView?,
                cameraUpdate: KNMapCameraUpdate?
            ) {
                Log.v(TAG, "onCameraAnimationCanceled")
            }

            override fun onCameraAnimationEnded(
                mapView: KNMapView?,
                cameraUpdate: KNMapCameraUpdate?
            ) {
                binding.mapView.userLocation?.apply {
                    isVisible = true
                    isVisibleGuideLine = true
                    // 사용자 위치가 표시되는 카텍(KATEC) 좌표
                    //coordinate = currentGPSData.toFloatPoint()
                }
                    // icon 속성을 없애면 기본TBT로 설정됨. GPS정보를 찾을 수 없을때 회색 TBT아이콘을 설정할 방법이 KNMapView에는 현재 존재하지 않음. 아래 데브톡에서 문의중.
                    //https://devtalk.kakao.com/t/knsdk-tbt/142014
                    ?.icon = userTBTIcon

            }

            override fun onDoubleTapped(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                coordinate: FloatPoint
            ) {
                val tmp = 3
            }

            override fun onLongPressed(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                coordinate: FloatPoint
            ) {
                val tmp = 3
            }

            // panning은 지도를 손가락으로 드래그하거나 화면에서 스와이프하여 지도 뷰를 이동시키는 동작을 의미합니다.
            override fun onPanningChanging(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                coordinate: FloatPoint
            ) {
                // 현재 시점을 3인칭으로 바꿈
                _userPOV.value = 3
                Log.v(TAG, "onPanningChanging called")
            }

            override fun onPanningEnded(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                coordinate: FloatPoint
            ) {
                Log.v(TAG, "onPanningEnded called")
            }

            override fun onPanningStarted(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                coordinate: FloatPoint
            ) {
                Log.v(TAG, "onPanningStarted called")
            }

            override fun onSingleTapped(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                coordinate: FloatPoint
            ) {
                val tmp = 3
            }

            override fun onTiltChanging(mapView: KNMapView?, screenPoint: IntPoint, tilt: Float) {
                val tmp = 3
            }

            override fun onTiltEnded(mapView: KNMapView?, screenPoint: IntPoint, tilt: Float) {
                val tmp = 3
            }

            override fun onTiltStarted(mapView: KNMapView?, screenPoint: IntPoint, tilt: Float) {
                val tmp = 3
            }

            override fun onZoomingChanging(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                zoom: Float
            ) {
                // 현재 시점을 3인칭으로 바꿈
                _userPOV.value = 3
                currentZoom = zoom
                Log.v(TAG, "onZoomingChanging called")
            }

            override fun onZoomingEnded(mapView: KNMapView?, screenPoint: IntPoint, zoom: Float) {
                val tmp = 3
            }

            override fun onZoomingStarted(mapView: KNMapView?, screenPoint: IntPoint, zoom: Float) {
                val tmp = 3
            }

        }
    }



    // 현재 사용자가 있는 위치에 카메라를 설정해주는 메소드
    private fun setCameraCurrentLocation() {
        Log.v(TAG, "recentGPS Data x : ${FindLoadApplication.knsdk.sharedGpsManager()?.recentGpsData?.pos?.x} y : ${FindLoadApplication.knsdk.sharedGpsManager()?.recentGpsData?.pos?.y}")
        Log.v(TAG, "lastValidGpsData Data x : ${FindLoadApplication.knsdk.sharedGpsManager()?.lastValidGpsData?.pos?.x} y : ${FindLoadApplication.knsdk.sharedGpsManager()?.lastValidGpsData?.pos?.y}")

        val x = FindLoadApplication.knsdk.sharedGpsManager()?.recentGpsData?.pos?.x ?: 0.0
        val y = FindLoadApplication.knsdk.sharedGpsManager()?.recentGpsData?.pos?.y ?: 0.0

        var currentLocWGS = KATECToWGS84(x, y)
        val currentLocKATEC = WGS84ToKATEC(currentLocWGS.x ,currentLocWGS.y)
        binding.mapView.moveCamera(KNMapCameraUpdate.targetTo(currentLocKATEC.toFloatPoint()).zoomTo(2.5f).tiltTo(0f), false, false)

    }

    private fun initMapView(mapView: KNMapView) {
        KNSDK.bindingMapView(mapView, mapView.mapTheme) { error ->
            if (error != null) {
                Toast.makeText(this, "맵 초기화 작업이 실패하였습니다. \n[${error.code}] : ${error.msg}",Toast.LENGTH_LONG).show()
                Log.v(TAG, "error.code : ${error.code}, error.msg : ${error.msg}")
                return@bindingMapView
            }

            setCameraCurrentLocation()
            requestRoute()
        }
    }

    // 사용자의 현재 위치를 찍어주는 마커인 TBT위치를 설정해준다.
    private fun setTBT(aGpsData : KNGPSData?) {
        var currentLocWGS = aGpsData?.pos?.let { KATECToWGS84(it.x, aGpsData.pos.y) }
        val currentLocKATEC = currentLocWGS?.let { WGS84ToKATEC(it.x ,currentLocWGS.y) }


        val bearing = aGpsData?.angle ?: 0
        rotate(bearing, aGpsData)
    }

    fun requestRoute() {

        val x = FindLoadApplication.knsdk.sharedGpsManager()?.recentGpsData?.pos?.x ?: 0.0
        val y = FindLoadApplication.knsdk.sharedGpsManager()?.recentGpsData?.pos?.y ?: 0.0
        var currentLocWGS = KATECToWGS84(x, y)

        // 예시 목적지는 서울역
        val goalKatec = WGS84ToKATEC(126.972263, 37.556091)
        val startKatec = WGS84ToKATEC(currentLocWGS.x, currentLocWGS.y)

        // 출발지와 목적지를 설정합니다.
        val startPoi = KNPOI("현위치", startKatec.x.toInt(),startKatec.y.toInt(),"현위치")
        val goalPoi = KNPOI("목적지",goalKatec.x.toInt(),goalKatec.y.toInt(),"목적지")

        FindLoadApplication.knsdk.makeTripWithStart(
            aStart = startPoi,
            aGoal = goalPoi,
            aVias = null
        ) { aError, aTrip ->

            // 경로 요청이 성공하면 aError는 Null이 됩니다.
            if (aError == null) {
                Log.v(TAG, "경로요청 성공")
                runOnUiThread {
                    val avoidOption = 0
                    val routeOption = KNRoutePriority.KNRoutePriority_Recommand
                    aTrip?.routeWithPriority(routeOption, avoidOption) { error, routes ->
                        // 경로 요청 실패
                        if (error != null) {
                            Log.v(TAG,"error is ${error.msg} code is ${error.code}")
                        }
                        // 경로 요청 성공
                        else {
                            Log.v(TAG,"routes is ${routes}")

                            if (routes != null) {
                                //binding.mapView.setRoutes(routes.toList())

                                //setTBT()

                                FindLoadApplication.knsdk.sharedGuidance()?.apply {
                                    // 가이던스의 상태를 나타내는 델리게이트로 주행 중 안내 상태가 변경될 때 호출됩니다. 모든 콜백은 메인스레드로 전달됩니다.
                                    guideStateDelegate = object : KNGuidance_GuideStateDelegate,
                                        KNGuidance_LocationGuideDelegate {
                                        // 경로의 변화를 감지합니다. 교통 변화, 경로 이탈로 인한 경로 재탐색이나 사용자가 경로를 재탐색할 때 호출됩니다.
                                        override fun guidanceCheckingRouteChange(aGuidance: KNGuidance) {
                                            Log.v(TAG, "guidanceCheckingRouteChange")

                                        }

                                        // todo : 얘는 뭐 공식문서에도 없고 뭐하는 애인지 모르겠음
                                        // 실내 경로(IndoorRoute) 정보가 변경되거나 업데이트될 때 호출되는 메소드라고 해석할 수 있다.
                                        override fun guidanceDidUpdateIndoorRoute(
                                            aGuidance: KNGuidance,
                                            aRoute: KNRoute?
                                        ) {
                                            Log.v(TAG, "guidanceDidUpdateIndoorRoute")

                                        }

                                        // 기존 경로가 변경됩니다.
                                        override fun guidanceDidUpdateRoutes(
                                            // 길 안내 기능 및 경로 정보
                                            aGuidance: KNGuidance,
                                            // 경로 리스트. 최대 2개의 경로를 전달할 수 있으며 순서대로 주 경로, 대안 경로로 구성됨(대안 경로 생략 가능)
                                            aRoutes: List<KNRoute>,
                                            // 대안 경로 정보(대안 경로 정보가 있는 경우)
                                            aMultiRouteInfo: KNMultiRouteInfo?
                                        ) {
                                            runOnUiThread {
                                                val avoidOption = 0
                                                val routeOption = KNRoutePriority.KNRoutePriority_Recommand
                                                aGuidance.trip?.routeWithPriority(routeOption, avoidOption, { error, routes ->

                                                    // 경로 요청 실패
                                                    if (error != null) {
                                                        Log.v(TAG,"error is ${error.msg} code is ${error.code}")
                                                    }
                                                    // 경로 요청 성공
                                                    else {
                                                        if (routes != null) {
                                                            binding.mapView.setRoutes(routes.toList())
                                                            binding.mapView.routeProperties?.theme = KNMapRouteTheme.driveDay()

                                                        }
                                                    }

                                                })
                                            }

                                            Log.v(TAG, "guidanceDidUpdateRoutes")
                                        }

                                        // 길 안내가 종료됩니다.
                                        override fun guidanceGuideEnded(aGuidance: KNGuidance) {
                                            Log.v(TAG, "guidanceGuideEnded")

                                        }

                                        // 길 안내를 시작합니다.
                                        override fun guidanceGuideStarted(aGuidance: KNGuidance) {
                                            Log.v(TAG, "guidanceGuideStarted")

                                        }

                                        // 기존 경로를 이탈합니다.
                                        override fun guidanceOutOfRoute(aGuidance: KNGuidance) {
                                            Log.v(TAG, "guidanceOutOfRoute")

                                        }

                                        // 경로 변경를 전달합니다. 여러 개의 경로 중 첫 번째 경로가 주행 경로이며 나머지는 대안 경로가 됩니다.
                                        override fun guidanceRouteChanged(
                                            aGuidance: KNGuidance,
                                            aFromRoute: KNRoute,
                                            aFromLocation: KNLocation,
                                            aToRoute: KNRoute,
                                            aToLocation: KNLocation,
                                            aChangeReason: KNGuideRouteChangeReason
                                        ) {
                                            Log.v(TAG, "guidanceRouteChanged")

                                        }

                                        // 기존 경로를 유지합니다. 교통 변화를 감지한 뒤 경로 변화가 없거나, 교통 상황의 변화로 요청한 새로운 경로가 기존의 경로와 동일할 경우 호출됩니다.
                                        override fun guidanceRouteUnchanged(aGuidance: KNGuidance) {
                                            Log.v(TAG, "guidanceRouteUnchanged")


                                        }

                                        // 경로 요청에 실패 시 에러 메시지를 반환합니다.
                                        override fun guidanceRouteUnchangedWithError(
                                            aGuidnace: KNGuidance,
                                            aError: KNError
                                        ) {


                                        }

                                        // 위치 정보를 업데이트합니다.
                                        override fun guidanceDidUpdateLocation(
                                            aGuidance: KNGuidance,
                                            aLocationGuide: KNGuide_Location
                                        ) {


                                            //Log.v(TAG, "guidanceDidUpdateLocation")

                                        }
                                    }

                                    // 위치를 나타내는 델리게이트로 주행 중 안내 위치가 변경될 때 호출됩니다. 모든 콜백은 메인스레드로 전달됩니다.
                                    locationGuideDelegate = object : KNGuidance_LocationGuideDelegate {
                                        // 위치 정보가 변경될 경우 호출됩니다. locationGuide의 항목이 1개 이상 변경 시 전달됩니다.
                                        override fun guidanceDidUpdateLocation(
                                            aGuidance: KNGuidance,
                                            aLocationGuide: KNGuide_Location
                                        ) {
                                            // todo : 여기는 항상 호출된다. withUI에서 모의주행을 실행하지 않아도 계속 호출되는 곳이다.
                                            Log.v(TAG, "guidanceDidUpdateLocation")

                                            if(aGuidance.locationGuide?.gpsOrigin?.valid == true) {
                                                // 현재 GPS정보가 수신되는중일떄
                                                userTBTIcon = BitmapFactory.decodeResource(resources, R.drawable.powered_navigation)
                                            } else {
                                                // 현재 GPS정보가 수신중이지 않을때
                                                userTBTIcon = BitmapFactory.decodeResource(resources, R.drawable.unpowered_navigation)
                                            }

                                            val roadName = aLocationGuide.location?.roadName ?: "nothing"
                                            viewModel.setCurrentRoadName(roadName)

                                            val roadType = aLocationGuide.location?.roadType?.let {
                                                viewModel.convertKNRoadType(it)
                                            } ?: "값이 없음"

                                            viewModel.setCurrentRoadType(roadType)

                                        }

                                    }

                                    // 경로를 안내하는 델리게이트로 주행 중 경로 안내 정보가 변경될 때 호출됩니다. 모든 콜백은 메인스레드로 전달됩니다.
                                    routeGuideDelegate = object : KNGuidance_RouteGuideDelegate,
                                        KNGuidance_LocationGuideDelegate {
                                        override fun guidanceDidUpdateRouteGuide(
                                            aGuidance: KNGuidance,
                                            aRouteGuide: KNGuide_Route
                                        ) {
                                            Log.v(TAG, "guidanceDidUpdateRouteGuide")

                                        }

                                        override fun guidanceDidUpdateLocation(
                                            aGuidance: KNGuidance,
                                            aLocationGuide: KNGuide_Location
                                        ) {
                                            //Log.v(TAG, "guidanceDidUpdateLocation")

                                        }

                                    }

                                    // 안전 운행 델리게이트로 안전 운행 정보가 변경될 때 호출됩니다. 모든 콜백은 메인스레드로 전달됩니다.
                                    safetyGuideDelegate = object : KNGuidance_SafetyGuideDelegate {
                                        override fun guidanceDidUpdateAroundSafeties(
                                            aGuidance: KNGuidance,
                                            aSafeties: List<KNSafety>?
                                        ) {
                                            Log.v(TAG, "guidanceDidUpdateAroundSafeties")

                                        }

                                        // 안전 운행 정보를 업데이트합니다. safetyGuide의 항목이 1개 이상 변경 시 전달됩니다. safetyGuide의 세부 항목 중 변경이 없는 항목은 이전과 동일한 객체로 전달됩니다.
                                        override fun guidanceDidUpdateSafetyGuide(
                                            aGuidance: KNGuidance,
                                            aSafetyGuide: KNGuide_Safety?
                                        ) {
                                            Log.v(TAG, "guidanceDidUpdateSafetyGuide")

                                            aSafetyGuide?.safetiesOnGuide?.map { knSafety: KNSafety ->
                                                val value = viewModel.convertSafetyCode(knSafety.code.name)
                                                viewModel.setCurrentSafetyCode(value)

                                                val trafficSpd = knSafety.location.trafficSpd
                                                viewModel.setCurrentTrafficSpd(trafficSpd)
                                            }




                                        }

                                    }

                                    // 음성 안내 델리게이트로 음성 안내 시작 및 종료와 관련된 정보를 요청 및 전달합니다. 모든 콜백은 메인스레드로 전달됩니다.
                                    voiceGuideDelegate = object : KNGuidance_VoiceGuideDelegate {
                                        // 음성 안내를 종료합니다.
                                        override fun didFinishPlayVoiceGuide(
                                            aGuidance: KNGuidance,
                                            aVoiceGuide: KNGuide_Voice
                                        ) {
                                            Log.v(TAG, "didFinishPlayVoiceGuide")

                                        }

                                        // 음성 안내 사용 여부를 설정합니다. (true: 음성 안내 사용 / false: 음성 안내 사용 안 함)
                                        override fun shouldPlayVoiceGuide(
                                            aGuidance: KNGuidance,
                                            aVoiceGuide: KNGuide_Voice,
                                            aNewData: MutableList<ByteArray>
                                        ): Boolean {
                                            Log.v(TAG, "shouldPlayVoiceGuide")

                                            return true
                                        }

                                        // 음성 안내를 시작합니다.
                                        override fun willPlayVoiceGuide(
                                            aGuidance: KNGuidance,
                                            aVoiceGuide: KNGuide_Voice
                                        ) {
                                            Log.v(TAG, "willPlayVoiceGuide")

                                        }
                                    }

                                    // C-ITS(Cooperative-Intelligent Transport Systems, 협력 지능형 교통 체계)의 정보가 변경되면 호출됩니다. 모든 콜백은 메인 스레드로 전달됩니다.
                                    citsGuideDelegate = object : KNGuidance_CitsGuideDelegate {
                                        // C-ITS가 업데이트 될 경우 호출됩니다. citsGuide의 항목이 1개 이상 변경 시 전달됩니다. citsGuide의 세부 항목 중 변경이 없는 항목은 이전과 동일한 객체로 전달됩니다.
                                        override fun didUpdateCitsGuide(
                                            aGuidance: KNGuidance,
                                            aCitsGuide: KNGuide_Cits
                                        ) {
                                            Log.v(TAG, "didUpdateCitsGuide")

                                        }

                                    }

                                }

                                setCurrentTBT()

                                FindLoadApplication.knsdk.sharedGuidance()?.startWithTrip(aTrip, KNRoutePriority.KNRoutePriority_Recommand, KNRouteAvoidOption.KNRouteAvoidOption_None.value)
                            }

                        }

                    }
                }
            } else {
                Log.v(TAG, "aError msg: ${aError.msg} code : ${aError.code}")
            }
        }
    }

    // 입력받은 사용자의 위치 정보를 통해 사용자가 지나간 경로 부분을 자릅니다.
    // https://developers.kakaomobility.com/docs/android-ref-kotlin/class-KNMapView/
    fun cullingRouteWithMapView() {
        FindLoadApplication.knsdk.sharedGuidance()?.locationGuide?.location?.let {
            binding.mapView.cullPassedRoute(
                it, true)
        }

    }

    private fun setCurrentTBT() {
        FindLoadApplication.knsdk.requestLocationUpdate(delegate = object : KNGPSReceiver {
            override fun didReceiveGpsData(aGpsData: KNGPSData)     {
                cullingRouteWithMapView()

                _currentSpeed.value = aGpsData.speed.toString()

                binding.mapView.routeProperties?.theme = KNMapRouteTheme.trafficDay()
                if(userPOV.value == 3) {
                    // 현재 사용자가 3인칭 시점인 경우
                    val matchedGPS = FindLoadApplication.knsdk.sharedGuidance()?.locationGuide?.gpsMatched
                    if (matchedGPS != null) {
                        matchedGPS?.angle?.toFloat()?.let {
                            binding.mapView.userLocation?.animate(
                                // 이동할 위치의 카텍 좌표
                                coordinate = matchedGPS.pos,
                                angle = it,
                                duration = 500,
                                useNorthHeadingMode = false
                            )
                        }
                    }

                    Log.v(TAG, "bearing ${binding.mapView.bearing}")
                } else if(userPOV.value == 1) {
                    // 현재 사용자가 1인칭 시점인 경우
                    var currentLocWGS = KATECToWGS84(aGpsData.pos.x, aGpsData.pos.y)
                    Log.v(TAG, "aGpsData.angle.toFloat() ${aGpsData.angle.toFloat()}")
                    setTBT(FindLoadApplication.knsdk.sharedGuidance()?.locationGuide?.gpsMatched)
                }
            }
        })
    }

    private fun rotate(bearing : Int, aGpsData : KNGPSData?) {
        var currentLocWGS = aGpsData?.pos?.let { KATECToWGS84(it.x, aGpsData.pos.y) }
        val currentLocKATEC = currentLocWGS?.let { WGS84ToKATEC(it.x ,currentLocWGS.y) }
        //Log.v(TAG, "rotated bearing is ${bearing.toString()}")
        if (currentLocKATEC != null) {
            //binding.mapView.animateCamera(KNMapCameraUpdate.bearingTo(bearing.toFloat()), 500, true, false)
            binding.mapView.animateCamera(KNMapCameraUpdate.bearingTo(bearing.toFloat()).targetTo(currentLocKATEC.toFloatPoint()), 500, true, false)
        }
    }

    fun copyRawResourceToInternalStorage(context: Context, rawResId: Int, fileName: String) {
        val inputStream = context.resources.openRawResource(rawResId) // raw 리소스 읽기
        val outFile = File(context.filesDir, fileName) // 내부 저장소에 저장
        val outputStream = FileOutputStream(outFile)

        inputStream.copyTo(outputStream) // 데이터 복사
        inputStream.close()
        outputStream.close()
    }

}