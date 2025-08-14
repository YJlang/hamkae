package com.example.hamkae.service;

import com.example.hamkae.DTO.GptVerificationRequestDTO;
import com.example.hamkae.DTO.GptVerificationResponseDTO;
import com.example.hamkae.domain.Photo;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * GPT API를 사용하여 사진 비교 검증을 수행하는 서비스
 * 청소 전/후 사진을 비교하여 청소 완료 여부를 AI로 판단합니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GptVerificationService {

    private final OpenAiService openAiService;

    @Value("${openai.api.model:gpt-4o-mini}")
    private String modelName;

    @Value("${openai.api.max-tokens:1000}")
    private Integer maxTokens;

    @Value("${ai.verification.points.reward:100}")
    private Integer pointsReward;

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

            // GPT API에 전달할 프롬프트 생성
            String prompt = createVerificationPrompt(beforePhoto, afterPhoto);
            
            // GPT API 호출
            String gptResponse = callGptApi(prompt);
            
            // GPT 응답을 파싱하여 검증 결과 생성
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
     * GPT API에 전달할 프롬프트를 생성합니다.
     * 
     * @param beforePhoto 청소 전 사진
     * @param afterPhoto 청소 후 사진
     * @return 생성된 프롬프트
     */
    private String createVerificationPrompt(Photo beforePhoto, Photo afterPhoto) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 현실적인 환경 보호 전문가입니다. ");
        prompt.append("두 장의 사진을 비교하여 쓰레기 청소 노력이 있었는지 현실적으로 판단해주세요.\n");
        prompt.append("길거리 청소는 완벽할 수 없으며, 청소 노력과 개선 정도를 중점적으로 봐주세요.\n\n");
        
        prompt.append("청소 전 사진: ").append(beforePhoto.getImagePath()).append("\n");
        prompt.append("청소 후 사진: ").append(afterPhoto.getImagePath()).append("\n");
        prompt.append("위치: ").append(beforePhoto.getMarker().getDescription()).append("\n\n");
        
        prompt.append("다음 형식으로 응답해주세요:\n");
        prompt.append("{\n");
        prompt.append("  \"verification_result\": \"APPROVED\" 또는 \"REJECTED\",\n");
        prompt.append("  \"confidence\": 0.0~1.0 사이의 신뢰도,\n");
        prompt.append("  \"reason\": \"판단 근거를 간단히 설명\"\n");
        prompt.append("}\n\n");
        
        prompt.append("APPROVED 기준 (현실적인 길거리 청소 기준):\n");
        prompt.append("- 주요 쓰레기가 제거되었을 때 (완벽하지 않아도 됨)\n");
        prompt.append("- 청소 노력이 명확하게 보일 때\n");
        prompt.append("- 환경이 개선되었을 때 (완전히 깨끗하지 않아도 됨)\n");
        prompt.append("- 자연스러운 환경 요소(나뭇잎, 흙 등)는 허용\n");
        prompt.append("- 작은 쓰레기나 미세한 오염은 허용\n\n");
        
        prompt.append("REJECTED 기준:\n");
        prompt.append("- 주요 쓰레기가 여전히 대부분 남아있을 때\n");
        prompt.append("- 청소 노력이 전혀 보이지 않을 때\n");
        prompt.append("- 사진이 불분명하거나 부적절할 때\n");
        prompt.append("- 청소 전후 차이가 전혀 없을 때");
        
        return prompt.toString();
    }

    /**
     * GPT API를 호출합니다.
     * 
     * @param prompt 전달할 프롬프트
     * @return GPT API 응답
     */
    private String callGptApi(String prompt) {
        try {
            ChatMessage message = new ChatMessage("user", prompt);
            
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(modelName)
                    .messages(List.of(message))
                    .maxTokens(maxTokens)
                    .temperature(0.3) // 더 유연한 판단을 위해 온도 조정
                    .build();

            String response = openAiService.createChatCompletion(request)
                    .getChoices().get(0).getMessage().getContent();
            
            log.debug("GPT API 응답: {}", response);
            return response;

        } catch (Exception e) {
            log.error("GPT API 호출 중 오류 발생", e);
            throw new RuntimeException("GPT API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * GPT API 응답을 파싱하여 검증 결과를 생성합니다.
     * 
     * @param gptResponse GPT API 응답
     * @return 파싱된 검증 결과
     */
    private GptVerificationResponseDTO parseGptResponse(String gptResponse) {
        try {
            // 간단한 JSON 파싱 (실제로는 Jackson ObjectMapper 사용 권장)
            boolean isApproved = gptResponse.toLowerCase().contains("\"verification_result\": \"approved\"");
            boolean isRejected = gptResponse.toLowerCase().contains("\"verification_result\": \"rejected\"");
            
            String verificationResult = isApproved ? "APPROVED" : 
                                     isRejected ? "REJECTED" : "REJECTED";
            
            // 신뢰도 추출 (간단한 방식)
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
                    return Double.parseDouble(confidenceStr);
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
