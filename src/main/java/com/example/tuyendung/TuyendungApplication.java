package com.example.tuyendung;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableJpaAuditing  // Tự động fill @CreationTimestamp, @UpdateTimestamp
@EnableAspectJAutoProxy  // Enable AOP cho @Transactional
public class TuyendungApplication {

    public static void main(String[] args) {
        SpringApplication.run(TuyendungApplication.class, args);
        System.out.println("✅ Hệ thống Tuyển dụng MVP đã khởi động thành công!");
    }

    /**
     * [D1] Script hỗ trợ tạo mật khẩu hash để chèn vào DB/Mock data
     */
    @Bean
    public CommandLineRunner run() {
        return args -> {
            String rawPass = "123";
            String hashed = new BCryptPasswordEncoder().encode(rawPass);
            System.out.println("--------------------------------------------------");
            System.out.println("🔑 RAW PASSWORD: " + rawPass);
            System.out.println("🔑 HASHED PASSWORD (BCRYPT): " + hashed);
            System.out.println("--------------------------------------------------");
        };
    }
}