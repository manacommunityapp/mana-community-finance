package com.manacommunity.api.support;

import com.manacommunity.api.model.Community;
import com.manacommunity.api.user.model.AppUser;
import com.manacommunity.api.user.security.UserPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collections;

public class TestDataBuilder {

    public static Community community() {
        return Community.builder()
                .id(100L)
                .name("Test Community")
                .city("Metro")
                .state("State")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static AppUser adminUser() {
        Community comm = community();

        return AppUser.builder()
                .id(200L)
                .fullName("Admin User")
                .email("admin@example.com")
                .passwordHash("hashedpassword")
                .community(comm)
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
