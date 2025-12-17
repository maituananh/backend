package com.spring.backend.entity;

import com.spring.backend.enums.ProductStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
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

  @Column(name = "price")
  private double price;

  @Column(name = "start_date")
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  @Column(name = "type")
  private String type;

  @Column(name = "code")
  private String code;

  @Column(name = "description")
  private String description;

  @Column(name = "quantity")
  private int quantity;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private ProductStatus status;

  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<ImageEntity> images;

  @Column(name = "daily_profit")
  private Double dailyProfit;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "category_id")
  private CategoryEntity category;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "customer_id")
  private UserEntity customer;

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
}
