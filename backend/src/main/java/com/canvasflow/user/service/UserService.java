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

/**
 * 담당: 회원(User) 기본 CRUD.
 * User Entity/Repository는 이 Service를 통해서만 접근한다 — auth 모듈을 포함한 다른 모듈에서
 * com.canvasflow.user.entity.User / com.canvasflow.user.repository.UserRepository를 직접 import하지 않는다.
 * (user 모듈은 현재 package-info.java에서 OPEN으로 열려 있어 강제되진 않지만, 이 Service가 그 문을 좁히는 첫 단계다.)
 */
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
        // TODO: 닉네임 중복 검증(userRepository.existsByNickname) 추가
        User user = getUserOrThrow(userId);
        user.changeNickname(request.nickname());
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

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.USER_NOT_FOUND));
    }
}
