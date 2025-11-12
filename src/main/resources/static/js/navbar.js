// 🍔 navbar 햄버거 메뉴 토글
//📌 전체 구조
//  네비게이션 바(상단 메뉴)의 햄버거 메뉴 기능을 담당합니다.
//  특징: 모바일에서 메뉴를 펼치고 접는 기능
document.addEventListener('DOMContentLoaded', function() {
    const toggle = document.getElementById('navbarToggle');     // 햄버거 버튼
    const menu = document.getElementById('navbarMenu');         // 메뉴 목록
    const user = document.getElementById('navbarUser');         // 사용자 메뉴
    /*역할: 필요한 네비게이션 요소들을 변수에 저장
    HTML 구조 예시:
    html<nav>
        <!-- 햄버거 버튼 (모바일에서만 보임) -->
        <button id="navbarToggle">☰</button>

        <!-- 메인 메뉴 -->
        <div id="navbarMenu">
            <a href="/" class="nav-link">홈</a>
            <a href="/restaurants" class="nav-link">맛집</a>
            <a href="/boards" class="nav-link">게시판</a>
        </div>

        <!-- 사용자 메뉴 -->
        <div id="navbarUser">
            <a href="/mypage">마이페이지</a>
            <a href="/logout">로그아웃</a>
        </div>
    </nav> */

    //요소 존재 여부 확인, 역할: 3개 요소가 모두 존재하는지 확인

    if (toggle && menu && user) {
    /*왜 필요한가요?
      javascript// 만약 요소가 없는데 접근하면?
      const toggle = document.getElementById('navbarToggle'); // null
      toggle.addEventListener(...); // ❌ 에러! (null은 addEventListener가 없음)

      // 안전한 방식
      if (toggle && menu && user) {
          // 모두 존재할 때만 실행
          toggle.addEventListener(...); // ✅ 안전!
      } */

        toggle.addEventListener('click', function() {
             // 버튼 애니메이션 (X자 모양으로 변경)
            this.classList.toggle('active');

/* 역할: 햄버거 버튼 클릭 시 메뉴 펼치기/접기
   classList.toggle() 설명:
   javascript// toggle()의 동작
   element.classList.toggle('active');

   // 클래스가 없으면 → 추가
   // 클래스가 있으면 → 제거
   ```

   **실제 동작 예시**:
   ```
   [초기 상태]
   <button id="navbarToggle">☰</button>
   <div id="navbarMenu">...</div>      (숨김)
   <div id="navbarUser">...</div>      (숨김)

   ↓ 첫 번째 클릭

   [메뉴 열림]
   <button id="navbarToggle" class="active">☰ → X</button>
   <div id="navbarMenu" class="active">...</div>  (표시)
   <div id="navbarUser" class="active">...</div>  (표시)

   ↓ 두 번째 클릭

   [메뉴 닫힘]
   <button id="navbarToggle">☰</button>
   <div id="navbarMenu">...</div>      (숨김)
   <div id="navbarUser">...</div>      (숨김)
   CSS 예시:
   css 햄버거 버튼 기본 상태
   #navbarToggle {
        ☰ 모양
   }

    메뉴 열렸을 때 버튼
   #navbarToggle.active {
        X 모양으로 변경
       transform: rotate(90deg);
   }

    메뉴 기본 상태 (모바일)
   @media (max-width: 768px) {
       #navbarMenu {
           display: none;   숨김
       }

       #navbarMenu.active {
           display: block;  표시
       }
   } */

            // 메뉴 표시/숨김
            menu.classList.toggle('active');
            user.classList.toggle('active');
        });

        // 메뉴 링크 클릭 시 메뉴 닫기 (모바일)
        const menuLinks = menu.querySelectorAll('.nav-link');
        menuLinks.forEach(link => {
            link.addEventListener('click', function() {
                if (window.innerWidth <= 768) {
                    toggle.classList.remove('active');
                    menu.classList.remove('active');
                    user.classList.remove('active');
                }
            });
        });
    }
    /*== **역할**: 메뉴 항목을 클릭하면 메뉴를 자동으로 닫음 (모바일에서만)

         **시나리오**:
         ```
         [모바일 화면]

         1. 사용자: 햄버거 버튼 클릭
            ↓
            메뉴 펼쳐짐
            ┌──────────────┐
            │ ☰ → X       │
            │ - 홈         │ ← 메뉴 표시됨
            │ - 맛집       │
            │ - 게시판     │
            │ - 마이페이지 │
            └──────────────┘

         2. 사용자: "맛집" 메뉴 클릭
            ↓
            메뉴 자동으로 닫힘
            ┌──────────────┐
            │ ☰           │
            └──────────────┘
            ↓
            맛집 페이지로 이동
         코드 설명:
         javascriptif (window.innerWidth <= 768) {
             // 화면 너비가 768px 이하일 때만 (모바일/태블릿)
             toggle.classList.remove('active');
             menu.classList.remove('active');
             user.classList.remove('active');
         }
         ```

         **`window.innerWidth`**:
         - 브라우저 창의 내부 너비 (픽셀)
         - 모바일: ~375px
         - 태블릿: ~768px
         - 데스크탑: 1024px 이상

         **왜 모바일에서만?**
         ```
         [데스크탑]
         메뉴가 항상 보이는 상태
         → 클릭해도 닫을 필요 없음

         [모바일]
         메뉴가 접혀있다가 펼쳐짐
         → 메뉴 선택 후 다시 접어야 화면이 깔끔함
         .remove() vs .toggle():
         javascript// toggle: 있으면 제거, 없으면 추가
         element.classList.toggle('active');

         // remove: 무조건 제거
         element.classList.remove('active');

         // add: 무조건 추가
         element.classList.add('active');
         ```

         여기서는 `remove()`를 사용하는 이유:
         - 메뉴를 **확실히 닫아야** 하니까
         - `toggle()`을 쓰면 닫혀있을 때 열릴 수도 있음

         ---

         ## 🎯 **전체 시나리오**

         ### **시나리오 1: 모바일에서 메뉴 사용**
         ```
         [초기 화면 - 767px]
         ┌─────────────────┐
         │ 🍔 MyApp       │ ← 햄버거 버튼만 보임
         └─────────────────┘

         ↓ 햄버거 버튼 클릭

         [메뉴 열림]
         ┌─────────────────┐
         │ ✕ MyApp        │ ← 버튼이 X로 변경
         ├─────────────────┤
         │ 홈              │
         │ 맛집            │
         │ 게시판          │
         │ 마이페이지      │
         │ 로그아웃        │
         └─────────────────┘

         ↓ "맛집" 클릭

         [메뉴 닫힘 + 페이지 이동]
         ┌─────────────────┐
         │ 🍔 MyApp       │
         └─────────────────┘
            ↓
         맛집 페이지로 이동
         ```

         ### **시나리오 2: 데스크탑에서 메뉴 사용**
         ```
         [데스크탑 화면 - 1024px]
         ┌────────────────────────────────────┐
         │ MyApp  [홈] [맛집] [게시판]  [마이페이지] [로그아웃] │
         └────────────────────────────────────┘
                 ↑ 항상 보임, 햄버거 버튼 없음

         ↓ "맛집" 클릭

         메뉴는 그대로, 페이지만 이동
         (window.innerWidth > 768 이므로 메뉴 닫기 코드 실행 안 됨)

         💡 핵심 개념 정리
         1. classList 메서드
         javascriptelement.classList.add('class');      // 클래스 추가
         element.classList.remove('class');   // 클래스 제거
         element.classList.toggle('class');   // 있으면 제거, 없으면 추가
         element.classList.contains('class'); // 클래스 있는지 확인
         2. 반응형 웹 기본 개념
         css 모바일 우선
         .menu {
             display: none;  기본은 숨김
         }

          데스크탑
         @media (min-width: 769px) {
             .menu {
                 display: block;  큰 화면에서는 항상 표시
             }

             #navbarToggle {
                 display: none;  햄버거 버튼 숨김
             }
         }
         3. window.innerWidth
         javascript// 현재 브라우저 창 너비 확인
         if (window.innerWidth <= 768) {
             // 모바일/태블릿 처리
         } else {
             // 데스크탑 처리
         }
         4. this vs event.target vs event.currentTarget
         javascriptelement.addEventListener('click', function(event) {
             this                 // 이벤트가 등록된 요소
             event.target         // 실제로 클릭된 요소
             event.currentTarget  // 이벤트가 등록된 요소 (this와 동일)
         });
         예시:
         html<div id="parent">
             <button id="child">클릭</button>
         </div>
         javascriptparent.addEventListener('click', function(e) {
             console.log(this);           // <div id="parent">
             console.log(e.target);       // <button id="child"> (실제 클릭)
             console.log(e.currentTarget); // <div id="parent">
         });
         ```

         ---

         ## 🔄 **다른 파일들과의 비교**

         | 파일 | 주요 기능 | 복잡도 |
         |------|----------|--------|
         | **admin.js** | 관리자 페이지 UI | ⭐⭐⭐ |
         | **board-detail.js** | 댓글 작성/수정 | ⭐⭐ |
         | **board-form.js** | 게시글 작성 + 이탈 방지 | ⭐⭐⭐ |
         | **board-list.js** | 목록 검색/클릭 | ⭐⭐ |
         | **bookmark-list.js** | 비동기 통신 + 애니메이션 | ⭐⭐⭐⭐ |
         | **navbar.js** | 햄버거 메뉴 토글 | ⭐ (간단함!) |

         ---

         ## 📱 **실제 사용 예시**

         ### **모바일 햄버거 메뉴 패턴**
         ```
         [닫힘]     [열림]      [선택]
         ☰         ✕          ☰
                  ━━━
                  홈         → 홈 페이지
                  맛집
                  게시판
         CSS 애니메이션 추가 예시
         css 햄버거 아이콘 애니메이션
         #navbarToggle {
             transition: transform 0.3s ease;
         }

         #navbarToggle.active {
             transform: rotate(90deg);
         }

          메뉴 슬라이드 애니메이션
         #navbarMenu {
             max-height: 0;
             overflow: hidden;
             transition: max-height 0.3s ease;
         }

         #navbarMenu.active {
             max-height: 500px;
         }

         🎨 개선 아이디어
         1. 외부 클릭 시 메뉴 닫기
         javascript// 메뉴 외부 클릭 감지
         document.addEventListener('click', function(e) {
             if (!menu.contains(e.target) && !toggle.contains(e.target)) {
                 if (menu.classList.contains('active')) {
                     toggle.classList.remove('active');
                     menu.classList.remove('active');
                     user.classList.remove('active');
                 }
             }
         });
         2. ESC 키로 메뉴 닫기
         javascriptdocument.addEventListener('keydown', function(e) {
             if (e.key === 'Escape' && menu.classList.contains('active')) {
                 toggle.classList.remove('active');
                 menu.classList.remove('active');
                 user.classList.remove('active');
             }
         });
         3. 스크롤 시 메뉴 닫기
         javascriptwindow.addEventListener('scroll', function() {
             if (menu.classList.contains('active')) {
                 toggle.classList.remove('active');
                 menu.classList.remove('active');
                 user.classList.remove('active');
             }
         }); ==*/
});