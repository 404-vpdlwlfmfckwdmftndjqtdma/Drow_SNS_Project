package com.canvasflow.post;

import java.util.Set;

public interface FollowingPolicy {
    Set<Long> followingIds(Long viewerId);
}
