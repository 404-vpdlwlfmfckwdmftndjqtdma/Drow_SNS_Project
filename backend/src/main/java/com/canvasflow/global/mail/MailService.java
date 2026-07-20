package com.canvasflow.global.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * 메일 발송 인프라. 지금은 auth 모듈의 비밀번호 재설정 메일에만 쓰이지만,
 * global(공유 커널, ApplicationModule.Type.OPEN)에 둬서 다른 모듈도 필요해지면
 * 그대로 재사용할 수 있게 한다.
 */
@Component
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /** 비밀번호 재설정 링크가 담긴 메일을 보낸다. 링크는 프론트 /reset-password?token=... 로 연결된다. */
    public void sendPasswordResetEmail(String to, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[CanvasFlow] 비밀번호 재설정 안내");
        message.setText(
                "안녕하세요, CanvasFlow입니다.\n\n" +
                        "아래 링크를 눌러 비밀번호를 재설정해주세요. (30분간 유효)\n\n" +
                        resetLink + "\n\n" +
                        "본인이 요청하지 않았다면 이 메일은 무시하셔도 됩니다."
        );
        mailSender.send(message);
    }
}
