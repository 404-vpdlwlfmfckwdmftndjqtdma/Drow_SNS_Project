package com.canvasflow.search.service;

import com.canvasflow.post.PostReader;
import com.canvasflow.user.UserFacade;
import com.canvasflow.user.UserProfileView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 담당: 검색 페이지 "태그 검색" / "유저 검색".
 * post/user 모듈의 테이블/리포지토리를 직접 참조하지 않고, 각 모듈이 공개한 읽기 창구인
 * PostReader / UserFacade로만 의존한다 - post/user 쪽 파일은 이 기능 때문에 건드리지 않는다
 * (PostReader.searchByTag, UserFacade.searchByNickname은 각 담당자가 검토할 인터페이스 변경 지점).
 */
@RequiredArgsConstructor
@Service
public class SearchService {

    private final PostReader postReader;
    private final UserFacade userFacade;

    public List<PostReader.PostView> searchPostsByTag(String tag, Long viewerId) {
        return postReader.searchByTag(tag, viewerId);
    }

    public List<UserProfileView> searchUsersByNickname(String nickname) {
        return userFacade.searchByNickname(nickname);
    }
}
