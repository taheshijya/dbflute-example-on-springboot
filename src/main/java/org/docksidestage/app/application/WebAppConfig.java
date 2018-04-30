package org.docksidestage.app.application;

import org.docksidestage.bizfw.GodHandableInterceptor;
import org.docksidestage.bizfw.RequestLoggingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author jflute
 */
@Configuration
public class WebAppConfig extends WebMvcConfigurerAdapter {

    @Bean
    public FilterRegistrationBean filterRegistrationBean() { // for logging
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        RequestLoggingFilter loggingFilter = new RequestLoggingFilter();
        registrationBean.setFilter(loggingFilter);
        registrationBean.setOrder(1);
        return registrationBean;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) { // for logging
        registry.addInterceptor(new GodHandableInterceptor()).addPathPatterns("/**").excludePathPatterns("/static/**");
        //.excludePathPatterns("/static/**", "/images/**", "/css/**", "/js/**", "/webjars/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //        if (!registry.hasMappingForPattern("/css/**")) {
        //            registry.addResourceHandler("/css/**").addResourceLocations("/resources/static/css/");
        //        }
        //        if (!registry.hasMappingForPattern("/images/**")) {
        //            registry.addResourceHandler("/images/**").addResourceLocations("/resources/static/images/");
        //        }
        //        if (!registry.hasMappingForPattern("/fonts/**")) {
        //            registry.addResourceHandler("/fonts/**").addResourceLocations("/resources/static/fonts/");
        //        }
        //        if (!registry.hasMappingForPattern("/js/**")) {
        //            registry.addResourceHandler("/js/**").addResourceLocations("/resources/static/js/");
        //        }

        //registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }
}
