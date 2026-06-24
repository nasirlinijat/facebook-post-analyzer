package com.intern.metaanalysis.mapper;

import com.intern.metaanalysis.dto.response.PostResponse;
import com.intern.metaanalysis.model.Post;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper from the domain/Meta {@link Post} model to the public {@link PostResponse}.
 *
 * <p>{@code totalLikes}, {@code totalComments} and {@code engagementScore} are derived from
 * the matching getters on {@link Post}, so MapStruct wires them automatically.
 */
@Mapper(componentModel = "spring")
public interface PostMapper {

    PostResponse toResponse(Post post);

    List<PostResponse> toResponseList(List<Post> posts);
}
