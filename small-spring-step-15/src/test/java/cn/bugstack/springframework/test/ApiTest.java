package cn.bugstack.springframework.test;

import cn.bugstack.springframework.context.support.ClassPathXmlApplicationContext;
import cn.bugstack.springframework.test.bean.IUserService;
import org.junit.Test;

/**
 * 给代理对象的属性设置值
 * 即解析填充完Bean的属性后 再返回代理。 所以代理逻辑实现是放在BeanPostProcessor：DefaultAdvisorAutoProxyCreator的
 * 方法 postProcessAfterInitialization ，即等bean初始化好后，再去判断是否要生成代理对象
 */
public class ApiTest {

    @Test
    public void test_autoProxy() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");
        IUserService userService = applicationContext.getBean("userService", IUserService.class);
        System.out.println("测试结果：" + userService.queryUserInfo());
    }

}
