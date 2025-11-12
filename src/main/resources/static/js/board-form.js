// 게시글 작성/수정 페이지 스크립트
//📌 전체 구조
// 게시글을 쓰거나 수정할 때 사용하는 페이지의 스크립트입니다.

document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ 게시글 작성/수정 페이지 로드 완료');

    const titleInput = document.getElementById('title');
    const contentTextarea = document.getElementById('content');
    const titleCount = document.getElementById('titleCount');
    const contentCount = document.getElementById('contentCount');
    const boardForm = document.querySelector('.board-form');
//    역할: 필요한 HTML 요소들을 변수에 저장
//
//    titleInput: 제목 입력란
//    contentTextarea: 내용 입력란
//    titleCount: 제목 글자 수 표시 영역
//    contentCount: 내용 글자 수 표시 영역
//    boardForm: 폼 전체
//    왜 이렇게 하나요?
//
//    한 번만 찾아서 저장해두면 나중에 계속 재사용 가능
//    코드가 깔끔해짐

    // 제목 글자 수 카운터
    if (titleInput && titleCount) {
        titleInput.addEventListener('input', function() {
            const length = this.value.length;           // 현재 제목 길이
            titleCount.textContent = length;            // 화면에 표시

            if (length >= 100) {
                titleCount.style.color = '#f44336';     // 빨간색
                titleCount.style.fontWeight = '700';
            } else if (length >= 80) {
                titleCount.style.color = '#ff9800';     // 주황색
                titleCount.style.fontWeight = '600';
            } else {
                titleCount.style.color = '#999';        // 회색
                titleCount.style.fontWeight = 'normal';
            }
        });

        // 페이지 로드 시 초기값 표시 (수정 모드일 때 필요)
        titleCount.textContent = titleInput.value.length;
    }
//    ```
//    **역할**: board-detail.js의 댓글 카운터와 동일한 방식
//
//    **제목 기준**:
//    - 0~79자: 회색 (정상)
//    - 80~99자: 주황색 (거의 찼어요)
//    - 100자: 빨간색 (최대 도달!)
//
//    **화면 예시**:
//    ```
//    제목: [________________] 15 / 100

    // 내용 글자 수 카운터
    if (contentTextarea && contentCount) {
        contentTextarea.addEventListener('input', function() {
            const length = this.value.length;
            contentCount.textContent = length;

            if (length >= 5000) {
                contentCount.style.color = '#f44336';
                contentCount.style.fontWeight = '700';
            } else if (length >= 4500) {
                contentCount.style.color = '#ff9800';
                contentCount.style.fontWeight = '600';
            } else {
                contentCount.style.color = '#999';
                contentCount.style.fontWeight = 'normal';
            }
        });
//        역할: 제목 카운터와 동일하지만 제한이 더 큼
//        내용 기준:
//
//        0~4499자: 회색
//        4500~4999자: 주황색
//        5000자: 빨간색
//
//        왜 따로 만들었나요?
//
//        제목은 짧게 (100자)
//        내용은 길게 (5000자)
//        각각 다른 제한이 필요

        // 초기 글자 수 표시
        contentCount.textContent = contentTextarea.value.length;
    }

    // 폼 제출 시 유효성 검사
    if (boardForm) {
        boardForm.addEventListener('submit', function(e) {
            const title = titleInput.value.trim();              // 앞뒤 공백 제거
            const content = contentTextarea.value.trim();

            // 제목 검증
            if (!title) {
                e.preventDefault();                             // 제출 막기
                alert('제목을 입력해주세요.');
                titleInput.focus();                             // 커서를 제목 입력란으로
                return false;
            }

            if (title.length < 2) {
                e.preventDefault();
                alert('제목은 2글자 이상 입력해주세요.');
                titleInput.focus();
                return false;
            }

            if (title.length > 100) {
                e.preventDefault();
                alert('제목은 100자 이하로 입력해주세요.');
                titleInput.focus();
                return false;
            }

            // 내용 검증
            if (!content) {
                e.preventDefault();
                alert('내용을 입력해주세요.');
                contentTextarea.focus();
                return false;
            }

            if (content.length < 10) {
                e.preventDefault();
                alert('내용은 10글자 이상 입력해주세요.');
                contentTextarea.focus();
                return false;
            }

            if (content.length > 5000) {
                e.preventDefault();
                alert('내용은 5000자 이하로 입력해주세요.');
                contentTextarea.focus();
                return false;                               // 모든 검사 통과 → 제출 진행
            }
//            ```
//            **역할**: "등록" 버튼 누를 때 데이터 검증
//
//            **검사 순서**:
//            ```
//            1. 제목이 비어있나? → ❌ 경고
//            2. 제목이 2글자 미만? → ❌ 경고
//            3. 제목이 100자 초과? → ❌ 경고
//            4. 내용이 비어있나? → ❌ 경고
//            5. 내용이 10글자 미만? → ❌ 경고
//            6. 내용이 5000자 초과? → ❌ 경고
//            7. 모두 통과 → ✅ 서버로 전송
//            .focus() 의 역할:
//
//            문제가 있는 입력란으로 커서를 자동 이동
//            사용자가 바로 수정할 수 있게 도와줌

            console.log('✅ 게시글 제출: ' + title);
            return true;
        });
    }

    // 페이지 이탈 방지 (작성 중인 내용이 있을 때)
    let formChanged = false;                    // 플래그 변수 (변경 여부 추적)

    if (titleInput) {
        titleInput.addEventListener('input', function() {
            formChanged = true;                 // 제목을 입력하면 true로 변경
        });
    }

    if (contentTextarea) {
        contentTextarea.addEventListener('input', function() {
            formChanged = true;                 // 내용을 입력하면 true로 변경
        });
        //            역할: 사용자가 뭔가 입력했는지 추적
        //            플래그 패턴:
        //
        //            처음: formChanged = false (변경사항 없음)
        //            타이핑 시작: formChanged = true (변경사항 있음!)
    }

    window.addEventListener('beforeunload', function(e) {
        if (formChanged) {
            e.preventDefault();
            e.returnValue = '작성 중인 내용이 있습니다. 페이지를 떠나시겠습니까?';
            return e.returnValue;
//            ```
//            **역할**: 브라우저 탭을 닫거나 새로고침할 때 경고
//
//            **동작**:
//            ```
//            사용자: [F5 누름 or 탭 닫기]
//                     ↓
//            브라우저: "작성 중인 내용이 있습니다. 페이지를 떠나시겠습니까?"
//                     [페이지 나가기] [취소]
//            언제 작동하나요?
//
//            formChanged === true 일 때만!
//            아무것도 안 썼으면 경고 안 뜸

        }
    });

    // 폼 제출 시에는 이탈 방지 해제
    if (boardForm) {
        boardForm.addEventListener('submit', function() {
            formChanged = false;            // 플래그를 다시 false로
        });
//        ```
//        **역할**: "등록" 버튼으로 제출할 때는 경고 안 뜨게
//
//        **왜 필요한가요?**
//        ```
//        [문제 상황]
//        사용자: 게시글 작성 완료 → [등록] 클릭
//        브라우저: "작성 중인 내용이 있습니다..." ← 이건 이상하잖아요!
//
//        [해결]
//        등록 버튼 누르면 → formChanged = false
//        → 서버로 전송 → 페이지 이동 → 경고 안 뜸!
    }

    // 취소 버튼 클릭 시 확인
    const cancelBtn = document.querySelector('.btn-secondary');
    if (cancelBtn && cancelBtn.tagName === 'A') {
        cancelBtn.addEventListener('click', function(e) {
            if (formChanged) {
                if (!confirm('작성 중인 내용이 있습니다. 정말 취소하시겠습니까?')) {
                    e.preventDefault();         // 취소 버튼의 이동을 막음
                    return false;
                }
            }
        });
    }

    console.log('📝 게시글 작성/수정 스크립트 초기화 완료');
});
/* ```
   **역할**: "취소" 버튼 클릭 시 재확인

   **동작 흐름**:
   ```
   사용자: [취소] 버튼 클릭
            ↓
   formChanged === true?
            ↓ yes
   확인창: "작성 중인 내용이 있습니다. 정말 취소하시겠습니까?"
            ↓
   [확인] → 목록으로 이동
   [취소] → 그대로 머무름
   ```

   **`cancelBtn.tagName === 'A'` 체크 이유**:
   - 취소 버튼이 `<a>` 링크인지 확인
   - `<button>` 타입이면 다르게 처리해야 해서

   ---

   ## 🎯 **전체 시나리오**

   ### **시나리오 1: 게시글 새로 작성**
   ```
   1. 페이지 로드
      → formChanged = false

   2. 제목 입력: "안녕하세요"
      → formChanged = true
      → 화면에 "6 / 100" 표시

   3. 내용 입력: "처음 가입했어요..."
      → 화면에 "10 / 5000" 표시

   4. [등록] 버튼 클릭
      → 유효성 검사 통과
      → formChanged = false (경고 해제)
      → 서버로 전송
   ```

   ### **시나리오 2: 실수로 새로고침**
   ```
   1. 게시글 열심히 작성 중...
      → formChanged = true

   2. 실수로 F5 누름
      ↓
      "작성 중인 내용이 있습니다. 페이지를 떠나시겠습니까?"
      ↓
      [취소] 클릭 → 내용 보존! 😊
   ```

   ### **시나리오 3: 제목이 너무 짧음**
   ```
   1. 제목: "ㅋ"
   2. 내용: "재미있네요 정말..."
   3. [등록] 클릭
      ↓
      ❌ "제목은 2글자 이상 입력해주세요."
      ↓
      제목 입력란으로 커서 이동

   💡 핵심 개념 정리
   1. 플래그 패턴
   javascriptlet flag = false; // 상태를 추적하는 변수

   // 상태 변경
   input.addEventListener('input', () => {
       flag = true;
   });

   // 상태 확인
   if (flag) {
       // 뭔가 변경되었을 때만 실행
   }
   2. beforeunload 이벤트

   브라우저가 페이지를 떠나기 직전에 발생
   탭 닫기, 새로고침, 뒤로가기, 다른 페이지 이동 등

   3. 유효성 검사 체인
   javascriptif (!제목) return false;
   if (제목.length < 2) return false;
   if (제목.length > 100) return false;
   // ... 모든 검사를 통과해야 return true

   하나라도 실패하면 즉시 중단
   모두 통과해야 제출

   4. focus() 활용
   javascriptif (오류) {
       alert('오류 메시지');
       input.focus(); // 사용자를 해당 입력란으로 안내
   }

   🔄 board-detail.js와의 차이점
   항목                board-detail.               jsboard-form.js
   목적                댓글 작성/수정                 게시글 작성/수정
   글자 제한              500자                  제목 100자 + 내용 5000자
   페이지 이탈 방지       ❌ 없음                     ✅ 있음 (중요!)
   입력                필드1개 (댓글)                2개 (제목 + 내용) */