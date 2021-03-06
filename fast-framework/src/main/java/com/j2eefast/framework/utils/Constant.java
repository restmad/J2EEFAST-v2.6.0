/*
 * All content copyright http://www.j2eefast.com, unless
 * otherwise indicated. All rights reserved.
 * No deletion without permission
 */

package com.j2eefast.framework.utils;

import com.google.common.collect.Lists;
import com.j2eefast.framework.sys.entity.SysDictDataEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 常量
 * @author huanzhou
 */
public class Constant {

	public static final	String KAPTCHA_SESSION_KEY = "KAPTCHA_SESSION_KEY";
	/** 超级管理员ID */
	public static final Long SUPER_ADMIN = 1L;
	public static final String SU_ADMIN = "ADMIN";
	/** 管理员租户**/
	public static final String SUPER_TENANT = "";
	public static final String TENANT_PARAMETER = "tenantId";


	//0=未设置, 1=仅本人数据, 2=本公司数据,3=本部门及以下数据权限,4=本部门数据权限, 5=自定义数据权限  6 所有数据权限
	/**
	 * 未设置
	 */
	public static final String DATA_SCOPE_NULL = "0";
	public static final String DATA_SCOPE_ALL = "6";
	/**
	 * 系统登录默认风格
	 */
	public static final String ADMIN_LTE = "Admin-LTE";
	/**
	 * 系统默认值0
	 */
	public static final String SYS_DEFAULT_VALUE_ZERO = "0";

	public static final String SYS_DEFAULT_VALUE_YES = "Y";

	public static final String SYS_DEFAULT_VALUE_NO = "N";
	/**
	 * 系统默认值1
	 */
	public static final String SYS_DEFAULT_VALUE_ONE = "1";
	/**
	 * 系统默认值2
	 */
	public static final String SYS_DEFAULT_VALUE_TWO = "2";
	/**
	 * 自定数据权限
	 */
	public static final String DATA_SCOPE_CUSTOM = "5";
	/**
	 * 部门数据权限
	 */
	public static final String DATA_SCOPE_DEPT = "4";
	/**
	 * 部门及以下数据权限
	 */
	public static final String DATA_SCOPE_DEPT_AND_CHILD = "3";
	/**
	 * 仅本人数据权限
	 */
	public static final String DATA_SCOPE_SELF = "1";
	/**
	 * 仅公司数据权限
	 */
	public static final String DATA_SCOPE_COMP = "2";
	/** 数据权限过滤 */
	public static final String SQL_FILTER = "sql_filter";
	/** 请求权限字符Key*/
	public static final String REQUIRES_PERMISSIONS = "__REQUIRESPERMISSIONS";
	public static final String BANNER = "spring.main.banner-mode";
	public static final String FALSE = "false";
	/** 公司正常状态 */
	public static final String COMP_NORMAL = "0";
	public static final String DEPT_NORMAL = "0";
	/** 登录名称是否唯一的返回结果码 */
	public final static String USER_NAME_UNIQUE = "true";
	public final static String USER_NAME_NOT_UNIQUE = "false";
	/**
	 * 资源映射路径 前缀
	 */
//	public static final String RESOURCE_URLPREFIX = "/profile";
	public static final String BASE_WEB_HTML = "templates/";
	/**
     * 手机号码格式限制
     */
    public static final String MOBILE_PHONE_NUMBER_PATTERN = "^0{0,1}(13[0-9]|17[0-9]|19[0-9]|16[0-9]|15[0-9]|14[0-9]|18[0-9])[0-9]{8}$";
    /**
     * 邮箱格式限制
     */
    public static final String EMAIL_PATTERN = "^((([a-z]|\\d|[!#\\$%&'\\*\\+\\-\\/=\\?\\^_`{\\|}~]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\"
    		+ "uFFEF])+(\\.([a-z]|\\d|[!#\\$%&'\\*\\+\\-\\/=\\?\\^_`{\\|}~]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])+)*)|((\\x22)((((\\"
    		+ "x20|\\x09)*(\\x0d\\x0a))?(\\x20|\\x09)+)?(([\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x7f]|\\x21|[\\x23-\\x5b]|[\\x5d-\\x7e]|"
    		+ "[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])|(\\\\([\\x01-\\x09\\x0b\\x0c\\x0d-\\x7f]|[\\u00A0-\\uD7FF\\uF900-\\uFD"
    		+ "CF\\uFDF0-\\uFFEF]))))*(((\\x20|\\x09)*(\\x0d\\x0a))?(\\x20|\\x09)+)?(\\x22)))@((([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-"
    		+ "\\uFDCF\\uFDF0-\\uFFEF])|(([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])([a-z]|\\d|-|\\.|_|~|[\\u00A0-\\u"
    		+ "D7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])*([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])))\\.)+(([a-z]|[\\u00A"
    		+ "0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])|(([a-z]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])([a-z]|\\d|-|\\.|_"
    		+ "|~|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])*([a-z]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])))\\.?";

//	/** 添加的参数 登陆错误次数*/ 后期添加
//	public static final int LOGIN_NUM_MAX = 3;

	/**
	 * 系统缓存字典
	 */
	public static Map<String, List<SysDictDataEntity>> SYS_DICT = new HashMap<>();

	/**
	 * 菜单类型
	 */
	public enum MenuType {
		/**
		 * 目录
		 */
		CATALOG(0),
		/**
		 * 菜单
		 */
		MENU(1),
		/**
		 * 按钮
		 */
		BUTTON(2);

		private int value;

		MenuType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	/**
	 * 定时任务状态
	 */
	public enum ScheduleStatus {
		/**
		 * 正常
		 */
		NORMAL("0"),
		/**
		 * 暂停
		 */
		PAUSE("1");

		private String value;

		ScheduleStatus(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	/**
	 * 云服务商
	 */
	public enum CloudService {
		
		/**
		 * 本地
		 */
		LOCAL(0),
		
		/**
		 * 阿里云
		 */
		ALIYUN(1),
		/**
		 * 腾讯云
		 */
		QCLOUD(2),

		/**
		 * 七牛云
		 */
		QINIU(3),

		/**
		 * FTP
		 */
		FTP(4);

		private int value;

		CloudService(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

}
