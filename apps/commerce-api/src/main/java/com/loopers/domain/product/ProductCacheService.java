
package com.loopers.domain.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.product.ProductStock;
import com.loopers.application.product.ProductWithLikeCount;
import com.loopers.domain.stock.Stock;
import com.loopers.domain.stock.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class ProductCacheService {
  private final ProductListViewService productListViewService;
  private final ProductService productService;
  private final StockService stockService;
  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper; // Jackson ObjectMapper


  private static final String CACHE_KEY_PREFIX = "product:list:"; // 페이징 단위 캐싱
  private static final Duration DETAIL_TTL = Duration.ofMinutes(10);

  public Page<ProductWithLikeCount> getProducts(Long brandId,
                                                String sort,
                                                int page,
                                                int size) {
    String key = CACHE_KEY_PREFIX + brandId + ":" + page + ":" + size + ":" + sort;
    String cachedJson = redisTemplate.opsForValue().get(key);
    if (cachedJson != null) {
      try {
        List<ProductWithLikeCount> cachedList = objectMapper.readValue(
            cachedJson,
            new TypeReference<List<ProductWithLikeCount>>() {
            }
        );
        return new PageImpl<>(cachedList, PageRequest.of(page, size), cachedList.size());
      } catch (JsonProcessingException e) {
        // 직렬화 실패 시 캐시 무시
        e.printStackTrace();
      }
    }
    Page<ProductListView> productPage = productListViewService.getProducts(brandId, sort, page, size);
    List<ProductListView> products = productPage.getContent();

    List<ProductWithLikeCount> list = products
        .stream()
        .map(item -> new ProductWithLikeCount(item.getProductId(), item.getName(), item.getPrice(), item.getLikeCount())
        )
        .toList();
    try {
      String json = objectMapper.writeValueAsString(list);
      redisTemplate.opsForValue().set(key, json, Duration.ofMinutes(10));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return new PageImpl<>(list, productPage.getPageable(), productPage.getTotalElements());
  }

  public ProductStock getProduct(long productId) {
    String key = "product:detail:" + productId;
    ProductStock cached = getFromCache(key, ProductStock.class);
    if (cached != null) return cached;

    Product product = productService.getProduct(productId);
    Stock stock = stockService.getStock(productId);
    ProductStock productStock = ProductStock.from(product, stock);
    putToCache(key, productStock, DETAIL_TTL);
    return productStock;
  }

  private <T> T getFromCache(String key, Class<T> clazz) {
    String json = redisTemplate.opsForValue().get(key);
    if (json == null) return null;
    try {
      return objectMapper.readValue(json, clazz);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return null;
    }
  }

  private void putToCache(String key, Object value, Duration ttl) {
    try {
      redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  public void evictCache() {
    Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
    if (keys != null && !keys.isEmpty()) {
      redisTemplate.delete(keys);
    }
  }
}
