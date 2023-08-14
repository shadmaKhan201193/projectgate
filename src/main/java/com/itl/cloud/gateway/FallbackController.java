package com.itl.cloud.gateway;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class FallbackController {

    @RequestMapping("/masterFallBack")
    public Mono<String> masterServiceFallBack() {
        return Mono.just("Master Service is taking too long to respond or is down. Please try again later");
    }
    
    @RequestMapping("/custServiceFallback")
    public Mono<String> custServiceFallback() {
        return Mono.just("Customer Service is taking too long to respond or is down. Please try again later");
    }
}
