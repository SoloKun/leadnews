package com.heima.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.admin.mapper.AdSensitiveMapper;
import com.heima.admin.service.AdSensitiveService;
import com.heima.model.admin.dtos.SensitiveDTO;
import com.heima.model.admin.pojos.AdSensitive;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassName: AdsensitiveServiceImpl
 * Package: com.heima.admin.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/14 19:59
 * @Version 1.0
 */
@Service
public class AdsensitiveServiceImpl extends ServiceImpl<AdSensitiveMapper,AdSensitive> implements AdSensitiveService{
    @Override
    public ResponseResult list(SensitiveDTO dto) {
        if(dto==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"参数错误");
        }
        dto.checkParam();//校验参数
        IPage pageParam = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<AdSensitive> wrapper =  Wrappers.lambdaQuery();
        if(StringUtils.isNotBlank(dto.getName())){
            wrapper.like(AdSensitive::getSensitives,dto.getName());
            //select * from ad_sensitive where sensitives like '%xxx%'
            //like()方法的第一个参数是实体类的属性,第二个参数是要查询的值
        }
        IPage pageResult = page(pageParam, wrapper);
        return new PageResponseResult(dto.getPage(), dto.getSize(),pageResult.getTotal(),pageResult.getRecords());
    }

    @Override
    public ResponseResult insert(AdSensitive adSensitive) {
        if(adSensitive==null||StringUtils.isBlank(adSensitive.getSensitives())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"参数错误");
        }
        //校验敏感词是否存在
        LambdaQueryWrapper<AdSensitive> wrapper = Wrappers.lambdaQuery();
        int count = this.count(wrapper.eq(AdSensitive::getSensitives, adSensitive.getSensitives()));
        if(count>0){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST,"敏感词已存在");
        }
        //保存敏感词
        adSensitive.setCreatedTime(new Date());
        this.save(adSensitive);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);

    }

    @Override

    public ResponseResult deleteById(Integer id) {
        if(id==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"参数错误");
        }
        //根据id查询敏感词
        AdSensitive adSensitive = this.getById(id);
        if(adSensitive==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"敏感词不存在");
        }
        //删除敏感词
        this.removeById(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult update(AdSensitive adSensitive) {
        if(adSensitive==null||adSensitive.getId()==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"参数错误");
        }
        //根据id查询敏感词
        AdSensitive oldAdSensitive = this.getById(adSensitive.getId());
        String oldSensitive = oldAdSensitive.getSensitives();
        if(StringUtils.isNotBlank(adSensitive.getSensitives())&&!oldSensitive.equals(adSensitive.getSensitives())){
            int count = this.count(Wrappers.<AdSensitive>lambdaQuery().eq(AdSensitive::getSensitives, adSensitive.getSensitives()));
            if(count>0){
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST,"敏感词已存在");
            }
        }
        this.updateById(adSensitive);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Autowired
    private AdSensitiveMapper adSensitiveMapper;
    @Override
    public ResponseResult<List<String>> selectAllSensitives() {
        return ResponseResult.okResult(adSensitiveMapper.findAllSensitives());
    }
}
