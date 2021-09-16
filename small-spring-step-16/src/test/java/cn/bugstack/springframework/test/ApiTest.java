package cn.bugstack.springframework.test;

import cn.bugstack.springframework.context.support.ClassPathXmlApplicationContext;
import cn.bugstack.springframework.test.bean.Husband;
import cn.bugstack.springframework.test.bean.Wife;
import org.junit.Test;

/**
 * 解决循坏依赖：DefaultSingletonBeanRegistry的getSingleton方法处理的三级缓存
 */
public class ApiTest {

    @Test
    public void test_circular() {
        // 基于缓存如何解决而循环依赖问题
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");

        Husband husband = applicationContext.getBean("husband", Husband.class);
        Wife wife = applicationContext.getBean("wife", Wife.class);
        System.out.println("老公的媳妇：" + husband.queryWife());
        System.out.println("媳妇的老公：" + wife.queryHusband());
    }

}
