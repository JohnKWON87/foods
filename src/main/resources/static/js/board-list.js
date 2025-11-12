// 게시글 목록 페이지 스크립트
// 📌 전체 구조
// 게시글 목록을 보여주는 페이지에서 검색, 클릭 등의 기능을 담당합니다.

document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ 게시글 목록 페이지 로드 완료');

    // 자동으로 메시지 숨기기 (3초 후)
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s ease';
            alert.style.opacity = '0';
            setTimeout(() => {
                alert.remove();
            }, 500);
        }, 3000);
    });
//    역할: 이전 파일들과 동일
//
//    "게시글이 등록되었습니다" 같은 알림 메시지를 3초 후 자동으로 사라지게 함
//    재사용된 코드: 모든 페이지에서 사용하는 공통 기능

    // 검색 폼 유효성 검사
    const searchForm = document.querySelector('.search-form');
    if (searchForm) {
        searchForm.addEventListener('submit', function(e) {
            const keyword = this.querySelector('input[name="keyword"]').value.trim();

            // 빈 검색어 체크
            if (!keyword) {
                e.preventDefault();             // 검색 취소
                alert('검색어를 입력해주세요.');
                return false;
            }

            // 너무 짧은 검색어 체크
            if (keyword.length < 2) {
                e.preventDefault();
                alert('검색어는 2글자 이상 입력해주세요.');
                return false;
            }

            console.log('🔍 검색: ' + keyword);
        });
//        **역할**: 검색 버튼 클릭 시 입력값 검증
//
//        **검사 항목**:
//        1. ❌ **빈 검색어**: 아무것도 안 쓰고 검색
//        2. ❌ **너무 짧음**: "ㅋ" 같은 1글자 검색
//
//        **왜 2글자 이상이어야 하나요?**
//        - 1글자 검색은 결과가 너무 많아서 비효율적
//        - 서버 부하도 줄일 수 있음
//
//        **화면 예시**:
//        ```
//        ┌─────────────────────────┐
//        │ [검색어 입력...] [검색]    │
//        └─────────────────────────┘
//
//        [검색] 클릭 → 검증 → 통과하면 서버로 전송
    }

    // 테이블 행 클릭 시 상세 페이지로 이동
    const boardRows = document.querySelectorAll('.board-row');
    boardRows.forEach(row => {
        row.style.cursor = 'pointer';                   // 마우스 커서를 손가락 모양으로

        row.addEventListener('click', function(e) {
            // 링크를 직접 클릭한 경우는 제외
            if (e.target.tagName === 'A') {
                return;                                 // 함수 종료 (아무것도 안 함)
            }

            // 행 안에 있는 제목 링크를 찾아서
            const link = this.querySelector('.col-title a');
            if (link) {
                window.location.href = link.href;       // 그 링크로 이동
            }
//            **역할**: 게시글 행 전체를 클릭 가능하게 만듦
//
//            **동작 설명**:
//            ```
//            [게시글 목록]
//            ┌─────────┬──────────────┬────────┬────────┐
//            │ 번호     │ 제목         │ 작성자   │ 날짜   │
//            ├─────────┼──────────────┼────────┼────────┤
//            │ 5       │ 안녕하세요     │ 홍길동  │ 10-22  │ ← 이 행 어디든 클릭 가능!
//            └─────────┴──────────────┴────────┴────────┘
//            상세 로직:
//            javascript// 1. 마우스 커서 변경
//            row.style.cursor = 'pointer'; // 손가락 모양 커서
//
//            // 2. 클릭 이벤트 등록
//            row.addEventListener('click', function(e) {
//                // 3. 이미 링크를 클릭한 경우는 무시
//                if (e.target.tagName === 'A') {
//                    return; // 링크가 알아서 처리하니까 우리는 손 떼기
//                }
//
//                // 4. 제목 링크를 찾아서 그 주소로 이동
//                const link = this.querySelector('.col-title a');
//                window.location.href = link.href;
//            });
//            왜 e.target.tagName === 'A' 체크가 필요한가요?
//            시나리오를 볼게요:
//            html<tr class="board-row">
//                <td>5</td>
//                <td class="col-title">
//                    <a href="/board/5">안녕하세요</a>
//                </td>
//                <td>홍길동</td>
//            </tr>
//            ```
//
//            **문제 상황** (체크 안 했을 때):
//            ```
//            사용자: 제목 "안녕하세요" 클릭
//                     ↓
//            <a> 태그: "/board/5"로 이동 시도
//                     ↓
//            row 클릭 이벤트: 또 "/board/5"로 이동 시도
//                     ↓
//            결과: 두 번 이동! (버그)
//            ```
//
//            **해결** (체크 했을 때):
//            ```
//            사용자: 제목 "안녕하세요" 클릭
//                     ↓
//            if (e.target.tagName === 'A') return; ← 여기서 중단!
//                     ↓
//            <a> 태그만 이동 처리
//                     ↓
//            결과: 정상 작동!
//            실제 사용 예시:
//
//            ✅ "번호" 컬럼 클릭 → 상세 페이지로 이동
//            ✅ "작성자" 컬럼 클릭 → 상세 페이지로 이동
//            ✅ "날짜" 컬럼 클릭 → 상세 페이지로 이동
//            ✅ "제목" 링크 클릭 → 링크가 알아서 이동 (중복 처리 방지)
        });
    });

    // 검색어 입력 시 실시간 글자 수 표시 (선택사항)
    const searchInput = document.querySelector('.search-input');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const length = this.value.length;               // 현재 입력된 글자 수
            if (length > 50) {
                this.value = this.value.substring(0, 50);   // 50자까지만 자름
                alert('검색어는 50자 이내로 입력해주세요.');
            }
//            **역할**: 검색어를 너무 길게 입력하지 못하게 제한
//
//            **동작 흐름**:
//
//            사용자: "안녕하세요 저는..." (타이핑 중)
//                     ↓
//            글자 수 체크: 30자 → OK, 계속 입력 가능
//                     ↓
//            사용자: 계속 타이핑... 50자 도달
//                     ↓
//            51번째 글자 입력 시도!
//                     ↓
//            즉시 잘림: this.value.substring(0, 50)
//                     ↓
//            경고창: "검색어는 50자 이내로 입력해주세요."
//            substring(0, 50) 설명:
//            javascriptlet text = "0123456789"; // 10글자
//
//            text.substring(0, 5); // "01234" (0번째부터 5번째 전까지)
//            text.substring(0, 50); // "0123456789" (전체, 50자가 없으면 있는 만큼만)
//            이 방법 vs 다른 방법:
//            javascript// 방법 1: 현재 코드 (실시간 잘라내기)
//            if (length > 50) {
//                this.value = this.value.substring(0, 50);
//            }
//
//            방법 2: HTML 속성 사용
//            <input maxlength="50">
//
//            차이점:
//            방법 1: 경고창을 보여줄 수 있음
//            방법 2: 더 간단하지만 경고 불가능
        });
    }

    console.log('📋 게시글 개수: ' + boardRows.length);
//    **역할**: 개발자 도구에서 현재 화면에 몇 개의 게시글이 있는지 확인
//
//    **개발자 도구에서 보이는 내용**:
//
//    ✅ 게시글 목록 페이지 로드 완료
//    📋 게시글 개수: 10
});

// 페이지 번호 클릭 시 부드러운 스크롤
document.querySelectorAll('.page-btn').forEach(btn => {
    btn.addEventListener('click', function() {
        window.scrollTo({   top: 0,                     // 페이지 맨 위로
                            behavior: 'smooth' });      // 부드럽게 스크롤
    });
});
/* **역할**: 페이지 번호 클릭 시 화면을 맨 위로 스크롤

   **시나리오**:
   ```
   [게시글 목록 화면]

   게시글 1
   게시글 2
   게시글 3
   ...
   게시글 10

   [1] [2] [3] [4] [5] ← 현재 1페이지
        ↑
     여기서 스크롤을 많이 내린 상태


   **[2] 클릭 시**:

   1. 서버에서 2페이지 데이터를 가져옴
   2. 페이지가 새로고침되거나 업데이트됨
   3. 이 코드 실행: 화면을 슈웅~ 하고 맨 위로 이동
   왜 필요한가요?

   페이지를 넘길 때마다 맨 위부터 보고 싶잖아요!
   안 하면 중간부터 보여서 불편함

   기본 동작 vs 부드러운 스크롤:
   javascript// 기본 (뚝 하고 이동)
   window.scrollTo(0, 0);

   // 부드러운 스크롤 (슈웅~)
   window.scrollTo({ top: 0, behavior: 'smooth' });


   ---

   ## 🎯 **전체 시나리오**

   ### **시나리오 1: 게시글 검색**

   1. 검색어 입력: "자바스크립트"
      → 실시간 글자 수 체크 (50자 이내인지)

   2. [검색] 버튼 클릭
      → 유효성 검사:
        - 빈 검색어? ❌
        - 2글자 미만? ❌
        - 모두 통과! ✅

   3. 서버로 검색 요청 전송
      → 검색 결과 페이지 표시


   ### **시나리오 2: 게시글 클릭**

   [게시글 목록]
   ┌────┬──────────────┬────────┐
   │ 5  │ 안녕하세요     │ 홍길동  │
   └────┴──────────────┴────────┘
           ↑ 여기 클릭!

   1. "홍길동" 부분 클릭
   2. row 클릭 이벤트 발생
   3. e.target.tagName === 'A'? → No (td를 클릭했으니까)
   4. 제목 링크 찾기: ".col-title a"
   5. window.location.href = "/board/5"
   6. 상세 페이지로 이동!
   ```

   ### **시나리오 3: 페이지 이동**

   1. 게시글 목록을 스크롤해서 맨 아래로 내림
      [1] [2] [3] [4] [5]

   2. [3] 페이지 번호 클릭

   3. 페이지 로드 + 화면 맨 위로 부드럽게 스크롤
      → 3페이지의 첫 번째 게시글부터 볼 수 있음

   💡 핵심 개념 정리
   1. 이벤트 버블링
   html<tr class="board-row" onclick="...">  ← 부모
       <td>번호</td>
       <td><a href="...">제목</a></td>  ← 자식
   </tr>
   ```

   클릭 이벤트는 자식 → 부모로 전파됩니다:
   ```
   1. <a> 클릭
   2. <a>의 클릭 이벤트 실행
   3. 이벤트가 <td>로 전파
   4. 이벤트가 <tr>로 전파
   5. <tr>의 클릭 이벤트 실행
   해결책: if (e.target.tagName === 'A') return;으로 중복 방지

   2. window.location.href
   javascript// 현재 URL 확인
   console.log(window.location.href);
   // "https://example.com/board/list"

   // 다른 페이지로 이동
   window.location.href = "/board/5";
   <a> 태그 클릭과 동일한 효과:
   html<a href="/board/5">클릭</a>

   3. substring() 메서드
   javascriptlet text = "안녕하세요";

   text.substring(0, 2);  // "안녕" (0번째부터 2번째 전까지)
   text.substring(2, 5);  // "하세요"
   text.substring(0, 100); // "안녕하세요" (넘치면 있는 만큼만)

   4. scrollTo() 옵션
   javascriptwindow.scrollTo({
       top: 0,              // Y 좌표 (0 = 맨 위)
       left: 0,             // X 좌표 (옵션)
       behavior: 'smooth'   // 'auto' or 'smooth'
   });

   🔄 다른 파일들과의 비교
   파일                  주요 기능                특징
   admin.js            관리자 페이지       테이블 정렬, 삭제 확인
   board-detail.js     게시글 상세            댓글 작성/수정
   board-form.js       게시글 작성            페이지 이탈 방지
   board-list.js       게시글 목록           행 클릭 이동, 검색 */