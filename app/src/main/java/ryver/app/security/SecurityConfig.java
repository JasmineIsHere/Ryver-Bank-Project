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
@EnableGlobalMethodSecurity(prePostEnabled = true)  
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
     * Customer role: can view their own accounts; can update own phone number, password and address
     * Manager role: can add/delete/update customers/accounts, and add customers; can view all customers/accounts
     * 
     * 
     * */
  
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
        .httpBasic()
            .and() 
        .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/customers").hasRole("MANAGER")
            .antMatchers(HttpMethod.POST, "/customers").hasRole("MANAGER")

            .antMatchers(HttpMethod.GET, "/customers/*/accounts").hasAnyRole("MANAGER", "USER")
            .antMatchers(HttpMethod.POST, "/customers/*/accounts").hasRole("MANAGER")

            .antMatchers(HttpMethod.GET, "/content").hasAnyRole("MANAGER", "USER", "ANALYST")
            .antMatchers(HttpMethod.POST, "/content").hasAnyRole("MANAGER", "ANALYST")
            .antMatchers(HttpMethod.PUT, "/content/*").hasAnyRole("MANAGER", "ANALYST")
            .antMatchers(HttpMethod.DELETE, "/content/*").hasAnyRole("MANAGER", "ANALYST")

            .antMatchers(HttpMethod.GET, "/portfolio/*").hasRole("USER")
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
 