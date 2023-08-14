package com.itl.cloud.gateway;
import java.util.List;

import org.springframework.stereotype.Component;

import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.function.Predicate;

@Component
public class RouterValidator {

	 public static final List<String> openApiEndpoints= List.of(
	            "/loginService/login"
	    );

	    public Predicate<ServerHttpRequest> isSecured =request -> openApiEndpoints
	    		.stream().noneMatch(uri -> request.getURI().getPath().contains(uri));
	
}
