package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductListView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductListViewJpaRepository extends JpaRepository<ProductListView, Long> {
  Page<ProductListView> findByBrandId(Long brandId, Pageable pageable);
}
