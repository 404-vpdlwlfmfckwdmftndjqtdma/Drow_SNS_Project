package com.canvasflow.auth;

import com.canvasflow.auth.dto.LoginRequest;
import com.canvasflow.auth.dto.ReissueRequest;
import com.canvasflow.auth.dto.SignupRequest;
import com.canvasflow.auth.dto.TokenResponse;

/**
 * auth 모듈이 다른 모듈에 노출하는 기능을 모은 파사드 인터페이스.
 * com.canvasflow.auth는 이 모듈의 기본 패키지라 Spring Modulith가 자동으로 노출해준다.
 * 현재는 AuthController(같은 모듈)만 쓰지만, 다른 모듈이 나중에 auth 기능이 필요해지면
 * AuthService를 직접 참조하지 않고 이 인터페이스로만 의존하도록 미리 준비해둔다.
 */
public interface AuthFacade {

    void signup(SignupRequest request);

    TokenResponse login(LoginRequest request);

    TokenResponse reissue(ReissueRequest request);

    void logout(Long userId);

    /**
     * 비밀번호 재설정 메일 발송. 존재하지 않는 이메일이어도 예외를 던지지 않고 조용히 종료한다
     * (이메일 가입 여부를 노출하지 않기 위함) - 컨트롤러는 항상 같은 성공 응답을 준다.
     */
    void requestPasswordReset(String email);

    /** 메일로 받은 토큰으로 실제 비밀번호를 변경한다. */
    void resetPassword(String token, String newPassword);
}
