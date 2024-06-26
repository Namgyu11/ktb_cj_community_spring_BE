package com.ktb.ktb_cj_community_spring_be.global.util.aws.entity;

import com.ktb.ktb_cj_community_spring_be.global.entity.BaseEntity;
import com.ktb.ktb_cj_community_spring_be.post.entity.Post;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class PostImage extends BaseEntity {

      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;

      @Column(nullable = false)
      private String url;

      @Column(nullable = false)
      private String fileName;

      @Column
      private Long size;

      @ManyToOne
      @JoinColumn(name = "post_id")
      private Post post;

      public void mappingPost(Post post) {
            this.post = post;

      }

}
