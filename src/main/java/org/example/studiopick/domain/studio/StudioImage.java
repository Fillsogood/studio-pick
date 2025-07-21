package org.example.studiopick.domain.studio;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "studio_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudioImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "studio_id", nullable = false)
  private Studio studio;

  @Column(name = "image_url", nullable = false, length = 255)
  private String imageUrl;

  @Column(name = "is_thumbnail")
  private Boolean isThumbnail = false;

  @Column(name = "display_order")
  private Integer displayOrder = 0;

  @Builder
  public StudioImage(Studio studio, String imageUrl, Boolean isThumbnail, Integer displayOrder) {
    this.studio = studio;
    this.imageUrl = imageUrl;
    this.isThumbnail = isThumbnail != null ? isThumbnail : false;
    this.displayOrder = displayOrder != null ? displayOrder : 0;
  }

  public void setStudio(Studio studio) {this.studio = studio;}
  public void setThumbnail(boolean isThumbnail) {this.isThumbnail = isThumbnail;}
  public void updateImageOrder(int displayOrder, boolean isThumbnail) {
    this.displayOrder = displayOrder;
    this.isThumbnail = isThumbnail;
  }

  public StudioImage(String imageUrl) {
    this.imageUrl = imageUrl;
  }
}
