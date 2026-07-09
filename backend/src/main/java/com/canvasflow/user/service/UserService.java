package com.canvasflow.user.service;

import com.canvasflow.user.dto.UpdateNicknameRequest;
import com.canvasflow.user.dto.UpdateProfileImageRequest;
import com.canvasflow.user.dto.UserResponse;
import com.canvasflow.user.entity.User;
import com.canvasflow.user.repository.UserRepository;
import com.canvasflow.global.exception.CanvasflowException;
import com.canvasflow.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

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

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CanvasflowException(ErrorCode.USER_NOT_FOUND));
    }
}
