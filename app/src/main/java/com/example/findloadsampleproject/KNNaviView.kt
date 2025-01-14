package com.example.findloadsampleproject

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.findloadsampleproject.databinding.ActivityKnnaviViewBinding
import com.example.findloadsampleproject.databinding.ActivityNaviBinding
import com.kakaomobility.knsdk.KNCarFuel
import com.kakaomobility.knsdk.KNCarType
import com.kakaomobility.knsdk.KNCarUsage
import com.kakaomobility.knsdk.KNDisplayType
import com.kakaomobility.knsdk.KNRouteAvoidOption
import com.kakaomobility.knsdk.KNRoutePriority
import com.kakaomobility.knsdk.common.gps.KATECToWGS84
import com.kakaomobility.knsdk.common.gps.WGS84ToKATEC
import com.kakaomobility.knsdk.common.objects.KNError
import com.kakaomobility.knsdk.common.objects.KNPOI
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_CitsGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_GuideStateDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_LocationGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_RouteGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_SafetyGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_VoiceGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuideRouteChangeReason
import com.kakaomobility.knsdk.guidance.knguidance.citsguide.KNGuide_Cits
import com.kakaomobility.knsdk.guidance.knguidance.common.KNLocation
import com.kakaomobility.knsdk.guidance.knguidance.locationguide.KNGuide_Location
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.KNGuide_Route
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.objects.KNMultiRouteInfo
import com.kakaomobility.knsdk.guidance.knguidance.safetyguide.KNGuide_Safety
import com.kakaomobility.knsdk.guidance.knguidance.safetyguide.objects.KNSafety
import com.kakaomobility.knsdk.guidance.knguidance.voiceguide.KNGuide_Voice
import com.kakaomobility.knsdk.trip.knrouteconfiguration.KNRouteConfiguration
import com.kakaomobility.knsdk.trip.kntrip.KNTrip
import com.kakaomobility.knsdk.trip.kntrip.knroute.KNRoute
import com.kakaomobility.knsdk.ui.view.KNNaviViewState

class KNNaviView : AppCompatActivity(),
    KNGuidance_GuideStateDelegate,
    KNGuidance_LocationGuideDelegate,
    KNGuidance_RouteGuideDelegate,
    KNGuidance_SafetyGuideDelegate,
    KNGuidance_VoiceGuideDelegate,
    KNGuidance_CitsGuideDelegate {
    val TAG = "KNNaviView"
    private lateinit var binding : ActivityKnnaviViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKnnaviViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        requestRoute()

        binding.mockPlay.setOnClickListener {
            startMockDrive()
        }

    }

    fun requestRoute() {
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
                                startGuide(aTrip)
                            }

                        }

                    }
                }
            }
        }
    }

    fun startGuide(trip: KNTrip?) {
        FindLoadApplication.knsdk.sharedGuidance()?.apply {
            // guidance delegate 등록
            guideStateDelegate = this@KNNaviView        // 가이던스의 상태를 나타내는 델리게이트로 주행 중 안내 상태가 변경될 때 호출.
            locationGuideDelegate = this@KNNaviView     // 위치를 나타내는 델리게이트로 주행 중 안내 위치가 변경될 때 호출.
            routeGuideDelegate = this@KNNaviView        // 경로를 안내하는 델리게이트로 주행 중 경로 안내 정보가 변경될 때 호출
            safetyGuideDelegate = this@KNNaviView       // 안전 운행 델리게이트로 안전 운행 정보가 변경될 때 호출
            voiceGuideDelegate = this@KNNaviView
            citsGuideDelegate = this@KNNaviView         // C-ITS(Cooperative-Intelligent Transport Systems, 협력 지능형 교통 체계)의 정보가 변경되면 호출

            binding.naviView.initWithGuidance(
                this,
                trip,
                KNRoutePriority.KNRoutePriority_Recommand,
                0
            )

            binding.naviView.changeDisplayType(KNDisplayType.KNDisplayType_SEARCH)

        }
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


    override fun guidanceCheckingRouteChange(aGuidance: KNGuidance) {
        binding.naviView.guidanceCheckingRouteChange(aGuidance)
    }

    override fun guidanceDidUpdateIndoorRoute(aGuidance: KNGuidance, aRoute: KNRoute?) {
        binding.naviView.guidanceDidUpdateIndoorRoute(aGuidance, aRoute)
    }

    override fun guidanceDidUpdateRoutes(
        aGuidance: KNGuidance,
        aRoutes: List<KNRoute>,
        aMultiRouteInfo: KNMultiRouteInfo?
    ) {
        binding.naviView.guidanceDidUpdateRoutes(aGuidance, aRoutes, aMultiRouteInfo)
    }


    override fun guidanceGuideEnded(aGuidance: KNGuidance) {
        binding.naviView.guidanceGuideEnded(aGuidance)
    }

    override fun guidanceGuideStarted(aGuidance: KNGuidance) {
        binding.naviView.guidanceGuideStarted(aGuidance)
    }

    override fun guidanceOutOfRoute(aGuidance: KNGuidance) {
        binding.naviView.guidanceOutOfRoute(aGuidance)
    }

    override fun guidanceRouteChanged(
        aGuidance: KNGuidance,
        aFromRoute: KNRoute,
        aFromLocation: KNLocation,
        aToRoute: KNRoute,
        aToLocation: KNLocation,
        aChangeReason: KNGuideRouteChangeReason
    ) {
        binding.naviView.guidanceRouteChanged(aGuidance)
    }

    override fun guidanceRouteUnchanged(aGuidance: KNGuidance) {
        binding.naviView.guidanceRouteUnchanged(aGuidance)
    }

    override fun guidanceRouteUnchangedWithError(aGuidnace: KNGuidance, aError: KNError) {
        binding.naviView.guidanceRouteUnchangedWithError(aGuidnace, aError)
    }

    override fun guidanceDidUpdateLocation(
        aGuidance: KNGuidance,
        aLocationGuide: KNGuide_Location
    ) {
        val bearing = aLocationGuide.gpsOrigin.angle
        Toast.makeText(this, "${bearing.toString()}", Toast.LENGTH_LONG).show()
        Log.v(TAG, "current bearing is ${bearing}")
        binding.naviView.guidanceDidUpdateLocation(aGuidance, aLocationGuide)
    }

    override fun guidanceDidUpdateRouteGuide(aGuidance: KNGuidance, aRouteGuide: KNGuide_Route) {
        binding.naviView.guidanceDidUpdateRouteGuide(aGuidance, aRouteGuide)
    }

    override fun guidanceDidUpdateAroundSafeties(
        aGuidance: KNGuidance,
        aSafeties: List<KNSafety>?
    ) {
        binding.naviView.guidanceDidUpdateAroundSafeties(aGuidance, aSafeties)
    }

    override fun guidanceDidUpdateSafetyGuide(
        aGuidance: KNGuidance,
        aSafetyGuide: KNGuide_Safety?
    ) {
        binding.naviView.guidanceDidUpdateSafetyGuide(aGuidance, aSafetyGuide)
    }

    override fun didFinishPlayVoiceGuide(aGuidance: KNGuidance, aVoiceGuide: KNGuide_Voice) {
        binding.naviView.didFinishPlayVoiceGuide(aGuidance, aVoiceGuide)
    }

    override fun shouldPlayVoiceGuide(
        aGuidance: KNGuidance,
        aVoiceGuide: KNGuide_Voice,
        aNewData: MutableList<ByteArray>
    ): Boolean {
        return binding.naviView.shouldPlayVoiceGuide(aGuidance, aVoiceGuide, aNewData)
    }

    override fun willPlayVoiceGuide(aGuidance: KNGuidance, aVoiceGuide: KNGuide_Voice) {
        binding.naviView.willPlayVoiceGuide(aGuidance, aVoiceGuide)
    }

    override fun didUpdateCitsGuide(aGuidance: KNGuidance, aCitsGuide: KNGuide_Cits) {
        binding.naviView.didUpdateCitsGuide(aGuidance, aCitsGuide)
    }

}