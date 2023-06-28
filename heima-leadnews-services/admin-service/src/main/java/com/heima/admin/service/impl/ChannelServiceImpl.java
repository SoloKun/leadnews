package com.heima.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.admin.mapper.ChannelMapper;
import com.heima.admin.service.ChannelService;
import com.heima.model.admin.dtos.ChannelDTO;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * ClassName: ChannelServiceImpl
 * Package: com.heima.admin.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/13 21:31
 * @Version 1.0
 */
@Service
public class ChannelServiceImpl extends ServiceImpl<ChannelMapper,AdChannel> implements ChannelService {

    @Override
    public ResponseResult findByNameAndPage(ChannelDTO dto) {
        //1.校验参数 非空 分页
        if(dto==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"参数错误");
        }
        dto.checkParam();
        //2.条件查询
        Page<AdChannel> pageReq = new Page<>(dto.getPage(), dto.getSize());
        //封装分页查询
        //条件构造器，封装查询条件，可以使用lambda表达式
        LambdaQueryWrapper<AdChannel> wrapper = Wrappers.lambdaQuery();
        //封装条件参数 name status ord排序
        //name为空 模糊查询
        if(StringUtils.isNotBlank(dto.getName())){
            //left like %xxx right like xxx%  like %xxx%
            wrapper.like(AdChannel::getName,dto.getName());
            //AdChannel::getName 代表的是实体类的属性
            //select * from ad_channel where name like '%xxx%'
        }
        //status 不为空 精确查询
        if(dto.getStatus()!=null){
            wrapper.eq(AdChannel::getStatus,dto.getStatus());
            //select * from ad_channel where status = 1
        }
        //ord
        wrapper.orderByAsc(AdChannel::getOrd);
        //3.执行查询 封装返回结果 PageResponseResult
        IPage<AdChannel> pageResult = this.page(pageReq, wrapper);
        //4.返回结果
        return new PageResponseResult(dto.getPage(), dto.getSize(),pageResult.getTotal(),pageResult.getRecords());
    }

    @Override
    public ResponseResult insert(AdChannel Channel) {
        //1.校验参数 name非空 长度不能大于10 以及不能重复
        String name = Channel.getName();
        if(StringUtils.isBlank(name)){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"频道名称不能为空");
        }
        if(name.length()>10){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"频道名称不能大于10");
        }
        //根据名称查询频道
        int count = this.count(Wrappers.<AdChannel>lambdaQuery().eq(AdChannel::getName, name));
        if (count>0){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"频道名称不能重复");
        }
        //2.保存数据
        Channel.setCreatedTime(new Date());
        this.save(Channel);
        //3.返回结果
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);

    }

    @Override
    public ResponseResult update(AdChannel Channel) {
        if(Channel==null||Channel.getId()==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"id参数错误");
        }
        AdChannel oldChannel = this.getById(Channel.getId());

        if(oldChannel==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"数据不存在");
        }
        //校验唯一性
        String name = Channel.getName();
        if(StringUtils.isNotBlank(name)&&!name.equals(oldChannel.getName())){
            //根据名称查询频道
            int count = this.count(Wrappers.<AdChannel>lambdaQuery().eq(AdChannel::getName, name));
            if (count>0){
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST,"频道名称不能重复");
            }
        }
        //更新数据
        updateById(Channel);
        //返回结果
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult deleteById(Integer id) {
        if(id==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"id参数为空");
        }
        AdChannel Channel = this.getById(id);
        if(Channel==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"数据不存在");
        }
        if(Channel.getStatus()){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_ALLOW,"频道为启用状态不能删除");
        }
        //删除数据
        removeById(id);

        //返回结果
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult findAll() {
        //查询所有频道
        LambdaQueryWrapper<AdChannel> wrapper = Wrappers.lambdaQuery();
        wrapper.orderByAsc(AdChannel::getOrd);
        //被禁用的频道不查询
        wrapper.eq(AdChannel::getStatus,true);
        return ResponseResult.okResult(list(wrapper));
    }

}
