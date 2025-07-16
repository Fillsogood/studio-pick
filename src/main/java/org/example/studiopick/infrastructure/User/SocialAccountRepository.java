package org.example.studiopick.infrastructure.User;

import org.example.studiopick.domain.user.SocialAccount;
import org.example.studiopick.domain.common.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndSocialId(SocialProvider provider, String socialId);
}