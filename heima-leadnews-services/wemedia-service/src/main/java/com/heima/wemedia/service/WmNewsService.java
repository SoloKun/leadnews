package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.NewsAuthDTO;
import com.heima.model.wemedia.dtos.WmNewsDTO;
import com.heima.model.wemedia.dtos.WmNewsPageReqDTO;
import com.heima.model.wemedia.pojos.WmNews;

/**
 * ClassName: WmNewsService
 * Package: com.heima.wemedia.service
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/18 23:31
 * @Version 1.0
 */
public interface WmNewsService extends IService<WmNews> {
    public ResponseResult findList(WmNewsPageReqDTO dto);

    public ResponseResult submitNews(WmNewsDTO dto);

    public ResponseResult findWmNewsById(Integer id);


    public ResponseResult delNews(Integer id);

    public ResponseResult downOrUp(WmNewsDTO dto);

    public ResponseResult findList(NewsAuthDTO dto);

    public ResponseResult findWmNewsVo(Integer id) ;

    public ResponseResult updateStatus(Short status, NewsAuthDTO dto);

}
