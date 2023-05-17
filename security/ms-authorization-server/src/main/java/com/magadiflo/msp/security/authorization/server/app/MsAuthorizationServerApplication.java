package com.magadiflo.msp.security.authorization.server.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableFeignClients
@EnableEurekaClient
@SpringBootApplication
public class MsAuthorizationServerApplication {

    private final Logger LOG = LoggerFactory.getLogger(MsAuthorizationServerApplication.class);
    private final PasswordEncoder passwordEncoder;

    public MsAuthorizationServerApplication(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public static void main(String[] args) {
        SpringApplication.run(MsAuthorizationServerApplication.class, args);
    }

    @Bean
    public CommandLineRunner run() throws Exception {
        return args -> {
            String password = "12345";
            for (int i = 0; i < 4; i++) {
                String passwordBcrypt = this.passwordEncoder.encode(password);
                LOG.info(passwordBcrypt);
            }
        };
    }
}
