package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

//切面，拦截公共字段自动填充
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut (){ }
        /*
        前置通知
         */
        @Before("autoFillPointCut()")
        public void autoFill(JoinPoint joinPoint) {
            log.info("公共字段自动填充");
            // 获取当前被拦截的方法上的数据库操作类型
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
            OperationType operationType = autoFill.value();
            // 获取当前被拦截的方法参数列表--实体对象
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) {
                return;
            }
            Object entity = args[0];
            //准备赋值的数据
            LocalDateTime now = LocalDateTime.now();
            long currentId = Thread.currentThread().getId();
            //为对应的属性通过反射来赋值
            if (operationType == OperationType.INSERT) {
                try {
                    Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                    Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                    Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                    Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                    // 通过反射为实体对象赋值
                    setUpdateTime.invoke(entity, now);
                    setUpdateUser.invoke(entity, currentId);
                    setCreateTime.invoke(entity, now);
                    setCreateUser.invoke(entity, currentId);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (operationType == OperationType.UPDATE) {
                try {
                    Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                    Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                    // 通过反射为实体对象赋值
                    setUpdateTime.invoke(entity, now);
                    setUpdateUser.invoke(entity, currentId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
}
