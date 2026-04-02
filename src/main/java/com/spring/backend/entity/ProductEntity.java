package com.spring.backend.entity;

import com.spring.backend.enums.ProductStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
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

  @Column(name = "is_actived")
  @Builder.Default
  private Boolean isActived = true;

  @Column(name = "name")
  private String name;

  @Column(name = "price")
  private double price;

  @Column(name = "start_date")
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  @Column(name = "code")
  private String code;

  @Column(name = "description")
  private String description;

  @Column(name = "stock_qty", columnDefinition = "integer default 0", nullable = false)
  @Builder.Default
  private int stockQty = 0;

  @Column(name = "reserved_qty", columnDefinition = "integer default 0", nullable = false)
  @Builder.Default
  private int reservedQty = 0;

  @Column(name = "available_qty", columnDefinition = "integer default 0", nullable = false)
  @Builder.Default
  private int availableQty = 0;

  public void setStockQty(int stockQty) {
    this.stockQty = stockQty;
    this.availableQty = this.stockQty - this.reservedQty;
  }

  public void setReservedQty(int reservedQty) {
    this.reservedQty = reservedQty;
    this.availableQty = this.stockQty - this.reservedQty;
  }

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private ProductStatus status;

  @OneToMany(
      mappedBy = "product",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private List<ImageEntity> images;

  @Column(name = "daily_profit")
  private Double dailyProfit;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private CategoryEntity category;

  @ManyToOne(fetch = FetchType.LAZY)
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
