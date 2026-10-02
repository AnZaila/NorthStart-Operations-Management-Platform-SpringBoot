package com.nomp.northstar.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.nomp.northstar.config.NorthstarProperties.Cors;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
  private final NorthstarProperties properties;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new SaInterceptor(handle -> SaRouter.match("/api/v1/**")
            .notMatch(
                "/api/v1/auth/login",
                "/api/v1/auth/refresh",
                "/api/v1/auth/captcha",
                "/api/v1/files/raw/**")
            .check(r -> StpUtil.checkLogin())))
        .addPathPatterns("/**");
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    Cors cors = properties.getCors();
    List<String> origins = new java.util.ArrayList<>(cors.getOrigins());
    origins.add("http://localhost:*");
    origins.add("http://127.0.0.1:*");
    registry.addMapping("/api/**")
        .allowedOriginPatterns(origins.toArray(String[]::new))
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .exposedHeaders("X-Request-Id")
        .allowCredentials(true);
  }
}
