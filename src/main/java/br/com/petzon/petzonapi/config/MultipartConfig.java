package br.com.petzon.petzonapi.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;

@Configuration
public class MultipartConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // Define limites para os arquivos (opcional, mas uma boa prática)
        factory.setMaxFileSize(DataSize.ofMegabytes(10)); // Tamanho máximo por arquivo: 10MB
        factory.setMaxRequestSize(DataSize.ofMegabytes(10)); // Tamanho máximo da requisição total: 10MB
        return factory.createMultipartConfig();
    }
}