package com.scb.settlement.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AmazonS3ServiceTest {

    private static final String BUCKET = "rider-internal-content-test";
    private static final String FOLDER_NAME = "settlement";
    private static final String KEY = "S1000000001";
    private static final String TEMP_FILE_NAME = "temp.txt";

    @InjectMocks
    private AmazonS3Service amazonS3Service;

    @Mock
    private AmazonS3 s3Client;

    @Before
    void init() {
        ReflectionTestUtils.setField(amazonS3Service, "bucketName", BUCKET);
    }

    @Test
    void shouldUploadFile() throws IOException {
        File file = null;
        try {
            file = new File(TEMP_FILE_NAME);
            file.createNewFile();
            String result = amazonS3Service.uploadFile(file, FOLDER_NAME, KEY);
            assertNotNull(result, "Invalid Response");
            verify(s3Client, times(1)).putObject(any(PutObjectRequest.class));
        } finally {
            if(Objects.nonNull(file)) Files.deleteIfExists(file.toPath());
        }
    }

    @Test
    void shouldDownloadFile() throws IOException {
        S3Object s3Object = new S3Object();
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(s3Object);
        byte[] result = amazonS3Service.downloadFile(FOLDER_NAME, KEY, TEMP_FILE_NAME);
        assertNotNull(result, "Invalid Response");
    }
}
