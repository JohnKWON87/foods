// ========== 이미지 모달 관련 기능 ==========
//📌 전체 구조
//
//이미지 모달: 이미지를 크게 확대해서 볼 수 있는 팝업
//북마크 기능: 맛집을 즐겨찾기에 추가/제거
/**
 * 이미지 모달 열기
 * @param {string} imagePath - 확대할 이미지 경로
 */
function openImageModal(imagePath) {
    const modal = document.getElementById('imageModal');
    const modalImage = document.getElementById('modalImage');

    if (modal && modalImage) {
        modalImage.src = imagePath;             // 이미지 경로 설정
        modal.classList.add('active');          // 모달 보이기

        // body 스크롤 방지
        document.body.style.overflow = 'hidden';

        console.log('이미지 모달 열림:', imagePath);
    }
//    **역할**: 이미지를 클릭하면 전체 화면으로 확대
//
//    **동작 흐름**:
//    ```
//    [맛집 상세 페이지]
//    ┌────────────────┐
//    │   🍕 피자집     │
//    │                │
//    │ [작은 이미지] ← 클릭!
//    │   (200x200)    │
//    └────────────────┘
//
//    ↓ openImageModal('/images/pizza.jpg') 실행
//
//    [모달 팝업]
//    ┌────────────────────────────────┐
//    │                                │
//    │                                │
//    │         [큰 이미지]             │ ← 전체 화면
//    │        (800x800)               │
//    │                                │
//    │                                │
//    └────────────────────────────────┘
//    document.body.style.overflow = 'hidden' 설명:
//    javascript// 스크롤 방지
//    document.body.style.overflow = 'hidden';
//
//    // 왜 필요한가요?
//    // 모달이 열렸을 때 뒤 페이지가 스크롤되면 이상하잖아요!
//    실제 사용 예시:
//    html<!-- 작은 이미지 -->
//    <img src="/images/pizza.jpg"
//         onclick="openImageModal('/images/pizza.jpg')"
//         style="cursor: pointer;">
}

/**
 * 이미지 모달 닫기
 */
function closeImageModal() {
    const modal = document.getElementById('imageModal');

    if (modal) {
        modal.classList.remove('active');           // 모달 숨기기

        // body 스크롤 복원
        document.body.style.overflow = 'auto';

        console.log('이미지 모달 닫힘');
//        역할: 모달을 닫고 원래 상태로 복원
//        스크롤 복원:
//        javascriptdocument.body.style.overflow = 'auto'; // 다시 스크롤 가능하게
//        HTML 예시:
//        html<div id="imageModal" onclick="closeImageModal()">
//            <img id="modalImage" src="">
//        </div>
    }
}

/**
 * ESC 키로 모달 닫기
 */
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeImageModal();
    }
//    **역할**: ESC 키를 누르면 모달이 닫힘
//
//    **사용자 경험 개선**:
//    ```
//    사용자: 이미지 확대 → ESC 키 → 모달 닫힘
//    (마우스로 X 버튼 클릭 안 해도 됨!)
//    키보드 이벤트 종류:
//    javascriptevent.key === 'Escape'   // ESC 키
//    event.key === 'Enter'    // 엔터 키
//    event.key === 'ArrowUp'  // 위 화살표
//    event.key === 'a'        // a 키
});

// ========== 북마크 기능 ==========

/**
 * 북마크 기능 초기화
 */
function initBookmark() {
    const bookmarkBtn = document.getElementById('bookmarkBtn');
    if (!bookmarkBtn) {
        console.log('북마크 버튼을 찾을 수 없습니다.');
        return;
    }

    const restaurantId = bookmarkBtn.dataset.restaurantId;
    console.log('북마크 초기화 - 레스토랑 ID:', restaurantId);

    // 북마크 상태 확인
    checkBookmarkStatus(restaurantId);

    // 북마크 버튼 클릭 이벤트
    bookmarkBtn.addEventListener('click', async function() {
        console.log('북마크 버튼 클릭됨!');
        await toggleBookmark(restaurantId);
    });
//    역할: 페이지 로드 시 북마크 기능 준비
//    HTML 예시:
//    html<button id="bookmarkBtn" data-restaurant-id="123">
//        <span class="btn-text">북마크</span>
//    </button>
//    <span id="bookmarkCount">15</span>명이 북마크함
//    ```
//
//    **초기화 순서**:
//    ```
//    1. 북마크 버튼 찾기
//        ↓
//    2. 레스토랑 ID 가져오기 (data-restaurant-id="123")
//        ↓
//    3. 서버에서 현재 북마크 상태 확인
//        - 내가 이미 북마크 했나?
//        - 총 몇 명이 북마크 했나?
//        ↓
//    4. 클릭 이벤트 등록
}

/**
 * 북마크 상태 확인
 */
async function checkBookmarkStatus(restaurantId) {
    try {
        console.log('북마크 상태 확인 중...');
        // GET 요청으로 상태 확인
        const response = await fetch(`/api/bookmarks/check?restaurantId=${restaurantId}`);
        const data = await response.json();

        console.log('북마크 상태:', data);
        updateBookmarkUI(data.isBookmarked, data.bookmarkCount);
    } catch (error) {
        console.error('북마크 상태 확인 오류:', error);
//        **역할**: 서버에서 현재 북마크 상태를 가져옴
//
//        **요청 URL 예시**:
//        ```
//        GET /api/bookmarks/check?restaurantId=123
//        서버 응답 예시:
//        json{
//            "isBookmarked": true,      // 내가 북마크 했는지
//            "bookmarkCount": 15        // 총 북마크 수
//        }
//        ```
//
//        **동작 흐름**:
//        ```
//        1. 서버에 요청: "123번 맛집, 내가 북마크 했나요?"
//            ↓
//        2. 서버 응답: "네, 했어요. 총 15명이 북마크했어요."
//            ↓
//        3. UI 업데이트:
//           - 버튼: "북마크" → "북마크됨" (하트 채워짐)
//           - 개수: "15"
    }
}

/**
 * 북마크 토글
 */
async function toggleBookmark(restaurantId) {
    const bookmarkBtn = document.getElementById('bookmarkBtn');

    // 버튼 비활성화(중복 클릭 방지)
    bookmarkBtn.disabled = true;
    console.log('북마크 토글 시작...');

    try {
         // 🔥 CSRF 토큰 가져오기
                const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

                const headers = {
                    'Content-Type': 'application/x-www-form-urlencoded',
                };

                // CSRF 토큰이 있으면 헤더에 추가
                if (csrfToken && csrfHeader) {
                    headers[csrfHeader] = csrfToken;
                }

        // POST 요청으로 북마크 토글
        const response = await fetch('/api/bookmarks/toggle', {
            method: 'POST',
            headers: headers,
            body: `restaurantId=${restaurantId}`
        });

        console.log('응답 상태:', response.status);
        const data = await response.json();
        console.log('응답 데이터:', data);
        /* 역할: 북마크 추가/제거 요청
           서버 응답 예시:
           json// 북마크 추가 성공
           {
               "success": true,
               "isBookmarked": true,
               "bookmarkCount": 16,
               "message": "북마크에 추가되었습니다."
           }

           // 북마크 제거 성공
           {
               "success": true,
               "isBookmarked": false,
               "bookmarkCount": 15,
               "message": "북마크가 제거되었습니다."
           } */

        if (data.success) {
        // 성공 시 UI 업데이트
            updateBookmarkUI(data.isBookmarked, data.bookmarkCount);
            showToast(data.message, 'success');
        } else {
        // 401: 로그인 필요
            if (response.status === 401) {
                showToast('로그인이 필요합니다.', 'warning');
                setTimeout(() => {
                    window.location.href = '/login';    // 1.5초 후 로그인 페이지로
                }, 1500);
            } else {
            // 기타 오류
                showToast(data.message || '오류가 발생했습니다.', 'error');
            }
        }
    } catch (error) {
        console.error('북마크 토글 오류:', error);
        showToast('네트워크 오류가 발생했습니다.', 'error');
    } finally {
        bookmarkBtn.disabled = false; // 버튼 다시 활성화
    }
    /* **역할**: 서버 응답에 따라 처리

       **응답 코드별 처리**:
       ```
       ✅ 200 + success: true
       → UI 업데이트 + 성공 메시지

       ❌ 401 (Unauthorized)
       → "로그인이 필요합니다"
       → 1.5초 후 로그인 페이지로 이동

       ❌ 기타 오류
       → 오류 메시지 표시

       ❌ 네트워크 오류
       → "네트워크 오류가 발생했습니다"
       finally 블록:
       javascripttry {
           // 실행
       } catch (error) {
           // 오류 처리
       } finally {
           // 성공이든 실패든 무조건 실행!
           bookmarkBtn.disabled = false;
       } */
}

/**
 * 북마크 UI 업데이트
 */
function updateBookmarkUI(isBookmarked, count) {
    const bookmarkBtn = document.getElementById('bookmarkBtn');
    const bookmarkCount = document.getElementById('bookmarkCount');
    const btnText = bookmarkBtn.querySelector('.btn-text');

    console.log('UI 업데이트 - 북마크:', isBookmarked, '개수:', count);

    if (isBookmarked) {
        bookmarkBtn.classList.add('active');
        btnText.textContent = '북마크됨';       // ♥ 채워진 하트
    } else {
        bookmarkBtn.classList.remove('active');
        btnText.textContent = '북마크';        // ♡ 빈 하트
    }

    if (bookmarkCount) {
        bookmarkCount.textContent = count;
    }
    /* **역할**: 북마크 상태에 따라 버튼과 개수 업데이트

       **UI 변화**:
       ```
       [북마크 안 됨]
       ┌─────────────────┐
       │ ♡ 북마크        │  15명이 북마크함
       └─────────────────┘

       ↓ 클릭!

       [북마크 됨]
       ┌─────────────────┐
       │ ♥ 북마크됨      │  16명이 북마크함
       └─────────────────┘
       CSS 예시:
       css 기본 상태
       #bookmarkBtn {
           color: #999;
       }

        북마크됨 상태
       #bookmarkBtn.active {
           color: #f44336;  빨간색
           font-weight: 700;
       } */
}

/**
 * 토스트 메시지 표시
 */
function showToast(message, type = 'info') {
    // 기존 토스트 제거
    const existingToast = document.querySelector('.toast');
    if (existingToast) {
        existingToast.remove();
    }

    // 새 토스트 생성
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.textContent = message;

    // 스타일 직접 적용
    toast.style.cssText = `
        position: fixed;
        bottom: 30px;
        left: 50%;
        transform: translateX(-50%);
        padding: 16px 24px;
        background: ${type === 'success' ? '#4caf50' : type === 'error' ? '#f44336' : type === 'warning' ? '#ff9800' : '#333'};
        color: white;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 10000;
        font-size: 15px;
        font-weight: 500;
        opacity: 0;
        transition: opacity 0.3s;
    `;

    document.body.appendChild(toast);

    // 애니메이션
    setTimeout(() => toast.style.opacity = '1', 10);

    // 3초 후 제거
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 3000);

    /* 역할: bookmark-list.js와 동일하지만 스타일이 인라인으로 적용됨
       타입별 색상:

       success: 녹색 (#4caf50)
       error: 빨간색 (#f44336)
       warning: 주황색 (#ff9800)
       info: 회색 (#333)

       인라인 스타일 사용 이유:
       javascript// CSS 파일 없이도 작동
       toast.style.cssText = `...`;  // 모든 스타일을 한 번에 적용 */
}

/**
 * 페이지 로드 시 초기화
 */
document.addEventListener('DOMContentLoaded', function() {
    const modalImage = document.getElementById('modalImage');

    console.log('✅ 상세 페이지 로드 완료');

    // 모달 이미지 클릭 시 이벤트 전파 방지 (모달 닫힘 방지)
    if (modalImage) {
        modalImage.addEventListener('click', function(event) {
            event.stopPropagation();
        });
    }

    console.log('📸 이미지 모달 기능 활성화');

    // 북마크 기능 초기화
    initBookmark();
    console.log('❤️ 북마크 기능 활성화');
});

console.log('✅ restaurant-detail.js 로드 완료');

/* 역할: 페이지 로드 시 모든 기능 활성화
   event.stopPropagation() 설명:
   html<div id="imageModal" onclick="closeImageModal()">  ← 배경
       <img id="modalImage" src="">  ← 이미지
   </div>
   ```

   문제 상황:
   ```
   사용자: 이미지를 클릭
       ↓
   이미지 클릭 이벤트 발생
       ↓
   이벤트가 배경(modal)으로 전파
       ↓
   배경의 closeImageModal() 실행
       ↓
   모달이 닫혀버림! (의도하지 않음)
   해결:
   javascriptmodalImage.addEventListener('click', function(event) {
       event.stopPropagation();  // 여기서 멈춤!
   });

   // 이제 이미지 클릭해도 모달 안 닫힘!
   ```

   ---

   ## 🎯 **전체 시나리오**

   ### **시나리오 1: 이미지 확대 보기**
   ```
   1. 사용자: 맛집 상세 페이지 접속
      ┌────────────┐
      │ 🍕 피자집   │
      │ [이미지]    │ ← 작은 이미지
      └────────────┘

   2. 사용자: 이미지 클릭
      → openImageModal('/images/pizza.jpg')
      ↓
   3. 모달 열림 (전체 화면)
      ┌──────────────────┐
      │                  │
      │   [큰 이미지]    │
      │                  │
      └──────────────────┘
      - 뒤 페이지 스크롤 방지

   4. 사용자: ESC 키 or 배경 클릭
      → closeImageModal()
      ↓
   5. 모달 닫힘, 원래 페이지로 복귀
   ```

   ### **시나리오 2: 북마크 추가**
   ```
   1. 페이지 로드
      → initBookmark() 실행
      → checkBookmarkStatus(123)
      ↓
   2. 서버: "북마크 안 됨, 총 15명"
      → UI: ♡ 북마크 | 15명

   3. 사용자: [북마크] 버튼 클릭
      → toggleBookmark(123)
      → 버튼 비활성화 (중복 방지)
      ↓
   4. 서버에 POST 요청
      ↓
   5. 서버: "추가 완료!"
      {
        "success": true,
        "isBookmarked": true,
        "bookmarkCount": 16
      }
      ↓
   6. UI 업데이트
      - 버튼: ♥ 북마크됨
      - 개수: 16명
      - 토스트: "북마크에 추가되었습니다"
      ↓
   7. 버튼 다시 활성화
   ```

   ### **시나리오 3: 로그인 안 한 사용자**
   ```
   1. 비로그인 사용자: [북마크] 클릭
      ↓
   2. 서버 응답: 401 Unauthorized
      ↓
   3. 토스트: "로그인이 필요합니다"
      ↓
   4. 1.5초 후 로그인 페이지로 자동 이동

   💡 핵심 개념 정리
   1. 모달 패턴
   javascript// 열기
   modal.classList.add('active');
   document.body.style.overflow = 'hidden';

   // 닫기
   modal.classList.remove('active');
   document.body.style.overflow = 'auto';
   2. 이벤트 전파 제어
   javascriptevent.stopPropagation();  // 부모로 전파 막기
   event.preventDefault();    // 기본 동작 막기
   3. GET vs POST
   javascript// GET: 데이터 조회
   fetch(`/api/bookmarks/check?id=${id}`);

   // POST: 데이터 변경
   fetch('/api/bookmarks/toggle', {
       method: 'POST',
       body: '...'
   });
   4. HTTP 상태 코드

   200: 성공
   401: 인증 필요 (로그인 필요)
   404: 없음
   500: 서버 오류

   5. 템플릿 리터럴 조건부 표현식
   javascript// 삼항 연산자 중첩
   const color = type === 'success' ? 'green' :
                 type === 'error' ? 'red' :
                 'gray';

   🔄 파일 비교
   파일                       주요 기능                      서버 통신                난이도
   bookmark-list.js         북마크 목록 관리                 ✅ POST              ⭐⭐⭐⭐
   restaurant-detail.js     이미지 모달 + 북마크             ✅ GET/POST          ⭐⭐⭐⭐
   공통점:

   둘 다 북마크 기능
   CSRF 토큰 처리
   토스트 메시지

   차이점:

   bookmark-list.js: 북마크 제거만
   restaurant-detail.js: 북마크 추가/제거 + 이미지 모달 */