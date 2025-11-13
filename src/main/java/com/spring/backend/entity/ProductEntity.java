package com.spring.backend.entity;

import com.spring.backend.enums.ProductStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Table(name = "products")
@Entity
@Getter
@Setter
@SuperBuilder
public class ProductEntity extends BaseEntity {
  public ProductEntity() {}

  @Column(name = "name")
  private String name;

  @Column(name = "code", nullable = false, unique = true)
  private String code;

  @Column(name = "price")
  private double price;

  @Column(name = "startDay")
  private Instant startDay;

  @Column(name = "endDate")
  private Instant endDate;

  @Column(name = "type")
  private String type;

  @Column(name = "description")
  private String description;

  @Column(name = "quantity")
  private int quantity;

  @Column(name = "status")
  private ProductStatus status;

  @Column(name = "category_id")
  private Long categoryId;

  @Column(name = "customer_id")
  private Long customerId;

  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<ImageEntity> images;

  public void addImage(ImageEntity image) {
    images.add(image);
    image.setProduct(this);
  }

  public void setImages(List<ImageEntity> images) {
    this.images = images;
    if (images != null) {
      for (ImageEntity image : images) {
        image.setProduct(this);
      }
    }
  }

  @PrePersist // Dùng để tự động tạo mã trước khi lưu vào DB
  protected void onCreate() {
    if (code == null) {
      code = UUID.randomUUID().toString(); // ví dụ dùng UUID
    }
  }
}
