package com.hjh.bibackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.gson.Gson;
import com.hjh.bibackend.model.domain.Chart;
import com.hjh.bibackend.model.domain.User;
import com.hjh.bibackend.service.CacheService;
import com.hjh.bibackend.service.ChartService;
import com.hjh.bibackend.service.UserService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hjh.bibackend.common.constant.RedisConstant.CACHE_CHARTS_USER;

@Slf4j
@Service
public class CacheServiceImpl implements CacheService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserService userService;

    @Resource
    private ChartService chartService;

    @Resource
    private Gson gson;

    /**
     * 定时更新缓存数据，时间为一天更新一次,每天早上1点更新
     */
    @SneakyThrows
    @Scheduled(cron ="0 0 1 * * ?")
    @Override
    public void updateCache() {
        log.info("Update Redis Data : {}",new Date());

        //把当天的登录的用户的chart全部放到redis中
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date endTime = sdf.parse(sdf.format(new Date()));

        List<User> users = userService.list();
        users.stream().filter(user->{
            Date loginTime = user.getLoginTime();
            long diff = endTime.getTime()-loginTime.getTime();
            return diff < 86400000L;
        }).forEach(user -> {
            LambdaQueryWrapper<Chart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(Chart::getUserId,user.getId());
            List<Chart> myCharts = chartService.list(lambdaQueryWrapper);
            if(myCharts==null||myCharts.isEmpty()) myCharts = Collections.EMPTY_LIST;
            String myChartsJson = gson.toJson(myCharts);
            stringRedisTemplate.opsForValue().set(CACHE_CHARTS_USER+user.getId(),myChartsJson,1 ,TimeUnit.DAYS);
        });
    }
}
