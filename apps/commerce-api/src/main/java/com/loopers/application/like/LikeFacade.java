package com.loopers.application.like;

import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LikeFacade {
  private final LikeService likeService;              // DB 기반
  private final LikeCacheRepository likeCacheRepository;

  /**
   * 좋아요
   */
  public LikeInfo like(Long userId, Long productId) {
    likeCacheRepository.putUserLiked(userId, productId, true);
    Long cachedCount = likeCacheRepository.getLikeCount(productId);
    cachedCount = cachedCount == null ? 0 : cachedCount;
    likeService.save(userId, productId);
    return LikeInfo.from(cachedCount, true);
  }

  /**
   * 좋아요 취소
   */
  public LikeInfo unlike(Long userId, Long productId) {
    likeCacheRepository.evictUserLike(userId, productId);
    Long cachedCount = likeCacheRepository.subtractLikeCount(productId);
    cachedCount = cachedCount == null ? 0 : cachedCount;
    likeService.remove(userId, productId);

    return LikeInfo.from(cachedCount, false);
  }

  /**
   * 사용자별 & 상품별 좋아요 조회
   */
  public LikeInfo getLikeInfo(Long userId, Long productId) {
    Boolean liked = likeCacheRepository.getUserLiked(userId, productId);
    if (liked == null) {
      liked = likeService.isLiked(userId, productId);
      likeCacheRepository.putUserLiked(userId, productId, liked);
    }

    Long likeCount = likeCacheRepository.getLikeCount(productId);
    if (likeCount == null) {
      likeCount = likeService.getLikeCount(productId);
      likeCacheRepository.putLikeCount(productId, likeCount);
    }

    return LikeInfo.from(likeCount, liked);
  }
}
