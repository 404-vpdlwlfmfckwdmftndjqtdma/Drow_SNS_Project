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

    /**
     * 여러 유저의 프로필을 한 번에 조회한다 (N+1 방지용, 예: 팔로잉/팔로워 목록에서 프로필 일괄 조회).
     * findNicknamesByIds와 동일한 패턴 - 존재하지 않는 id는 결과 Map에서 그냥 빠진다(예외 없음).
     */
    Map<Long, UserProfileView> getProfileViews(Collection<Long> userIds);

    /**
     * 이메일로 userId만 조회한다 (비밀번호 찾기 등에서 사용). 없으면 null.
     * findNicknameById와 동일하게 존재 여부를 예외로 다루지 않는다 - 호출하는 쪽(auth)이
     * "이메일 존재 여부를 노출하지 않는" 정책을 스스로 적용할 수 있게 하기 위함.
     */
    Long findIdByEmail(String email);

    /** 비밀번호 재설정. 평문 비밀번호만 넘기면 암호화는 이 메서드 내부에서 처리한다. */
    void updatePassword(Long userId, String rawPassword);
}
