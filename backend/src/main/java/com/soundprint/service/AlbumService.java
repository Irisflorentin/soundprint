package com.soundprint.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soundprint.common.PageResult;
import com.soundprint.dto.request.album.AlbumCreateRequest;
import com.soundprint.dto.request.album.AlbumQueryRequest;
import com.soundprint.dto.request.album.AlbumUpdateRequest;
import com.soundprint.dto.response.AlbumDetailResponse;
import com.soundprint.dto.response.AlbumResponse;
import com.soundprint.entity.Album;

/**
 * <p>
 * 专辑表 服务类
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
public interface AlbumService extends IService<Album> {

    PageResult<AlbumResponse> pageQuery(AlbumQueryRequest query);

    AlbumDetailResponse getDetail(Long id);

    AlbumResponse create(AlbumCreateRequest request);

    AlbumResponse updateAlbum(Long id, AlbumUpdateRequest request);

    void deleteAlbum(Long id);
}
