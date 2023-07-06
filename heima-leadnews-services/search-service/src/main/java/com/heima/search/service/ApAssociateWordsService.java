package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDTO;

/**
 * ClassName: ApAssociateWordsService
 * Package: com.heima.search.service
 * Description:
 *
 * @Author solokun
 * @Create 2023/7/6 16:35
 * @Version 1.0
 */
public interface ApAssociateWordsService {
    ResponseResult findAssociate(UserSearchDTO userSearchDto);
}
