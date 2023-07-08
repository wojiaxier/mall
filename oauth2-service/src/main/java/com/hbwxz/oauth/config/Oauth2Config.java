package com.hbwxz.oauth.config;

import com.hbwxz.oauth.service.UserDetailServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.annotation.Resource;
import javax.sql.DataSource;

@Configuration
@EnableAuthorizationServer
public class Oauth2Config extends AuthorizationServerConfigurerAdapter {
    @Resource
    private DataSource dataSource;

    @Resource
    private UserDetailServiceImpl userDetailService;

    @Resource
    private AuthenticationManager authenticationManager;

    //Oauth2 是为了生token令牌，所以token令牌需要存储到数据库里
    @Bean
    public TokenStore tokenStore () {
        return new JdbcTokenStore(dataSource);
    }

    //token可以自定义过期时间，如果没有设置值，默认过期时间是12个小时
    @Bean
    @Primary
    public DefaultTokenServices defaultTokenServices () {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        //此处设置30天过期
        defaultTokenServices.setAccessTokenValiditySeconds(30 * 24 * 3600);
        defaultTokenServices.setTokenStore(tokenStore());
        return defaultTokenServices;
    }

    //client id 和client secret需要从数据库里获取
    @Bean
    public ClientDetailsService clientDetails () {
        return new JdbcClientDetailsService(dataSource);
    }

    //可以将ClientDetailsService放入ClientDetailsServiceConfig
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientDetails ());
    }

    //userPWD和client secret这两种密码都需要进行加密处理，而不能明文存储到mysql数据库
    @Bean
    public PasswordEncoder passwordEncoder () {
        //如果企业需要自己的加密算法，以下注释代码可以自定义加密算法和校验规则
        /*return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return null;
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return false;
            }
        }*/

        return new BCryptPasswordEncoder();
    }

    //添加自定义安全配置，可以不添加
    //一般企业会把它用于放开某些接口的查询权限，比如checkToken接口
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.allowFormAuthenticationForClients() //可以进行表单验证
                .checkTokenAccess("permitAll()");   //checkToken
    }

    //将userDetailService配置进来
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.userDetailsService(userDetailService);
        endpoints.tokenServices(defaultTokenServices());
        endpoints.authenticationManager(authenticationManager);
        endpoints.tokenStore(tokenStore());
    }


}
