package com.canvasflow.user.service;

import com.canvasflow.user.UserFacade;
import com.canvasflow.user.UserProfileView;
import com.canvasflow.user.dto.UpdateBioRequest;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 담당: 회원(User) 기본 CRUD.
 * User Entity/Repository는 이 Service를 통해서만 접근한다 — 다른 모든 모듈은
 * com.canvasflow.user.entity.User / com.canvasflow.user.repository.UserRepository를 직접 import하지 않고
 * 이 파사드가 노출하는 메서드만 호출한다.
 * 다른 모듈은 이 클래스(구현체, user.service 소속 - internal)를 직접 타입으로 참조하지 않고,
 * user 모듈의 기본 패키지(com.canvasflow.user)에 있는 UserFacade로만 의존한다.
 * 기본 패키지는 Spring Modulith가 자동으로 노출해주므로 이 클래스엔 더 이상 @PackageInfo/@NamedInterface가
 * 필요 없다 (user.service/user.dto/user.entity/user.repository는 여전히 internal).
 */
@RequiredArgsConstructor
@Service
public class UserService implements UserFacade {

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

    @Transactional
    public UserResponse updateBio(Long userId, UpdateBioRequest request) {
        User user = getUserOrThrow(userId);
        user.changeBio(request.bio());
        return UserResponse.from(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 회원 생성. 비밀번호 암호화는 이 메서드 내부에서 처리하므로,
     * 호출하는 쪽(auth 모듈 등)은 평문 비밀번호만 넘기면 된다.
     */
    @Override
    @Transactional
    public Long createUser(String email, String rawPassword, String nickname) {
        // 이메일 중복은 호출하는 쪽(AuthService)에서 미리 확인하지만, 닉네임 중복은 여기서 직접 확인한다.
        // 확인 없이 저장하면 DB의 nickname unique 제약에 걸려 DataIntegrityViolationException이 터지고,
        // 그건 CanvasflowException이 아니라서 전역 예외 처리기의 catch-all(500, "서버 내부 오류가 발생했습니다")로
        // 빠져버린다 - 회원가입 시 닉네임 중복 에러 메시지가 이상하게 뜨던 원인.
        if (userRepository.existsByNickname(nickname)) {
            throw new CanvasflowException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
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
    @Override
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
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    /**
     * 닉네임만 필요하고 유저가 없어도 예외를 던지지 않아도 되는 경우(단순 표시용) 사용한다.
     * 유저가 없으면 null을 반환한다.
     */
    @Override
    @Transactional(readOnly = true)
    public String findNicknameById(Long userId) {
        return userRepository.findById(userId).map(User::getNickname).orElse(null);
    }

    /**
     * 닉네임이 반드시 필요하고 유저가 없으면 실패해야 하는 경우 사용한다 (예: 댓글 작성 시 작성자 검증).
     */
    @Override
    @Transactional(readOnly = true)
    public String getNicknameOrThrow(Long userId) {
        return getUserOrThrow(userId).getNickname();
    }

    /**
     * 여러 유저의 닉네임을 한 번에 조회한다 (N+1 방지용, 예: 댓글 목록에서 작성자 닉네임 일괄 조회).
     */
    @Override
    @Transactional(readOnly = true)
    public Map<Long, String> findNicknamesByIds(Collection<Long> userIds) {
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));
    }

    /**
     * 여러 유저의 프로필을 한 번에 조회한다 (N+1 방지용, 예: 댓글 목록에서 작성자 프로필 일괄 조회).
     */
    @Override
    @Transactional(readOnly = true)
    public Map<Long, UserProfileView> findProfilesByIds(Collection<Long> userIds) {
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(
                        User::getId,
                        u -> new UserProfileView(u.getId(), u.getNickname(), u.getProfileImageUrl(), u.getBio())
                ));
    }

    /**
     * 다른 모듈(mypage 등)에 프로필을 보여줄 때 사용한다. email은 포함하지 않는다.
     */
    @Override
    @Transactional(readOnly = true)
    public UserProfileView getProfileView(Long userId) {
        User user = getUserOrThrow(userId);
        return new UserProfileView(user.getId(), user.getNickname(), user.getProfileImageUrl(), user.getBio());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileView> searchByNickname(String keyword) {
        return userRepository.findByNicknameContainingIgnoreCaseOrderByNicknameAsc(keyword).stream()
                .map(u -> new UserProfileView(u.getId(), u.getNickname(), u.getProfileImageUrl(), u.getBio()))
                .toList();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.USER_NOT_FOUND));
    }
}
