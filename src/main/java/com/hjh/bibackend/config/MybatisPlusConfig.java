package com.hjh.bibackend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * mybatisPlusInterceptor的拦截器，并为其添加了两个内部拦截器：
 * PaginationInnerInterceptor和OptimisticLockerInnerInterceptor。
 * 这些拦截器将在执行 MyBatis Plus 操作时被调用，以处理分页和乐观锁相关的逻辑。
 * */

@Configuration
@EnableTransactionManagement
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        //创建拦截器
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        //优化 SQL 语句中的连接操作，以提高查询性能。
        paginationInnerInterceptor.setOptimizeJoin(true);
        //拦截器当前使用的数据库类型是 MySQL。
        paginationInnerInterceptor.setDbType(DbType.MYSQL);
        //将启用分页查询的溢出处理，当结果集超过分页限制时，将返回部分数据而不是抛出异常
        paginationInnerInterceptor.setOverflow(true);
        //这意味着在执行 MyBatis Plus 操作时，PaginationInnerInterceptor将被调用，以处理分页相关的逻辑。
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        OptimisticLockerInnerInterceptor optimisticLockerInnerInterceptor = new OptimisticLockerInnerInterceptor();
        //这意味着在执行 MyBatis Plus 操作时，OptimisticLockerInnerInterceptor将被调用，以处理乐观锁相关的逻辑。
        interceptor.addInnerInterceptor(optimisticLockerInnerInterceptor);
        return interceptor;
    }
}
