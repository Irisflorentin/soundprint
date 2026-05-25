package com.soundprint.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.soundprint.common.PageResult;
import com.soundprint.dto.request.conversion.ConversionSubmitRequest;
import com.soundprint.dto.response.ConversionTaskResponse;
import com.soundprint.entity.ConversionTask;

import java.io.File;

/**
 * <p>
 * 格式转换任务表 服务类
 * </p>
 *
 * @author Soundprint
 * @since 2026-05-25
 */
public interface ConversionTaskService extends IService<ConversionTask> {

    /** 提交转换任务（Phase 3 用后台线程模拟进度，Phase 5 换成真 FFmpeg） */
    ConversionTaskResponse submit(ConversionSubmitRequest request);

    /** 查询任务状态/进度 */
    ConversionTaskResponse getTask(Long id);

    /** 我的转换历史（分页） */
    PageResult<ConversionTaskResponse> pageQuery(Long page, Long size);

    /** 取转换结果文件（仅 SUCCESS 可下载） */
    File getOutputFile(Long id);
}
