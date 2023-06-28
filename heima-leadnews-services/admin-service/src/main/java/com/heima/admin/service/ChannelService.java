package com.heima.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.ChannelDTO;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.common.dtos.ResponseResult;

/**
 * ClassName: ChannelService
 * Package: com.heima.admin.service
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/13 21:29
 * @Version 1.0
 */
public interface ChannelService extends IService<AdChannel> {
    /**
     * 根据名称分页查询频道列表
     * @param dto
     * @return
     */
    public ResponseResult findByNameAndPage(ChannelDTO dto);
    /**
     * 新增频道
     * @param Channel
     * @return
     */
    public ResponseResult insert(AdChannel Channel);

    /**
     * 修改频道
     * @param Channel
     * @return
     */
    public ResponseResult update(AdChannel Channel);
    /**
     * 删除频道
     * @param id
     * @return
     */
    public ResponseResult deleteById(Integer id);

    /**
     * 查询所有频道
     * @param
     * @return
     */
    public ResponseResult findAll();
}
