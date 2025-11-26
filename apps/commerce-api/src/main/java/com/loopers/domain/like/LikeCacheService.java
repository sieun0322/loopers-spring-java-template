
package com.loopers.domain.like;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.like.LikeInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@RequiredArgsConstructor
@Component
public class LikeCacheService {

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;
  private final LikeService likeService;

  private static final Duration USER_LIKE_TTL = Duration.ofMinutes(10);
  private static final Duration LIKE_COUNT_TTL = Duration.ofMinutes(10);

  public LikeInfo getLikeInfo(long userId, long productId) {
    // 사용자별 좋아요 여부
    String likedKey = "product:liked:" + userId + ":" + productId;
    Boolean isLiked = getFromCache(likedKey, Boolean.class);
    if (isLiked == null) {
      isLiked = likeService.isLiked(userId, productId);
      putToCache(likedKey, isLiked, Duration.ofMinutes(10));
    }

    // 상품별 좋아요 수
    String countKey = "product:likeCount:" + productId;
    Long likeCount = getFromCache(countKey, Long.class);
    if (likeCount == null) {
      likeCount = likeService.getLikeCount(productId);
      putToCache(countKey, likeCount, Duration.ofMinutes(10));
    }

    return new LikeInfo(likeCount, isLiked);
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
}

