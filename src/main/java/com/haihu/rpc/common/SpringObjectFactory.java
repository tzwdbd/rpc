package com.haihu.rpc.common;

import io.netty.util.internal.StringUtil;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 从spring中,根据id获取bean实例
 * @author 
 *
 */
public class SpringObjectFactory implements ApplicationContextAware{
    
    protected static ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringObjectFactory.applicationContext = applicationContext;
    }
    
    /**
     * 从spring application context 获取bean实例
     * @param beanName
     * @return
     */
    public static Object getInstance(String beanName){
        return applicationContext.getBean(beanName);
    }
    
    public static Map<String,Object> getInstancesWithAnnotation(Class<? extends Annotation> annotationType){
        return applicationContext.getBeansWithAnnotation(annotationType);
    }
    
    public static Object getRemotionServiceByInterface(String name){
    	if(StringUtil.isNullOrEmpty(name)){
    		return null;
    	}
    	Map<String,Object> map = applicationContext.getBeansWithAnnotation(RemoteService.class);
    	for (Map.Entry<String, Object> entry : map.entrySet()) {
    		String key = entry.getKey();
      	  	if(!StringUtil.isNullOrEmpty(key)){
	      	  	key = key.toLowerCase();
	      	  	name = name.toLowerCase();
	      	  	if(key.contains(name)){
	      	  		return entry.getValue();
	      	  	}
      	  	}
    	}
    	return null;
    }
}
