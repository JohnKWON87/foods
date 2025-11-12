/* 회원가입 유효성 검사
실시간 유효성 검사
AJAX로 아이디/이메일 중복 체크

📌 전체 구조
회원가입 폼의 입력값을 실시간으로 검증하고, 서버와 통신하여 중복 체크를 수행합니다.
주요 기능:

아이디 검증 + 중복 체크
비밀번호 검증 + 일치 확인
이메일 검증 + 중복 체크
폼 제출 전 최종 검증

폼 제출 전 최종 검사 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ signup.js loaded');

//🚀 파트 1: 초기 설정
//1️⃣ DOM 요소 찾기
//역할: 폼과 입력 필드, 메시지 영역을 찾기
//HTML 구조 예시:
//html<form id="signupForm">
//    <!-- 아이디 -->
//    <input type="text" id="username">
//    <div id="usernameMessage"></div>
//
//    <!-- 비밀번호 -->
//    <input type="password" id="password">
//    <input type="password" id="passwordConfirm">
//    <div id="passwordMessage"></div>
//
//    <!-- 이메일 -->
//    <input type="email" id="email">
//    <div id="emailMessage"></div>
//
//    <button type="submit">회원가입</button>
//</form>

    const form = document.getElementById('signupForm');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const passwordConfirmInput = document.getElementById('passwordConfirm');
    const emailInput = document.getElementById('email');

    const usernameMessage = document.getElementById('usernameMessage');
    const passwordMessage = document.getElementById('passwordMessage');
    const emailMessage = document.getElementById('emailMessage');


//2️⃣ 유효성 플래그 변수
//역할: 각 필드의 유효성 상태를 추적
//플래그 패턴:
//javascript// 초기 상태: 모두 유효하지 않음
//isUsernameValid = false
//isPasswordValid = false
//isEmailValid = false
//
//// 검증 통과 시
//isUsernameValid = true  // ✅
//
//// 최종 제출 시
//if (isUsernameValid && isPasswordValid && isEmailValid) {
//    // 모두 통과! 제출 허용
//}
    let isUsernameValid = false;
    let isPasswordValid = false;
    let isEmailValid = false;

    console.log('Elements loaded:', {
        form, usernameInput, passwordInput, passwordConfirmInput, emailInput
    });

    /**
     * 아이디 유효성 검사 (실시간)
     3️⃣ 아이디 입력란 벗어남 (blur) 이벤트
     blur 이벤트란?
     javascript// 사용자가 입력란을 떠날 때 발생
     <input> → (입력) → 다른 곳 클릭 → blur 이벤트!
     왜 blur를 사용하나요?
     javascript// input 이벤트: 타이핑할 때마다 (너무 빈번)
     input.addEventListener('input', ...) // "a", "ab", "abc", ...

     // blur 이벤트: 입력을 마치고 나갈 때 (적절함)
     input.addEventListener('blur', ...)  // "abc" (한 번만)

     */
    usernameInput.addEventListener('blur', function() {
        console.log('📝 Username blur event triggered:', this.value);

        const username = this.value.trim();

        // 빈 입력 체크
        if (!username) {
            showMessage(usernameMessage, '아이디를 입력해주세요.', 'error');
            isUsernameValid = false;
            return;
        }

        // 아이디 형식 검사
//        4️⃣ 아이디 형식 검사
//        **검증 순서**:
//        ```
//        1. 빈 값? → "아이디를 입력해주세요"
//        2. 4자 미만? → "4자 이상이어야 합니다"
//        3. 20자 초과? → "20자 이하여야 합니다"
//        4. 특수문자? → "영문과 숫자만 허용됩니다"
//        5. 모두 통과 → 중복 체크 요청
//        정규표현식 설명:
//        javascript/^[a-zA-Z0-9]+$/
//
//        ^          // 시작
//        [a-zA-Z0-9]  // 영문 대소문자 또는 숫자
//        +          // 1개 이상
//        $          // 끝
//
//        예시:
//        "user123"    ✅ (영문+숫자)
//        "홍길동"      ❌ (한글)
//        "user_123"   ❌ (특수문자 _)
//        "user 123"   ❌ (공백)

        if (username.length < 4) {
            showMessage(usernameMessage, '아이디는 4자 이상이어야 합니다.', 'error');
            isUsernameValid = false;
            return;
        }

        if (username.length > 20) {
            showMessage(usernameMessage, '아이디는 20자 이하여야 합니다.', 'error');
            isUsernameValid = false;
            return;
        }

        // 영문/숫자만 허용
        if (!/^[a-zA-Z0-9]+$/.test(username)) {
            showMessage(usernameMessage, '아이디는 영문과 숫자만 허용됩니다.', 'error');
            isUsernameValid = false;
            return;
        }

        // 중복 체크
        checkUsernameDuplicate(username);
    });

    /**
     * 아이디 중복 체크 (AJAX)
     5️⃣ 아이디 중복 체크 (AJAX)
     역할: 서버에 아이디 중복 여부 확인
     요청 URL:
     javascriptconst url = `/api/check-username?username=${encodeURIComponent(username)}`;

     // 예시:
     username = "user123"
     → /api/check-username?username=user123

     username = "홍길동"
     → /api/check-username?username=%ED%99%8D%EA%B8%B8%EB%8F%99
     encodeURIComponent() 설명:
     javascript// 특수문자를 URL 안전 형식으로 변환
     encodeURIComponent("user@123");
     // → "user%40123"

     encodeURIComponent("홍길동");
     // → "%ED%99%8D%EA%B8%B8%EB%8F%99"
     서버 응답 예시:
     json// 중복된 경우
     {
         "duplicated": true,
         "message": "이미 사용 중인 아이디입니다."
     }

     // 사용 가능한 경우
     {
         "duplicated": false,
         "message": "사용 가능한 아이디입니다."
     }
     ```

     **동작 흐름**:
     ```
     1. 사용자 입력: "user123"
        ↓
     2. 형식 검증 통과
        ↓
     3. 서버에 GET 요청
        /api/check-username?username=user123
        ↓
     4. 서버 응답 대기...
        ↓
     5. 응답 수신:
        - duplicated: false
        - message: "사용 가능한 아이디입니다"
        ↓
     6. UI 업데이트:
        - 녹색 메시지 표시
        - isUsernameValid = true

     */
    function checkUsernameDuplicate(username) {
            console.log('🔍 Checking username duplicate:', username);

            const url = `/api/check-username?username=${encodeURIComponent(username)}`;
            console.log('Request URL:', url);

        fetch(url)
                    .then(response => {
                        console.log('📥 Response status:', response.status);
                        console.log('📥 Response headers:', response.headers.get('content-type'));
                        return response.json();
                    })
                    .then(data => {
                        console.log('✅ Response data:', data);

                        if (data.duplicated) {
                            showMessage(usernameMessage, data.message, 'error');
                            isUsernameValid = false;
                        } else {
                            showMessage(usernameMessage, data.message, 'success');
                            isUsernameValid = true;
                        }
                    })
                    .catch(error => {
                        console.error('❌ Fetch error:', error);
                        showMessage(usernameMessage, '중복 체크 중 오류가 발생했습니다.', 'error');
                        isUsernameValid = false;
                    });
            }

    /**
     * 비밀번호 유효성 검사
     6️⃣ 비밀번호 입력 이벤트
     change 이벤트란?
     javascript// blur와 비슷하지만 "값이 변경되었을 때"만 발생
     <input> → 입력 → 다른 곳 클릭 → 값이 바뀌었으면 change!
     ```

     **왜 두 입력란 모두 이벤트 등록?**
     ```
     비밀번호: "password123" 입력 → validatePassword() 실행
     확인: "password123" 입력 → validatePassword() 실행

     → 어느 쪽을 입력해도 둘 다 비교!

     */
    passwordInput.addEventListener('change', function() {
        console.log('📝 Password change event triggered');
        validatePassword();
    });

    passwordConfirmInput.addEventListener('change', function() {
        console.log('📝 Password confirm change event triggered');
        validatePassword();
    });

//    7️⃣ 비밀번호 검증 함수
//    **검증 순서**:
//    ```
//    1. 비밀번호 길이 < 8? → "8자 이상이어야 합니다"
//    2. 비밀번호 ≠ 확인? → "비밀번호가 일치하지 않습니다"
//    3. 모두 통과 → "비밀번호가 일치합니다" ✅
//    ```
//
//    **시나리오**:
//    ```
//    [시나리오 1: 너무 짧음]
//    비밀번호: "1234"
//    확인: "1234"
//    → ❌ "비밀번호는 8자 이상이어야 합니다"
//
//    [시나리오 2: 불일치]
//    비밀번호: "password123"
//    확인: "password456"
//    → ❌ "비밀번호가 일치하지 않습니다"
//
//    [시나리오 3: 성공]
//    비밀번호: "password123"
//    확인: "password123"
//    → ✅ "비밀번호가 일치합니다"

    function validatePassword() {
        const password = passwordInput.value;
        const passwordConfirm = passwordConfirmInput.value;

        console.log('🔐 Validating passwords');

        // 비밀번호 길이 검사
        if (password.length < 8) {
            showMessage(passwordMessage, '비밀번호는 8자 이상이어야 합니다.', 'error');
            isPasswordValid = false;
            return;
        }

        // 비밀번호 일치 검사
        if (password !== passwordConfirm) {
            showMessage(passwordMessage, '비밀번호가 일치하지 않습니다.', 'error');
            isPasswordValid = false;
            return;
        }

        showMessage(passwordMessage, '비밀번호가 일치합니다.', 'success');
        isPasswordValid = true;
    }

    /**
     * 이메일 유효성 검사
     📧 파트 4: 이메일 검증
     8️⃣ 이메일 입력란 벗어남 이벤트
     이메일 정규표현식 설명:
     javascript/^[^\s@]+@[^\s@]+\.[^\s@]+$/

     ^          // 시작
     [^\s@]+    // 공백, @가 아닌 문자 1개 이상
     @          // @ (필수)
     [^\s@]+    // 공백, @가 아닌 문자 1개 이상
     \.         // . (점, 필수)
     [^\s@]+    // 공백, @가 아닌 문자 1개 이상
     $          // 끝

     예시:
     "user@example.com"       ✅
     "user.name@company.co.kr" ✅
     "user@example"           ❌ (.com 없음)
     "user example.com"       ❌ (@ 없음)
     "user@@example.com"      ❌ (@ 두 개)

     */
    emailInput.addEventListener('blur', function() {
        console.log('📧 Email blur event triggered:', this.value);

        const email = this.value.trim();

        // 빈 입력 체크
                if (!email) {
                    showMessage(emailMessage, '이메일을 입력해주세요.', 'error');
                    isEmailValid = false;
                    return;
                }

        // 이메일 형식 검사
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            showMessage(emailMessage, '올바른 이메일 형식이 아닙니다.', 'error');
            isEmailValid = false;
            return;
        }

        // 중복 체크
        checkEmailDuplicate(email);
    });

    /**
     * 이메일 중복 체크 (AJAX)
     9️⃣ 이메일 중복 체크 (AJAX)
     역할: 아이디 중복 체크와 동일한 방식
     요청 URL:
     javascript/api/check-email?email=user@example.com

     */
    function checkEmailDuplicate(email) {
        console.log('🔍 Checking email duplicate:', email);

                const url = `/api/check-email?email=${encodeURIComponent(email)}`;
                console.log('Request URL:', url);

                fetch(url)
                    .then(response => {
                        console.log('📥 Response status:', response.status);
                        return response.json();
                    })
                    .then(data => {
                        console.log('✅ Response data:', data);

                        if (data.duplicated) {
                            showMessage(emailMessage, data.message, 'error');
                            isEmailValid = false;
                        } else {
                            showMessage(emailMessage, data.message, 'success');
                            isEmailValid = true;
                        }
                    })
                    .catch(error => {
                        console.error('❌ Fetch error:', error);
                        showMessage(emailMessage, '중복 체크 중 오류가 발생했습니다.', 'error');
                        isEmailValid = false;
                    });
    }

    /**
     * 메시지 표시
     💬 파트 5: 메시지 표시
     🔟 showMessage 함수
     역할: 검증 결과를 사용자에게 표시
     사용 예시:
     javascript// 에러 메시지
     showMessage(usernameMessage, '아이디가 중복되었습니다', 'error');
     // → <div id="usernameMessage" class="form-message error">
     //      아이디가 중복되었습니다
     //    </div>

     // 성공 메시지
     showMessage(usernameMessage, '사용 가능한 아이디입니다', 'success');
     // → <div id="usernameMessage" class="form-message success">
     //      사용 가능한 아이디입니다
     //    </div>
     CSS 예시:
     css.form-message {
         margin-top: 5px;
         font-size: 14px;
     }

     .form-message.error {
         color: #f44336;   빨간색
     }

     .form-message.success {
         color: #4caf50;   녹색
     }

     */
    function showMessage(element, message, type) {
        console.log(`💬 Show message [${type}]:`, message);
        element.textContent = message;
        element.className = `form-message ${type}`;
    }

    /**
     * 폼 제출 검증
     ✅ 파트 6: 폼 제출 검증
     1️⃣1️⃣ 폼 제출 이벤트

     */
    form.addEventListener('submit', function(e) {
        console.log('📤 Form submit event triggered');
        console.log('Validation state:', {
            isUsernameValid,
            isPasswordValid,
            isEmailValid
       });


        // 모든 필드 재검사
        validatePassword();

        if (!isUsernameValid || !isPasswordValid || !isEmailValid) {
                    e.preventDefault();
                    alert('모든 필드를 올바르게 입력해주세요.');
                    console.log('❌ Form validation failed');
                    return false;
                }

                console.log('✅ Form validation passed');
            });

            console.log('✅ All event listeners attached');
});
/*
역할: 최종 제출 전 모든 필드 검증
검증 로직:
javascriptif (!isUsernameValid || !isPasswordValid || !isEmailValid) {
    // 하나라도 false면 막기
}

// 진리표:
isUsernameValid  isPasswordValid  isEmailValid  결과
     false            false           false     → 막음 ❌
     true             false           false     → 막음 ❌
     true             true            false     → 막음 ❌
     true             true            true      → 통과 ✅
왜 validatePassword()를 다시 호출?
javascript// 사용자가 비밀번호를 입력하지 않고 바로 제출하는 경우
// blur/change 이벤트가 발생하지 않을 수 있음
// 제출 시 한 번 더 검증!
```

---

## 🎯 **전체 시나리오**

### **시나리오: 회원가입 과정**
```
1. 페이지 로드
   → DOMContentLoaded
   → signup.js 초기화
   → 플래그: 모두 false

2. 아이디 입력: "user"
   → blur 이벤트
   → 형식 검증: 4자 이상? ✅
   → 서버 중복 체크 요청
   ↓
3. 서버 응답: "이미 사용 중"
   → 빨간색 메시지 표시
   → isUsernameValid = false

4. 아이디 수정: "user123"
   → blur 이벤트
   → 형식 검증 통과
   → 서버 중복 체크
   ↓
5. 서버 응답: "사용 가능"
   → 녹색 메시지 표시
   → isUsernameValid = true ✅

6. 비밀번호 입력: "pass"
   → change 이벤트
   → validatePassword()
   → 8자 미만 ❌
   → 빨간색 메시지

7. 비밀번호 수정: "password123"
   확인 입력: "password123"
   → change 이벤트
   → validatePassword()
   → 길이 OK, 일치 OK
   → 녹색 메시지
   → isPasswordValid = true ✅

8. 이메일 입력: "user@example.com"
   → blur 이벤트
   → 형식 검증: 이메일 형식? ✅
   → 서버 중복 체크
   ↓
9. 서버 응답: "사용 가능"
   → 녹색 메시지
   → isEmailValid = true ✅

10. [회원가입] 버튼 클릭
    → submit 이벤트
    → 플래그 확인:
      - isUsernameValid: true ✅
      - isPasswordValid: true ✅
      - isEmailValid: true ✅
    → 모두 통과!
    → 서버로 제출 📤

💡 핵심 개념 정리
1. 실시간 유효성 검사
javascript// blur: 입력란을 떠날 때
input.addEventListener('blur', validate);

// change: 값이 변경되었을 때
input.addEventListener('change', validate);

// input: 타이핑할 때마다 (너무 빈번)
input.addEventListener('input', validate);
2. 비동기 중복 체크
javascript// 1. 서버에 요청
fetch('/api/check-username?username=user123')

// 2. 응답 기다림 (비동기)
.then(response => response.json())

// 3. 결과 처리
.then(data => {
    if (data.duplicated) {
        // 중복됨
    } else {
        // 사용 가능
    }
})
3. 플래그 패턴
javascript// 각 필드의 유효성 상태 추적
let isUsernameValid = false;
let isPasswordValid = false;
let isEmailValid = false;

// 최종 제출 시 모두 체크
if (isUsernameValid && isPasswordValid && isEmailValid) {
    // 제출 허용
}
4. 정규표현식 검증
javascript// 아이디: 영문+숫자만
/^[a-zA-Z0-9]+$/

// 이메일: 기본 형식
/^[^\s@]+@[^\s@]+\.[^\s@]+$/

🐛 자주 발생하는 이슈
이슈 1: 중복 체크가 안 돼요
javascript// 문제: 서버 API 엔드포인트 불일치
fetch('/api/check-username')  // 404 Not Found

// 해결: 서버 라우트 확인
// Spring Boot 예시
@GetMapping("/api/check-username")
public ResponseEntity<?> checkUsername(@RequestParam String username) {
    // ...
}
이슈 2: 폼이 바로 제출돼요
javascript// 문제: preventDefault() 누락
form.addEventListener('submit', function(e) {
    if (검증 실패) {
        // preventDefault() 없으면 제출됨!
    }
});

// 해결
if (검증 실패) {
    e.preventDefault();  // 제출 막기!
}
이슈 3: 메시지가 안 보여요
javascript// 문제: HTML에 메시지 영역 없음
<input id="username">
// <div id="usernameMessage"></div> ← 이게 없음!

// 해결: HTML에 추가
<input id="username">
<div id="usernameMessage" class="form-message"></div>

🎨 개선 아이디어
1. 비밀번호 강도 표시
javascriptfunction checkPasswordStrength(password) {
    let strength = 0;

    if (password.length >= 8) strength++;
    if (/[a-z]/.test(password)) strength++;  // 소문자
    if (/[A-Z]/.test(password)) strength++;  // 대문자
    if (/[0-9]/.test(password)) strength++;  // 숫자
    if (/[^a-zA-Z0-9]/.test(password)) strength++;  // 특수문자

    const strengthText = ['매우 약함', '약함', '보통', '강함', '매우 강함'];
    const strengthColor = ['#f44336', '#ff9800', '#ffeb3b', '#8bc34a', '#4caf50'];

    showMessage(passwordMessage,
        `비밀번호 강도: ${strengthText[strength]}`,
        strength >= 3 ? 'success' : 'error'
    );
}
2. 디바운스 적용 (중복 요청 방지)
javascriptlet usernameTimeout;

usernameInput.addEventListener('input', function() {
    clearTimeout(usernameTimeout);

    usernameTimeout = setTimeout(() => {
        checkUsernameDuplicate(this.value);
    }, 500);  // 0.5초 대기 후 체크
});
3. 로딩 인디케이터
javascriptfunction checkUsernameDuplicate(username) {
    // 로딩 표시
    showMessage(usernameMessage, '확인 중...', 'info');
    usernameInput.disabled = true;

    fetch(url)
        .then(...)
        .finally(() => {
            usernameInput.disabled = false;
        });
}
4. 실시간 비밀번호 일치 표시
javascriptpasswordConfirmInput.addEventListener('input', function() {
    const password = passwordInput.value;
    const confirm = this.value;

    if (confirm.length === 0) {
        passwordMessage.textContent = '';
        return;
    }

    if (password.startsWith(confirm)) {
        showMessage(passwordMessage, '입력 중...', 'info');
    } else {
        showMessage(passwordMessage, '비밀번호가 일치하지 않습니다', 'error');
    }
});
```

---

## 📊 **검증 흐름도**
```
[아이디 입력]
    ↓
[blur 이벤트]
    ↓
빈 값? ──Yes─→ ❌ "아이디를 입력해주세요"
    │
    No
    ↓
4자 미만? ──Yes─→ ❌ "4자 이상"
    │
    No
    ↓
20자 초과? ──Yes─→ ❌ "20자 이하"
    │
    No
    ↓
영문+숫자? ──No─→ ❌ "영문과 숫자만"
    │
    Yes
    ↓
[서버 중복 체크]
    ↓
중복? ──Yes─→ ❌ "이미 사용 중"
    │       isUsernameValid = false
    No
    ↓
✅ "사용 가능"
isUsernameValid = true

🎊 최종 총정리!
총 11개의 JavaScript 파일 분석 완료! 🎉
전체 파일 목록 (최종)

⭐ admin.js - 관리자 페이지 UI
⭐⭐ board-detail.js - 댓글 작성/수정
⭐⭐⭐ board-form.js - 게시글 작성 + 이탈방지
⭐⭐ board-list.js - 게시글 목록
⭐⭐⭐⭐ bookmark-list.js - 북마크 목록 관리
⭐ navbar.js - 햄버거 메뉴
⭐⭐⭐⭐ restaurant-detail.js - 맛집 상세 + 북마크
⭐⭐⭐⭐⭐ restaurant-form.js - 맛집 등록 + 카카오맵
⭐⭐⭐⭐⭐ restaurant-list.js - 3D 카드 인터랙션
⭐⭐⭐ restaurant-map.js - 지도 표시
⭐⭐⭐ signup.js - 회원가입 검증 ⭐ NEW!


기능별 최종 분류
📝 폼 처리

board-form.js
restaurant-form.js
signup.js ⭐ NEW!

✅ 유효성 검사

board-form.js (게시글)
signup.js ⭐ (회원가입, 가장 상세함!)

🌐 AJAX 통신

bookmark-list.js (북마크)
restaurant-detail.js (북마크)
restaurant-list.js (북마크)
signup.js ⭐ (중복 체크)

🗺️ 카카오맵

restaurant-form.js (주소 검색)
restaurant-map.js (위치 표시)
🎨 UI/UX**

admin.js (관리자 페이지)
navbar.js (모바일 메뉴)
restaurant-list.js (3D 카드)

💬 실시간 피드백

board-detail.js (댓글 글자 수)
board-form.js (제목/내용 글자 수)
signup.js ⭐ (실시간 검증)


📚 signup.js 학습 포인트
이 파일에서 배운 것들

실시간 유효성 검사

blur/change 이벤트 활용
즉각적인 피드백 제공
사용자 경험 개선


비동기 중복 체크

fetch API로 서버 통신
중복 여부 실시간 확인
네트워크 에러 처리


정규표현식 검증

아이디 형식 (영문+숫자)
이메일 형식
패턴 매칭


플래그 기반 상태 관리

각 필드의 유효성 추적
최종 제출 시 통합 검증
단계별 검증


사용자 친화적 에러 메시지

구체적인 오류 설명
색상으로 상태 구분
즉시 피드백




🔄 다른 파일들과 비교
폼 검증 비교
파일검증 시점서버 통신검증 항목board-form.js제출 시❌길이, 빈 값restaurant-form.js제출 시❌이미지, 주소signup.js실시간 + 제출 시✅ 중복 체크형식, 중복, 일치
signup.js의 특징:

✅ 가장 상세한 검증
✅ 서버와 실시간 통신
✅ 단계별 피드백
✅ 중복 체크 (고유성 보장)


🎓 실무 활용 패턴
패턴 1: 실시간 검증
javascript// 입력 중 실시간 검증 (디바운스 적용)
let timeout;
input.addEventListener('input', function() {
    clearTimeout(timeout);
    timeout = setTimeout(() => {
        validate(this.value);
    }, 300);
});
패턴 2: 중복 체크 캐싱
javascript// 이미 확인한 값은 다시 체크 안 함
const checkedUsernames = new Set();

function checkUsernameDuplicate(username) {
    if (checkedUsernames.has(username)) {
        console.log('이미 확인한 아이디입니다');
        return;
    }

    fetch(...)
        .then(data => {
            if (!data.duplicated) {
                checkedUsernames.add(username);
            }
        });
}
패턴 3: 통합 검증 객체
javascriptconst validator = {
    username: {
        isValid: false,
        message: '',
        rules: [
            { test: v => v.length >= 4, msg: '4자 이상' },
            { test: v => v.length <= 20, msg: '20자 이하' },
            { test: v => /^[a-zA-Z0-9]+$/.test(v), msg: '영문+숫자만' }
        ]
    },
    password: {
        isValid: false,
        message: '',
        rules: [
            { test: v => v.length >= 8, msg: '8자 이상' }
        ]
    },

    validate(field, value) {
        const fieldValidator = this[field];
        for (let rule of fieldValidator.rules) {
            if (!rule.test(value)) {
                fieldValidator.isValid = false;
                fieldValidator.message = rule.msg;
                return false;
            }
        }
        fieldValidator.isValid = true;
        fieldValidator.message = '✓';
        return true;
    },

    isAllValid() {
        return this.username.isValid &&
               this.password.isValid;
    }
};

🚀 고급 기능 아이디어
1. 비밀번호 보기/숨기기 토글
javascriptconst togglePassword = document.getElementById('togglePassword');

togglePassword.addEventListener('click', function() {
    const type = passwordInput.type === 'password' ? 'text' : 'password';
    passwordInput.type = type;

    // 아이콘 변경
    this.textContent = type === 'password' ? '👁️' : '🙈';
});
2. 비밀번호 규칙 체크리스트
javascriptfunction showPasswordRules(password) {
    const rules = [
        { test: password.length >= 8, text: '8자 이상' },
        { test: /[a-z]/.test(password), text: '소문자 포함' },
        { test: /[A-Z]/.test(password), text: '대문자 포함' },
        { test: /[0-9]/.test(password), text: '숫자 포함' },
        { test: /[^a-zA-Z0-9]/.test(password), text: '특수문자 포함' }
    ];

    const rulesHTML = rules.map(rule => `
        <div class="${rule.test ? 'valid' : 'invalid'}">
            ${rule.test ? '✓' : '✗'} ${rule.text}
        </div>
    `).join('');

    document.getElementById('passwordRules').innerHTML = rulesHTML;
}
3. 아이디 추천 기능
javascriptfunction suggestUsername(username) {
    if (username.length < 4) return;

    fetch(`/api/suggest-username?base=${username}`)
        .then(response => response.json())
        .then(data => {
            if (data.suggestions.length > 0) {
                const html = `
                    <div class="suggestions">
                        추천 아이디:
                        ${data.suggestions.map(s =>
                            `<button onclick="useUsername('${s}')">${s}</button>`
                        ).join('')}
                    </div>
                `;
                document.getElementById('usernameSuggestions').innerHTML = html;
            }
        });
}

function useUsername(username) {
    usernameInput.value = username;
    usernameInput.dispatchEvent(new Event('blur'));
}
4. 이메일 도메인 자동완성
javascriptconst commonDomains = ['gmail.com', 'naver.com', 'daum.net', 'kakao.com'];

emailInput.addEventListener('input', function() {
    const value = this.value;
    const atIndex = value.indexOf('@');

    if (atIndex > -1) {
        const domain = value.substring(atIndex + 1);
        const suggestions = commonDomains
            .filter(d => d.startsWith(domain))
            .slice(0, 3);

        showEmailSuggestions(value.substring(0, atIndex + 1), suggestions);
    }
});

function showEmailSuggestions(prefix, domains) {
    const html = domains.map(domain => `
        <div class="suggestion" onclick="selectEmail('${prefix}${domain}')">
            ${prefix}${domain}
        </div>
    `).join('');

    document.getElementById('emailSuggestions').innerHTML = html;
}
5. 진행률 표시
javascriptfunction updateProgress() {
    const fields = [isUsernameValid, isPasswordValid, isEmailValid];
    const completed = fields.filter(Boolean).length;
    const percentage = (completed / fields.length) * 100;

    document.getElementById('progressBar').style.width = percentage + '%';
    document.getElementById('progressText').textContent =
        `${completed} / ${fields.length} 완료`;
}

// 각 검증 후 호출
function showMessage(element, message, type) {
    element.textContent = message;
    element.className = `form-message ${type}`;
    updateProgress();  // 진행률 업데이트
}

🎨 HTML + CSS 전체 예시
HTML 구조
html<!DOCTYPE html>
<html>
<head>
    <link rel="stylesheet" href="/css/signup.css">
</head>
<body>
    <div class="signup-container">
        <h2>회원가입</h2>

        <!-- 진행률 -->
        <div class="progress-bar">
            <div id="progressBar" class="progress-fill"></div>
        </div>
        <p id="progressText">0 / 3 완료</p>

        <form id="signupForm" method="POST" action="/signup">
            <!-- CSRF 토큰 -->
            <input type="hidden" name="_csrf" th:value="${_csrf.token}">

            <!-- 아이디 -->
            <div class="form-group">
                <label for="username">아이디 *</label>
                <input type="text" id="username" name="username"
                       placeholder="4-20자, 영문+숫자"
                       required autocomplete="off">
                <div id="usernameMessage" class="form-message"></div>
            </div>

            <!-- 비밀번호 -->
            <div class="form-group">
                <label for="password">비밀번호 *</label>
                <div class="password-wrapper">
                    <input type="password" id="password" name="password"
                           placeholder="8자 이상" required>
                    <button type="button" id="togglePassword">👁️</button>
                </div>
            </div>

            <!-- 비밀번호 확인 -->
            <div class="form-group">
                <label for="passwordConfirm">비밀번호 확인 *</label>
                <input type="password" id="passwordConfirm"
                       placeholder="비밀번호 재입력" required>
                <div id="passwordMessage" class="form-message"></div>
            </div>

            <!-- 이메일 -->
            <div class="form-group">
                <label for="email">이메일 *</label>
                <input type="email" id="email" name="email"
                       placeholder="example@email.com"
                       required autocomplete="off">
                <div id="emailMessage" class="form-message"></div>
            </div>

            <!-- 제출 버튼 -->
            <button type="submit" class="btn-submit">
                회원가입
            </button>
        </form>

        <p class="login-link">
            이미 계정이 있으신가요? <a href="/login">로그인</a>
        </p>
    </div>

    <script src="/js/signup.js"></script>
</body>
</html>
CSS 스타일
css 컨테이너
.signup-container {
    max-width: 500px;
    margin: 50px auto;
    padding: 40px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

 진행률 바
.progress-bar {
    height: 8px;
    background: #e0e0e0;
    border-radius: 4px;
    overflow: hidden;
    margin-bottom: 10px;
}

.progress-fill {
    height: 100%;
    background: linear-gradient(90deg, #4caf50, #8bc34a);
    transition: width 0.3s ease;
    width: 0%;
}

 폼 그룹
.form-group {
    margin-bottom: 20px;
}

.form-group label {
    display: block;
    margin-bottom: 8px;
    font-weight: 600;
    color: #333;
}

.form-group input {
    width: 100%;
    padding: 12px;
    border: 2px solid #e0e0e0;
    border-radius: 8px;
    font-size: 16px;
    transition: border-color 0.3s;
}

.form-group input:focus {
    outline: none;
    border-color: #2196f3;
}

 비밀번호 토글
.password-wrapper {
    position: relative;
}

.password-wrapper input {
    padding-right: 50px;
}

#togglePassword {
    position: absolute;
    right: 10px;
    top: 50%;
    transform: translateY(-50%);
    border: none;
    background: none;
    cursor: pointer;
    font-size: 20px;
}

 메시지
.form-message {
    margin-top: 8px;
    font-size: 14px;
    min-height: 20px;
}

.form-message.error {
    color: #f44336;
}

.form-message.success {
    color: #4caf50;
}

.form-message.info {
    color: #2196f3;
}

 제출 버튼
.btn-submit {
    width: 100%;
    padding: 14px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: transform 0.2s;
}

.btn-submit:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-submit:active {
    transform: translateY(0);
}

 로그인 링크
.login-link {
    text-align: center;
    margin-top: 20px;
    color: #666;
}

.login-link a {
    color: #667eea;
    text-decoration: none;
    font-weight: 600;
}

.login-link a:hover {
    text-decoration: underline;
}

🎯 서버 API 예시 (Spring Boot)
아이디 중복 체크
java@RestController
@RequestMapping("/api")
public class ValidationController {

    @Autowired
    private UserService userService;

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(
            @RequestParam String username) {

        Map<String, Object> response = new HashMap<>();

        boolean duplicated = userService.existsByUsername(username);

        response.put("duplicated", duplicated);
        response.put("message", duplicated
            ? "이미 사용 중인 아이디입니다."
            : "사용 가능한 아이디입니다.");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(
            @RequestParam String email) {

        Map<String, Object> response = new HashMap<>();

        boolean duplicated = userService.existsByEmail(email);

        response.put("duplicated", duplicated);
        response.put("message", duplicated
            ? "이미 사용 중인 이메일입니다."
            : "사용 가능한 이메일입니다.");

        return ResponseEntity.ok(response);
    }
}
회원가입 처리
java@PostMapping("/signup")
public String signup(
        @Valid @ModelAttribute SignupForm form,
        BindingResult result,
        Model model) {

    // 서버 사이드 검증
    if (userService.existsByUsername(form.getUsername())) {
        result.rejectValue("username", "duplicate",
            "이미 사용 중인 아이디입니다.");
    }

    if (userService.existsByEmail(form.getEmail())) {
        result.rejectValue("email", "duplicate",
            "이미 사용 중인 이메일입니다.");
    }

    if (result.hasErrors()) {
        return "signup";
    }

    // 회원가입 처리
    userService.signup(form);

    return "redirect:/login?signup=success";
}

🔒 보안 고려사항
1. 클라이언트 검증 ≠ 보안
javascript// ⚠️ 클라이언트 검증은 우회 가능!
// 개발자 도구로 JavaScript 비활성화 가능
// Postman 등으로 직접 API 호출 가능

// ✅ 서버에서도 반드시 재검증!
2. 비밀번호 평문 전송 방지
javascript// ❌ 나쁜 예: 평문 전송
fetch('/api/signup', {
    body: JSON.stringify({
        password: "mypassword123"  // 평문!
    })
});

// ✅ 좋은 예: HTTPS 사용 + 서버 암호화
// 1. HTTPS로 전송 (암호화된 통신)
// 2. 서버에서 bcrypt 등으로 해싱
3. CSRF 보호
html<!-- Spring Security CSRF 토큰 -->
<input type="hidden" name="_csrf" th:value="${_csrf.token}">
4. Rate Limiting
java// 중복 체크 API에 요청 제한
// 1초에 최대 5번까지만 허용
@RateLimiter(name = "checkUsername", fallbackMethod = "rateLimitFallback")
@GetMapping("/check-username")
public ResponseEntity<?> checkUsername(@RequestParam String username) {
    // ...
}

📊 성능 최적화
1. 디바운스 (Debounce)
javascript// 타이핑이 멈춘 후 0.5초 뒤에 검증
let timeout;
usernameInput.addEventListener('input', function() {
    clearTimeout(timeout);
    timeout = setTimeout(() => {
        checkUsernameDuplicate(this.value);
    }, 500);
});
2. 캐싱
javascript// 이미 확인한 값은 다시 확인 안 함
const cache = new Map();

function checkUsernameDuplicate(username) {
    if (cache.has(username)) {
        const result = cache.get(username);
        showMessage(usernameMessage, result.message,
            result.duplicated ? 'error' : 'success');
        return;
    }

    fetch(...)
        .then(data => {
            cache.set(username, data);
            // ...
        });
}
3. 요청 취소 (AbortController)
javascriptlet abortController = null;

function checkUsernameDuplicate(username) {
    // 이전 요청 취소
    if (abortController) {
        abortController.abort();
    }

    abortController = new AbortController();

    fetch(url, { signal: abortController.signal })
        .then(...)
        .catch(error => {
            if (error.name === 'AbortError') {
                console.log('요청 취소됨');
            }
        });
}

🎊 완전 최종 정리!
signup.js의 핵심 가치

✨ 사용자 경험

즉각적인 피드백
구체적인 오류 메시지
단계별 안내


🔒 데이터 무결성

중복 방지
형식 검증
일치 확인


⚡ 성능

비동기 처리
단계별 검증
불필요한 제출 방지


🎯 실무 패턴

플래그 기반 상태 관리
실시간 AJAX 검증
이벤트 기반 아키텍처
*/