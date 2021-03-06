package com.mi.cims.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.mi.cims.aop.annotation.Operation;
import com.mi.cims.bean.bo.userInfo.AddUserInfoBo;
import com.mi.cims.bean.bo.userInfo.GetUserInfoBo;
import com.mi.cims.bean.bo.userInfo.UpdateUserInfoBo;
import com.mi.cims.bean.pojo.SuccessInfo;
import com.mi.cims.bean.vo.UserInfoVo;
import com.mi.cims.constant.ErrorCode;
import com.mi.cims.constant.HttpHeaderNames;
import com.mi.cims.exception.BusinessException;
import com.mi.cims.service.UserInfoService;

/**
 * ClassName: UserInfoController
 * Function:  用户管理控制器
 *
 * @author  孙忠飞
 * @date    2020年12月09日 下午1:14:09
 * @version V1.0.0
 */
@RestController
public class UserInfoController {

	
	// HTTP请求
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private UserInfoService userInfoService;
	
    /**   
     * @Title: getUserInfoByPage
     * @Description: 分页查询用户信息
     * @author: 孙忠飞 
     * @date:   2020年12月10日 下午1:23:06
     * @param:  GetUserInfoBo 查询条件
     * @return: PageInfo 分页列表
     * @throws: Exception
     */
	@SuppressWarnings("rawtypes")
	// 请求方式和链接
	@GetMapping("/userInfo")
	// 权限注解
	@Operation("GET_USER")
	public PageInfo getUserInfoByPage(@Valid GetUserInfoBo getUserInfoBo) throws Exception {
		// 取得当前页码数
		String currentPage = request.getHeader(HttpHeaderNames.CURRENT_PAGE);
		// 判断当前页是否为空
		if (StringUtils.isEmpty(currentPage)) {
			throw new BusinessException(ErrorCode.PAGE_PARAM_ERROR);
		}
		// 类型转换
		int pageNum = Integer.parseInt(currentPage);
		// 根据条件查询公司列表
		return userInfoService.getUserInfo(getUserInfoBo, pageNum, request);
	}
	
    /**   
     * @Title: addUserInfo
     * @Description: 新建用户信息
     * @author: 孙忠飞 
     * @date:   2020年12月10日 下午1:23:06
     * @param:  AddUserInfoBo 新建信息
     * @return: SuccessInfo 成功标识
     * @throws: Exception
     */
	@PostMapping("/userInfo/add")
	@Operation("ADD_USER")
	public SuccessInfo addUserInfo(@Valid AddUserInfoBo addUserInfoBo) throws Exception {
		// 添加用户
		userInfoService.addUserInfo(addUserInfoBo, request);
		// 返回成功
		return new SuccessInfo();
	}
	
    /**   
     * @Title: selectUserInfo
     * @Description: 根据用户ID查询用户信息
     * @author: 孙忠飞 
     * @date:   2020年12月10日 下午1:23:06
     * @param:  userId 		用户ID
     * @return: SuccessInfo 成功标识
     * @throws: Exception
     */
	@GetMapping("/userInfo/update/{userId}")
	@Operation("UPDATE_USER")
	public UserInfoVo selectUserInfo(@PathVariable 	@NotNull(message = "parameter.error") Integer userId) throws Exception {
		// 查询用户要更改的信息
		return userInfoService.selectUserInfoByUserId(userId);
	}
	
    /**   
     * @Title: updateUserInfo
     * @Description: 更新用户信息
     * @author: 孙忠飞 
     * @date:   2020年12月10日 下午1:23:06
     * @param:  UpdateUserInfoBo 更新用户信息
     * @return: SuccessInfo 成功标识
     * @throws: Exception
     */
	@PutMapping("/userInfo/updateUser")
	@Operation("UPDATE_USER")
	public SuccessInfo updateUserInfo(@Valid UpdateUserInfoBo updateUserInfoBo) throws Exception {
		// 更新用户信息
		userInfoService.updateUserInfo(updateUserInfoBo, request);
		// 返回成功信息
		return new SuccessInfo();
	}
	
    /**   
     * @Title: resetUserInfoPwd
     * @Description: 重置用户密码
     * @author: 孙忠飞 
     * @date:   2020年12月10日 下午1:23:06
     * @param:  userId 		用户ID
     * @return: SuccessInfo 成功标识
     * @throws: Exception
     */
	@PutMapping("/userInfo/resetPwd/{userId}")
	public SuccessInfo resetUserInfoPwd(@PathVariable @NotNull(message = "parameter.error") Integer userId) throws Exception {
		// 重置用户密码
		userInfoService.resetUserInfoPwd(userId);
		// 返回成功信息
		return new SuccessInfo();
	}
	
    /**   
     * @Title: deleteUserInfo
     * @Description: 根据用户ID删除用户信息
     * @author: 孙忠飞 
     * @date:   2020年12月10日 下午1:23:06
     * @param:  userId 		用户ID
     * @return: SuccessInfo 成功标识
     * @throws: Exception
     */
	@PutMapping("/userInfo/delete/{userId}")
	@Operation("DELETE_USER")
	public SuccessInfo deleteUserInfo(@PathVariable Integer userId) throws Exception {
		// 逻辑删除用户信息
		userInfoService.deleteUserInfoByUserId(userId);
		// 返回成功信息
		return new SuccessInfo();
	}
}
