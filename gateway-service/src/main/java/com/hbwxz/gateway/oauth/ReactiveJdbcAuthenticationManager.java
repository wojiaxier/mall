package com.hbwxz.gateway.oauth;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;

public class ReactiveJdbcAuthenticationManager implements ReactiveAuthenticationManager {

    private TokenStore tokenStore;

    public ReactiveJdbcAuthenticationManager (DataSource dataSource) {
        this.tokenStore = new JdbcTokenStore(dataSource);
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.justOrEmpty(authentication)
                //后续我们在前端项目里或者postMan发请求的时候，
                // 我们会在header里面的authentication里面放入带bearer前缀的token值，
                // 这个token值就是在下面这几行代码里获取到的
                .filter(a -> a instanceof BearerTokenAuthenticationToken)
                .cast(BearerTokenAuthenticationToken.class)
                //token在这里获取到了，调用的是BearerTokenAuthenticationToken.getToken()方法
                .map(BearerTokenAuthenticationToken :: getToken)
                .flatMap((accessToken -> {
                    OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(accessToken);
                    //在此验证token是否真的存在于数据库中（tokenStore是连接数据库进行数据查询的工具）
                    if (ObjectUtils.isEmpty(oAuth2AccessToken)) {
                        return Mono.error(new InvalidTokenException("InvalidTokenException"));
                    } else if (oAuth2AccessToken.isExpired()) {
                        //再次验证token是否过期
                        return Mono.error(new InvalidTokenException("InvalidTokenException，isExpired"));
                    }
                    OAuth2Authentication auth2Authentication = this.tokenStore.readAuthentication(accessToken);
                    if (org.apache.commons.lang3.ObjectUtils.isEmpty(auth2Authentication)) {
                        return Mono.error(new InvalidTokenException("fake token!"));
                    }
                    return Mono.justOrEmpty(auth2Authentication);
                })).cast(Authentication.class);
    }
}
