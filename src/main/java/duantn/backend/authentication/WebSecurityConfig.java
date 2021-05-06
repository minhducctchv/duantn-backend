package duantn.backend.authentication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@ComponentScan
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    final
    CustomUserDetailsService customUserDetailsService;

    final
    CustomJwtAuthenticationFilter customJwtAuthenticationFilter;

    final
    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public WebSecurityConfig(CustomUserDetailsService customUserDetailsService, CustomJwtAuthenticationFilter customJwtAuthenticationFilter, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.customUserDetailsService = customUserDetailsService;
        this.customJwtAuthenticationFilter = customJwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception
    {
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        // We don't need CSRF for this example
        httpSecurity.csrf().disable().cors().and()
                // dont authenticate this particular request
                .authorizeRequests()
                .antMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .antMatchers("/super-admin/**").hasRole("SUPER_ADMIN")
                .antMatchers("/customer/**").hasRole("CUSTOMER")
                .antMatchers("/refreshtoken").authenticated()
                .antMatchers("/**").permitAll()
                .anyRequest().authenticated()
                .and().exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).
                and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
                and().addFilterBefore(customJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);;
    }
}
