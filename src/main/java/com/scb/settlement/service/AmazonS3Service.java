package com.scb.settlement.service;

import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
public class AmazonS3Service extends AmazonClientService {

    @Value("${amazon.s3.bucket-name}")
    private String bucketName;

    public String uploadFile(File file, String folderName, String key) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            return uploadInputStream(inputStream, folderName, key, file.getName());
        }
    }

    public String uploadInputStream(InputStream fileStream, String folderName, String key, String fileName) throws IOException {
        log.info("bucketName {}, folderName {}, key {}, fileName {}", bucketName, folderName, key, fileName);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(fileStream.available());
        objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
        PutObjectRequest putRequest = new PutObjectRequest(bucketName, getKey(folderName, key, fileName), fileStream, objectMetadata);
        getClient().putObject(putRequest);
        log.info("File {} has been uploaded successfully on s3", fileName);
        return fileName;
    }

    public byte[] downloadFile(String folderName, String key, String fileName) throws IOException {
        log.info("bucketName {}, folderName {}, key {}, fileName {}", bucketName, folderName, key, fileName);
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, getKey(folderName, key, fileName));
        S3Object object = getClient().getObject(getObjectRequest);
        return IOUtils.toByteArray(object.getObjectContent());
    }
    
    private String getKey(String folderName, String key, String fileName) {
        StringBuilder sb = new StringBuilder(folderName);
        sb.append("/").append(key).append("/").append(fileName);
        return sb.toString();
    }

}
