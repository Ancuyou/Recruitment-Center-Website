package com.example.tuyendung.security;

import com.example.tuyendung.entity.TaiKhoan;
import com.example.tuyendung.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final TaiKhoanRepository taiKhoanRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        TaiKhoan taiKhoan = taiKhoanRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản với email: " + email));

        if (!taiKhoan.getLaKichHoat()) {
            throw new UsernameNotFoundException("Tài khoản đã bị khóa");
        }

        return new User(
                taiKhoan.getEmail(),
                taiKhoan.getMatKhauHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + taiKhoan.getVaiTro().name()))
        );
    }
}