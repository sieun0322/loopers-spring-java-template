package com.loopers.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductListViewRepository {
  Page<ProductListView> findByBrandId(Long brandId, Pageable pageable);

  Page<ProductListView> findAll(Pageable pageable);

}
