package com.example.music.auth.config;

import com.example.music.auth.basic.config.UnAuthenticationEntryPoint;
import com.example.music.auth.basic.config.handler.AuthAccessDeniedHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * WebSecurityConfig
 *
 * @author fengzheng
 * @date 2019/10/10
 */
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    /**
     * 允许匿名访问oauth接口 主要是 oauth 接口
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/oauth/**","/password/forget","/sms/send").permitAll()
                .and().authorizeRequests().anyRequest().authenticated()
                .and().exceptionHandling().accessDeniedHandler(new AuthAccessDeniedHandler()).authenticationEntryPoint(new UnAuthenticationEntryPoint())
                .and().csrf().disable();
    }

}
