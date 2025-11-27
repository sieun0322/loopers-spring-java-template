package com.loopers.infrastructure.product;

import com.loopers.domain.view.ProductListView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductListViewJpaRepository extends JpaRepository<ProductListView, Long> {
  Page<ProductListView> findByBrandId(Long brandId, Pageable pageable);

  Optional<ProductListView> findByProductId(Long brandId);

  List<ProductListView> save(List<ProductListView> product);

}
