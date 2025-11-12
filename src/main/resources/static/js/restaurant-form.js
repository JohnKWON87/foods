//📌 전체 구조
//맛집 정보를 등록/수정하는 페이지에서 사용하는 기능들:
//
//카카오맵: 주소 검색 및 지도 표시
//이미지 업로드: 대표 이미지 + 추가 이미지
//이미지 미리보기: 업로드 전 확인
//폼 검증: 필수 항목 체크

// ========== 전역 변수 ==========
let mainImageFile = null;               // 대표 이미지 파일
let additionalImageFiles = [];          // 추가 이미지 파일들 (배열)
let formMap = null;                     // 카카오맵 객체
let mapMarker = null;                   // 지도 마커
let mapInfoWindow = null;               // 지도 정보창

//역할: 파일 전체에서 사용할 변수들
//왜 전역 변수인가요?
//
//여러 함수에서 공유해야 하는 데이터
//예: 이미지 파일을 선택하는 함수, 미리보기하는 함수, 업로드하는 함수 모두 접근 필요
//
//변수 설명:
//javascriptmainImageFile = File {
//    name: "pizza.jpg",
//    size: 524288,
//    type: "image/jpeg"
//}
//
//additionalImageFiles = [
//    File { name: "menu1.jpg", ... },
//    File { name: "menu2.jpg", ... },
//    File { name: "interior.jpg", ... }
//]

// ========== 카카오맵 초기화 ==========
function initializeFormMap() {
    const mapContainer = document.getElementById('formMap');

    if (!mapContainer) {
        console.error('❌ 지도 컨테이너를 찾을 수 없습니다.');
        return;
    }

    const mapOptions = {
        center: new kakao.maps.LatLng(37.4979, 127.0276), // 기본: 강남역
        level: 3        // 확대 레벨 (1~14, 숫자가 작을수록 확대)
    };

    formMap = new kakao.maps.Map(mapContainer, mapOptions);
    console.log('✅ 폼 지도 초기화 완료');

//    역할: 페이지 로드 시 카카오맵 생성
//    카카오맵 API 기본 구조:
//    javascript// 1. 지도를 표시할 div
//    <div id="formMap" style="width:100%; height:400px;"></div>
//
//    // 2. 지도 옵션 설정
//    const mapOptions = {
//        center: new kakao.maps.LatLng(위도, 경도),
//        level: 확대레벨
//    };
//
//    // 3. 지도 생성
//    const map = new kakao.maps.Map(div요소, 옵션);
//    좌표 설명:
//
//    37.4979, 127.0276 = 강남역 좌표
//    위도(Latitude): 북위 37.4979도
//    경도(Longitude): 동경 127.0276도




    // 기존 주소가 있으면 자동 표시
    const existingAddress = document.getElementById('address').value.trim();
    if (existingAddress) {
        setTimeout(() => {
            searchAddressOnMap(existingAddress);
        }, 500);        // 0.5초 후 실행
    }
//    **역할**: 수정 모드일 때 기존 주소를 지도에 표시
//
//    **시나리오**:
//    ```
//    [신규 등록]
//    → existingAddress = "" (빈 값)
//    → 지도만 표시 (강남역 중심)
//
//    [수정 모드]
//    → existingAddress = "서울시 강남구 테헤란로 123"
//    → 0.5초 후 해당 주소를 지도에 표시

//    왜 setTimeout?
//    javascript// 지도가 완전히 로드되기 전에 실행하면 오류!
//    // 약간의 지연을 줘서 안정적으로 동작하게 함
//    setTimeout(() => { ... }, 500);

}

// ========== 주소 검색 및 지도 업데이트 ==========
function searchAddressOnMap(addressParam = null) {
// 파라미터가 있으면 그걸 사용, 없으면 입력란에서 가져옴
    const address = (addressParam || document.getElementById('addressSearch').value).trim();

    if (!address) {
        alert('주소를 입력해주세요.');
        return;
    }

    // 카카오맵 주소-좌표 변환 객체
    const geocoder = new kakao.maps.services.Geocoder();
//    Geocoder란?
//
//    주소 → 좌표 변환 (예: "강남역" → 37.4979, 127.0276)
//    좌표 → 주소 변환 (역지오코딩)

        geocoder.addressSearch(address, function(result, status) {
            if (status === kakao.maps.services.Status.OK) {
                // 성공!
                const coords = new kakao.maps.LatLng(result[0].y, result[0].x);

                // ✅ 1. 폼의 주소 필드에 입력
                document.getElementById('address').value = address;

                // ✅ 2. 좌표 hidden 필드에 입력
                document.getElementById('latitude').value = result[0].y;
                document.getElementById('longitude').value = result[0].x;

                console.log('✅ 주소 저장:', address);
                console.log('✅ 위도:', result[0].y);
                console.log('✅ 경도:', result[0].x);
//                역할: 주소를 좌표로 변환하고 폼에 저장
//                HTML 폼 구조 예시:
//                html<!-- 사용자가 보는 검색창 -->
//                <input type="text" id="addressSearch" placeholder="주소 검색">
//                <button id="searchBtn">검색</button>
//
//                <!-- 서버로 전송될 실제 데이터 (hidden) -->
//                <input type="hidden" id="address" name="address">
//                <input type="hidden" id="latitude" name="latitude">
//                <input type="hidden" id="longitude" name="longitude">
//                주소 검색 결과 예시:
//                javascriptresult = [
//                    {
//                        address_name: "서울 강남구 역삼동 123",
//                        x: "127.0276",  // 경도
//                        y: "37.4979"    // 위도
//                    }
//                ]


                // 지도 중심 이동
                formMap.setCenter(coords);

                // 기존 마커 제거
                if (mapMarker) {
                    mapMarker.setMap(null);     // 지도에서 제거
                }

                // 새 마커 생성
                mapMarker = new kakao.maps.Marker({
                    map: formMap,
                    position: coords,
                    title: address
                });
//                **역할**: 검색한 주소에 마커(핀) 표시
//
//                **동작 흐름**:
//                ```
//                [초기 상태]
//                ┌──────────────┐
//                │   🗺️ 지도    │ (강남역 중심)
//                └──────────────┘
//
//                ↓ "서울시 종로구" 검색
//
//                [마커 표시]
//                ┌──────────────┐
//                │   🗺️ 지도    │
//                │      📍      │ ← 새 마커
//                └──────────────┘
//                (종로구로 이동)


//                왜 기존 마커를 제거?
//                javascript// 제거 안 하면 마커가 계속 쌓임!
//                // 첫 검색: 📍
//                // 두 번째 검색: 📍📍
//                // 세 번째 검색: 📍📍📍



                // 인포윈도우
                if (mapInfoWindow) {
                    mapInfoWindow.close();      // 기존 정보창 닫기
                }

                mapInfoWindow = new kakao.maps.InfoWindow({
                    content: '<div style="padding:10px; font-size:13px;"><strong>' + address + '</strong></div>',
                    removable: true             // X 버튼으로 닫을 수 있음
                });

                mapInfoWindow.open(formMap, mapMarker);

                alert('✅ 주소가 확정되었습니다!\n' + address);
//                **역할**: 마커 위에 주소를 표시하는 말풍선
//
//                **화면 예시**:
//                ```
//                ┌──────────────────────┐
//                │   🗺️ 지도            │
//                │  ┌─────────────┐     │
//                │  │ 서울시 강남구 │ [X] │ ← InfoWindow
//                │  └──────┬──────┘     │
//                │         📍           │ ← Marker
//                └──────────────────────┘



            } else if (status === kakao.maps.services.Status.ZERO_RESULT) {
                alert('⚠️ 검색 결과가 없습니다.\n정확한 주소를 입력해주세요.');
                console.warn('검색 결과 없음');

            } else if (status === kakao.maps.services.Status.ERROR) {
                alert('❌ 지도 서비스 오류가 발생했습니다.');
                console.error('지오코딩 에러');
            }
//            상태 코드별 처리:
//
//            OK: 성공
//            ZERO_RESULT: 검색 결과 없음
//            ERROR: 서비스 오류
        });
    }

// ========== 🌟 대표 이미지 처리 ==========
function handleMainImageSelect(event) {
    const file = event.target.files[0];         // 첫 번째 파일만

    if (!file) {
        console.log('선택된 파일이 없습니다.');
        return;
    }

    // 파일 유효성 검사
    if (!file.type.startsWith('image/')) {
        alert(`❌ ${file.name}은(는) 이미지 파일이 아닙니다.`);
        event.target.value = '';                // 입력 초기화
        return;
    }

    const maxSize = 10 * 1024 * 1024;            // 10MB
    if (file.size > maxSize) {
        alert(`❌ ${file.name}의 크기가 너무 큽니다.\n최대: 10MB`);
        event.target.value = '';
        return;
    }

    mainImageFile = file;                       // 전역 변수에 저장
    console.log(`🌟 대표 이미지 선택: ${file.name}`);
    displayMainImagePreview();

//    역할: 파일 선택 시 유효성 검사 + 전역 변수에 저장

//    HTML 연결:
//    html<input type="file" id="mainImageInput"
//           accept="image/*"
//           onchange="handleMainImageSelect(event)">
//    파일 타입 체크:
//    javascriptfile.type.startsWith('image/')
//
//    // 가능한 타입:
//    // "image/jpeg" ✅
//    // "image/png"  ✅
//    // "image/gif"  ✅
//    // "video/mp4"  ❌
//    // "text/html"  ❌


//    파일 크기 계산:
//    javascript10 * 1024 * 1024
//    = 10 × 1024KB
//    = 10 × 1MB
//    = 10MB
}

function displayMainImagePreview() {
    const previewContainer = document.getElementById('mainImagePreview');

    if (!previewContainer) {
        console.error('대표 이미지 미리보기 컨테이너를 찾을 수 없습니다.');
        return;
    }

    if (!mainImageFile) {
        previewContainer.innerHTML = '';        // 비우기
        return;
    }

    const reader = new FileReader();

    reader.onload = function(e) {
        previewContainer.innerHTML = `
            <div class="image-preview">
                <div class="image-preview-item">
                    <img src="${e.target.result}" alt="대표 이미지"
                         onclick="openImageModal('${e.target.result}')">
                    <span class="badge bg-warning text-dark">🌟 대표</span>
                    <div class="mt-2">
                        <input type="text" name="mainImageDesc" class="form-control form-control-sm"
                               placeholder="대표 이미지 설명 (선택)" maxlength="200">
                    </div>
                </div>
            </div>
//            역할: 선택한 이미지를 화면에 미리보기

//            FileReader API 설명:
//            javascriptconst reader = new FileReader();
//
//            // 파일을 Base64 문자열로 변환
//            reader.readAsDataURL(file);
//
//            // 변환 완료 시 실행
//            reader.onload = function(e) {
//                const base64 = e.target.result;
//                // "data:image/jpeg;base64,/9j/4AAQSkZJRg..."
//            };
//            ```
//
//            **Base64란?**
//            ```
//            원본 파일: pizza.jpg (바이너리 데이터)
//                   ↓ FileReader
//            Base64 문자열: "data:image/jpeg;base64,iVBORw0KGgo..."
//                   ↓
//            HTML에서 사용: <img src="data:image/jpeg;base64,...">
//            ```
//
//            **미리보기 화면 예시**:
//            ```
//            [대표 이미지 미리보기]
//            ┌──────────────┐
//            │   🌟 대표     │
//            │ ┌──────────┐ │
//            │ │  이미지   │ │ ← 클릭하면 확대
//            │ └──────────┘ │
//            │ [설명 입력...] │
//            └──────────────┘
        `;
    };

    reader.readAsDataURL(mainImageFile);
}

function removeMainImage() {
    if (!confirm('대표 이미지를 제거하시겠습니까?')) {
        return;
    }

    console.log('🗑️ 대표 이미지 제거');
    mainImageFile = null;
    document.getElementById('mainImageInput').value = '';           // input 초기화
    document.getElementById('mainImagePreview').innerHTML = '';     // 미리보기 제거
//    역할: 선택한 대표 이미지를 취소
}

// ========== 📸 추가 이미지 처리 ==========
function handleAdditionalImagesSelect(event) {
    const files = Array.from(event.target.files);       // FileList를 배열로 변환

    if (files.length === 0) {
        console.log('선택된 파일이 없습니다.');
        return;
    }

    console.log(`📁 ${files.length}개의 추가 이미지 선택됨`);

    const validFiles = [];
        // 이미지 파일 체크
    for (let file of files) {
        if (!file.type.startsWith('image/')) {
            alert(`❌ ${file.name}은(는) 이미지 파일이 아닙니다.`);
            continue;       // 다음 파일로
        }

        // 크기 체크
        const maxSize = 10 * 1024 * 1024;
        if (file.size > maxSize) {
            alert(`❌ ${file.name}의 크기가 너무 큽니다.\n최대: 10MB`);
            continue;
        }

        validFiles.push(file);  // 유효한 파일만 추가
    }

    if (validFiles.length === 0) {
        console.log('유효한 파일이 없습니다.');
        return;
    }

    // 기존 파일에 추가
    additionalImageFiles = [...additionalImageFiles, ...validFiles];
    console.log(`✅ 총 ${additionalImageFiles.length}개의 추가 이미지`);

    displayAdditionalImagesPreview();

//    역할: 여러 이미지를 선택하고 유효성 검사

//    HTML:
//    html<input type="file" multiple
//           accept="image/*"
//           onchange="handleAdditionalImagesSelect(event)">
//    multiple 속성:
//
//    여러 파일 동시 선택 가능
//    Ctrl+클릭 or Shift+클릭
//
//    Spread 연산자 사용:
//    javascriptadditionalImageFiles = [...additionalImageFiles, ...validFiles];
//
//    // 예시:
//    기존: [img1, img2]
//    새로: [img3, img4]
//    결과: [img1, img2, img3, img4]
}

function displayAdditionalImagesPreview() {
    const previewContainer = document.getElementById('additionalImagesPreview');

    if (!previewContainer) {
        console.error('추가 이미지 미리보기 컨테이너를 찾을 수 없습니다.');
        return;
    }

    previewContainer.innerHTML = '';        // 기존 내용 지우기

    if (additionalImageFiles.length === 0) {
        return;
    }

    const container = document.createElement('div');
    container.className = 'image-preview';

    additionalImageFiles.forEach((file, index) => {
        const reader = new FileReader();

        reader.onload = function(e) {
            const item = document.createElement('div');
            item.className = 'image-preview-item';
            item.innerHTML = `
                <img src="${e.target.result}" alt="추가 이미지 ${index + 1}"
                     onclick="openImageModal('${e.target.result}')">
                <div class="menu-name">추가 이미지 ${index + 1}</div>
                <div class="mt-2">
                    <input type="text" name="additionalImageDescs" class="form-control form-control-sm"
                           placeholder="이미지 설명 (선택)" maxlength="200">
                </div>
            `;
            container.appendChild(item);
        };

        reader.readAsDataURL(file);
    });

    previewContainer.appendChild(container);

//    **역할**: 모든 추가 이미지를 미리보기
//
//    **화면 예시**:
//    ```
//    [추가 이미지]
//    ┌────────┬────────┬────────┐
//    │ 이미지1 │ 이미지2 │ 이미지3 │
//    │[설명..] │[설명...] │[설명..]│
//    └────────┴────────┴────────┘
}

// ========== 이미지 모달 ==========
function openImageModal(imageSrc) {
    const modal = document.getElementById('imageModal');
    const modalImg = document.getElementById('modalImage');

    if (modal && modalImg) {
        modalImg.src = imageSrc;
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }
}

function closeImageModal() {
    const modal = document.getElementById('imageModal');

    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }
}

// ESC 키로 모달 닫기
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeImageModal();
    }
});
//역할: restaurant-detail.js와 동일한 모달 기능

// ========== 폼 제출 검증 ==========
function validateForm(event) {
    console.log('📋 폼 제출 검증 시작...');

    const isNewForm = !document.querySelector('input[name="id"]');

    // 신규 등록 시 대표 이미지 필수
    if (isNewForm && !mainImageFile) {
        alert('❌ 대표 이미지는 필수입니다.');
        event.preventDefault();
        document.getElementById('mainImageInput').scrollIntoView({ behavior: 'smooth', block: 'center' });
        return false;
    }

    console.log('✅ 폼 검증 통과');
    return true;
//    역할: 폼 제출 전 필수 항목 체크
//    신규 vs 수정 판단:
//    javascriptconst isNewForm = !document.querySelector('input[name="id"]');
//
//    // 신규: id 필드 없음 → isNewForm = true
//    // 수정: id 필드 있음 → isNewForm = false

//    scrollIntoView 설명:
//    javascriptelement.scrollIntoView({
//        behavior: 'smooth',  // 부드럽게 스크롤
//        block: 'center'      // 화면 중앙에 위치
//    });
}

// ========== 대표 이미지 설정 (CSRF 포함) ==========
function setMainImage(restaurantId, imageId) {
    if (!confirm('이 이미지를 대표 이미지로 설정하시겠습니까?')) {
        return;
    }

    console.log('대표 이미지 설정:', restaurantId, imageId);

    // 동적 폼 생성
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = `/restaurants/${restaurantId}/images/${imageId}/setMain`;

    // CSRF 토큰 추가
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    const csrfInput = document.createElement('input');
    csrfInput.type = 'hidden';
    csrfInput.name = '_csrf';
    csrfInput.value = csrfToken;
    form.appendChild(csrfInput);

    document.body.appendChild(form);
    form.submit();      // 폼 제출

//    역할: 기존 이미지 중 하나를 대표 이미지로 변경

//    동적 폼 생성 이유:
//    javascript// 일반 링크로는 POST 요청 불가
//    <a href="...">변경</a>  // GET 요청만 가능
//
//    // JavaScript로 폼을 만들어서 POST 요청
//    const form = document.createElement('form');
//    form.method = 'POST';
//    form.submit();
//    생성되는 HTML:
//    html<form method="POST" action="/restaurants/123/images/456/setMain">
//        <input type="hidden" name="_csrf" value="abc123xyz">
//    </form>
}

// ========== 이미지 삭제 (CSRF 포함) ==========
function deleteImage(restaurantId, imageId) {
    if (!confirm('이 이미지를 삭제하시겠습니까?')) {
        return;
    }

    console.log('이미지 삭제:', restaurantId, imageId);

    const form = document.createElement('form');
    form.method = 'POST';
    form.action = `/restaurants/${restaurantId}/images/${imageId}/delete`;

    // CSRF 토큰 추가
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    const csrfInput = document.createElement('input');
    csrfInput.type = 'hidden';
    csrfInput.name = '_csrf';
    csrfInput.value = csrfToken;
    form.appendChild(csrfInput);

    document.body.appendChild(form);
    form.submit();
//    역할: setMainImage()와 동일한 방식으로 이미지 삭제
}

// ========== 초기화 ==========
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 맛집 폼 초기화 시작...');

    // 1. 카카오맵 API 체크
    if (typeof kakao === 'undefined' || !kakao.maps) {
        console.error('❌ 카카오맵 API 로드 안됨');
    } else {
        initializeFormMap();
    }

    // 2. 주소 검색 버튼
    const searchBtn = document.getElementById('searchBtn');
    if (searchBtn) {
        searchBtn.addEventListener('click', function(e) {
            e.preventDefault();
            searchAddressOnMap();
        });
    }

    // 3. 엔터키로 주소 검색
    const addressSearch = document.getElementById('addressSearch');
    if (addressSearch) {
        addressSearch.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                searchAddressOnMap();
            }
        });
    }

    // 4. 폼 제출
    const form = document.querySelector('form');
    if (form) {
        form.addEventListener('submit', validateForm);
        console.log('✅ 폼 제출 이벤트 등록');
    }

    // 5. 모달 배경 클릭 시 닫기
    const modal = document.getElementById('imageModal');
    if (modal) {
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {       // 배경만 클릭했을 때
                closeImageModal();
            }
        });
    }

    console.log('✅ 초기화 완료');
});

console.log('✅ restaurant-form.js 로드 완료');

//**역할**: 페이지 로드 시 모든 이벤트 리스너 등록
//
//---
//
//## 🎯 **전체 시나리오**
//
//### **시나리오: 맛집 신규 등록**
//```
//1. 페이지 로드
//   → 카카오맵 초기화 (강남역 중심)
//
//2. 주소 입력: "서울시 종로구 종로3가 123"
//   → [검색] 버튼 클릭
//   ↓
//3. searchAddressOnMap() 실행
//   → 주소 → 좌표 변환
//   → 지도에 마커 표시
//   → 폼 필드에 저장:
//     - address: "서울시 종로구..."
//     - latitude: "37.5707"
//     - longitude: "126.9907"
//
//4. 대표 이미지 선택
//   → handleMainImageSelect()
//   → 유효성 검사 (이미지 파일? 10MB 이하?)
//   → mainImageFile에 저장
//   → 미리보기 표시
//
//5. 추가 이미지 선택 (3개)
//   → handleAdditionalImagesSelect()
//   → 각 파일 유효성 검사
//   → additionalImageFiles에 저장
//   → 미리보기 표시 (3개)
//
//6. [등록] 버튼 클릭
//   → validateForm() 실행
//   → 대표 이미지 있는지 체크
//   ↓
//7. 검증 통과
//   → 서버로 폼 제출
//   → 주소, 좌표, 이미지들 모두 전송
//
//💡 핵심 개념 정리
//1. FileReader API
//javascriptconst reader = new FileReader();
//reader.readAsDataURL(file);  // 파일 → Base64 문자열
//reader.onload = function(e) {
//    const dataURL = e.target.result;
//    // "data:image/jpeg;
//    ### **2. 카카오맵 API 핵심**
//    ```javascript
//    // 지도 생성
//    const map = new kakao.maps.Map(container, options);
//
//    // 마커 생성
//    const marker = new kakao.maps.Marker({
//        map: map,
//        position: coords
//    });
//
//    // 주소 → 좌표 변환
//    const geocoder = new kakao.maps.services.Geocoder();
//    geocoder.addressSearch(address, callback);
//    ```
//
//    ### **3. 전역 변수 패턴**
//    ```javascript
//    // 여러 함수에서 공유할 데이터
//    let mainImageFile = null;
//    let additionalImageFiles = [];
//
//    function selectImage() {
//        mainImageFile = file;  // 저장
//    }
//
//    function uploadImage() {
//        // mainImageFile 사용
//    }
//    ```
//
//    ### **4. 동적 폼 생성**
//    ```javascript
//    // JavaScript로 폼 만들어서 제출
//    const form = document.createElement('form');
//    form.method = 'POST';
//    form.action = '/url';
//    document.body.appendChild(form);
//    form.submit();
//    ```
//
//    ### **5. Array 메서드**
//    ```javascript
//    // FileList → 배열 변환
//    Array.from(fileList)
//
//    // 배열 합치기
//    [...arr1, ...arr2]
//
//    // 배열 순회
//    arr.forEach((item, index) => { ... })
//    ```
//
//    ---
//
//    ## 📊 **데이터 흐름도**
//
//    ### **이미지 업로드 흐름**
//    ```
//    [사용자]
//       ↓ 파일 선택
//    [File 객체] ────────────────┐
//       ↓                        │
//    [handleMainImageSelect]     │
//       ↓ 유효성 검사            │
//    [mainImageFile 저장]        │
//       ↓                        │
//    [FileReader]                │
//       ↓ Base64 변환            │
//    [미리보기 표시]             │
//       ↓                        │
//    [폼 제출] ─────────────────┤
//                               │
//                         [서버로 전송]
//    ```
//
//    ### **주소 검색 흐름**
//    ```
//    [사용자]
//       ↓ 주소 입력 "강남역"
//    [검색 버튼 클릭]
//       ↓
//    [searchAddressOnMap]
//       ↓
//    [Geocoder] ────→ [카카오맵 API 서버]
//       ↓                    ↓
//       ←────────────── [좌표 반환]
//       ↓
//    [좌표를 hidden 필드에 저장]
//       ↓
//    [지도에 마커 표시]
//       ↓
//    [폼 제출 시 서버로 전송]
//    ```
//
//    ---
//
//    ## 🔍 **주요 함수 관계도**
//    ```
//    initializeFormMap()
//       └─→ 지도 생성
//       └─→ 기존 주소 있으면 searchAddressOnMap() 호출
//
//    searchAddressOnMap()
//       └─→ Geocoder로 주소 검색
//       └─→ 좌표를 폼 필드에 저장
//       └─→ 지도에 마커 표시
//
//    handleMainImageSelect()
//       └─→ 파일 유효성 검사
//       └─→ mainImageFile에 저장
//       └─→ displayMainImagePreview() 호출
//
//    displayMainImagePreview()
//       └─→ FileReader로 Base64 변환
//       └─→ HTML 생성하여 미리보기 표시
//
//    handleAdditionalImagesSelect()
//       └─→ 여러 파일 유효성 검사
//       └─→ additionalImageFiles에 추가
//       └─→ displayAdditionalImagesPreview() 호출
//
//    validateForm()
//       └─→ 신규 등록인지 확인
//       └─→ 대표 이미지 필수 체크
//       └─→ 통과하면 폼 제출
//    ```
//
//    ---
//
//    ## 🆚 **다른 파일들과의 비교**
//
//    | 파일 | 주요 기능 | 외부 API | 복잡도 |
//    |------|----------|---------|--------|
//    | **board-form.js** | 게시글 작성 | ❌ | ⭐⭐⭐ |
//    | **bookmark-list.js** | 북마크 목록 | ❌ | ⭐⭐⭐⭐ |
//    | **restaurant-detail.js** | 맛집 상세 | ❌ | ⭐⭐⭐⭐ |
//    | **restaurant-form.js** | 맛집 등록/수정 | ✅ 카카오맵 | ⭐⭐⭐⭐⭐ |
//
//    **복잡도가 높은 이유**:
//    1. 카카오맵 API 사용
//    2. 여러 이미지 처리
//    3. 파일 업로드 + 미리보기
//    4. 동적 폼 생성
//    5. 다양한 유효성 검사
//
//    ---
//
//    ## 🎨 **실제 사용 예시**
//
//    ### **HTML 구조**
//    ```html
//    <!-- 주소 검색 -->
//    <div>
//        <input type="text" id="addressSearch" placeholder="주소 입력">
//        <button id="searchBtn">검색</button>
//    </div>
//
//    <!-- 지도 -->
//    <div id="formMap" style="width:100%; height:400px;"></div>
//
//    <!-- Hidden 필드 (서버로 전송) -->
//    <input type="hidden" id="address" name="address">
//    <input type="hidden" id="latitude" name="latitude">
//    <input type="hidden" id="longitude" name="longitude">
//
//    <!-- 대표 이미지 -->
//    <input type="file" id="mainImageInput"
//           accept="image/*"
//           onchange="handleMainImageSelect(event)">
//    <div id="mainImagePreview"></div>
//
//    <!-- 추가 이미지 -->
//    <input type="file" multiple
//           accept="image/*"
//           onchange="handleAdditionalImagesSelect(event)">
//    <div id="additionalImagesPreview"></div>
//    ```
//
//    ### **CSS 예시**
//    ```css
//    /* 지도 컨테이너 */
//    #formMap {
//        width: 100%;
//        height: 400px;
//        border: 1px solid #ddd;
//        border-radius: 8px;
//    }
//
//    /* 이미지 미리보기 그리드 */
//    .image-preview {
//        display: grid;
//        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
//        gap: 16px;
//    }
//
//    .image-preview-item {
//        position: relative;
//        border: 1px solid #ddd;
//        border-radius: 8px;
//        padding: 8px;
//    }
//
//    .image-preview-item img {
//        width: 100%;
//        height: 200px;
//        object-fit: cover;
//        cursor: pointer;
//        border-radius: 4px;
//    }
//
//    /* 대표 이미지 뱃지 */
//    .badge {
//        position: absolute;
//        top: 16px;
//        right: 16px;
//    }
//    ```
//
//    ---
//
//    ## 🐛 **자주 발생하는 이슈와 해결**
//
//    ### **이슈 1: 카카오맵이 안 나와요**
//    ```javascript
//    // 문제: kakao가 undefined
//    if (typeof kakao === 'undefined') {
//        console.error('카카오맵 API 로드 안됨');
//    }
//
//    // 해결: HTML에 스크립트 추가
//    <script src="//dapi.kakao.com/v2/maps/sdk.js?appkey=YOUR_APP_KEY&libraries=services"></script>
//    ```
//
//    ### **이슈 2: 이미지 미리보기가 안 나와요**
//    ```javascript
//    // 문제: FileReader를 동기적으로 사용
//    const reader = new FileReader();
//    reader.readAsDataURL(file);
//    console.log(reader.result); // null! (아직 안 읽음)
//
//    // 해결: onload 콜백 사용
//    reader.onload = function(e) {
//        console.log(e.target.result); // Base64 문자열
//    };
//    reader.readAsDataURL(file);
//    ```
//
//    ### **이슈 3: 여러 이미지 선택이 안 돼요**
//    ```html
//    <!-- 문제: multiple 속성 없음 -->
//    <input type="file">
//
//    <!-- 해결: multiple 추가 -->
//    <input type="file" multiple>
//    ```
//
//    ### **이슈 4: 파일 크기가 너무 커요**
//    ```javascript
//    // 문제: 제한 없이 업로드 시도
//    const file = event.target.files[0];
//
//    // 해결: 크기 체크 후 거부
//    const maxSize = 10 * 1024 * 1024; // 10MB
//    if (file.size > maxSize) {
//        alert('파일이 너무 큽니다');
//        event.target.value = ''; // 초기화
//        return;
//    }
//    ```
//
//    ---
//
//    ## 💻 **실전 팁**
//
//    ### **Tip 1: 이미지 압축**
//    ```javascript
//    // 실제 프로젝트에서는 이미지를 압축해서 업로드
//    function compressImage(file) {
//        return new Promise((resolve) => {
//            const reader = new FileReader();
//            reader.onload = (e) => {
//                const img = new Image();
//                img.onload = () => {
//                    const canvas = document.createElement('canvas');
//                    const ctx = canvas.getContext('2d');
//
//                    // 최대 크기 설정
//                    const maxWidth = 1200;
//                    const maxHeight = 1200;
//                    let width = img.width;
//                    let height = img.height;
//
//                    if (width > height) {
//                        if (width > maxWidth) {
//                            height *= maxWidth / width;
//                            width = maxWidth;
//                        }
//                    } else {
//                        if (height > maxHeight) {
//                            width *= maxHeight / height;
//                            height = maxHeight;
//                        }
//                    }
//
//                    canvas.width = width;
//                    canvas.height = height;
//                    ctx.drawImage(img, 0, 0, width, height);
//
//                    canvas.toBlob(resolve, 'image/jpeg', 0.8);
//                };
//                img.src = e.target.result;
//            };
//            reader.readAsDataURL(file);
//        });
//    }
//    ```
//
//    ### **Tip 2: 드래그 앤 드롭**
//    ```javascript
//    // 파일을 드래그해서 업로드
//    const dropZone = document.getElementById('dropZone');
//
//    dropZone.addEventListener('dragover', (e) => {
//        e.preventDefault();
//        dropZone.classList.add('drag-over');
//    });
//
//    dropZone.addEventListener('dragleave', () => {
//        dropZone.classList.remove('drag-over');
//    });
//
//    dropZone.addEventListener('drop', (e) => {
//        e.preventDefault();
//        dropZone.classList.remove('drag-over');
//
//        const files = Array.from(e.dataTransfer.files);
//        // handleAdditionalImagesSelect() 로직 사용
//    });
//    ```
//
//    ### **Tip 3: 진행률 표시**
//    ```javascript
//    // 파일 업로드 진행률
//    function uploadWithProgress(file) {
//        const xhr = new XMLHttpRequest();
//
//        xhr.upload.addEventListener('progress', (e) => {
//            if (e.lengthComputable) {
//                const percent = (e.loaded / e.total) * 100;
//                console.log(`업로드 진행률: ${percent}%`);
//                // 프로그레스 바 업데이트
//            }
//        });
//
//        xhr.open('POST', '/upload');
//        xhr.send(formData);
//    }
//    ```
//
//    ---
//
//    ## 🎓 **학습 포인트**
//
//    이 파일에서 배울 수 있는 것들:
//
//    1. **외부 API 통합** (카카오맵)
//    2. **파일 처리** (FileReader, 유효성 검사)
//    3. **비동기 작업** (지오코딩)
//    4. **동적 DOM 생성** (미리보기, 폼)
//    5. **상태 관리** (전역 변수)
//    6. **사용자 경험** (미리보기, 유효성 검사)
//
//    ---
//
//    ## 🚀 **개선 아이디어**
//
//    ### **1. 이미지 순서 변경**
//    ```javascript
//    function moveImage(index, direction) {
//        const newIndex = index + direction;
//        if (newIndex < 0 || newIndex >= additionalImageFiles.length) {
//            return;
//        }
//
//        // 배열에서 위치 교환
//        [additionalImageFiles[index], additionalImageFiles[newIndex]] =
//        [additionalImageFiles[newIndex], additionalImageFiles[index]];
//
//        displayAdditionalImagesPreview();
//    }
//    ```
//
//    ### **2. 주소 자동완성**
//    ```javascript
//    // 카카오맵 장소 검색 API 활용
//    const ps = new kakao.maps.services.Places();
//
//    function searchPlaces(keyword) {
//        ps.keywordSearch(keyword, (data, status) => {
//            if (status === kakao.maps.services.Status.OK) {
//                // 자동완성 목록 표시
//                displayAutoComplete(data);
//            }
//        });
//    }
//    ```
//
//    ### **3. 이미지 크롭(자르기)**
//    ```javascript
//    // Cropper.js 같은 라이브러리 활용
//    function cropImage(file) {
//        const cropper = new Cropper(imageElement, {
//            aspectRatio: 16 / 9,
//            crop(event) {
//                // 크롭 영역 정보
//            }
//        });
//    }
//    ```
//
//    ---
//
//    ## 📝 **전체 요약**
//
//    **restaurant-form.js의 핵심 기능**:
//
//    1. ✅ **카카오맵 통합**
//       - 주소 검색 → 좌표 변환
//       - 지도에 마커 표시
//       - 좌표를 폼 필드에 저장
//
//    2. ✅ **이미지 처리**
//       - 대표 이미지 1개 (필수)
//       - 추가 이미지 여러 개 (선택)
//       - 파일 유효성 검사 (타입, 크기)
//       - Base64 미리보기
//
//    3. ✅ **사용자 경험**
//       - 실시간 미리보기
//       - 이미지 확대 모달
//       - 폼 검증
//       - 스크롤 위치 조정
//
//    4. ✅ **보안**
//       - CSRF 토큰 처리
//       - 파일 타입 검증
//       - 크기 제한
//
//    ---
//
//    지금까지 **8개의 JavaScript 파일**을 모두 설명했어요! 🎉
//
//    1. admin.js - 관리자 페이지
//    2. board-detail.js - 게시글 상세
//    3. board-form.js - 게시글 작성
//    4. board-list.js - 게시글 목록
//    5. bookmark-list.js - 북마크 목록
//    6. navbar.js - 네비게이션
//    7. restaurant-detail.js - 맛집 상세
//    8. restaurant-form.js - 맛집 등록/수정
