package com.zdzc.collector.receiver.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.zdzc.collector.common.jfinal.Config;

import java.sql.Connection;

/**
 * 数据库连接池类
 * @Author liuwei
 * @Description
 * @Date 2018/12/21 10:53
 */
public class DbConnectionPool {

    private static DruidDataSource dataSource = null;

    public DbConnectionPool() {

    }

    /**
     * 连接池初始化
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/21 10:52
     */
    public void init () {
        dataSource = new DruidDataSource();
        //设置连接参数
        String url = Config.get("datasource.url");
        String driverClass = Config.get("datasource.driverClassName");
        String userName = Config.get("datasource.username");
        String pwd = Config.get("datasource.password");
        int initialSize = Config.getInt("datasource.initialSize");
        int minIdle = Config.getInt("datasource.minIdle");
        int maxActive = Config.getInt("datasource.maxActive");
        int removeAbandonedTimeout = Config.getInt("datasource.removeAbandoned.timeout");
        int maxWait = Config.getInt("datasource.maxWait");
        int timeBetweenEvictionMillis = Config.getInt("datasource.timeBetweenEvictionRunsMillis");
        String validationQuery = Config.get("datasource.validationQuery");
        dataSource.setUrl(url);
        dataSource.setDriverClassName(driverClass);
        dataSource.setUsername(userName);
        dataSource.setPassword(pwd);
        //配置初始化大小、最小、最大
        dataSource.setInitialSize(initialSize);
        dataSource.setMinIdle(minIdle);
        dataSource.setMaxActive(maxActive);
        //连接泄漏监测
        dataSource.setRemoveAbandoned(true);
        dataSource.setRemoveAbandonedTimeout(removeAbandonedTimeout);
        //配置获取连接等待超时的时间
        dataSource.setMaxWait(maxWait);
        //配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
        dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionMillis);
        //防止过期
        dataSource.setValidationQuery(validationQuery);
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(true);
    }

    /**
     * 获取一个数据库连接
     * @author liuwei
     * @return
     * @exception
     * @date 2018/12/21 10:52
     */
    public static Connection getConnect() throws Exception{

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
        } catch (Exception e) {

            throw e;

        }
        return connection;
    }

}
