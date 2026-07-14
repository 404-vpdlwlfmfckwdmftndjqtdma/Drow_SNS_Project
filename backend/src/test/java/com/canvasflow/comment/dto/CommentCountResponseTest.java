package com.canvasflow.comment.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommentCountResponseTest {

    @Test
    void shouldExposeCommentCount() {
        CommentCountResponse response = new CommentCountResponse(7L);

        assertThat(response.count()).isEqualTo(7L);
    }
}
