package org.example.plus.domain.post.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.example.plus.domain.post.model.dto.PostDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CACHE_PREFIX = "post:";
    private static final String RANKING_KEY = "ranking:posts"; // 인기글 랭킹용 키


    // 캐시에 저장
    public void savePostCache(Long postId, PostDto postData) {
        String key = CACHE_PREFIX + postId;
        redisTemplate.opsForValue().set(key, postData, 10, TimeUnit.MINUTES); // TTL 10분
    }

    // 캐시에서 조회
    public PostDto getPostCache(Long postId) {
        String key = CACHE_PREFIX + postId;
        return (PostDto) redisTemplate.opsForValue().get(key);
    }

    // 캐시에서 삭제
    public void deletePostCache(Long postId) {
        String key = CACHE_PREFIX + postId;
        redisTemplate.delete(key);
    }

    // 조회수 증가 (ZSet 사용)
    public void increaseViewCount(Long postId) {
        redisTemplate.opsForZSet().incrementScore(RANKING_KEY, postId.toString(), 1);
    }

    // 인기 게시글 조회 상위 N개
    public List<Long> getTopPosts(int limit) {
        Set<Object> postIds = redisTemplate.opsForZSet().reverseRange(RANKING_KEY, 0, limit - 1);

        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyList();
        }

        return postIds.stream()
            .map(id -> Long.parseLong(id.toString()))
            .toList();
    }

}
