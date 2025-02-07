package com.example.findloadsampleproject

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kakaomobility.knsdk.KNRoadType
import com.kakaomobility.knsdk.common.gps.WGS84ToKATEC
import com.kakaomobility.knsdk.common.objects.KNPOI
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NaviViewModel @Inject constructor() : ViewModel() {
    // 현재 안전운행 데이터 코드. 쉽게 말해서 경로 안내 시 표시되는 정보를 의미함.
    private var _currentSafetyCode = MutableLiveData<String>()
    val currentSafetyCode : LiveData<String> = _currentSafetyCode

    private var _currentTrafficSpd = MutableLiveData<Int>()
    val currentTrafficSpd : LiveData<Int> = _currentTrafficSpd

    private var _roadName = MutableLiveData<String>()
    val roadName : LiveData<String> = _roadName

    private var _roadType = MutableLiveData<String>()
    val roadType : LiveData<String> = _roadType

    // 현재 사용자의 시점이 1인칭인지 3인칭인지 판별해주는 변수
    private var _userPOV = MutableLiveData<Int>(1)
    val userPOV :LiveData<Int>  = _userPOV

    // 현재 시스템의 각종 정보를 표현해주기 위한 문자열
    private var _currentSpeed = MutableLiveData<String>()
    val currentSpeed : LiveData<String> = _currentSpeed

    // https://developers.kakaomobility.com/docs/android-ref-kotlin/class-KNMapView/
    //범위는 서버 설정에 따라 0.1에서 9999.0까지 지정할 수 있으며 이 범위를 벗어나는 설정은 무시됩니다.
    private var _currentZoom = MutableLiveData<Float>(2.5f)
    val currentZoom : LiveData<Float> = _currentZoom

    // 경유지 추가여부를 판단해주는 변수
    var isAddVias = false

    val pos1 = WGS84ToKATEC(129.039325, 35.114511)
    val poi = KNPOI("부산역", pos1.x.toInt(),pos1.y.toInt(),"부산역")

    fun setCurrentSpeed(currentSpeed : String) {
        _currentSpeed.value = currentSpeed
    }

    fun setUserPOV(viewPoint : Int) {
        _userPOV.value = viewPoint
    }

    fun setZoom(zoom: Float) {
        _currentZoom.value = zoom
    }

    fun zoomOut() {
        if(_currentZoom.value!! > 9999) {
            return
        }
        _currentZoom.value = _currentZoom.value!! + 0.5f
    }

    fun zoomIn() {
        if(_currentZoom.value!! < 0.1) {
            return
        }

        _currentZoom.value = _currentZoom.value!! - 0.5f
    }

    fun convertSafetyCode(safetyCode : String) : String{
        //https://developers.kakaomobility.com/docs/android-ref-kotlin/enumerate-KNSafetyCode/
        when(safetyCode) {
            "KNSafetyCode_TrafficAccidentPos" -> return "교통사고 다발 구간"
            "KNSafetyCode_SharpTurnSection" -> return "급회전"
            "KNSafetyCode_FallingRocksArea" -> return "낙석 위험 지역"
            "KNSafetyCode_FogArea" -> return "안개 지역"
            "KNSafetyCode_FallingCaution" -> return "추락 위험 지역"
            "KNSafetyCode_SlippingRoad" -> return "미끄럼 주의"
            "KNSafetyCode_Hump" -> return "과속 방지턱"
            "KNSafetyCode_RailroadCrossing" -> return "철도 건널목"
            "KNSafetyCode_ChildrenProtectionZone" -> return "어린이 보호 구역"
            "KNSafetyCode_RoadNarrows" -> return "좁아지는 도로"
            "KNSafetyCode_SteepDownhillSection" -> return "급경사 내리막 구역"
            "KNSafetyCode_AnimalsAppearingCaution" -> return "야생 동물 보호 지역"
            "KNSafetyCode_RestArea" -> return "졸음 쉼터"
            "KNSafetyCode_DrowsyDrivingAccidentPos" -> return "졸음 운전 사고 다발 구간"
            "KNSafetyCode_UphillSection" -> return "오르막 차로 구간"
            "KNSafetyCode_CautionSignals" -> return "주의 신호등"
            "KNSafetyCode_OneTollingGate" -> return "무정차 톨게이트"
            "KNSafetyCode_CarAccidentPos" -> return "차대차 사고 다발 구간"
            "KNSafetyCode_PedestrianAccidentPos" -> return "보행자 사고 다발 구간"
            "KNSafetyCode_ChildrenAccidentPos" -> return "어린이 보호 구역 내 어린이 사고 다발 구간"
            "KNSafetyCode_FrozenRoad" -> return "상습 결빙 구간"
            "KNSafetyCode_HeightLimitPos" -> return "높이 제한 구역"
            "KNSafetyCode_WeightLimitPos" -> return  "중량 제한 구역"
            "KNSafetyCode_FogAreaLive" -> return "안개 주의 구간(실시간 제공)"
            "KNSafetyCode_FrozenRoadLive" -> return "결빙 주의 구간(실시간 제공)"
            "KNSafetyCode_ViolationCamera" -> return "기타 단속 구간"
            "KNSafetyCode_MovableSpeedViolationCamera" -> return "이동식 과속 단속 카메라"
            "KNSafetyCode_SpeedViolationCamera"	-> return "고정식 과속 단속 카메라"
            "KNSafetyCode_TrafficCollectionCamera"	-> return "정보 수집 카메라"
            "KNSafetyCode_BuslaneViolationCamera" -> return "버스 전용 차로 위반 단속 카메라"
            "KNSafetyCode_OverloadViolationCamera" -> return "과적 단속 카메라"
            "KNSafetyCode_SignalAndSpeedViolationCamera" -> return "신호 및 과속 단속 카메라"
            "KNSafetyCode_ParkingViolationCamera" -> return "주정차 위반 단속 카메라"
            "KNSafetyCode_CargoViolationCamera" -> return "적재 불량 단속 카메라"
            "KNSafetyCode_BuslaneAndSpeedViolationCamera" -> return "버스 전용 차로 및 신호 위반 단속 카메라"
            "KNSafetyCode_SignalViolationCamera" -> return "신호 위반 단속 카메라"
            "KNSafetyCode_LaneAndSpeedViolationCamera" -> return "차로 및 과속 단속 카메라"
            "KNSafetyCode_SpeedViolationSectionInCamera" -> return "구간 단속 시점 카메라"
            "KNSafetyCode_SpeedViolationSectionOutCamera" -> return "구간 단속 종점 카메라"
            "KNSafetyCode_ShoulderLaneViolationCamera" -> return "갓길 단속 카메라"
            "KNSafetyCode_CutInViolationCamera" -> return "끼어들기 위반 단속 카메라"
            "KNSafetyCode_SpeedViolationSection" -> return "구간 단속 구간"
            "KNSafetyCode_DrivingLaneViolationCamera" -> return "지정 차로 단속 카메라"
            "KNSafetyCode_LandChangeViolationSectionInCamera" -> return "차로 변경 구간 단속 시점 카메라"
            "KNSafetyCode_LandChangeViolationSectionOutCamera" -> return "차로 변경 구간 단속 종점 카메라"
            "KNSafetyCode_BoxedSpeedViolationCamera" -> return "박스형 과속 단속 카메라"
            "KNSafetyCode_SeatBeltViolationCamera" -> return "안전벨트 미착용 단속 카메라"
            "KNSafetyCode_SpeedViolationBackwardCamera" -> return "후면 과속 단속 카메라"
            "KNSafetyCode_SignalAndSpeedViolationBackwardCamera" -> return "후면 신호 및 과속 단속 카메라"
            else -> return "code is ${safetyCode}"
        }
    }

    fun initalizeCurrentZoom() {
        _currentZoom.value = 2.5f
    }

    fun convertKNRoadType(knRoadType: KNRoadType) : String{
        when(knRoadType.name) {
            "KNRoadType_Unknown" -> return "알 수 없음"
            "KNRoadType_Highway" -> return "고속 도로"
            "KNRoadType_GeneralRoad" -> return "일반 도로"
            "KNRoadType_Ferry" -> return "페리 항로"
            else -> return "값이 없음"
        }

    }

    fun setCurrentSafetyCode(safetyCode: String) {
        _currentSafetyCode.value = safetyCode
    }

    fun setCurrentTrafficSpd(speed: Int) {
        _currentTrafficSpd.value = speed
    }

    fun setCurrentRoadName(roadName: String) {
        _roadName.value = roadName
    }

    fun setCurrentRoadType(roadType: String) {
        _roadType.value = roadType
    }


}