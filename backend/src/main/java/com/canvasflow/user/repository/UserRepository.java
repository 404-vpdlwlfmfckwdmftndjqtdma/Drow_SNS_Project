package com.canvasflow.user.repository;

import com.canvasflow.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    // 검색 페이지 "유저 검색"용: 부분/대소문자 무관 닉네임 일치, 닉네임 가나다순.
    List<User> findByNicknameContainingIgnoreCaseOrderByNicknameAsc(String keyword);
}
