package br.com.petzon.petzonapi.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 s3client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public URL uploadFile(MultipartFile multipartFile) throws IOException {
        // Gera um nome de arquivo único para evitar colisões
        String originalFilename = multipartFile.getOriginalFilename();
        String newFileName = UUID.randomUUID().toString() + "_" + originalFilename;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        // Faz o upload do arquivo para o S3
        s3client.putObject(bucketName, newFileName, multipartFile.getInputStream(), metadata);

        // Retorna a URL pública do objeto no S3
        return s3client.getUrl(bucketName, newFileName);
    }
}