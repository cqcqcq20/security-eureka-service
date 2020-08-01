package com.example.music.auth.config;

import com.example.music.auth.basic.config.UnAuthenticationEntryPoint;
import com.example.music.auth.basic.config.handler.AuthAccessDeniedHandler;
import com.example.music.auth.config.granter.SMSCodeTokenGranter;
import com.example.music.auth.config.provider.SmsCodeAuthenticationProvider;
import com.example.music.auth.service.CustomUserDetailsService;
import com.example.music.auth.service.RedisTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableAuthorizationServer
public class OAuth2Config extends AuthorizationServerConfigurerAdapter {

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public CustomUserDetailsService customUserDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTokenStore jdbcTokenStore;

    @Autowired
    private RedisTokenService redisTokenService;

    @Autowired
    private WebResponseExceptionTranslator<OAuth2Exception> customWebResponseExceptionTranslator;

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        TokenEnhancerChain enhancerChain = new TokenEnhancerChain();
        List<TokenEnhancer> enhancerList = new ArrayList<>();
        enhancerList.add(provideTokenEnhancer());
        enhancerChain.setTokenEnhancers(enhancerList);
        endpoints.authenticationManager(authenticationManager)
                .tokenStore(jdbcTokenStore)
                .tokenGranter(tokenGranter(endpoints))
                .tokenEnhancer(enhancerChain)
                .userDetailsService(customUserDetailsService)
                .exceptionTranslator(customWebResponseExceptionTranslator)
                .pathMapping("/oauth/token","/oauth/login");
    }

    @Bean
    public TokenEnhancer provideTokenEnhancer() {
        return new AuthTokenEnhancer();
    }

    private TokenGranter tokenGranter(AuthorizationServerEndpointsConfigurer endpoints) {
        SMSCodeTokenGranter smsCodeTokenGranter = new SMSCodeTokenGranter(endpoints.getTokenServices(), endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory(), authenticationManager);
        List<TokenGranter> granters = new ArrayList<>(Arrays.asList(endpoints.getTokenGranter(),smsCodeTokenGranter));
        if (authenticationManager instanceof ProviderManager) {
            SmsCodeAuthenticationProvider smsCodeAuthenticationProvider = new SmsCodeAuthenticationProvider();
            smsCodeAuthenticationProvider.setUserServiceDetail(customUserDetailsService);
            smsCodeAuthenticationProvider.setRedisTokenService(redisTokenService);
            ((ProviderManager) authenticationManager).getProviders().add(smsCodeAuthenticationProvider);
        }
        return new CompositeTokenGranter(granters);
    }

    @Bean
    public JdbcClientDetailsService jdbcClientDetailsService(){
        return new JdbcClientDetailsService(dataSource);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(jdbcClientDetailsService());
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security.allowFormAuthenticationForClients()
                .accessDeniedHandler(new AuthAccessDeniedHandler())
                .authenticationEntryPoint(new UnAuthenticationEntryPoint())
                .checkTokenAccess("isAuthenticated()")
                .tokenKeyAccess("permitAll()");
    }
}