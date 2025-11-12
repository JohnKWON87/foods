package com.hyukmin.foods.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
/**
 * FileUploadService - 파일 업로드/삭제 처리
 *
 * 이 파일 역할: 사진 저장/삭제/검증
 *
 * 데이터 흐름:
 * 사진선택 → Controller → Service(여기! 파일저장) → 로컬폴더 → 경로반환 → DB저장
 */
@Service
public class FileUploadService {

    // application.properties에서 설정한 업로드 경로
    @Value("${file.upload-dir:uploads/images}")
    private String uploadDir;

    /**
     * 파일 저장
     * @param file 업로드된 파일
     * @return 저장된 파일 경로(예: "/uploads/images/uuid.jpg")
     */
    public String saveFile(MultipartFile file) throws IOException {
        // 1. 폴더 없으면 생성
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();     // uploads/images 폴더 생성
            System.out.println("📁 폴더 생성: " + directory.getAbsolutePath());
        }

        // 2. 파일명 생성 (중복 방지)
        String originalFilename = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String savedFilename = uuid + extension; // "a1b2c3d4-....jpg"

        // 3. 파일 저장
        Path filePath = Paths.get(uploadDir, savedFilename);
        Files.write(filePath, file.getBytes());

        // 4. 웹 경로 반환 (DB에 저장할 경로)
        String webPath = "/uploads/images/" + savedFilename;

        System.out.println("✅ 파일 저장 완료!");
        System.out.println("   - 실제 경로: " + filePath.toAbsolutePath());
        System.out.println("   - 웹 경로: " + webPath);

        return webPath;  // ⚠️ 이 부분 수정!
    }

    /**
     * 파일 삭제
     * @param filePath 삭제할 파일 경로 (예: "/uploads/images/uuid.jpg")
     */
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        try {
            // 웹 경로 → 실제 경로 변환
            // "/uploads/images/uuid.jpg" → "uploads/images/uuid.jpg"
            String actualPath = filePath;


            if (actualPath.startsWith("/uploads/images/")) {
                actualPath = actualPath.substring(1);  // "/" 제거
            }

            Path path = Paths.get(actualPath);
            boolean deleted = Files.deleteIfExists(path); // 파일 삭제

            if (deleted) {
                System.out.println("✅ 파일 삭제 성공: " + filePath);
            } else {
                System.err.println("⚠️ 파일을 찾을 수 없음: " + filePath);
                System.err.println("   - 변환된 경로: " + actualPath);
                System.err.println("   - 존재 여부: " + Files.exists(path));
            }
        } catch (IOException e) {
            System.err.println("❌ 파일 삭제 실패: " + filePath);
            e.printStackTrace();
        }
    }

    /**
     * 이미지 파일인지 검증
     *
     * @param file 업로드된 파일
     * @return 이미지면 true, 아니면 false
     */
    public boolean isImageFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }

        // 확장자 추출 (예: "pizza.jpg" → "jpg")
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

        // jpg, jpeg, png, gif, webp만 허용
        return extension.equals("jpg") || extension.equals("jpeg") ||
                extension.equals("png") || extension.equals("gif") ||
                extension.equals("webp");
    }
}

/*
 * 핵심 역할:
 *
 * 1. 파일 저장
 *    - 폴더 자동 생성 (없으면)
 *    - UUID로 중복 방지 (같은 파일명 여러번 업로드해도 괜찮음)
 *    - 웹 경로 반환 (DB에 저장)
 *
 * 2. 파일 삭제
 *    - 맛집 삭제 시 사진도 함께 삭제
 *
 * 3. 검증
 *    - 이미지 파일만 업로드 가능
 *
 *
 * 동작 흐름 예시:
 *
 * 맛집 사진 등록:
 * 1. 사용자: "pizza.jpg" 선택
 * 2. Controller → saveFile(file)
 * 3. UUID 생성: "a1b2c3d4-e5f6-..."
 * 4. 파일명: "a1b2c3d4-e5f6-....jpg"
 * 5. 저장 위치: "uploads/images/a1b2c3d4-e5f6-....jpg"
 * 6. 반환: "/uploads/images/a1b2c3d4-e5f6-....jpg"
 * 7. DB에 경로 저장
 * 8. HTML에서: <img src="/uploads/images/a1b2c3d4-e5f6-....jpg">
 *
 *
 * UUID 사용 이유:
 *
 * ❌ 원본 파일명 그대로:
 * - "pizza.jpg" 여러번 업로드 → 덮어쓰기 문제
 * - 한글 파일명 → 깨짐 문제
 *
 * ✅ UUID 사용:
 * - "a1b2c3d4-....jpg" → 절대 중복 안됨
 * - 영문+숫자만 → 깨짐 없음
 */