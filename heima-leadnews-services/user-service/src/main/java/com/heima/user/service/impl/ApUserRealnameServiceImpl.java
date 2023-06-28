package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.exception.CustException;
import com.heima.feigns.ArticleFeign;
import com.heima.feigns.WemediaFeign;
import com.heima.model.article.pojos.ApAuthor;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.constants.admin.AdminConstants;
import com.heima.model.user.dtos.AuthDTO;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.ApUserRealname;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.mapper.ApUserRealnameMapper;
import com.heima.user.service.ApUserRealnameService;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * ClassName: ApUserRealnameServiceImpl
 * Package: com.heima.user.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/14 23:06
 * @Version 1.0
 */
@Service
public class ApUserRealnameServiceImpl extends ServiceImpl<ApUserRealnameMapper, ApUserRealname> implements ApUserRealnameService {

    @Override
    public ResponseResult loadListByStatus(AuthDTO dto) {
        if(dto == null){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"参数异常");
        }
        dto.checkParam();
        Page<ApUserRealname> pageReq = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<ApUserRealname> QueryWrapper = new LambdaQueryWrapper<>();
        if(dto.getStatus() != null){
            QueryWrapper.eq(ApUserRealname::getStatus,dto.getStatus());
        }
        this.page(pageReq,QueryWrapper);
        IPage<ApUserRealname> pageResp = this.page(pageReq, QueryWrapper);
        return new PageResponseResult(dto.getPage(),dto.getSize(),pageResp.getTotal(),pageResp.getRecords());
    }
    @Autowired
    private ApUserMapper apUserMapper;
    @Override
    @GlobalTransactional(rollbackFor = Exception.class,timeoutMills = 300000)
    public ResponseResult updateStatusById(AuthDTO dto, Short status) {
    //1.校验参数(实名认证d 不能为空
    if(dto.getId()==null){
        CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"实名认证id不能为空");
    }

    //2.据实名认证的id 查询 ap_user_realname数据 ap_user_realname(user_id)
    ApUserRealname apUserRealname = this.getById(dto.getId());

    // 3.判断实名认证的状态 是否 为 待审核 ( 1，2，9)
    if(apUserRealname == null || !apUserRealname.getStatus().equals(AdminConstants.WAIT_AUTH)){
        CustException.cust(AppHttpCodeEnum.DATA_NOT_ALLOW,"实名认证信息不存在或者状态不为待审核");
    }
    // 4.根据实名认证信息关联的apUserId 查询出 apUser信息 apUser
    ApUser apUser = apUserMapper.selectById(apUserRealname.getUserId());
    if (apUser == null){
        CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"关联的用户信息不存在");
    }
    // 5.修改实名认证的状态
    apUserRealname.setStatus(status);
    if(StringUtils.isNotBlank(dto.getMsg())){
        apUserRealname.setReason(dto.getMsg());
    }
    updateById(apUserRealname);

    // 6.判断 状态是2(审核失败 方法结束)还是9
    if(status.equals(AdminConstants.FAIL_AUTH)){
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
    // 7. 如果是9 代表审核通过:
    // 8. TODO 开通自媒体账户(查询是否已开通过保存自媒体账户信息) wm_user
    WmUser  wmUser = creatrWmuser(apUser);
    // 9. TODO 创建作信息《查询是否已创建保存作者信息) ap_author
    createApAuthor(apUser,wmUser);
    // if(dto.getId()==5){
    //     throw new RuntimeException("测试分布式事务，回滚");
    // }
    return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Autowired
    private ArticleFeign articleFeign;

    public void createApAuthor(ApUser apUser, WmUser wmUser) {
       ResponseResult<ApAuthor> responseResult = articleFeign.findByUserId(apUser.getId());
         if(!responseResult.checkCode()){
              CustException.cust(AppHttpCodeEnum.REMOTE_SERVER_ERROR,"远程调用文章微服务失败");
         }
        ApAuthor apAuthor = responseResult.getData();


        if(apAuthor != null){
            CustException.cust(AppHttpCodeEnum.DATA_EXIST,"作者信息已存在");
        }
       apAuthor = new ApAuthor();
       apAuthor.setName(apUser.getName());
       apAuthor.setType(2);
       apAuthor.setUserId(apUser.getId());
       apAuthor.setCreatedTime(new Date());
       apAuthor.setWmUserId(wmUser.getId());
       ResponseResult<ApAuthor> saveResult = articleFeign.save(apAuthor);
         if(!saveResult.checkCode()){
              CustException.cust(AppHttpCodeEnum.REMOTE_SERVER_ERROR,"远程调用文章微服务失败");
         }

   }

    @Autowired
    private WemediaFeign wemediaFeign;

    public WmUser creatrWmuser(ApUser apUser){
        //远程调用自媒体接口
        ResponseResult<WmUser> responseResult = wemediaFeign.findByName(apUser.getName());
        if(!responseResult.checkCode()){
            CustException.cust(AppHttpCodeEnum.REMOTE_SERVER_ERROR,"远程调用自媒体接口失败");
        }
        WmUser wmUser = responseResult.getData();


        //判断是否已经开通过自媒体账户
        if(wmUser!=null){
            CustException.cust(AppHttpCodeEnum.DATA_EXIST,"自媒体账户已存在");
        }
        //创建自媒体账户
        wmUser = new WmUser();
        wmUser.setName(apUser.getName());
        wmUser.setPassword(apUser.getPassword());
        wmUser.setSalt(apUser.getSalt());
        wmUser.setImage(apUser.getImage());
        wmUser.setPhone(apUser.getPhone());
        wmUser.setStatus(AdminConstants.PASS_AUTH.intValue());
        wmUser.setType(0);
        wmUser.setCreatedTime(new Date());
        wmUser.setApUserId(apUser.getId());
        ResponseResult<WmUser> saveResult = wemediaFeign.save(wmUser);
        if(!saveResult.checkCode()){
            CustException.cust(AppHttpCodeEnum.REMOTE_SERVER_ERROR,"远程调用自媒体接口失败");
        }
        return saveResult.getData();
    }
}
