package org.example.studiopick.domain.workshop;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "workshop_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkShopImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workshop_id", nullable = false)
  private WorkShop workShop;

  @Column(name = "image_url", nullable = false, length = 255)
  private String imageUrl;

  @Builder
  public WorkShopImage(WorkShop workShop, String imageUrl) {
    this.workShop = workShop;
    this.imageUrl = imageUrl;
  }
}