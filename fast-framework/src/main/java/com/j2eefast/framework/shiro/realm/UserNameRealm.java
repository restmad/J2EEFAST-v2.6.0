/*
 * All content copyright http://www.j2eefast.com, unless 
 * otherwise indicated. All rights reserved.
 * No deletion without permission
 */
package com.j2eefast.framework.shiro.realm;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

import com.j2eefast.common.core.base.entity.LoginUserEntity;
import com.j2eefast.common.core.enums.LogType;
import com.j2eefast.common.core.utils.RedisUtil;
import com.j2eefast.common.core.utils.SpringUtil;
import com.j2eefast.framework.sys.constant.factory.ConstantFactory;
import com.j2eefast.framework.sys.entity.SysRoleEntity;
import com.j2eefast.framework.sys.entity.SysUserEntity;
import com.j2eefast.framework.sys.service.SysUserService;
import com.j2eefast.framework.utils.UserUtils;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import com.j2eefast.common.core.exception.RxcException;
import com.j2eefast.common.core.manager.AsyncManager;
import com.j2eefast.common.core.shiro.RedisCacheManager;
import com.j2eefast.common.core.utils.ToolUtil;
import com.j2eefast.framework.manager.factory.AsyncFactory;
import com.j2eefast.framework.shiro.service.SysLoginService;
import com.j2eefast.framework.utils.Constant;

/**
 * ??????????????????Shiro???????????????????????? ??????
 * @author zhouzhou
 * @date 2020-03-12 20:57
 */
@Slf4j
public class UserNameRealm extends AuthorizingRealm {

	/**
	 * ??????(?????????????????????)
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		LoginUserEntity user = (LoginUserEntity) principals.getPrimaryPrincipal();
		log.info("------->>>??????(?????????????????????)");
		Long userId = user.getId();
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		// ????????????????????????????????????
		if (userId.equals(Constant.SUPER_ADMIN)){
			info.addRole("ADMIN");
			info.addStringPermission("*:*:*");
		} else {
			info.addRoles(UserUtils.getRoleKey(user.getRoles()));
			//??????????????????
			info.setStringPermissions(ConstantFactory.me()
					.findPermissionsByUserId(userId));
		}
		return info;
	}

	/**
	 * ??????(???????????????)
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken)
			throws AuthenticationException {
		UsernamePasswordToken token = (UsernamePasswordToken) authcToken;
		// ??????????????????
		LoginUserEntity authenUser = new LoginUserEntity();
		try {
			authenUser = SpringUtil.getBean(SysLoginService.class).loginVerify(token);
		}catch (RxcException e) {
			//????????????????????????
			if(e.getCode().equals("50001")) {
				throw new UnknownAccountException(e.getMessage(), e);
			}else if(e.getCode().equals("50002")) {
				throw new LockedAccountException(e.getMessage(), e);
			}else if(e.getCode().equals("50003")) {
				throw new ExcessiveAttemptsException(e.getMessage(), e);
			}else if(e.getCode().equals("50005")) {
				throw new IncorrectCredentialsException(e.getMessage(), e);
			}else if(e.getCode().equals("50004")) {
				throw new UnknownAccountException(e.getMessage(), e);
			}else if(e.getCode().equals("50006")) {
				throw new UnknownAccountException(e.getMessage(), e);
			}
		}catch (Exception e){
			log.error("?????????[" + authenUser.getUsername() + "]??????????????????..???????????????:{}", e.getMessage());
            throw new AuthenticationException(e.getMessage(), e);
        }
		SysUserEntity sysUser = SpringUtil.getBean(SysUserService.class).getById(authenUser.getId());
		SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(authenUser, sysUser.getPassword(),
				ByteSource.Util.bytes(sysUser.getSalt()), getName());
		return info;
	}

	@Override
	public void setCredentialsMatcher(CredentialsMatcher credentialsMatcher) {
		HashedCredentialsMatcher shaCredentialsMatcher = new HashedCredentialsMatcher();
		shaCredentialsMatcher.setHashAlgorithmName(UserUtils.hashAlgorithmName);
		shaCredentialsMatcher.setHashIterations(UserUtils.hashIterations);
		shaCredentialsMatcher.setStoredCredentialsHexEncoded(true);
		super.setCredentialsMatcher(shaCredentialsMatcher);
	}
	
	/**
	 * ??????????????????Realm
	 */
	@Override
	public boolean supports(AuthenticationToken token) {
		return token instanceof UsernamePasswordToken;
	}
	
	/**
	 * ????????????
	 */
	@Override
	public void onLogout(PrincipalCollection principals) {
		super.onLogout(principals);
		LoginUserEntity user = (LoginUserEntity) principals.getPrimaryPrincipal();
		//????????????
		AsyncManager.me().execute(AsyncFactory.outLoginInfor(user.getUsername(),
				user.getCompId(),user.getDeptId(), "00000","????????????!",
				DateUtil.date(),
				LogType.SYS.getVlaue(),user.getTenantId()));

        //super.onLogout(principals);
    }

	/**
	 * ??????????????????
	 */
	public void clearCachedAuthorizationInfo() {

		LoginUserEntity loginUser = UserUtils.getUserInfo();

		//????????????
		ConstantFactory.me().clearMenu();
		ConstantFactory.me().clearRole();
		ConstantFactory.me().clearModules();
		
		//????????????????????????
		if(!loginUser.getId().equals(Constant.SUPER_ADMIN)){

			//????????????????????????
			List<SysRoleEntity> roleList = ConstantFactory.me().getRoles(loginUser.getId());
			List<Object> reles = new ArrayList<>();
			roleList.forEach(r->{
				reles.add(r);
			});

			loginUser.setRoles(reles);
			//????????????
			UserUtils.reloadUser(loginUser);
		}
		
		this.clearCachedAuthorizationInfo(SecurityUtils.getSubject().getPrincipals());
		
		SpringUtil.getBean(RedisUtil.class).deletes(RedisCacheManager.DEFAULT_CACHE_KEY_PREFIX+"*");
		
	}
}
