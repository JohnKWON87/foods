package com.hyukmin.foods.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
/**
 * WebConfig - 웹 설정 파일
 *
 * 이 파일이 하는 일:
 * 1. 이미지, CSS, JS 파일을 브라우저에서 볼 수 있게 연결
 * 2. 업로드된 이미지 파일 경로 설정
 *
 * 왜 필요한가?
 * - Spring Boot는 기본적으로 /static 폴더만 공개함
 * - 사용자가 업로드한 이미지는 /uploads 폴더에 저장되므로 별도 설정 필요
 */

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // application.properties에서 설정한 업로드 경로 가져오기
    @Value("${file.upload-dir:uploads/images}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ✅ CSS, JS 정적 리소스 추가
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");

        // 업로드된 이미지 파일 연결
        File uploadPath = new File(uploadDir);
        String absolutePath = uploadPath.getAbsolutePath().replace("\\", "/");

        // 경로 끝에 / 추가 (필수)
        if (!absolutePath.endsWith("/")) {
            absolutePath += "/";
        }
        // 개발 중 경로 확인용 로그
        System.out.println("====================================");
        System.out.println("🖼️ 이미지 서빙 경로 설정");
        System.out.println("   URL: /uploads/images/**");
        System.out.println("   실제 경로: file:" + absolutePath);
        System.out.println("====================================");

        // 이미지 경로 연결: /uploads/images/abc.jpg → 실제 파일 시스템의 uploads/images/abc.jpg
        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations("file:" + absolutePath);
    }
}
/*
 * ========== 실제로 어떻게 동작하나요? ==========
 *
 * 예시 1) HTML에서 이미지 표시
 *   <img src="/uploads/images/restaurant_123.jpg">
 *   → 실제로는 프로젝트 폴더의 uploads/images/restaurant_123.jpg 파일을 보여줌
 *
 * 예시 2) CSS 파일 불러오기
 *   <link href="/css/style.css">
 *   → src/main/resources/static/css/style.css 파일을 불러옴
 *
 * 예시 3) 파일 업로드 후 접근
 *   1. 사용자가 이미지 업로드
 *   2. FileUploadService가 uploads/images/abc.jpg에 저장
 *   3. 브라우저에서 /uploads/images/abc.jpg 접속
 *   4. 이 설정 덕분에 파일이 보임 ✅
 *
 * 이 설정이 없으면?
 *   → 이미지를 업로드해도 브라우저에서 404 에러 발생 ❌
 */