package org.example.studiopick.infrastructure.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.studiopick.common.dto.artwork.ArtworkFeedDto;
import org.example.studiopick.domain.artwork.Artwork;

import java.util.List;

@Mapper
public interface ArtworkMapper {
    List<ArtworkFeedDto> findAllSorted(@Param("sort") String sort,
                                       @Param("offset") int offset,
                                       @Param("limit") int limit,
                                       @Param("hashtags") String hashtags);
}
