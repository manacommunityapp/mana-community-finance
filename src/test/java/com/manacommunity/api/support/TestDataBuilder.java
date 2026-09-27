package com.manacommunity.api.support;

import com.manacommunity.common.user.model.AppUser;
import com.manacommunity.common.user.security.UserPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collections;

public class TestDataBuilder {

    public static AppUser adminUser() {
        return AppUser.builder()
                .id(200L)
                .fullName("Admin User")
                .email("admin@example.com")
                .passwordHash("hashedpassword")
                .role("ADMIN")
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static UserPrincipal createUserPrincipal(AppUser user) {
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
                user
        );
    }
}
