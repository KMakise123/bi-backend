package com.hjh.bibackend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hjh.bibackend.common.ErrorCode;
import com.hjh.bibackend.exception.BusinessException;
import com.hjh.bibackend.model.domain.Chart;
import com.hjh.bibackend.model.query.chartQuery.ChartQueryRequest;
import com.hjh.bibackend.utils.ThrowUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hjh.bibackend.common.constant.RedisConstant.CACHE_CHARTS_USER;

/**
* @author HIKI
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2024-04-25 14:56:51
*/
public interface ChartService extends IService<Chart> {
    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    Wrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);

    /**
     * 构建用户输入
     *
     * @param chart 图表对象
     * @return 用户输入字符串
     */
    String buildUserInput(Chart chart);

    /**
     * 构建用户输入
     *
     * @param goal
     * @param csvData
     * @param chartType
     * @return
     */
    String buildUserInput(String goal, String csvData, String chartType);

    /**
     * 校验文件是否合规
     *
     * @param multipartFile
     */
    void validFile(MultipartFile multipartFile);

    /**
     * 更新缓存
     * @param userId
     */
    public void deleteCacheChart(Long userId);

    /**
     * 错误处理
     * @param chartId
     * @param execMessage
     */
    public void handleChartUpdateError(long chartId, String execMessage);

    /**
     * 通知前端更新
     * @param userId
     * @param execMsg
     */
    public void notifyLastChartList(Long userId,String execMsg);
}
