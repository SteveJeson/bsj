## Collector数据采集者，聚合项目
### 一、技术框架
- 核心框架：Netty
- 消息中间件：RabbitMQ
### 二、项目模块
1、核心模块：collector-sender
- 职责：负责设备数据的接入，并将数据推送到RabbitMQ,以供业务消费使用
- 启动类：Main   

2、核心模块：collector-receiver
- 职责：负责消费RabbitMQ的数据，解析并将数据存入对应数据库表 
- 启动类：Main

3、核心模块：collector-http-server
- 职责：负责接入遵循http协议的数据，当前已接入的是零零科技公司的设备数据
- 启动类：Main

4、辅助模块：simulator
- 职责：负责模拟设备数据进行测试，目前只模拟了博实结私有协议数据
- 启动类：SimulatorMain

5、辅助模块：collector-common
- 职责：核心模块的依赖模块，通用解析方法工具类
- 无启动类

6、辅助模块：collector-rabbitmq-client
- 职责：核心模块的依赖模块，RabbitMQ客户端，负责连接MQ服务端、初始化队列配置、监听和处理队列消息
- 无启动类

7、辅助模块：collector-tcp-client
- 职责：核心模块的依赖模块，TCP客户端，负责与TCP服务端通讯，当前主要用来转发沃瑞特808协议数据到车管通平台
- 无启动类