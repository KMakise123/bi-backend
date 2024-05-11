package com.hjh.bibackend.mq;

import com.hjh.bibackend.common.ErrorCode;
import com.hjh.bibackend.config.RabbitmqConfig;
import com.hjh.bibackend.exception.BusinessException;
import com.hjh.bibackend.manager.AiManager;
import com.hjh.bibackend.model.domain.Chart;
import com.hjh.bibackend.model.enums.StatusEnums;
import com.hjh.bibackend.service.ChartService;
import com.hjh.bibackend.service.WebSocketService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.hjh.bibackend.common.ErrorCode.NOT_FOUND;
import static com.hjh.bibackend.common.ErrorCode.SYSTEM_ERROR;
import static com.hjh.bibackend.common.constant.BiConstant.BI_MODEL_ID;
import static com.hjh.bibackend.model.enums.StatusEnums.RUNNING;
import static com.hjh.bibackend.model.enums.StatusEnums.SUCCESS;

@Slf4j
@Component
public class BiMessageConsumer {
    @Resource
    private ChartService chartService;

    @Resource
    private AiManager aiManager;

    @SneakyThrows
    @RabbitListener(queues = {RabbitmqConfig.QUEUE_BI})
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message = {}", message);
        try {
            if (StringUtils.isBlank(message)) {
                // 如果失败，消息拒绝,投入死信队列
                throw new BusinessException(SYSTEM_ERROR, "消息为空");
            }
            long chartId = Long.parseLong(message);
            Chart chart = chartService.getById(chartId);
            if (chart == null) {
                // 如果失败，消息拒绝,投入死信队列
                throw new BusinessException(NOT_FOUND, "图表为空");
            }
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus(RUNNING.getValue());
            boolean b = chartService.updateById(updateChart);
            if (!b) {
                // 如果失败，消息拒绝,投入死信队列
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新图表执行中状态失败");
            }

            //通知前端
            chartService.notifyLastChartList(chart.getUserId(),RUNNING.getValue());

            String result = aiManager.doChat(BI_MODEL_ID,chartService.buildUserInput(chart));

            log.info("[AI返回结果]: {}",result);

            String[] splits = result.split("【【【【【");

            if(splits.length < 3){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
            }

            String genChart = splits[1].trim();
            String genResult = splits[2].trim();

            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(genChart);
            updateChartResult.setGenResult(genResult);
            updateChartResult.setStatus(StatusEnums.SUCCESS.getValue());
            boolean updateResult = chartService.updateById(updateChartResult);
            if (!updateResult) {
                throw new BusinessException(SYSTEM_ERROR,"更新图表成功状态失败");
            }
            // 消息确认
            channel.basicAck(deliveryTag, false);
            log.info("图表{}成功生成",chartId);

            //通知前端
            chartService.notifyLastChartList(chart.getUserId(),SUCCESS.getValue());
        } catch (Exception e) {
            //发送到死信队列
            channel.basicNack(deliveryTag, false, false);
            //打印错误信息
            e.printStackTrace();
        }
    }

//    /**
//     * 构建用户输入
//     * @param chart
//     * @return
//     */
//    private String buildUserInput(Chart chart) {
//        String goal = chart.getGoal();
//        String chartType = chart.getChartType();
//        String csvData = chart.getChartData();
//
//        // 构造用户输入
//        StringBuilder userInput = new StringBuilder();
//        userInput.append("分析需求：").append("\n");
//
//        // 拼接分析目标
//        String userGoal = goal;
//        if (StringUtils.isNotBlank(chartType)) {
//            userGoal += "，请使用" + chartType;
//        }
//        userInput.append(userGoal).append("\n");
//        userInput.append("原始数据：").append("\n");
//        // 压缩后的数据
//        userInput.append(csvData).append("\n");
//
//        return userInput.toString();
//    }

}
