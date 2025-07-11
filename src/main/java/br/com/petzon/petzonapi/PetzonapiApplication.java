package br.com.petzon.petzonapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class PetzonapiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetzonapiApplication.class, args);
    }

}
