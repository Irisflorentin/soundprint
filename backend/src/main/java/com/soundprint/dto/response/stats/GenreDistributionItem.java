package com.soundprint.dto.response.stats;

import lombok.Data;

/**
 * 流派分布项
 */
@Data
public class GenreDistributionItem {
    private String genre;
    private Long count;
    /** 占比（百分数，保留 1 位小数），由 Service 计算填充 */
    private Double percentage;
}
