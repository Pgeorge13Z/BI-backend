package com.george.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiManagerTest {

    @Resource
    private AiManager aiManager;

    @Test
    void doChat() {
        String message = "分析目标：分析用户增长情况\\n数据：日期,用户\\n1,10\\n2,100\\n3,200\\n\\n";
        String res = aiManager.doChat(1759945694640558082L,message);
        System.out.println(res);
    }

    @Test
    void test1() {
        String result = "【【【【【\n" +
                "         {\n" +
                "         title: {\n" +
                "         text: '网站用户增长情况',\n" +
                "         subtext: ''\n" +
                "         },\n" +
                "         tooltip: {\n" +
                "         trigger: 'axis',\n" +
                "         axisPointer: {\n" +
                "         type: 'shadow'\n" +
                "         }\n" +
                "         },\n" +
                "         legend: {\n" +
                "         data: ['用户数']\n" +
                "         },\n" +
                "         xAxis: {\n" +
                "         data: ['1号', '2号', '3号']\n" +
                "         },\n" +
                "         yAxis: {},\n" +
                "         series: [{\n" +
                "         name: '用户数',\n" +
                "         type: 'bar',\n" +
                "         data: [10, 20, 30]\n" +
                "         }]\n" +
                "         }\n" +
                "         【【【【【\n" +
                "         根据数据分析可得，该网站用户数量逐日增长，时间越长，用户数量增长越多。";
        String[] splits = result.split("【【【【【");
        System.out.println(splits[1].trim());
        System.out.println(splits[1]);
        System.out.println(splits[2].trim());
        System.out.println(splits[2]);
    }
}