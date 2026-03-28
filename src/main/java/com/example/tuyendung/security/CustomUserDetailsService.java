package com.example.tuyendung.security;

import com.example.tuyendung.entity.TaiKhoan;
import com.example.tuyendung.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        return new CustomUserDetails(taiKhoan);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        TaiKhoan taiKhoan = taiKhoanRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản với id: " + id));
        
        if (!taiKhoan.getLaKichHoat()) {
            throw new UsernameNotFoundException("Tài khoản đã bị khóa");
        }

        return new CustomUserDetails(taiKhoan);
    }
}