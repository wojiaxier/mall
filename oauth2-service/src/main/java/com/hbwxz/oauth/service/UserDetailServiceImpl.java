package com.hbwxz.oauth.service;

import com.hbwxz.oauth.pojo.User;
import com.hbwxz.oauth.repo.UserRepository;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Resource
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.queryByUserName(username);
        if (!ObjectUtils.isEmpty(user)) {
            return new org.springframework.security.core.userdetails.User(user.getUserName(),
                    user.getPasswd(),
                    AuthorityUtils.createAuthorityList(user.getPasswd()));
        } else {
            throw new UsernameNotFoundException("user not found");
        }
    }
}
