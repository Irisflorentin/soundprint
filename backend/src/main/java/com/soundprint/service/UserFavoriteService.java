package com.soundprint.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soundprint.common.PageResult;
import com.soundprint.dto.response.TrackResponse;
import com.soundprint.entity.UserFavorite;

/**
 * <p>
 * 用户收藏表 服务类
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
public interface UserFavoriteService extends IService<UserFavorite> {

    /** 收藏（幂等：已收藏则不重复） */
    void addFavorite(Long trackId);

    /** 取消收藏 */
    void removeFavorite(Long trackId);

    /** 我的收藏（分页） */
    PageResult<TrackResponse> listFavorites(Long page, Long size);

    /** 是否已收藏 */
    boolean checkFavorite(Long trackId);
}
