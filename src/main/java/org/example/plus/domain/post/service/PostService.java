package org.example.plus.domain.post.service;

import jakarta.transaction.Transactional;
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
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostDto creatPost(String username, String content) {

        User user = userRepository.findUserByUsername(username).orElseThrow(
            ()-> new IllegalArgumentException("등록된 사용자가 없습니다.")
        );

        Post post = postRepository.save(new Post(content, user.getId()));

        return PostDto.from(post);

    }


    public List<PostSummaryDto> getPostSummaryListByUsername(String username) {

        List<PostSummaryDto> result = postRepository.findPostSummary(username);
        return result;
    }

    @Cacheable(value = "postCache", key = "#postId")
    public PostDto getPostById(long postId) {

        log.info("캐시에 없으니 DB에서 직접 조회");

        Post post = postRepository.findById(postId)
            .orElseThrow(()-> new IllegalArgumentException("등록된 포스트가 없습니다."));

        return PostDto.from(post);
    }

    @CachePut(value = "postCache", key = "#postId")
    public PostDto updatePostById(long postId, UpdatePostRequest request) {

        log.info("캐시의 값을 없데이트 해줍니다.");

        Post post = postRepository.findById(postId)
            .orElseThrow(()-> new IllegalArgumentException("등록된 포스트가 없습니다."));

        post.updateContent(request);

        return PostDto.from(post);
    }
}


