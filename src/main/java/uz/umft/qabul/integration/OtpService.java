package uz.umft.qabul.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OtpService {

    private static final String PARSE_MODE = "HTML";
    private static final String MESSAGE_TEMPLATE = "Phone number: <b>%s</b>\nYour otp: <code>%s</code>";

    private final String otpToken;
    private final RestClient restClient;

    public OtpService(
            RestClient.Builder builder,
            @Value("${qabul.otp.url}") String otpServiceUrl,
            @Value("${qabul.otp.token}") String otpToken
    ) {
        this.otpToken = otpToken;
        this.restClient = builder.baseUrl(otpServiceUrl)
                .build();
    }

    public void sendOtp(String phoneNumber, String otp) {
        if (otpToken == null || otpToken.isBlank()) {
            return;
        }
        String message = MESSAGE_TEMPLATE.formatted(phoneNumber, otp);
//        String tempUser1 = "968242298";
//        sendMessage(tempUser1, message);
//        String tempUser2 = "1531443832";
//        sendMessage(tempUser2, message);
    }

    private void sendMessage(String chatId, String message) {
        restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{token}/sendMessage")
                        .queryParam("chat_id", chatId)
                        .queryParam("parse_mode", PARSE_MODE)
                        .queryParam("text", message)
                        .build(otpToken))
                .retrieve()
                .toBodilessEntity();
    }


}
