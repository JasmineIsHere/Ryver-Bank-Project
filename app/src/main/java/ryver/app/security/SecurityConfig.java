package ryver.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)  
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    private UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userSvc){
        this.userDetailsService = userSvc;
    }
    
    /** 
     * Attach the user details and password encoder.
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth)
        throws Exception {
        auth
        .userDetailsService(userDetailsService)
        .passwordEncoder(encoder());
    }

    /**
     * The various authority levels for the different roles
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
        .httpBasic()
            .and() 
        .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/api/customers").hasRole("MANAGER")
            .antMatchers(HttpMethod.POST, "/api/customers").hasRole("MANAGER")
            .antMatchers(HttpMethod.GET, "/api/customers/*").hasAnyRole("MANAGER", "USER")
            .antMatchers(HttpMethod.PUT, "/api/customers/*").hasAnyRole("MANAGER", "USER")
            
            .antMatchers(HttpMethod.GET, "/api/accounts").hasAnyRole("MANAGER", "USER")
            .antMatchers(HttpMethod.POST, "/api/accounts").hasRole("MANAGER")
            .antMatchers(HttpMethod.GET, "/api/accounts/*").hasAnyRole("MANAGER", "USER")
            .antMatchers(HttpMethod.POST, "/api/accounts/*/transactions").hasRole("USER")
            .antMatchers(HttpMethod.GET, "/api/accounts/*/transactions").hasRole("USER")

            .antMatchers(HttpMethod.GET, "/api/contents").hasAnyRole("MANAGER", "USER", "ANALYST")
            .antMatchers(HttpMethod.GET, "/api/contents/*").hasAnyRole("MANAGER", "USER", "ANALYST")
            .antMatchers(HttpMethod.POST, "/api/contents").hasAnyRole("MANAGER", "ANALYST")
            .antMatchers(HttpMethod.PUT, "/api/contents/*").hasAnyRole("MANAGER", "ANALYST")
            .antMatchers(HttpMethod.DELETE, "/api/contents/*").hasAnyRole("MANAGER", "ANALYST")

            .antMatchers(HttpMethod.GET, "/api/portfolio").hasRole("USER")
            
            .antMatchers(HttpMethod.GET, "/api/trades").hasRole("USER")
            .antMatchers(HttpMethod.GET, "/api/trades/{tradeId}").hasRole("USER")
            .antMatchers(HttpMethod.POST, "/api/trades").hasRole("USER")
            .antMatchers(HttpMethod.PUT, "/api/trades/{tradeId}").hasRole("USER")
            .and()
        .csrf().disable() // CSRF protection is needed only for browser based attacks
        .formLogin().disable()
        .headers().disable()
        ;
    }

    /**
     * @Bean annotation is used to declare a PasswordEncoder bean in the Spring application context. 
     * Any calls to encoder() will then be intercepted to return the bean instance.
     */
    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}
 