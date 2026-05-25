package com.soundprint.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soundprint.common.PageResult;
import com.soundprint.dto.request.artist.ArtistCreateRequest;
import com.soundprint.dto.request.artist.ArtistQueryRequest;
import com.soundprint.dto.request.artist.ArtistUpdateRequest;
import com.soundprint.dto.response.ArtistDetailResponse;
import com.soundprint.dto.response.ArtistResponse;
import com.soundprint.entity.Artist;

/**
 * <p>
 * 艺术家表 服务类
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
public interface ArtistService extends IService<Artist> {

    PageResult<ArtistResponse> pageQuery(ArtistQueryRequest query);

    ArtistDetailResponse getDetail(Long id);

    ArtistResponse create(ArtistCreateRequest request);

    ArtistResponse updateArtist(Long id, ArtistUpdateRequest request);

    void deleteArtist(Long id);
}
