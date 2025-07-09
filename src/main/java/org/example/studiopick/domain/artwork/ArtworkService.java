package org.example.studiopick.domain.artwork;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.common.dto.artwork.ArtworkFeedDto;
import org.example.studiopick.infrastructure.mybatis.ArtworkMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtworkService {

    private final ArtworkMapper artworkMapper;

    public List<ArtworkFeedDto> getArtworks(String sort, int offset, int limit, String hashtags) {
        return artworkMapper.findAllSorted(sort, offset, limit, hashtags);
    }
}
