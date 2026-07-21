package com.canvasflow.user.entity;

import com.canvasflow.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    private String profileImageUrl;

    private String bio;

    @Builder
    public User(String email, String password, String nickname, String profileImageUrl, String bio) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.bio = bio;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void changeBio(String bio) {
        this.bio = bio;
    }

    /** 비밀번호 재설정용. 호출하는 쪽(UserService)에서 이미 암호화한 값을 넘겨준다. */
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
