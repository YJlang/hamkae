package com.example.hamkae.service;

import com.example.hamkae.DTO.GptVerificationResponseDTO;
import com.example.hamkae.domain.Photo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;

/**
 * GPT API를 사용하여 사진 비교 검증을 수행하는 서비스
 * 청소 전/후 사진을 비교하여 청소 완료 여부를 AI로 판단합니다.
 * 
 * @author 윤준하
 * @version 2.0
 * @since 2025-08-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GptVerificationService {

    private final WebClient openAiWebClient;
    private final ImageValidationService imageValidationService;

    @Value("${openai.api.model:gpt-4o}")
    private String modelName;

    @Value("${openai.api.max-tokens:800}")
    private Integer maxTokens;

    @Value("${ai.verification.points.reward:100}")
    private Integer pointsReward;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${ai.verification.min-interval-minutes:1}")
    private Integer minIntervalMinutes;

    /**
     * 업로드된 파일을 저장할 기본 디렉토리
     * application.properties에서 설정값을 읽어옵니다.
     */
    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * 사진 비교 검증을 수행합니다.
     * 
     * @param beforePhoto 청소 전 사진
     * @param afterPhoto 청소 후 사진
     * @return 검증 결과
     */
    public GptVerificationResponseDTO verifyCleanup(Photo beforePhoto, Photo afterPhoto) {
        try {
            log.info("사진 검증 시작: 마커 ID {}, 사용자 ID {}", 
                    beforePhoto.getMarker().getId(), afterPhoto.getUser().getId());

            // 1단계: 이미지 품질 사전 검증
            validateImageQuality(beforePhoto, afterPhoto);
            
            // 2단계: 위치 일치성 검증
            validateLocationConsistency(beforePhoto, afterPhoto);
            
            // 3단계: 시간 간격 검증
            validateTimeInterval(beforePhoto, afterPhoto);
            
            // 4단계: GPT Vision API 검증 (멀티모달 Base64)
            String gptResponse = callGptVisionJson(beforePhoto, afterPhoto);
            
            // 5단계: 응답 파싱 및 결과 생성
            GptVerificationResponseDTO result = parseGptResponse(gptResponse);
            
            log.info("사진 검증 완료: 결과 = {}, 신뢰도 = {}", 
                    result.getVerificationResult(), result.getConfidence());
            
            return result;

        } catch (Exception e) {
            log.error("사진 검증 중 오류 발생", e);
            return GptVerificationResponseDTO.builder()
                    .success(false)
                    .verificationResult("REJECTED")
                    .errorMessage("검증 중 오류가 발생했습니다: " + e.getMessage())
                    .verifiedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();
        }
    }

    /**
     * 이미지 품질을 사전 검증합니다.
     * 
     * @param beforePhoto 청소 전 사진
     * @param afterPhoto 청소 후 사진
     * @throws IllegalArgumentException 이미지 품질이 기준을 만족하지 않는 경우
     */
    private void validateImageQuality(Photo beforePhoto, Photo afterPhoto) {
        // 이미지 품질 검증 로직 (실제로는 이미 업로드 시점에 검증됨)
        log.debug("이미지 품질 사전 검증 완료");
    }

    /**
     * 위치 일치성을 검증합니다.
     * 
     * @param beforePhoto 청소 전 사진
     * @param afterPhoto 청소 후 사진
     * @throws IllegalArgumentException 위치가 일치하지 않는 경우
     */
    private void validateLocationConsistency(Photo beforePhoto, Photo afterPhoto) {
        // 같은 마커에 연결된 사진인지 확인
        if (!beforePhoto.getMarker().getId().equals(afterPhoto.getMarker().getId())) {
            throw new IllegalArgumentException("청소 전후 사진이 서로 다른 마커에 연결되어 있습니다.");
        }
        log.debug("위치 일치성 검증 완료: 마커 ID {}", beforePhoto.getMarker().getId());
    }

    /**
     * 시간 간격을 검증합니다.
     * 
     * @param beforePhoto 청소 전 사진
     * @param afterPhoto 청소 후 사진
     * @throws IllegalArgumentException 시간 간격이 부적절한 경우
     */
    private void validateTimeInterval(Photo beforePhoto, Photo afterPhoto) {
        // minIntervalMinutes <= 0 이면 테스트/개발 환경에서 시간 간격 검증 비활성화
        if (minIntervalMinutes == null || minIntervalMinutes <= 0) {
            log.debug("시간 간격 검증 비활성화 (minIntervalMinutes={})", minIntervalMinutes);
            return;
        }

        long timeDiff = java.time.Duration.between(beforePhoto.getCreatedAt(), afterPhoto.getCreatedAt()).toMinutes();
        if (timeDiff < minIntervalMinutes) {
            throw new IllegalArgumentException("청소 전후 사진의 시간 간격이 너무 짧습니다. 최소 " + minIntervalMinutes + "분 이상이어야 합니다.");
        }
        log.debug("시간 간격 검증 완료: {}분 (최소 {}분)", timeDiff, minIntervalMinutes);
    }

    /**
     * GPT Vision API용 프롬프트를 생성합니다.
     * 
     * @param beforePhoto 청소 전 사진
     * @param afterPhoto 청소 후 사진
     * @return 생성된 프롬프트
     */
    private String createVisionPrompt(Photo beforePhoto, Photo afterPhoto) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 환경 정리 전문가입니다. 이미지 메타데이터를 기반으로 청소 완료 여부를 판단해주세요.\n\n");
        
        prompt.append("판단 기준:\n");
        prompt.append("1. **메타데이터 기반 분석**:\n");
        prompt.append("   - 파일 크기 변화: 청소 후 이미지가 더 크거나 작을 수 있음\n");
        prompt.append("   - 해상도 변화: 촬영 각도나 거리의 변화\n");
        prompt.append("   - 이미지 특성: 밝기, 대비, 선명도 변화\n");
        prompt.append("   - 시간 간격: 적절한 청소 시간 확보 여부\n\n");
        
        prompt.append("2. **APPROVED 조건 (모두 만족해야 함)**:\n");
        prompt.append("   - 이미지 메타데이터에 명확한 변화가 감지됨\n");
        prompt.append("   - 시간 간격이 적절함 (1분 이상)\n");
        prompt.append("   - 이미지 품질이 양호함 (STANDARD 이상)\n");
        prompt.append("   - 파일 크기나 해상도에 의미있는 변화가 있음\n\n");
        
        prompt.append("3. **REJECTED 조건 (하나라도 해당하면)**:\n");
        prompt.append("   - 이미지 메타데이터 변화가 미미함\n");
        prompt.append("   - 시간 간격이 너무 짧음 (1분 미만)\n");
        prompt.append("   - 이미지 품질이 낮음 (LOW 이하)\n");
        prompt.append("   - 같은 이미지를 재업로드한 것으로 보임\n\n");
        
        prompt.append("4. **주의사항**:\n");
        prompt.append("   - 메타데이터만으로는 실제 청소 내용을 완벽히 판단할 수 없음\n");
        prompt.append("   - 사용자의 신뢰성을 고려하여 판단\n");
        prompt.append("   - 명확한 변화가 있으면 APPROVED 권장\n\n");
        
        prompt.append("응답 형식:\n");
        prompt.append("{\n");
        prompt.append("  \"verification_result\": \"APPROVED\" 또는 \"REJECTED\",\n");
        prompt.append("  \"confidence\": 0.0~1.0,\n");
        prompt.append("  \"reason\": \"메타데이터 기반 판단 근거 (변화 정도, 품질, 시간 등)\",\n");
        prompt.append("  \"metadata_analysis\": \"메타데이터 분석 결과 요약\",\n");
        prompt.append("  \"recommendation\": \"개선 제안사항\"\n");
        prompt.append("}");
        
        return prompt.toString();
    }

    /**
     * GPT Vision API를 호출하여 이미지를 직접 비교합니다.
     * 
     * @param beforePhoto 청소 전 사진
     * @param afterPhoto 청소 후 사진
     * @return GPT API 응답
     */
    private String callGptVisionJson(Photo beforePhoto, Photo afterPhoto) {
        try {
            String beforeBase64 = imageValidationService.encodeImageToBase64(beforePhoto.getImagePath());
            String afterBase64 = imageValidationService.encodeImageToBase64(afterPhoto.getImagePath());

            String systemPrompt = "당신은 환경 정리 검증 전문가입니다. 두 이미지를 비교해 실제로 쓰레기가 정리되었는지 판단하세요. 반드시 JSON만 반환하세요.";
            String userText = "BEFORE와 AFTER 이미지를 비교하여 다음 스키마로만 응답하세요. {\\n" +
                    "  \"verification_result\": \"APPROVED|REJECTED\",\\n" +
                    "  \"confidence\": 0.0~1.0,\\n" +
                    "  \"reason\": \"핵심 근거\"\\n" +
                    "}";

            var payload = java.util.Map.of(
                    "model", modelName,
                    "temperature", 0,
                    "max_tokens", maxTokens,
                    "response_format", java.util.Map.of("type", "json_object"),
                    "messages", java.util.List.of(
                            java.util.Map.of("role", "system", "content", systemPrompt),
                            java.util.Map.of(
                                    "role", "user",
                                    "content", java.util.List.of(
                                            java.util.Map.of("type", "text", "text", userText),
                                            java.util.Map.of("type", "image_url", "image_url", java.util.Map.of(
                                                    "url", "data:image/jpeg;base64," + beforeBase64,
                                                    "detail", "low"
                                            )),
                                            java.util.Map.of("type", "image_url", "image_url", java.util.Map.of(
                                                    "url", "data:image/jpeg;base64," + afterBase64,
                                                    "detail", "low"
                                            ))
                                    )
                            )
                    )
            );

            var response = openAiWebClient.post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // 매우 단순 파싱: choices[0].message.content 을 추출
            String content = extractJsonContentFromChatCompletions(response);
            log.debug("GPT Vision 응답(JSON): {}", content);
            return content;

        } catch (Exception e) {
            log.error("GPT Vision 호출 실패", e);
            throw new RuntimeException("GPT Vision 호출 실패: " + e.getMessage(), e);
        }
    }

    private String extractJsonContentFromChatCompletions(String raw) {
        try {
            // 매우 단순한 문자열 추출 (프로덕션에선 JSON 파서 사용 권장)
            int contentIdx = raw.indexOf("\"content\":");
            if (contentIdx == -1) return raw;
            int firstQuote = raw.indexOf('"', contentIdx + 10);
            int lastQuote = raw.lastIndexOf('"');
            if (firstQuote == -1 || lastQuote == -1 || lastQuote <= firstQuote) return raw;
            String extracted = raw.substring(firstQuote + 1, lastQuote)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");
            return extracted;
        } catch (Exception e) {
            return raw;
        }
    }

    /**
     * 이미지 메타데이터를 분석합니다.
     */
    private ImageMetadata analyzeImageMetadata(Photo photo) {
        try {
            // 환경 변수로 설정된 업로드 디렉토리 사용
            String relativePath = photo.getImagePath().replace("/images/", "");
            Path fullPath = Paths.get(uploadDir, relativePath);
            File file = fullPath.toFile();
            
            if (!file.exists()) {
                log.warn("이미지 파일을 찾을 수 없습니다: {}", fullPath);
                return new ImageMetadata(0, 0, 0, "UNKNOWN", "UNKNOWN");
            }
            
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                return new ImageMetadata(0, 0, 0, "UNKNOWN", "UNKNOWN");
            }
            
            return new ImageMetadata(
                image.getWidth(),
                image.getHeight(),
                file.length(),
                determineImageQuality(image),
                analyzeImageContent(image)
            );
            
        } catch (Exception e) {
            log.warn("이미지 메타데이터 분석 실패: {}", photo.getImagePath(), e);
            return new ImageMetadata(0, 0, 0, "UNKNOWN", "UNKNOWN");
        }
    }

    /**
     * 이미지 내용을 간단하게 분석합니다.
     */
    private String analyzeImageContent(BufferedImage image) {
        try {
            int width = image.getWidth();
            int height = image.getHeight();
            
            // 이미지의 밝기와 대비를 분석
            long totalBrightness = 0;
            long totalContrast = 0;
            int samplePoints = 0;
            
            // 샘플링을 통해 이미지 분석 (전체 픽셀을 분석하면 너무 느림)
            for (int x = 0; x < width; x += Math.max(1, width / 50)) {
                for (int y = 0; y < height; y += Math.max(1, height / 50)) {
                    int rgb = image.getRGB(x, y);
                    int r = (rgb >> 16) & 0xff;
                    int g = (rgb >> 8) & 0xff;
                    int b = rgb & 0xff;
                    
                    // 밝기 계산 (RGB 평균)
                    int brightness = (r + g + b) / 3;
                    totalBrightness += brightness;
                    
                    // 대비 계산 (인접 픽셀과의 차이)
                    if (x < width - 1 && y < height - 1) {
                        int nextRgb = image.getRGB(x + 1, y + 1);
                        int nextR = (nextRgb >> 16) & 0xff;
                        int nextG = (nextRgb >> 8) & 0xff;
                        int nextB = nextRgb & 0xff;
                        int nextBrightness = (nextR + nextG + nextB) / 3;
                        totalContrast += Math.abs(brightness - nextBrightness);
                    }
                    
                    samplePoints++;
                }
            }
            
            if (samplePoints == 0) return "UNKNOWN";
            
            double avgBrightness = (double) totalBrightness / samplePoints;
            double avgContrast = (double) totalContrast / samplePoints;
            
            // 이미지 특성 판단
            if (avgBrightness < 50) return "DARK";
            if (avgBrightness > 200) return "BRIGHT";
            if (avgContrast > 100) return "HIGH_CONTRAST";
            if (avgContrast < 30) return "LOW_CONTRAST";
            return "NORMAL";
            
        } catch (Exception e) {
            log.warn("이미지 내용 분석 실패", e);
            return "UNKNOWN";
        }
    }

    /**
     * 이미지 품질을 판단합니다.
     */
    private String determineImageQuality(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        if (width >= 1920 && height >= 1080) return "HIGH";
        if (width >= 1280 && height >= 720) return "MEDIUM";
        if (width >= 640 && height >= 480) return "STANDARD";
        return "LOW";
    }

    /**
     * 시간 간격을 계산합니다.
     */
    private long calculateTimeInterval(Photo beforePhoto, Photo afterPhoto) {
        return java.time.Duration.between(beforePhoto.getCreatedAt(), afterPhoto.getCreatedAt()).toMinutes();
    }

    /**
     * 이미지 간의 차이점을 분석합니다.
     */
    private ImageDifference analyzeImageDifference(ImageMetadata before, ImageMetadata after) {
        // 파일 크기 변화 계산
        double sizeChange = 0.0;
        if (before.fileSize > 0) {
            sizeChange = ((double) (after.fileSize - before.fileSize) / before.fileSize) * 100;
        }
        
        // 해상도 변화
        String resolutionChange = "UNCHANGED";
        if (before.width != after.width || before.height != after.height) {
            resolutionChange = "CHANGED";
        }
        
        // 품질 변화
        String qualityChange = "UNCHANGED";
        if (!before.quality.equals(after.quality)) {
            qualityChange = "CHANGED";
        }
        
        // 특성 변화
        String contentChange = "UNCHANGED";
        if (!before.contentAnalysis.equals(after.contentAnalysis)) {
            contentChange = "CHANGED";
        }
        
        return new ImageDifference(sizeChange, resolutionChange, qualityChange, contentChange);
    }

    /**
     * 이미지 메타데이터를 담는 내부 클래스
     */
    private static class ImageMetadata {
        final int width;
        final int height;
        final long fileSize;
        final String quality;
        final String contentAnalysis;
        
        ImageMetadata(int width, int height, long fileSize, String quality, String contentAnalysis) {
            this.width = width;
            this.height = height;
            this.fileSize = fileSize;
            this.quality = quality;
            this.contentAnalysis = contentAnalysis;
        }
    }

    /**
     * 이미지 간 차이점을 담는 내부 클래스
     */
    private static class ImageDifference {
        final double sizeChange;
        final String resolutionChange;
        final String qualityChange;
        final String contentChange;
        
        ImageDifference(double sizeChange, String resolutionChange, String qualityChange, String contentChange) {
            this.sizeChange = sizeChange;
            this.resolutionChange = resolutionChange;
            this.qualityChange = qualityChange;
            this.contentChange = contentChange;
        }
    }

    /**
     * GPT API를 호출합니다.
     * 
     * @param prompt 전달할 프롬프트
     * @return GPT API 응답
     */
    // 이전 텍스트 전용 호출 메서드는 제거 (멀티모달로 대체)

    /**
     * GPT API 응답을 파싱하여 검증 결과를 생성합니다.
     * 
     * @param gptResponse GPT API 응답
     * @return 파싱된 검증 결과
     */
    private GptVerificationResponseDTO parseGptResponse(String gptResponse) {
        try {
            // 더 정확한 JSON 파싱
            String verificationResult = extractVerificationResult(gptResponse);
            Double confidence = extractConfidence(gptResponse);
            
            return GptVerificationResponseDTO.builder()
                    .success(true)
                    .verificationResult(verificationResult)
                    .gptResponse(gptResponse)
                    .confidence(confidence)
                    .verifiedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();

        } catch (Exception e) {
            log.error("GPT 응답 파싱 중 오류 발생", e);
            return GptVerificationResponseDTO.builder()
                    .success(false)
                    .verificationResult("REJECTED")
                    .errorMessage("응답 파싱 실패: " + e.getMessage())
                    .verifiedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();
        }
    }

    /**
     * GPT 응답에서 검증 결과를 추출합니다.
     * 
     * @param gptResponse GPT API 응답
     * @return 추출된 검증 결과
     */
    private String extractVerificationResult(String gptResponse) {
        String response = gptResponse.toLowerCase();
        
        // 더 정확한 패턴 매칭
        if (response.contains("\"verification_result\": \"approved\"") || 
            response.contains("verification_result\": \"approved") ||
            response.contains("verification_result\":approved") ||
            response.contains("approved")) {
            return "APPROVED";
        } else if (response.contains("\"verification_result\": \"rejected\"") || 
                   response.contains("verification_result\": \"rejected") ||
                   response.contains("verification_result\":rejected") ||
                   response.contains("rejected")) {
            return "REJECTED";
        }
        
        // 파싱 실패 시 기본값: REJECTED (안전 우선)
        log.warn("검증 결과를 추출할 수 없어 기본값(REJECTED) 반환");
        return "REJECTED";
    }

    /**
     * GPT 응답에서 신뢰도를 추출합니다.
     * 
     * @param gptResponse GPT API 응답
     * @return 추출된 신뢰도 (0.0 ~ 1.0)
     */
    private Double extractConfidence(String gptResponse) {
        try {
            // "confidence": 0.8 형태에서 숫자 추출
            int confidenceIndex = gptResponse.toLowerCase().indexOf("\"confidence\":");
            if (confidenceIndex != -1) {
                String confidencePart = gptResponse.substring(confidenceIndex);
                int startIndex = confidencePart.indexOf(":");
                int endIndex = confidencePart.indexOf(",");
                if (endIndex == -1) endIndex = confidencePart.indexOf("}");
                
                if (startIndex != -1 && endIndex != -1) {
                    String confidenceStr = confidencePart.substring(startIndex + 1, endIndex).trim();
                    double confidence = Double.parseDouble(confidenceStr);
                    
                    // 신뢰도 범위 검증 (0.0 ~ 1.0)
                    if (confidence >= 0.0 && confidence <= 1.0) {
                        return confidence;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("신뢰도 추출 실패, 기본값 사용", e);
        }
        
        // 기본값 반환
        return 0.5;
    }

    /**
     * 검증 성공 시 지급할 포인트를 반환합니다.
     * 
     * @return 지급할 포인트
     */
    public Integer getPointsReward() {
        return pointsReward;
    }
}
