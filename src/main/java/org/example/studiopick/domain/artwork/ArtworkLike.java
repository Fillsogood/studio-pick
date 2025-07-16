package org.example.studiopick.domain.artwork;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.example.studiopick.domain.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "artwork_like", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"artwork_id", "user_id"})})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtworkLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artwork_id", nullable = false)
    private Artwork artwork;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)

    private User user;

    private LocalDateTime createdAt = LocalDateTime.now();
}
