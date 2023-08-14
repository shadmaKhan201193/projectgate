package com.itl.cloud.gateway;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itl.entities.TokenEntity;
import com.itl.repo.TokenRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenUtil {

	public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;

	private String secret = "infrasoft";
	
	@Autowired
	private EncryptionAES256 ecryptionAES256;
	
	
	@Autowired
	private TokenRepository tokenRepo;

	Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	private boolean isTokenExpired(String token) {
		return this.getAllClaimsFromToken(token).getExpiration().before(new Date());
	}

	// retrieve expiration date from jwt token
	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	// validate token
	public Boolean validateToken(String token,String decryptToken ) {
		//final String username = getUsernameFromToken(token);
		//String decryptToken=ecryptionAES256.decrypt(token);///token decryption
		return (!isTokenExpired(decryptToken) && isTokenPresent(token));
		// return (username.equals(userDetails.getUsername()) &&
		// !isTokenExpired(token));

	}

	// retrieve username from jwt token
	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	public String generateToken(String obj) {
		Map<String, Object> claims = new HashMap<>();
		return doGenerateToken(claims, obj);
	}

	private String doGenerateToken(Map<String, Object> claims, String subject) {
		return Jwts.builder().setClaims(claims).setSubject(subject)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
				.signWith(SignatureAlgorithm.HS512, secret).compact();
	}

	// check token in database
	private boolean isTokenPresent(String token) {
		TokenEntity entity = tokenRepo.findByToken(token);
		if (entity != null) {
			return true;
		}
		return false;
	}

}