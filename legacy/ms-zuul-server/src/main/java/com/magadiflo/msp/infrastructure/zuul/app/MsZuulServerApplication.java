package com.magadiflo.msp.infrastructure.zuul.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@EnableZuulProxy
@EnableEurekaClient
@SpringBootApplication
public class MsZuulServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsZuulServerApplication.class, args);
	}

}
