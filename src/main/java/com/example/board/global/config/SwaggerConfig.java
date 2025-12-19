package com.example.board.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Board API 명세서",
                description = "사용자 관리, 게시글, 댓글 기능 API",
                version = "v1.0.0"
        )
)
public class SwaggerConfig {

    // [중요] 생성자(static 블록)에서 설정을 추가합니다.
    static {
        // "@AuthenticationPrincipal" 어노테이션이 붙은 파라미터는
        // 스웨거 문서에서 아예 무시해라(숨겨라) 라고 설정하는 것입니다.
        SpringDocUtils.getConfig().addAnnotationsToIgnore(AuthenticationPrincipal.class);
    }
}