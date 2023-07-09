//package com.hbwxz.gateway.oauth;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.authentication.ReactiveAuthenticationManager;
//import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
//import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.oauth2.server.resource.web.server.ServerBearerTokenAuthenticationConverter;
//import org.springframework.security.web.server.SecurityWebFilterChain;
//import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
//
//import javax.annotation.Resource;
//import javax.sql.DataSource;
//
//@Configuration
//@EnableWebFluxSecurity
//public class SecurityConfig {
//
//    @Resource
//    private DataSource dataSource;
//
//    @Resource
//    private AccessManager accessManager;
//
//    @Bean
//    public SecurityWebFilterChain webFluxSecurityWebFilterChain (ServerHttpSecurity serverHttpSecurity) {
//        ReactiveAuthenticationManager reactiveAuthenticationManager = new ReactiveJdbcAuthenticationManager(dataSource);
//        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(reactiveAuthenticationManager);
//        authenticationWebFilter.setServerAuthenticationConverter(new ServerBearerTokenAuthenticationConverter());
//
//        serverHttpSecurity.httpBasic().disable()
//                .csrf().disable()
//                .authorizeExchange().pathMatchers(HttpMethod.OPTIONS).permitAll()
//                .anyExchange().access(accessManager)
//                .and().addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHORIZATION);
//        return serverHttpSecurity.build();
//    }
//}
