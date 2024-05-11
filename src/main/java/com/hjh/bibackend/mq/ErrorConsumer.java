package com.hjh.bibackend.mq;

import com.hjh.bibackend.config.RabbitmqConfig;
import com.hjh.bibackend.model.domain.Chart;
import com.hjh.bibackend.service.ChartService;
import com.hjh.bibackend.service.WebSocketService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@Component
public class ErrorConsumer {

    @Resource
    private ChartService chartService;

    @SneakyThrows
    @RabbitListener(queues = {RabbitmqConfig.QUEUE_BI_DLX_QUEUE})
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        log.info("receiveMessage message = {}", message);

        long chartId = Long.parseLong(message);

        chartService.handleChartUpdateError(chartId,"系统内部异常");

        channel.basicNack(deliveryTag,false,false);
    }

//    /**
//     * 错误处理
//     * @param chartId
//     * @param execMessage
//     */
//    private void handleChartUpdateError(long chartId, String execMessage) {
//        Chart updateChartResult = new Chart();
//        updateChartResult.setId(chartId);
//        updateChartResult.setStatus("failed");
//        updateChartResult.setExecMessage("execMessage");
//
//        boolean updateResult = chartService.updateById(updateChartResult);
//        if (!updateResult) {
//            log.error("更新图表失败状态失败" + chartId + "," + execMessage);
//        }
//
//        try {
//            webSocketService.sendMessage(String.valueOf(chartId),"ok");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        //刷新缓存
//        Chart newChart = chartService.getById(chartId);
//        if(newChart==null)return;
//        chartService.deleteCacheChart(newChart.getUserId());
//    }
}
