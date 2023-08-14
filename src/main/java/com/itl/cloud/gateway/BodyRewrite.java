package com.itl.cloud.gateway;

import java.util.Map;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itl.entities.TokenEntity;
import com.itl.repo.TokenRepository;

import reactor.core.publisher.Mono;

@Component
public class BodyRewrite implements RewriteFunction<String, String> {

	@Autowired
	private TokenRepository tokenRepo;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	private ObjectMapper objectMapper;

	@Autowired
	private EncryptionAES256 ecryptionAES256;

	public BodyRewrite(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public Publisher<String> apply(ServerWebExchange exchange, String body) {
		String token = null;
		String path = exchange.getRequest().getPath().toString();
		HttpStatus statusCode = exchange.getResponse().getStatusCode();
		try {
			if (path.equalsIgnoreCase("/loginService/login")) {
				if (body != null && statusCode.value() == 200) {

					Map<String, Object> map = objectMapper.readValue(body, Map.class);
					Object userName = map.get("userName");
					String name = userName.toString();
					System.out.println();
					token = jwtTokenUtil.generateToken(name);
					String encryptToken = ecryptionAES256.encrypt(token); /// token encryption

					TokenEntity tokenentity = new TokenEntity();
					tokenentity.setToken(encryptToken);
					tokenentity.setUserName(name);
					tokenRepo.save(tokenentity);
					exchange.getResponse().getHeaders().set("Authorization", encryptToken);

					return Mono.just(objectMapper.writeValueAsString(map));
				}

//				else if (statusCode.value() == 404) {
//					String HttpStatus = statusCode.UNAUTHORIZED.toString();
//					return Mono.just(HttpStatus);
//				} 
				else if ((statusCode.value() != 404) && (statusCode.value() != 200)) {

					return Mono.just(body);
				}
			} // first if close here

		} catch (Exception ex) {

			return Mono.error(new Exception("2. json process fail", ex));
		}

		return Mono.just(body);
	}
}