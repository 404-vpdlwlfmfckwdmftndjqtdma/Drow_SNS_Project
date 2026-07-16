package com.canvasflow.search;

import com.canvasflow.post.PostReader;
import com.canvasflow.post.PostSearchView;
import com.canvasflow.user.UserFacade;
import com.canvasflow.user.UserProfileView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SearchService {

    private final PostReader postReader;
    private final UserFacade userFacade;

    @Transactional(readOnly = true)
    public List<PostSearchView> searchPostsByTag(String tag) {
        return postReader.findByTag(tag);
    }

    @Transactional(readOnly = true)
    public List<UserProfileView> searchUsers(String nickname) {
        return userFacade.searchByNickname(nickname);
    }
}
