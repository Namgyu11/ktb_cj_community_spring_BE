package com.ktb.ktb_cj_community_spring_be.global.config;

import com.ktb.ktb_cj_community_spring_be.auth.config.LoginUserArgumentResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

      private final LoginUserArgumentResolver loginUserArgumentResolver;

      @Override
      public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            // loginUserArgumentResolver 를 Argument Resolver 목록에 추가
            resolvers.add(loginUserArgumentResolver);
      }
}
