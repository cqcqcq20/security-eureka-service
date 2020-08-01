package com.example.music.auth.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.music.auth.entity.DBUserEntity;
import com.example.music.auth.mapper.UserMapper;
import com.example.common.users.AuthorityEntity;
import com.example.common.users.UserEntity;
import com.example.common.utils.Md5Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component("CustomUserDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JdbcTokenStore jdbcTokenStore;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        return loadUserByUserId(id);
    }

    public UserDetails loadUserByUserId(long id) throws UsernameNotFoundException {
        return loadUserByUserId(String.valueOf(id));
    }

    public UserDetails loadUserByUserId(String id) throws UsernameNotFoundException {
        UserEntity byPhone = userMapper.findByPhoneOrId(id,id);
        if (byPhone == null) {
            throw new UsernameNotFoundException("");
        }
        return getUserDetail(byPhone);
    }

    public UserDetails loadUserByUsername(String phone,String area) throws UsernameNotFoundException {
        DBUserEntity byPhone = userMapper.findByPhone(phone,area);
        if (byPhone == null) {
            byPhone = createNew(phone,area);
        }
        return getUserDetail(byPhone);
    }

    private UserDetails getUserDetail(UserEntity userEntity) {
        List<AuthorityEntity> entities = userMapper.findRoleByUser(String.valueOf(userEntity.getId()));
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        // 用户角色也应在数据库中获取
        for (AuthorityEntity entity : entities) {
            authorities.add(new SimpleGrantedAuthority(entity.getName()));
        }
        String password = userEntity.getPassword();
        return new org.springframework.security.core.userdetails.User(String.valueOf(userEntity.getId()),password, authorities);
    }


    public DBUserEntity createNew(String username,String area) {
        DBUserEntity userEntity = new DBUserEntity();
        userEntity.setNickname(RandomStringUtils.randomAlphabetic(6));
        userEntity.setPhone(username);
        String password = passwordEncoder.encode(Md5Utils.encode(RandomStringUtils.randomAlphanumeric(16)));
        userEntity.setPassword(password);
        userEntity.setArea(area);
        userEntity.setId(Long.parseLong(RandomStringUtils.randomNumeric(10)));
        if (userMapper.insert(userEntity) <= 0) {
            return null;
        }
        return userEntity;
    }

    /**
     * 更新用户密码
     * @param id
     * @param password
     * @return
     */
    public boolean updatePasswordById(String id,String password) {
        String newPassword = passwordEncoder.encode(Md5Utils.encode(password).toLowerCase());
        UpdateWrapper<DBUserEntity> userEntityUpdateWrapper = new UpdateWrapper<DBUserEntity>()
                .eq("id",id)
                .set("password",newPassword);
        if (userMapper.update(null,userEntityUpdateWrapper) > 0) {
            Collection<OAuth2AccessToken> tokensByUserName = jdbcTokenStore.findTokensByUserName(id);
            for (OAuth2AccessToken oAuth2AccessToken : tokensByUserName) {
                jdbcTokenStore.removeAccessToken(oAuth2AccessToken);
            }
            return true;
        }
        return false;
    }

}
