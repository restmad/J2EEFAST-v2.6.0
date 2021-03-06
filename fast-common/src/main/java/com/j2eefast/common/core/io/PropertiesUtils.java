/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 */
package com.j2eefast.common.core.io;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.j2eefast.common.core.constants.ConfigConstant;
import com.j2eefast.common.core.crypto.SM4;
import com.j2eefast.common.core.crypto.SoftEncryption;
import com.j2eefast.common.core.utils.HexUtil;
import com.j2eefast.common.core.utils.YamlParsing;
import lombok.extern.slf4j.Slf4j;
import com.j2eefast.common.core.utils.ToolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

/**
 * Properties工具类， 可载入多个properties、yml文件，
 * 相同的属性在最后载入的文件中的值将会覆盖之前的值，
 * @author ThinkGem
 * @version 2017-12-30
 * J2eeFAST 二次修改
 */
@Slf4j
public class PropertiesUtils {

	/**
	 * 系统默认加载文件(可覆盖配置文件参数)
	 */
	public static final String[] DEFAULT_CONFIG_FILE = new String[]{
			"classpath:config/bootstrap.yml", "classpath:bootstrap.yml",
			"classpath:config/application.yml", "classpath:application.yml"};

	private final Set<String>					configSet 					=  new LinkedHashSet<>();
	private final Properties 					properties 					= new Properties();
	private static Environment 					environment;
	
	/**
	 * 当前类的实例持有者（静态内部类，延迟加载，懒汉式，线程安全的单例模式）
	 */
	private static final class PropertiesLoaderHolder {
		private static PropertiesUtils INSTANCE;
		static {
			ToolUtil.getFastServerInfos();
			releadInstance();
		}
		public static void releadInstance(){
			// 获取平台及模块相关的配置文件
			Set<String> configSet = new LinkedHashSet<>();
			Resource[] resources = ResourceUtils.getResources("classpath*:/config/fast-*.*");
			for(Resource resource : resources){
				configSet.add("classpath:config/"+resource.getFilename());
			}
			// 获取全局设置默认的配置文件（以下是支持环境配置的属性文件）
			Set<String> set = new LinkedHashSet<>();
			for (String configFile : DEFAULT_CONFIG_FILE){
				set.add(configFile);
			}
			// 获取 spring.config.location 外部自定义的配置文件
			String customConfigs = System.getProperty("spring.config.location");
			if (StringUtils.isNotBlank(customConfigs)){
				for (String customConfig : StringUtils.split(customConfigs, ",")){
					if (!customConfig.contains("$")){
						customConfig = org.springframework.util.StringUtils.cleanPath(customConfig);
						if (!ResourceUtils.isUrl(customConfig)){
							customConfig = ResourceUtils.FILE_URL_PREFIX + customConfig;
						}
					}
					set.add(customConfig);
				}
			}
			// 获取 spring.profiles.active 活动环境名称的配置文件
			String[] configFiles = set.toArray(new String[set.size()]);
			String profiles = System.getProperty("spring.profiles.active");
			if (StringUtils.isBlank(profiles)){
				PropertiesUtils propsTemp = new PropertiesUtils(configFiles);
				profiles = propsTemp.getProperty("spring.profiles.active");
			}
			for (String location : configFiles){
				configSet.add(location);
				if (StringUtils.isNotBlank(profiles)){
					if (location.endsWith(".properties")){
						configSet.add(StringUtils.substringBeforeLast(location, ".properties")
								+ "-" + profiles + ".properties");
					}else if (location.endsWith(".yml")){
						configSet.add(StringUtils.substringBeforeLast(location, ".yml")
								+ "-" + profiles + ".yml");
					}
				}
			}
			
			configFiles = configSet.toArray(new String[configSet.size()]);
			
			INSTANCE = new PropertiesUtils(configFiles);

			// J2eeFAST 赋值系统加密数据
			if(ToolUtil.isEmpty(INSTANCE.get("machineCode"))){
				INSTANCE.put("machineCode",HexUtil.encodeHexStr(SM4.encryptData_ECB(HexUtil.decodeHex
						(ConfigConstant.FAST_OS_SN),ConfigConstant.FAST_KEY)));
				INSTANCE.put("checkCode",HexUtil.encodeHexStr(SM4.encryptData_ECB(HexUtil.decodeHex
						(ConfigConstant.FAST_OS_SN),ConfigConstant.FAST_VERIFY_KEY)).substring(0,6));
				INSTANCE.put("pCIp",StrUtil.cleanBlank(StrUtil.join(StrUtil.COMMA,ConfigConstant.FAST_IPS)));

				if(ToolUtil.isEmpty(INSTANCE.get(ConfigConstant.SYS_KEY_PUBKEY))
						|| ToolUtil.isEmpty(INSTANCE.get(ConfigConstant.SYS_KEY_PRIKEY))){
					JSONObject object = SoftEncryption.genSM2Keys();
					// J2eeFAST 2021年09月05日 21:19:13
					// 系统生成 公私密钥对 --> 目前系统登录加密使用-> 后期可以扩展到 前端重要数据加密使用
					ConfigConstant.PRIVKEY = object.get("priKeyByte",byte[].class);
					ConfigConstant.PUBKEY = Base64.encode(object.get("pubKeyByte",byte[].class));
				}else{
					ConfigConstant.PUBKEY = (String) INSTANCE.get(ConfigConstant.SYS_KEY_PUBKEY);
					ConfigConstant.PRIVKEY = Base64.decode((String) INSTANCE.get(ConfigConstant.SYS_KEY_PRIKEY));
				}
			}

			ConfigConstant.DICT_TAG = RandomUtil.randomString(5);

			try{
				// J2eeFAST 2020-12-18 23:05:53 动态设置系统环境
				if(ToolUtil.isEmpty(INSTANCE.get("management.health.rabbit.enabled"))){
					INSTANCE.put("management.health.rabbit.enabled",INSTANCE.get(ConfigConstant.FLOWABLE_ENABLED_YML));
				}

				if(ToolUtil.isEmpty(INSTANCE.get("spring.jta.enabled")) && !(Boolean) INSTANCE.get(ConfigConstant.JTA_ENABLED_YML)){
					INSTANCE.put("spring.jta.enabled",false);
				}

				if(ToolUtil.isNotEmpty(INSTANCE.get(ConfigConstant.FLOWABLE_ENABLED_YML))
						&& (Boolean)INSTANCE.get(ConfigConstant.FLOWABLE_ENABLED_YML)
						&& ToolUtil.isEmpty(INSTANCE.get("flowable.common.app.idm-url"))){
					String path = (String) INSTANCE.get(ConfigConstant.SERVER_SERVLET_CONTEXTPATH);
					if(path.equals(StrUtil.SLASH)){
						path = path + "bpm";
					}else if(StrUtil.endWith(path,StrUtil.SLASH)){
						path = path + "bpm";
					}else{
						path = path + "/bpm";
					}
					INSTANCE.put("flowable.common.app.idm-url",path);
				}
			}catch (Exception e){}

		}
	}

	/**
	 * 载入多个文件，路径使用Spring Resource格式，相同的属性在最后载入的文件中的值将会覆盖之前的值。
	 */
	public PropertiesUtils(String... configFiles) {
		for (String location : configFiles) {
			try {
				Resource resource = ResourceUtils.getResource(location);
				if (resource.exists()){
        			if (location.endsWith(".properties")){
        				try{
        					InputStreamReader is = new InputStreamReader(resource.getInputStream(), "UTF-8");
	    					properties.load(is);
	    					configSet.add(location);
	        			} catch (IOException ex) {
	            			log.error("Load " + location + " failure. ", ex);
	        			}
        			}
        			else if (location.endsWith(".yml")){
        				//J2eeFAST 修改适配问题
						YamlParsing bean = new YamlParsing();
        				bean.setResources(resource);
        				for (Map.Entry<Object,Object> entry : bean.getObject().entrySet()){
        					properties.put(StringUtils.defaultString(String.valueOf(entry.getKey())),
									entry.getValue());
        					if(location.endsWith("fast-default.yml") &&
        							"version".equals(String.valueOf(entry.getKey()))) {
            					ConfigConstant.F_VSRION = (String) entry.getValue();
            				}
        				}
    					configSet.add(location);
        			}
				}
			} catch (Exception e) {
				System.err.println("系统配置文件书写异常:"+location);
				System.err.println(e);
			}
		}
	}
	
	/**
	 * 获取当前加载的属性文件
	 */
	public Set<String> getConfigSet() {
		return configSet;
	}
	
	/**
	 * 获取当前加载的属性数据
	 */
	public Properties getProperties() {
		return properties;
	}

	public void put(Object key, Object value){
		properties.put(key,value);
	}
	
	/**
	 * 当前类实例
	 */
	public static PropertiesUtils getInstance(){
		return PropertiesLoaderHolder.INSTANCE;
	}
	
	/**
	 * 重新加载实例（重新实例化，以重新加载属性文件数据）
	 */
	public static void releadInstance(){
		PropertiesLoaderHolder.releadInstance();
	}
	
	// 正则表达式预编译
	private static Pattern p1 = Pattern.compile("\\$\\{.*?\\}");


	public  Object get(String key){
      return properties.get(key);
	}

	/**
	 * 获取属性值，取不到从System.getProperty()获取，都取不到返回null
	 */
	public String getProperty(String key) {
		if (environment != null){
			String value = environment.getProperty(key);
			if (value != null){
				return value;
			}
		}
        String value = properties.getProperty(key);
        if (value != null){
	    	Matcher m = p1.matcher(value);
	        while(m.find()) {
	            String g = m.group();
	            String childKey = g.replaceAll("\\$\\{|\\}", "");
	            value = StringUtils.replace(value, g, getProperty(childKey));
	        }
	        return value;
	    }else{
	    	String systemProperty = System.getProperty(key);
			if (systemProperty != null) {
				return systemProperty;
			}
	    }
		return null;
	}

	/**
	 * 取出String类型的Property，但以System的Property优先，如果都为null则返回defaultValue值
	 */
	public String getProperty(String key, String defaultValue) {
		String value = getProperty(key);
		return value != null ? value : defaultValue;
	}
	
	/**
	 * 设置环境属性
	 * @param environment
	 */
	public static void setEnvironment(Environment environment) {
		PropertiesUtils.environment = environment;
	}
	
}
