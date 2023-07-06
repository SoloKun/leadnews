package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.HistorySearchDTO;
import com.heima.model.search.dtos.UserSearchDTO;

/**
 * ClassName: ApUserSearchService
 * Package: com.heima.search.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/7/6 15:37
 * @Version 1.0
 */
public interface ApUserSearchService {
    public void insert( UserSearchDTO userSearchDto);
    ResponseResult findUserSearch(UserSearchDTO userSearchDto);
    ResponseResult delUserSearch(HistorySearchDTO historySearchDto);
}
