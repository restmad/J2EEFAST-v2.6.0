/*
 * All content copyright http://www.j2eefast.com, unless
 * otherwise indicated. All rights reserved.
 * No deletion without permission
 */
package com.j2eefast.common.core.auth;


import org.apache.shiro.authc.UsernamePasswordToken;

import com.j2eefast.common.core.base.entity.LoginUserEntity;

/**
 * <p>授权数据库操作</p>
 *
 * @author: zhouzhou
 * @date: 2019-04-01 15:08
 * @web: http://www.j2eefast.com
 * @version: 1.0.1
 */
public interface AuthService {


	/**
	 * 登陆前校验
	 * @param username
	 * @param password
	 * @return
	 */
	Integer loginBeforeVerify(String username, String password);

	/**
	 * 登陆校验账号密码
	 * @param username
	 * @param password
	 * @return
	 */
	LoginUserEntity loginVerify(UsernamePasswordToken taken);

	/**
	 * 免密授权登录
	 * @param openId
	 * @return
	 */
	LoginUserEntity freeLoginVerify(String openId, String tenantId);
	
	
	/**
	 * 手机验证码登录
	 * @date 2021-09-24
	 * @param mobile
	 * @return
	 */
	LoginUserEntity valideCodeLoginVerify(String mobile, String valideCode);


	/**
	 * 获取相应权限
	 * @param loginUser
	 * @param userId
	 * @return
	 */
	void authorization(LoginUserEntity loginUser, Long userId);


	/**
	 * 设置登陆详情
	 * @param loginUser
	 * @param userId
	 */
	void setLoginDetails(LoginUserEntity loginUser, Long userId, String source);
	
	
	/**
	 * 排挤登录
	 * @author ZhouZhou
	 * @date 2021-10-15
	 * @param username
	 */
	void excludeLogin(LoginUserEntity loginUser);

}
