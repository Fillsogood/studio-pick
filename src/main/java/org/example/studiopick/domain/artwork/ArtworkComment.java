package org.example.studiopick.domain.artwork;

import jakarta.persistence.*;
import lombok.*;
import org.example.studiopick.domain.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "artwork_comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtworkComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artwork_id", nullable = false)
    private Artwork artwork;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 300)
    private String comment;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public void updateComment(String newComment) {
        this.comment = newComment;
    }

}
