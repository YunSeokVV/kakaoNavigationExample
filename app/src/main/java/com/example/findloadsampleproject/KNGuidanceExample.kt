package com.example.findloadsampleproject

import android.app.Application
import android.util.Log
import com.kakaomobility.knsdk.common.gps.KNGPSData
import com.kakaomobility.knsdk.common.gps.KNGPSReceiver
import com.kakaomobility.knsdk.guidance.knlteuploader.KNLTEReceiver

class KNGuidanceExample(private val application: Application) : KNGPSReceiver, KNLTEReceiver {
    val TAG = "KNGuidance"

    override fun didReceiveGpsData(aGpsData: KNGPSData) {
        Log.v(TAG, "aGpsData ${aGpsData}")
        Log.v(TAG, "didReceiveGpsData called")

    }

    override fun didReceiveLteData(aGpsData: KNGPSData) {
        Log.v(TAG, "aGpsData ${aGpsData}")
        Log.v(TAG, "didReceiveLteData called")
    }

//    fun tmp() {
//        viewModel.coordZipResult.collectLatest {
//            Log.d(NAVI_ROTATION, "좌표 변환 결과: $it")
//            if (it.success == null) return@collectLatest
//
//            val katechStartX = it.success.startLongitude!!.split(".")[0].toInt()
//            val katechStartY = it.success.startLatitude!!.split(".")[0].toInt()
//            val katechEndX = it.success.endLongitude!!.split(".")[0].toInt()
//            val katechEndY = it.success.endLatitude!!.split(".")[0].toInt()
//
//            // 출발지 설정
//            val start = KNPOI("", katechStartX, katechStartY, null)
//
//            // 목적지 설정
//            val end = KNPOI("", katechEndX, katechEndY, null)
//
//            // KNTrip 생성
//            KNSDK.makeTripWithStart(
//                start,
//                end,
//                null,
//                null,
//                aCompletion = { knError: KNError?, knTrip: KNTrip? ->
//                    if (knError != null) {
//                        Log.d(NAVI_ROTATION, "경로 생성 에러(KNError: $knError")
//                    }
//
//                    /**
//                     * 경로 옵션 설정
//                     * 1. KNRoutePriority : 목적지까지의 경로 안내 옵션 중 우선적으로 고려할 항목 설정
//                     * 2. KNRouteAvoidOption : 경로에서 회피하고 싶은 구간 설정
//                     */
//                    val curRoutePriority = KNRoutePriority.valueOf(KNRoutePriority.KNRoutePriority_Recommand.toString())
//                    val curAvoidOptions = KNRouteAvoidOption.KNRouteAvoidOption_None.value
//
//                    knTrip?.routeWithPriority(curRoutePriority, curAvoidOptions) { error, _ ->
//                        // 경로 요청 실패
//                        if (error != null) {
//                            Log.d(NAVI_ROTATION, "경로 요청 실패 : $error")
//                        }
//                        // 경로 요청 성공
//                        else {
//                            Log.d(NAVI_ROTATION, "경로 요청 성공")
//                            KNSDK.sharedGuidance()?.apply {
//                                // 각 가이던스 델리게이트 등록
//                                guideStateDelegate = this@NavigationActivity
//                                locationGuideDelegate = this@NavigationActivity
//                                routeGuideDelegate = this@NavigationActivity
//                                safetyGuideDelegate = this@NavigationActivity
//                                voiceGuideDelegate = this@NavigationActivity
//                                citsGuideDelegate = this@NavigationActivity
//
//                                settingMap()
//
//                                // 기본 주행 화면을 통해 길 안내 시작
//                                binding.naviView.initWithGuidance(
//                                    this,
//                                    knTrip,
//                                    curRoutePriority,
//                                    curAvoidOptions
//                                )
//                            }
//                        }
//                    }
//                })
//        }
//    }

}