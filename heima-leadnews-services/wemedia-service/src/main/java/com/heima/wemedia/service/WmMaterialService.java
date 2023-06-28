package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDTO;
import com.heima.model.wemedia.pojos.WmMaterial;
import org.springframework.web.multipart.MultipartFile;

/**
 * ClassName: WmMaterialService
 * Package: com.heima.wemedia.service
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/18 0:30
 * @Version 1.0
 */
public interface WmMaterialService extends IService<WmMaterial> {
    /**
     * 上传图片接口
     * @param multipartFile
     * @return
     */
    ResponseResult uploadPicture(MultipartFile multipartFile);

    /**
     * 删除图片
     * @param id
     * @return
     */
    ResponseResult delPicture(Integer id);
    /**
     * 查询图片列表
     * @param dto
     * @return
     */

    ResponseResult findList(WmMaterialDTO dto);

    ResponseResult updateStatus(Integer id, Short type);

}
