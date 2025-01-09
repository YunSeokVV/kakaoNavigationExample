package com.example.findloadsampleproject

import android.location.Location
import android.os.Bundle
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
import com.kakaomobility.knsdk.common.util.FloatPoint
import com.kakaomobility.knsdk.common.util.IntPoint
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_GuideStateDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_LocationGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_RouteGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuideRouteChangeReason
//import com.kakaomobility.knsdk.guidance.knguidance.KNDriveGuidance
import com.kakaomobility.knsdk.guidance.knguidance.common.KNLocation
import com.kakaomobility.knsdk.guidance.knguidance.locationguide.KNGuide_Location
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.KNGuide_Route
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.objects.KNMultiRouteInfo
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.objects.KNRoadInfo
import com.kakaomobility.knsdk.guidance.knguidance.voiceguide.KNVoiceCode
import com.kakaomobility.knsdk.guidance.knguidance.voiceguide.KNVoiceDist
import com.kakaomobility.knsdk.map.knmaprenderer.objects.KNMapCameraUpdate
import com.kakaomobility.knsdk.map.knmapview.KNMapView
import com.kakaomobility.knsdk.map.knmapview.idl.KNMapViewEventListener
import com.kakaomobility.knsdk.trip.knrouteconfiguration.KNRouteConfiguration
import com.kakaomobility.knsdk.trip.kntrip.knroute.KNRoute

class NaviActivity : AppCompatActivity()
    //, KNGuidance_LocationGuideDelegate, KNGuidance_RouteGuideDelegate
{
    // 현재 사용자의 시점이 1인칭인지 3인칭인지 판별해주는 변수
    //var userPOV = 3

    var userPOV = MutableLiveData<Int>(1)

    val TAG = "NaviActivity"
    private lateinit var binding : ActivityNaviBinding
    //private lateinit var guidance: KNDriveGuidance
    //lateinit var mapView: KNMapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNaviBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initMapView(binding.mapView)

        binding.goCurrentLocation.setOnClickListener {

            setCameraCurrentLocation()
            userPOV.value = 1
            it.visibility = View.GONE
        }

        binding.mockPlay.setOnClickListener {
            startMockDrive()
        }

        userPOV.observe(this) { pov ->
            if(pov == 3) {
                binding.userPOV.setText("3인칭")
            } else if(pov == 1) {
                binding.userPOV.setText("1인칭")

            }

        }

    }


    // 현재 사용자가 있는 위치에 카메라를 설정해주는 메소드
    private fun setCameraCurrentLocation() {
        Log.v(TAG, "recentGPS Data x : ${FindLoadApplication.knsdk.sharedGpsManager()?.recentGpsData?.pos?.x} y : ${FindLoadApplication.knsdk.sharedGpsManager()?.recentGpsData?.pos?.y}")
        Log.v(TAG, "lastValidGpsData Data x : ${FindLoadApplication.knsdk.sharedGpsManager()?.lastValidGpsData?.pos?.x} y : ${FindLoadApplication.knsdk.sharedGpsManager()?.lastValidGpsData?.pos?.y}")

        val x = FindLoadApplication.knsdk.sharedGpsManager()?.recentGpsData?.pos?.x ?: 0.0
        val y = FindLoadApplication.knsdk.sharedGpsManager()?.recentGpsData?.pos?.y ?: 0.0

        // todo : 정말 KNSDK에서 이해가 안되는 부분이다. 최근위치 좌표를 WGS84 -> KATEC 으로 변환시키고 사용해야 한다.
        var currentLocWGS = KATECToWGS84(x, y)
        val currentLocKATEC = WGS84ToKATEC(currentLocWGS.x ,currentLocWGS.y)
        //val currentLocKATEC = WGS84ToKATEC(127.109685651985,37.39385204911604)
        binding.mapView.moveCamera(KNMapCameraUpdate.targetTo(currentLocKATEC.toFloatPoint()).zoomTo(2.5f).tiltTo(0f), false, false)

        //setTBT()
    }

    private fun mapViewEventListener() {
        binding.mapView.mapViewEventListener = object : KNMapViewEventListener {
            // 사용하지 않는 추상메소드에서도 구현을 해놓지 않으면 앱이 죽기 때문에 무의미한 코드를 넣었다.
            override fun onBearingChanging(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                bearing: Float
            ) {
                val a = 3
            }

            override fun onBearingEnded(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                bearing: Float
            ) {
                val a = 3
            }

            override fun onBearingStarted(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                bearing: Float
            ) {
                val a = 3
            }

            override fun onCameraAnimationCanceled(
                mapView: KNMapView?,
                cameraUpdate: KNMapCameraUpdate?
            ) {
                val a = 3
            }

            override fun onCameraAnimationEnded(
                mapView: KNMapView?,
                cameraUpdate: KNMapCameraUpdate?
            ) {
                val a = 3
            }

            override fun onDoubleTapped(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                coordinate: FloatPoint
            ) {
                val a = 3
            }

            override fun onLongPressed(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                coordinate: FloatPoint
            ) {
                val a = 3
            }

            override fun onPanningChanging(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                coordinate: FloatPoint
            ) {
                Log.v(TAG, "onPanningChanging called")
                binding.goCurrentLocation.visibility = View.VISIBLE
                userPOV.value = 3
            }

            override fun onPanningEnded(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                coordinate: FloatPoint
            ) {
                val a = 3
            }

            override fun onPanningStarted(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                coordinate: FloatPoint
            ) {
                val a = 3
            }

            override fun onSingleTapped(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                coordinate: FloatPoint
            ) {
                val a = 3
            }

            override fun onTiltChanging(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                tilt: Float
            ) {
                val a = 3
            }

            override fun onTiltEnded(mapView: KNMapView?, screenPoint: IntPoint, tilt: Float) {
                val a = 3
            }

            override fun onTiltStarted(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                tilt: Float
            ) {
                val a = 3
            }

            override fun onZoomingChanging(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                zoom: Float
            ) {
                binding.goCurrentLocation.visibility = View.VISIBLE
                userPOV.value = 3
                Log.v(TAG, "zoom : ${zoom}")
            }

            override fun onZoomingEnded(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                zoom: Float
            ) {
                val a = 3
            }

            override fun onZoomingStarted(
                mapView: KNMapView?,
                screenPoint: IntPoint,
                zoom: Float
            ) {
                val a = 3
            }

        }
    }

    private fun initMapView(mapView: KNMapView) {
        KNSDK.bindingMapView(mapView, mapView.mapTheme) { error ->
            if (error != null) {
                Toast.makeText(this, "맵 초기화 작업이 실패하였습니다. \n[${error.code}] : ${error.msg}",Toast.LENGTH_LONG).show()
                Log.v(TAG, "error.code : ${error.code}, error.msg : ${error.msg}")
                return@bindingMapView
            }

            mapViewEventListener()
            //bearing45WithMoveMap(false, false)
            setCameraCurrentLocation()
            //move()
            requestRoute()
        }
    }

    // 사용자의 현재 위치를 찍어주는 마커인 TBT위치를 설정해준다.
    private fun setTBT() {

        FindLoadApplication.knsdk.requestLocationUpdate(delegate = object : KNGPSReceiver {
            override fun didReceiveGpsData(aGpsData: KNGPSData) {
                if(userPOV.value == 3) {
                    // 현재 사용자가 3인칭 시점인 경우
                    var currentLocWGS = KATECToWGS84(aGpsData.pos.x, aGpsData.pos.y)
                    val currentLocKATEC = WGS84ToKATEC(currentLocWGS.x ,currentLocWGS.y)

                    //Log.v(TAG, "currentLocKATEC is x ${currentLocKATEC.x} y ${currentLocKATEC.y}")
                    binding.mapView.userLocation?.apply {
                        isVisible = true
                        isVisibleGuideLine = true
                        coordinate = currentLocKATEC.toFloatPoint()
                        angle = aGpsData.angle.toFloat()
                    }

                    cullingRouteWithMapView()

                    Log.v(TAG, "bearing ${binding.mapView.bearing}")
                } else if(userPOV.value == 1) {
                    // 현재 사용자가 1인칭 시점인 경우
                    var currentLocWGS = KATECToWGS84(aGpsData.pos.x, aGpsData.pos.y)
                    val currentLocKATEC = WGS84ToKATEC(currentLocWGS.x ,currentLocWGS.y)

                    //Log.v(TAG, "currentLocKATEC is x ${currentLocKATEC.x} y ${currentLocKATEC.y}")
                    binding.mapView.userLocation?.apply {
                        isVisible = true
                        isVisibleGuideLine = true
                        coordinate = currentLocKATEC.toFloatPoint()
                        angle = 0F
                    }

                    Log.v(TAG, "binding.mapView.bearing ${binding.mapView.bearing}")
                    //temp(binding.mapView.bearing.toString())
                    binding.mapView.moveCamera(KNMapCameraUpdate.targetTo(currentLocKATEC.toFloatPoint()).zoomTo(2.5f).tiltTo(0f), true, false)
                    //rotate(binding.mapView.bearing)

                    cullingRouteWithMapView()

                    //check point
                    //FindLoadApplication.knsdk.requestLocationUpdate()



                }



            }
        })
    }


    fun temp(data : String) {
        Toast.makeText(this, data,Toast.LENGTH_LONG).show()
    }

    // 입력받은 사용자의 위치 정보를 통해 사용자가 지나간 경로 부분을 자릅니다.
    // https://developers.kakaomobility.com/docs/android-ref-kotlin/class-KNMapView/
    fun cullingRouteWithMapView() {
        FindLoadApplication.knsdk.sharedGuidance()?.locationGuideDelegate = object : KNGuidance_LocationGuideDelegate {
            override fun guidanceDidUpdateLocation(
                aGuidance: KNGuidance,
                aLocationGuide: KNGuide_Location
            ) {
                aLocationGuide.location?.let { binding.mapView.cullPassedRoute(it, true) }
            }
        }
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
                                binding.mapView.setRoutes(routes.toList())
                                setTBT()
                                FindLoadApplication.knsdk.sharedGuidance()?.startWithTrip(aTrip, KNRoutePriority.KNRoutePriority_Time, KNRouteAvoidOption.KNRouteAvoidOption_None.value)

                            }

                        }

                    }
                }
            } else {
                Log.v(TAG, "aError msg: ${aError.msg} code : ${aError.code}")
            }
        }
    }

    fun bearingWithKNMapCameraUpdate(bearing:Float): KNMapCameraUpdate {
        return KNMapCameraUpdate.bearingTo(bearing)
    }
    fun bearing45WithMoveMap() {
        runOnUiThread {
            val coordinate = WGS84ToKATEC(127.10990647707422,37.39371215608904)
            val bearing = binding.mapView.bearing + 45f // 회전 각도를 45도로 설정
            binding.mapView.animateCamera(bearingWithKNMapCameraUpdate(bearing).targetTo(coordinate.toFloatPoint()), 500L, true, false)
        }
    }

    private fun rotate(bearing : Float) {
        //bearing45WithMoveMap()
        binding.mapView.animateCamera(KNMapCameraUpdate.bearingTo(binding.mapView.bearing + bearing), 500, true, false)
    }

    private fun move() {
        val coordinate = WGS84ToKATEC(127.10990647707422,37.39371215608904)
        //binding.mapView.animateCamera(KNMapCameraUpdate.targetTo(coordinate.toFloatPoint())), 500, true)
        binding.mapView.animateCamera(
            cameraUpdate = KNMapCameraUpdate().targetTo(coordinate.toFloatPoint()),
            duration = 500,
            withUserLocation = true,
            useNorthHeadingMode = true
        )
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