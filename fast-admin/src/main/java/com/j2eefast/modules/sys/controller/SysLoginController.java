/*
 * All content copyright http://www.j2eefast.com, unless
 * otherwise indicated. All rights reserved.
 * No deletion without permission
 */
package com.j2eefast.modules.sys.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cn.hutool.core.codec.Base64Encoder;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.j2eefast.common.core.base.entity.LoginTenantEntity;
import com.j2eefast.common.core.base.entity.LoginUserEntity;
import com.j2eefast.common.core.constants.ConfigConstant;
import com.j2eefast.common.core.controller.BaseController;
import com.j2eefast.common.core.license.annotation.FastLicense;
import com.j2eefast.common.core.utils.*;
import com.j2eefast.framework.manager.factory.AsyncFactory;
import com.j2eefast.framework.shiro.token.MobileToken;
import com.j2eefast.framework.sys.entity.SysDictDataEntity;
import com.j2eefast.framework.sys.entity.SysUserEntity;
import com.j2eefast.framework.sys.service.SysDictDataService;
import com.j2eefast.framework.sys.service.SysTenantService;
import com.j2eefast.framework.sys.service.SysUserService;
import com.j2eefast.framework.utils.Constant;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.GifCaptcha;
import com.wf.captcha.base.Captcha;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.Super;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.j2eefast.common.core.crypto.SoftEncryption;
import com.j2eefast.common.core.exception.RxcException;
import com.j2eefast.common.core.exception.ServiceException;
import com.j2eefast.common.core.io.PropertiesUtils;
import com.j2eefast.common.core.io.file.MimeType;
import com.j2eefast.common.core.manager.AsyncManager;
import com.j2eefast.framework.utils.RedisKeys;
import com.j2eefast.framework.utils.UserUtils;

/**
 * 
 * ???????????????
 * @author zhouzhou
 * @date 2018-11-14 23:28
 */
@Controller
@Slf4j
public class SysLoginController extends BaseController {

	@Autowired
	private SysDictDataService sysDictDataService;
	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private SysTenantService sysTenantService;
	@Autowired
	private SysUserService sysUserService;
	@Autowired
	private CaptchaService captchaService;

	/**
	 * ???????????????????????????
	 */
	@Value("#{ @environment['shiro.rememberMe.enabled'] ?: false }")
	private boolean rememberMe;
	
	@Value("#{ @environment['fast.messages.enabled'] ?: false }")
	private boolean msgEnabled;
	
	/**
	 * ?????????????????????????????????
	 */
	@Value("#{ @environment['fast.tenantModel.enabled'] ?: false }")
	private boolean enabled;

	@Value("#{ @environment['shiro.isAllowRefreshIndex'] ?: false }")
	private boolean isAllowRefreshIndex;

	/**
	 * ?????????????????????
	 */
	@Value("#{${web.view.loginHtml} ?: null}")
	private LinkedHashMap<String, String> loginHtml;

	/**
	 * <p>????????????????????? ????????????????????????</p>
	 * <p>??????0 ?????? 1 ?????? 2 ????????????????????????</p>
	 * @author zhouzhou
	 * @date 2020-03-07 14:46
	 */
	@RequestMapping(value = "captcha.gif", method = RequestMethod.GET)
	public void captcha(HttpServletResponse response) throws IOException {
		String __ajax =  super.getPara("__ajax");
		if(__ajax != null && __ajax.equals("json")) {
			response.setContentType("application/json; charset=utf-8");
			response.setCharacterEncoding("UTF-8");
			JSONObject paramIn = new JSONObject();
			if(Global.getDbKey(ConfigConstant.SYS_LOGIN_CAPTACHA_TYPE,Constant.SYS_DEFAULT_VALUE_ZERO)
					.equals(Constant.SYS_DEFAULT_VALUE_ONE)){
				ArithmeticCaptcha gifCaptcha = new ArithmeticCaptcha();
				// ?????????????????????????????????
				gifCaptcha.setLen(3);
				// ????????????????????????3+2=?
				gifCaptcha.getArithmeticString();
				// ????????????????????????5
				String result =  gifCaptcha.text();
				UserUtils.setSessionAttribute(Constant.KAPTCHA_SESSION_KEY, result);
				paramIn.set("img",gifCaptcha.toBase64());
			}else if(Global.getDbKey(ConfigConstant.SYS_LOGIN_CAPTACHA_TYPE,Constant.SYS_DEFAULT_VALUE_ZERO)
					.equals(Constant.SYS_DEFAULT_VALUE_TWO)){
				int rd= Math.random()>0.5?1:0;
				if(rd == 1){
					GifCaptcha gifCaptcha = new GifCaptcha(130,48,4);
					gifCaptcha.setCharType(Captcha.TYPE_DEFAULT);
					String result = gifCaptcha.text();
					UserUtils.setSessionAttribute(Constant.KAPTCHA_SESSION_KEY, result);
					paramIn.set("img",gifCaptcha.toBase64());
				}else{
					ArithmeticCaptcha gifCaptcha = new ArithmeticCaptcha();
					// ?????????????????????????????????
					gifCaptcha.setLen(3);
					// ????????????????????????3+2=?
					gifCaptcha.getArithmeticString();
					// ????????????????????????5
					String result =  gifCaptcha.text();
					UserUtils.setSessionAttribute(Constant.KAPTCHA_SESSION_KEY, result);
					paramIn.set("img",gifCaptcha.toBase64());
				}
			}else{
				GifCaptcha gifCaptcha = new GifCaptcha(130,48,4);
				gifCaptcha.setCharType(Captcha.TYPE_DEFAULT);
				String result = gifCaptcha.text();
				UserUtils.setSessionAttribute(Constant.KAPTCHA_SESSION_KEY, result);
				paramIn.set("img",gifCaptcha.toBase64());
			}
			paramIn.set("uuid", "");
			paramIn.set("code", "0");
			InputStream input= new ByteArrayInputStream(paramIn.toString().getBytes());
			OutputStream output=response.getOutputStream();
			try{
				IoUtil.copy(input,output);
			}finally{
				IoUtil.close(input);
				IoUtil.close(output);
			}
		}else {
			response.setHeader("Cache-Control", "no-store, no-cache");
			response.setContentType(MimeType.IMAGE_GIF);
			if(Global.getDbKey(ConfigConstant.SYS_LOGIN_CAPTACHA_TYPE,Constant.SYS_DEFAULT_VALUE_ZERO)
					.equals(Constant.SYS_DEFAULT_VALUE_ONE)){
				ArithmeticCaptcha gifCaptcha = new ArithmeticCaptcha();
				// ?????????????????????????????????
				gifCaptcha.setLen(3);
				// ????????????????????????3+2=?
				gifCaptcha.getArithmeticString();
				// ????????????????????????5
				String result =  gifCaptcha.text();
				UserUtils.setSessionAttribute(Constant.KAPTCHA_SESSION_KEY, result);
				gifCaptcha.out(response.getOutputStream());
				return;
			}else if(Global.getDbKey(ConfigConstant.SYS_LOGIN_CAPTACHA_TYPE,Constant.SYS_DEFAULT_VALUE_ZERO)
					.equals(Constant.SYS_DEFAULT_VALUE_TWO)){
				int rd= Math.random()>0.5?1:0;
				if(rd == 1){
					GifCaptcha gifCaptcha = new GifCaptcha(130,48,4);
					gifCaptcha.setCharType(Captcha.TYPE_DEFAULT);
					String result = gifCaptcha.text();
					UserUtils.setSessionAttribute(Constant.KAPTCHA_SESSION_KEY, result);
					gifCaptcha.out(response.getOutputStream());
					return;
				}else{
					ArithmeticCaptcha gifCaptcha = new ArithmeticCaptcha();
					// ?????????????????????????????????
					gifCaptcha.setLen(3);
					// ????????????????????????3+2=?
					gifCaptcha.getArithmeticString();
					// ????????????????????????5
					String result =  gifCaptcha.text();
					UserUtils.setSessionAttribute(Constant.KAPTCHA_SESSION_KEY, result);
					gifCaptcha.out(response.getOutputStream());
					return;
				}
			}else{
				GifCaptcha gifCaptcha = new GifCaptcha(130,48,4);
				gifCaptcha.setCharType(Captcha.TYPE_DEFAULT);
				String result = gifCaptcha.text();
				UserUtils.setSessionAttribute(Constant.KAPTCHA_SESSION_KEY, result);
				gifCaptcha.out(response.getOutputStream());
				return;
			}
		}
	}
	
	/**
	 * ????????????????????????????????? 
	 * ?????????????????????????????? tenantId
	 * @author ZhouZhou
	 * @date 2021-09-23
	 * @param mobile ????????????
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getLoginValideCode", method = RequestMethod.POST)
	public ResponseData getLoginValideCode(String mobile) {
		
		if(!Global.isValidCode()) {
			return error(ToolUtil.message("?????????????????????????????????"));
		}
		
		//1.??????????????????
		if(!PhoneUtil.isMobile(mobile)) {
			return error(ToolUtil.message("???????????????????????????????????????"));
		}
		
		//2.???????????????????????????????????????
		String tenantId = StrUtil.EMPTY;
		if(enabled) {
			tenantId = super.getPara(Constant.TENANT_PARAMETER);
			if(ToolUtil.isEmpty(tenantId)){
				return error(ToolUtil.message("?????????????????????"));
			}
		}
		
		if(!sysUserService.findIsMobile(mobile, tenantId)) {
			return error(ToolUtil.message("?????????????????????"));
		}
		
		//3.????????????????????????????????????,??????????????????.
		Long timeLen = redisUtil.getExpire(RedisKeys.getLoginValidKey(mobile));
		if(timeLen > 0 && (Global.Validity() - timeLen) < Global.smsRate()) {
			return error(ToolUtil.message("???????????????,??????????????????????????????,??????????????????"));
		}
		
		//4.?????????????????????????????????????????? ???????????????????????????????????????
		if(Global.isValidationCode()){
			//?????????????????? ???????????????
			CaptchaVO captchaVO = new CaptchaVO();
			captchaVO.setCaptchaVerification(getPara("__captchaVerification"));
			if(!captchaService.verification(captchaVO).isSuccess()){
				return error(ToolUtil.message("???????????????"));
			}
		}
		
		//5. ???????????????
		String valideCode  = RandomUtil.randomNumbers(6);

		//??????
		redisUtil.set(RedisKeys.getLoginValidKey(mobile), valideCode, Global.Validity());
		return success().put("valideCode",valideCode);
		
		/** ????????????????????????
		//6. ???????????????
		ResponseData data = MsgPushUtils.SendSms("", Global.loginSmsTemplateId(), new String[] {mobile},
				new String[] {valideCode,Convert.toStr(Global.validityInt())});
		if(data.get("code").equals(ResponseData.DEFAULT_SUCCESS_CODE)) {
			//7.????????????
			redisUtil.set(RedisKeys.getLoginValidKey(mobile), valideCode, Global.Validity());
			return success();
		}else {
			return data;
		}
		*/
		
	}
	

	/**
	 * ??????????????????
	 * @param mmp
	 * @return
	 */
	@RequestMapping("${shiro.loginUrl}")
	public String login(ModelMap mmp){

		log.info("login");
		// ??????????????????
		if(isAllowRefreshIndex){
			String logined = super.getCookie("__LOGINED__");
			if (ToolUtil.isEmpty(logined) || "true".equals(logined)){
				CookieUtil.setCookie(getHttpServletResponse(), "__LOGINED__", "false");
			}
			if(UserUtils.isLogin()){
				UserUtils.logout();
			}
		}

		//?????????????????????????????????
		if(UserUtils.isLogin()){
			UserUtils.logout();
			return REDIRECT + super.loginUrl;
		}
		
		String view = super.getPara("view");
		if(ToolUtil.isEmpty(view)){
			view = Global.getDbKey(ConfigConstant.SYS_LOGIN_DEFAULT_VIEW,Constant.ADMIN_LTE);
		}else{
			List<SysDictDataEntity> listView = sysDictDataService.selectDictDataByType("sys_login_view");
			boolean flag = false;
			for(SysDictDataEntity dict: listView){
				if(dict.getDictValue().equals(view)){
					flag = true;
					break;
				}
			}
			if(!flag){
				view = Global.getDbKey(ConfigConstant.SYS_LOGIN_DEFAULT_VIEW,Constant.ADMIN_LTE);
			}
		}
		mmp.put("loginView",view);
		mmp.put("verification",Global.getDbKey(ConfigConstant.SYS_LOGIN_VERIFICATION,Constant.SYS_DEFAULT_VALUE_ONE)
				.equals(Constant.SYS_DEFAULT_VALUE_ONE));
		mmp.put("rememberMe",rememberMe);
		mmp.put("tenantModel",enabled);
		mmp.put("valideLogin",Global.isValidCode());
		mmp.put("login",this.loginUrl);
		mmp.put("msgEnabled",msgEnabled);
		mmp.put("isValidationCode",Global.isValidationCode());
		List<LoginTenantEntity>  loginTenantList = sysTenantService.getLoginTenantList();
		mmp.put("loginTenantList",loginTenantList);

		String urlPrefix = "login-" + view;
		//????????????????????????????????????
		if(ToolUtil.isNotEmpty(loginHtml) && ToolUtil.isNotEmpty(loginHtml.get(view))){
			urlPrefix = loginHtml.get(view);
		}
		return urlPrefix;
	}


	/**
	 * ??????????????????
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/tenant/list", method = RequestMethod.GET)
	public ResponseData tenantList(){
		List<LoginTenantEntity>  loginTenantList = sysTenantService.getLoginTenantList();
		return success(loginTenantList);
	}


	/**
	 * ??????
	 * @author zhouzhou
	 * @date 2020-03-07 14:47
	 */
	@FastLicense(vertifys = {"online","detection"})
	@ResponseBody
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ResponseData login(String username, String password,Boolean rememberMe) {
		log.info("login");
		try {
			if(ToolUtil.isEmpty(username) || 
					ToolUtil.isEmpty(password) || 
					ToolUtil.isEmpty(rememberMe)) {
				return error("50003",ToolUtil.message("??????????????????????????????.")); 
			}
			//??????????????????
			UsernamePasswordToken token = new UsernamePasswordToken(username, password,rememberMe);
			Subject subject = UserUtils.getSubject();
			subject.login(token);
		}catch (ServiceException e){
			CookieUtil.setReadCookie(getHttpServletResponse(),ConfigConstant.SECRETKEY, ConfigConstant.PUBKEY,60*60*24*7);
			return error("50006",ToolUtil.message("sys.login.sm4"));
		}
		catch (AuthenticationException e) {
			RxcException ex = (RxcException) e.getCause();
			String msg = ToolUtil.message("sys.login.failure");
			if(!ToolUtil.isEmpty(e.getMessage())){
				msg = e.getMessage();
			}
			if(ex.getCode() != null && "50004".equals(ex.getCode())) {
				return error(ex.getCode(),ex.getMessage());
			}
			return error(msg);
		}
		if(super.getHeader("__ajax").equals("json")){
			return success("????????????!").put("sId",UserUtils.getSession().getId())
			.put("token", Base64Encoder.encode(UserUtils.getUserInfo().getCsrfToken()))
			.put("expires_in",UserUtils.getSession().getTimeout() / (1000 * 60));
		}else{
			return success("????????????!").put("index", this.successUrl);
		}
	}
	
	@FastLicense(vertifys = {"online","detection"})
	@ResponseBody
	@RequestMapping(value = "/valideCodeLogin", method = RequestMethod.POST)
	public ResponseData valideCodeLogin(String mobile, String valideCode,Boolean rememberMe) {
		//1.??????????????????
		if(!PhoneUtil.isMobile(mobile)) {
			return error("????????????????????????!");
		}
		try {
			Subject subject = null;
			MobileToken token = new MobileToken(mobile, valideCode,rememberMe);
			subject = UserUtils.getSubject();
			subject.login(token);
		}catch (AuthenticationException e) {
			RxcException ex = (RxcException) e.getCause();
			String msg = ToolUtil.message("sys.login.failure");
			if(!ToolUtil.isEmpty(e.getMessage())){
				msg = e.getMessage();
			}
			if("50004".equals(ex.getCode())) {
				return error(ex.getCode(),ex.getMessage());
			}
			return error(msg);
		}
		return success("????????????!");
	}




	/**
	 * ??????
	 * @param mmap
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/Account/Lock",method = RequestMethod.GET)
	public String lock(ModelMap mmap) throws Exception {
		mmap.put("avatar", UserUtils.getUserInfo().getAvatar());
		LoginUserEntity loginUser = UserUtils.getUserInfo();
		loginUser.setLoginStatus(-1);
		UserUtils.reloadUser(loginUser);
		return "lock";
	}


	/**
	 * ??????????????????
	 * @param username ??????
	 * @param password ??????
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/Account/login", method = RequestMethod.POST)
	public ResponseData login(String username, String password, HttpServletRequest request) {

		if(ToolUtil.isNotEmpty(username) && ToolUtil.isNotEmpty(password)){
			LoginUserEntity loginUser = UserUtils.getUserInfo();
			try{
				String kg4 = super.getPara("kg4");
				String sign = super.getPara("sign");
				//????????????
				String _sign = SoftEncryption.genSM3Keys((kg4 + username + password).getBytes())
						.getStr("b64") ;
				if(!_sign.equals(sign)){
					//??????????????????
					throw new ServiceException("E0XA00011");
				}
				kg4 = SoftEncryption.decryptBySM2(Base64.decode(kg4),
						ConfigConstant.PRIVKEY).getStr("hex");
				username =new String(SoftEncryption.decryptBySM4(Base64.decode(username),
						HexUtil.decodeHex(kg4)).get("bytes",byte[].class)).trim();
				password =new String(SoftEncryption.decryptBySM4(Base64.decode(password),
						HexUtil.decodeHex(kg4)).get("bytes",byte[].class)).trim();

			}catch (Exception e){
				throw new RxcException("????????????,????????????","50004");
			}
			if(loginUser.getUsername().equals(username)){
				//??????????????????????????????
				Integer number = redisUtil.get(RedisKeys.getUserLoginKey(username),Integer.class);
				if( number != null  && number >= Global.getLoginNumCode()) {
					String kaptcha = UserUtils.getKaptcha(Constant.KAPTCHA_SESSION_KEY);
					String captcha = (String) ServletUtil.getRequest().getParameter("captcha");
					if (ToolUtil.isEmpty(captcha) || !captcha.equalsIgnoreCase(kaptcha)) {
						throw new RxcException(ToolUtil.message("sys.login.code.error"),"50004");
					}
				}
				SysUserEntity user = sysUserService.getById(loginUser.getId());
				password = UserUtils.sha256(password, user.getSalt());
				if (password.equals(user.getPassword())){
					loginUser.setLoginStatus(0);
					UserUtils.reloadUser(loginUser);
					UserUtils.getSession().setAttribute("__unlock","unlock");
					//??????????????????
					redisUtil.delete(RedisKeys.getUserLoginKey(username));
					return success().put("index", this.successUrl);
				}else{
					if(number == null) {
						number = 1;
						redisUtil.set(RedisKeys.getUserLoginKey(username), number, RedisUtil.MINUTE * Global.getLockTime());
					}else {
						number++;
						redisUtil.set(RedisKeys.getUserLoginKey(username), number, RedisUtil.MINUTE * Global.getLockTime());
					}
					AsyncManager.me().execute(AsyncFactory.recordLogininfor(username,loginUser.getCompId(),loginUser.getDeptId(), "50010","??????????????????????????????,????????????"+number+" ???!"));
					if(number >= Global.getLoginNumCode()) {
						throw new RxcException(ToolUtil.message("sys.login.password.retry.limit.count",Global.getLoginMaxCount()),"50004");
					}
					throw new RxcException(ToolUtil.message("sys.login.password.retry.limit.count",Global.getLoginMaxCount()),"50005");
				}
			}else {
				return error("???????????????");
			}
		}else{
			return error("????????????!");
		}

	}

	/**
	 * ??????
	 */
	@RequestMapping(value = "logout", method = RequestMethod.GET)
	public String logout() {
		log.info("logout get request RequestMapping");
		UserUtils.getSession().stop();
		UserUtils.logout();
		return REDIRECT + PropertiesUtils.getInstance().getProperty("shiro.loginUrl");
	}

	@ResponseBody
	@RequestMapping(value = "logout",method = RequestMethod.POST)
	public ResponseData loginOut() {
		log.info("logout get request ResponseData");
		UserUtils.getSession().stop();
		UserUtils.logout();
		return success("????????????");
	}

}
