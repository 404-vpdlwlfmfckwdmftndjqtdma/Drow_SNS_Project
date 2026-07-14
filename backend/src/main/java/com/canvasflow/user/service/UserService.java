package com.canvasflow.user.service;

import com.canvasflow.user.dto.UpdateNicknameRequest;
import com.canvasflow.user.dto.UpdateProfileImageRequest;
import com.canvasflow.user.dto.UserResponse;
import com.canvasflow.user.entity.User;
import com.canvasflow.user.repository.UserRepository;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 담당: 회원(User) 기본 CRUD.
 * User Entity/Repository는 이 Service를 통해서만 접근한다 — 다른 모든 모듈은
 * com.canvasflow.user.entity.User / com.canvasflow.user.repository.UserRepository를 직접 import하지 않고
 * 이 파사드가 노출하는 메서드만 호출한다.
 * user 모듈은 package-info.java가 없어 기본값(CLOSED)이고, com.canvasflow.user.service 패키지는
 * 하위 패키지라 원래 internal로 취급된다. 이 클래스에 @PackageInfo + @NamedInterface를 붙여서
 * package-info.java 없이 이 패키지(UserService)만 다른 모듈에 노출되도록 명시한다.
 * entity/repository 패키지는 여전히 internal이라 ModularityTests가 계속 그쪽 직접 참조를 막아준다.
 */
@org.springframework.modulith.PackageInfo
@org.springframework.modulith.NamedInterface
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getMyInfo(Long userId) {
        return UserResponse.from(getUserOrThrow(userId));
    }

    @Transactional
    public UserResponse updateNickname(Long userId, UpdateNicknameRequest request) {
        User user = getUserOrThrow(userId);
        String newNickname = request.nickname();
        // 현재 닉네임과 동일하면(변경 없음) 중복 검증을 건너뛴다 — 안 그러면 본인 닉네임 때문에 항상 막힘.
        if (!newNickname.equals(user.getNickname()) && userRepository.existsByNickname(newNickname)) {
            throw new CanvasflowException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
        user.changeNickname(newNickname);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateProfileImage(Long userId, UpdateProfileImageRequest request) {
        User user = getUserOrThrow(userId);
        user.changeProfileImage(request.profileImageUrl());
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 회원 생성. 비밀번호 암호화는 이 메서드 내부에서 처리하므로,
     * 호출하는 쪽(auth 모듈 등)은 평문 비밀번호만 넘기면 된다.
     */
    @Transactional
    public Long createUser(String email, String rawPassword, String nickname) {
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .nickname(nickname)
                .build();
        return userRepository.save(user).getId();
    }

    /**
     * 이메일/비밀번호 검증 후 userId를 반환한다.
     * User의 password 필드가 이 모듈 밖으로 노출되지 않도록, 비교 로직 자체를 이 안에서 처리한다.
     */
    @Transactional(readOnly = true)
    public Long verifyCredentials(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new CanvasflowException(ErrorCode.INVALID_CREDENTIALS);
        }

        return user.getId();
    }

    /**
     * 유저 존재 여부만 확인한다 (다른 모듈이 회원가입 여부/유효성만 검증할 때 사용, 예: 팔로우 대상 존재 확인).
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    /**
     * 닉네임만 필요하고 유저가 없어도 예외를 던지지 않아도 되는 경우(단순 표시용) 사용한다.
     * 유저가 없으면 null을 반환한다.
     */
    @Transactional(readOnly = true)
    public String findNicknameById(Long userId) {
        return userRepository.findById(userId).map(User::getNickname).orElse(null);
    }

    /**
     * 닉네임이 반드시 필요하고 유저가 없으면 실패해야 하는 경우 사용한다 (예: 댓글 작성 시 작성자 검증).
     */
    @Transactional(readOnly = true)
    public String getNicknameOrThrow(Long userId) {
        return getUserOrThrow(userId).getNickname();
    }

    /**
     * 여러 유저의 닉네임을 한 번에 조회한다 (N+1 방지용, 예: 댓글 목록에서 작성자 닉네임 일괄 조회).
     */
    @Transactional(readOnly = true)
    public Map<Long, String> findNicknamesByIds(Collection<Long> userIds) {
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.USER_NOT_FOUND));
    }
}
