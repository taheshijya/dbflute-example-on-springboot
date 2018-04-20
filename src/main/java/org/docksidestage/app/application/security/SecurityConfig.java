package org.docksidestage.app.application.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

/**
 * @author inoue on 2016/12/18.
 * @author jflute
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailService;

    //private LoginService loginService;

    //private UserDetailsService userDetailService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // #for_now for test, enable later by jflute
        http.authorizeRequests()
                .antMatchers("/login", "/register")
                .permitAll()
                .antMatchers("/css/**", "/js/**", "/images/**")
                .permitAll()
                .antMatchers(HttpMethod.POST, "/login")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/member")
                .failureUrl("/login")
                .usernameParameter("memberAccount")
                .passwordParameter("password")
                .permitAll()
                .loginPage("/login")
                .and()
                .logout()
                .logoutSuccessUrl("/login")
                .permitAll()
                .and()
                .csrf()
                .disable();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailService).passwordEncoder(new StandardPasswordEncoder()); // 既存のsampleプロジェクトに合わせる
        // 既存のsampleプロジェクトとはencode後の値が異なる。ser⇒5279bf7d2b9b3e8bccae7c1d8bb9a98577b78fd7dcbc8fa4fab72b72a54bf87318bc4453ca219bce
        //auth.userDetailsService(userDetailService).passwordEncoder(new PlaintextPasswordEncoder());

    }
}
