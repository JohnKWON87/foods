// ========== 카카오맵 초기화 및 관리 ==========
//📌 전체 구조
//맛집 상세 페이지에서 주소를 기반으로 카카오맵을 표시하고 마커를 찍는 기능입니다.주요 특징:
//
//비동기 API 로딩 대기
//주소 → 좌표 변환 (지오코딩)
//마커 + 정보창 표시
//에러 처리 및 디버깅

console.log('🗺️ restaurant-map.js 파일 로드됨');

/**
 * 카카오맵 초기화 함수
 */
function initializeKakaoMap() {
    const mapContainer = document.getElementById('map');
    const loadingMessage = document.getElementById('mapLoadingMessage');

    if (!mapContainer) {
        console.error('❌ 지도 컨테이너를 찾을 수 없습니다.');
        return;
    }

    console.log('🗺️ 지도 컨테이너 발견');

//    역할: 지도를 표시할 div 요소 찾기HTML 구조 예시:
//    html<div id="map" style="width:100%; height:400px;"></div>
//    <div id="mapLoadingMessage">지도를 불러오는 중...</div>

    // 카카오맵 기본 설정
    const mapOptions = {
        center: new kakao.maps.LatLng(37.4979, 127.0276),   // 강남역
        level: 3    // 확대 레벨
    };

    const map = new kakao.maps.Map(mapContainer, mapOptions);
    console.log('✅ 카카오맵 객체 생성 완료');

//    역할: 기본 위치(강남역)로 지도 생성
//    확대 레벨:
//    javascriptlevel: 1  // 가장 확대 (건물 단위)
//    level: 3  // 적당히 확대 (동네 단위) ⭐ 기본값
//    level: 5  // 중간 (구 단위)
//    level: 10 // 멀리 (시 단위)

    // 주소 확인
    if (!window.restaurantData || !window.restaurantData.address || window.restaurantData.address.trim() === '') {
        console.warn('⚠️ 주소 정보가 없습니다.');
        if (loadingMessage) {
            loadingMessage.innerHTML = '<em>⚠️ 주소 정보가 등록되어 있지 않습니다.</em>';
        }
        return;
    }

//    역할: 서버에서 전달받은 맛집 데이터 확인
//    window.restaurantData란?
//    javascript// HTML에서 서버 데이터를 JavaScript 변수로 전달
//    <script>
//        window.restaurantData = {
//            name: "맛있는 피자집",
//            address: "서울시 강남구 테헤란로 123",
//            phone: "02-1234-5678"
//        };
//    </script>
//
//    // 그 후에 이 파일 로드
//    <script src="/js/restaurant-map.js"></script>
//    Thymeleaf 예시:
//    html<script th:inline="javascript">
//        window.restaurantData = {
//            name: /*[[${restaurant.name}]]*/ '',
//            address: /*[[${restaurant.address}]]*/ '',
//            phone: /*[[${restaurant.phone}]]*/ ''
//        };
//    </script>

    console.log('📍 주소 정보:', window.restaurantData.address);

    // 지오코더 생성
    const geocoder = new kakao.maps.services.Geocoder();

//    Geocoder란?
//
//    주소 → 좌표 변환하는 객체
//    예: "서울시 강남구 테헤란로 123" → (37.4979, 127.0276)
//
//    addressSearch 메서드:
//    javascriptgeocoder.addressSearch(주소, 콜백함수);
//
//    // 비동기로 실행됨
//    // 결과가 오면 콜백 함수가 호출됨

    geocoder.addressSearch(window.restaurantData.address, function(result, status) {
        if (status === kakao.maps.services.Status.OK) {
            const coords = new kakao.maps.LatLng(result[0].y, result[0].x);

            map.setCenter(coords);      // 지도 중심을 해당 위치로

            const marker = new kakao.maps.Marker({
                map: map,
                position: coords,
                title: window.restaurantData.name

//                **동작 흐름**:
//                ```
//                1. 주소 검색 성공!
//                   result = [{
//                       address_name: "서울 강남구 역삼동 123",
//                       x: "127.0276",  // 경도
//                       y: "37.4979"    // 위도
//                   }]
//
//                2. 좌표 객체 생성
//                   coords = LatLng(37.4979, 127.0276)
//
//                3. 지도 중심 이동
//                   [강남역] → [검색된 주소]
//
//                4. 마커 표시
//                   📍 (검색된 위치에 핀)
//                result[0]을 사용하는 이유:
//                javascript// 검색 결과는 배열 (여러 개일 수 있음)
//                result = [
//                    { y: "37.4979", x: "127.0276" },  // 가장 정확한 결과
//                    { y: "37.4980", x: "127.0277" }   // 비슷한 주소
//                ]
//
//                // 첫 번째(가장 정확한) 결과 사용
//                result[0]
            });

            const infoWindow = new kakao.maps.InfoWindow({
                content: '<div style="padding:10px; font-size:14px; width:200px;">' +
                        '<strong>' + window.restaurantData.name + '</strong><br/>' +
                        '📍 ' + window.restaurantData.address + '<br/>' +
                        '📞 ' + window.restaurantData.phone +
                        '</div>',
                removable: true     // X 버튼으로 닫을 수 있음
            });

            infoWindow.open(map, marker);

            kakao.maps.event.addListener(marker, 'click', function() {
                infoWindow.open(map, marker);
            });

//            **InfoWindow 구조**:
//            ```
//            ┌────────────────────────┐
//            │ 맛있는 피자집     [X]    │ ← removable: true
//            │ 📍 서울시 강남구...      │
//            │ 📞 02-1234-5678       │
//            └──────┬─────────────────┘
//                   📍
//                (마커)
//            이벤트 리스너:
//            javascriptkakao.maps.event.addListener(marker, 'click', function() {
//                // 마커 클릭 시 실행
//                infoWindow.open(map, marker);
//            });
//
//            // 다른 이벤트들
//            kakao.maps.event.addListener(marker, 'mouseover', ...);  // 마우스 올림
//            kakao.maps.event.addListener(marker, 'mouseout', ...);   // 마우스 벗어남

            console.log('✅ 카카오맵 로드 성공:', coords);
            if (loadingMessage) {
                loadingMessage.style.display = 'none';      // 로딩 메시지 숨김
            }

//            **화면 변화**:
//            ```
//            [로딩 중]
//            ┌──────────────────┐
//            │ 지도를 불러오는 중...│
//            └──────────────────┘
//
//            ↓
//
//            [로드 완료]
//            ┌──────────────────┐
//            │   🗺️ 지도        │
//            │      📍          │
//            │  (맛집 위치)      │
//            └──────────────────┘

        } else if (status === kakao.maps.services.Status.ZERO_RESULT) {
            console.warn('⚠️ 검색 결과 없음');
            if (loadingMessage) {
                loadingMessage.innerHTML = '<em>❌ 주소를 찾을 수 없습니다.</em>';
            }

        } else {
            console.error('❌ 지오코딩 에러:', status);
            if (loadingMessage) {
                loadingMessage.innerHTML = '<em>❌ 지도 로드 중 오류 발생</em>';
            }
        }
    });
}

//상태별 처리:
//javascript// 성공
//Status.OK → 마커 표시
//
//// 검색 결과 없음
//Status.ZERO_RESULT → "주소를 찾을 수 없습니다"
//
//// 기타 오류
//Status.ERROR → "지도 로드 중 오류 발생"



/**
 * 카카오맵 API 로딩 대기
 */
function waitForKakao() {
    console.log('⏳ 카카오맵 API 로딩 대기 시작');

    let attempts = 0;
    const maxAttempts = 50;     // 최대 50번 시도

//    왜 필요한가요?
//    html<!-- HTML 구조 -->
//    <script src="//dapi.kakao.com/v2/maps/sdk.js?appkey=..."></script>
//    <script src="/js/restaurant-map.js"></script>
//
//    문제:
//    카카오맵 API 로딩이 늦으면
//    → restaurant-map.js 실행 시점에 kakao가 undefined
//    → 에러 발생!
//
//    해결:
//    API가 로드될 때까지 기다리기

    const checkInterval = setInterval(() => {
        attempts++;

        if (typeof kakao !== 'undefined' && kakao.maps) {
            clearInterval(checkInterval);       // 더 이상 체크 안 함
            console.log(`✅ 카카오맵 API 로드 완료 (${attempts}번째 시도)`);

            // restaurantData 확인
            if (!window.restaurantData) {
                console.error('❌ restaurantData가 정의되지 않았습니다');
                return;
            }

            // kakao.maps.load() 사용
            kakao.maps.load(() => {
                console.log('✅ kakao.maps.load() 완료');
                initializeKakaoMap();       // 지도 초기화!
            });

//            폴링(Polling) 방식:
//            javascriptsetInterval(() => {
//                // 100ms마다 반복 실행
//                if (kakao가 로드됨) {
//                    // 성공! 지도 초기화
//                    clearInterval();
//                }
//            }, 100);
//            ```
//
//            **동작 흐름**:
//            ```
//            시도 1 (100ms): kakao 없음 → 계속
//            시도 2 (200ms): kakao 없음 → 계속
//            시도 3 (300ms): kakao 없음 → 계속
//            ...
//            시도 8 (800ms): kakao 로드됨! ✅
//                            → clearInterval()
//                            → initializeKakaoMap() 실행
//            kakao.maps.load() 설명:
//            javascript// 카카오맵 공식 초기화 방법
//            kakao.maps.load(() => {
//                // 모든 카카오맵 리소스가 준비된 후 실행
//                // 안전하게 지도 생성 가능
//            });


        } else if (attempts >= maxAttempts) {
            clearInterval(checkInterval);
            console.error('❌ 카카오맵 API 로드 실패 (타임아웃)');
            console.error('🔍 디버깅 정보:');
            console.error('  - typeof kakao:', typeof kakao);
            console.error('  - window.kakao:', window.kakao);

            const script = document.querySelector('script[src*="dapi.kakao.com"]');
            console.error('  - 스크립트 태그:', script);
            console.error('  - 스크립트 URL:', script?.src);

            const mapLoadingMessage = document.getElementById('mapLoadingMessage');
            if (mapLoadingMessage) {
                mapLoadingMessage.innerHTML = '<em>❌ 카카오맵을 불러올 수 없습니다. Network 탭을 확인하세요.</em>';
            }

//            역할: 5초(50 × 100ms) 안에 로드 안 되면 에러 표시
//            디버깅 정보 출력:
//            javascript// 콘솔에 표시되는 내용:
//            ❌ 카카오맵 API 로드 실패 (타임아웃)
//            🔍 디버깅 정보:
//              - typeof kakao: undefined
//              - window.kakao: undefined
//              - 스크립트 태그: <script src="...">
//              - 스크립트 URL: //dapi.kakao.com/v2/maps/sdk.js?appkey=...
//
//            → 개발자가 문제 원인 파악 가능!

        } else {
            if (attempts % 10 === 0) {      // 10번마다 로그
                console.log(`⏳ 카카오맵 로딩 대기 중... (${attempts}/${maxAttempts})`);
            }
        }
    }, 100);        // 100ms마다 실행
}

//**콘솔 출력 예시**:
//```
//⏳ 카카오맵 API 로딩 대기 시작
//⏳ 카카오맵 로딩 대기 중... (10/50)
//⏳ 카카오맵 로딩 대기 중... (20/50)
//✅ 카카오맵 API 로드 완료 (23번째 시도)

/**
 * 페이지 로드 시 실행
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ restaurant-map.js DOMContentLoaded 이벤트 발생');
    waitForKakao();     // API 로딩 대기 시작
});

//**전체 실행 순서**:
//```
//1. HTML 파싱
//   ↓
//2. DOMContentLoaded 이벤트 발생
//   ↓
//3. waitForKakao() 실행
//   ↓
//4. 100ms마다 kakao 확인
//   ↓
//5. kakao 로드되면 initializeKakaoMap() 실행
//   ↓
//6. 주소 검색 (지오코딩)
//   ↓
//7. 지도에 마커 표시
//   ↓
//8. 완료!
//```
//
//---
//
//## 🎯 **전체 시나리오**
//
//### **시나리오: 맛집 상세 페이지 접속**
//```
//1. 사용자가 맛집 상세 페이지 접속
//   URL: /restaurants/123
//
//2. 서버가 HTML 응답
//   <script>
//     window.restaurantData = {
//       name: "맛있는 피자집",
//       address: "서울시 강남구 테헤란로 123",
//       phone: "02-1234-5678"
//     };
//   </script>
//   <script src="//dapi.kakao.com/v2/maps/sdk.js"></script>
//   <script src="/js/restaurant-map.js"></script>
//
//3. restaurant-map.js 로드
//   → DOMContentLoaded 이벤트 등록
//
//4. DOM 로드 완료
//   → waitForKakao() 실행
//
//5. 100ms마다 체크
//   시도 1: kakao 없음
//   시도 2: kakao 없음
//   ...
//   시도 8: kakao 로드됨! ✅
//
//6. kakao.maps.load() 실행
//   → initializeKakaoMap() 호출
//
//7. 지도 컨테이너 확인
//   → div#map 발견 ✅
//
//8. 주소 데이터 확인
//   → window.restaurantData.address 있음 ✅
//
//9. 지오코더로 주소 검색
//   "서울시 강남구 테헤란로 123"
//   → (37.4979, 127.0276)
//
//10. 지도에 마커 표시
//    ┌──────────────────┐
//    │   🗺️ 지도        │
//    │ ┌──────────────┐ │
//    │ │맛있는 피자집 │ │
//    │ │📍 서울시...  │ │
//    │ │📞 02-1234... │ │
//    │ └──┬───────────┘ │
//    │    📍            │
//    └──────────────────┘
//
//11. 로딩 메시지 숨김
//    "지도를 불러오는 중..." → 사라짐
//
//💡 핵심 개념 정리
//1. 비동기 API 로딩
//javascript// 문제: 외부 스크립트 로딩이 느림
//<script src="external-api.js"></script>
//<script>
//    externalAPI.init(); // 에러! 아직 안 로드됨
//</script>
//
//// 해결: 폴링으로 대기
//setInterval(() => {
//    if (typeof externalAPI !== 'undefined') {
//        externalAPI.init();
//        clearInterval();
//    }
//}, 100);
//2. 지오코딩 (Geocoding)
//javascript// 주소 → 좌표
//"서울시 강남구" → (37.4979, 127.0276)
//
//// 역지오코딩 (Reverse Geocoding)
//(37.4979, 127.0276) → "서울시 강남구"
//3. 콜백 패턴
//javascriptgeocoder.addressSearch(address, function(result, status) {
//    // 검색 완료 후 실행
//    if (status === 'OK') {
//        // 성공 처리
//    }
//});
//
//// 비동기로 실행됨
//// 결과가 오면 콜백 함수가 호출됨
//4. 서버 데이터 전달
//javascript// 서버 → JavaScript
//<script>
//    window.globalVariable = {
//        data: "서버 데이터"
//    };
//</script>
//<script src="my-script.js"></script>
//
//// my-script.js에서 사용
//console.log(window.globalVariable.data);
//
//🐛 자주 발생하는 이슈
//이슈 1: kakao is not defined
//javascript// 원인
//카카오맵 API 스크립트가 로드되기 전에 실행
//
//// 해결책 1: waitForKakao() 사용 (현재 코드)
//// 해결책 2: defer 속성
//<script defer src="//dapi.kakao.com/v2/maps/sdk.js"></script>
//
//// 해결책 3: async/await
//<script async src="//dapi.kakao.com/v2/maps/sdk.js"
//        onload="initMap()"></script>
//이슈 2: 주소를 찾을 수 없습니다
//javascript// 원인
//- 잘못된 주소 형식
//- 오타
//- 너무 간략한 주소
//
//// 해결
//정확한 도로명 주소 사용:
//❌ "강남구"
//❌ "서울 강남"
//✅ "서울시 강남구 테헤란로 123"
//이슈 3: restaurantData가 undefined
//javascript// 원인
//서버에서 데이터를 전달하지 않음
//
//// 체크
//1. HTML에 window.restaurantData가 있는지
//2. restaurant-map.js보다 먼저 선언되었는지
//
//// 올바른 순서
//<script>
//    window.restaurantData = { ... };  // 먼저!
//</script>
//<script src="/js/restaurant-map.js"></script>  // 나중!
//이슈 4: 지도가 회색으로만 보여요
//javascript// 원인
//지도 컨테이너의 크기가 0
//
//// 해결
//<div id="map" style="width:100%; height:400px;"></div>
//                    ↑           ↑
//                반드시 크기 지정!
//
//🎨 개선 아이디어
//1. 로딩 스피너 추가
//javascriptfunction initializeKakaoMap() {
//    const loadingMessage = document.getElementById('mapLoadingMessage');
//    loadingMessage.innerHTML = `
//        <div class="spinner"></div>
//        <p>지도를 불러오는 중...</p>
//    `;
//
//    // ... 나머지 코드
//}
//css.spinner {
//    border: 4px solid #f3f3f3;
//    border-top: 4px solid #3498db;
//    border-radius: 50%;
//    width: 40px;
//    height: 40px;
//    animation: spin 1s linear infinite;
//}
//
//@keyframes spin {
//    0% { transform: rotate(0deg); }
//    100% { transform: rotate(360deg); }
//}
//2. 재시도 버튼
//javascriptif (status === kakao.maps.services.Status.ZERO_RESULT) {
//    loadingMessage.innerHTML = `
//        <em>❌ 주소를 찾을 수 없습니다.</em>
//        <button onclick="initializeKakaoMap()">다시 시도</button>
//    `;
//}
//3. 길찾기 버튼
//javascriptconst infoWindow = new kakao.maps.InfoWindow({
//    content: `
//        <div style="padding:10px;">
//            <strong>${restaurantData.name}</strong><br/>
//            📍 ${restaurantData.address}<br/>
//            📞 ${restaurantData.phone}<br/>
//            <a href="https://map.kakao.com/link/to/${restaurantData.name},${result[0].y},${result[0].x}"
//               target="_blank">
//                🚗 길찾기
//            </a>
//        </div>
//    `
//});
//4. 지도 확대/축소 컨트롤
//javascriptconst map = new kakao.maps.Map(mapContainer, mapOptions);
//
//// 줌 컨트롤 추가
//const zoomControl = new kakao.maps.ZoomControl();
//map.addControl(zoomControl, kakao.maps.ControlPosition.RIGHT);
//
//// 지도 타입 컨트롤 (일반/스카이뷰)
//const mapTypeControl = new kakao.maps.MapTypeControl();
//map.addControl(mapTypeControl, kakao.maps.ControlPosition.TOPRIGHT);
//```
//
//---
//
//## 🔄 **restaurant-form.js와 비교**
//
//| 특징 | restaurant-form.js | restaurant-map.js |
//|------|-------------------|------------------|
//| **목적** | 맛집 등록/수정 | 맛집 위치 보기 |
//| **지도** | 주소 검색용 | 위치 표시용 |
//| **마커** | 사용자가 설정 | 자동 표시 |
//| **정보창** | 주소 확인용 | 맛집 정보 표시 |
//| **좌표 저장** | ✅ 폼에 저장 | ❌ 표시만 |
//| **API 대기** | ❌ 동기 | ✅ 폴링 방식 |
//
//---
//
//## 📚 **학습 포인트**
//
//### **이 파일에서 배운 것들**
//
//1. **비동기 API 로딩 처리**
//   - 폴링 방식
//   - setInterval/clearInterval
//   - 타임아웃 처리
//
//2. **에러 처리 및 디버깅**
//   - 상세한 에러 메시지
//   - 디버깅 정보 출력
//   - 사용자 친화적 메시지
//
//3. **카카오맵 API**
//   - 지도 생성
//   - 지오코딩
//   - 마커 표시
//   - 정보창
//
//4. **데이터 전달**
//   - 서버 → JavaScript
//   - window 객체 활용
//
//---
//
//## 🎊 **완전 종료!**
//
//**총 10개의 JavaScript 파일 분석 완료!** 🎉
//
//### **최종 파일 목록**
//
//1. ⭐ admin.js - 관리자 페이지
//2. ⭐⭐ board-detail.js - 게시글 상세
//3. ⭐⭐⭐ board-form.js - 게시글 작성
//4. ⭐⭐ board-list.js - 게시글 목록
//5. ⭐⭐⭐⭐ bookmark-list.js - 북마크 목록
//6. ⭐ navbar.js - 네비게이션
//7. ⭐⭐⭐⭐ restaurant-detail.js - 맛집 상세
//8. ⭐⭐⭐⭐⭐ restaurant-form.js - 맛집 등록
//9. ⭐⭐⭐⭐⭐ restaurant-list.js - 3D 카드
//10. ⭐⭐⭐ restaurant-map.js - 지도 표시
//
//---
//
//### **카카오맵 사용 파일**
//
//| 파일 | 용도 | API 대기 |
//|------|------|---------|
//| restaurant-form.js | 주소 검색 + 저장 | ❌ |
//| restaurant-map.js | 위치 표시 | ✅ 폴링 |
//
//---
//
//### **프로젝트 전체 구조**
//```
//맛집 플랫폼
//├── 관리자 기능
//│   └── admin.js
//│
//├── 게시판 기능
//│   ├── board-detail.js (댓글)
//│   ├── board-form.js (작성)
//│   └── board-list.js (목록)
//│
//├── 맛집 기능
//│   ├── restaurant-detail.js (상세)
//│   ├── restaurant-form.js (등록)
//│   ├── restaurant-list.js (3D 목록)
//│   └── restaurant-map.js (지도)
//│
//├── 북마크 기능
//│   └── bookmark-list.js
//│
//└── 공통 UI
//    └── navbar.js