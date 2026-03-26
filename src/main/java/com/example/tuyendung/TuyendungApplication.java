package com.example.tuyendung;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing  // Tự động fill @CreationTimestamp, @UpdateTimestamp
@EnableAspectJAutoProxy  // Enable AOP cho @Transactional
public class TuyendungApplication {

    public static void main(String[] args) {
        SpringApplication.run(TuyendungApplication.class, args);
        System.out.println("✅ Hệ thống Tuyển dụng MVP đã khởi động thành công!");
    }
}