package com.hbwxz.gateway.filter;

import com.hbwxz.gateway.feignclient.Oauth2ServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.Map;

@Component
public class AuthFilter implements GlobalFilter, Ordered {
    @Autowired
    @Lazy
    private Oauth2ServiceClient oauth2ServiceClient;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getURI().getPath();
        /**
         * 1.先放行不需要校验的路径
         */
        if (path.contains("/oauth")
                || path.contains("/user/register")) {
            chain.filter(exchange);
        }
        /**
         * 2.开始校验请求中的token
         */
        String token = request.getHeaders().getFirst("Authorization");
        //feign调用oauth2-service模块的checkToken方法，校验当前获取的token是否有效
        Map<String, Object> result = oauth2ServiceClient.checkToken(token);
        Boolean active = (Boolean) result.get("active");
        //如果active为false，说明当前token无效，返回401状态码
        if (!active) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.setComplete();
        }
        //之后我们可以在header中设置一些值
        ServerHttpRequest httpRequest = request.mutate().headers(httpHeaders -> {
            httpHeaders.set("personId", "");
            httpHeaders.set("tracingId", "");
        }).build();
        //将我们在header中设置的值通过httpRequest传输到exchange对象里
        exchange.mutate().request(httpRequest);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
