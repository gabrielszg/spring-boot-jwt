package com.cognizant.dio.jwt.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.cognizant.dio.jwt.data.UserData;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private final AuthenticationManager authenticationManager;

	public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
			                                    throws AuthenticationException {
		try {
			UserData creds = new ObjectMapper()
					.readValue(req.getInputStream(), UserData.class);
			
			return authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							creds.getUserName(),
							creds.getPassword(),
							new ArrayList<>())
					);
		}catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@Override
	protected void successfulAuthentication(HttpServletRequest req,
				   HttpServletResponse res,
				   FilterChain chain,
				   Authentication auth) throws IOException, ServletException {
		
		String token = JWT.create()
				.withSubject(((User) auth.getPrincipal()).getUsername())
				.withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstrants.EXPIRATION_TIME))
				.sign(Algorithm.HMAC512(SecurityConstrants.SECRET.getBytes()));
		
		res.addHeader(SecurityConstrants.HEADER_STRING, SecurityConstrants.TOKEN_PREFIX + token);
	}
}
