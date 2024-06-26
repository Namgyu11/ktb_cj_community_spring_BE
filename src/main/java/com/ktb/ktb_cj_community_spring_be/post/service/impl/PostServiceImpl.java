package com.ktb.ktb_cj_community_spring_be.post.service.impl;

import static com.ktb.ktb_cj_community_spring_be.global.exception.type.ErrorCode.POST_NOT_FOUND;
import static com.ktb.ktb_cj_community_spring_be.global.exception.type.ErrorCode.USER_NOT_FOUND;
import static com.ktb.ktb_cj_community_spring_be.global.exception.type.ErrorCode.WRITE_NOT_YOURSELF;

import com.ktb.ktb_cj_community_spring_be.comment.repository.CommentRepository;
import com.ktb.ktb_cj_community_spring_be.global.exception.GlobalException;
import com.ktb.ktb_cj_community_spring_be.global.service.RedisService;
import com.ktb.ktb_cj_community_spring_be.global.util.aws.dto.S3ImageDto;
import com.ktb.ktb_cj_community_spring_be.global.util.aws.entity.PostImage;
import com.ktb.ktb_cj_community_spring_be.global.util.aws.service.AwsS3Service;
import com.ktb.ktb_cj_community_spring_be.member.entity.Member;
import com.ktb.ktb_cj_community_spring_be.member.repository.MemberRepository;
import com.ktb.ktb_cj_community_spring_be.post.dto.PostRequest;
import com.ktb.ktb_cj_community_spring_be.post.dto.PostResponse;
import com.ktb.ktb_cj_community_spring_be.post.entity.Post;
import com.ktb.ktb_cj_community_spring_be.post.repository.PostRepository;
import com.ktb.ktb_cj_community_spring_be.post.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

      private final PostRepository postRepository;
      private final MemberRepository memberRepository;
      private final AwsS3Service awsS3Service;
      private static final String VIEW_HASH_KEY = "post::views";
      private final RedisService redisService;
      private final CommentRepository commentRepository;


      /**
       * 게시물을 생성하고 그에 대한 정보를 반환
       *
       * @param request 생성할 게시물 정보
       * @param images  업로드할 이미지 파일들
       * @param email   게시물을 생성하는 회원의 이메일
       * @return 생성된 게시물의 정보를 포함한 PostResponse 객체
       */

      @Override
      public PostResponse createPost(PostRequest request, List<MultipartFile> images,
              String email) {

            Member member = getMember(email);
            Post post = request.toEntity();

            if (images != null && !images.isEmpty()) {
                  Post saveImagePost = uploadS3Image(request, images);
                  saveImagePost.getImages().forEach(post::addImage);
            }
            member.addPost(post);

            Post saved = postRepository.save(post);

            return PostResponse.fromEntity(saved);
      }

      /**
       * 게시물을 조회하고 그에 대한 정보를 반환
       *
       * @param postId 조회할 게시물의 id
       * @return 조회된 게시물의 정보를 포함한 PostResponse 객체
       */
      @Override
      public PostResponse readPost(Long postId, HttpServletRequest request) {
            Post post = getPost(postId);

            HttpSession session = request.getSession();

            // 클라이언트의 세션에서 중복 조회 여부 확인
            Boolean hasRead = (Boolean) session.getAttribute("readPost:" + postId);

            if (hasRead == null || !hasRead) {

                  //중복 조회 여부를 세션에 저장
                  session.setAttribute("readPost:" + postId, true);

                  //조회수 증가
                  redisService.increaseHashData(VIEW_HASH_KEY, postId.toString());
            } else {
                  log.info(" 중복 요청 발생 Redis == readPost ");
            }

            return PostResponse.fromEntity(post);
      }

      /**
       * 게시물을 수정하고 그에 대한 정보를 반환
       *
       * @param id               게시물의 id
       * @param request          게시물 수정 정보
       * @param newImages        새로 추가할 이미지 파일들
       * @param imageIdsToDelete 삭제할 이미지 id 목록
       * @param email            게시물을 수정하는 회원의 이메일
       * @return 수정된 게시물의 정보를 포함한 PostResponse 객체
       */

      @Override
      public PostResponse updatePost(Long id, PostRequest request, List<MultipartFile> newImages,
              List<Long> imageIdsToDelete, String email) {
            Post post = getPost(id);
            Member member = getMember(email);

            validationPost(post, member);

            // PostRequest 로부터 값을 받아오 Post 객체의 상태 변경
            request.updatePostEntity(post);

            // 새로운 이미지가 제공될 경우 해당 이미지 추가
            if (newImages != null && !newImages.isEmpty()) {
                  Post updatePost = uploadS3Image(request, newImages);
                  updatePost.getImages().forEach(post::addImage);
            }

            // 삭제할 이미지 id가 제공될 경우 해당 이미지 삭제(특정 이미지만 삭제)
            if (imageIdsToDelete != null && !imageIdsToDelete.isEmpty()) {
                  List<PostImage> imagesToDelete = post.getImages().stream()
                          .filter(image -> imageIdsToDelete.contains(image.getId())).toList();
                  imagesToDelete.forEach(image -> {
                        log.info("delete image: {}", image.getFileName());
                        awsS3Service.deleteFile(image.getFileName());
                        post.removeImage(image);
                  });
            }

            return PostResponse.fromEntity(post);
      }

      @Override
      public void deletePost(Long id, String email) {
            Post post = getPost(id);
            Member member = getMember(email);

            validationPost(post, member);
            List<PostImage> images = post.getImages();
            images.forEach(image -> awsS3Service.deleteFile(image.getFileName()));

            member.removePost(post);
            postRepository.delete(post);
      }

      @Override
      public Post uploadS3Image(PostRequest request, List<MultipartFile> multipartFiles) {
            Post post = request.toEntity();

            List<S3ImageDto> list = multipartFiles.stream().map(awsS3Service::uploadImage).toList();
            List<PostImage> imagesList = list.stream().map(S3ImageDto::toEntity).toList();
            imagesList.forEach(post::addImage);

            return post;
      }

      /**
       * 페이징 처리된 게시물 리스트 조회
       *
       * @param pageable 페이지 정보
       * @return 조회된 게시물 리스트
       */
      @Override
      public Page<PostResponse> postList(Pageable pageable) {
            return postRepository.findAll(pageable).map(PostResponse::listFromEntity);
      }

      /**
       * 무한 스크롤을 위한 게시물 리스트 조회
       *
       * @param pageable 페이지 정보
       * @return 조회된 게시물 리스트
       */
      @Override
      public Slice<PostResponse> postListScroll(Pageable pageable) {
            return postRepository.findAll(pageable).map(PostResponse::listFromEntity);
      }


      private Member getMember(String email) {
            return memberRepository.findByEmail(email)
                    .orElseThrow(() -> new GlobalException(USER_NOT_FOUND));
      }

      private Post getPost(Long postId) {
            return postRepository.findById(postId)
                    .orElseThrow(() -> new GlobalException(POST_NOT_FOUND));
      }

      private void validationPost(Post post, Member member) {
            if (!post.getMember().getEmail().equals(member.getEmail())) {
                  throw new GlobalException(WRITE_NOT_YOURSELF);
            }
      }

      /**
       * Redis 에 저장된 조회수를 DB에 업데이트
       */
      @Scheduled(cron = "${spring.scheduler.refresh-time}")
      public void updateViewCountToDB() {
            Map<Object, Object> map = redisService.hasHashKeys(VIEW_HASH_KEY);

            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                  Long postId = Long.parseLong(entry.getKey().toString());
                  int views = Integer.parseInt(entry.getValue().toString());

                  postRepository.updateViews(postId, views);

                  redisService.deleteHashKey(VIEW_HASH_KEY, postId.toString());
            }
      }
}
