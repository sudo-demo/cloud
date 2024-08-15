package com.example.common.util.bean;

/**
 * 
 */
public final class BeanUtils {

    public static <T> T client(Class<T> clazz) throws Exception {
        if (clazz == null) {
            throw new IllegalArgumentException("Class must not be null");
        }
        try {
            // 使用反射创建对象实例，并保持泛型类型
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            // 处理反射过程中可能抛出的异常
            throw new Exception("Failed to create instance of " + clazz.getName(), e);
        }
    }

}
