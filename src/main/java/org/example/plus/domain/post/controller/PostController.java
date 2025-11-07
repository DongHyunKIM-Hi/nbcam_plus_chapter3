package org.example.plus.domain.post.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.plus.domain.post.model.dto.PostDto;
import org.example.plus.domain.post.model.dto.PostSummaryDto;
import org.example.plus.domain.post.model.request.CreatePostRequest;
import org.example.plus.domain.post.model.request.UpdatePostRequest;
import org.example.plus.domain.post.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostDto> createPost(@AuthenticationPrincipal User user, @RequestBody CreatePostRequest request) {
        return ResponseEntity.ok(postService.creatPost(user.getUsername(), request.getContent()));
    }


    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPostById(@PathVariable long postId) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostDto> updatePost(@PathVariable long postId, @RequestBody UpdatePostRequest request) {
        return ResponseEntity.ok(postService.updatePost(postId, request));
    }

    // 인기 게시글 조회
    @GetMapping("/popular")
    public ResponseEntity<List<PostDto>> getPopularPosts(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(postService.getTopPosts(limit));
    }

}
