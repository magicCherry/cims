package com.mi.cims.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mi.cims.bean.bo.ChangeMatrixPwdBo;
import com.mi.cims.bean.po.MenuInfo;
import com.mi.cims.bean.po.UserInfo;
import com.mi.cims.bean.po.UserRole;
import com.mi.cims.bean.pojo.LoginedManager;
import com.mi.cims.bean.pojo.MenuItem;
import com.mi.cims.constant.ErrorCode;
import com.mi.cims.dao.MenuInfoMapper;
import com.mi.cims.dao.RoleOperationMapper;
import com.mi.cims.dao.UserInfoMapper;
import com.mi.cims.dao.UserRoleMapper;
import com.mi.cims.exception.BusinessException;
import com.mi.cims.service.HomeService;
import com.mi.cims.service.RedisService;
import com.mi.cims.util.CacheUtils;

/**
 * ClassName: HomeServiceImpl
 * Function: 主页业务服务实现
 *
 * @author Magic Image-刘伟
 * @date 2017年10月19日 下午1:47:01
 * @version V1.0.0
 */
@Service
public class HomeServiceImpl implements HomeService {

    // 菜单DAO
    @Autowired
    private MenuInfoMapper menuInfoMapper;

    // 角色-操作关系DAO
    @Autowired
    private RoleOperationMapper roleOperationMapper;

    // 管理员信息DAO
    @Autowired
    UserInfoMapper userInfoMapper;
    
    // 用户角色信息
    @Autowired
    private UserRoleMapper userRoleMapper;

    // redis服务
    @Autowired
    private RedisService redisService;

/*    // 多语言资源文件处理器
    @Autowired
    private MessageSource messageSource;*/

    // 动态阵列表超时时间
    @Value("${matirx.timeout}")
    private long matrixTimeout;

    // session超时时间
    @Value("${sesion.timeout}")
    private long sessionTimeout;

    /** 
     * getMenuList:取得菜单列表
     * 
     * @author 刘伟 
     * @date 2017年10月16日 下午7:13:35
     * @param request HTTP请求
     * @return 菜单列表
     * @throws Exception 
     */
    public List<MenuItem> getMenuList(HttpServletRequest request) throws Exception {

        // 取得已登录管理员ID
        LoginedManager loginedManager = CacheUtils.getLoginedManager(redisService, request);
        // 取得已登录管理员最新角色信息
        UserInfo userInfo = userInfoMapper.selectByLoginId(loginedManager.getManagerId());
    	// 根据用户ID查询所拥有的角色
    	List<UserRole> UserRole = userRoleMapper.selectRoleByUserId(userInfo.getUserId());
        // 用户角色信息数组
        Integer[] roleId = new Integer[UserRole.size()];
    	for(int i=0;i<UserRole.size();i++) {
    		roleId[i] = UserRole.get(i).getRoleId();
    	}
    	// 权限数组升序排序
    	java.util.Arrays.sort(roleId);
        // 更新已登录管理员缓存信息
        loginedManager.setRoleId(roleId[0].toString());
        CacheUtils.updateLoginedManager(redisService, loginedManager, request, sessionTimeout * 60);

//        // 用户角色信息数组
//        String[] roleId =  userInfo.getRoleId().split(",");
//        // 登录用户有多个角色时，取得最大权限，只有一个角色时取得当前权限
//        List<MenuInfo> menuInfoList = new ArrayList<MenuInfo>();
//        if(roleId.length == 1) {
//            // 取得角色ID对应的菜单信息列表
//            menuInfoList = menuInfoMapper.selectByRoleId(Integer.parseInt(userInfo.getRoleId()));
//        }else {
//        	java.util.Arrays.sort(roleId);
//            // 取得角色ID对应的菜单信息列表
//            menuInfoList = menuInfoMapper.selectByRoleId(Integer.parseInt(roleId[0]));
//        }
        List<MenuInfo> menuInfoList = menuInfoMapper.selectByRoleId(roleId[0]);
        // 构建菜单列表
        List<MenuItem> menuItemList = new ArrayList<MenuItem>();
        Map<Integer, MenuItem> menuItemMap = new HashMap<Integer, MenuItem>();
        //Locale locale = MiUtils.getRequestLocale(request);
        for (int i = 0; i < menuInfoList.size(); i++) {

            // 转化成菜单项对象
            MenuItem menuItem = new MenuItem();
            menuItem.setId(menuInfoList.get(i).getMenuId()); // 菜单ID
            menuItem.setText(menuInfoList.get(i).getMenuName()); // 菜单文字
            menuItem.setPage(menuInfoList.get(i).getPageUrl()); // 页面URL
            menuItem.setIcon(menuInfoList.get(i).getIcon()); // 图标
            menuItemMap.put(menuItem.getId(), menuItem);
            // 如果是一级菜单
            if (menuInfoList.get(i).getParentMenuId() == 0) {
                menuItemList.add(menuItem);
            } else { // 如果是子菜单
                // 将子菜单加入到加入父菜单的子菜单列表中
                MenuItem parentMenuItem = menuItemMap.get(menuInfoList.get(i).getParentMenuId());
                if (parentMenuItem == null) {
                    continue;
                }
                if (parentMenuItem.getSubMenu() == null) {
                    parentMenuItem.setSubMenu(new ArrayList<MenuItem>());
                }
                parentMenuItem.getSubMenu().add(menuItem);
            }
        }

        return menuItemList;
    }

    /** 
     * getOperationCodeSet:取得操作编码集合
     * 
     * @author 刘伟 
     * @date 2017年10月19日 下午2:32:26
     * @param request HTTP请求
     * @return 操作编码集合
     * @throws Exception 
     */
    public Set<String> getOperationCodeSet(HttpServletRequest request) throws Exception {
        // 取得已登录管理员角色ID
        LoginedManager loginedManager = CacheUtils.getLoginedManager(redisService, request);
        // 取得已登录管理员最新角色信息
        UserInfo userInfo = userInfoMapper.selectByLoginId(loginedManager.getManagerId());

    	// 根据用户ID查询所拥有的角色
    	List<UserRole> UserRole = userRoleMapper.selectRoleByUserId(userInfo.getUserId());
        // 用户角色信息数组
        Integer[] roleId = new Integer[UserRole.size()];
    	for(int i=0;i<UserRole.size();i++) {
    		roleId[i] = UserRole.get(i).getRoleId();
    	}
    	// 权限数组升序排序
    	java.util.Arrays.sort(roleId);
        
//        // 用户角色信息数组
//        String[] roleId =  userInfo.getRoleId().split(",");
//        // 登录用户有多个角色时，取得最大权限，只有一个角色时取得当前权限
//        Set<String> operationSet = new HashSet<String>();
//        if(roleId.length == 1) {
//            // 查询该角色对应的操作编码集合
//            operationSet = roleOperationMapper.selectOperationCodeByRoleId(Integer.parseInt(userInfo.getRoleId()));
//        }else {
//        	// 权限数组升序排序
//        	java.util.Arrays.sort(roleId);
//            // 查询该角色对应的操作编码集合
//            operationSet = roleOperationMapper.selectOperationCodeByRoleId(Integer.parseInt(roleId[0]));
//        }
    	Set<String> operationSet = roleOperationMapper.selectOperationCodeByRoleId(roleId[0]);
        // 更新已登录管理员缓存信息
        loginedManager.setOperationSet(operationSet);
        loginedManager.setRoleId(roleId[0].toString());
        CacheUtils.updateLoginedManager(redisService, loginedManager, request, sessionTimeout * 60);

        return operationSet;
    }

    /**   
     * @Title: changeMatrixPwd
     * @Description: 修改用户登录密码
     * @author: 孙忠飞 
     * @date:   2020年12月10日 下午1:23:06
     * @param:  ChangeMatrixPwdBo 修改密码信息
     * @param:  loginManagerId 	       管理员ID
     * @param:  request			       请求信息
     * @throws: Exception
     */
    // 事务注解
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void changeMatrixPwd(ChangeMatrixPwdBo changeMatrixPwdBo, Integer loginManagerId, HttpServletRequest request) throws Exception {
        // 根据管理员登录id查询管理员信息
        UserInfo userInfo = userInfoMapper.selectByUserId(loginManagerId);
        // 管理员不存在提示错误信息
        if(userInfo == null) {
        	throw new BusinessException(ErrorCode.ACCT_NOT_EXIST);
        }
        // 判断旧密码是否输入正确，如果错误提示错误消息
        if(userInfo.getPassWord().equalsIgnoreCase(changeMatrixPwdBo.getOldMatrixPwd())) {
        	// 判断新密码和确认密码是否一致，如果不一致提示错误消息
        	if(changeMatrixPwdBo.getMatrixPwd().equalsIgnoreCase(changeMatrixPwdBo.getMatrixPwdForSure())) {
        		// 验证成功，更新管理员登录密码
        		userInfo.setPassWord(changeMatrixPwdBo.getMatrixPwd());
        		userInfoMapper.resetPwd(userInfo);
        	}else {
        		throw new BusinessException(ErrorCode.NEWPWD_CFMPWD_NOT_SAME);
        	}
        }else {
        	throw new BusinessException(ErrorCode.OLD_PWD_ERROR);
        }
    }

}

