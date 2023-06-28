package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.exception.CustException;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.constants.message.NewsAutoScanConstants;
import com.heima.model.constants.message.NewsUpOrDownConstants;
import com.heima.model.constants.message.PublishArticleConstants;
import com.heima.model.constants.wemedia.WemediaConstants;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ClassName: WmNewsServiceImpl
 * Package: com.heima.wemedia.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/18 23:32
 * @Version 1.0
 */
@Service
@Slf4j
public class WmNewsServiceImpl  extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {
    @Value("${file.oss.web-site}")
    private String webSite;

    /**
     * 分页带条件查询自媒体文章列表
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findList(WmNewsPageReqDTO dto) {
        if (dto == null) {
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID, "参数异常");
        }
        dto.checkParam();
        LambdaQueryWrapper<WmNews> wrapper = new LambdaQueryWrapper<>();
        //模糊查询
        if (StringUtils.isNotEmpty(dto.getKeyword())) {
            wrapper.like(WmNews::getTitle, dto.getKeyword());
        }
        //文章id查询
        if (dto.getChannelId() != null) {
            wrapper.eq(WmNews::getChannelId, dto.getChannelId());
        }
        //文章状态
        if (dto.getStatus() != null) {
            wrapper.eq(WmNews::getStatus, dto.getStatus());
        }
        //时间范围查询
        //发布时间<=开始时间
        if (dto.getBeginPubDate() != null) {
            wrapper.le(WmNews::getPublishTime, dto.getBeginPubDate());
        }
        //发布时间>=结束时间
        if (dto.getEndPubDate() != null) {
            wrapper.ge(WmNews::getPublishTime, dto.getEndPubDate());
        }
        WmUser user = WmThreadLocalUtils.getUser();
        if (user == null) {
            CustException.cust(AppHttpCodeEnum.NEED_LOGIN, "需要登录");
        }
        wrapper.eq(WmNews::getUserId, user.getId());
        wrapper.orderByDesc(WmNews::getCreatedTime);

        Page<WmNews> page = new Page<>(dto.getPage(), dto.getSize());

        IPage<WmNews> pageResult = page(page, wrapper);

        PageResponseResult result = new PageResponseResult(dto.getPage(), dto.getSize(), pageResult.getTotal());
        result.setData(pageResult.getRecords());
        // 处理文章图片
        result.setHost(webSite);

        return result;
    }
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult submitNews(WmNewsDTO dto) {
        WmUser user = WmThreadLocalUtils.getUser();
        if (user == null) {
            CustException.cust(AppHttpCodeEnum.NEED_LOGIN, "需要登录");
        }
        if (StringUtils.isBlank(dto.getContent())) {
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID, "参数异常");
        }
        WmNews wmNews = new WmNews();
        BeanUtils.copyProperties(dto, wmNews);

        if (dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)) {
            wmNews.setType(null);
        }
        if(dto.getImages()!=null){
            String images = imageListToStr(dto.getImages());
            wmNews.setImages(images);
        }
        // String images = imageListToStr(dto.getImages());
        // wmNews.setImages(images);

        saveWmNews(wmNews);
        if(WemediaConstants.WM_NEWS_DRAFT_STATUS.equals(wmNews.getStatus())){
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }
        // TODO 3.1 抽取文章中关联的图片路径
        List<String> contentImages = parseContentImages(dto.getContent());
        // TODO 3.2 关联文章内容中的图片和素材关系
        if(!CollectionUtils.isEmpty(contentImages)) {
            saveRelativeInfo(contentImages,wmNews.getId(),WemediaConstants.WM_CONTENT_REFERENCE);
        }
        // TODO 3.3 关联文章封面中的图片和素材关系  封面可能是选择自动或者是无图
        saveRelativeInfoForCover(dto,contentImages, wmNews);
        // TODO 3.4 发送消息给文章审核微服务

        rabbitTemplate.convertAndSend(NewsAutoScanConstants.WM_NEWS_AUTO_SCAN_QUEUE,wmNews.getId());
        log.info("发送消息给文章审核微服务，文章id:{}",wmNews.getId());

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);

    }

    @Override
    public ResponseResult findWmNewsById(Integer id) {
        if(id==null){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"参数异常");
        }
        WmNews wmNews = getById(id);
        if(wmNews==null){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
        }
        ResponseResult responseResult = ResponseResult.okResult(wmNews);
        responseResult.setHost(webSite);
        return responseResult;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult delNews(Integer id) {
        if(id==null){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"参数异常");
        }
        WmNews wmNews = getById(id);
        if(wmNews==null){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
        }
        if(!wmNews.getUserId().equals(WmThreadLocalUtils.getUser().getId())){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_ALLOW,"无权限操作");
        }


        //已发布且已上架的文章不能删除
        if((wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())
                &&wmNews.getStatus().equals(WemediaConstants.WM_NEWS_UP))) {
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"文章已发布，不能删除");
        }
        //去除关联关系
        wmNewsMaterialMapper.delete(new LambdaQueryWrapper<WmNewsMaterial>().eq(WmNewsMaterial::getNewsId,wmNews.getId()));
        //删除文章
        removeById(wmNews.getId());
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult downOrUp(WmNewsDTO dto) {
        if (dto == null || dto.getId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        Short status = dto.getEnable();
        if (!(WemediaConstants.WM_NEWS_UP.equals(status)
                || WemediaConstants.WM_NEWS_DOWN.equals(status))) {
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID, "参数错误");
        }
        WmNews wmNews = getById(dto.getId());
        if(wmNews==null){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
        }
        if(!wmNews.getUserId().equals(WmThreadLocalUtils.getUser().getId())){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_ALLOW,"无权限操作");
        }
        if(!wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"文章未发布，不能上架或下架");
        }

        //TODO 同步状态到app端
        update(Wrappers.<WmNews>lambdaUpdate().eq(WmNews::getId,dto.getId())
                .set(WmNews::getEnable,dto.getEnable()));
        //这里和updateById的区别是，updateById不会更新null值，而这里会更新null值
        //通知Article微服务 和ES微服务
        if(dto.getEnable().equals(WemediaConstants.WM_NEWS_UP)){
            rabbitTemplate.convertAndSend(NewsUpOrDownConstants.NEWS_UP_OR_DOWN_EXCHANGE,
                    NewsUpOrDownConstants.NEWS_UP_ROUTE_KEY,wmNews.getArticleId());
        }else{
            rabbitTemplate.convertAndSend(NewsUpOrDownConstants.NEWS_UP_OR_DOWN_EXCHANGE,
                    NewsUpOrDownConstants.NEWS_DOWN_ROUTE_KEY,wmNews.getArticleId());
        }


        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Autowired
    private WmNewsMapper wmNewsMapper;
    @Override
    public ResponseResult findList(NewsAuthDTO dto) {
        dto.checkParam();
        int currentPage = dto.getPage();

        dto.setPage((currentPage-1)*dto.getSize());
        if(StringUtils.isNotBlank(dto.getTitle())){
            dto.setTitle("%"+dto.getTitle()+"%");
        }
        //分页
        List<WmNewsVO> wmNewsVos = wmNewsMapper.findListAndPage(dto);
        long total = wmNewsMapper.findListCount(dto);

        ResponseResult responseResult = new PageResponseResult(currentPage,
                dto.getSize(),total,wmNewsVos);
        responseResult.setHost(webSite);
        return responseResult;


    }
    @Autowired
    private WmUserMapper wmUserMapper;
    @Override
    public ResponseResult findWmNewsVo(Integer id) {
        if(id==null){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"参数异常");
        }
        WmNews wmNews = getById(id);
        if(wmNews==null){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
        }
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());

        WmNewsVO wmNewsVO = new WmNewsVO();
        BeanUtils.copyProperties(wmNews,wmNewsVO);
        if(wmUser!=null){
            wmNewsVO.setAuthorName(wmUser.getName());
        }
        ResponseResult responseResult = ResponseResult.okResult(wmNewsVO);
        responseResult.setHost(webSite);
        return responseResult;
    }

    @Override
    public ResponseResult updateStatus(Short status, NewsAuthDTO dto) {
        if(status==null||dto==null||dto.getId()==null){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"参数异常");
        }
        WmNews wmNews = getById(dto.getId());
        if(wmNews==null){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
        }
        if(wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"文章已发布，不能修改状态");
        }
        wmNews.setStatus(status);
        if(dto.getMsg()!=null){
            wmNews.setReason(dto.getMsg());
        }
        updateById(wmNews);
        // 通知定时发送文章
        if(wmNews.getStatus().equals(WmNews.Status.ADMIN_SUCCESS.getCode())){
            long delay = wmNews.getPublishTime().getTime()-System.currentTimeMillis();
            rabbitTemplate.convertAndSend(
                    PublishArticleConstants.DELAY_DIRECT_EXCHANGE,
                    PublishArticleConstants.PUBLISH_ARTICLE_ROUTE_KEY,
                    wmNews.getId(),
                    new MessagePostProcessor() {
                        @Override
                        public Message postProcessMessage(Message message) throws AmqpException {
                            message.getMessageProperties().setHeader("x-delay",delay<=0?0:delay);
                            return message;
                        }
                    }
            );
            log.info("通知定时发送文章：{}",wmNews.getId());
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);

    }



    /**
     * 保存封面和文章的关系
     * @param dto
     * @param materials
     * @param wmNews
     */
    private void saveRelativeInfoForCover(WmNewsDTO dto, List<String> materials, WmNews wmNews){
        List<String> images = dto.getImages();
        if(WemediaConstants.WM_NEWS_TYPE_AUTO.equals(dto.getType())) {
            int materialsSize = materials.size();
            if(materialsSize>0&&materialsSize<=2){//单图
                images = materials.stream().limit(1).collect(Collectors.toList());
                wmNews.setType(WemediaConstants.WM_NEWS_SINGLE_IMAGE);
            }else if(materialsSize>2){//多图
                images = materials.stream().limit(3).collect(Collectors.toList());
                wmNews.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
            }else{//无图
                wmNews.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
            }
            if(images!=null&&images.size()>0){
                wmNews.setImages(imageListToStr(images));
            }
            updateById(wmNews);
        }
        if(images!=null&&images.size()>0){
            images=images.stream().map(x ->x.replace(webSite,"")
                    .replace(" ",""))
                    .collect(Collectors.toList());
            saveRelativeInfo(images,wmNews.getId(),WemediaConstants.WM_IMAGE_REFERENCE);
            }
        }



    /**
     * 保存素材和文章的关系
     * @param contentImages
     * @param id
     * @param wmContentReference
     */
    @Autowired
    private WmMaterialMapper wmMaterialMapper;
    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;
    private void saveRelativeInfo(List<String> urls, Integer newsId, Short type) {
        List<Integer>ids = wmMaterialMapper.selectRelationsIds(urls,WmThreadLocalUtils.getUser().getId());
        if(CollectionUtils.isEmpty(ids)||ids.size()!=urls.size()){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"素材不存在");
        }
        wmNewsMaterialMapper.saveRelations(ids,newsId,type);
    }

    /**
     * 抽取文章中关联的图片路径
     * @param content
     * @return
     */
    private List<String> parseContentImages(String content){
        List<Map> contents = JSON.parseArray(content, Map.class);
        return contents.stream()
                .filter(map ->
                        map.get("type").equals(WemediaConstants.WM_NEWS_TYPE_IMAGE))
                .map(x->(String)x.get("value"))
                .map(url->url.replace(webSite,"").replace(" ",""))
                .distinct()
                .collect(Collectors.toList());
        //先使用fastjson将content转换成List<Map>集合
        //过滤出type为image的数据
        //获取value值
        //去掉域名和空格
        //去重

    }





    private void saveWmNews(WmNews wmNews){
        wmNews.setCreatedTime(new Date());
        wmNews.setUserId(WmThreadLocalUtils.getUser().getId());
        wmNews.setSubmitedTime(new Date());
        wmNews.setEnable(WemediaConstants.WM_NEWS_UP);
        if(wmNews.getId()==null){//新增
            save(wmNews);
        }else{
            //删除关联关系
            wmNewsMaterialMapper.delete(new LambdaQueryWrapper<WmNewsMaterial>().eq(WmNewsMaterial::getNewsId,wmNews.getId()));
            updateById(wmNews);
        }
    }

    private String imageListToStr(List<String> images) {
        return images.stream()
                .map((url)->{
                    if (url.startsWith(webSite)) {
                        return url.replace(webSite, "");
                    }
                    return url;
                }).collect(Collectors.joining(","));
        //stream里面的map方法，是把集合里面的每一个元素都执行一次map里面的方法，然后把执行的结果放到一个新的集合里面
    }
}
