//package com.hbwxz.gateway.oauth;
//
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.security.authorization.AuthorizationDecision;
//import org.springframework.security.authorization.ReactiveAuthorizationManager;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.provider.OAuth2Authentication;
//import org.springframework.security.web.server.authorization.AuthorizationContext;
//import org.springframework.stereotype.Component;
//import org.springframework.util.AntPathMatcher;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.util.Set;
//import java.util.concurrent.ConcurrentSkipListSet;
//@Component
//public class AccessManager implements ReactiveAuthorizationManager<AuthorizationContext> {
//    //有一些路径（比如注册，登录，首页）是不需要token校验的，这个set集合存放的是不需要token校验的路径（正则表达式）
//    private Set<String> permitAll = new ConcurrentSkipListSet<>();
//
//    //正则校验器
//    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();
//
//    public AccessManager () {
//        //一般路径里面带有oauth的都是去获取token的路径，这种的需要放行
//        permitAll.add("/**/oauth/**");
//
//    }
//
//    //此方法是决定请求是否放行的最终函数
//    @Override
//    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext authorizationContext) {
//        //1.exchange对象里面含有request信息
//        // 2.可以从request信息里面获取到请求路径
//        // 3.我们可以通过校验请求路径里面是否包含了可以放行的关键词(比如 oauth)来决定是否放行
//        // 4.如果可以放行，那没啥说的，直接放行，
//        // 5.如果不可以放行，那我们就开始真正的校验逻辑了，也就是与数据库开始交互，获取到用户信息来决定是否放行
//        ServerWebExchange exchange = authorizationContext.getExchange();
//        return authentication.map(auth -> {
//            String requestPath = exchange.getRequest().getURI().getPath();
//            //不做权限验证直接放行的path，比如获取token的请求
//            if (checkPermit(requestPath)) {
//                return new AuthorizationDecision(true);
//            }
//            //如果从token里直接获取到了clientId，说明该请求已经提前获取到了token，可以放行
//            if (auth instanceof OAuth2Authentication) {
//                OAuth2Authentication auth2Authentication = (OAuth2Authentication) auth;
//                String clientId = auth2Authentication.getOAuth2Request().getClientId();
//                if (StringUtils.isNoneEmpty(clientId)) {
//                    return new AuthorizationDecision(true);
//                }
//            }
//            //如果该请求既不是直接放行的path，也不是携带着token来请求的path，
//            // 那说明没有权限，直接返回false
//            return new AuthorizationDecision(false);
//        });
//    }
//
//    //路径校验方法
//    private boolean checkPermit(String requestPath) {
//        //1.循环permitAll（我们提前添加进去的，需要放过的路径）的每一个路径，
//        // 2.将每个路径与当前的实际请求过来的路径做一个对比，如果存在，就返回true（true的意思就是放行）
//        return permitAll.stream().filter(path -> antPathMatcher.match(path,requestPath)).findFirst().isPresent();
//    }
//}
