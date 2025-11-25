package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.querydsl.core.annotations.Immutable;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Immutable
@Table(name = "product_list_view")
@Getter
public class ProductListView extends BaseEntity {

  private Long productId;
  private Long brandId;
  private String name;
  private BigDecimal price;
  private Long stockQuantity;
  private Long likeCount;


  protected ProductListView() {
  }

  public ProductListView(Long productId, Long brandId, String name, BigDecimal price,
                         Long stockQuantity, Long likeCount) {
    this.productId = productId;
    this.brandId = brandId;
    this.name = name;
    this.price = price;
    this.stockQuantity = stockQuantity;
    this.likeCount = likeCount;
  }
}
