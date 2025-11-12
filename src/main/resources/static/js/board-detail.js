// 게시글 상세 페이지 스크립트

document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ 게시글 상세 페이지 로드 완료');
    //역할: 페이지가 완전히 로드되면 실행
    //이전 admin.js와 동일한 시작 방식

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
//        역할: admin.js와 동일
//        "댓글이 등록되었습니다" 같은 알림을 3초 후 자동으로 사라지게 함
//        재사용된 코드: 여러 페이지에서 같은 기능 사용
    });

    // 댓글 글자 수 카운터
    const commentTextarea = document.querySelector('.comment-textarea');
    const charCount = document.getElementById('charCount');

    if (commentTextarea && charCount) {
        commentTextarea.addEventListener('input', function() {
            const length = this.value.length;               // 현재 입력된 글자 수
            charCount.textContent = length;                 // 화면에 표시

           // 글자 수에 따라 색상 변경
            if (length >= 500) {
                charCount.style.color = '#f44336';          // 빨간색 (위험!)
                charCount.style.fontWeight = '700';
            } else if (length >= 450) {
                charCount.style.color = '#ff9800';          // 주황색 (경고)
                charCount.style.fontWeight = '600';
            } else {
                charCount.style.color = '#999';             // 회색 (정상)
                charCount.style.fontWeight = 'normal';
            }
//            **역할**: 실시간으로 댓글 글자 수 표시 + 경고
//
//            **동작 흐름**:
//            1. 사용자가 댓글 입력란에 타이핑
//            2. `input` 이벤트 발생 (글자가 입력될 때마다)
//            3. 현재 글자 수를 세서 화면에 표시
//            4. 글자 수에 따라 색상 변경
//               - 0~449자: 회색 (정상)
//               - 450~499자: 주황색 (거의 찼어요)
//               - 500자 이상: 빨간색 (제한 초과!)
//
//            **화면 예시**:
//            ```
//            ┌─────────────────────────┐
//            │ 댓글을 입력하세요...       │
//            │                         │
//            └─────────────────────────┘
//            127 / 500 ← 이 부분이 charCount
        });

        // 페이지 로드 시 초기 글자 수 표시
        charCount.textContent = commentTextarea.value.length;
    }

    // 댓글 작성 폼 제출 시 유효성 검사
    const commentForm = document.querySelector('.comment-form');
    if (commentForm) {
        commentForm.addEventListener('submit', function(e) {
            const content = this.querySelector('textarea[name="content"]').value.trim();

            // 빈 댓글 체크
            if (!content) {
                e.preventDefault();     // 제출 취소
                alert('댓글 내용을 입력해주세요.');
                return false;
            }

            // 너무 짧은 댓글 체크
            if (content.length < 2) {
                e.preventDefault();
                alert('댓글은 2글자 이상 입력해주세요.');
                return false;
            }

            // 너무 긴 댓글 체크
            if (content.length > 500) {
                e.preventDefault();
                alert('댓글은 500자 이하로 입력해주세요.');
                return false;
            }

            console.log('💬 댓글 작성 제출');
        });
    }
//    역할: 댓글 등록 버튼 누를 때 검증
//    검사 항목:
//
//    ❌ 빈 댓글: 공백만 있거나 아무것도 안 썼을 때
//    ❌ 너무 짧음: "ㅋ" 같은 1글자 댓글
//    ❌ 너무 김: 500자 초과
//
//    모두 통과하면 → 서버로 전송
//    하나라도 실패하면 → 경고창 + 제출 취소

    console.log('📖 게시글 상세 스크립트 초기화 완료');
});

/**
 * 댓글 수정 모드 활성화
 */
function editComment(commentId, button) {
    console.log('✏️ 댓글 수정 모드: ' + commentId);

    // 수정 폼 표시
    const editForm = document.getElementById('editForm-' + commentId);
    const commentContent = button.closest('.comment-item').querySelector('.comment-content');
    const commentActions = button.closest('.comment-actions');

    if (editForm && commentContent && commentActions) {
        // 원본 댓글 내용 숨기기
        commentContent.style.display = 'none';
        // 수정/삭제 버튼 숨기기
        commentActions.style.display = 'none';
        // 수정 폼 보이기
        editForm.style.display = 'block';

        // 텍스트 영역에 포커스 + 커서를 맨 끝으로
        const textarea = editForm.querySelector('textarea');
        if (textarea) {
            textarea.focus();
            textarea.setSelectionRange(textarea.value.length, textarea.value.length);
        }
//        ```
//        **역할**: "수정" 버튼 클릭 시 실행되는 함수
//
//        **동작 순서**:
//        ```
//        [기존 화면]
//        ┌──────────────────────┐
//        │ 댓글 내용: 안녕하세요    │
//        │ [수정] [삭제]          │
//        └──────────────────────┘
//
//        ↓ editComment() 실행
//
//        [수정 모드]
//        ┌──────────────────────┐
//        │ ┌──────────────────┐ │
//        │ │ 안녕하세요▋        │ │ ← textarea (커서 맨 끝)
//        │ └──────────────────┘ │
//        │ [저장] [취소]         │
//        └──────────────────────┘
//        핵심 동작:
//
//        button.closest('.comment-item'): "수정" 버튼에서 가장 가까운 댓글 전체 영역 찾기
//        원본 내용과 버튼들은 숨기고 → 수정 폼은 보이게
//        입력창에 자동으로 포커스 + 커서를 텍스트 끝으로 이동
//        setSelectionRange: 커서 위치를 (끝, 끝)으로 설정
    }
}

/**
 * 댓글 수정 취소
 */
function cancelEditComment(commentId) {
    console.log('❌ 댓글 수정 취소: ' + commentId);

    // 수정 폼 숨기기
    // DOM 요소들 찾기
    const editForm = document.getElementById('editForm-' + commentId);
    const commentItem = editForm.closest('.comment-item');
    const commentContent = commentItem.querySelector('.comment-content');
    const commentActions = commentItem.querySelector('.comment-actions');

    if (editForm && commentContent && commentActions) {
        // 수정 폼 숨기기
        editForm.style.display = 'none';
        // 원본 댓글 내용 보이기
        commentContent.style.display = 'block';
        // 수정/삭제 버튼 보이기
        commentActions.style.display = 'flex';
    }
//    ```
//    **역할**: "취소" 버튼 클릭 시 실행되는 함수
//
//    **동작**: editComment()의 반대
//    ```
//    [수정 모드]
//    ┌──────────────────────┐
//    │ ┌──────────────────┐ │
//    │ │ 수정된 내용...     │ │
//    │ └──────────────────┘ │
//    │ [저장] [취소]         │
//    └──────────────────────┘
//
//    ↓ cancelEditComment() 실행
//
//    [원래 화면]
//    ┌──────────────────────┐
//    │ 댓글 내용: 안녕하세요   │ ← 원본 그대로 복구
//    │ [수정] [삭제]         │
//    └──────────────────────┘
//    왜 필요한가요?
//
//    수정하다가 마음이 바뀌면 취소할 수 있어야 함
//    원본 내용은 그대로 유지
}

// 부드러운 스크롤
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth',     // 부드럽게 스크롤
                block: 'start'          // 화면 상단에 위치
            });
        }
    });
});
//역할: #댓글 같은 링크 클릭 시 부드럽게 스크롤
//예시 상황:
//html<a href="#comments">댓글로 이동</a>
//
//...
//
//<div id="comments">
//  댓글 목록...
//</div>
//기본 동작: 클릭 → 바로 "툭" 하고 점프
//이 코드 적용: 클릭 → 부드럽게 "슈웅~" 스크롤
//작동 원리:
//
//a[href^="#"]: href가 #으로 시작하는 모든 링크 찾기
//e.preventDefault(): 기본 점프 동작 막기
//scrollIntoView({ behavior: 'smooth' }): 부드럽게 스크롤 이동
//
//
//🎯 전체 흐름 요약
//댓글 작성 과정:
//
//사용자가 댓글 입력
//실시간 글자 수 표시 (3번 블록)
//450자 넘으면 주황색 경고
//"등록" 버튼 클릭
//유효성 검사 (4번 블록)
//
//빈 댓글? → 경고
//2글자 미만? → 경고
//500자 초과? → 경고
//
//
//통과하면 서버로 전송
//
//댓글 수정 과정:
//
//"수정" 버튼 클릭
//editComment() 실행 (5번 블록)
//
//원본 내용 숨기고 → 수정 폼 표시
//
//
//내용 수정
//"취소" 버튼 클릭?
//
//cancelEditComment() 실행 (6번 블록)
//수정 �폐기하고 원본 표시
//
//
//"저장" 버튼 클릭?
//
//서버로 전송
//
//
//
//
//💡 핵심 개념
//DOM 탐색 패턴:
//javascript// 1. 특정 버튼에서 출발
//button.closest('.comment-item')  // 위로 올라가며 찾기
//      .querySelector('.comment-content')  // 그 안에서 찾기
//
//closest(): 부모 방향으로 올라가며 찾기
//querySelector(): 자식 방향으로 내려가며 찾기
//
//실시간 입력 감지:
//javascripttextarea.addEventListener('input', function() {
//    // 타이핑할 때마다 실행
//});
//폼 제출 가로채기:
//javascriptform.addEventListener('submit', function(e) {
//    e.preventDefault(); // 제출 막기
//    // 검증 로직...
//    // 통과하면 제출 진행
//});