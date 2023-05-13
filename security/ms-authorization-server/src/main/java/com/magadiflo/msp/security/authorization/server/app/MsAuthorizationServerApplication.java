package com.magadiflo.msp.security.authorization.server.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class MsAuthorizationServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsAuthorizationServerApplication.class, args);
	}

}
