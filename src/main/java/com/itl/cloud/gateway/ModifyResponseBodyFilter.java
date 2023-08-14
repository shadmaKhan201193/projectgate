package com.itl.cloud.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

@Component

public class ModifyResponseBodyFilter implements GlobalFilter, Ordered {
	@Autowired
	private ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilterFactory;

	@Autowired
	private BodyRewrite bodyRewrite;

	@Autowired
	private EncryptionAES256 ecryptionAES256;

	@Autowired
	private RouterValidator routerValidator;
	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// try {
		String path = exchange.getRequest().getPath().toString();
		ServerHttpRequest request = exchange.getRequest();
		String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

		if (path.equalsIgnoreCase("/loginService/login") || path.equalsIgnoreCase("/loginService/usersRegister")
				|| path.startsWith("/loginService/user") || path.equalsIgnoreCase("/customerService/ClientCustomer")) {
			GatewayFilter delegate = modifyResponseBodyGatewayFilterFactory
					.apply(new ModifyResponseBodyGatewayFilterFactory.Config().setRewriteFunction(String.class,
							String.class, bodyRewrite));
			return delegate.filter(exchange, chain);
		}

		else if (header.startsWith("API")) {
			exchange.getResponse().getHeaders().set("Authorization", header);
			return chain.filter(exchange);
		} else {
			if (routerValidator.isSecured.test(request)) {
				if (this.isAuthMissing(request))
					return this.onError(exchange, "Authorization header is missing in request",
							HttpStatus.UNAUTHORIZED);

				final String token = this.getAuthHeader(request);
				String decryptToken = ecryptionAES256.decrypt(token);/// token decryption
				if (!jwtTokenUtil.validateToken(token, decryptToken))
					return this.onError(exchange, "Authorization header is invalid", HttpStatus.UNAUTHORIZED);

				this.populateRequestWithHeaders(exchange, decryptToken);
			}
			return chain.filter(exchange);
		}

	}

	// ----------------------------------------------------------------

	private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(httpStatus);
		return response.setComplete();
	}

	private String getAuthHeader(ServerHttpRequest request) {
		return request.getHeaders().getOrEmpty("Authorization").get(0);
	}

	private boolean isAuthMissing(ServerHttpRequest request) {
		return !request.getHeaders().containsKey("Authorization");
	}

	private void populateRequestWithHeaders(ServerWebExchange exchange, String token) {
		Claims claims = jwtTokenUtil.getAllClaimsFromToken(token);
		try {
			exchange.getRequest().mutate().header("id", String.valueOf(claims.get("id")))
					.header("role", String.valueOf(claims.get("role"))).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getOrder() {
		return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
	}
}