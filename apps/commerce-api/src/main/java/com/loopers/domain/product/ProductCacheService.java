
package com.loopers.domain.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.product.ProductWithLikeCount;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class ProductCacheService {
  private final ProductListViewService productListViewService;
  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper; // Jackson ObjectMapper


  private static final String CACHE_KEY_PREFIX = "product:list:"; // 페이징 단위 캐싱

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

  public void evictCache() {
    Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
    if (keys != null && !keys.isEmpty()) {
      redisTemplate.delete(keys);
    }
  }
}
