package br.ifpb.project.denguemaps.pdmuserms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
public class PdmUserMsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdmUserMsApplication.class, args);
    }

}
