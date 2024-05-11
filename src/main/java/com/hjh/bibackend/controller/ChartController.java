package com.hjh.bibackend.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.hjh.bibackend.annotation.RoleCheck;
import com.hjh.bibackend.common.BaseResponse;
import com.hjh.bibackend.common.ErrorCode;
import com.hjh.bibackend.common.constant.UserConstant;
import com.hjh.bibackend.exception.BusinessException;
import com.hjh.bibackend.manager.AiManager;
import com.hjh.bibackend.manager.RedisLimiterManager;
import com.hjh.bibackend.model.domain.Chart;
import com.hjh.bibackend.model.dto.UserDto;
import com.hjh.bibackend.model.query.DeleteRequest;
import com.hjh.bibackend.model.query.chartQuery.*;
import com.hjh.bibackend.model.vo.BiResponse;
import com.hjh.bibackend.mq.BiMessageProvider;
import com.hjh.bibackend.service.ChartService;
import com.hjh.bibackend.service.UserService;
import com.hjh.bibackend.service.WebSocketService;
import com.hjh.bibackend.utils.ExcelUtils;
import com.hjh.bibackend.utils.ResponseUtils;
import com.hjh.bibackend.utils.ThrowUtils;
import com.hjh.bibackend.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hjh.bibackend.common.ErrorCode.NOT_FOUND;
import static com.hjh.bibackend.common.constant.BiConstant.BI_MODEL_ID;
import static com.hjh.bibackend.common.constant.RedisConstant.CACHE_CHARTS_USER;
import static com.hjh.bibackend.common.constant.RedisConstant.MUTEX_KEY;
import static com.hjh.bibackend.model.enums.StatusEnums.*;

@Slf4j
@RestController
@ResponseBody
@RequestMapping("/chart")
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private AiManager aiManager;

    @Resource
    private BiMessageProvider biMessageProvider;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private Gson gson;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private WebSocketService webSocketService;

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);

        UserDto loginUser = UserHolder.getUser();
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResponseUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @RoleCheck(mustRole = UserConstant.ADMIN)
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserDto loginUser = UserHolder.getUser();
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NULL_ERROR);
        // 仅本人或管理员可删除
        ThrowUtils.throwIf(!oldChart.getUserId().equals(loginUser.getId()) || !userService.isAdmin(loginUser),ErrorCode.NO_AUTH);
        boolean b = chartService.removeById(id);
        return ResponseUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @RoleCheck(mustRole = UserConstant.ADMIN)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);

        // 参数校验
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, NOT_FOUND);
        boolean result = chartService.updateById(chart);
        return ResponseUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<Chart> getChartById(@RequestParam long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(NOT_FOUND);
        }
        return ResponseUtils.success(chart);
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param chartQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @RoleCheck(mustRole = UserConstant.ADMIN)
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest) {
        long current = chartQueryRequest.getCurrentPage();
        long size = chartQueryRequest.getPageSize();
        Page<Chart> chartPage = chartService.page(new Page<>(current, size), chartService.getQueryWrapper(chartQueryRequest));
        return ResponseUtils.success(chartPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<Chart>> listChartByPageVo(@RequestBody ChartQueryRequest chartQueryRequest) {
        long current = chartQueryRequest.getCurrentPage();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),chartService.getQueryWrapper(chartQueryRequest));
        return ResponseUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserDto loginUser = UserHolder.getUser();
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrentPage();
        long size = chartQueryRequest.getPageSize();

        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        //todo:添加redis作为缓存，提高速度
        String key = CACHE_CHARTS_USER + chartQueryRequest.getUserId();
        String cacheChartList = stringRedisTemplate.opsForValue().get(key);

        if(!StringUtils.isBlank(cacheChartList)){
            try {
                List<Chart> chartList = gson.fromJson(cacheChartList,new TypeToken<List<Chart>>(){}.getType());
                Page<Chart> chartPage = new Page<>();
                if(StringUtils.isEmpty(chartQueryRequest.getName())){
                    //返回全部
                    chartPage.setRecords(chartList);
                    chartPage.setTotal(chartList.size());
                }else{
                    //返回指定的图表
                    List<Chart> myChartList = chartList.stream().filter((chart)->{
                        //用contains代替模糊查询
                        return chart.getName().contains(chartQueryRequest.getName());
                    }).collect(Collectors.toList());

                    chartPage.setRecords(myChartList);
                    chartPage.setTotal(myChartList.size());
                }

                return ResponseUtils.success(chartPage);

            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                //如果一旦发生错误，那么就使用数据库查询
                Page<Chart> chartPage = chartService.page(new Page<>(current, size), chartService.getQueryWrapper(chartQueryRequest));
                return ResponseUtils.success(chartPage);
            }
        }

        //防止缓存穿透,返回一个空值
        if(cacheChartList != null){
            return ResponseUtils.success(new Page<Chart>());
        }

        //防止缓存击穿，使用互斥锁让线程排队
        String lockKey = MUTEX_KEY + chartQueryRequest.getUserId();
        Page<Chart> chartPage = null;
        try {
            //尝试获得锁
            boolean b = tryLock(lockKey);
            //没有获得锁，重试
            if (!b){
                return listMyChartByPage(chartQueryRequest);
            }
            //否则，就直接去数据库查询
            chartPage = chartService.page(new Page<>(current, size), chartService.getQueryWrapper(chartQueryRequest));
            if(chartPage.getRecords().isEmpty()){
                //并在查询后，向缓存添加信息
                String myChartListJson = gson.toJson(chartPage.getRecords());
                //设置较短的TTL
                stringRedisTemplate.opsForValue().set(key,myChartListJson,5, TimeUnit.MINUTES);
                return ResponseUtils.success(chartPage);
            }
            //并在查询后，向缓存添加信息
            String myChartListJson = gson.toJson(chartPage.getRecords());
            //必须添加过期时间，因为Redis的内存并不能扩充
            stringRedisTemplate.opsForValue().set(key,myChartListJson,12, TimeUnit.HOURS);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            unlock(lockKey);
        }
        return ResponseUtils.success(chartPage);
    }

    /**
     * 获得锁
     * @param key
     * @return
     */
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(10));
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 释放锁
     * @param key
     */
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);

        UserDto loginUser = UserHolder.getUser();
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, NOT_FOUND);
        // 仅本人或管理员可编辑
        ThrowUtils.throwIf(!oldChart.getUserId().equals(loginUser.getId()) || !userService.isAdmin(loginUser),ErrorCode.NO_AUTH);
        boolean result = chartService.updateById(chart);
        return ResponseUtils.success(result);
    }

    /**
     * 智能分析（同步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        // 校验是否为空
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        //校验文件名称
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        // 校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        // 校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        UserDto loginUser = UserHolder.getUser();
        // 限流判断，每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());

        //校验是否已经生成20个表格了
        long count = chartService.count(new LambdaQueryWrapper<Chart>().eq(Chart::getUserId,loginUser.getId()));
        ThrowUtils.throwIf(count>=20,ErrorCode.PARAMS_ERROR,"最多生成20个图表");

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
        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        String result = aiManager.doChat(BI_MODEL_ID, userInput.toString());

        log.info("[AI返回结果]: {}",result);

        String[] splits = result.split("【【【【【");

        ThrowUtils.throwIf(splits.length < 3,new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误"));

        String genChart = splits[1].trim();
        String genResult = splits[2].trim();

        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        //刷新缓存:先更新数据库，再删除缓存
        chartService.deleteCacheChart(loginUser.getId());

        //返回结果
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());
        return ResponseUtils.success(biResponse);
    }

    @PostMapping("/gen/async/thread")
    public BaseResponse<BiResponse> genChartAsyncByThread(@RequestPart("file") MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest){
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        // 校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        // 校验文件后缀 aaa.png
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        UserDto loginUser = UserHolder.getUser();

        // 限流判断，每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());

        //校验是否已经生成20个表格了
        long count = chartService.count(new LambdaQueryWrapper<Chart>().eq(Chart::getUserId,loginUser.getId()));
        ThrowUtils.throwIf(count>=20,ErrorCode.PARAMS_ERROR,"最多生成20个图表");

        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);

        //构建输入
        String userInput = chartService.buildUserInput(goal,csvData,chartType);

        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setStatus(WAIT.getValue());
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        //通过websocket通知前端更新列表
        chartService.notifyLastChartList(chart.getUserId(),WAIT.getValue());

        // todo 建议处理任务队列满了后，抛异常的情况
        CompletableFuture.runAsync(() -> {
            // 先修改图表任务状态为 “执行中”。等执行成功后，修改为 “已完成”、保存执行结果；执行失败后，状态修改为 “失败”，记录任务失败信息。
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus(RUNNING.getValue());

            chartService.notifyLastChartList(chart.getUserId(),RUNNING.getValue());

            boolean b = chartService.updateById(updateChart);
            if (!b) {
                chartService.handleChartUpdateError(chart.getId(), "更新图表执行中状态失败");
                return;
            }



            // 调用 AI
            String result = aiManager.doChat(BI_MODEL_ID, userInput.toString());
            String[] splits = result.split("【【【【【");
            if (splits.length < 3) {
                chartService.handleChartUpdateError(chart.getId(), "AI 生成错误");
                return;
            }

            String genChart = splits[1].trim();
            String genResult = splits[2].trim();
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(genChart);
            updateChartResult.setGenResult(genResult);
            updateChartResult.setStatus(SUCCESS.getValue());

            boolean updateResult = chartService.updateById(updateChartResult);
            if (!updateResult) {
                chartService.handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
            }

            //todo:测试用的，上线记得删除
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            chartService.notifyLastChartList(chart.getUserId(),SUCCESS.getValue());

        }, threadPoolExecutor);

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return ResponseUtils.success(biResponse);
    }


    @PostMapping("/gen/async/mq")
    public BaseResponse<BiResponse> genChartAsyncByAi(@RequestPart("file") MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest){
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        //校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        // 校验是否为空
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        //校验文件名称
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        // 校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        // 校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx","xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        UserDto loginUser = UserHolder.getUser();

        // 限流判断，每个用户一个限流器
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());

        //校验是否已经生成20个表格了
        long count = chartService.count(new LambdaQueryWrapper<Chart>().eq(Chart::getUserId,loginUser.getId()));
        ThrowUtils.throwIf(count>=20,ErrorCode.PARAMS_ERROR,"最多生成20个图表");

        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);

        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setStatus(WAIT.getValue());
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        //todo:测试用的，上线记得删除
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //发送消息
        biMessageProvider.sendMessage(String.valueOf(chart.getId()));

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return ResponseUtils.success(biResponse);
    }
}
