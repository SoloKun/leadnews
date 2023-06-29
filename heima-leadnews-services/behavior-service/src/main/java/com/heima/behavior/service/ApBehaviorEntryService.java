package com.heima.behavior.service;

import com.heima.model.behavior.pojos.ApBehaviorEntry;

/**
 * ClassName: ApBehaviorEntryService
 * Package: com.heima.behavior.service
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/29 16:29
 * @Version 1.0
 */
public interface ApBehaviorEntryService {
    /**
     * 查询行为实体
     * @param userId  用户id
     * @param equipmentId  设备id
     * @return
     */
    ApBehaviorEntry findByUserIdOrEquipmentId(Integer userId, Integer equipmentId);
}
