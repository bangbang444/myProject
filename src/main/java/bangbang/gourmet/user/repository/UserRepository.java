package bangbang.gourmet.user.repository;

import bangbang.gourmet.common.domain.SocialProvider;
import bangbang.gourmet.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmailAndProvider(String email, SocialProvider provider);
}
