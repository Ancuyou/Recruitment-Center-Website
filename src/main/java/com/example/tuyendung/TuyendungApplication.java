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
            System.out.println("✅ HỆ THỐNG ĐÃ KHỞI ĐỘNG XONG.");
            System.out.println("--------------------------------------------------");
            System.out.println("🔑 RAW PASSWORD CHO MOCK DATA: " + rawPass);
            System.out.println("🔑 HASH EXAMPLE (BCRYPT): " + hashed);
            System.out.println("--------------------------------------------------");
            System.out.println("🎯 DANH SÁCH TÀI KHOẢN MẪU DÀNH CHO GIẢNG VIÊN REVIEW:");
            System.out.println("   [ADMIN]          Email: admin@system.local       | Password: 123");
            System.out.println("   [NHÀ TUYỂN DỤNG] Email: hr1@fsoft.local          | Password: 123");
            System.out.println("                    Email: hr2@vng.local            | Password: 123");
            System.out.println("   [ỨNG VIÊN]       Email: candidate1@mail.local    | Password: 123");
            System.out.println("                    Email: candidate2@mail.local    | Password: 123");
            System.out.println("--------------------------------------------------");
        };
    }
}