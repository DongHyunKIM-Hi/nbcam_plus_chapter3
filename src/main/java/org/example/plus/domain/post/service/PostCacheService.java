package org.example.plus.domain.post.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CACHE_PREFIX = "post:";

    // 캐시에 저장
    public void savePostCache(Long postId, Object postData) {
        String key = CACHE_PREFIX + postId;
        redisTemplate.opsForValue().set(key, postData, 10, TimeUnit.MINUTES); // TTL 10분
    }

    // 캐시에서 조회
    public Object getPostCache(Long postId) {
        String key = CACHE_PREFIX + postId;
        return redisTemplate.opsForValue().get(key);
    }

    // 캐시에서 삭제
    public void deletePostCache(Long postId) {
        String key = CACHE_PREFIX + postId;
        redisTemplate.delete(key);
    }

}
