package com.sky.aop;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    //我们在切面类做的工作只是把数据自动填充到对象的属性中，但是当我们把对象的数据插入到数据库时，需要在sql中写上creat_Time....,不然插不进去

    @Around("@annotation(autoFill)")
    public Object autoFill(ProceedingJoinPoint joinPoint, AutoFill autoFill) throws Throwable {
        log.info("开始自动填充数据");

        // 1. 拿到方法参数，第一个就是 entity 对象
        Object[] args = joinPoint.getArgs();
        Object entity = args[0];

        // 2. 拿到操作类型 INSERT / UPDATE
        //直接用在连接点方法所获取到的注解，可以获取到其中的属性
        OperationType operationType = autoFill.value();

        // 3. 拿到当前时间 + 当前用户 ID
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        // 4. 反射调用 set 方法填值
        Class<?> clazz = entity.getClass();
        if (operationType == OperationType.INSERT) {
            Method setCreateTime = clazz.getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            //getMethod方法返回的是一个Method对象，使用invoke动态调用这方法时，要给他传递实例对象，也就是说是哪个对象现在调用的这个方法
            //我们传递的entity对象，就是我们连接点方法的第一个参数，也就是我们新增的员工对象
            //如果不写对象的话，只调用这个方法，那么他不知道具体需要改变那个实例，也就改变不了实例对象的createTime属性
            setCreateTime.invoke(entity, now);

            Method setCreateUser = clazz.getMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            setCreateUser.invoke(entity, currentId);
        }

        Method setUpdateTime = clazz.getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
        setUpdateTime.invoke(entity, now);
        Method setUpdateUser = clazz.getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
        setUpdateUser.invoke(entity, currentId);

        // 5. 放行
        return joinPoint.proceed();
    }
}
//1、在形参中写我们自定义的注释，再配合上@annotation() 注解，就可以直接在方法中使用注释中的属性
//2、我们要把自定义的注解加在Mapper层，因为这样子，我们获取到的连接点的参数才是entity中的对象，这些对象才有createTime....等等方法
//这样子我们后续才可以使用反射来调用方法，如果写在Service层，那么获取到的是前端传递的DTO层中的对象，没有createTime....等等方法
//3、使用getMethod、invoke等方法实现反射
