package com.heima.wemedia.service.impl;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.message.NewsAutoScanConstants;
import com.heima.common.constants.message.NewsUpOrDownConstants;
import com.heima.common.constants.message.PublishArticleConstants;
import com.heima.common.constants.wemedia.WemediaConstants;
import com.heima.common.exception.CustException;
import com.heima.common.exception.CustomException;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.threadlocal.WmThreadLocalUtils;
import com.heima.model.wemedia.dtos.NewsAuthDTO;
import com.heima.model.wemedia.dtos.WmNewsDTO;
import com.heima.model.wemedia.dtos.WmNewsPageReqDTO;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.model.wemedia.vos.WmNewsVO;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {
    @Value("${file.oss.web-site}")
    String webSite;

    @Override
    public ResponseResult downOrUp(WmNewsDTO dto) {
        //1.检查参数
        if(dto == null || dto.getId() == null){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID);
        }
        Short enable = dto.getEnable();
        if(enable == null ||
                (!WemediaConstants.WM_NEWS_UP.equals(enable)&&!WemediaConstants.WM_NEWS_DOWN.equals(enable))){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"上下架状态错误");
        }
        //2.查询文章
        WmNews wmNews = getById(dto.getId());
        if(wmNews == null){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
        }
        //3.判断文章是否发布
        if(!wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"当前文章不是发布状态，不能上下架");
        }
        //4.修改文章状态，同步到app端（后期做）TODO
        update(Wrappers.<WmNews>lambdaUpdate().eq(WmNews::getId,dto.getId())
            .set(WmNews::getEnable,dto.getEnable()));

        //5. 上下架发送消息通知  用于同步article 及 elasticsearch
        if(enable.equals(WemediaConstants.WM_NEWS_UP)&&wmNews.getArticleId()!=null){
            // 上架消息
            rabbitTemplate.convertAndSend(NewsUpOrDownConstants.NEWS_UP_OR_DOWN_EXCHANGE,
                    NewsUpOrDownConstants.NEWS_UP_ROUTE_KEY,wmNews.getArticleId());
        }else {
            // 下架消息
            rabbitTemplate.convertAndSend(NewsUpOrDownConstants.NEWS_UP_OR_DOWN_EXCHANGE,
                    NewsUpOrDownConstants.NEWS_DOWN_ROUTE_KEY,wmNews.getArticleId());
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }


    @Override
    public ResponseResult delNews(Integer id) {
        //1.检查参数
        if(id == null){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"文章Id不可缺少");
        }
        //2.获取数据
        WmNews wmNews = getById(id);
        if(wmNews == null){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
        }
        //3.判断当前文章的状态  status==9  enable == 1
        if(wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())
                && wmNews.getEnable().equals(WemediaConstants.WM_NEWS_UP)){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"文章已发布，不能删除");
        }
        //4.去除素材与文章的关系
        wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getNewsId,wmNews.getId()));
        //5.删除文章
        removeById(wmNews.getId());
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult findWmNewsById(Integer id) {
        //1 参数检查
        if (id == null) {
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2 执行查询
        WmNews wmNews = getById(id);
        if (wmNews == null) {
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        //3 返回结果
        ResponseResult result = ResponseResult.okResult(wmNews);
        result.setHost(webSite);
        return result;
    }

    /**
     * 查询所有自媒体文章
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findList(WmNewsPageReqDTO dto) {
        //1 参数检查
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        dto.checkParam();
        //2 条件封装执行查询
        LambdaQueryWrapper<WmNews> wrapper = new LambdaQueryWrapper<>();
        // 文章标题模糊查询
        wrapper.like(StringUtils.isNotBlank(dto.getKeyword()),WmNews::getTitle, dto.getKeyword());
        // 频道id
        wrapper.eq(dto.getChannelId() != null,WmNews::getChannelId, dto.getChannelId());
        // 文章状态
        wrapper.eq(dto.getStatus() != null,WmNews::getStatus, dto.getStatus());
        // 发布时间 >= 开始时间
        wrapper.ge(dto.getBeginPubDate() != null,WmNews::getPublishTime, dto.getBeginPubDate());
        // 发布时间 <= 开始时间
        wrapper.le(dto.getEndPubDate()!=null,WmNews::getPublishTime, dto.getBeginPubDate());
        // 当前自媒体人文章
        WmUser user = WmThreadLocalUtils.getUser();
        if (user == null) {
            CustException.cust(AppHttpCodeEnum.NEED_LOGIN);
        }
        wrapper.eq(WmNews::getUserId, user.getId());
        // 按照创建日期倒序
        wrapper.orderByDesc(WmNews::getCreatedTime);
        // 分页条件构建
        Page<WmNews> page = new Page<>(dto.getPage(), dto.getSize());
        //3 执行查询
        IPage<WmNews> pageResult = page(page, wrapper);
        //4 返回封装查询结果
        PageResponseResult result = new PageResponseResult(dto.getPage(), dto.getSize(), pageResult.getTotal());
        result.setData(pageResult.getRecords());
        // 处理文章图片 
        result.setHost(webSite);
       return result;
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 自媒体文章发布
     * @param dto
     * @return
     */
    @Override
    public ResponseResult submitNews(WmNewsDTO dto) {
        // 1 参数校验
        if (dto == null || StringUtils.isBlank(dto.getContent())) {
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID);
        }
        // 2 保存或修改文章
        WmNews wmNews = new WmNews();
        // 将dto参数里面的值设置到wmNews
        BeanUtils.copyProperties(dto, wmNews);
        //如果文章布局是自动，需要设置为null
        if (dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)) {
            wmNews.setType(null);
        }
        // 处理dto参数 images封面集合 转换成 字符串
        String images = imageListToStr(dto.getImages(), webSite);
        wmNews.setImages(images);
        saveWmNews(wmNews);
        //3.1 抽取文章中关联的图片路径
        List<Map> contents = JSON.parseArray(dto.getContent(), Map.class);
        List<String> materials = parseContentImages(contents);
        //3.2 关联文章内容中的图片和素材关系
        Short status = dto.getStatus();
        if (status.equals(WmNews.Status.SUBMIT.getCode())){
            if(CollectionUtils.isNotEmpty(materials)){
                saveRelativeInfo(materials, wmNews.getId(),WemediaConstants.WM_CONTENT_REFERENCE);
            }
            // 3.3 关联文章封面中的图片和素材关系  封面可能是选择自动或者是无图
            saveRelativeInfoForCover(dto,materials, wmNews);
            // 3.4 发送审核消息
            rabbitTemplate.convertAndSend(NewsAutoScanConstants.WM_NEWS_AUTO_SCAN_TOPIC,wmNews.getId());
            log.info("成功发送 待审核消息 ==> 队列:{}, 文章id:{}",NewsAutoScanConstants.WM_NEWS_AUTO_SCAN_TOPIC,wmNews.getId());
        }
        return ResponseResult.okResult();
    }
    /**
     * 【3.3】 关联文章封面中的图片和素材关系
     * @param dto  前端用户选择封面信息数据
     * @param materials  从内容中解析的图片列表
     * @param wmNews     文章ID
     */
    private void saveRelativeInfoForCover(WmNewsDTO dto, List<String> materials, WmNews wmNews) {
        // 前端用户选择的图
        List<String> images = dto.getImages();

        // 自动获取封面 ****
        if (WemediaConstants.WM_NEWS_TYPE_AUTO.equals(dto.getType())) {
            int materialSize = materials.size();
            if (materialSize > 0 && materialSize <= 2) {  // 单图
                images =  materials.stream().limit(1).collect(Collectors.toList());
                wmNews.setType(WemediaConstants.WM_NEWS_SINGLE_IMAGE);
            } else if (materialSize > 2) { // 多图
                images =  materials.stream().limit(3).collect(Collectors.toList());
                wmNews.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
            } else {  // 无图
                wmNews.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
            }

            if (images != null && images.size() > 0) {
                // 将图片集合 转为字符串  url1,url2,url3
                wmNews.setImages(imageListToStr(images,webSite));
            }
            updateById(wmNews);
        }
        // 保存图片列表和素材的关系
        if (images != null && images.size() > 0) {
            images = images.stream().map(x->x.replace(webSite,"")
                    .replace(" ","")).collect(Collectors.toList());
            saveRelativeInfo(images, wmNews.getId(), WemediaConstants.WM_IMAGE_REFERENCE);
        }
    }
    @Autowired
    WmMaterialMapper wmMaterialMapper;
    /**
     * 保存素材和文章关系
     * @param materials  素材列表
     * @param newsId     文章ID
     * @param type       类型 0：内容素材  1：封面素材
     */
    private void saveRelativeInfo(List<String> materials, Integer newsId, Short type) {
        //1 查询文章内容中的图片对应的素材ID
        List<WmMaterial> dbmaterials = wmMaterialMapper.selectList(
                Wrappers.<WmMaterial>lambdaQuery().in(WmMaterial::getUrl,materials)
                        .eq(WmMaterial::getUserId, WmThreadLocalUtils.getUser().getId()));
        //2 校验素材列表
        if (dbmaterials == null || dbmaterials.size() == 0) {
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        List<Integer> materialIds = new ArrayList<>();
        // {"1.jpg":55,"2.jpg":56}  草稿中的图片列表大于等于 数据库的素材
        Map<String, Integer> matreialMap = dbmaterials.stream()
                .collect(Collectors.toMap(WmMaterial::getUrl, WmMaterial::getId));
        for (String material : materials) {
            Integer id = matreialMap.get(material);
            if (id == null) { // 数据库中已删除改图片
                CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST);
            }
            materialIds.add(id);
        }
        //3 保存素材关系
        wmNewsMaterialMapper.saveRelations(materialIds,newsId,type);
    }


    @Autowired
    WmNewsMaterialMapper wmNewsMaterialMapper;
    /**
     * 保存或修改文章
     * @param wmNews 文章对象（前端传递）
     */
    private void saveWmNews(WmNews wmNews) {
        wmNews.setCreatedTime(new Date());
        wmNews.setUserId(WmThreadLocalUtils.getUser().getId());
        wmNews.setSubmitedTime(new Date());
        wmNews.setEnable(WemediaConstants.WM_NEWS_UP); // 上架
        if (wmNews.getId()==null) { // 保存操作
            save(wmNews);
        }else {  // 修改
            //文章状态为9 且 已经上架  无法修改
            WmNews oldNews = getById(wmNews.getId());
            if(oldNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())
                    && oldNews.getEnable().equals(WemediaConstants.WM_NEWS_UP)){
                CustException.cust(AppHttpCodeEnum.DATA_NOT_ALLOW,"已经发布上架的文章无法修改，请先下架");
            }
            // 当前文章 和 素材关系表数据删除
            wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery()
                    .eq(WmNewsMaterial::getNewsId, wmNews.getId()));
            updateById(wmNews);
        }
    }
    /**
     * 抽取文章内容中 所引用的所有图片
     * @param contents 文章内容
     * @return
     */
    private List<String> parseContentImages(List<Map> contents) {
        // 遍历文章内容   将所有 type为image的 value获取出来  去除前缀路径
        return contents.stream()
                // 过滤type=image所有的集合
                .filter( map -> map.get("type").equals(WemediaConstants.WM_NEWS_TYPE_IMAGE))
                // 获取到image下的value  图片url
                .map(x -> (String)x.get("value"))
                // 图片url去除前缀
                .map(url-> url.replace(webSite,"").replace(" ",""))
                // sretam 转成list集合
                .collect(Collectors.toList());
    }


    /**
     * 图片列表转字符串，并去除图片前缀
     * @param images 图片列表
     * @param webSite 图片前缀
     */
    private String imageListToStr(List<String> images, String webSite) {
        return images.stream()  // 获取流
                .map((url) -> url.replace(webSite, ""))  // 对流数据的中间操作
                .collect(Collectors.joining(","));
    }


    @Autowired
    WmNewsMapper wmNewsMapper;
    /**
     * 查询文章列表
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findList(NewsAuthDTO dto) {
        //1.检查参数
        dto.checkParam();
        //记录当前页
        int currentPage = dto.getPage();
        //设置起始页
        dto.setPage((dto.getPage()-1)*dto.getSize());
        if(StringUtils.isNotBlank(dto.getTitle())){
            dto.setTitle("%"+dto.getTitle()+"%");
        }

        //2.分页查询
        List<WmNewsVO> wmNewsVoList = wmNewsMapper.findListAndPage(dto);
        //统计多少条数据
        long count = wmNewsMapper.findListCount(dto);

        //3.结果返回
        ResponseResult result = new PageResponseResult(currentPage, dto.getSize(), count, wmNewsVoList);
        result.setHost(webSite);
        return result;
    }

    @Autowired
    private WmUserMapper wmUserMapper;

    /**
     * 查询文章详情
     * @param id
     * @return
     */
    @Override
    public ResponseResult findWmNewsVo(Integer id) {
        //1参数检查
        if(id == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.查询文章信息
        WmNews wmNews = getById(id);
        if(wmNews == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        //3.查询作者
        WmUser wmUser = null;
        if(wmNews.getUserId() != null){
            wmUser = wmUserMapper.selectById(wmNews.getUserId());
        }

        //4.封装vo信息返回
        WmNewsVO wmNewsVo = new WmNewsVO();
        BeanUtils.copyProperties(wmNews,wmNewsVo);
        if(wmUser != null){
            wmNewsVo.setAuthorName(wmUser.getName());
        }
        ResponseResult responseResult = ResponseResult.okResult(wmNewsVo);
        responseResult.setHost(webSite);
        return responseResult;
    }

    /**
     * 自媒体文章人工审核
     * @param status 2  审核失败  4 审核成功
     * @param dto
     * @return
     */
    @Override
    public ResponseResult updateStatus(Short status, NewsAuthDTO dto) {
        //1.参数检查
        if(dto == null || dto.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.查询文章
        WmNews wmNews = getById(dto.getId());
        if(wmNews == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        // 检查文章状态 不能为9  已发布
        if (wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())) {
            CustException.cust(AppHttpCodeEnum.DATA_NOT_ALLOW,"文章已发布");
        }
        //3.修改文章状态
        wmNews.setStatus(status);
        if(StringUtils.isNotBlank(dto.getMsg())){
            wmNews.setReason(dto.getMsg());
        }
        updateById(wmNews);
        // 通知定时发布文章
        if(status.equals(WmNews.Status.ADMIN_SUCCESS.getCode())){
            //5. 通知定时发布文章
            // 发布时间
            long publishTime = wmNews.getPublishTime().getTime();
            // 当前时间
            long nowTime = new Date().getTime();
            long remainTime = publishTime - nowTime;
            if(remainTime > 0){
                // 立即发布
                rabbitTemplate.convertAndSend(PublishArticleConstants.PUBLISH_ARTICLE_EXCHANGE
                        ,PublishArticleConstants.PUBLISH_ARTICLE_ROUTE_KEY
                        ,wmNews.getId()
                );
                log.info("立即发布文章通知成功发送，文章id : {}",wmNews.getId());
            }else {
                // 延时发布
                rabbitTemplate.convertAndSend(PublishArticleConstants.PUBLISH_ARTICLE_EXCHANGE
                        , PublishArticleConstants.PUBLISH_ARTICLE_DEAD_ROUTE_KEY
                        , wmNews.getId()
                        , (message)-> {
                            message.getMessageProperties().setExpiration(String.valueOf(remainTime));
                            return message;
                        }
                );
                log.info("延时发布文章通知成功发送，文章id : {} , 延时时间: {}毫秒",wmNews.getId(),remainTime);
            }
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

}