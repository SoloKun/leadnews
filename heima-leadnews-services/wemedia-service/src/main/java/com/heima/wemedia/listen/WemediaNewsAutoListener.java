package com.heima.wemedia.listen;

import com.heima.model.constants.message.NewsAutoScanConstants;
import com.heima.wemedia.service.WmNewsService;
import com.heima.wemedia.service.impl.WmNewsAutoScanServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ClassName: WemediaNewsAutoListener
 * Package: com.heima.wemedia.listen
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/20 22:24
 * @Version 1.0
 */
@Component
@Slf4j
public class WemediaNewsAutoListener {
    @Autowired
    WmNewsAutoScanServiceImpl wmNewsAutoScanService;
    @Autowired
    WmNewsService wmNewsService;
    /**
     * queues: 监听指定队列
     * queuesToDeclare: 声明并监听指定队列
     * bindings: 声明队列  交换机  并通过路由绑定
     */
    @RabbitListener(queuesToDeclare = @Queue(NewsAutoScanConstants.WM_NEWS_AUTO_SCAN_QUEUE))
    public void handleAutoScan(String newsId){
        log.info("自动审核文章监听到消息：{}",newsId);
        wmNewsAutoScanService.autoScanByMediaNews(Integer.valueOf(newsId));
        log.info("自动审核文章完成：{}",newsId);
    }
}
