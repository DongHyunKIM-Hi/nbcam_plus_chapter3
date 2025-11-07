package org.example.plus.domain.post.service;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.plus.common.entity.Post;
import org.example.plus.common.entity.User;
import org.example.plus.domain.post.model.dto.PostDto;
import org.example.plus.domain.post.model.dto.PostSummaryDto;
import org.example.plus.domain.post.model.request.UpdatePostRequest;
import org.example.plus.domain.post.repository.PostRepository;
import org.example.plus.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostCacheService postCacheService;
    private final UserRepository userRepository;

    public PostDto creatPost(String username, String content) {

        User user = userRepository.findUserByUsername(username).orElseThrow(
            ()-> new IllegalArgumentException("등록된 사용자가 없습니다.")
        );

        Post post = postRepository.save(new Post(content, user.getId()));

        return PostDto.from(post);
    }

    public PostDto getPost(long postId) {

        // 1단계 : 캐시가 있나요?

        PostDto cached = postCacheService.getPostCache(postId);
        if (cached != null) {
            log.info(" Redis Cache HIT ! ");
            postCacheService.increaseViewCount(postId);
            return cached;
        }

        // 2단계 : 캐시가 없을 경우 직접 조회

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post가 없습니다."));


        PostDto postDto = PostDto.from(post);

        // 3단계 : DB에서 가져온 값을 캐시에 저장 -> 다음번에 활용하기 위해서 + 조회수 반영
        postCacheService.savePostCache(postId, postDto);
        postCacheService.increaseViewCount(postId);

        return postDto;
    }

    public PostDto updatePost(long postId, UpdatePostRequest request) {

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post가 없습니다."));

        post.update(request);
        postRepository.save(post);

        // 캐시 삭제 (무효화)
        postCacheService.deletePostCache(postId);

        return PostDto.from(post);

    }

    public List<PostDto> getTopPosts(int limit) {

        List<Long> topPostIdList = postCacheService.getTopPosts(limit);
        List<PostDto> result = new ArrayList<>();


        if (topPostIdList.isEmpty()) {
            return Collections.emptyList();
        }

        for (Long postId : topPostIdList) {
            PostDto postDto = postCacheService.getPostCache(postId);

            if (postDto == null) {
                postDto = PostDto.from(postRepository.findById(postId)
                    .orElseThrow(()-> new IllegalArgumentException("Post가 없습니다.")));
            }
            result.add(postDto);
        }
        return result;
    }


}


