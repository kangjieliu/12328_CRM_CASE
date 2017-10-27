package com.CallThink.ut_service.controller;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 测试bs架构下的springmvc定时器使用方式
 * 需要配置 /UltraCRM/src/main/resources/spring-mvc-servlet.xml 中关于文件
 * 参考文章: http://www.cnblogs.com/wunaozai/p/5026765.html
 * 注意:后面如果 <task:annotation-driven 标签发生异常,是因为最前面没加 xmlns:task="http://www.springframework.org/schema/task"
 * @author G-APPLE
 *
 */
@Component("taskJob")
public class TestEverySecond {
  /*  @Scheduled(cron = "1/5 * * * * ?")
    public void testTask(){
        System.out.println(System.currentTimeMillis());
    }
    
    
    @Scheduled(cron = "1/1 * * * * ?")
    public void testTask22(){
        System.out.println("检测任务");
    }*/
}