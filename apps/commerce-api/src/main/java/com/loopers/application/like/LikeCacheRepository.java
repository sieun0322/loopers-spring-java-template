
package com.loopers.application.like;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.like.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@RequiredArgsConstructor
@Component
public class LikeCacheRepository {
  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;
  private final LikeService likeService;

  private static final Duration USER_LIKE_TTL = Duration.ofMinutes(10);
  private static final Duration LIKE_COUNT_TTL = Duration.ofMinutes(10);

  /**
   * 사용자별 좋아요 여부 조회
   */
  public Boolean getUserLiked(Long userId, Long productId) {
    String key = "product:liked:" + userId + ":" + productId;
    return getFromCache(key, Boolean.class);
  }

  public void putUserLiked(Long userId, Long productId, boolean liked) {
    String key = "product:liked:" + userId + ":" + productId;
    Boolean userLiked = getUserLiked(userId, productId);
    if (userLiked != null) {
      if (userLiked == true && liked == false) {
        subtractLikeCount(productId);
      } else if (userLiked == false && liked == true) {
        addLikeCount(productId);
      }
    } else {
      if (userLiked == null && liked == true) {
        addLikeCount(productId);
      }
    }
    putToCache(key, liked, USER_LIKE_TTL);
  }

  /**
   * 상품별 좋아요 수 조회
   */
  public Long getLikeCount(Long productId) {
    String key = "product:likeCount:" + productId;
    return getFromCache(key, Long.class);
  }

  public void putLikeCount(Long productId, Long count) {
    String key = "product:likeCount:" + productId;
    putToCache(key, count, LIKE_COUNT_TTL);
  }

  public Long addLikeCount(Long productId) {
    Long currentCount = getLikeCount(productId);
    Long newCount = currentCount == null ? 1 : currentCount + 1;
    putLikeCount(productId, newCount);
    return newCount;
  }

  public Long subtractLikeCount(Long productId) {
    Long currentCount = getLikeCount(productId);
    Long newCount = currentCount == null ? 0 : currentCount - 1;
    putLikeCount(productId, newCount);
    return newCount;
  }

  /**
   * LikeInfo 단일 메서드 통합
   */
  public LikeInfo getLikeInfo(Long userId, Long productId) {
    Boolean liked = false;
    if (userId != null) {
      getUserLiked(userId, productId);

      if (liked == null) {
        liked = likeService.isLiked(userId, productId); // DB 조회
        putUserLiked(userId, productId, liked);
      }
    }
    Long likeCount = getLikeCount(productId);
    if (likeCount == null) {
      likeCount = likeService.getLikeCount(productId); // DB 조회
      putLikeCount(productId, likeCount);
    }

    return LikeInfo.from(likeCount, liked);
  }

  /**
   * 공통 JSON 직렬화
   */
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

  public void evictUserLike(long userId, long productId) {
    redisTemplate.delete("product:liked:" + userId + ":" + productId);
  }

  public void evictLikeCount(long productId) {
    redisTemplate.delete("product:likeCount:" + productId);
  }
}

