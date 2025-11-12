// ========== 관리자 페이지 스크립트 ==========

document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ 관리자 페이지 로드 완료');
// 역할: HTML이 완전히 로드된 후에 실행
// 페이지의 모든 요소가 준비되면 안의 코드들이 실행돼요

    // ========== 자동으로 메시지 숨기기 (3초 후) ==========
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
    /* 역할: 알림 메시지를 자동으로 사라지게 함

       .alert 클래스를 가진 모든 요소를 찾음
       3초 후 투명해지기 시작 (0.5초 동안)
       투명해진 후 0.5초 뒤 완전히 제거
       예: "저장되었습니다" 같은 메시지가 자동으로 사라짐*/

    // ========== 테이블 행 하이라이트 ==========
    const tableRows = document.querySelectorAll('.admin-table tbody tr');
    tableRows.forEach(row => {
        row.addEventListener('mouseenter', function() {
            this.style.background = '#f0f7ff';
        });

        row.addEventListener('mouseleave', function() {
            this.style.background = '';
        });
    });
    /* 역할: 마우스를 올리면 행의 배경색 변경

       마우스가 행 위에 올라가면 → 연한 파란색 배경
       마우스가 벗어나면 → 원래대로
       UX 향상: 어떤 행을 보고 있는지 명확하게 보임 */

    // ========== 검색 폼 유효성 검사 ==========
    const searchForm = document.querySelector('.search-form');
    if (searchForm) {
        searchForm.addEventListener('submit', function(e) {
            const keyword = this.querySelector('input[name="keyword"]').value.trim();

            if (keyword && keyword.length < 2) {
                e.preventDefault();
                alert('검색어는 2글자 이상 입력해주세요.');
                return false;
            }
        });
    }
    /* 역할: 검색어가 너무 짧으면 검색 방지

       검색 버튼을 누를 때 실행
       검색어가 2글자 미만이면 → 경고창 + 검색 취소
       이유: 1글자 검색은 결과가 너무 많아서 비효율적 */

    // ========== 통계 카드 애니메이션 ==========
    const statCards = document.querySelectorAll('.stat-card');
    statCards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';

        setTimeout(() => {
            card.style.transition = 'all 0.5s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * 100);
    });
    /* 역할: 통계 카드들이 순차적으로 나타나는 애니메이션

       처음엔 투명하고 아래쪽에 위치
       0.1초 간격으로 하나씩 나타남
       아래→위로 슬라이드하며 페이드인
       효과: 첫 번째 카드 → 0초 후, 두 번째 → 0.1초 후, 세 번째 → 0.2초 후... */

    // ========== 삭제 확인 강화 ==========
    const deleteForms = document.querySelectorAll('form[action*="/delete"]');
    deleteForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const confirmDelete = confirm(
                '⚠️ 정말 삭제하시겠습니까?\n\n' +
                '이 작업은 되돌릴 수 없습니다.\n' +
                '연관된 데이터도 함께 삭제될 수 있습니다.'
            );

            if (!confirmDelete) {
                e.preventDefault();
                return false;
            }
        });
    });
    /* 역할: 삭제 버튼 누를 때 재확인

       URL에 "/delete"가 포함된 모든 폼을 찾음
       확인 창에서 "취소" 누르면 → 삭제 안 됨
       확인 창에서 "확인" 누르면 → 삭제 진행
       안전장치: 실수로 삭제하는 것 방지 */

    // ========== 권한 변경 확인 ==========
    const roleForms = document.querySelectorAll('form[action*="/toggle-role"]');
    roleForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const confirmRole = confirm(
                '권한을 변경하시겠습니까?\n\n' +
                '관리자 권한을 부여하면 모든 콘텐츠를 수정/삭제할 수 있습니다.'
            );

            if (!confirmRole) {
                e.preventDefault();
                return false;
            }
        });
    });
    /* 역할: 사용자 권한 변경 시 확인

       관리자 권한 부여/해제 전 경고
       중요한 작업이라 한 번 더 확인 */

    // ========== 테이블 정렬 기능 (선택사항) ==========
    const tableHeaders = document.querySelectorAll('.admin-table th');
    tableHeaders.forEach((header, index) => {
        // 마지막 컬럼(관리)은 제외
        if (header.textContent.includes('관리')) return;

        header.style.cursor = 'pointer';
        header.title = '클릭하여 정렬';

        header.addEventListener('click', function() {
            sortTable(index);
        });
    });
    /* 역할: 테이블 헤더 클릭 시 정렬

       "관리" 컬럼은 제외 (정렬 불필요)
       마우스 커서가 포인터로 변경 (클릭 가능하다는 표시) */

    let sortDirection = {};

    function sortTable(columnIndex) {
        const table = document.querySelector('.admin-table');
        const tbody = table.querySelector('tbody');
        const rows = Array.from(tbody.querySelectorAll('tr'));
        // 1. 테이블의 모든 행을 배열로 가져옴


        const dataRows = rows.filter(row => !row.querySelector('td[colspan]')
        );
        // 2. 빈 행 제외 (예: "데이터가 없습니다" 메시지)

        if (dataRows.length === 0) return;


        sortDirection[columnIndex] = !sortDirection[columnIndex];
        // 3. 정렬 방향 토글 (오름차순 ↔ 내림차순)
        const isAscending = sortDirection[columnIndex];

        // 4. 실제 정렬
        dataRows.sort((a, b) => {
            const aValue = a.children[columnIndex].textContent.trim();
            const bValue = b.children[columnIndex].textContent.trim();

            // 숫자인 경우
            if (!isNaN(aValue) && !isNaN(bValue)) {
                return isAscending
                    ? Number(aValue) - Number(bValue)
                    : Number(bValue) - Number(aValue);
            }

            // 문자열인 경우
            return isAscending
                ? aValue.localeCompare(bValue)
                : bValue.localeCompare(aValue);
        });

        // 5. 정렬된 순서대로 다시 표시
        dataRows.forEach(row => tbody.appendChild(row));
        // 예: "가입일" 클릭 → 날짜순 정렬, 다시 클릭 → 역순 정렬

        console.log(`🔄 ${columnIndex}번 컬럼 정렬: ${isAscending ? '오름차순' : '내림차순'}`);
    }

    // ========== 통계 숫자 카운팅 애니메이션 ==========
    const statValues = document.querySelectorAll('.stat-value');
    statValues.forEach(stat => {
        const text = stat.textContent;
        const number = parseInt(text.replace(/[^0-9]/g, ''));

        if (!isNaN(number) && number > 0) {
            animateNumber(stat, number, text);
        }
    });

    function animateNumber(element, targetNumber, originalText) {
        const duration = 1000;                  // 1초
        const steps = 30;                       // 30단계
        const increment = targetNumber / steps; // 한 단계당 증가량
        let current = 0;
        let step = 0;

        const timer = setInterval(() => {
            current += increment;
            step++;

            if (step >= steps) {
                clearInterval(timer);
                element.textContent = originalText; // 최종값 표시
            } else {
                const suffix = originalText.replace(/[0-9]/g, '');
                element.textContent = Math.floor(current) + suffix;
            }
        }, duration / steps);                       // 약 33ms마다 실행
    }
    /* 역할: 숫자가 0에서 목표값까지 올라가는 애니메이션

       "회원 150명" 이라면 → 0, 5, 10, 15... 150 이렇게 증가
       효과: 페이지가 동적으로 느껴짐 */

    console.log('📊 관리자 페이지 스크립트 초기화 완료');
});

// ========== 테이블 검색 기능 (실시간 필터) ==========
function filterTable(inputId, tableId) {
    const input = document.getElementById(inputId);
    if (!input) return;

    input.addEventListener('keyup', function() {
        const filter = this.value.toLowerCase();            // 입력값
        const table = document.getElementById(tableId);
        const rows = table.querySelectorAll('tbody tr');

        rows.forEach(row => {
            const text = row.textContent.toLowerCase();
            // 검색어가 포함된 행만 표시
            row.style.display = text.includes(filter) ? '' : 'none';
        });
        /* 역할: 입력란에 타이핑하면 실시간으로 테이블 필터링

           검색어가 포함된 행만 보이고 나머지는 숨김
           예: "홍길동" 입력 → 이름에 "홍길동"이 있는 행만 표시 */
    });
}

console.log('✅ admin.js 로드 완료');
/* 🎯 요약
   이 파일은 관리자 페이지의 사용자 경험(UX)을 개선하는 스크립트예요:

   시각적 피드백: 알림 자동 삭제, 행 하이라이트, 애니메이션
   안전장치: 삭제/권한 변경 확인
   편의기능: 테이블 정렬, 실시간 검색
   유효성 검사: 검색어 길이 체크 */