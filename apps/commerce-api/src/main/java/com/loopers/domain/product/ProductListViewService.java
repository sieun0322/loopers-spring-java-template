
package com.loopers.domain.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class ProductListViewService {

  private final ProductListViewRepository productListViewRepository;

  @Transactional(readOnly = true)
  public Page<ProductListView> getProducts(
      Long brandId,
      String sortType,
      int page,
      int size
  ) {
    Sort sort = this.getSortBySortType(sortType);
    Pageable pageable = PageRequest.of(page, size, sort);
    Page<ProductListView> products = null;
    if (brandId != null) {
      products = productListViewRepository.findByBrandId(brandId, pageable);
    } else {
      products = productListViewRepository.findAll(pageable);
    }
    return products;
  }

  private Sort getSortBySortType(String sortType) {
    if (sortType == null) sortType = "latest";
    Sort latestSort = Sort.by("createdAt").descending();
    switch (sortType.toLowerCase()) {
      case "latest":
        return latestSort;
      case "price_asc":
        return Sort.by("totalPrice").ascending().and(latestSort);
      case "likes_desc":
        return Sort.by("likesCount").descending().and(latestSort);
      default:
        return latestSort;
    }
  }

}
