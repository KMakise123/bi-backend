package com.hjh.bibackend.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.hjh.bibackend.common.ErrorCode;
import com.hjh.bibackend.exception.BusinessException;
import com.hjh.bibackend.mapper.ChartMapper;
import com.hjh.bibackend.model.domain.Chart;
import com.hjh.bibackend.model.query.chartQuery.ChartQueryRequest;
import com.hjh.bibackend.service.ChartService;
import com.hjh.bibackend.service.UserService;
import com.hjh.bibackend.service.WebSocketService;
import com.hjh.bibackend.utils.ThrowUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.hjh.bibackend.common.constant.RedisConstant.CACHE_CHARTS_USER;
import static com.hjh.bibackend.model.enums.StatusEnums.FAILED;

/**
* @author HIKI
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2024-04-25 14:56:51
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart> implements ChartService{

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private Gson gson;

    @Resource
    private WebSocketService webSocketService;




    private static final long ONE_MB = 1024 * 1024L;

    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    @Override
    public Wrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        LambdaQueryWrapper<Chart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (chartQueryRequest == null) {
            return lambdaQueryWrapper;
        }

        Long id = chartQueryRequest.getId();
        String goal = chartQueryRequest.getGoal();
        String name = chartQueryRequest.getName();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();

        lambdaQueryWrapper.eq(id != null && id > 0, Chart::getId, id);
        lambdaQueryWrapper.like(StringUtils.isNotBlank(goal), Chart::getGoal, goal);
        lambdaQueryWrapper.like(StringUtils.isNotBlank(name),Chart::getName,name);
        lambdaQueryWrapper.eq(StringUtils.isNotBlank(chartType), Chart::getChartType, chartType);
        lambdaQueryWrapper.eq(userId != null && userId > 0, Chart::getUserId, userId);

        return lambdaQueryWrapper;
    }



    /**
     * 构建用户输入
     *
     * @param chart 图表对象
     * @return 用户输入字符串
     */
    @Override
    public String buildUserInput(Chart chart) {
        // 获取图表的目标、类型和数据
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String csvData = chart.getChartData();

        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        userInput.append(csvData).append("\n");
        // 将StringBuilder转换为String并返回
        return userInput.toString();
    }

    /**
     * 构建用户输入
     *
     * @param goal
     * @param csvData
     * @param chartType
     * @return
     */
    @Override
    public String buildUserInput(String goal, String csvData, String chartType) {

        // 实现分析
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        // 压缩后的数据（把multipartFile传进来，其他的东西先注释）
        userInput.append(csvData).append("\n");

        return userInput.toString();
    }

    /**
     * 校验文件是否合规
     * @param multipartFile
     */
    @Override
    public void validFile(MultipartFile multipartFile) {
        // 校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();

        // 文件大小,防止有人上传个100G的文件，把流量耗完
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件内容过大");
        // 后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        List<String> validFileSuffix = Arrays.asList("xlsx", "xls", "csv");
        ThrowUtils.throwIf(!validFileSuffix.contains(suffix), ErrorCode.PARAMS_ERROR, "文件格式非法");
    }

    /**
     * 删除缓存
     * @param userId
     */
    @Transactional(rollbackFor=Exception.class)
    @Override
    public void deleteCacheChart(Long userId){
        //刷新缓存：先更新数据库，再删除缓存
        String key = CACHE_CHARTS_USER + userId;
        if(stringRedisTemplate.hasKey(key)) {
            boolean deleteRes = stringRedisTemplate.delete(key);
            ThrowUtils.throwIf(!deleteRes, new BusinessException(ErrorCode.SYSTEM_ERROR, "删除缓存失败"));
        }
    }

    /**
     * 错误处理
     * @param chartId
     * @param execMessage
     */
    public void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus("failed");
        updateChartResult.setExecMessage("execMessage");

        boolean updateResult = updateById(updateChartResult);
        if (!updateResult) {
            log.error("更新图表失败状态失败" + chartId + "," + execMessage);
        }

        //刷新缓存
        Chart newChart = getById(chartId);
        if(newChart==null)return;
        deleteCacheChart(newChart.getUserId());

        try {
            webSocketService.sendMessage(String.valueOf(chartId),FAILED.getValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通知前端更新
     * @param userId
     * @param execMsg
     */
    @Override
    public void notifyLastChartList(Long userId,String execMsg){
        //刷新缓存：先更新数据库，再删除缓存
        //删除缓存
        deleteCacheChart(userId);

        //通过websocket通知前端更新列表
        try {
            webSocketService.sendMessage(userId.toString(),execMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}




