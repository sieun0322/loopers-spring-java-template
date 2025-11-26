package com.loopers.application.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.stock.Stock;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public record ProductStock(Product product, Stock stock) {
  public static ProductStock from(Product product, Stock stock) {
    if (product == null) throw new CoreException(ErrorType.NOT_FOUND, "상품정보를 찾을수 없습니다.");
    if (stock == null) throw new CoreException(ErrorType.NOT_FOUND, "재고정보를 찾을수 없습니다.");
    return new ProductStock(
        product,
        stock
    );
  }
}
