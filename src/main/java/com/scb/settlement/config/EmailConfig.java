package com.scb.settlement.config;

import com.scb.settlement.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

@Configuration
public class EmailConfig {

    @Value("${secretsPath}")
    private String secretsPath;
    @Value("${spring.mail.host}")
    private String host;
    @Value("${spring.mail.port}")
    private Integer port;
    @Value("${spring.mail.username}")
    private String userName;

    @Value("${spring.mail.properties.mail.debug}")
    private String debug;
    @Value("${spring.mail.properties.mail.transport.protocol}")
    private String protocol;
    @Value("${spring.mail.properties.mail.smtp.auth}")
    private String auth;
    @Value("${spring.mail.properties.mail.smtp.connectiontimeout}")
    private String connectionTimeOut;
    @Value("${spring.mail.properties.mail.smtp.timeout}")
    private String timeOut;
    @Value("${spring.mail.properties.mail.smtp.writetimeout}")
    private String writeTimeOut;
    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private String enable;

    @Bean
    public JavaMailSender javaMailSender() throws IOException, URISyntaxException {
        URI passwordFilePath = ResourceUtils.getURL(secretsPath + "/SETTLEMENT_SMTP_PASSWORD").toURI();
        String password = CommonUtils.sanitize(Files.readAllBytes(Paths.get(passwordFilePath)));

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);

        mailSender.setUsername(userName);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();

        props.put("mail.transport.protocol", protocol);
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", enable);
        props.put("mail.debug", debug);
        props.put("mail.smtp.connectiontimeout", connectionTimeOut);
        props.put("mail.smtp.timeout", timeOut);
        props.put("mail.smtp.writetimeout", writeTimeOut);
        return mailSender;
    }
}
