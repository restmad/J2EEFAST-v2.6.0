package com.j2eefast.common.db.collector;

import cn.hutool.core.date.SystemClock;
//import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import com.j2eefast.common.core.exception.RxcException;
import com.j2eefast.common.db.context.DataSourceContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.autoconfigure.SpringBootVFS;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import org.springframework.core.io.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;
/**
 * <p>mybatis????????????</p>
 * ??????copy???com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration
 * @author: zhouzhou
 * @date: 2020-04-15 14:12
 * @web: http://www.j2eefast.com
 * @version: 1.0.1
 */
@Data
@Slf4j
@Configuration
public class SqlSessionFactoryCreator {

	private final MybatisPlusProperties properties;

	private final Interceptor[] interceptors;

	@SuppressWarnings("rawtypes")
	private final TypeHandler[] typeHandlers;

	private final LanguageDriver[] languageDrivers;

	private final ResourceLoader resourceLoader;

	private final DatabaseIdProvider databaseIdProvider;

	private final List<ConfigurationCustomizer> configurationCustomizers;

	private final List<MybatisPlusPropertiesCustomizer> mybatisPlusPropertiesCustomizers;

	private final ApplicationContext applicationContext;

	public SqlSessionFactoryCreator(MybatisPlusProperties properties,
									ObjectProvider<Interceptor[]> interceptorsProvider,
									@SuppressWarnings("rawtypes") ObjectProvider<TypeHandler[]> typeHandlersProvider,
									ObjectProvider<LanguageDriver[]> languageDriversProvider,
									ResourceLoader resourceLoader,
									ObjectProvider<DatabaseIdProvider> databaseIdProvider,
									ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizersProvider,
									ObjectProvider<List<MybatisPlusPropertiesCustomizer>> mybatisPlusPropertiesCustomizerProvider,
									ApplicationContext applicationContext) {
		this.properties = properties;
		this.interceptors = interceptorsProvider.getIfAvailable();
		this.typeHandlers = typeHandlersProvider.getIfAvailable();
		this.languageDrivers = languageDriversProvider.getIfAvailable();
		this.resourceLoader = resourceLoader;
		this.databaseIdProvider = databaseIdProvider.getIfAvailable();
		this.configurationCustomizers = configurationCustomizersProvider.getIfAvailable();
		this.mybatisPlusPropertiesCustomizers = mybatisPlusPropertiesCustomizerProvider.getIfAvailable();
		this.applicationContext = applicationContext;
	}

	protected static Properties databaseTypeMappings = getDefaultDatabaseTypeMappings();

	public static final String DATABASE_TYPE_H2 = "h2";
	public static final String DATABASE_TYPE_HSQL = "hsql";
	public static final String DATABASE_TYPE_MYSQL = "mysql";
	public static final String DATABASE_TYPE_ORACLE = "oracle";
	public static final String DATABASE_TYPE_POSTGRES = "postgres";
	public static final String DATABASE_TYPE_MSSQL = "mssql";
	public static final String DATABASE_TYPE_DB2 = "db2";

	public static Properties getDefaultDatabaseTypeMappings() {
		Properties databaseTypeMappings = new Properties();
		databaseTypeMappings.setProperty("H2", DATABASE_TYPE_H2);
		databaseTypeMappings.setProperty("HSQL Database Engine", DATABASE_TYPE_HSQL);
		databaseTypeMappings.setProperty("MySQL", DATABASE_TYPE_MYSQL);
		databaseTypeMappings.setProperty("Oracle", DATABASE_TYPE_ORACLE);
		databaseTypeMappings.setProperty("PostgreSQL", DATABASE_TYPE_POSTGRES);
		databaseTypeMappings.setProperty("Microsoft SQL Server", DATABASE_TYPE_MSSQL);
		databaseTypeMappings.setProperty(DATABASE_TYPE_DB2, DATABASE_TYPE_DB2);
		databaseTypeMappings.setProperty("DB2", DATABASE_TYPE_DB2);
		databaseTypeMappings.setProperty("DB2/NT", DATABASE_TYPE_DB2);
		databaseTypeMappings.setProperty("DB2/NT64", DATABASE_TYPE_DB2);
		databaseTypeMappings.setProperty("DB2 UDP", DATABASE_TYPE_DB2);
		databaseTypeMappings.setProperty("DB2/LINUX", DATABASE_TYPE_DB2);
		databaseTypeMappings.setProperty("DB2/LINUX390", DATABASE_TYPE_DB2);
		databaseTypeMappings.setProperty("DB2/LINUXX8664", DATABASE_TYPE_DB2);
		databaseTypeMappings.setProperty("DB2/LINUXZ64", DATABASE_TYPE_DB2);
		databaseTypeMappings.setProperty("DB2/LINUXPPC64", DATABASE_TYPE_DB2);
		databaseTypeMappings.setProperty("DB2/400 SQL", DATABASE_TYPE_DB2);
		databaseTypeMappings.setProperty("DB2/6000", DATABASE_TYPE_DB2);
		databaseTypeMappings.setProperty("DB2 UDB iSeries", DATABASE_TYPE_DB2);
		databaseTypeMappings.setProperty("DB2/AIX64", DATABASE_TYPE_DB2);
		databaseTypeMappings.setProperty("DB2/HPUX", DATABASE_TYPE_DB2);
		databaseTypeMappings.setProperty("DB2/HP64", DATABASE_TYPE_DB2);
		databaseTypeMappings.setProperty("DB2/SUN", DATABASE_TYPE_DB2);
		databaseTypeMappings.setProperty("DB2/SUN64", DATABASE_TYPE_DB2);
		databaseTypeMappings.setProperty("DB2/PTX", DATABASE_TYPE_DB2);
		databaseTypeMappings.setProperty("DB2/2", DATABASE_TYPE_DB2);
		databaseTypeMappings.setProperty("DB2 UDB AS400", DATABASE_TYPE_DB2);
		return databaseTypeMappings;
	}

	/**
	 * ??????SqlSessionFactory
	 */
	public synchronized SqlSessionFactory createSqlSessionFactory(DataSource dataSource, String dbName){

		//????????????MybatisSqlSessionFactoryBean????????????????????????????????????????????????MybatisSqlSessionFactoryBean??????????????????????????????mapper??????????????????????????????mp????????????
		MybatisConfiguration originConfiguration =  properties.getConfiguration();
		GlobalConfig originGlobalConfig = properties.getGlobalConfig();

		//??????mapper?????????
		originGlobalConfig.setMapperRegistryCache(new ConcurrentSkipListSet<>());

		//??????????????????
		MybatisConfiguration mybatisConfiguration = new MybatisConfiguration();
		GlobalConfig globalConfig = GlobalConfigUtils.defaults();

		//??????????????????
		BeanUtil.copyProperties(originConfiguration, mybatisConfiguration, CopyOptions.create().ignoreError());
		BeanUtil.copyProperties(originGlobalConfig, globalConfig, CopyOptions.create().ignoreError());



		GlobalConfigUtils.setGlobalConfig(mybatisConfiguration, globalConfig);

		// TODO ?????? MybatisSqlSessionFactoryBean ????????? SqlSessionFactoryBean
		MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
		factory.setDataSource(dataSource);
		factory.setVfs(SpringBootVFS.class);
		factory.setConfiguration(mybatisConfiguration);
		if (StringUtils.hasText(this.properties.getConfigLocation())) {
			factory.setConfigLocation(this.resourceLoader.getResource(this.properties.getConfigLocation()));
		}
		Properties properties = null;
		if (this.properties.getConfigurationProperties() != null) {
			properties = new Properties(this.properties.getConfigurationProperties());
		}else{
			properties = new Properties();
		}
		if(dbName.equals(DataSourceContext.FLOWABLE_DATASOURCE_NAME)){
			String databaseType = initDatabaseType(dataSource);
			properties.put("prefix", "");
			properties.put("blobType", "BLOB");
			properties.put("boolValue", "TRUE");
			try {
				properties.load(this.getClass().getClassLoader().getResourceAsStream("org/flowable/db/properties/" + databaseType + ".properties"));
			} catch (Exception e) {
				log.error("??????????????????!?????????????????????????????????,???????????????????????????",e);
			}
			factory.setConfigurationProperties(properties);
		}else{
			factory.setConfigurationProperties(properties);
		}
		if (!ObjectUtils.isEmpty(this.interceptors)) {
			factory.setPlugins(this.interceptors);
		}
		if (this.databaseIdProvider != null) {
			factory.setDatabaseIdProvider(this.databaseIdProvider);
		}else {
			DatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
			Properties p = new Properties();
			p.setProperty("Oracle", "oracle");
			p.setProperty("MySQL", "mysql");
			p.setProperty("PostgreSQL", "postgresql");
			p.setProperty("DB2", "db2");
			p.setProperty("SQL Server", "sqlserver");
			databaseIdProvider.setProperties(p);
			factory.setDatabaseIdProvider(databaseIdProvider);
		}
		if (StringUtils.hasLength(this.properties.getTypeAliasesPackage())) {
			factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
		}
		if (this.properties.getTypeAliasesSuperType() != null) {
			factory.setTypeAliasesSuperType(this.properties.getTypeAliasesSuperType());
		}
		if (StringUtils.hasLength(this.properties.getTypeHandlersPackage())) {
			factory.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
		}
		if (!ObjectUtils.isEmpty(this.typeHandlers)) {
			factory.setTypeHandlers(this.typeHandlers);
		}

		if (!ObjectUtils.isEmpty(this.properties.resolveMapperLocations())) {
			Resource[] resource = this.properties.resolveMapperLocations();
			Resource[] resource1 = new Resource[0];
			Resource[] resource2 = new Resource[0];
			if(dbName.equals(DataSourceContext.FLOWABLE_DATASOURCE_NAME)){
				Resource[] resource3 = new Resource[0];
				try {
					resource1 = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).
							getResources("classpath*:/META-INF/modeler-mybatis-mappings/*.xml");
//					resource2  = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).
//							getResources("classpath*:mapper/bpm/*.xml");
//					resource3  = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).
//							getResources("classpath*:mapper/generator/*.xml");
//					Resource[] mapper = ArrayUtils.addAll(resource1, resource2);
//					Resource[] mapperLocations = ArrayUtils.addAll(mapper,resource3);
					Resource[] mapperLocations = ArrayUtils.addAll(resource1, resource);
					factory.setMapperLocations(mapperLocations);
				} catch (IOException e) {
					log.error("??????");
				}
			}else{
				factory.setMapperLocations(resource);
			}
			DataSourceContext.addMapperLocations(dbName,resource);
			DataSourceContext.addBeforeTime(dbName, SystemClock.now());
			//
		}

		// TODO ??????????????????????????????(??????????????????????????????mybatis??????,????????????????????????)
		Class<? extends LanguageDriver> defaultLanguageDriver = this.properties.getDefaultScriptingLanguageDriver();
		if (!ObjectUtils.isEmpty(this.languageDrivers)) {
			factory.setScriptingLanguageDrivers(this.languageDrivers);
		}
		Optional.ofNullable(defaultLanguageDriver).ifPresent(factory::setDefaultScriptingLanguageDriver);

		// TODO ??????????????????
		if (StringUtils.hasLength(this.properties.getTypeEnumsPackage())) {
			factory.setTypeEnumsPackage(this.properties.getTypeEnumsPackage());
		}
		// TODO ???????????????
		this.getBeanThen(MetaObjectHandler.class, globalConfig::setMetaObjectHandler);

		// TODO ?????????????????????
		this.getBeansThen(IKeyGenerator.class, i -> globalConfig.getDbConfig().setKeyGenerators(i));
		// TODO ??????sql?????????
		this.getBeanThen(ISqlInjector.class, globalConfig::setSqlInjector);
		// TODO ??????ID?????????
		this.getBeanThen(IdentifierGenerator.class, globalConfig::setIdentifierGenerator);
		// TODO ?????? GlobalConfig ??? MybatisSqlSessionFactoryBean
		factory.setGlobalConfig(globalConfig);

		try {
			return factory.getObject();
		} catch (Exception e) {
			log.error("?????????SqlSessionFactory?????????", e);
			throw new RxcException("?????????SqlSessionFactory?????????","60001");
		}
	}

	/**
	 * ??????spring???????????????????????????bean,??????????????????
	 *
	 * @param clazz    class
	 * @param consumer ??????
	 * @param <T>      ??????
	 */
	private <T> void getBeanThen(Class<T> clazz, Consumer<T> consumer) {
		if (this.applicationContext.getBeanNamesForType(clazz, false, false).length > 0) {
			consumer.accept(this.applicationContext.getBean(clazz));
		}
	}

	/**
	 * ??????spring???????????????????????????bean,??????????????????
	 *
	 * @param clazz    class
	 * @param consumer ??????
	 * @param <T>      ??????
	 */
	private <T> void getBeansThen(Class<T> clazz, Consumer<List<T>> consumer) {
		if (this.applicationContext.getBeanNamesForType(clazz, false, false).length > 0) {
			final Map<String, T> beansOfType = this.applicationContext.getBeansOfType(clazz);
			List<T> clazzList = new ArrayList<>();
			beansOfType.forEach((k, v) -> clazzList.add(v));
			consumer.accept(clazzList);
		}
	}

	protected String initDatabaseType(DataSource dataSource) {
		String databaseType = null;
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			String databaseProductName = databaseMetaData.getDatabaseProductName();
			log.info("database product name: '{}'", databaseProductName);
			databaseType = databaseTypeMappings.getProperty(databaseProductName);
			if (databaseType == null) {
				throw new RuntimeException("couldn't deduct database type from database product name '" + databaseProductName + "'");
			}
			log.info("using database type: {}", databaseType);

		} catch (SQLException e) {
			log.error("Exception while initializing Database connection", e);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				log.error("Exception while closing the Database connection", e);
			}
		}

		return databaseType;
	}
}
