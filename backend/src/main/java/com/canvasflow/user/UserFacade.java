package com.canvasflow.user;

import java.util.Collection;
import java.util.Map;

/**
 * user 모듈이 다른 모듈에 노출하는 기능을 모은 파사드 인터페이스.
 * com.canvasflow.user는 이 모듈의 기본 패키지라 Spring Modulith가 별도 어노테이션 없이
 * 자동으로 다른 모듈에 노출해준다 (하위 패키지인 user.service/user.dto/user.entity는 여전히 internal).
 * 다른 모듈(auth/follow/mypage 등)은 구현체인 UserService를 직접 타입으로 참조하지 않고
 * 이 인터페이스로만 의존한다. 구현체가 하나뿐이라 Spring이 타입 기반으로 자동 주입해준다.
 */
public interface UserFacade {

    boolean existsByEmail(String email);

    Long createUser(String email, String rawPassword, String nickname);

    Long verifyCredentials(String email, String rawPassword);

    boolean existsById(Long userId);

    String findNicknameById(Long userId);

    String getNicknameOrThrow(Long userId);

    Map<Long, String> findNicknamesByIds(Collection<Long> userIds);

    UserProfileView getProfileView(Long userId);
}
