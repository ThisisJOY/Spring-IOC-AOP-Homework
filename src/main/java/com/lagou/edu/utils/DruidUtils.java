package com.lagou.edu.utils;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * @author 应癫
 */
public class DruidUtils {

    private DruidUtils(){
    }

    private static DruidDataSource druidDataSource = new DruidDataSource();


    static {
        druidDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        druidDataSource.setUrl("jdbc:mysql:///bank");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("lyq520025");

    }

    public static DruidDataSource getInstance() {
        return druidDataSource;
    }

}
