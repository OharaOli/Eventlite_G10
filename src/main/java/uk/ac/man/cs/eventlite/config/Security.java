package uk.ac.man.cs.eventlite.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class Security extends WebSecurityConfigurerAdapter {

//	public static final String ADMIN_ROLE = "ADMINISTRATOR";
	public static final String ATTENDEES_ROLE = "ATTENDEES";
	public static final String ORGANISER_ROLE = "ORGANISER";

	// List the mappings/methods for which no authorisation is required.
	// By default we allow all GETs and full access to the H2 console.
	private static final RequestMatcher[] NO_AUTH = { 
			new AntPathRequestMatcher("/webjars/**", "GET"),
			new AntPathRequestMatcher("/**", "GET"), 
			new AntPathRequestMatcher("/h2-console/**"),
			new AntPathRequestMatcher("/events/tweet/**", "POST")};

	private static final RequestMatcher[] ORGANISER_AUTH = {
			new AntPathRequestMatcher("/**", "DELETE"),
			new AntPathRequestMatcher("/events/add", "GET"),
			new AntPathRequestMatcher("/events/add", "POST"),
			new AntPathRequestMatcher("/events/update/**", "POST"),
			new AntPathRequestMatcher("/events/update/**", "GET"),
			new AntPathRequestMatcher("/venues/add", "GET"),
			new AntPathRequestMatcher("/venues/add", "POST"),
			new AntPathRequestMatcher("/venues/update/**", "POST"),
			new AntPathRequestMatcher("/venues/update/**", "GET")};
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// By default, all requests are authenticated except our specific list.
		http.authorizeRequests().requestMatchers(NO_AUTH).permitAll().anyRequest().hasRole(ATTENDEES_ROLE);
		http.authorizeRequests().requestMatchers(ORGANISER_AUTH).permitAll().anyRequest().hasRole(ORGANISER_ROLE);
		
		// Use form login/logout for the Web.
		http.formLogin().loginPage("/sign-in").permitAll();
		http.logout().logoutUrl("/sign-out").logoutSuccessUrl("/").permitAll();

		// Use HTTP basic for the API.
		http.requestMatcher(new AntPathRequestMatcher("/api/**")).httpBasic();

		// Only use CSRF for Web requests.
		// Disable CSRF for the API and H2 console.
		http.antMatcher("/**").csrf().ignoringAntMatchers("/api/**", "/h2-console/**");

		// Disable X-Frame-Options for the H2 console.
		http.headers().frameOptions().disable();
	}

	@Override
	@Bean
	public UserDetailsService userDetailsService() {
		PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

		UserDetails rob = User.withUsername("Rob").password(encoder.encode("Haines")).roles(ATTENDEES_ROLE).build();
		UserDetails caroline = User.withUsername("Caroline").password(encoder.encode("Jay")).roles(ATTENDEES_ROLE).build();
		UserDetails markel = User.withUsername("Markel").password(encoder.encode("Vigo")).roles(ATTENDEES_ROLE).build();
		UserDetails mustafa = User.withUsername("Mustafa").password(encoder.encode("Mustafa")).roles(ATTENDEES_ROLE).build();
		UserDetails organiser = User.withUsername("Organiser").password(encoder.encode("Organiser")).roles(ORGANISER_ROLE).build();

		return new InMemoryUserDetailsManager(rob, caroline, markel, mustafa, organiser);
	}
}
