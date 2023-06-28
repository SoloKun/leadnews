package com.heima.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.SensitiveDTO;
import com.heima.model.admin.pojos.AdSensitive;
import com.heima.model.common.dtos.ResponseResult;

import java.util.List;

/**
 * ClassName: AdSensitiveService
 * Package: com.heima.admin.service
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/14 19:55
 * @Version 1.0
 */
public interface AdSensitiveService extends IService<AdSensitive> {
    //IService是mybatis-plus提供的一个接口，用于实现基本的CRUD操作

    /**
     * 显示敏感词列表
     * @return
     */
    public ResponseResult list(SensitiveDTO dto);

    /**
     * 新增敏感词
     * @param adSensitive
     * @return
     */
    public ResponseResult insert(AdSensitive adSensitive);

    /**
     * 删除敏感词
     * @param id
     * @return
     */
    public ResponseResult deleteById(Integer id);

    /**
     * 修改敏感词
     * @param adSensitive
     * @return
     */
    public ResponseResult update(AdSensitive adSensitive);

    public ResponseResult<List<String>> selectAllSensitives();

}
