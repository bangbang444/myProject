package bangbang.gourmet.user.entity;

import bangbang.gourmet.common.domain.Role;
import bangbang.gourmet.common.domain.SocialProvider;
import bangbang.gourmet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialProvider provider;

    @Column(nullable = false, unique = true)
    private String providerId;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = true)
    private String imgUrl;

    @Builder
    public User(String email, String nickname, Role role, SocialProvider provider, String providerId) {
        this.email = email;
        this.nickname = nickname;
        this.role = role == null ? Role.ROLE_USER : role;
        this.provider = provider;
        this.providerId = providerId;
    }
}
