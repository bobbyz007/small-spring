package cn.bugstack.springframework.beans.factory.support;

import cn.bugstack.springframework.beans.BeansException;
import cn.bugstack.springframework.beans.factory.DisposableBean;
import cn.bugstack.springframework.beans.factory.ObjectFactory;
import cn.bugstack.springframework.beans.factory.config.SingletonBeanRegistry;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 博客：https://bugstack.cn - 沉淀、分享、成长，让自己和他人都能有所收获！
 * 公众号：bugstack虫洞栈
 * Create by 小傅哥(fustack)
 */
public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {

    /**
     * Internal marker for a null singleton object:
     * used as marker value for concurrent Maps (which don't support null values).
     */
    protected static final Object NULL_OBJECT = new Object();

    // 一级缓存，普通对象
    /**
     * Cache of singleton objects: bean name --> bean instance
     */
    private Map<String, Object> singletonObjects = new ConcurrentHashMap<>();

    // 二级缓存，提前暴漏对象，没有完全实例化的对象
    /**
     * Cache of early singleton objects: bean name --> bean instance
     */
    protected final Map<String, Object> earlySingletonObjects = new HashMap<String, Object>();

    // 三级缓存，存放代理对象
    /**
     * Cache of singleton factories: bean name --> ObjectFactory
     */
    private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<String, ObjectFactory<?>>();

    private final Map<String, DisposableBean> disposableBeans = new LinkedHashMap<>();

    @Override
    public Object getSingleton(String beanName) {
        Object singletonObject = singletonObjects.get(beanName);
        if (null == singletonObject) {
            singletonObject = earlySingletonObjects.get(beanName);
            // 判断二级缓存中是否有对象，如果没有，则表示需要从三级缓存的factory中生成，
            if (null == singletonObject) {
                ObjectFactory<?> singletonFactory = singletonFactories.get(beanName);
                // 如果三级缓存的factory中也没有，则表示对应的beanname还没有创建，则在后面调用createBean方法时会调用
                // this.addSingletonFactory 方法加入到singletonFactories 中
                // 比如对于 A与B的循环依赖，流程时序如下
                // 1. 创建A时， AbstractBeanFactory.doGetBean方法会调用此方法，A加入到 singletonFactories
                // 2. 给A填充属性（调用AbstractAutowireCapableBeanFactory.applyPropertyValues），会触发创建B
                // 3. 创建B时， AbstractBeanFactory.doGetBean方法会调用此方法，B加入到 singletonFactories
                // 4. 给B填充属性（调用AbstractAutowireCapableBeanFactory.applyPropertyValues），会触发创建A
                //    此时进入本方法发现 singletonFactories 中的A不为空，则直接取出A对象，避免递归死循环，A加入到二级缓存中。
                // 5. B的属性填充完成后， 又会调用本方法获取对象B，类似地，此时进入本方法发现 singletonFactories 中的B不为空，
                //    则直接取出B对象，避免递归死循环，把B加入到二级缓存中。
                // 6. 此时B初始化完成，调用 registerSingleton()方法，把B对象从二级缓存移动到一级缓存。
                // 7. B创建完成，回到步骤2， A的属性填充完成，类似地， 又会继续调用本方法获取A，把已经在二级缓存的A 移动到一级缓存。

                // 总结： 对象刚实例化后即保存到map映射表中，后续涉及引用时直接查表，防止递归循环调用。
                if (singletonFactory != null) {
                    singletonObject = singletonFactory.getObject();
                    // 把三级缓存中的factory对象生成的真实对象获取出来，放入二级缓存中
                    earlySingletonObjects.put(beanName, singletonObject);
                    singletonFactories.remove(beanName);
                }
            }
        }
        return singletonObject;
    }

    public void registerSingleton(String beanName, Object singletonObject) {
        singletonObjects.put(beanName, singletonObject);
        earlySingletonObjects.remove(beanName);
        singletonFactories.remove(beanName);
    }

    protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory){
        if (!this.singletonObjects.containsKey(beanName)) {
            this.singletonFactories.put(beanName, singletonFactory);
            this.earlySingletonObjects.remove(beanName);
        }
    }

    public void registerDisposableBean(String beanName, DisposableBean bean) {
        disposableBeans.put(beanName, bean);
    }

    public void destroySingletons() {
        Set<String> keySet = this.disposableBeans.keySet();
        Object[] disposableBeanNames = keySet.toArray();

        for (int i = disposableBeanNames.length - 1; i >= 0; i--) {
            Object beanName = disposableBeanNames[i];
            DisposableBean disposableBean = disposableBeans.remove(beanName);
            try {
                disposableBean.destroy();
            } catch (Exception e) {
                throw new BeansException("Destroy method on bean with name '" + beanName + "' threw an exception", e);
            }
        }
    }

}
