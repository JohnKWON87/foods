// 북마크 목록 페이지 기능
//📌 전체 구조
//북마크 목록 페이지에서 북마크를 제거할 수 있는 기능을 담당합니다.
//특징: 페이지 새로고침 없이 실시간으로 처리 (AJAX)

document.addEventListener('DOMContentLoaded', function() {
    // 모든 북마크 버튼에 이벤트 리스너 추가
    const bookmarkButtons = document.querySelectorAll('.btn-bookmark');

    bookmarkButtons.forEach(button => {
        button.addEventListener('click', handleBookmarkToggle);
//        **역할**: 모든 북마크 버튼에 클릭 이벤트 연결
//
//        **화면 예시**:
//        ```
//        [북마크 목록]
//        ┌─────────────────┐  ┌─────────────────┐
//        │  🍕 피자집       │  │  🍔 햄버거집     │
//        │  [북마크 제거]    │  │  [북마크 제거]   │
//        └─────────────────┘  └─────────────────┘
//                 ↑                    ↑
//            클릭 가능            클릭 가능
    });
});

/**
 * 북마크 토글 처리 (핵심!)
 */
async function handleBookmarkToggle(event) {
    event.preventDefault();             // 기본 동작 막기 (폼 제출, 링크 이동 등)
    event.stopPropagation();            // 이벤트 전파 막기 (부모 요소로 안 올라감)

    const button = event.currentTarget;                 // 클릭된 버튼
    const restaurantId = button.dataset.restaurantId;   // data-restaurant-id 값
    const card = button.closest('.bookmark-card');      // 버튼이 속한 카드 전체

    // 버튼 비활성화 (중복 클릭 방지)
    button.disabled = true;

//    async function 이란?
//
//    비동기 함수: 서버와 통신하는 동안 다른 작업 가능
//    await 키워드를 사용할 수 있음
//
//    event.stopPropagation() 설명:
//    html<div class="bookmark-card" onclick="goToDetail()">  ← 부모
//        <button class="btn-bookmark">제거</button>  ← 자식
//    </div>
//    ```
//
//    만약 `stopPropagation()` 없으면:
//    ```
//    1. 버튼 클릭
//    2. 북마크 제거 실행
//    3. 이벤트가 카드로 전파
//    4. 상세 페이지로 이동 (원치 않는 동작!)
//    ```
//
//    `stopPropagation()` 있으면:
//    ```
//    1. 버튼 클릭
//    2. 북마크 제거 실행
//    3. 이벤트 전파 중단! ✋
//    4. 끝!
//    button.dataset.restaurantId 설명:
//    html<button data-restaurant-id="123">제거</button>
//    javascriptbutton.dataset.restaurantId // "123"

    try {
        // 🔥 CSRF 토큰 가져오기
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

        const headers = {
            'Content-Type': 'application/x-www-form-urlencoded',
        };
//        **CSRF란?** (Cross-Site Request Forgery)
//        - 악의적인 사이트에서 당신의 계정으로 요청을 보내는 공격
//        - 예: 해커가 "북마크 삭제" 요청을 몰래 보냄
//
//        **CSRF 토큰 동작 원리**:
//        ```
//        [서버가 HTML에 토큰 심어둠]
//        <meta name="_csrf" content="abc123xyz">
//        <meta name="_csrf_header" content="X-CSRF-TOKEN">
//
//        [JavaScript가 토큰을 읽어서 요청에 포함]
//        headers: {
//            'X-CSRF-TOKEN': 'abc123xyz'  ← 이게 없으면 서버가 거부!
//        }
//
//        [서버가 토큰 검증]
//        "이 토큰이 내가 준 거 맞네? OK!"
//        ?. 연산자 (Optional Chaining):
//        javascript// 일반 방식 (에러 가능성)
//        const token = document.querySelector('meta[name="_csrf"]').content;
//        // querySelector가 null이면 → 에러!
//
//        // 안전한 방식
//        const token = document.querySelector('meta[name="_csrf"]')?.content;
//        // querySelector가 null이면 → undefined (에러 없음)

        // CSRF 토큰이 있으면 헤더에 추가
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        const response = await fetch('/api/bookmarks/toggle', {
            method: 'POST',                          // POST 방식
            headers: headers,                       // 위에서 만든 헤더
            body: `restaurantId=${restaurantId}`    // 전송할 데이터
        });

        const data = await response.json();         // 서버 응답을 JSON으로 변환
//        fetch API 설명:
//        javascript// 기본 구조
//        fetch('URL', {
//            method: 'POST',           // GET, POST, PUT, DELETE 등
//            headers: { ... },         // 요청 헤더
//            body: '데이터'            // 보낼 데이터
//        })
//        .then(response => response.json())  // 응답을 JSON으로
//        .then(data => { /* 처리 */ })       // 데이터 사용
//        async/await 방식 (더 깔끔):
//        javascriptconst response = await fetch(...);  // 응답 기다림
//        const data = await response.json(); // JSON 변환 기다림
//        // 이제 data 사용 가능
//        전송 데이터 형식:
//        javascriptbody: `restaurantId=${restaurantId}`
//        // 실제 전송: "restaurantId=123"
//        서버 응답 예시:
//        json{
//            "success": true,
//            "message": "북마크가 제거되었습니다."
//        }

        if (data.success) {
            // 카드 애니메이션 (페이드아웃 + 축소)
            card.style.opacity = '0';
            card.style.transform = 'scale(0.9)';

            setTimeout(() => {
                card.remove();          // 300ms 후 카드 제거

                // 남은 북마크가 있는지 확인
                const remainingCards = document.querySelectorAll('.bookmark-card');
                if (remainingCards.length === 0) {
                    showEmptyState();               // 빈 상태 표시
                } else {
                    updateBookmarkCount(-1);        // 개수 -1
                }
            }, 300);

            showToast(data.message || '북마크가 제거되었습니다.', 'success');
//            **동작 흐름**:
//            ```
//            1. [제거] 버튼 클릭
//                ↓
//            2. 서버에 요청 (await fetch)
//                ↓
//            3. 서버: "OK, 제거했어요"
//                ↓
//            4. 카드가 흐려지고 작아짐 (애니메이션)
//                ↓
//            5. 300ms 후 카드 완전히 사라짐
//                ↓
//            6. 남은 카드 확인
//               - 0개? → "북마크한 맛집이 없습니다" 표시
//               - 있음? → 개수만 업데이트
//                ↓
//            7. 토스트 메시지: "북마크가 제거되었습니다"
//            애니메이션 설명:
//            css/* 초기 상태 */
//            card {
//                opacity: 1;
//                transform: scale(1);
//            }
//
//            /* JavaScript로 변경 */
//            card {
//                opacity: 0;           /* 투명해짐 */
//                transform: scale(0.9); /* 90% 크기로 축소 */
//                transition: 0.3s;     /* 0.3초 동안 부드럽게 */
//            }
        } else {
            showToast(data.message || '오류가 발생했습니다.', 'error');
            button.disabled = false;            // 버튼 다시 활성화
        }
    } catch (error) {
        console.error('북마크 토글 오류:', error);
        showToast('네트워크 오류가 발생했습니다.', 'error');
        button.disabled = false;
//        **에러 처리 단계**:
//        ```
//        1. 서버가 "실패" 응답 → else 블록
//        2. 네트워크 오류 (인터넷 끊김 등) → catch 블록
//        ```
//
//        **왜 `button.disabled = false`?**
//        ```
//        [실패 시]
//        사용자: [제거] 클릭
//        시스템: 오류 발생!
//        버튼: 다시 활성화 ← 재시도할 수 있게!
//
//        [활성화 안 하면]
//        버튼이 계속 비활성화 상태
//        → 사용자가 다시 시도할 수 없음 (나쁜 UX)
    }
}

/**
 * 빈 상태 표시
 */
function showEmptyState() {
    const bookmarkGrid = document.querySelector('.bookmark-grid');
    if (bookmarkGrid) {
        bookmarkGrid.innerHTML = `
            <div class="empty-state" style="grid-column: 1 / -1;">
                <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
                </svg>
                <h2>북마크한 맛집이 없습니다</h2>
                <p>마음에 드는 맛집을 북마크해보세요!</p>
                <a href="/restaurants" class="btn-primary">맛집 둘러보기</a>
            </div>
        `;
    }
    updateBookmarkCount(0, true);
    **역할**: 북마크가 모두 제거되면 빈 화면 표시

//    **화면 변화**:
//    ```
//    [모든 북마크 제거 전]
//    ┌─────────────────┐
//    │  🍕 마지막 북마크│
//    │  [제거]         │
//    └─────────────────┘
//
//    ↓ [제거] 클릭
//
//    [모든 북마크 제거 후]
//    ┌─────────────────────┐
//    │       💔            │
//    │ 북마크한 맛집이     │
//    │    없습니다         │
//    │                     │
//    │  [맛집 둘러보기]    │
//    └─────────────────────┘
//    innerHTML 사용:
//
//    기존 내용을 모두 지우고 새로운 HTML로 교체
//    템플릿 리터럴(백틱 `)로 여러 줄 HTML 작성 가능
//
//    style="grid-column: 1 / -1":
//
//    CSS Grid에서 전체 너비 사용
//    1번 컬럼부터 마지막(-1) 컬럼까지 차지
}

/**
 * 북마크 개수 업데이트
 */
function updateBookmarkCount(delta, setZero = false) {
    const countElement = document.querySelector('.bookmark-count span');
    if (countElement) {
        if (setZero) {
            countElement.textContent = '0';
        } else {
            const currentCount = parseInt(countElement.textContent) || 0;
            const newCount = Math.max(0, currentCount + delta);
            countElement.textContent = newCount;
        }
//        역할: 화면 상단의 북마크 개수 업데이트
//        사용 예시:
//        javascript// 북마크 1개 제거
//        updateBookmarkCount(-1);  // 15 → 14
//
//        // 북마크 1개 추가
//        updateBookmarkCount(1);   // 14 → 15
//
//        // 0으로 초기화
//        updateBookmarkCount(0, true); // → 0
//        로직 설명:
//        javascriptconst currentCount = parseInt(countElement.textContent) || 0;
//        // "15" → 15 (숫자로 변환)
//        // "" → 0 (빈 문자열이면 0)
//        // "abc" → 0 (숫자 아니면 0)
//
//        const newCount = Math.max(0, currentCount + delta);
//        // Math.max(0, 10) → 10
//        // Math.max(0, -5) → 0 (음수 방지!)
//        ```
//
//        **화면 변화**:
//        ```
//        [상단 헤더]
//        내 북마크 (15)  ← updateBookmarkCount(-1) 호출
//               ↓
//        내 북마크 (14)
    }
}

/**
 * 토스트 메시지 표시
 */
function showToast(message, type = 'info') {
    // 1. 기존 토스트 제거
    const existingToast = document.querySelector('.toast');
    if (existingToast) {
        existingToast.remove();
    }

    // 2. 새 토스트 생성
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;            // toast toast-success
    toast.textContent = message;

    // 3. body에 추가
    document.body.appendChild(toast);

    // 4. 애니메이션 (페이드인)
    setTimeout(() => toast.classList.add('show'), 10);

    // 5. 3초 후 제거
    setTimeout(() => {
        toast.classList.remove('show');             // 페이드아웃
        setTimeout(() => toast.remove(), 300);      // 완전히 제거
    }, 3000);
}
//역할: 화면 하단에 알림 메시지 표시
//사용 예시:
//javascriptshowToast('북마크가 제거되었습니다.', 'success');
//showToast('오류가 발생했습니다.', 'error');
//showToast('로딩 중...', 'info');
//```
//
//**동작 흐름**:
//```
//1. 기존 토스트가 있으면 삭제 (중복 방지)
//    ↓
//2. 새 div 요소 생성
//    ↓
//3. body 맨 아래에 추가
//    ↓
//4. 10ms 후 'show' 클래스 추가 (페이드인)
//    ↓
//5. 3초 동안 표시
//    ↓
//6. 'show' 클래스 제거 (페이드아웃)
//    ↓
//7. 300ms 후 완전히 제거
//왜 10ms 후에 'show' 추가?
//javascript// 바로 추가하면 애니메이션 안 보임
//toast.classList.add('show');
//
//// 10ms 지연 → 브라우저가 렌더링할 시간 줌
//setTimeout(() => toast.classList.add('show'), 10);
//CSS 예시:
//css.toast {
//    opacity: 0;
//    transform: translateY(20px);
//    transition: 0.3s;
//}
//
//.toast.show {
//    opacity: 1;
//    transform: translateY(0);
//}
//```
//
//**화면 예시**:
//```
//[화면 하단]
//┌─────────────────────────────┐
//│ ✅ 북마크가 제거되었습니다. │ ← 슈욱 올라오며 나타남
//└─────────────────────────────┘
//         3초 후 사라짐 ↓
//```
//
//---
//
//## 🎯 **전체 시나리오**
//
//### **시나리오: 북마크 제거**
//```
//1. 페이지 로드
//   → 북마크 카드 3개 표시
//   → 상단: "내 북마크 (3)"
//
//2. 사용자: 첫 번째 카드의 [제거] 버튼 클릭
//   ↓
//3. handleBookmarkToggle() 실행
//   - button.disabled = true (중복 클릭 방지)
//   - CSRF 토큰 가져오기
//   - 서버에 POST 요청
//
//4. 서버 응답 대기... (await)
//   ↓
//5. 서버: { success: true, message: "제거 완료" }
//   ↓
//6. 카드 애니메이션 (흐려지고 작아짐)
//   ↓
//7. 300ms 후 카드 제거
//   ↓
//8. 남은 카드 확인: 2개
//   → updateBookmarkCount(-1) 호출
//   → "내 북마크 (2)"로 변경
//   ↓
//9. 토스트 메시지: "북마크가 제거되었습니다."
//   → 화면 하단에 3초간 표시
//
//💡 핵심 개념 정리
//1. async/await
//javascript// 전통적인 방식 (콜백 지옥)
//fetch('/api')
//    .then(response => response.json())
//    .then(data => {
//        // 처리
//    });
//
//// 현대적인 방식 (더 읽기 쉬움)
//const response = await fetch('/api');
//const data = await response.json();
//// 처리
//2. fetch API
//javascript// GET 요청
//fetch('/api/data');
//
//// POST 요청
//fetch('/api/data', {
//    method: 'POST',
//    headers: { 'Content-Type': 'application/json' },
//    body: JSON.stringify({ id: 123 })
//});
//3. 이벤트 제어
//javascriptevent.preventDefault();     // 기본 동작 막기
//event.stopPropagation();   // 이벤트 전파 막기
//4. DOM 조작
//javascript// 요소 찾기
//document.querySelector('.class');
//element.closest('.parent');
//
//// 요소 추가/제거
//element.remove();
//parent.appendChild(child);
//
//// 내용 변경
//element.textContent = '텍스트';
//element.innerHTML = '<div>HTML</div>';
//5. 타이머
//javascriptsetTimeout(() => {
//    // 1초 후 실행
//}, 1000);
//
//🔄 다른 파일들과의 차이점
//특징               이전 파일들                bookmark-list.js
//통신 방식         폼 제출 (새로고침)        fetch (비동기, 새로고침 없음)
//복잡도            기본적인 DOM 조작           서버 통신 + 애니메이션
//보안                  기본                     CSRF 토큰 처리
//사용자 경험         페이지 전환                  실시간 업데이트