# RabbitMQ

## 1.MQ是什么

MQ(message queue)：消息队列，遵循先进先出原则，用于“上下游”的跨进程通信

- 流量消峰：高并发时将用户请求发送给队列，由队列发送任务给服务器
- 应用解耦：队列代替父系统监督子系统的任务执行
- 异步处理

RabbitMQ:基于AMQP（高级消息队列）基础上完成的，由erlang语言开发。<font color=orange>负责接收、存储、转发消息数据</font>

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202211925115.png" alt="image-20220221192525049" style="zoom:75%;float:left" />

### 1.1 安装Rabbitmq

~~~shell
# 安装启动rabbitmq容器
docker run -d --name myRabbitMQ -e RABBITMQ_DEFAULT_USER=root -e RABBITMQ_DEFAULT_PASS=root -p 15672:15672 -p 5672:5672 rabbitmq:3.8.14-management
~~~

<font color=orange>这里设置了登录rabbitmq的账号密码</font>

### 1.2 安装启动RabbitMQWeb管理界面

> 默认情况下,rabbitmq没有安装web端的客户软件,需要安装才能生效

~~~shell
# 打开RabbitMQWeb管理界面插件
rabbitmq-plugins enable rabbitmq_management
~~~

### 1.3 添加远程用户

~~~shell
#添加用户

rabbitmqctl add_user 用户名 密码

#设置用户角色,分配操作权限

rabbitmqctl set_user_tags 用户名 角色

#为用户添加资源权限(授予访问虚拟机根节点的所有权限)

rabbitmqctl set_permissions -p / 用户名 ".*" ".*" ".*"
~~~

角色有四种：

- administrator：可以登录控制台、查看所有信息、并对rabbitmq进行管理
- monToring：监控者；登录控制台，查看所有信息
- policymaker：策略制定者；登录控制台指定策略
- managment：普通管理员；登录控制

其他指令

~~~shell
# 修改密码
rabbitmqctl change_ password 用户名 新密码

# 删除用户
rabbitmqctl delete_user 用户名

# 查看用户清单
rabbitmqctl list_users
~~~



![img](https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202221853601.png)

登录成功后则显示如下界面

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202221854592.png" alt="image-20220222185438497" style="zoom: 50%;float:left" />

rabbitmq有多个端口

- 5672：amqp通信端口
- 15672：http端口
- 25672：集群端口

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202221935647.png" alt="image-20220222193505619" style="zoom:80%;float:left" />

## 2.RabbitMQ的使用

### 2.1 AMPQ协议

<font color=orange>Virtual Host类似于关系型数据库的库，一个项目或者一个业务服务对应一个虚拟主机</font>

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202221938567.png" alt="image-20220222193821526" style="zoom:120%;float:left" />




使用rabbitmq：

1. 创建虚拟主机
2. 用户绑定虚拟主机
3. 生产者发送消息
4. 消费者消费消息

### 2.2 使用rabbitmq

1. 引入依赖

~~~xml
<!-- 引入依赖-->
<dependency>
    <groupId>com.rabbitmq</groupId>
    <artifactId>amqp-client</artifactId>
    <version>5.7.2</version>
</dependency>
~~~

2. 创建虚拟主机

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202222011211.png" alt="image-20220222201144165" style="zoom:80%;float:left" />

3. 添加用户到虚拟主机

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202222013628.png" alt="image-20220222201332589" style="zoom:80%;float:left" />

#### 2.2.1 直连模型

<font color=orange>只有队列没有交换机</font>

![image-20220222201936328](https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202222019355.png)

- 生产者：生产消息
- 消费者：等待接收消息
- 消息队列：类似于邮箱，可以缓存消息；生产者向队列投入消息，消费者向邮箱取出消息

> 如果不指定交换机，生产者会将消息发布给AMQP default交换机；而每一个队列，无论后天绑定了哪个交换机，先天会默认绑定AMQP default交换机（无法解绑，这个交换机也无法被删除）；而这个交换机的匹配方式，是通过生产者的routingKey匹配队列的queue name；这就解释了为什么不指定交换机时，会发送给名称为routingKey的队列。

##### 1. 生产者

~~~java
public class Provider {
    @Test
    public void sendMsg() throws IOException, TimeoutException {
        // 创建rabbitmq连接工厂对象
        ConnectionFactory factory = new ConnectionFactory();
        // 设置连接的主机
        factory.setHost("47.98.179.176");
        // 设置AMQP的端口
        factory.setPort(5672);
        // 设置虚拟主机
        factory.setVirtualHost("/ems");
        // 设置访问虚拟主机的账号、密码
        factory.setUsername("root");
        factory.setPassword("root");
        // 获取连接
        Connection connection = factory.newConnection();
        // 获取通道
        Channel channel = connection.createChannel();
        // 通道绑定消息队列
        // 1. 队列名称
        // 2. 队列是否要持久化（如果true,rabbitmq的队列将存入磁盘，重启后依然存在，但数据不存在，如果需要存在设置发布时的消息参数）
        // 3. connection是否独占队列
        // 4. 是否在消费完成后自动消除队列
        channel.queueDeclare("hello",false,false,false,null);
        // 发布消息
        // 1. 交换机名称
        // 2. 队列名称(routing key)
        // 3. 消息的额外参数（消息是否持久化）
        // 4. 消息的具体内容
        channel.basicPublish("","hello",null,"hello world".getBytes());
        // 关闭信道和连接
        channel.close();
        connection.close();
    }
}
~~~

发送成功后队列成功存储了消息

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202222054038.png" alt="image-20220222205447005" style="zoom:80%;float:left" />

##### 2. 消费者

~~~java
public class Customer {
    public static void main(String[] args) throws IOException, TimeoutException {
        // 创建rabbitmq连接工厂对象
        ConnectionFactory factory = new ConnectionFactory();
        // 设置连接的主机
        factory.setHost("47.98.179.176");
        // 设置AMQP的端口
        factory.setPort(5672);
        // 设置虚拟主机
        factory.setVirtualHost("/ems");
        // 设置访问虚拟主机的账号、密码
        factory.setUsername("root");
        factory.setPassword("root");
        // 获取连接
        Connection connection = factory.newConnection();
        // 获取通道
        Channel channel = connection.createChannel();
        // 通道绑定消息队列
        channel.queueDeclare("hello",false,false,false,null);
        // 消费消息
        // 1. 消费的队列名称
        // 2. 自动确认机制
        // 3. 回调接口
        channel.basicConsume("hello", true, new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("消费的消息是:"+new String(body));
            }
        });
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 让消费者线程一直等待消息，不关闭通道
//        channel.close();
//        connection.close();
    }
}
~~~

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202222124297.png" alt="image-20220222212414265" style="float:left" />

##### 3. 连接工具类

~~~java
public class RabbitMQUtils {
    private static ConnectionFactory factory;
    static{
        // 重量级资源，类加载的时候执行
        factory = new ConnectionFactory();
        // 设置连接的主机
        factory.setHost("47.98.179.176");
        // 设置AMQP的端口
        factory.setPort(5672);
        // 设置虚拟主机
        factory.setVirtualHost("/ems");
        // 设置访问虚拟主机的账号、密码
        factory.setUsername("root");
        factory.setPassword("root");
    }
    public static Connection getConnection(){
        try {
            // 获取连接
            return factory.newConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void closeConnectionAndChannel(Channel channel, Connection conn){
        try {
            if(channel != null){
                channel.close();
            }
            if(conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
~~~

#### 2.2.2 工作队列

`work queue`称为工作队列，当消费者处理消息耗时较大时，生产消息的速度会远远大于消费的速度，进而造成队列阻塞。

此时可以使用工作队列，安排多个消费者绑定同一个队列，共同消费队列中的消息。队列中的消息一旦被消费，就会消失，不会被重复消费。

![image-20220223190546580](https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202231905656.png)

##### 1. 生产者

~~~java
Connection connection = RabbitMQUtils.getConnection();
Channel channel = connection.createChannel();
channel.queueDeclare("work", true, false, false, null);
for (int i = 0; i < 10; i++) {
    channel.basicPublish("", "work", null, (i+"hellow workqueue").getBytes());
}
RabbitMQUtils.closeConnectionAndChannel(channel,connection);
~~~

##### 2. 消费者1

~~~java
public static void main(String[] args) throws IOException {
    Connection connection = RabbitMQUtils.getConnection();
    Channel channel = connection.createChannel();
    channel.queueDeclare("work",true, false, false, null);
    channel.basicConsume("work",true,new DefaultConsumer(channel){
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            System.out.println("消费信息:"+new String(body));
        }
    });
}
~~~

##### 3. 消费者2

~~~java
public static void main(String[] args) throws IOException {
    Connection connection = RabbitMQUtils.getConnection();
    Channel channel = connection.createChannel();
    channel.queueDeclare("work",true, false, false, null);
    channel.basicConsume("work",true,new DefaultConsumer(channel){
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            System.out.println("消费信息:"+new String(body));
        }
    });
}
~~~

##### 4. 结果

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202231928573.png" alt="image-20220223192854537" style="float:left" />

<font color=orange>默认情况下，RabbitMQ 将按顺序将每条消息发送给下一个消费者。平均而言，每个消费者将获得相同数量的消息。这种分发消息的方式称为轮循机制。与三个或更多工人一起尝试。</font>

平均分配的坏处

- 有些任务执行的较快，消费者已经全部执行完任务空闲了
- 有些任务执行的较慢，消费者一直在执行某个任务，而且也堆积了其他平均分配的任务
- 由于平均分配，空闲的消费者得不到在另一个消费者没有执行的任务

##### 5. 能者多劳

> 执行任务可能需要几秒钟。您可能想知道，如果其中一个消费者开始执行一项长期任务，但只完成了部分任务，会发生什么情况。使用我们当前的代码，一旦RabbitMQ将消息传递给消费者，它就会立即将其标记为删除。在这种情况下，如果您杀死了一个消费者，我们将丢失它刚刚处理的消息。我们还将丢失已派往此特定消费者但尚未处理的所有消息。
>
> 但我们不想丢失任何任务。如果一个消费者死亡，我们希望将任务交付给另一个工作人员。

- 关闭消息自动确认机制
- 消费者一次只能收到一个任务
- 如果消费者没有确认处理的消息，该消费者也就无法接收到队列中剩余的消息

~~~java
// 服务器一次能够传递的最大消息数量
channel.basicQos(1);
channel.queueDeclare("work",true, false, false, null);
// 关闭自动确认机制
channel.basicConsume("work",false,new DefaultConsumer(channel){
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        System.out.println("消费信息:"+new String(body));
        // 手动确认，消息已经处理完毕
        channel.basicAck(envelope.getDeliveryTag(), false);
    }
});
~~~

#### 2.2.3 扇出模型

<font color=orange>扇出模型（fanout）其实就是广播模型</font>

![image-20220223202211099](https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202232024164.png)

- 可以有多个消费者
- 每个消费者有自己的队列
- 每个队列绑定到交换机
- 生产者发送的消息，只能发送到交换机，交换机来决定发送到哪个队列，生产者无法决定
- 交换机把消息发送到绑定的所有队列
- 队列的消费者都能拿到消息，实现一条消息发送给所有消费者

##### 1. 生产者

~~~java
// 参数1：交换机名称 参数2：交换机类型
channel.exchangeDeclare("news","fanout");
// 发布消息
channel.basicPublish("news","",null,"fanout".getBytes());
// 关闭资源
RabbitMQUtils.closeConnectionAndChannel(channel,connection);
~~~

##### 2. 消费者

~~~java
// 通道绑定交换机
channel.exchangeDeclare("news","fanout");
// 建立临时队列
String queue = channel.queueDeclare().getQueue();
// 绑定队列和交换机
channel.queueBind(queue,"news","");
// 消费
channel.basicConsume(queue,true,new DefaultConsumer(channel){
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        System.out.println("消费者1:"+new String(body));
    }
});
~~~

##### 3. 结果

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202232056735.png" alt="image-20220223205622705" style="zoom:100%;float:left" />

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202232049344.png" alt="image-20220223204937307" style="float:left" />

#### 2.2.4 路由模型-Direct

路由模型（routing-direct）:在某些情况下，我们希望一些消息只能给部分消费者消费

- 队列与交换机的绑定，不再是任意绑定了。而是要指定一个`Routing Key`(路由key)
- 生产者发送消息给交换机时，也必须指定消息的`Routing Key`
- Exchange不再把消息发送给所有队列，而是根据`Routing Key`进行判断，队列只有与消息的`Routing Key`相同时才能接收到消息

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202232057842.png" alt="image-20220223205750809" style="float:left" />

- 生产者：向exchange发消息，指定Routing Key
- 交换机：接收生产者信息，然后把消息传递给与routing key匹配的队列
- C1:其消费的队列指定了routing key为error的消息

##### 1. 生产者

~~~java
// 声明交换机
channel.exchangeDeclare("logs_direct","direct");
// 声明routing key的发布消息
String routingKey = "info";
channel.basicPublish("logs_direct",routingKey,null,("这是direct模型基于routingkey的消息:"+routingKey).getBytes());
RabbitMQUtils.closeConnectionAndChannel(channel,connection);
~~~

##### 2. 消费者1

~~~java
// 声明交换机
channel.exchangeDeclare("logs_direct","direct");
// 创建临时队列
String queue = channel.queueDeclare().getQueue();
// 基于routing_key绑定临时队列与交换机
channel.queueBind(queue, "logs_direct", "info");
// 获取消费的消息
channel.basicConsume(queue,true,new DefaultConsumer(channel){
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        System.out.println("消费者1:"+ new String(body));
    }
});
~~~

##### 3. 消费者2

~~~java
// 声明交换机
channel.exchangeDeclare("logs_direct", "direct");
// 创建临时队列
String queue = channel.queueDeclare().getQueue();
// 临时队列与交换机基于routing_key绑定
channel.queueBind(queue, "logs_direct","info");
channel.queueBind(queue, "logs_direct", "error");
channel.queueBind(queue, "logs_direct", "warning");
// 获取消费的消息
channel.basicConsume(queue, true, new DefaultConsumer(channel) {
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        System.out.println("消费者2:" + new String(body));
    }
});
~~~

##### 4. 结果

~~~java
消费者1:这是direct模型基于routingkey的消息:info
消费者2:这是direct模型基于routingkey的消息:info
~~~

#### 2.2.5 动态路由-Topic

Topic类型的Exchange与Direct相比，都是可以根据Routing Key把消息路由到不同的队列。<font color=orange>但是Topic类型Exhange在让队列绑定到Routing Key的时候使用通配符</font>

这种类型的routing key一般都由一个或多个单词组成，多个单词以'.'分割，例如：item.insert

![image-20220225145251453](https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202251452551.png)

- *（星号）可以只代替一个单词。
- \#（井号）可以代替零个或多个单词。

> 例如： 
>
> audit.# 匹配audit.irs.corporate或者audit.irs
>
> audit.* 匹配aaudit.irs	

##### 1. 生产者

~~~java
channel.exchangeDeclare("topics","topic");
// 设置路由key
String routingKey = "user.save.del";
// 发布消息
channel.basicPublish("topics", routingKey, null, "这是topic类型".getBytes());
RabbitMQUtils.closeConnectionAndChannel(channel,connection);
~~~

##### 2. 消费者1

~~~java
// 声明交换机
channel.exchangeDeclare("topics", "topic");
// 创建临时队列
String queue = channel.queueDeclare().getQueue();
// 根据routingkey绑定队列与交换机
channel.queueBind(queue,"topics","user.*");
channel.basicConsume(queue,true,new DefaultConsumer(channel){
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        System.out.println("消费者1:"+new String(body));
    }
});
~~~

##### 3. 消费者2

~~~java
channel.exchangeDeclare("topics", "topic");
String queue = channel.queueDeclare().getQueue();
channel.queueBind(queue,"topics","user.#");
channel.basicConsume(queue,true,new DefaultConsumer(channel){
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        System.out.println("消费者2:"+new String(body));
    }
});
~~~

##### 4. 结果

只有消费者2消费了这条消息

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202251514943.png" alt="image-20220225151432905" style="float:left" />

### 2.3 SpringBoot使用

#### 2.3.1 搭建环境

~~~xml-dtd
<!--rabbitmq的依赖-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
~~~

#### 2.3.2 配置环境

application.yml

~~~yml
spring:
  application:
    name: springboot-rabbitmq
  rabbitmq:
    username: loubei
    password: loubei
    host: 47.98.179.176
    port: 5672
    virtual-host: /ems
~~~

`RabbitTemplate`用来简化操作，使用时直接就在项目中注入即可

#### 2.3.3 直连模式

##### 1. 生产者

~~~java
@SpringBootTest
class SpringbootRabbitmqApplicationTests {
	//	注入RabbitTemplate
	@Autowired
	RabbitTemplate rabbitTemplate;
	//	使用直连模式发送消息
	@Test
	public void testDirect(){
        // 第1个参数：队列
        // 第2个参数：消息
		rabbitTemplate.convertAndSend("hello","hello-world");
	}
}
~~~

##### 2. 消费者

~~~java
@Component
@RabbitListener(queuesToDeclare = @Queue("hello"))
public class Consumer {
    @RabbitHandler
    public void receive(String message){
        System.out.println("message = " + message);
    }
}
~~~

##### 3. 结果

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202251601466.png" alt="image-20220225160123422" style="zoom:80%;float:left" />

#### 2.3.4 工作队列

##### 1. 消费者

```java
@Test
public void testWork(){
   for (int i = 0; i < 10; i++) {
      rabbitTemplate.convertAndSend("work", "hello-work"+i);
   }
}
```

##### 2. 消费者

~~~java
@Component
public class Consumer1 {
    @RabbitListener(queuesToDeclare = @Queue("work"))
    public void consumer1(String msg){
        System.out.println("消费者1："+msg);
    }

    @RabbitListener(queuesToDeclare = @Queue("work"))
    public void consumer2(String msg){
        System.out.println("消费者2："+msg);
    }
}
~~~

##### 3. 结果

~~~java
消费者2：hello-work0
消费者1：hello-work1
消费者2：hello-work2
消费者1：hello-work3
消费者2：hello-work4
消费者1：hello-work5
消费者2：hello-work6
消费者1：hello-work7
消费者2：hello-work8
消费者1：hello-work9
~~~

默认在Spring中实现的是公平调度，如果想要能者多劳，需要额外设置

#### 2.3.5 扇出模型

##### 1. 生产者

~~~java
@Test
public void testFanout(){
    // 参数1：交换机
    // 参数2：routing key
    // 参数3：消息
    rabbitTemplate.convertAndSend("logs", "","扇出模型发送的消息");
}
~~~

##### 2. 消费者

```java
// 声明临时队列、与交换机绑定
@RabbitListener(bindings = {@QueueBinding(value = @Queue, exchange = @Exchange(value = "logs",type = "fanout"))})
public void consume(String msg){
    System.out.println("消费者1:"+msg);
}

@RabbitListener(bindings = {@QueueBinding(value = @Queue, exchange = @Exchange(value = "logs",type = "fanout"))})
public void consume2(String msg){
    System.out.println("消费者2:"+msg);
}
```

#### 2.3.5 路由模型-Direct

##### 1. 生产者

```java
@Test
public void testDirect(){
   rabbitTemplate.convertAndSend("directs","error","路由模式,routingkey为error");
}
```

##### 2. 消费者

```java
@RabbitListener(bindings = {
        @QueueBinding(
                value = @Queue, // 临时队列
                exchange = @Exchange(value = "directs",  type = "direct"), // 交换机
                key={"log","error","warning"} // routing key
        )
})
public void receive(String msg){
    System.out.println("消费者1:"+msg);
}

@RabbitListener(
        bindings = {@QueueBinding(
                value = @Queue,
                exchange = @Exchange(value = "directs", type = "direct"),
                key = {"log"}
        )}
)
public void receice(String msg){
    System.out.println("消费者2:"+msg);
}
```

##### 3. 结果

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202251816959.png" alt="image-20220225181615914" style="zoom:100%;float:left" />

#### 2.3.6 动态路由-Topic

##### 1. 生产者

```java
@Test
public void testTopic(){
   rabbitTemplate.convertAndSend("topics","user.save.del","动态路由模式");
}
```

##### 2. 消费者

```java
@RabbitListener(bindings = {
        @QueueBinding(
                value = @Queue,
                exchange = @Exchange(value = "topics", type = "topic"),
                key = {"user.*"}
        )
})
public void receive(String msg){
    System.out.println(msg);
}

@RabbitListener(bindings = {
        @QueueBinding(
                value = @Queue,
                exchange = @Exchange(value = "topics", type = "topic"),
                key = {"user.#"}
        )
})
public void receive2(String msg){
    System.out.println(msg);
}
```

##### 3. 结果

只有第二个消费者得到了消息

## 3.使用场景

### 3.1 异步处理

场景说明：用户注册后，需要发注册邮件和注册短信,传统的做法有两种. 1. 串行的方式; 2. 并行的方式

- 串行方式：将注册信息写入数据库后,发送注册邮件,再发送注册短信,以上三个任务全部完成后才返回给客户端。 这有一个问题是,邮件,短信并不是必须的,它只是一个通知,而这种做法让客户端等待没有必要等待的东西.

<img src="C:\Users\PSJ\AppData\Roaming\Typora\typora-user-images\image-20220225183522768.png" alt="image-20220225183522768" style="zoom:80%;float:left" />

- 并行方式：将注册信息写入数据库后,发送邮件的同时,发送短信,以上三个任务完成后,返回给客户端,并行的方式能提高处理的时间。虽然并性已经提高的处理时间,但是,前面说过,邮件和短信对我正常的使用网站没有任何影响，客户端没有必要等着其发送完成才显示注册成功，应该是写入数据库后就返回.

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202251835633.png" alt="image-20220225183500583" style="zoom:80%;float:left" />

- 消息队列：引入消息队列后，把发送邮件,短信不是必须的业务逻辑异步处理

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202251836183.png" alt="image-20220225183632133" style="zoom:80%;float:left" />

### 3.2 应用解耦

场景：双11是购物狂节,用户下单后,订单系统需要通知库存系统,传统的做法就是订单系统调用库存系统的接口.

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202251847381.png" alt="image-20220225184740340" style="float:left" />

这种做法有一个缺点:

- 当库存系统出现故障时，订单就会失败
- 订单系统和库存系统高耦合

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202251848763.png" alt="image-20220225184851718" style="float:left" />

- 订单系统:用户下单后,订单系统完成持久化处理,将消息写入消息队列,返回用户订单下单成功。
- 库存系统:订阅下单的消息,获取下单消息,进行库操作。
  就算库存系统出现故障,消息队列也能保证消息的可靠投递,不会导致消息丢失

### 3.3 流量削峰

流量削峰一般在秒杀活动中应用广泛
场景:秒杀活动，一般会因为流量过大，导致应用挂掉,为了解决这个问题，一般在应用前端加入消息队列。

作用:
1.可以控制活动人数，超过此一定阀值的订单直接丢弃
2.可以缓解短时间的高流量压垮应用(应用程序按自己的最大处理能力获取订单)

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202251859300.png" alt="image-20220225185914256" style="float:left" />

1.用户的请求,服务器收到之后,首先写入消息队列,加入消息队列长度超过最大值,则直接抛弃用户请求或跳转到错误页面.
2.秒杀业务根据消息队列中的请求信息，再做后续处理.

## 4.集群

### 4.1 集群架构

[【实践】docker简易搭建RabbitMQ集群 - 云+社区 - 腾讯云 (tencent.com)](https://cloud.tencent.com/developer/article/1783899)

#### 4.1.1 普通集群(副本集群)

> RabbitMQ代理操作所需的所有数据/状态都将复制到所有节点上。但默认情况下，消息队列只能驻留在主节点上，其他节点可以访问主节点的消息队列。
>
> Exchange的元数据信息在所有节点上是一致的，而Queue（存放消息的队列）的完整数据则只会存在于它所创建的那个节点上。

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202261910750.webp" alt="img" style="zoom:110%;float:left" />

RabbitMQ集群会始终同步四种类型的内部元数据：
 a. 队列元数据：队列名称和它的属性；
 b. 交换器元数据：交换器名称、类型和属性；
 c. 绑定元数据：一张简单的表格展示了如何将消息路由到队列；
 d. 虚拟机元数据：为虚拟机内的队列、交换器和绑定提供命名空间和安全属性；
 因此，当用户访问其中任何一个RabbitMQ节点时，通过rabbitmqctl查询到的queue、user、exchange、vhost等信息都是相同的

缺点：这种模式无法完成主节点宕机、从节点接替的操作，因为从节点没有消息队列的数据.因此一旦主节点失效，整个集群也就失效了

优点：

- 节省了集群所占用的空间，
- 节约了同步数据所需要的时间
- 分散流量

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202261853915.png" alt="image-20220226185305793" style="float:left" />

- 如果消息生产者所连接的是slave节点，此时队列1的完整数据不在该两个节点上，那么在发送消息过程中这两个节点主要起了一个<font color=orange>路由转发作用，根据这两个节点上的元数据转发至节点1上，</font>最终发送的消息还是会存储至节点1的队列1上。
- 如果消息消费者所连接的是slave节点，那这两个节点也会作为<font color=orange>路由节点起到转发作用</font>, 将会从节点1的队列1中拉取消息进行消费。

#### 4.1.2 docker实现

##### 1. 创建映射数据卷目录

~~~
[root@bepigkiller rabitmqCluster]# mkdir rabbitmq01 rabbitmq02 rabbitmq03
[root@bepigkiller rabitmqCluster]# ls
rabbitmq01  rabbitmq02  rabbitmq03
[root@bepigkiller rabitmqCluster]# pwd
/home/coding/rabitmqCluster 
~~~

##### 2. 启动rabbitmq

```javascript
docker run -d --hostname rabbitmq01 --name rabbitmqCluster01 -v /home/coding/rabitmqCluster/rabbitmq01:/var/lib/rabbitmq -p 15672:15672 -p 5672:5672 -e RABBITMQ_ERLANG_COOKIE='rabbitmqCookie' rabbitmq:3.8.14-management

docker run -d --hostname rabbitmq02 --name rabbitmqCluster02 -v /home/coding/rabitmqCluster/rabbitmq02:/var/lib/rabbitmq -p 15673:15672 -p 5673:5672 -e RABBITMQ_ERLANG_COOKIE='rabbitmqCookie'  --link rabbitmqCluster01:rabbitmq01 rabbitmq:3.8.14-management

docker run -d --hostname rabbitmq03 --name rabbitmqCluster03 -v /home/coding/rabitmqCluster/rabbitmq03:/var/lib/rabbitmq -p 15674:15672 -p 5674:5672 -e RABBITMQ_ERLANG_COOKIE='rabbitmqCookie'  --link rabbitmqCluster01:rabbitmq01 --link rabbitmqCluster02:rabbitmq02 rabbitmq:3.8.14-management
```

##### 3. 集群配置

**首先**在centos窗口中，执行如下命令，进入第一个rabbitmq节点容器：

```javascript
docker exec -it rabbitmqCluster01 bash
```

进入容器后，操作rabbitmq,执行如下命令：

```javascript
rabbitmqctl stop_app
rabbitmqctl reset
rabbitmqctl start_app
exit
```

操作日志信息如下：

```javascript
[root@localhost rabbitmq01]# docker exec -it rabbitmqCluster01 bash
root@rabbitmq01:/# rabbitmqctl stop_app
Stopping rabbit application on node rabbit@rabbitmq01 ...
root@rabbitmq01:/# rabbitmqctl reset
Resetting node rabbit@rabbitmq01 ...
root@rabbitmq01:/# rabbitmqctl start_app
Starting node rabbit@rabbitmq01 ...
 completed with 3 plugins.
root@rabbitmq01:/# exit
exit
```

**接下来**，进入第二个rabbitmq节点容器，执行如下命令：

```javascript
docker exec -it rabbitmqCluster02 bash
rabbitmqctl stop_app
rabbitmqctl reset
rabbitmqctl join_cluster --ram rabbit@rabbitmq01
rabbitmqctl start_app
exit
```

操作日志信息如下：

```javascript
[root@localhost rabbitmq01]# docker exec -it rabbitmqCluster02 bash
root@rabbitmq02:/# rabbitmqctl stop_app
Stopping rabbit application on node rabbit@rabbitmq02 ...
root@rabbitmq02:/# rabbitmqctl reset
Resetting node rabbit@rabbitmq02 ...
root@rabbitmq02:/# rabbitmqctl join_cluster --ram rabbit@rabbitmq01
Clustering node rabbit@rabbitmq02 with rabbit@rabbitmq01
root@rabbitmq02:/# rabbitmqctl start_app
Starting node rabbit@rabbitmq02 ...
 completed with 3 plugins.
root@rabbitmq02:/# exit
exit
```

**最后**，进入第三个rabbitmq节点容器，执行如下命令：

```javascript
docker exec -it rabbitmqCluster03 bash
rabbitmqctl stop_app
rabbitmqctl reset
rabbitmqctl join_cluster --ram rabbit@rabbitmq01
rabbitmqctl start_app
exit
```

操作日志信息如下：

```javascript
[root@localhost rabbitmq01]# docker exec -it rabbitmqCluster03 bash
root@rabbitmq03:/#  rabbitmqctl stop_app
Stopping rabbit application on node rabbit@rabbitmq03 ...
root@rabbitmq03:/# rabbitmqctl reset
Resetting node rabbit@rabbitmq03 ...
root@rabbitmq03:/# rabbitmqctl join_cluster --ram rabbit@rabbitmq01
Clustering node rabbit@rabbitmq03 with rabbit@rabbitmq01
root@rabbitmq03:/# rabbitmqctl start_app
Starting node rabbit@rabbitmq03 ...
 completed with 3 plugins.
root@rabbitmq03:/# exit
exit
```

执行上述操作，这时候 再查看客户端操作页的**overview**面板中的**Nodes**信息，可查看到节点信息。

![image-20220226194643759](https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202261946823.png)

查看集群状态

~~~javascript
rabbitmqctl cluster_status
~~~

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202202261959547.png" alt="image-20220226195908492" style="zoom:90%;float:left" />

##### 4. 负载均衡设置

```javascript
user  nginx;
worker_processes  1;

error_log  /var/log/nginx/error.log warn;
pid        /var/run/nginx.pid;


events {
    worker_connections  1024;
}


http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';

    access_log  /var/log/nginx/access.log  main;

    sendfile        on;
    #tcp_nopush     on;

    keepalive_timeout  65;

    #gzip  on;
    
    proxy_redirect          off;
    proxy_set_header        Host $host;
    proxy_set_header        X-Real-IP $remote_addr;
    proxy_set_header        X-Forwarded-For $proxy_add_x_forwarded_for;
    client_max_body_size    10m;
    client_body_buffer_size   128k;
    proxy_connect_timeout   5s;
    proxy_send_timeout      5s;
    proxy_read_timeout      5s;
    proxy_buffer_size        4k;
    proxy_buffers           4 32k;
    proxy_busy_buffers_size  64k;
    proxy_temp_file_write_size 64k;
    #rabbitmq管理界面(http通信协议)
    upstream rabbitManage {
        server 47.98.179.176:15672;
        server 47.98.179.176:15673;
        server 47.98.179.176:15674;
    }
    server {
        listen       15675;
        server_name  47.98.179.176; 
        location / {  
            proxy_pass   http://rabbitManage;
            index  index.html index.htm;  
        }  

    }
}
# rabbitmq通信(AMQP协议)
stream{
    upstream rabbitTcp{
        server 47.98.179.176:5672;
        server 47.98.179.176:5673;
        server 47.98.179.176:5674;
    }

    server {
        listen 5675;
        proxy_pass rabbitTcp;
    }
}
```

##  5.面试题

### 1. 如何防止消息丢失

1. 序号机制：使用序号机制来检查消息的身份。正常情况下，Consumer收到的消息序号是连续的，如果不是连续的就说明消息可能丢失了，不响应让它再次发送
   - 消费者通过拦截器给消息注入序号
   - 生产者通过拦截器对消息检查序号
2. 确认机制：

- 生产阶段：生产者发送消息给消息队列，Broker收到后会返回确认信息给生产者。如果生产者在一定时间内没有收到确认消息，会发送重复的消息给Broker。
- 消费阶段：消费者会在收到消息，并在处理完业务逻辑后返回确认消息给Broker。如果Broker没有收到确认信息，会再次发送同一条消息给Consumer

3. 备份：Broker可以选择参数持久化消息队列中的消息到磁盘中，防止系统突然的崩溃。也可以使用集群，复制消息到其他节点上

### 2. 如何防止重复消费

1. 幂等性：对于一条指令，多次操作和一次操作的影响是一样的
2. 只要保证多次传送数据这个操作的幂等性，就能防止消息重复消费
3. 解决方案：在发送消息时，给每个消息注入全局ID，消费消息时，先根据ID检查是否已经存在，如果不存在，就插入消息，然后去处理消息，然后更新消息的状态

### 3. 如何防止消息积压

消费者的处理能力更不上生产者的生产能力

1. 扩容消费者端的实例
2. 降级，关掉一些不重要的业务
3. 优化业务代码