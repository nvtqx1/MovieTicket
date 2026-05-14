package com.ticketrush.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Tiện ích tạo, đọc và kiểm tra JWT.
 *
 * Secret được đọc từ cấu hình ứng dụng và giải mã Base64 để ký token bằng HS512.
 */
@Component
public class JwtUtils {
    @Value("${ticketrush.app.jwtSecret}")
    private String jwtSecret;

    @Value("${ticketrush.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    /**
     * Tạo khóa ký JWT từ secret Base64.
     *
     * @return khóa dùng để ký và kiểm tra JWT.
     */
    private Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Sinh JWT cho người dùng đã xác thực.
     *
     * @param authentication thông tin xác thực hiện tại.
     * @return JWT đã ký và có thời hạn.
     */
    public String generateJwtToken(Authentication authentication){
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Lấy username/email từ subject của JWT.
     *
     * @param token JWT cần đọc.
     * @return username/email trong subject.
     */
    public String getUserNameFromJwtToken(String token){
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * Kiểm tra JWT có hợp lệ và chưa bị sửa đổi hay không.
     *
     * @param authToken JWT cần kiểm tra.
     * @return true nếu token hợp lệ, ngược lại false.
     */
    public boolean validateJwtToken(String authToken){
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (Exception e) {
            // Token không hợp lệ, hết hạn hoặc sai chữ ký.
        }
        return false;
    }
}
