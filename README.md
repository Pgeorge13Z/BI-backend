<p align="center">
<img src="https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202311020941065.png" style="zoom:50%;" align="center" />
</p>



<p align="center">
<a>
    <img src="https://img.shields.io/badge/Spring Boot-2.7.2-brightgreen.svg" alt="Spring Boot">
    <img src="https://img.shields.io/badge/MySQL-8.0.20-orange.svg" alt="MySQL">
    <img src="https://img.shields.io/badge/Java-1.8.0__371-blue.svg" alt="Java">
    <img src="https://img.shields.io/badge/Redis-5.0.14-red.svg" alt="Redis">
    <img src="https://img.shields.io/badge/RabbitMQ-3.9.11-orange.svg" alt="RabbitMQ">
    <img src="https://img.shields.io/badge/MyBatis--Plus-3.5.2-blue.svg" alt="MyBatis-Plus">
    <img src="https://img.shields.io/badge/Redisson-3.21.3-yellow.svg" alt="Redisson">
    <img src="https://img.shields.io/badge/Gson-3.9.1-blue.svg" alt="Gson">
    <img src="https://img.shields.io/badge/Hutool-5.8.8-green.svg" alt="Hutool">
    <img src="https://img.shields.io/badge/MyBatis-2.2.2-yellow.svg" alt="MyBatis">
</a>
</p>

# Pgeorge 智能BI平台

> 作者：[Pgeorge](https://github.com/Pgeorge13Z)

# 项目介绍

本项目是基于React + Spring Boot + RabbitMQ + AIGC + 线程池实现的智能BI数据分析平台。

访问地址：

区别于传统的BI，数据分析者只需要导入原始的Excel数据集，输入想要分析的目标，平台会自动生成符合要求的图表以及分析结论。此外，还会有失败重试，异步生成，错误图表自恢复等功能，大幅降低人工数据分析成本。

## 核心功能

1. 智能分析（同步）：调用AI根据用户上传csv文件生成对应的 JSON 数据，利用**AIGC能力**，并使用 ECharts图表 将分析结果可视化展示。
2. 智能分析（异步）：由于AIGC相应时间较长，基于**自定义 IO 密集型线程池**+基于任务队列实现了 AIGC 的异步化处理，优化用户体验并减轻系统负担。
3. 分布式持久化：由于本地任务队列重启丢失数据，使用 **RabbitMQ（分布式消息队列）**来持久化任务消息，通过 Direct 交换机转发给解耦的 AI 生成模块消费并处理任务，提高了系统的可靠性。
4. 异常处理与补偿：考虑到任务失败的情况，使用**死信队列**+ **Spring Scheduler 定时任务**的方式对失败的任务进行补偿处理，并通过基于**Redisson的分布式锁**来保证多级部署的定时任务不会重复执行。
5. 用户限流：本项目用到了令牌桶算法，使用**Redisson**实现简单且高效的**分布式限流**，限制用户每秒的接口调用次数，防止用户恶意占用系统资源。
6. 数据处理：调试AI的prompt，控制AI的输出。并根据AIGC的输入Token限制，适用Excel 解析用户上传的XLSX表格数据文件，并压缩为CSV，实测**提高了30%以上的单次输入量**，并将**AI结果可用性提升了50%**以上。

6. 图表信息缓存：适用**Redis缓存**高频访问的公开信息页，万级数据量下，**响应时长从400ms优化至25ms**。且自定义Redis序列化器来解决数据乱码和空间浪费的问题。

## 技术栈

1. SSM+SpringBoot+MybatisPlus
2. Redis： Redisson限流控制、Redisson分布式锁、RedisTemplate缓存
3. RabbitMQ: 时效性极高的消息队列，在项目中用于将 AIGC 生成图表这一耗时任务进行异步化和解耦。
4. AI SDK: 基于AI接口的AI能力
5. JDK 线程池以及异步化
6. Spring Scheduler定时任务 配合 死信队列进行失败补偿。
7. Easy Excel：表格处理、Hutool工具库（文件后缀建议、List判空）、Apache Common Utils（ObjectUtils：流中判空、StringUtils：字符串拼接与判空）、Gson
8. Swagger + Knife4j 项目文档

## 快速启动

1. 下载/拉取本项目到本地
2. 通过 IDEA 代码编辑器进行打开项目，等待依赖的下载
3. 修改配置文件 `application.yaml` 的信息，比如数据库、Redis、RabbitMQ等
4. 修改信息完成后，通过 `ShierApplication` 程序进行运行项目

## 项目架构

**基础版本流程**：初始版本，客户端输入分析诉求和原始数据，像业务后端发送请求。业务后端处理原始数据和分析诉求，发送给AI服务，AI服务生成图表和分析结果，并返回给后端，后端存储数据和图表，并将结果返回给客户端展示。

1. 用户输入

​       a.分析目标

​		b.上传原始数据（excel)

​		c.更精细化的控制图表：比如图表的类型、图表的名称

2. 后端检验

   a.检验用户的输入是否合法（比如长度等）

   b.成本控制（次数统计和校验、鉴权、限流）

3. 处理数据（数据压缩：用EasyExcel库将excel数据转换为csv）

4. 把处理后的数据输入给AI模型（调用AI接口），AI提供图表信息和结论文本。（图表信息采用正则表达式提取合理的echarts配置js代码）

5. 在前端展示图表信息和结论文本。（AI生成特定模板的js代码，由前端的Echarts组件完成展示）

6. 将输入信息、输出结果等存入数据库

![img](https://cdn.nlark.com/yuque/0/2024/jpeg/2398715/1708328300633-051eceac-c186-4eba-b07a-d26c5fb066ad.jpeg)

**优化版本1（异步化）**：

1. 客户端点击智能分析页的提交按钮，输入分析诉求和原始数据，向业务后端发送请求。

2. 后端立刻将图表保存到数据库中，然后提交任务（任务：先修改图表的任务状态为“执行中”。等执行成功后，修改为“已完成”、保存执行结果；执行失败后，状态修改为“失败”，记录任务失败信息。）

3. 业务后端将请求事件放入消息队列，并让需要生成图表的客户端排队，消息队列根据AI服务的负载情况，定期检查进度，如果AI服务还能处理更多的图标生成请求，向任务模块发送信息。

4. 任务模块调用AI服务处理客户端数据，**异步**生成图表和分析结果，并返回给后端并保存到数据库，每当后端的AI服务调用完毕，修改数据库中任务状态为成功或者失败，业务后端监控数据库中图表生成服务的状态，来确定生成结果是否可用。若生成结果可用，前端即可获取并处理相应的数据，最终将结果返回给客户端展示。（在此期间，用户可以做自己的事）

5. 任务模块失败的消息会被放入死信队列，将状态更新为“失败”，存入数据库中。定时任务模块会定时检查数据库中状态为“失败”的数据，通过数据处理更改为可执行的消息，并重新放入消息队列中。

   ![image-20240302143837551](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202403021438617.png)



## 效果展示

![image-20240302160613718](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202403021606794.png)

![image-20240302160633260](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202403021606340.png)

![image-20240302160642068](https://pgeorge-1310330018.cos.ap-chongqing.myqcloud.com/202403021606144.png)

## 待优化

1. 界面优化
2. 支持用户的数据修改
3. 分库分表，减小原始数据带来的压力。