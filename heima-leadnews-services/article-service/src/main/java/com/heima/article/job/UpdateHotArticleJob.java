package com.heima.article.job;

import com.alibaba.fastjson.JSON;
import com.heima.article.service.HotArticleService;
import com.heima.common.exception.CustException;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.constants.article.HotArticleConstants;
import com.heima.model.mess.app.AggBehaviorDTO;
import com.heima.model.mess.app.NewBehaviorDTO;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import javax.json.Json;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UpdateHotArticleJob {
    @Autowired
    private HotArticleService hotArticleService;
    @XxlJob("updateHotArticleJob")
    public ReturnT updateHotArticleHandler(String params){
        log.info("热文章分值更新 调度任务开始执行....");
        List<NewBehaviorDTO> newBehaviorList = getRedisBehaviorList();
        if(newBehaviorList.isEmpty()){
           log.info("redis中没有行为数据");
            return ReturnT.SUCCESS;
        }
        List<AggBehaviorDTO> aggBehaviorDTO = getAggBehaviorList(newBehaviorList);
        log.info("aggBehaviorDTO:{}",aggBehaviorDTO);
        for(AggBehaviorDTO aggBehavior:aggBehaviorDTO){
            hotArticleService.updateApArticle(aggBehavior);
        }

        log.info("热文章分值更新 调度任务完成....");
        return ReturnT.SUCCESS;
    }

    private List<AggBehaviorDTO> getAggBehaviorList(List<NewBehaviorDTO> newBehaviorList) {
        List<AggBehaviorDTO> aggBehaviorList = new ArrayList<>();
        Map<Long,List<NewBehaviorDTO>>map = newBehaviorList.stream()
                .collect(Collectors.groupingBy(NewBehaviorDTO::getArticleId));
        map.forEach((articleId,messList)->{
            //Optional是一个容器类，代表一个值存在或不存在，原来用null表示一个值不存在，现在Optional可以更好的表达这个概念。
            Optional<AggBehaviorDTO> reduceResult = messList.stream()
                    .map(behavior->{
                        AggBehaviorDTO aggBehaviorDTO = new AggBehaviorDTO();
                        aggBehaviorDTO.setArticleId(behavior.getArticleId());
                        switch (behavior.getType()){
                            case COLLECTION:
                                aggBehaviorDTO.setCollect(behavior.getAdd());
                                break;
                            case COMMENT:
                                aggBehaviorDTO.setComment(behavior.getAdd());
                                break;
                            case LIKES:
                                aggBehaviorDTO.setLike(behavior.getAdd());
                                break;
                            case VIEWS:
                                aggBehaviorDTO.setView(behavior.getAdd());
                                break;
                            default:
                                break;
                        }
                        return aggBehaviorDTO;
                    }).reduce((a1,b1)->{
                        a1.setCollect(a1.getCollect()+b1.getCollect());
                        a1.setComment(a1.getComment()+b1.getComment());
                        a1.setLike(a1.getLike()+b1.getLike());
                        a1.setView(a1.getView()+b1.getView());
                        return a1;
                    });
                if(reduceResult.isPresent()){
                    AggBehaviorDTO aggBehaviorDTO = reduceResult.get();
                    aggBehaviorList.add(aggBehaviorDTO);
                }

            });
        return aggBehaviorList;
    }

    @Autowired
    StringRedisTemplate redisTemplate;
    private List<NewBehaviorDTO> getRedisBehaviorList() {
        try{
            DefaultRedisScript<List> script = new DefaultRedisScript<>();
            script.setResultType(List.class);
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/redis.lua")));
            List<String> BehaviorList = redisTemplate.execute(script,
                    Arrays.asList(HotArticleConstants.HOT_ARTICLE_SCORE_BEHAVIOR_LIST));
            return BehaviorList.stream()
                    .map(jsonStr-> JSON.parseObject(jsonStr,NewBehaviorDTO.class))
                    .collect(Collectors.toList());
        }catch (Exception e){
            e.printStackTrace();
            CustException.cust(AppHttpCodeEnum.REMOTE_SERVER_ERROR,"lua脚本获取redis行为数据失败");
        }
        return null;
    }
}