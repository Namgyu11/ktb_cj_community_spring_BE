package com.ktb.ktb_cj_community_spring_be.comment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QComment is a Querydsl query type for Comment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QComment extends EntityPathBase<Comment> {

    private static final long serialVersionUID = -1231689598L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QComment comment = new QComment("comment");

    public final com.ktb.ktb_cj_community_spring_be.global.entity.QBaseEntity _super = new com.ktb.ktb_cj_community_spring_be.global.entity.QBaseEntity(this);

    public final ListPath<CommentLike, QCommentLike> commentLikes = this.<CommentLike, QCommentLike>createList("commentLikes", CommentLike.class, QCommentLike.class, PathInits.DIRECT2);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> likeCount = createNumber("likeCount", Integer.class);

    public final com.ktb.ktb_cj_community_spring_be.member.entity.QMember member;

    public final com.ktb.ktb_cj_community_spring_be.post.entity.QPost post;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QComment(String variable) {
        this(Comment.class, forVariable(variable), INITS);
    }

    public QComment(Path<? extends Comment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QComment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QComment(PathMetadata metadata, PathInits inits) {
        this(Comment.class, metadata, inits);
    }

    public QComment(Class<? extends Comment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.ktb.ktb_cj_community_spring_be.member.entity.QMember(forProperty("member")) : null;
        this.post = inits.isInitialized("post") ? new com.ktb.ktb_cj_community_spring_be.post.entity.QPost(forProperty("post"), inits.get("post")) : null;
    }

}

