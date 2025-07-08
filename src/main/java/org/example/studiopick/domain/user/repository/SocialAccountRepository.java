package org.example.studiopick.domain.user.repository;

import org.example.studiopick.domain.user.entity.SocialAccount;
import org.example.studiopick.domain.common.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndSocialId(SocialProvider provider, String socialId);
}