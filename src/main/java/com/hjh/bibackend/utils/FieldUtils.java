package com.hjh.bibackend.utils;

import java.lang.reflect.Field;

public class FieldUtils {

    /**
     * 更新部分指定字段，仅限String
     * @param source
     * @param target
     * @param fields
     */
    public static void updatePartOfFields(Object source,Object target,String[] fields){
        // 判断字段数组是否为空
        if (fields == null || fields.length == 0) {
            return;
        }
        try {
            // 获取源对象的类
            Class<?> sourceClass = source.getClass();
            // 获取目标对象的类
            Class<?> targetClass = target.getClass();
            // 遍历字段名称数组
            for (String field : fields) {
                // 获取源对象的字段
                Field sourceField = sourceClass.getDeclaredField(field);
                // 设置字段可访问
                sourceField.setAccessible(true);
                // 获取目标对象的字段
                Field targetField = targetClass.getDeclaredField(field);
                // 设置字段可访问
                targetField.setAccessible(true);
                // 获取源对象的字段值
                Object sourceValue = sourceField.get(source);
                Object targetValue = targetField.get(target);
                // 将源对象的字段值设置到目标对象的字段上
                if (sourceValue == null) {
                    continue;
                }
                // 判断对象类型是否一致
                if (sourceValue.getClass() != targetValue.getClass()) {
                    continue;
                }
                //判断值是否一致
                if(targetValue == null){
                    targetField.set(target,sourceValue);
                }else if(targetValue.getClass() == String.class){
                    if(!sourceValue.equals(targetValue)) {
                        targetField.set(target, sourceValue);
                    }
                }else if(targetValue.getClass() == Long.class){
                    if(Long.valueOf(String.valueOf(sourceValue)).longValue() != Long.valueOf(String.valueOf(targetValue)).longValue()) {
                        targetField.set(target, sourceValue);
                    }
                }else if(targetValue.getClass() == Integer.class){
                    if(Integer.valueOf(String.valueOf(sourceValue)).intValue() != Integer.valueOf(String.valueOf(targetValue)).intValue()) {
                        targetField.set(target, sourceValue);
                    }
                }else if(targetValue.getClass() == Double.class){
                    if(Double.valueOf(String.valueOf(sourceValue)).doubleValue() != Double.valueOf(String.valueOf(targetValue)).doubleValue()) {
                        targetField.set(target, sourceValue);
                    }
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
