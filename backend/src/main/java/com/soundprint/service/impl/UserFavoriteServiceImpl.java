package com.soundprint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soundprint.common.PageResult;
import com.soundprint.dto.response.TrackResponse;
import com.soundprint.entity.UserFavorite;
import com.soundprint.mapper.TrackMapper;
import com.soundprint.mapper.UserFavoriteMapper;
import com.soundprint.service.UserFavoriteService;
import com.soundprint.util.CurrentUserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 用户收藏表 服务实现类
 * </p>
 *
 * 注意：user_favorite 是复合主键 (user_id, track_id)，
 * 不能用 getById/updateById/removeById，一律用 Wrapper 按两列定位。
 *
 * @author Soundprint
 * @since 2026-05-25
 */
@Service
@RequiredArgsConstructor
public class UserFavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite> implements UserFavoriteService {

    private final TrackMapper trackMapper;
    private final CurrentUserUtil currentUserUtil;

    @Override
    public void addFavorite(Long trackId) {
        Long uid = currentUserUtil.getCurrentUserId();
        if (checkFavorite(trackId)) {
            return; // 幂等
        }
        UserFavorite uf = new UserFavorite();
        uf.setUserId(uid);
        uf.setTrackId(trackId);
        uf.setFavoritedAt(LocalDateTime.now());
        baseMapper.insert(uf);
    }

    @Override
    public void removeFavorite(Long trackId) {
        baseMapper.delete(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, currentUserUtil.getCurrentUserId())
                .eq(UserFavorite::getTrackId, trackId));
    }

    @Override
    public PageResult<TrackResponse> listFavorites(Long page, Long size) {
        Page<TrackResponse> p = new Page<>(page, size);
        IPage<TrackResponse> result = trackMapper.pageByFavorite(p, currentUserUtil.getCurrentUserId());
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public boolean checkFavorite(Long trackId) {
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, currentUserUtil.getCurrentUserId())
                .eq(UserFavorite::getTrackId, trackId));
        return count != null && count > 0;
    }
}
