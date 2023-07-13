package com.hbwxz.user.service;

import com.hbwxz.common.response.CommonResponse;
import com.hbwxz.common.response.ResponseCode;
import com.hbwxz.common.response.ResponseUtils;
import com.hbwxz.user.pojo.AuthGrantType;
import com.hbwxz.user.pojo.Oauth2Client;
import com.hbwxz.user.pojo.RegisterType;
import com.hbwxz.user.pojo.User;
import com.hbwxz.user.processor.RedisCommonProcessor;
import com.hbwxz.user.repo.OauthClientRepository;
import com.hbwxz.user.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserRegisterLoginService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OauthClientRepository oauthClientRepository;

    @Autowired
    private RedisCommonProcessor redisCommonProcessor;

    @Autowired
    private RestTemplate innerRestTemplate;

    @Resource(name = "transactionManager")
    private JpaTransactionManager transactionManager;

    //如果当前存在事务，就加入该事务，如果不存在，就创建一个新的事务
//    @Transactional(propagation = Propagation.REQUIRED)
    public CommonResponse namePasswordRegister(User user) {
        //如果userName 和 clientId都查不出来数据，说明是新用户刚刚注册进来的
        if (userRepository.findByUserName(user.getUserName()) == null
            && oauthClientRepository.findByClientId(user.getUserName()) == null) {
            //user信息组装
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            String passwd = user.getPasswd();
            String passwordEncode = bCryptPasswordEncoder.encode(passwd);
            user.setPasswd(passwordEncode);
            Oauth2Client oauth2Client = Oauth2Client.builder()
                    .clientId(user.getUserName())
                    .clientSecret(passwordEncode)
                    .resourceIds(RegisterType.USER_PASSWORD.name())
                    .authorizedGrantTypes(AuthGrantType.refresh_token.name().concat(",").concat(AuthGrantType.password.name()))
                    .scope("web")
                    .authorities(RegisterType.USER_PASSWORD.name())
                    .build();
            Integer userId = this.saveUserAndOauthClient(user, oauth2Client);
            String personId = userId + 10000000 + "";
            redisCommonProcessor.set(personId , user);
            return ResponseUtils.okResponse(formatResponseContent(user,generateOauthToken(AuthGrantType.password, user.getUserName(), passwd,user.getUserName(), passwd)));
        }
        return ResponseUtils.failResponse(ResponseCode.BAD_REQUEST.getCode(), "user already exist",null);
    }

    private Map formatResponseContent (User user,Map oauth2Client) {
        return new HashMap(){{
            put("user",user);
            put("oauth",oauth2Client);
        }};
    }

    private Integer saveUserAndOauthClient(User user, Oauth2Client oauth2Client) {
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        transactionDefinition.setTimeout(30);
        TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
        try {
            user = this.userRepository.save(user);
            this.oauthClientRepository.save(oauth2Client);
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            if (!transactionStatus.isCompleted()) {
                transactionManager.rollback(transactionStatus);
            }
            throw new UnsupportedOperationException("db save failed");
        }
        return user.getId();
    }

    //使用restTemplate去获取token
    private Map generateOauthToken (AuthGrantType authGrantType,
                                    String userName,
                                    String password,
                                    String clientId,
                                    String clientSecret) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add("grant_type",authGrantType.name());
        params.add("client_id",clientId);
        params.add("client_secret",clientSecret);
        if (authGrantType == AuthGrantType.password) {
            params.add("username",userName);
            params.add("password",password);
        }
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params,httpHeaders);
        return innerRestTemplate.postForObject("http://oauth2-service/oauth/token",httpEntity, Map.class);
    }
}
