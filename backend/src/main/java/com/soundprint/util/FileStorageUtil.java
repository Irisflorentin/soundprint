package com.soundprint.util;

import com.soundprint.config.StorageProperties;
import com.soundprint.config.UploadProperties;
import com.soundprint.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

/**
 * 文件存储工具
 *
 * 核心约定：数据库只存「相对路径」（如 audio/uuid.flac），
 * 运行时用 baseDir 拼成绝对路径。这样换部署机器 / 迁移存储目录，
 * 数据库一行都不用改。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileStorageUtil {

    private final StorageProperties storageProps;
    private final UploadProperties uploadProps;

    /**
     * 保存上传文件到指定子目录，返回相对路径。
     *
     * @param file   上传的文件
     * @param subDir 子目录名（如 "audio"、"cover"）
     * @return 相对路径，如 "audio/3f2a....flac"
     */
    public String store(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);
        String relative = subDir + "/" + filename;
        File dest = new File(storageProps.getBaseDir(), relative);
        ensureParent(dest);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException("文件保存失败: " + e.getMessage());
        }
        log.debug("已保存上传文件: {}", relative);
        return relative;
    }

    /**
     * 保存字节数组（如从音频里抠出来的封面图），返回相对路径。
     */
    public String storeBytes(byte[] data, String subDir, String ext) {
        if (data == null || data.length == 0) {
            throw new BusinessException("写入内容为空");
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        String relative = subDir + "/" + filename;
        File dest = new File(storageProps.getBaseDir(), relative);
        ensureParent(dest);
        try {
            Files.write(dest.toPath(), data);
        } catch (IOException e) {
            throw new BusinessException("文件保存失败: " + e.getMessage());
        }
        return relative;
    }

    /**
     * 把一个已存在的文件复制到指定子目录，返回相对路径。
     * 用于转换骨架：把源音频"复制"为转换结果（Phase 5 才换成真正的 FFmpeg 转码）。
     */
    public String copyFile(File source, String subDir, String ext) {
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        String relative = subDir + "/" + filename;
        File dest = new File(storageProps.getBaseDir(), relative);
        ensureParent(dest);
        try {
            Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("文件复制失败: " + e.getMessage());
        }
        return relative;
    }

    /** 相对路径 → 绝对路径 */
    public String getAbsolutePath(String relativePath) {
        return new File(storageProps.getBaseDir(), relativePath).getAbsolutePath();
    }

    /** 相对路径 → File 对象 */
    public File getFile(String relativePath) {
        return new File(storageProps.getBaseDir(), relativePath);
    }

    /** 删除文件（按相对路径），不存在则忽略 */
    public void delete(String relativePath) {
        if (relativePath == null) return;
        try {
            Files.deleteIfExists(getFile(relativePath).toPath());
        } catch (IOException e) {
            log.warn("删除文件失败: {}, err={}", relativePath, e.getMessage());
        }
    }

    /** 取扩展名（小写，不含点）。无扩展名返回空串 */
    public String getExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /** 是否允许的音频格式 */
    public boolean isAllowedAudioFormat(String filename) {
        String ext = getExtension(filename);
        return uploadProps.getAllowedAudioFormats() != null
                && uploadProps.getAllowedAudioFormats().contains(ext);
    }

    private void ensureParent(File dest) {
        File parent = dest.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new BusinessException("无法创建存储目录: " + parent.getAbsolutePath());
        }
    }
}
