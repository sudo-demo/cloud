package com.example.common.util;


//import com.cobazaar.exception.BizException;

/**
 * sql操作工具类
 * 
 * @author meng
 */
public class SqlUtil
{
    /**
     * 仅支持字母、数字、下划线、空格、逗号、小数点（支持多个字段排序）
     */
    public static String SQL_PATTERN = "[a-zA-Z0-9_\\ \\,\\.]+";

    private static final String SQL_REGEX = "'|%|--|insert|delete|select|count|group|union|drop|truncate|alter|grant|execute|exec|xp_cmdshell|call|declare|sql";

    /**
     * 检查字符，防止注入绕过
     */
    public static String escapeOrderBySql(String value)
    {
        if (StringUtil.isNotEmpty(value) && !isValidOrderBySql(value))
        {
//            throw new BizException("参数不符合规范，不能进行查询");
        }
        return value;
    }

    /**
     * 验证 order by 语法是否符合规范
     */
    public static boolean isValidOrderBySql(String value)
    {
        return value.matches(SQL_PATTERN);
    }

    public static String filter(String param) {
        return param == null ? null : param.replaceAll("(?i)'|%|--|insert|delete|select|count|group|union|drop|truncate|alter|grant|execute|exec|xp_cmdshell|call|declare|sql", "");
    }
}
