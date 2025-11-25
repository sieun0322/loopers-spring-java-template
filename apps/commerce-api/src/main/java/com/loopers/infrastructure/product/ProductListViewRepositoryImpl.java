package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductListView;
import com.loopers.domain.product.ProductListViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductListViewRepositoryImpl implements ProductListViewRepository {
  private final ProductListViewJpaRepository jpaRepository;

  @Override
  public Page<ProductListView> findByBrandId(Long brandId, Pageable pageable) {
    return jpaRepository.findByBrandId(brandId, pageable);
  }

  @Override
  public Page<ProductListView> findAll(Pageable pageable) {
    return jpaRepository.findAll(pageable);
  }
}
