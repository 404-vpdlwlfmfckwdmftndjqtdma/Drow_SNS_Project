package com.canvasflow.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @EnableJpaAuditing 은 CanvasflowApplication 에 선언되어 있음.
 * TODO: QueryDSL 도입 시 JPAQueryFactory Bean 등록.
 */
@Configuration
public class JpaConfig {
}
