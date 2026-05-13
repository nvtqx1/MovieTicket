package com.ticketrush.backend.security;

import com.ticketrush.backend.entity.User;
import com.ticketrush.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service nạp thông tin người dùng cho Spring Security.
 *
 * Annotation {@link Service} đăng ký bean cho security config sử dụng khi xác
 * thực tài khoản.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Tải người dùng theo email đăng nhập.
     *
     * Annotation {@link Transactional} giữ persistence context trong quá trình
     * đọc user và role lazy để build UserDetails.
     *
     * @param email email đăng nhập.
     * @return thông tin người dùng theo chuẩn Spring Security.
     * @throws UsernameNotFoundException khi không tìm thấy người dùng theo email.
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản với email: " + email));

        return UserDetailsImpl.build(user);
    }
}
