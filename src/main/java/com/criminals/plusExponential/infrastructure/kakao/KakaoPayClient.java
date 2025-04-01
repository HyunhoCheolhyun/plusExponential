package com.criminals.plusExponential.infrastructure.kakao;
import com.criminals.plusExponential.application.dto.kakao.ApproveRequestDto;
import com.criminals.plusExponential.application.dto.kakao.PaymentRequestDto;
import com.criminals.plusExponential.application.dto.kakao.PaymentResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class KakaoPayClient {

    @Value("${PAYMENT_SECRET_KEY}")
    private  String PAYMENT_SECRET_KEY;
    private static final String HOST = "https://open-api.kakaopay.com";
    private static final String PAYMENT_READY_URL = "/online/v1/payment/ready";
    private static final String PAYMENT_APPROVE_URL = "/online/v1/payment/approve";

    /**
     * 결제 URL 요청
     * @param fare
     * @return PaymentResponseDto
     */
    public PaymentResponseDto getPayment(int fare){
        return WebClient.create(HOST +PAYMENT_READY_URL)
                .post()
                .header("Authorization", "SECRET_KEY " + PAYMENT_SECRET_KEY)
                .bodyValue(new PaymentRequestDto(fare))
                .retrieve() // 요청을 전송
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("getPayment Error: {}", body);
                                    return Mono.error(new RuntimeException("Client error: " + body));
                                }))// 예외처리
                .bodyToMono(PaymentResponseDto.class) // 응답값
                .block();
    }

    /**
     * 결제 승인 (승인되면 true 반환)
     * @param tid
     * @param pgToken
     * @return Boolean
     */
    public boolean getApprove(String tid, String pgToken){
        //
        return Boolean.TRUE.equals(WebClient.create(HOST + PAYMENT_APPROVE_URL)
                .post()
                .header("Authorization", "SECRET_KEY " + PAYMENT_SECRET_KEY)
                .bodyValue(new ApproveRequestDto(tid, pgToken))
                .retrieve()
                .toBodilessEntity() // 응답 본문을 무시하고 상태 코드만 확인
                .map(response -> true) // 성공적인 응답이면 true 반환
                .block());

    }

}
