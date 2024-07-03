package com.ktb.ktb_cj_community_spring_be.post.entity;

import com.ktb.ktb_cj_community_spring_be.comment.entity.Comment;
import com.ktb.ktb_cj_community_spring_be.global.entity.BaseEntity;
import com.ktb.ktb_cj_community_spring_be.global.util.aws.entity.PostImage;
import com.ktb.ktb_cj_community_spring_be.member.entity.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {

      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;

      @ManyToOne(fetch = FetchType.LAZY)
      @JoinColumn(name = "member_id")
      private Member member;

      @Column(nullable = false)
      private String title;

      @Column(nullable = false)
      private String content;

      @Column(nullable = false)
      private int hits;

      @Column(nullable = false)
      private int likeCount;

      @Builder.Default
      @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
      private List<Comment> comments = new ArrayList<>();

      @Builder.Default
      @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
      private List<PostLike> postLikes = new ArrayList<>();

      @Builder.Default
      @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
      private List<PostImage> images = new ArrayList<>();

      public void addImage(PostImage image) {
            this.images.add(image);
            image.mappingPost(this);
      }

      public void removeImage(PostImage image) {
            this.images.remove(image);
      }

      public void addPostLike(PostLike postLike) {
            this.postLikes.add(postLike);
      }

      public void removePostLike(PostLike postLike) {
            this.postLikes.remove(postLike);
      }

      public void updateLikeCount() {
            this.likeCount = this.postLikes.size();
      }

      public void addComment(Comment comment) {
            this.comments.add(comment);
      }
}
