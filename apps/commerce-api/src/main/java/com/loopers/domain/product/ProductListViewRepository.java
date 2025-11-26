package com.loopers.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductListViewRepository {
  Page<ProductListView> findByBrandId(Long brandId, Pageable pageable);

  Page<ProductListView> findAll(Pageable pageable);

  Optional<ProductListView> getProductListView(Long productId);

  ProductListView save(ProductListView productListView);

  List<ProductListView> saveAll(List<ProductListView> productListView);

}
