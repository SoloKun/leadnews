package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.exception.CustException;
import com.heima.file.service.FileStorageService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.constants.wemedia.WemediaConstants;
import com.heima.model.threadlocal.WmThreadLocalUtils;
import com.heima.model.wemedia.dtos.WmMaterialDTO;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * ClassName: WmMaterialServiceImpl
 * Package: com.heima.wemedia.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/18 0:31
 * @Version 1.0
 */
@Service
@Slf4j
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {
    @Autowired
    private FileStorageService fileStorageService;
    @Value("${file.oss.prefix}")
    private String prefix;
    @Value("${file.oss.web-site}")
    private String webSite;
    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile) {
        if(multipartFile == null){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"上传图片不能为空");
        }
        //获取当前登录用户信息
        WmUser user = WmThreadLocalUtils.getUser();
        if(user == null){
            CustException.cust(AppHttpCodeEnum.NO_OPERATOR_AUTH);
        }
        //获取文件名称
        String originalFilename = multipartFile.getOriginalFilename();

        //判断文件后缀是否符合要求
        if(!checkSuffix(originalFilename)){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"上传图片格式不支持,支持jpg,jpeg,png,gif");
        }

        //判断文件大小是否符合要求
        long size = multipartFile.getSize();
        if(size > 1024 * 1024 * 2){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"上传图片过大,不能超过2M");
        }
        //上传图片
        String fileId = null;
        try{
            //生成文件名称，防止文件重名，使用UUID
            String fileName = UUID.randomUUID().toString().replace("-","") ;
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            fileName = fileName + suffix;
            fileId = fileStorageService.store(prefix,fileName,multipartFile.getInputStream());
            log.info("上传图片成功，文件id为：{}",fileId);
        }catch (IOException e){
            e.printStackTrace();
            log.error("上传图片失败，错误信息为：{}",e.getMessage());
            CustException.cust(AppHttpCodeEnum.SERVER_ERROR,"服务器内部错误");

        }
        WmMaterial wmMaterial = new WmMaterial();
        wmMaterial.setIsCollection(WemediaConstants.CANCEL_COLLECT_MATERIAL);
        wmMaterial.setUserId(user.getId());
        wmMaterial.setType((short)0);
        wmMaterial.setUrl(fileId);
        wmMaterial.setCreatedTime(new Date());
        this.save(wmMaterial);
        return ResponseResult.okResult(wmMaterial);

    }
    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;
    @Override
    public ResponseResult delPicture(Integer id) {
        if(id == null){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"删除图片失败，图片id不能为空");
        }
        //获取当前登录用户信息
        WmUser user = WmThreadLocalUtils.getUser();
        if(user == null){
            CustException.cust(AppHttpCodeEnum.NO_OPERATOR_AUTH);
        }
        //根据图片id查询图片信息
        WmMaterial material = this.getById(id);
        if(material == null){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"删除图片失败，图片不存在");
        }
        //判断图片是否是当前用户的
        if(!material.getUserId().equals(user.getId())){
            CustException.cust(AppHttpCodeEnum.NO_OPERATOR_AUTH,"删除图片失败，图片不属于当前用户");
        }
        LambdaQueryWrapper<WmNewsMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WmNewsMaterial::getMaterialId, id);
        int count = wmNewsMaterialMapper.selectCount(wrapper);
        if(count > 0){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"删除图片失败，图片已被引用");
        }

        //删除图片
        fileStorageService.delete(material.getUrl());
        //删除图片信息
        this.removeById(material.getId());
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);

    }

    @Override
    public ResponseResult findList(WmMaterialDTO dto) {
        dto.checkParam();
        LambdaQueryWrapper<WmMaterial> queryWrapper = new LambdaQueryWrapper<>();
        if(dto.getIsCollection()!=null&&dto.getIsCollection()==1){
            queryWrapper.eq(WmMaterial::getIsCollection,dto.getIsCollection());
        }
        WmUser user = WmThreadLocalUtils.getUser();
        if(user!=null){
            queryWrapper.eq(WmMaterial::getUserId,user.getId());
        }
        queryWrapper.orderByAsc(WmMaterial::getCreatedTime);
        IPage<WmMaterial> pageParam = new Page<>(dto.getPage(),dto.getSize());
        IPage<WmMaterial> pageResult = this.page(pageParam, queryWrapper);
        List<WmMaterial> records = pageResult.getRecords();
        for(WmMaterial record : records){
            record.setUrl(webSite+record.getUrl());
        }
        PageResponseResult responseResult = new PageResponseResult(dto.getPage(),dto.getSize(),pageResult.getTotal());
        responseResult.setData(records);
        return responseResult;
    }

    @Override
    public ResponseResult updateStatus(Integer id, Short type) {
        if(id==null||type==null){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"修改图片状态失败，参数不能为空");
        }
        WmMaterial material = this.getById(id);
        if(material==null){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"修改图片状态失败，图片不存在");
        }
        //获取当前登录用户信息
        Integer uid = WmThreadLocalUtils.getUser().getId();
        if(!material.getUserId().equals(uid)){
            CustException.cust(AppHttpCodeEnum.NO_OPERATOR_AUTH,"修改图片状态失败，图片不属于当前用户");
        }
        material.setIsCollection(type);
        this.updateById(material);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 校验文件后缀
     * @param fileName
     * @return
     */
    boolean checkSuffix(String fileName){
        if(StringUtils.isBlank(fileName)){
            return false;
        }
        List<String>allowSuffix = Arrays.asList(".jpg",".jpeg",".png",".gif");
        Boolean isAllow = false;
        for(String suffix : allowSuffix){
            if(fileName.endsWith(suffix)){
                isAllow = true;
                break;
            }
        }
        return isAllow;
    }

}
