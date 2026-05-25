package com.soundprint.service.impl;

import com.soundprint.entity.TrackTag;
import com.soundprint.mapper.TrackTagMapper;
import com.soundprint.service.TrackTagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 曲目-标签关联表 服务实现类
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
@Service
public class TrackTagServiceImpl extends ServiceImpl<TrackTagMapper, TrackTag> implements TrackTagService {

}
