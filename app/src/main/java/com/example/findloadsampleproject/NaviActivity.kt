package com.example.findloadsampleproject

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.findloadsampleproject.databinding.ActivityNaviBinding

import com.kakaomobility.knsdk.KNRouteAvoidOption
import com.kakaomobility.knsdk.KNRoutePriority
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.common.gps.KATECToWGS84
import com.kakaomobility.knsdk.common.gps.KNGPSData
import com.kakaomobility.knsdk.common.gps.KNGPSReceiver
import com.kakaomobility.knsdk.common.gps.KNLocationReceiver
import com.kakaomobility.knsdk.common.gps.WGS84ToKATEC
import com.kakaomobility.knsdk.common.objects.KNPOI
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_LocationGuideDelegate
//import com.kakaomobility.knsdk.guidance.knguidance.KNDriveGuidance
import com.kakaomobility.knsdk.guidance.knguidance.common.KNLocation
import com.kakaomobility.knsdk.guidance.knguidance.locationguide.KNGuide_Location
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.objects.KNRoadInfo
import com.kakaomobility.knsdk.map.knmaprenderer.objects.KNMapCameraUpdate
import com.kakaomobility.knsdk.map.knmapview.KNMapView

class NaviActivity : AppCompatActivity()
    //, KNGuidance_LocationGuideDelegate, KNGuidance_RouteGuideDelegate
{
    val TAG = "NaviActivity"
    private lateinit var binding : ActivityNaviBinding
    //private lateinit var guidance: KNDriveGuidance
    //lateinit var mapView: KNMapView

    var flag = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNaviBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initMapView(binding.mapView)
    }


    // 사용자가 처음 지도를 볼떄 지도의 위치를 설정해주는 메소드
    private fun initSite() {
        Log.v(TAG, "recentGPS Data x : ${FindLoadApplication.knsdk.sharedGpsManager()?.recentGpsData?.pos?.x} y : ${FindLoadApplication.knsdk.sharedGpsManager()?.recentGpsData?.pos?.y}")
        Log.v(TAG, "lastValidGpsData Data x : ${FindLoadApplication.knsdk.sharedGpsManager()?.lastValidGpsData?.pos?.x} y : ${FindLoadApplication.knsdk.sharedGpsManager()?.lastValidGpsData?.pos?.y}")

        val x = FindLoadApplication.knsdk.sharedGpsManager()?.recentGpsData?.pos?.x ?: 0.0
        val y = FindLoadApplication.knsdk.sharedGpsManager()?.recentGpsData?.pos?.y ?: 0.0

        // todo : 정말 KNSDK에서 이해가 안되는 부분이다. 최근위치 좌표를 WGS84 -> KATEC 으로 변환시키고 사용해야 한다.
        var currentLocWGS = KATECToWGS84(x, y)
        val currentLocKATEC = WGS84ToKATEC(currentLocWGS.x ,currentLocWGS.y)
        binding.mapView.moveCamera(KNMapCameraUpdate.targetTo(currentLocKATEC.toFloatPoint()).zoomTo(2.5f).tiltTo(0f), false, false)

        requestRoute()

        //setTBT()
    }

    private fun initMapView(mapView: KNMapView) {
        KNSDK.bindingMapView(mapView, mapView.mapTheme) { error ->
            if (error != null) {
                Toast.makeText(this, "맵 초기화 작업이 실패하였습니다. \n[${error.code}] : ${error.msg}",Toast.LENGTH_LONG).show()
                Log.v(TAG, "error.code : ${error.code}, error.msg : ${error.msg}")
                return@bindingMapView
            }

            initSite()
        }
    }

    // 사용자의 현재 위치를 찍어주는 마커인 TBT위치를 설정해준다.
    private fun setTBT() {
        FindLoadApplication.knsdk.requestLocationUpdate(delegate = object : KNGPSReceiver {
            override fun didReceiveGpsData(aGpsData: KNGPSData) {
                flag = false
                aGpsData
                var currentLocWGS = KATECToWGS84(aGpsData.pos.x, aGpsData.pos.y)
                val currentLocKATEC = WGS84ToKATEC(currentLocWGS.x ,currentLocWGS.y)

                //Log.v(TAG, "currentLocKATEC is x ${currentLocKATEC.x} y ${currentLocKATEC.y}")
                binding.mapView.userLocation?.apply {
                    isVisible = true
                    isVisibleGuideLine = true
                    coordinate = currentLocKATEC.toFloatPoint()
                }

                cullingRouteWithMapView()

                // 현재 위치를 토스트메세지로 확인
                //Toast.makeText(this@NaviActivity, "x ${currentLocKATEC.x} \ny ${currentLocKATEC.y}",Toast.LENGTH_LONG).show()
            }
        })
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

                                FindLoadApplication.knsdk.sharedGuidance()?.startWithTrip(aTrip, KNRoutePriority.KNRoutePriority_Time, KNRouteAvoidOption.KNRouteAvoidOption_RoadEvent.value)

                            }

                        }

                    }
                }
            }
        }
    }


    fun requestRoute3() {
        val goalKatec = WGS84ToKATEC(126.972263, 37.556091)
        val startKatec = WGS84ToKATEC(127.102308, 37.334522)

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
//                    binding.naviView.guidance = FindLoadApplication.knsdk.sharedGuidance()!!
                    FindLoadApplication.knsdk.sharedGuidance()?.apply {
//                        guideStateDelegate = this@NaviActivity
                        //locationGuideDelegate = this@NaviActivity
                        //routeGuideDelegate = this@NaviActivity
//                        safetyGuideDelegate = this@NaviActivity
//                        citsGuideDelegate = this@NaviActivity
//                        voiceGuideDelegate = this@NaviActivity
                        //locationGuide = FindLoadApplication.knsdk.sharedGuidance()?.locationGuide
                        //trip = aTrip


                        //binding.naviView.bottomComponent.visibility = View.GONE

                        // initWithGuidance 안에서 내부적으로 startWithTrip 메소드가 호출된다고 한다.
                        //with UI 를 활용해서 시작하는 방법




                        //FindLoadApplication.knsdk.sharedGuidance()?.startWithTrip(aTrip, KNRoutePriority.KNRoutePriority_Time, KNRouteAvoidOption.KNRouteAvoidOption_RoadEvent.value)
                    }
                }
            }
        }
    }
}