package com.example.common.config.Mybatis;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.common.config.Security.PermissionService;
import com.example.common.model.PageDTO;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;


/**
 * @author huanghongjia
 * @Intercepts: 这是一个注解，用于标记一个类为 MyBatis 拦截器，并指定该拦截器要拦截的方法。
 *
 * @Signature: 这个注解用于描述要拦截的方法的详细信息。
 *
 *  type: 指定要拦截的类，这里是 Executor.class，表示拦截执行 SQL 的类。
 *  method: 指定要拦截的方法名，这里是 query，表示拦截查询操作。
 *  args: 指定方法的参数类型，这里是一个数组，包含了该方法的参数类型：
 *  MappedStatement.class: 表示 SQL 映射语句的描述。
 *  Object.class: 表示查询参数。
 *  RowBounds.class: 表示分页信息。
 *  ResultHandler.class: 表示结果处理器。
 *  CacheKey.class: 表示缓存键。
 *  BoundSql.class: 表示绑定的 SQL 语句。
 */
@Component
@Intercepts(
        {
                @Signature(
                        type = Executor.class,
                        method = "query",
                        args = {
                                MappedStatement.class,
                                Object.class,
                                RowBounds.class,
                                ResultHandler.class,
                                CacheKey.class,
                                BoundSql.class
                        }),
        }
)
public class DataScopeInterceptor implements Interceptor {
    @Resource
    PermissionService permissionService;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        Object parameter = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler resultHandler = (ResultHandler) args[3];
        Executor executor = (Executor) invocation.getTarget();
        CacheKey cacheKey;
        BoundSql boundSql;


        //由于逻辑关系，只会进入一次
        if (args.length == 4) {
            //4 个参数时
            boundSql = mappedStatement.getBoundSql(parameter);
//            cacheKey = executor.createCacheKey(mappedStatement, parameter, rowBounds, boundSql);
        } else {
            //6 个参数时
//            cacheKey = (CacheKey) args[4];
            boundSql = (BoundSql) args[5];
        }

            //获取到数据权限sql语句
        Class<?> clazz = permissionService.getContext().getClazz();
        // 获取并调用方法
        Method method = clazz.getMethod(permissionService.getContext().getCallMethod());
        Object invoke = method.invoke(clazz.newInstance());

        //id为执行的mapper方法的全路径名，如com.metro.dao.UserMapper.insertUser
        String id = mappedStatement.getId();
        String sql = boundSql.getSql();
        // 检查 SQL 是否已经被处理
        if (sql.contains("/* DataScope Processed */")) {
            return invocation.proceed(); // 如果已经处理过，直接执行
        }
        System.out.println(sql);
        String newSql = null;
        if(sql.contains("@DataScope")){
            if (sql.toLowerCase().contains("where")) {
                newSql = sql.replaceAll("@DataScope", String.format("%s", "and "+invoke.toString()));
            } else {
                newSql = sql.replaceAll("@DataScope", String.format("%s", "where "+invoke.toString()));
            }
            newSql = newSql + " /* DataScope Processed */";
            //通过反射修改boundSql对象的sql语句
            Field field = boundSql.getClass().getDeclaredField("sql");
            field.setAccessible(true);
            field.set(boundSql, newSql);// 获取修改后的 SQL
        }else{
            if (id.contains("com.example.system.mapper.SystemUserMapper.getUserPage")) {
                Expression sqlSegmentExpression = null;
                Statement sm = CCJSqlParserUtil.parse(sql);
                Select select = (Select) sm;
                SelectBody selectBody = select.getSelectBody();
                PlainSelect plainSelect = (PlainSelect) selectBody;
                sqlSegmentExpression = CCJSqlParserUtil.parseCondExpression(invoke.toString());
                if (plainSelect.getWhere() == null) {
                    plainSelect.setWhere(sqlSegmentExpression);
                } else {
                    AndExpression andExpression = new AndExpression(plainSelect.getWhere(), sqlSegmentExpression);
                    plainSelect.setWhere(andExpression);
                }
                newSql = select + " /* DataScope Processed */";
                //通过反射修改boundSql对象的sql语句
                Field field = boundSql.getClass().getDeclaredField("sql");
                field.setAccessible(true);
                field.set(boundSql, newSql);// 获取修改后的 SQL
            }
        }

        return invocation.proceed();
        //执行修改后的sql语句
//        return executor.query(mappedStatement, parameter, rowBounds, resultHandler, cacheKey, boundSql);
    }

    @Override
    public Object plugin(Object target) {
        return Interceptor.super.plugin(target);
    }

    @Override
    public void setProperties(Properties properties) {
        Interceptor.super.setProperties(properties);
    }

    /**
     * 处理数据权限
     *
     * @return
     */
    public String handleDataScope() {
        return "1 = 1";
    }
}


//@Intercepts({
//        @Signature(
//        type = StatementHandler.class,
//        method = "prepare",
//        args = {Connection.class, Integer.class}
//)})
//public class DataScopeInterceptor implements Interceptor {
//
//    @Resource
//    PermissionService permissionService;
//    @Override
//    public Object intercept(Invocation invocation) throws Throwable {
//        // 1. 获取 StatementHandler 对象也就是执行语句
//        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
//                MetaObject metaObject = MetaObject.forObject(statementHandler, SystemMetaObject.DEFAULT_OBJECT_FACTORY,
//                SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY,
//                new DefaultReflectorFactory());
//
//        Class<?> clazz = permissionService.getContext().getClazz();
//        // 获取并调用方法
//        Method method = clazz.getMethod(permissionService.getContext().getCallMethod());
//        Object invoke = method.invoke(clazz.newInstance());
//        Expression sqlSegmentExpression = null;
//
//        // 5. 获取包含原始 sql 语句的 BoundSql 对象
//        BoundSql boundSql = statementHandler.getBoundSql();
//
//        String sql = boundSql.getSql();
//        Statement sm = CCJSqlParserUtil.parse(sql);
//        Select select = (Select) sm;
//        SelectBody selectBody = select.getSelectBody();
//        PlainSelect plainSelect = (PlainSelect) selectBody;
//        sqlSegmentExpression = CCJSqlParserUtil.parseCondExpression(invoke.toString());
//        if(plainSelect.getWhere() == null){
//            plainSelect.setWhere(sqlSegmentExpression);
//        }else{
//            AndExpression andExpression = new AndExpression(plainSelect.getWhere(), sqlSegmentExpression);
//            plainSelect.setWhere(andExpression);
//        }
//        // 5. 更新 BoundSql 的 SQL
//        String newSql = select.toString(); // 获取修改后的 SQL
//        // 使用反射更新 BoundSql 的 SQL
//        metaObject.setValue("boundSql.sql", newSql);
//        // 拦截方法
//        String mSql = null;
//        mSql = sql.replaceAll("@DataScope", String.format("%s", "and 1=1"));
//        System.out.println(mSql);
//        if (StringUtils.isNotBlank(mSql)) {
//            // 8. 对 BoundSql 对象通过反射修改 SQL 语句。
//            Field field = boundSql.getClass().getDeclaredField("sql");
//            field.setAccessible(true);
//            field.set(boundSql, mSql);
//        }
//        // 9. 执行修改后的 SQL 语句。
//        return invocation.proceed();
//    }
//
//
//    @Override
//    public Object plugin(Object target) {
//        return Interceptor.super.plugin(target);
//    }
//
//    @Override
//    public void setProperties(Properties properties) {
//        Interceptor.super.setProperties(properties);
//    }
//}

//@Intercepts({
//        @Signature(type = ParameterHandler.class, method = "setParameters", args = PreparedStatement.class)
//})
//public class DataScopeInterceptor implements Interceptor {
//
//    private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
//    private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
//    private static final ReflectorFactory REFLECTOR_FACTORY = new DefaultReflectorFactory();
//
//    @Override
//    public Object intercept(Invocation invocation) throws Throwable {
//        // 获取拦截器拦截的设置参数对象DefaultParameterHandler
//        ParameterHandler parameterHandler = (ParameterHandler) invocation.getTarget();
//
//        // 通过mybatis的反射来获取对应的值
//        MetaObject metaResultSetHandler = MetaObject.forObject(parameterHandler, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, REFLECTOR_FACTORY);
//        Object parameterObject = metaResultSetHandler.getValue("parameterObject");
//        // 反射获取参数对象
//        MetaObject param = MetaObject.forObject(parameterObject, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, REFLECTOR_FACTORY);
////        PageDTO param1 = (PageDTO) param.getValue("PageDTO");
////        // 修改 PageDTO.sql 的值
////        param1.setSql("and user_name='demo'");  // 修改值
////        param.setValue("PageDTO", param1);  // 更新参数对象
//
//        System.out.println(parameterObject);
//        // 回写parameterObject对象
////        metaResultSetHandler.setValue("parameterObject", parameterObject);
//        return invocation.proceed();
//    }
//
//    @Override
//    public Object plugin(Object target) {
//        return Interceptor.super.plugin(target);
//    }
//
//    @Override
//    public void setProperties(Properties properties) {
//        Interceptor.super.setProperties(properties);
//    }
//}
