package com.example.tuyendung.security;

import com.example.tuyendung.entity.TaiKhoan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final TaiKhoan taiKhoan;

    public Long getId() {
        return taiKhoan.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + taiKhoan.getVaiTro().name()));
    }

    @Override
    public String getPassword() {
        return taiKhoan.getMatKhauHash();
    }

    @Override
    public String getUsername() {
        return taiKhoan.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return taiKhoan.getLaKichHoat();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return taiKhoan.getLaKichHoat();
    }
}
