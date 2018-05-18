package org.docksidestage.app.application.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

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
    public void configure(WebSecurity web) throws Exception {
        // セキュリティ設定を無視するリクエスト設定
        // 静的リソース(images、css、javascript)に対するアクセスはセキュリティ設定を無視する
        web.ignoring().antMatchers("/static/**", "/images/**", "/css/**", "/js/**", "/webjars/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // #for_now for test, enable later by jflute

        // 認可の設定
        http.authorizeRequests().antMatchers("/", "/index").permitAll() // indexは全ユーザーアクセス許可
                .antMatchers("/login", "/register")
                .permitAll()
                .antMatchers(HttpMethod.POST, "/login")
                .permitAll()
                .anyRequest()
                .authenticated(); // それ以外は全て認証無しの場合アクセス不許可

        http.formLogin()
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/mypage")
                .failureUrl("/login")
                .usernameParameter("memberAccount")
                .passwordParameter("password")
                .permitAll()
                .loginPage("/login")
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .permitAll()
                .and()
                .csrf()
                .disable();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailService).passwordEncoder(new StandardPasswordEncoder()); // 既存のsampleプロジェクトに合わせる
        // 既存のsampleプロジェクトとはencode後の値が異なる。sea⇒5279bf7d2b9b3e8bccae7c1d8bb9a98577b78fd7dcbc8fa4fab72b72a54bf87318bc4453ca219bce
        //auth.userDetailsService(userDetailService).passwordEncoder(new PlaintextPasswordEncoder());

    }
}
