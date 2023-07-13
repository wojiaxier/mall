package com.hbwxz.user.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class HttpClientConfig {

    //connection超时时间
    private static final int CONNECT_TIMEOUT = 30000;
    //request超时时间
    private static final int REQUEST_TIMEOUT = 30000;
    //socket超时时间
    private static final int SOCKET_TIMEOUT = 60000;

    //连接池最大连接数
    private static final int MAX_TOTAL_CONNECTIONS = 50;

    //默认连接池中的keep-alive线程的配置
    //这里先给一个默认值，如果请求的header中携带了连接时间，则优先使用header中携带的值
    private static final int DEFAULT_KEEP_ALIVE_TIME_MS = 20000;

    //清理空闲线程的定时任务
    private static final int CLOSE_IDLE_CONNECTION_WAIT_TIME_SECS = 30;

    @Bean
    public CloseableHttpClient httpClient () {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setConnectionRequestTimeout(REQUEST_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();
        CloseableHttpClient closeableHttpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(poolingHttpClientConnectionManager())
                .setKeepAliveStrategy(null)
                .build();
        return closeableHttpClient;
    }

    @Bean
    public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager () {
        //https和http的注册
        //同时将 MAX_TOTAL_CONNECTIONS 这个参数加入进来
        SSLContextBuilder builder = new SSLContextBuilder();
        try {
            builder.loadTrustMaterial(null,new TrustAllStrategy());
        }catch (NoSuchAlgorithmException | KeyStoreException e) {
            log.error("loadTrustMaterial failed ,error details = {}", e);
        }
        SSLConnectionSocketFactory sslsf = null;
        try {
            sslsf = new SSLConnectionSocketFactory(builder.build());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("SSLConnectionSocketFactory creat failed , error details = {}",e);
        }
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                .<ConnectionSocketFactory>create()
                .register("https",sslsf)
                .register("http",new PlainConnectionSocketFactory())
                .build();
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager =
                new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        poolingHttpClientConnectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        return poolingHttpClientConnectionManager;
    }

    @Bean
    public ConnectionKeepAliveStrategy connectionKeepAliveStrategy () {
        return new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse httpResponse, HttpContext httpContext) {
                HeaderElementIterator headerElementIterator = 
                        new BasicHeaderElementIterator(httpResponse.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (headerElementIterator.hasNext()) {
                    HeaderElement headerElement = headerElementIterator.nextElement();
                    String name = headerElement.getName();
                    String value = headerElement.getValue();
                    if (!StringUtils.isEmpty(name) && value.equalsIgnoreCase("timeout")) {
                        return Long.parseLong(value) * 1000;
                    }
                }
                return DEFAULT_KEEP_ALIVE_TIME_MS;
            }
        };
    }

    //定期清理空闲的connection + 过期的connection
    //PoolingHttpClientConnectionManager 池化技术需要考虑keep-alive，以及空闲连接和过期连接的清理
    @Bean
    public Runnable idleConnectionMonitor (final PoolingHttpClientConnectionManager connectionManager) {
        return new Runnable() {
            @Override
            @Scheduled(fixedDelay = 10000)
            public void run() {
                try {
                    if (ObjectUtils.isNotEmpty(connectionManager)) {
                        //关掉过期的连接
                        connectionManager.closeExpiredConnections();
                        //关掉空闲的连接
                        connectionManager.closeIdleConnections(CLOSE_IDLE_CONNECTION_WAIT_TIME_SECS, TimeUnit.SECONDS);
                    } else {
                        log.warn("PoolingHttpClientConnectionManager not init");
                    }
                } catch (Exception e) {
                    log.error("close connection error , details = {}",e);
                }
            }
        };
    }
}
