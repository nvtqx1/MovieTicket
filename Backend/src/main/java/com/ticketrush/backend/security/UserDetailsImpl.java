package com.ticketrush.backend.security;

import com.ticketrush.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter // Cần thiết để lấy id, email ra khi cần
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private Long id;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    // Hàm tiện ích: Biến Entity User thành UserDetailsImpl
    public static UserDetailsImpl build(User user) {
        // Lấy tên Role (VD: "ROLE_USER") bọc vào SimpleGrantedAuthority
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().getName());

        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(authority) // TicketRush 1 User có 1 Role
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    // getUsername nhưng ta trả về EMAIL vì dự án dùng Email để đăng nhập
    @Override
    public String getUsername() {
        return email;
    }

    // Các hàm kiểm tra trạng thái tài khoản (Tạm thời luôn trả về true)
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}