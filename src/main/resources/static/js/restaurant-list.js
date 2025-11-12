// ========== 3D 카드 드래그 & 클릭 시스템 ==========
//📌 전체 구조
//맛집 목록을 3D 카드 형태로 표시하고, 드래그/스와이프로 탐색할 수 있는 인터랙티브 UI입니다.
//주요 기능:
//
//3D 카드 레이아웃
//마우스/터치 드래그
//카드 호버 효과
//북마크 기능
//
//1️⃣ DOM 요소 찾기
//역할: 카드 컨테이너와 카드들을 찾기
//HTML 구조 예시:
//html<div class="card-wrapper">
//    <div class="food-card" data-id="1">맛집 1</div>
//    <div class="food-card" data-id="2">맛집 2</div>
//    <div class="food-card" data-id="3">맛집 3</div>
//    <div class="food-card" data-id="4">맛집 4</div>
//    <div class="food-card" data-id="5">맛집 5</div>
//</div>

document.addEventListener('DOMContentLoaded', function() {
    const wrapper = document.querySelector('.card-wrapper');
    const cards = document.querySelectorAll('.food-card');

    if (!wrapper || cards.length === 0) {
        console.warn('카드를 찾을 수 없습니다.');
        return;
    }

    console.log(`총 ${cards.length}개의 맛집 카드 로드됨`);

    // ========== 변수 초기화 ==========
    let currentOffset = 0;              // 현재 이동한 거리
    let isDragging = false;           // 드래그 중인지
    let startX = 0;                  // 드래그 시작 X 좌표
    let dragDelta = 0;              // 드래그 이동량
    let hasMoved = false;           // 실제로 움직였는지 (클릭 구분용)

//    역할: 드래그 상태를 추적하는 변수들
//    변수 설명:
//    javascript// 예시 값들
//    currentOffset = -240;    // 오른쪽으로 240px 이동함
//    isDragging = true;       // 현재 드래그 중
//    startX = 500;           // 마우스 시작 위치
//    dragDelta = -50;        // 왼쪽으로 50px 움직임
//    hasMoved = true;        // 5px 이상 움직였음


    // ========== 카드 위치 계산 설정 (test.html처럼 가깝게) ==========
    const CARD_SPACING = 120;   // 카드 간격 (픽셀)
    const BASE_LEFT = 40;       // 기본 왼쪽 위치 (%)
    const BASE_TOP = 30;        // 기본 위쪽 위치 (%)

    // ========== 카드 위치 업데이트 함수 ==========
    function updateCardPositions() {
        const totalOffset = currentOffset + dragDelta;

        cards.forEach((card, index) => {
            const centerIndex = Math.floor(cards.length / 2);
            const relativeIndex = index - centerIndex;
//
//            역할: 각 카드의 3D 위치를 계산하고 적용
//            중앙 카드 계산:
//            javascript// 카드가 5개일 때
//            cards.length = 5
//            centerIndex = Math.floor(5 / 2) = 2  // 3번째 카드 (index 2)
//
//            // 각 카드의 relativeIndex:
//            index 0: relativeIndex = 0 - 2 = -2  (왼쪽 2칸)
//            index 1: relativeIndex = 1 - 2 = -1  (왼쪽 1칸)
//            index 2: relativeIndex = 2 - 2 =  0  (중앙) ⭐
//            index 3: relativeIndex = 3 - 2 =  1  (오른쪽 1칸)
//            index 4: relativeIndex = 4 - 2 =  2  (오른쪽 2칸)

            // X축 위치
            const baseLeftPx = (window.innerWidth * BASE_LEFT) / 100;
            const leftPx = baseLeftPx + (relativeIndex * CARD_SPACING) + totalOffset;

            // Z축 깊이 (test.html 스타일)
            let zDepth;
            if (relativeIndex === 0) {
                zDepth = 50;                                    // 중앙: 앞으로 50px
            } else if (relativeIndex === -1) {
                zDepth = 30;                                    // 왼쪽 1칸: 앞으로 30px
            } else if (relativeIndex === 1) {
                zDepth = -20;                                   // 오른쪽 1칸: 뒤로 20px
            } else {
                zDepth = 50 - (Math.abs(relativeIndex) * 40);   // 멀수록 뒤로
            }

//            **Z축 깊이 시각화**:
//            ```
//                      사용자
//                        👁️
//                       ↗ ↑ ↖
//                    📄  📄  📄
//                  (뒤) (앞) (중간)
//
//            Z = -20   Z = 50   Z = 30

            // Y축 위치 (test.html 스타일)
            let topPercent;
            if (relativeIndex === 0) {
                topPercent = 33;                                    // 중앙: 33%
            } else if (relativeIndex === -1) {
                topPercent = 29;                                    // 왼쪽: 조금 위
            } else if (relativeIndex === 1) {
                topPercent = 37;                                    // 오른쪽: 조금 아래
            } else {
                topPercent = 33 + (Math.abs(relativeIndex) * 2);    // 멀수록 아래
            }
            const topPx = (window.innerHeight * topPercent) / 100;

//            **Y축 배치 효과**:
//            ```
//            화면 높이 1080px일 때
//
//            왼쪽 카드 (29%):  topPx = 1080 * 0.29 = 313px  ↑ (위)
//            중앙 카드 (33%):  topPx = 1080 * 0.33 = 356px  ─ (중간)
//            오른쪽 카드 (37%): topPx = 1080 * 0.37 = 400px  ↓ (아래)
//            ```
//
//            **물결 효과**:
//            ```
//                 📄        📄        📄
//               (위)     (중간)     (아래)

            // Z-index (중앙이 가장 높게)
            const baseZIndex = 100 - Math.abs(relativeIndex) * 10;

            // 투명도
            const opacity = Math.max(0.5, 1 - (Math.abs(relativeIndex) * 0.15));

            // 스타일 적용
            card.style.left = `${leftPx}px`;
            card.style.top = `${topPx}px`;
            card.style.setProperty('--z', `${zDepth}px`);       // CSS 변수
            card.style.setProperty('--base-z-index', baseZIndex);
            card.style.zIndex = baseZIndex;
            card.style.opacity = opacity;
        });
    }

    // ========== 마우스 드래그 이벤트 ==========
    wrapper.addEventListener('mousedown', (e) => {
        // 배너 클릭은 무시
        if (e.target.closest('.add-restaurant-banner')) return;

        isDragging = true;
        startX = e.clientX;                  // 시작 위치 저장
        dragDelta = 0;
        hasMoved = false;
        wrapper.style.cursor = 'grabbing';  // 커서 변경 (잡는 손)

//        **동작**:
//        ```
//        1. 마우스 버튼 누름
//           → isDragging = true
//           → startX = 500 (마우스 X 좌표)
//           → 커서: grab → grabbing 🖐️ → ✊
    });

    wrapper.addEventListener('mousemove', (e) => {
        if (!isDragging) return;                // 드래그 중이 아니면 무시

        dragDelta = e.clientX - startX;         // 이동 거리 계산
        hasMoved = Math.abs(dragDelta) > 5;     // 5px 이상 움직였는지
        updateCardPositions();                  // 카드 위치 업데이트
    });

    wrapper.addEventListener('mouseup', () => {
        if (!isDragging) return;

        isDragging = false;
        wrapper.style.cursor = 'grab';      // 커서 복원 🖐️
        currentOffset += dragDelta;         // 이동량을 누적
        dragDelta = 0;                      // 드래그 초기화
        updateCardPositions();
    });

    wrapper.addEventListener('mouseleave', () => {
        if (isDragging) {
            isDragging = false;
            wrapper.style.cursor = 'grab';
            currentOffset += dragDelta;
            dragDelta = 0;
            updateCardPositions();
        }
    });

    // ========== 터치 이벤트 (모바일) ==========
    let touchStartX = 0;

    wrapper.addEventListener('touchstart', (e) => {
        if (e.target.closest('.add-restaurant-banner')) return;

        isDragging = true;
        touchStartX = e.touches[0].clientX;     // 첫 번째 터치 위치
        dragDelta = 0;
        hasMoved = false;

//        마우스 vs 터치:
//        javascript// 마우스
//        e.clientX  // X 좌표
//
//        // 터치
//        e.touches[0].clientX  // 첫 번째 터치점 X 좌표
    });

    wrapper.addEventListener('touchmove', (e) => {
        if (!isDragging) return;

        dragDelta = e.touches[0].clientX - touchStartX;
        hasMoved = Math.abs(dragDelta) > 5;
        updateCardPositions();
    });

    wrapper.addEventListener('touchend', () => {
        if (!isDragging) return;

        isDragging = false;
        currentOffset += dragDelta;
        dragDelta = 0;
        updateCardPositions();
    });

    // ========== 카드 호버 시 앞으로 나오기 ==========
    cards.forEach((card) => {
        card.addEventListener('mouseenter', function() {
            // 호버 시 z-index를 최상위로
            this.style.zIndex = '999';
            this.style.setProperty('--z', '100px'); // 앞으로 100px
        });

        card.addEventListener('mouseleave', function() {
            // 호버 해제 시 원래 z-index로 복귀
            const baseZIndex = this.style.getPropertyValue('--base-z-index');
            this.style.zIndex = baseZIndex;
            // z값도 원래대로 (updateCardPositions가 다시 설정함)
            updateCardPositions();
        });

//        **효과**:
//        ```
//        [일반 상태]
//        📄 📄 📄 📄 📄
//           (평평)
//
//        [카드 3에 마우스]
//        📄 📄 🎴 📄 📄
//              ↑
//           (앞으로!)
    });

    // ========== 카드 클릭 이벤트 ==========
    cards.forEach((card, index) => {
        card.addEventListener('click', function(e) {
            if (hasMoved) {
                e.preventDefault();     // 드래그했으면 클릭 무시
                return;

//                드래그 vs 클릭 구분:
//                javascript// 드래그
//                hasMoved = true  → 클릭 무시 (페이지 이동 안 함)
//
//                // 클릭
//                hasMoved = false → 상세 페이지로 이동
            }

            const restaurantId = this.getAttribute('data-id');

            if (restaurantId) {
                console.log(`카드 ${index + 1} 클릭: 식당 ID ${restaurantId}`);
                window.location.href = `/restaurants/${restaurantId}`;
            } else {
                console.error('레스토랑 ID를 찾을 수 없습니다.');
            }
        });
    });

    // ========== 키보드 방향키 지원 ==========
    document.addEventListener('keydown', (e) => {
        if (e.key === 'ArrowLeft') {
            currentOffset += 100;       // 왼쪽 키: 오른쪽으로 이동
            updateCardPositions();
        } else if (e.key === 'ArrowRight') {
            currentOffset -= 100;       // 오른쪽 키: 왼쪽으로 이동
            updateCardPositions();
        }
    });

    // ========== 마우스 휠로 좌우 스크롤 ==========
    wrapper.addEventListener('wheel', (e) => {
    // ✅ 검색 폼 영역은 스크롤 허용
            if (e.target.closest('form')) return;

        e.preventDefault();                 // 기본 스크롤 막기
        currentOffset -= e.deltaY * 0.5;    // 휠 이동량의 50%
        updateCardPositions();
    }, { passive: false });                 // preventDefault 사용 가능

//    e.deltaY 설명:
//    javascript// 휠을 아래로
//    e.deltaY = 100  → currentOffset -= 50  (카드가 왼쪽으로)
//
//    // 휠을 위로
//    e.deltaY = -100 → currentOffset += 50  (카드가 오른쪽으로)
//    passive: false 이유:
//    javascript// passive: true (기본값)
//    // → preventDefault() 사용 불가
//    // → 스크롤을 막을 수 없음
//
//    // passive: false
//    // → preventDefault() 사용 가능
//    // → 스크롤을 막고 카드 이동으로 대체

    // ========== 초기 위치 설정 ==========
    updateCardPositions();

    console.log('✅ 3D 카드 시스템 초기화 완료');
    console.log('📍 카드 간격: 120px (test.html 스타일)');
    console.log('🎮 드래그, 휠, 키보드 모두 사용 가능');
    console.log('🖱️ 카드 호버 시 앞으로 나옴');

    // 북마크 기능 초기화
    initBookmarks();

    // 파일 끝에 함수 추가

    /**
     * 모든 북마크 버튼 초기화
     */
    function initBookmarks() {
        const bookmarkButtons = document.querySelectorAll('.btn-bookmark-card');

        bookmarkButtons.forEach(button => {
            const restaurantId = button.dataset.restaurantId;
            checkBookmarkStatus(restaurantId, button);
            button.addEventListener('click', handleBookmarkClick);
        });
    }

    /**
     * 북마크 버튼 클릭 처리
     */
    async function handleBookmarkClick(event) {
        event.preventDefault();
        event.stopPropagation();        // 카드 클릭 이벤트 방지

        const button = event.currentTarget;
        const restaurantId = button.dataset.restaurantId;
        button.disabled = true;

//        vent.stopPropagation() 중요성:
//        html<div class="food-card" onclick="goToDetail()">  ← 카드 클릭
//            <button class="btn-bookmark" onclick="toggleBookmark()">  ← 북마크
//            </button>
//        </div>
//        javascript// stopPropagation() 없으면:
//        1. 북마크 버튼 클릭
//        2. 북마크 토글 실행
//        3. 이벤트가 카드로 전파
//        4. 상세 페이지로 이동 (원치 않음!)
//
//        // stopPropagation() 있으면:
//        1. 북마크 버튼 클릭
//        2. 북마크 토글 실행
//        3. 이벤트 전파 중단! ✋

        try {

        // 🔥 CSRF 토큰 가져오기
                const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

                const headers = {
                    'Content-Type': 'application/x-www-form-urlencoded'
                };

                if (csrfToken && csrfHeader) {
                    headers[csrfHeader] = csrfToken;
                }

            const response = await fetch('/api/bookmarks/toggle', {
                method: 'POST',
                headers: headers,
                body: `restaurantId=${restaurantId}`
            });

            const data = await response.json();

            if (data.success) {
                button.classList.toggle('active', data.isBookmarked);
                showToastMessage(data.isBookmarked ? '북마크 추가!' : '북마크 제거');
            } else if (response.status === 401) {
                showToastMessage('로그인이 필요합니다');
                setTimeout(() => window.location.href = '/login', 1500);
            }
        } catch (error) {
            console.error('북마크 오류:', error);
        } finally {
            button.disabled = false;
        }
    }

    /**
     * 북마크 상태 확인
     */
    async function checkBookmarkStatus(restaurantId, button) {
        try {
            const response = await fetch(`/api/bookmarks/check?restaurantId=${restaurantId}`);
            const data = await response.json();
            if (data.isBookmarked) button.classList.add('active');
        } catch (error) {
            console.error('북마크 상태 확인 오류:', error);
        }
    }

    /**
     * 간단한 토스트 메시지
     */
    function showToastMessage(message) {
        const toast = document.createElement('div');
        toast.textContent = message;
        toast.style.cssText = `
            position: fixed; bottom: 30px; left: 50%; transform: translateX(-50%);
            padding: 12px 24px; background: rgba(0,0,0,0.8); color: white;
            border-radius: 8px; font-size: 14px; z-index: 10000;
        `;
        document.body.appendChild(toast);
        setTimeout(() => toast.remove(), 2000);
    }
});

//## 🎯 **전체 시나리오**
//
//### **시나리오: 3D 카드 탐색**
//```
//1. 페이지 로드
//   → 5개 카드 표시
//   → 중앙 카드가 가장 앞 (Z = 50)
//   ┌────────────────────────┐
//   │  📄 📄 🎴 📄 📄       │
//   │        ↑ (중앙)         │
//   └────────────────────────┘
//
//2. 마우스 드래그 (왼쪽으로 150px)
//   → dragDelta = -150
//   → 모든 카드가 왼쪽으로 150px 이동
//   ┌────────────────────────┐
//   │ 📄 📄 🎴 📄 📄         │
//   │      ←────             │
//   └────────────────────────┘
//
//3. 드래그 종료
//   → currentOffset = -150
//   → dragDelta = 0
//   → 다음 카드가 중앙으로
//
//4. 카드에 마우스 올림
//   → Z-index = 999
//   → Z = 100px (앞으로 툭!)
//   ┌────────────────────────┐
//   │ 📄 📄 🎴 📄 📄         │
//   │       ↗️               │
//   └────────────────────────┘
//
//5. 카드 클릭 (hasMoved = false)
//   → 상세 페이지로 이동
//
//💡 핵심 개념 정리
//1. 3D Transform
//css.food-card {
//    transform: translateZ(var(--z));
//    /* Z축으로 이동하여 깊이감 표현 */
//}
//2. 상대적 인덱스
//javascript// 중앙을 기준으로 상대적 위치 계산
//relativeIndex = index - centerIndex;
//3. 드래그 상태 관리
//javascriptlet isDragging = false;   // 드래그 중?
//let dragDelta = 0;        // 현재 드래그 이동량
//let currentOffset = 0;    // 누적 이동량
//4. 클릭 vs 드래그 구분
//javascriptlet hasMoved = Math.abs(dragDelta) > 5;
//if (hasMoved) {
//    // 드래그로 간주, 클릭 무시
//}
//5. CSS 변수 활용
//javascript// JavaScript에서 CSS 변수 설정
//element.style.setProperty('--z', '50px');
//
//// CSS에서 사용
//transform: translateZ(var(--z));
//```
//
//---
//
//## 🎨 **시각적 효과 분석**
//
//### **Z축 배치**
//```
//           사용자 시점
//              👁️
//           ↗  ↑  ↖
//        -30  50  30   ← Z값
//         📄  🎴  📄
//       (뒤) (앞) (중간)
//```
//
//### **Y축 물결**
//```
//화면 세로 기준
//
//📄  ← 29% (위)
//   📄  ← 33% (중간)
//      📄  ← 37% (아래)
//```
//
//### **투명도 그라데이션**
//```
//0.7   0.85   1.0   0.85   0.7
// 📄    📄    🎴    📄    📄
//(흐림) (흐림) (선명) (흐림) (흐림)
//
//🔄 모든 JavaScript 파일 총정리
//  번호             파일                    주요 기능                 난이도
//   1             admin.js               관리자 페이지 UI           ⭐⭐⭐
//   2          board-detail.js           댓글 작성/수정              ⭐⭐
//   3           board-form.js          게시글 작성 + 이탈방지        ⭐⭐⭐
//   4           board-list.js             목록 검색/클릭             ⭐⭐
//   5          bookmark-list.js          북마크 목록 관리          ⭐⭐⭐⭐
//   6            navbar.js                햄버거 메뉴                 ⭐
//   7        restaurant-detail.js       이미지 모달 + 북마크       ⭐⭐⭐⭐
//   8        restaurant-form.js         맛집 등록 + 카카오맵      ⭐⭐⭐⭐⭐
//   9        restaurant-list.js          3D 카드 인터랙션        ⭐⭐⭐⭐⭐
//   📊 restaurant-list.js 특징
//   1. 복잡한 수학 계산
//   javascript// 3D 공간의 위치 계산
//   const leftPx = baseLeftPx + (relativeIndex * CARD_SPACING) + totalOffset;
//   const zDepth = 50 - (Math.abs(relativeIndex) * 40);
//   const opacity = Math.max(0.5, 1 - (Math.abs(relativeIndex) * 0.15));
//   2. 다양한 입력 방식 지원
//
//   ✅ 마우스 드래그
//   ✅ 터치 스와이프 (모바일)
//   ✅ 키보드 방향키
//   ✅ 마우스 휠
//   ✅ 카드 클릭
//   ✅ 북마크 버튼
//
//   3. 실시간 애니메이션
//   javascript// 드래그할 때마다 실시간으로 위치 업데이트
//   wrapper.addEventListener('mousemove', (e) => {
//       if (!isDragging) return;
//       dragDelta = e.clientX - startX;
//       updateCardPositions();  // 즉시 반영!
//   });
//   ```
//
//   ---
//
//   ## 🎮 **인터랙션 흐름도**
//   ```
//   [사용자 입력]
//       ↓
//   ┌───┴───┐
//   │드래그? │──Yes──→ dragDelta 계산
//   └───┬───┘           ↓
//       │          updateCardPositions()
//       │               ↓
//       No         각 카드 위치 계산
//       ↓          (X, Y, Z, opacity)
//   ┌───┴───┐           ↓
//   │ 클릭?  │       스타일 적용
//   └───┬───┘           ↓
//       │          화면에 표시
//       Yes
//       ↓
//   hasMoved?
//       │
//       ├─Yes─→ 클릭 무시
//       │
//       └─No──→ 상세페이지 이동
//
//   🔧 성능 최적화 팁
//   문제: 드래그 시 버벅임
//   javascript// 나쁜 예: 모든 mousemove마다 업데이트
//   wrapper.addEventListener('mousemove', (e) => {
//       updateCardPositions();  // 초당 수백 번 실행!
//   });
//
//   // 좋은 예: requestAnimationFrame 사용
//   let rafId = null;
//   wrapper.addEventListener('mousemove', (e) => {
//       if (rafId) return;
//       rafId = requestAnimationFrame(() => {
//           updateCardPositions();
//           rafId = null;
//       });
//   });
//   문제: 카드가 많을 때 느려짐
//   javascript// 현재: 모든 카드를 매번 업데이트
//   cards.forEach((card, index) => {
//       // 계산...
//   });
//
//   // 개선: 화면에 보이는 카드만 업데이트
//   cards.forEach((card, index) => {
//       if (Math.abs(relativeIndex) > 3) {
//           card.style.display = 'none';  // 멀리 있는 카드 숨김
//           return;
//       }
//       card.style.display = 'block';
//       // 계산...
//   });
//
//   🐛 자주 발생하는 이슈
//   이슈 1: 드래그 후 클릭이 안 돼요
//   javascript// 문제: hasMoved가 true로 남아있음
//   if (hasMoved) {
//       e.preventDefault();  // 모든 클릭이 막힘!
//   }
//
//   // 해결: mouseup에서 초기화
//   wrapper.addEventListener('mouseup', () => {
//       // ... 다른 코드
//       setTimeout(() => {
//           hasMoved = false;  // 약간의 지연 후 초기화
//       }, 100);
//   });
//   이슈 2: 카드가 화면 밖으로 나가요
//   javascript// 개선: 이동 범위 제한
//   function updateCardPositions() {
//       // 최소/최대 offset 계산
//       const maxOffset = 0;
//       const minOffset = -(cards.length - 1) * CARD_SPACING;
//
//       // 범위 제한
//       currentOffset = Math.max(minOffset, Math.min(maxOffset, currentOffset));
//
//       // ... 나머지 코드
//   }
//   이슈 3: 모바일에서 페이지 스크롤도 돼요
//   javascript// 해결: touchmove에서 preventDefault
//   wrapper.addEventListener('touchmove', (e) => {
//       if (!isDragging) return;
//       e.preventDefault();  // 페이지 스크롤 방지
//       // ... 나머지 코드
//   }, { passive: false });
//
//   🎨 CSS와의 연동
//   필요한 CSS
//   css/* 3D 공간 설정 */
//   .card-wrapper {
//       perspective: 1500px;  /* 3D 깊이감 */
//       perspective-origin: 50% 50%;
//   }
//
//   /* 카드 기본 스타일 */
//   .food-card {
//       position: absolute;
//       width: 300px;
//       height: 400px;
//       transform-style: preserve-3d;
//       transform: translateZ(var(--z));  /* JS에서 설정 */
//       transition: transform 0.3s ease;
//       cursor: pointer;
//   }
//
//   /* 호버 효과 */
//   .food-card:hover {
//       transform: translateZ(100px) scale(1.05);
//   }
//
//   /* 북마크 버튼 */
//   .btn-bookmark-card {
//       position: absolute;
//       top: 10px;
//       right: 10px;
//       z-index: 10;
//   }
//
//   .btn-bookmark-card.active {
//       color: #ff4757;  /* 빨간 하트 */
//   }
//
//   🎯 실전 예제: 카드 추가하기
//   javascript// 동적으로 카드 추가하는 함수
//   function addNewCard(restaurant) {
//       const newCard = document.createElement('div');
//       newCard.className = 'food-card';
//       newCard.setAttribute('data-id', restaurant.id);
//       newCard.innerHTML = `
//           <img src="${restaurant.image}" alt="${restaurant.name}">
//           <h3>${restaurant.name}</h3>
//           <p>${restaurant.description}</p>
//           <button class="btn-bookmark-card"
//                   data-restaurant-id="${restaurant.id}">
//               ❤️
//           </button>
//       `;
//
//       wrapper.appendChild(newCard);
//
//       // 카드 목록 갱신
//       cards = document.querySelectorAll('.food-card');
//
//       // 위치 재계산
//       updateCardPositions();
//
//       // 북마크 이벤트 등록
//       const bookmarkBtn = newCard.querySelector('.btn-bookmark-card');
//       bookmarkBtn.addEventListener('click', handleBookmarkClick);
//   }
//
//   🚀 고급 기능 아이디어
//   1. 스냅 효과 (카드가 중앙으로 정렬)
//   javascriptwrapper.addEventListener('mouseup', () => {
//       if (!isDragging) return;
//
//       isDragging = false;
//       currentOffset += dragDelta;
//       dragDelta = 0;
//
//       // 가장 가까운 카드로 스냅
//       const snapIndex = Math.round(-currentOffset / CARD_SPACING);
//       currentOffset = -snapIndex * CARD_SPACING;
//
//       updateCardPositions();
//   });
//   2. 자동 슬라이드 (캐러셀)
//   javascriptlet autoPlayInterval = null;
//
//   function startAutoPlay() {
//       autoPlayInterval = setInterval(() => {
//           currentOffset -= CARD_SPACING;
//           updateCardPositions();
//       }, 3000);  // 3초마다
//   }
//
//   function stopAutoPlay() {
//       clearInterval(autoPlayInterval);
//   }
//
//   // 호버 시 자동 슬라이드 멈춤
//   wrapper.addEventListener('mouseenter', stopAutoPlay);
//   wrapper.addEventListener('mouseleave', startAutoPlay);
//   3. 관성 스크롤 (던지기)
//   javascriptlet velocity = 0;
//   let lastMoveTime = 0;
//   let lastMoveX = 0;
//
//   wrapper.addEventListener('mousemove', (e) => {
//       if (!isDragging) return;
//
//       const now = Date.now();
//       const dt = now - lastMoveTime;
//       const dx = e.clientX - lastMoveX;
//
//       velocity = dx / dt;  // 속도 계산
//
//       lastMoveTime = now;
//       lastMoveX = e.clientX;
//
//       dragDelta = e.clientX - startX;
//       updateCardPositions();
//   });
//
//   wrapper.addEventListener('mouseup', () => {
//       if (!isDragging) return;
//
//       isDragging = false;
//       currentOffset += dragDelta;
//       dragDelta = 0;
//
//       // 관성 적용
//       function applyInertia() {
//           if (Math.abs(velocity) < 0.1) return;
//
//           currentOffset += velocity * 10;
//           velocity *= 0.95;  // 감속
//           updateCardPositions();
//
//           requestAnimationFrame(applyInertia);
//       }
//
//       applyInertia();
//   });
//
//   📚 학습 포인트 총정리
//   이 파일에서 배운 것들
//
//   3D Transform
//
//   translateZ로 깊이감 표현
//   perspective로 원근감
//   CSS 변수 동적 제어
//
//
//   드래그 앤 드롭
//
//   mousedown/move/up 패턴
//   touch 이벤트 처리
//   드래그 vs 클릭 구분
//
//
//   수학적 계산
//
//   상대적 위치 계산
//   보간(interpolation)
//   범위 제한(clamping)
//
//
//   성능 최적화
//
//   requestAnimationFrame
//   이벤트 throttling
//   passive 이벤트
//
//
//   이벤트 제어
//
//   preventDefault
//   stopPropagation
//   이벤트 버블링
//
//
//
//
//   🎓 실무 활용 팁
//   이 코드를 다른 프로젝트에 적용하려면?
//
//   상품 목록
//
//   javascript   // 쇼핑몰 상품 카드
//      // 음악 앨범 커버
//      // 영화 포스터
//
//   포트폴리오
//
//   javascript   // 프로젝트 카드
//      // 사진 갤러리
//      // 팀 멤버 소개
//
//   대시보드
//
//   javascript   // 통계 카드
//      // 위젯 목록
//      // 알림 카드
//
//   🎉 마무리
//   restaurant-list.js의 핵심:
//
//   ✅ 3D 공간에서의 카드 배치
//   ✅ 부드러운 드래그 인터랙션
//   ✅ 다양한 입력 방식 지원
//   ✅ 실시간 위치 계산
//   ✅ 북마크 기능 통합
//
//   가장 어려운 부분:
//
//   3D 공간 좌표 계산
//   드래그 상태 관리
//   클릭 vs 드래그 구분
//   성능 최적화
//
//   가장 멋진 부분:
//
//   카드 호버 시 앞으로 나오는 효과
//   물결치는 Y축 배치
//   부드러운 드래그 애니메이션
//   투명도 그라데이션
//
//
//   🎊 전체 파일 분석 완료!
//   총 9개의 JavaScript 파일을 모두 설명했습니다! 🎉
//   난이도별 정리
//   입문 (⭐)
//
//   navbar.js
//
//   초급 (⭐⭐)
//
//   board-detail.js
//   board-list.js
//
//   중급 (⭐⭐⭐)
//
//   admin.js
//   board-form.js
//
//   고급 (⭐⭐⭐⭐)
//
//   bookmark-list.js
//   restaurant-detail.js
//
//   전문가 (⭐⭐⭐⭐⭐)
//
//   restaurant-form.js (카카오맵 API)
//   restaurant-list.js (3D 인터랙션)
//
//
//   기능별 정리
//   UI/UX 개선
//
//   admin.js, navbar.js
//
//   폼 처리
//
//   board-form.js, restaurant-form.js
//
//   비동기 통신
//
//   bookmark-list.js, restaurant-detail.js
//
//   인터랙티브 UI
//
//   restaurant-list.js (3D 카드)
//
//   검색/필터
//
//   board-list.js, admin.js
//
//
//   공통 패턴
//
//   DOMContentLoaded 사용
//
//   javascript   document.addEventListener('DOMContentLoaded', function() {
//          // 초기화 코드
//      });
//
//   유효성 검사
//
//   javascript   if (!input) {
//          alert('필수 입력입니다');
//          return false;
//      }
//
//   CSRF 토큰 처리
//
//   javascript   const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
//      headers[csrfHeader] = csrfToken;
//
//   에러 처리
//
//   javascript   try {
//          // 시도
//      } catch (error) {
//          console.error('오류:', error);
//      } finally {
//          // 정리
//      }
//
//   토스트 메시지
//
//   javascript   function showToast(message, type) {
//          // 알림 표시
//      }