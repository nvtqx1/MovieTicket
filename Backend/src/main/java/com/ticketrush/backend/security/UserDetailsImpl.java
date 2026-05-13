package com.ticketrush.backend.security;

import com.ticketrush.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Triển khai UserDetails để Spring Security xác thực người dùng TicketRush.
 *
 * Username của Spring Security được ánh xạ bằng email vì hệ thống đăng nhập bằng
 * email.
 */
@Getter
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private Long id;
    private String email;
    private String password;
    private boolean banned;
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * Chuyển entity User sang UserDetailsImpl.
     *
     * @param user entity người dùng cần chuyển đổi.
     * @return đối tượng UserDetailsImpl dùng cho Spring Security.
     */
    public static UserDetailsImpl build(User user) {
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().getName());

        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getIsBanned() != null && user.getIsBanned(),
                Collections.singletonList(authority)
        );
    }

    /**
     * Lấy danh sách quyền của người dùng.
     *
     * @return danh sách GrantedAuthority.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Lấy mật khẩu đã mã hóa của người dùng.
     *
     * @return mật khẩu đã mã hóa.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Lấy username theo chuẩn Spring Security.
     *
     * @return email dùng để đăng nhập.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Kiểm tra tài khoản chưa hết hạn.
     *
     * @return luôn true vì hệ thống chưa dùng hết hạn tài khoản.
     */
    @Override
    public boolean isAccountNonExpired() { return true; }

    /**
     * Kiểm tra tài khoản không bị khóa.
     *
     * @return true nếu người dùng không bị ban.
     */
    @Override
    public boolean isAccountNonLocked() { return !banned; }

    /**
     * Kiểm tra thông tin đăng nhập chưa hết hạn.
     *
     * @return luôn true vì hệ thống chưa dùng hết hạn mật khẩu.
     */
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    /**
     * Kiểm tra tài khoản đang được bật.
     *
     * @return true nếu người dùng không bị ban.
     */
    @Override
    public boolean isEnabled() { return !banned; }
}
