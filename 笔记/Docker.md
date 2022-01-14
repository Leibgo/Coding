# Docker

## Docker简介

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112262115086.png" alt="img" style="float:left" />

> 发布项目出现的问题：
>
> 1. 我在我的电脑上可以运行，其他机器上运行不了
>
> 2. 版本更新，部分功能不可用
>
> 3. 环境配置十分麻烦

发布项目的两种方式：

- 打包Jar包或War包，在服务器上再部署相应版本的JDK、MySQL、redis等服务
- 将代码和依赖资源打包在一起(包括Jar、JDK、MySQL，redis等服务)

<font color=orange>Docker发布项目时会打包该项目同时带上相应的环境信息。(镜像)</font>

### 基本组成

**Docker 中有非常重要的三个基本概念，理解了这三个概念，就理解了 Docker 的整个生命周期。**

- **镜像（Image）**
- **容器（Container）**
- **仓库（Repository）**

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112262047407.png" alt="image-20211226204737370" style="float:left" />

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201031330142.png" alt="image-20220103133057057" style="zoom:88%;float:left" />

**镜像（Image）：**

Docker镜像是一个特殊的`文件系统`，除了提供容器运行时所需的程序、库、资源、配置等文件外，还包含了一些为运行时准备的一些配置参数（如匿名卷、环境变量、用户等）。

- 从远程仓库下载
- 自己只做镜像 DockerFile

> UnionFS（联合文件系统）:docker的镜像实际上由一层一层文件构成的

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112272112577.png" alt="image-20211227211208514" style="float:left" />



![image-20211227214407579](https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112272144632.png)

**容器（Container）：**

- 镜像与容器的关系相当于Java中一个静态类与对象的关系。

- 一个类可以实例化多个对象，一个镜像可以创建多个容器。

- 利用容器技术，独立运行一个或者多个应用，通过镜像创建的。

- 容器可以被创建、启动、停止、删除、暂停等

**仓库（Repository）：**

> 仓库就是存放镜像的地方。镜像构建完成后，可以很容易的在当前宿主上运行，但是， **如果需要在其它服务器上使用这个镜像，我们就需要一个集中的存储、分发镜像的服务，Docker Registry 就是这样的服务。**

一个 Docker Registry 中可以包含多个仓库（Repository）；每个仓库可以包含多个标签（Tag）；每个标签对应一个镜像。

最常使用的 Registry 公开服务是官方的 **Docker Hub** ，这也是默认的 Registry，并拥有大量的高质量的官方镜像，网址为：[https://hub.docker.com/](https://hub.docker.com/) 

### VM与Docker

> VM与Docker都属于虚拟化技术

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112262025299.png" alt="img" style="float:left" />

- VM：虚拟化一套硬件，在其上运行完整的操作系统，在操作系统上运行进程，占用大量内存。<font color=orange>擅长彻底隔离整个运行环境。</font>

- Docker容器化技术：不需要虚拟化硬件，容器的应用进程直接运行在宿主的操作系统上，更加轻便。<font color=orange>擅长隔离不同的应用。</font>

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112262039142.png" alt="image-20211226203944115" style="float:left" />

### 安装Docker

由于我使用的是阿里云的CentOs，因此安装步骤与官方介绍的不同。

这里参考阿里云文档手册[点击此链接](https://help.aliyun.com/document_detail/264695.html)

## 常用命令

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112271500167.png" alt="image-20211227150056090" style="float:left;" />

### 帮助命令

~~~bash
docker version # docker的版本信息
docer info     # docker的系统信息，包括容器和镜像的数量
docker 命令 --help # 帮助命令
~~~

帮助文档的地址：[Dockerfile reference | Docker Documentation](https://docs.docker.com/engine/reference/builder/)

![image-20211226212433275](https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112262125399.png)

### 镜像命令

#### **docker images 查看本地的镜像**

~~~bash
[root@bepigkiller ~] `docker images`
REPOSITORY    TAG       IMAGE ID       CREATED        SIZE
hello-world   latest    feb5d9fea6a5   3 months ago   13.3kB
# 解释
REPOSITORY: 仓库源
TAG：镜像标签
IMAGE ID：镜像ID
CREATED:创建时间
SIZE:大小
# 可选项：
-a, --a   列出所有镜像
-q, --quiet  只显示镜像ID    
~~~

#### **docker search 搜索镜像**

~~~shell
[root@bepigkiller ~] `docker search mysql --filter STARS=800`
NAME                 DESCRIPTION                                     STARS     OFFICIAL   AUTOMATED
mysql                MySQL is a widely used, open-source relation…   11869     [OK]       
mariadb              MariaDB Server is a high performing open sou…   4536      [OK]       
mysql/mysql-server   Optimized MySQL Server Docker images. Create…   888                  [OK]
# 可选项：
--filter 过滤条件
~~~

#### **docker pull 下载镜像**

~~~shell
[root@bepigkiller ~] `docker pull mysql:8`
8: Pulling from library/mysql
72a69066d2fe: Pull complete 
93619dbc5b36: Pull complete 
99da31dd6142: Pull complete 
626033c43d70: Pull complete 
37d5d7efb64e: Pull complete 
ac563158d721: Pull complete 
d2ba16033dad: Pull complete 
688ba7d5c01a: Pull complete 
00e060b6d11d: Pull complete 
1c04857f594f: Pull complete 
4d7cfa90e6ea: Pull complete 
e0431212d27d: Pull complete 
Digest: sha256:e9027fe4d91c0153429607251656806cc784e914937271037f7738bd5b8e7709 # 密签
Status: Downloaded newer image for mysql:8
docker.io/library/mysql:8
~~~

下载MySQL之后本地的镜像

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112262150247.png" alt="image-20211226215054217" style="float:left" />

#### **docker rmi 删除镜像**

~~~shell
[root@bepigkiller ~] `docker rmi -f 镜像ID`
Untagged: hello-world:latest
Untagged: hello-world@sha256:2498fce14358aa50ead0cc6c19990fc6ff866ce72aeb5546e1d59caac3d0d60f
Deleted: sha256:feb5d9fea6a5e9606aa995e879d862b825965ba48de054caab5ef356dc6b3412
[root@bepigkiller ~] `docker rmi -f $(docker images -aq)` #删除所有镜像
~~~

### 容器命令

#### **docker run 创建一个新的容器并运行**

~~~shell
[root@bepigkiller ~] `docker run -it centos /bin/bash`
# 可选项
--name="Name" 容器名字 tomcat01
-d            后台方式运行
-it           分配一个伪终端以交互方式运行
-p            指定端口
   -p 主机IP:主机端口:容器端口
   -p 主机端口:容器端口
   -p 容器端口
   容器端口
-P            随机指定端口
~~~

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112271334901.png" alt="image-20211227133431867" style="float:left" />

#### **docker ps 查看运行的容器**

~~~shell
[root@bepigkiller ~] `docker ps -a`
CONTAINER ID   IMAGE          COMMAND                  CREATED              STATUS                      PORTS     NAMES
2f0506838b36   3218b38490ce   "docker-entrypoint.s…"   About a minute ago   Exited (0) 56 seconds ago             flamboyant_dewdney
d0c41e382604   feb5d9fea6a5   "/hello"                 31 hours ago         Exited (0) 31 hours ago               charming_engelbart
# 可选项
-a: 当前运行的容器 + 历史运行过的容器
-n=?: 查询指定数量的容器
-q: 显示容器编号
~~~

#### **exit 退出容器**

~~~shell
exit 直接退出容器如果没有前台进程就停止
Ctrl + P + Q 容器退出但不停止 
~~~

#### **docker rm 删除容器**

~~~shell
[root@bepigkiller /] `docker rm 30df30a19021`  删除指定的容器，但不能删除正在运行的容器，强制删除用 docker rm -f
30df30a19021
[root@bepigkiller /] `docker rm -f $(docker ps -aq)`
fa36db286ed3
2f0506838b36
d0c41e382604

可选项
-f: 强制移除容器
~~~

#### **docker start 启动容器**

~~~shell
docker start 容器id      启动
docker restart 容器id    重启
docker stop 容器id       停止
docker kill 容器id       杀死
~~~

### 常用的其他命令

#### 后台启动

~~~shell
[root@bepigkiller /] `docker run -d centos`
7a99495951718ec39cf21fada5623edd7f4cc79b830a9ad5e42aa611957b30ea

可选项是 -d 会出现问题：启动centos后centos自动退出了
Docker容器后台运行,就必须有一个前台进程，容器运行的命令如果不是那些一直挂起的命令（比如运行top，tail），就是会自动退出的

[root@bepigkiller /] `docker run -d --name loubei centos /bin/sh -c "while true;do echo loubei;sleep 1;done"
59361ced837b15abdcb57a3063ff4ac1a1a1b5a47f74bf09a7edee249433febd
[root@bepigkiller /]# docker ps
CONTAINER ID   IMAGE     COMMAND                  CREATED         STATUS         PORTS     NAMES
59361ced837b   centos    "/bin/sh -c 'while t…"   4 seconds ago   Up 3 seconds             loubei

# 添加一段启动命令的脚本，后台便不会自动退出
~~~

#### 打印日志

~~~shell
[root@bepigkiller /] `docker logs -ft --tail 10 59361ced837b
2021-12-27T06:18:04.147529356Z loubei
2021-12-27T06:18:05.149216408Z loubei
2021-12-27T06:18:06.150879604Z loubei
2021-12-27T06:18:07.152696312Z loubei

可选项
-f: 跟随日志的更新而更新
-t：时间戳
--tail n: 显示最后n条记录
~~~

#### **查看容器内进程的信息**

~~~shell
[root@bepigkiller /] `docker top 59361ced837b
UID                 PID                 PPID                C                   STIME               TTY                 TIME                CMD
root                180870              180852              0                   14:14               ?                   00:00:00            /bin/sh -c while true;do echo loubei;sleep 1;done
root                181479              180870              0                   14:23               ?                   00:00:00            /usr/bin/coreutils --coreutils-prog-shebang=sleep /usr/bin/sleep 1

~~~

#### **查看容器/镜像的元数据**

~~~shell
[root@bepigkiller /] `docker inspect 59361ced837b
~~~

~~~shell
运行结果
[
    {   
        "Id": "59361ced837b15abdcb57a3063ff4ac1a1a1b5a47f74bf09a7edee249433febd",  容器ID
        "Created": "2021-12-27T06:14:22.211002196Z",
        "Path": "/bin/sh",
        "Args": [
            "-c",
            "while true;do echo loubei;sleep 1;done"
        ],
        "State": {
            "Status": "running",
            "Running": true,
            "Paused": false,
            "Restarting": false,
            "OOMKilled": false,
            "Dead": false,
            "Pid": 180870,
            "ExitCode": 0,
            "Error": "",
            "StartedAt": "2021-12-27T06:14:22.760666418Z",
            "FinishedAt": "0001-01-01T00:00:00Z"
        },
        "Image": "sha256:5d0da3dc976460b72c77d94c8a1ad043720b0416bfc16c52c45d4847e53fadb6",
        "ResolvConfPath": "/var/lib/docker/containers/59361ced837b15abdcb57a3063ff4ac1a1a1b5a47f74bf09a7edee249433febd/resolv.conf",
        "HostnamePath": "/var/lib/docker/containers/59361ced837b15abdcb57a3063ff4ac1a1a1b5a47f74bf09a7edee249433febd/hostname",
   ...
]
~~~

#### **进入当前正在运行的容器**

容器通常都是使用后台方式运行的，需要进入容器修改配置文件

~~~shell
[root@bepigkiller /] `docker exec -it 59361ced837b  /bin/bash` 进入正在执行的容器，分配一个伪终端
[root@59361ced837b /] ls
bin  dev  etc  home  lib  lib64  lost+found  media  mnt  opt  proc  root  run  sbin  srv  sys  tmp  usr  var
# 参数
-it: 分配一个伪终端运行容器
~~~

~~~shell
[root@bepigkiller /] `docker attach 59361ced837b` 连接正在运行的容器，进入正在执行的终端
~~~

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112271446916.png" alt="image-20211227144649884" style="float:left;" />

#### **从容器内复制文件到主机**

~~~shell
[root@bepigkiller ~] `docker cp 516c14a61b2d:/home/test.java /home
[root@bepigkiller ~] cd ..
[root@bepigkiller /] cd home
[root@bepigkiller home] ls
coding  test.java
~~~

#### **查看docker状态**

~~~shell
[root@bepigkiller ~] `docker stats

CONTAINER ID   NAME            CPU %     MEM USAGE / LIMIT     MEM %     NET I/O           BLOCK I/O         PIDS
cdae95fce5e8   elasticsearch   0.00%     1.236GiB / 1.927GiB   64.16%    1.27kB / 0B       304MB / 729kB     43
9588b71d1afa   tomcat          0.17%     83.69MiB / 1.927GiB   4.24%     12.9kB / 8.59kB   56.8MB / 32.8kB   19
~~~

#### **curl命令**

~~~shell
[root@bepigkiller ~] `curl localhost:9200
{
  "name" : "cdae95fce5e8",
  "cluster_name" : "docker-cluster",
  "cluster_uuid" : "wUWj-xWpQ_u7lLimUtwHGA",
  "version" : {
    "number" : "7.6.2",
    "build_flavor" : "default",
    "build_type" : "docker",
    "build_hash" : "ef48eb35cf30adf4db14086e8aabd07ef6fb113f",
    "build_date" : "2020-03-26T06:34:37.794943Z",
    "build_snapshot" : false,
    "lucene_version" : "8.4.0",
    "minimum_wire_compatibility_version" : "6.8.0",
    "minimum_index_compatibility_version" : "6.0.0-beta1"
  },
  "tagline" : "You Know, for Search"
}
~~~

### 提交镜像

![image-20211228171242974](https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112281712082.png)

在原有镜像的基础上，添加自己改动的内容，在本地发布镜像.

~~~shell
CONTAINER ID   IMAGE          COMMAND             CREATED         STATUS         PORTS                                       NAMES
0dbc1f24c057   b8e65a4d736d   "catalina.sh run"   8 minutes ago   Up 7 minutes   0.0.0.0:8080->8080/tcp, :::8080->8080/tcp   crazy_khorana
[root@bepigkiller ~] `docker commit -a="loubei" -m="add webapp" 0dbc1f24c057 tomcat2:v1.0
sha256:66de7088096f5b7ddb2f40f11a25466d7ec34c7c1e7f445719cac41939996abd
[root@bepigkiller ~] `docker images
REPOSITORY      TAG       IMAGE ID       CREATED         SIZE
tomcat2         v1.0      66de7088096f   5 seconds ago   685MB   //增加了新的镜像
tomcat          9         b8e65a4d736d   4 days ago      680MB
nginx           latest    f6987c8d6ed5   6 days ago      141MB
mysql           8         3218b38490ce   6 days ago      516MB
centos          latest    5d0da3dc9764   3 months ago    231MB
elasticsearch   7.6.2     f29a1ee41030   21 months ago   791MB      
~~~

## 容器数据卷

### 介绍

docker提出Volume的概念，实现了两部分功能:

- 能够保存（持久化）数据
- 共享容器之间的数据

了解docker volume，需要知道docker的文件系统是如何工作的。

docker镜像由`多个文件系统（只读层）`叠加而成，当我们启动一个容器时，docker会加载只读层镜像并在其上（即镜像栈顶部）添加一个读写层。如果已经运行的容器修改了现有的文件，那么复制只读层的文件到读写层(Copy-On-Write)，<font color=orange>该文件只读层依然存在，只是已经被读写层中该文件的复制副本所隐藏。</font>

当删除docker容器，之前的修改将丢失。在docker中，只读层及在顶部的读写层组合被称为`Union File System（联合文件系统）`

<font color=pink>有没有办法实现删除docker容器后，那些修改还能保存呢？换句话说，我给某个docker容器额外添加了配置，如何才能给之后启动的容器都自动具备这些配置？</font>

**简单来说，Volume就是目录或文件，它绕过默认的联合文件系统，以正常的文件或目录的形式存在于宿主机。**

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112282003835.png" alt="image-20211228200319780" style="float:left" />

目录的挂载，将容器内的目录挂载到Linux的文件目录上

~~~shell
docker run -it -v 主机目录:容器目录 容器id /bin/bash
[root@bepigkiller home] `docker run -it -v /home/loubei:/home 5d0da3dc9764 /bin/bash
[root@bepigkiller loubei] `docker inspect bf1031dbecf2`  查看容器的详细信息
~~~

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112281726282.png" alt="image-20211228172654245" style="float:left" />

![image-20211228173422109](https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112281734175.png)

[安装MySQL](#安装MySQL)

### 具名挂载、匿名挂载、指定路径挂载

> 具名挂载和匿名挂载的本机文件都放置在 <font color=pink>/var/lib/docker/volumes</font>

- -v 具体名称:/容器内路径     #具名挂载
- -v /容器内路径             #匿名挂载
- -v /文件路径:/容器内路径    #指定路径挂载

~~~shell
1. 具名挂载
[root@bepigkiller ~] `docker run -d -P -v jumingNginx:/etc/nginx nginx
c25ac5ea52a702b132f551524c41a8833c3035a4e7bee4c6d831769102e12e5d
2. 匿名挂载
[root@bepigkiller ~] `docker run -d -P -v /etc/nginx nginx
c25ac5ea52a702b132f551524c41a8833c3035a4e7bee4c6d831769102e12e5d
3. 指定路径挂载
[root@bepigkiller home] `docker run -it -v /home/loubei:/home 5d0da3dc9764 /bin/bash
~~~

使用`docker volume`指令查看数据卷

~~~shell
[root@bepigkiller ~] docker volume ls
DRIVER    VOLUME NAME
local     085dd0eee29eae497ec71baa37b8da82befbae85db1d32edaf7c33f8f1fdfddd
local     95b72ebf663afa615813a2538e858a2d3b31535a8fe541ac2d934602471b4a86
local     jumingNginx
[root@bepigkiller ~] docker volume inspect jumingNginx
[
    {
        "CreatedAt": "2021-12-28T20:11:15+08:00",
        "Driver": "local",
        "Labels": null,
        "Mountpoint": "/var/lib/docker/volumes/jumingNginx/_data",
        "Name": "jumingNginx",
        "Options": null,
        "Scope": "local"
    }
]
[root@bepigkiller ~] cd /var/lib/docker
[root@bepigkiller docker] ls
buildkit  containers  image  network  overlay2  plugins  runtimes  swarm  tmp  trust  volumes
[root@bepigkiller docker] cd volumes
[root@bepigkiller volumes] ls
085dd0eee29eae497ec71baa37b8da82befbae85db1d32edaf7c33f8f1fdfddd  95b72ebf663afa615813a2538e858a2d3b31535a8fe541ac2d934602471b4a86  backingFsBlockDev  jumingNginx  metadata.d
~~~

设置卷的权限

~~~shell
1. 只读
docker run -it -v /home/loubei:/home:ro 5d0da3dc9764 /bin/bash
2. 读写
docker run -it -v /home/loubei:/home:rw 5d0da3dc9764 /bin/bash
~~~

### 初识DockerFile

> DockerFile:构建Docker镜像的命令脚本。（镜像的创建之前曾用commit实现过)

~~~shell
1. 构建docerkfile文件
    FROM centos

    VOLUME ["volume01","volume02"]

    CMD echo "-----end-----"
    CMD /bin/bash
2. 运行 docker build -f 文件路径 -t 镜像名称:版本 .
	docker build -f /home/DockerFile-Test/dockerfile1 -t mycentos:1.0 .
~~~

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112282107770.png" alt="image-20211228210729714" style="float:left" />

在构建`dockerfile`文件时，有这样一行代码`VOLUME ["volume01", "volume02"]`

这其实是直接在dockerfile文件中匿名挂载

~~~shell
[root@bepigkiller DockerFile-Test] docker run -it db6caea1012c /bin/bash
[root@3dc3ac950cdb /] ls
bin  dev  etc  home  lib  lib64  lost+found  media  mnt  opt  proc  root  run  sbin  srv  sys  tmp  usr  var  volume01	volume02
~~~

在本机里的文件挂载位置可以通过`docker inspect 容器id`查看

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112282113024.png" alt="image-20211228211321968" style="float:left" />

### 容器间的数据共享

~~~shell
1. 创建容器
2. 从已有的容器复制数据卷
	docker run -it --name=centos2 --volumes-from 3dc3ac950cdb mycentos:1.0
3. 修改其中一个容器volume里的数据
	[root@3dc3ac950cdb volume01] touch loubei.txt  容器1
4. 查看另一个容器volume里的数据是否更新
	[root@723cf4bb584a volume01] ls                容器2
	loubei.txt                                     
~~~

![image-20211228214742777](https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112282147822.png)

![image-20211228214940778](https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112282149834.png)

## DockerFile

### 步骤

使用DockerFile的步骤：

1. 编写DockerFile文件
2. docker build 将文件构造为镜像
3. docker run 运行镜像
4. docker push 发布镜像(DockerHub、阿里云镜像仓库)

### 编写

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202201012127263.png" alt="image-20220101212731180" style="float:left" />

~~~shell
FROM                  # 基础镜像
MAINTAINER            # 镜像是谁写的，姓名+邮箱
RUN                   # 构建镜像时需要运行的命令
ADD                   # 添加内容
WORKDIR               # 当前镜像的工作目录
VOLUME                # 挂载的目录
EXPOSE                # 暴露端口
RUN                   # 运行
CMD                   # 容器启动的时候要运行的命令，可被替代
ENTRYPOINT            # 容器启动的时候要运行的命令，可追加命令
ONBUILD               # 当文件被继承时会触发设置的指令
COPY                  # 将文件复制到镜像中
ENV                   # 环境变量
~~~

~~~shell
[root@bepigkiller DockerFile-Test] cat dockerfile2
FROM centos
MAINTAINER loubei<870965470@qq.com>

ENV MYPATH /user/local
WORKDIR $MYPATH

RUN yum -y install vim
RUN yum -y install net-tools

EXPOSE 80

CMD echo MYPATH
CMD "-----end-----"
CMD /bin/bash
~~~

### 构建镜像

~~~shell
docker build -f /home/DockerFile-Test/dockerfile2 -t mycentos:2.0 .
~~~

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202201012220048.png" alt="image-20220101222002996" style="float:left;" />

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202201012221300.png" alt="image-20220101222137254" style="float:left" />

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202201012132640.png" alt="image-20220101213211571" style="float:left" />

### 运行

~~~shell
docker run -it 942c1b0bdabb
~~~

新的镜像可以使用`ifconfig`和`vim`

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202201030910884.png" alt="image-20220103091007841" style="float:left" />

通过使用`docker history 镜像ID`可以查看镜像的构造历史

~~~shell
[root@bepigkiller ~] `docker history 942c1b0bdabb
IMAGE          CREATED        CREATED BY                                      SIZE      COMMENT
942c1b0bdabb   35 hours ago   /bin/sh -c #(nop)  CMD ["/bin/sh" "-c" "/bin…   0B        
df3eb2f30c40   35 hours ago   /bin/sh -c #(nop)  CMD ["/bin/sh" "-c" "\"--…   0B        
34ea7c6fcb68   35 hours ago   /bin/sh -c #(nop)  CMD ["/bin/sh" "-c" "echo…   0B        
76a13838b088   35 hours ago   /bin/sh -c #(nop)  EXPOSE 80                    0B        
dc2b9970ba0b   35 hours ago   /bin/sh -c yum -y install net-tools             14.6MB    
e38cc0bea01f   35 hours ago   /bin/sh -c yum -y install vim                   66.2MB    
be7d66a3607b   35 hours ago   /bin/sh -c #(nop) WORKDIR /user/local           0B        
9e86d404cb55   35 hours ago   /bin/sh -c #(nop)  ENV MYPATH=/user/local       0B        
3a844c528c54   35 hours ago   /bin/sh -c #(nop)  MAINTAINER loubei<8709654…   0B        
5d0da3dc9764   3 months ago   /bin/sh -c #(nop)  CMD ["/bin/bash"]            0B        
<missing>      3 months ago   /bin/sh -c #(nop)  LABEL org.label-schema.sc…   0B        
<missing>      3 months ago   /bin/sh -c #(nop) ADD file:805cb5e15fb6e0bb0…   231MB     
~~~

### CMD和ENTRYPOINT的区别

> CMD                   # 容器启动的时候要运行的命令，可被替代
> ENTRYPOINT            # 容器启动的时候要运行的命令，可追加命令

测试cmd

~~~shell
# 编写dockerfile文件
    [root@bepigkiller DockerFile-Test] cat dockerfiletest
    FROM centos
    CMD ["ls","-a"]
# 构建镜像
[root@bepigkiller DockerFile-Test] docker build -f /home/DockerFile-Test/dockerfiletest -t centostest .
    Sending build context to Docker daemon  4.096kB
    Step 1/2 : FROM centos
     ---> 5d0da3dc9764
    Step 2/2 : CMD ["ls","-a"]
     ---> Running in fd7136d3c5de
    Removing intermediate container fd7136d3c5de
     ---> ddd1a1d1b65e
    Successfully built ddd1a1d1b65e
    Successfully tagged centostest:latest
# 运行镜像,发现运行了 ls -a 命令
[root@bepigkiller DockerFile-Test] docker run ddd1a1d1b65e
    .
    ..
    .dockerenv
    bin
    dev
    etc
    home
    lib
    lib64
    lost+found
    media
    mnt
    ...
# 如果想执行ls -al,不能直接追加-l。由于-l不是命令，因此报错
[root@bepigkiller DockerFile-Test] `docker run ddd1a1d1b65e -l
docker: Error response from daemon: OCI runtime create failed: container_linux.go:380: starting container process caused: exec: "-l": executable file not found in $PATH: unknown.
# 执行完整的命令才可生效
[root@bepigkiller DockerFile-Test] docker run ddd1a1d1b65e ls -al
total 56
drwxr-xr-x   1 root root 4096 Jan  3 01:49 .
drwxr-xr-x   1 root root 4096 Jan  3 01:49 ..
-rwxr-xr-x   1 root root    0 Jan  3 01:49 .dockerenv
lrwxrwxrwx   1 root root    7 Nov  3  2020 bin -> usr/bin
drwxr-xr-x   5 root root  340 Jan  3 01:49 dev
drwxr-xr-x   1 root root 4096 Jan  3 01:49 etc
drwxr-xr-x   2 root root 4096 Nov  3  2020 home
lrwxrwxrwx   1 root root    7 Nov  3  2020 lib -> usr/lib
lrwxrwxrwx   1 root root    9 Nov  3  2020 lib64 -> usr/lib64
drwx------   2 root root 4096 Sep 15 14:17 lost+found
drwxr-xr-x   2 root root 4096 Nov  3  2020 media
drwxr-xr-x   2 root root 4096 Nov  3  2020 mnt
drwxr-xr-x   2 root root 4096 Nov  3  2020 opt
dr-xr-xr-x 183 root root    0 Jan  3 01:49 proc
dr-xr-x---   2 root root 4096 Sep 15 14:17 root
drwxr-xr-x  11 root root 4096 Sep 15 14:17 run
lrwxrwxrwx   1 root root    8 Nov  3  2020 sbin -> usr/sbin
drwxr-xr-x   2 root root 4096 Nov  3  2020 srv
dr-xr-xr-x  13 root root    0 Jan  3 01:49 sys
drwxrwxrwt   7 root root 4096 Sep 15 14:17 tmp
drwxr-xr-x  12 root root 4096 Sep 15 14:17 usr
drwxr-xr-x  20 root root 4096 Sep 15 14:17 var
~~~

测试entrypoint

~~~shell
# 编写entrypoint镜像
[root@bepigkiller DockerFile-Test] cat dockerEntryPointTest 
    FROM centos
    ENTRYPOINT ["ls","-a"]
# 构建镜像
[root@bepigkiller DockerFile-Test] docker build -f /home/DockerFile-Test/dockerEntryPointTest -t centosentrypoint .
    Sending build context to Docker daemon   5.12kB
    Step 1/2 : FROM centos
     ---> 5d0da3dc9764
    Step 2/2 : ENTRYPOINT ["ls","-a"]
     ---> Running in ac7f0f56e5a5
    Removing intermediate container ac7f0f56e5a5
     ---> c1df705c78b4
    Successfully built c1df705c78b4
    Successfully tagged centosentrypoint:latest
# 直接追加命令 -l => ls -al
[root@bepigkiller DockerFile-Test] docker run c1df705c78b4 -l
    total 56
    drwxr-xr-x   1 root root 4096 Jan  3 01:53 .
    drwxr-xr-x   1 root root 4096 Jan  3 01:53 ..
    -rwxr-xr-x   1 root root    0 Jan  3 01:53 .dockerenv
    lrwxrwxrwx   1 root root    7 Nov  3  2020 bin -> usr/bin
    drwxr-xr-x   5 root root  340 Jan  3 01:53 dev
    drwxr-xr-x   1 root root 4096 Jan  3 01:53 etc
    drwxr-xr-x   2 root root 4096 Nov  3  2020 home
    lrwxrwxrwx   1 root root    7 Nov  3  2020 lib -> usr/lib
    lrwxrwxrwx   1 root root    9 Nov  3  2020 lib64 -> usr/lib64
    drwx------   2 root root 4096 Sep 15 14:17 lost+found
    drwxr-xr-x   2 root root 4096 Nov  3  2020 media
    drwxr-xr-x   2 root root 4096 Nov  3  2020 mnt
    drwxr-xr-x   2 root root 4096 Nov  3  2020 opt
    dr-xr-xr-x 184 root root    0 Jan  3 01:53 proc
    dr-xr-x---   2 root root 4096 Sep 15 14:17 root
    drwxr-xr-x  11 root root 4096 Sep 15 14:17 run
    lrwxrwxrwx   1 root root    8 Nov  3  2020 sbin -> usr/sbin
    drwxr-xr-x   2 root root 4096 Nov  3  2020 srv
    dr-xr-xr-x  13 root root    0 Jan  3 01:53 sys
    drwxrwxrwt   7 root root 4096 Sep 15 14:17 tmp
    drwxr-xr-x  12 root root 4096 Sep 15 14:17 usr
    drwxr-xr-x  20 root root 4096 Sep 15 14:17 var
~~~

### 发布镜像到DockerHub上

1. 在服务器上登录dockerhub的账号

~~~shell
[root@bepigkiller tomcat] `docker login -u leib
Password: 
WARNING! Your password will be stored unencrypted in /root/.docker/config.json.
Configure a credential helper to remove this warning. See
https://docs.docker.com/engine/reference/commandline/login/#credentials-store

Login Succeeded
~~~

2. 发布自己的镜像

~~~shell
[root@bepigkiller tomcat] `docker tag 519c2eecdf6f leib/mytomcat:1.0
[root@bepigkiller tomcat] `docker images
REPOSITORY         TAG       IMAGE ID       CREATED             SIZE
leib/mytomcat      1.0       519c2eecdf6f   About an hour ago   690MB
mytomcat           1.0       519c2eecdf6f   About an hour ago   690MB
[root@bepigkiller tomcat] `docker push leib/mytomcat:1.0
The push refers to repository [docker.io/leib/mytomcat]
44dd2d9610ae: Pushed 
4039d4bc321e: Pushed 
445fa30f5147: Pushed 
2ee9a58bd49f: Pushed 
74ddd0ec08fa: Pushed 
1.0: digest: sha256:7c48459a8670b21eeea67ab9c2dd3d293f9b3e89f24ec018c6a5cebd080422cb size: 1373
~~~

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201031317176.png" alt="image-20220103131729124" style="zoom:88%;float:left" />

## Docker网络

- 宿主机与Docker容器的通信
- 容器与容器的通信
- 外部与容器的通信

Docker使用Linux桥接，在宿主机虚拟一个Docker容器网桥(docker0)，Docker启动一个容器时会根据Docker网桥的网段分配给容器一个IP地址，称为Container-IP。

同时Docker网桥是每个容器的默认网关。因为在同一宿主机内的容器都接入同一个网桥，这样容器之间就能够通过容器的Container-IP直接通信。

### Veth-pair

> veth-pair 是一对虚拟的设备接口，都是成对出现的。一端连着协议栈，一端彼此相连着
>
> 充当桥梁，连接虚拟的网络设备

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201031619813.png" alt="image-20220103161906745" style="zoom:90%;float:left" />

### Docker0

> 只要安装了docker，机器上就会有docker0网卡

~~~shell
[root@bepigkiller ~] ip addr
# 本机回环地址
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host 
       valid_lft forever preferred_lft forever
# 阿里云内网地址
2: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc fq_codel state UP group default qlen 1000
    link/ether 00:16:3e:17:54:9c brd ff:ff:ff:ff:ff:ff
    inet 172.25.16.191/20 brd 172.25.31.255 scope global dynamic noprefixroute eth0
       valid_lft 307672736sec preferred_lft 307672736sec
    inet6 fe80::216:3eff:fe17:549c/64 scope link 
       valid_lft forever preferred_lft forever
# docker内网地址
3: docker0: <NO-CARRIER,BROADCAST,MULTICAST,UP> mtu 1500 qdisc noqueue state DOWN group default 
    link/ether 02:42:a4:03:ef:92 brd ff:ff:ff:ff:ff:ff
    inet 172.17.0.1/16 brd 172.17.255.255 scope global docker0
       valid_lft forever preferred_lft forever
    inet6 fe80::42:a4ff:fe03:ef92/64 scope link 
       valid_lft forever preferred_lft forever
~~~

> 只要启动了一个容器，就会开启一个网卡
>

~~~shell
[root@bepigkiller ~] `docker exec -it d6f7283a1d7b ip addr
# 回环地址
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
# 容器ip地址
152: eth0@if153: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc noqueue state UP group default 
    link/ether 02:42:ac:11:00:02 brd ff:ff:ff:ff:ff:ff link-netnsid 0
    inet 172.17.0.2/16 brd 172.17.255.255 scope global eth0
       valid_lft forever preferred_lft forever
~~~

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201031429137.png" alt="image-20220103142918073" style="zoom:89%;float:left" />

#### 容器与容器的ping

~~~shell
root@d6f7283a1d7b:/usr/local/tomcat ping 172.17.0.3
PING 172.17.0.3 (172.17.0.3) 56(84) bytes of data.
64 bytes from 172.17.0.3: icmp_seq=1 ttl=64 time=0.099 ms
64 bytes from 172.17.0.3: icmp_seq=2 ttl=64 time=0.087 ms
64 bytes from 172.17.0.3: icmp_seq=3 ttl=64 time=0.077 ms
~~~

#### 查看network

~~~shell
[root@bepigkiller ~] docker network ls
NETWORK ID     NAME      DRIVER    SCOPE
979f411a4781   bridge    bridge    local    # docker0
80ef70f7199f   host      host      local
a364340905ac   none      null      local
[root@bepigkiller ~] docker network inspect 979f411a4781
~~~

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201031600958.png" alt="image-20220103160049883" style="float:left" />

#### 问题

- 如果tomcat容器内没有ip addr，执行apt update && apt install -y iproute2

- 如果tomcat容器内没有ping指令，执行apt-get update、apt install iputils-ping、apt install net-tools

### 自定义网络

>存在的问题：只能在容器内部的执行命令中与其他容器通信，无法在宿主机命令行中实现容器与容器之间的通信，也无法实现域名与域名的互ping
>
>[root@bepigkiller etc]# docker exec -it 3dcb0d0e0b5e ping d6f7283a1d7b
>ping: d6f7283a1d7b: Name or service not known

#### 网络模式

- bridge：桥接
- host：和宿主机共享网络
- container：容器网络联通
- none：不配置网络

#### 测试

~~~shell
## 默认运行一个容器执行的网络模式是桥接,--net bridge可以不写
docker run -d -P --name tomcat01 --net bridge tomcat
## 创建自己的网络模式
-- driver 网络模式
-- subnet 子网
-- gateway 网关
[root@bepigkiller ~] `docker network create --driver bridge --subnet 192.168.0.0/16 --gateway 192.168.0.1 mynet
6dadd34a27c4f7680ba90abc421c9c29d7e419d792601e2200f099971b75a0d3
~~~

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201041322267.png" alt="image-20220104132216213" style="float:left" />

<font color=orange>在自定义网络后，容器可以使用自定义的网络</font>

~~~shell
[root@bepigkiller ~] docker run -d -P --name tomcat-net-1 --net mynet tomcat
68242a86f18f64c9bca034089e47627e3392bcf51f8db9f6bc91e6d810143bf9
[root@bepigkiller ~] docker run -d -P --name tomcat-net-2 --net mynet tomcat
957fbbbfd09c014652eb53698048803ab18cadc14e837c9b860b40aa1dbf33ee
[root@bepigkiller ~] docker network inspect mynet
~~~

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201041332881.png" alt="image-20220104133249814" style="zoom:100%;float:left" />

使用自定义的网络，同一网络中两个不同的容器可以使用名称进行通信

~~~shell
[root@bepigkiller ~] `docker exec -it tomcat-net-1 ping tomcat-net-2
PING tomcat-net-2 (192.168.0.3) 56(84) bytes of data.
64 bytes from tomcat-net-2.mynet (192.168.0.3): icmp_seq=1 ttl=64 time=0.112 ms
64 bytes from tomcat-net-2.mynet (192.168.0.3): icmp_seq=2 ttl=64 time=0.064 ms
~~~

<font color=orange>不同的集群使用不同的子网网络，保证集群是健康且安全的。</font>

### 网络连通

> 不同的子网之间的容器是可以实现网络通信的

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201041443141.png" alt="image-20220104144349070" style="zoom:88%;float:left" />

~~~shell
[root@bepigkiller ~] `docker network connect mynet tomcat-d-1
[root@bepigkiller ~] `docker exec -it tomcat-net-1 ping tomcat-d-1
PING tomcat-d-1 (192.168.0.4) 56(84) bytes of data.
64 bytes from tomcat-d-1.mynet (192.168.0.4): icmp_seq=1 ttl=64 time=0.121 ms
64 bytes from tomcat-d-1.mynet (192.168.0.4): icmp_seq=2 ttl=64 time=0.083 ms
[root@bepigkiller ~] `docker network inspect mynet
~~~

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201041429497.png" alt="image-20220104142941430" style="zoom:100%;float:left" />



## DockerCompose

### 官方文档

Compose is a tool for defining and running multi-container Docker applications. With Compose, you use a YAML file to configure your application’s services. Then, with a single command, you create and start all the services from your configuration. To learn more about all the features of Compose, see [the list of features](https://docs.docker.com/compose/#features).

Compose works in all environments: production, staging, development, testing, as well as CI workflows. You can learn more about each case in [Common Use Cases](https://docs.docker.com/compose/#common-use-cases).

- Compose是多容器Docker应用的工具
- 使用yaml文件
- 通过命令，从配置文件中创建和启动所有的服务
- 适用于所有的环境

Using Compose is basically a three-step process:

1. Define your app’s environment with a `Dockerfile` so it can be reproduced anywhere.
2. Define the services that make up your app in `docker-compose.yml` so they can be run together in an isolated environment.
3. Run `docker compose up` and the [Docker compose command](https://docs.docker.com/compose/cli-command/) starts and runs your entire app. You can alternatively run `docker-compose up` using the docker-compose binary.

- 定义Dockerfile文件
- 定义docker-compose.yml文件
- 启动项目

### 安装

1. 下载compose

~~~shell
curl -L https://get.daocloud.io/docker/compose/releases/download/1.26.2/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose
~~~

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201042150944.png" alt="image-20220104215031893" style="float:left" />

2. 授权

~~~shell
sudo chmod +x /usr/local/bin/docker-compose
~~~

3. 查看版本信息

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201042153165.png" alt="image-20220104215349118" style="float:left" />

### 官方体验

[Get started with Docker Compose | Docker Documentation](https://docs.docker.com/compose/gettingstarted/)

1. 应用
2. 编写应用的Dockerfile

~~~dockerfile
FROM python:3.7-alpine
WORKDIR /code
ENV FLASK_APP=app.py
ENV FLASK_RUN_HOST=0.0.0.0
RUN apk add --no-cache gcc musl-dev linux-headers
COPY requirements.txt requirements.txt
RUN pip install -r requirements.txt
EXPOSE 5000
COPY . .
CMD ["flask", "run"]
~~~

3. 编写服务的docker-compose.yml(定义整个环境，redis、web等完整的上线服务)

~~~shell
version: "3.9"
services:
  web:
    build: .
    ports:
      - "5000:5000"
  redis:
    image: "redis:alpine"
~~~

4. 执行`docker-compose up`

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201050014993.png" alt="image-20220105001437915" style="float:left" />

查看docker images

~~~shell
[root@bepigkiller composetest] docker images
REPOSITORY        TAG          IMAGE ID       CREATED             SIZE
composetest_web   latest       12eadab5b5ff   About an hour ago   185MB  # 官方项目添加的
dommo             1.0          67d0ea256eee   17 hours ago        669MB
tomcat            latest       fb5657adc892   13 days ago         680MB
redis             alpine       3900abf41552   5 weeks ago         32.4MB #
python            3.7-alpine   a1034fd13493   5 weeks ago         41.8MB #  
java              8            d23bdf5b1b1b   4 years ago         643MB
~~~

docker-compose 会自动创建一个网络`composetest-default`，应用的所有服务都位于该网络中

~~~shell
[root@bepigkiller composetest] docker network ls
NETWORK ID     NAME                  DRIVER    SCOPE
979f411a4781   bridge                bridge    local
ffdd96c333e6   composetest_default   bridge    local  # docker-compose 创建的网络
80ef70f7199f   host                  host      local
6dadd34a27c4   mynet                 bridge    local
a364340905ac   none                  null      local
~~~

### yml文件的编写规则

[Compose file version 3 reference | Docker Documentation](https://docs.docker.com/compose/compose-file/compose-file-v3/)

docker-compose的填写的版本与docker版本的关系

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201050034239.png" alt="image-20220105003414175" style="float:left" />

~~~shell
# 版本配置
version: ''
# 服务配置
services: 
	服务1：
	   # 服务配置
	   images
	   build
	   netword
	   ...
	服务2：
	   # 服务配置
	   ...
	服务3：
	   # 服务配置
	   ...
# 其他配置
volumes:
networks:
configs:
~~~

### 补充

`docker-compose down`：运行该指令会停止项目，同时也会删除容器

## Linux

### 防火墙

~~~shelll
systemctl status firewalld		 			#查看firewall防火墙状态
firewall-cmd --list-ports					#查看firewall防火墙开放端口
systemctl start firewalld.service			#打开firewall防火墙
systemctl stop firewalld.service			#关闭firewall防火墙
firewall-cmd --reload						#重启firewal防火墙
systemctl disable firewalld.service			#禁止firewall开机启动  

#开放firewall防火墙端口，需重启防火墙生效
firewall-cmd --zone=public --add-port=80/tcp --permanent 	

命令含义:
–zone #作用域
–add-port=80/tcp #添加端口，格式为：端口/通讯协议
–permanent #永久生效，没有此参数重启后失效
~~~

## 安装软件

### 安装Nginx

~~~shell
docker search nginx
docker pull nginx
docker run -d --name nginx01 -p 8080:80 nginx 将宿主机的8080端口映射到docker容器的80端口

[root@bepigkiller coding]# docker ps
CONTAINER ID   IMAGE     COMMAND                  CREATED         STATUS         PORTS                                   NAMES
aff21d2f0cab   nginx     "/docker-entrypoint.…"   9 seconds ago   Up 8 seconds   0.0.0.0:8080->80/tcp, :::8080->80/tcp   nginx01
~~~

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112271534145.png" alt="image-20211227153413099" style="float:left" />

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112271613885.png" alt="image-20211227161342839" style="float:left" />

在nginx里可以配置nginx.conf文件，设置nginx监听的容器端口

~~~shell
events{
	worker_connections 1024;
}
http{
	server {
        	listen 80;
        	location / {
                	root /usr/share/nginx/html;
                	try_files $uri $uri/ /index.html;
                	index index.html index.htm;
        	}
	}
}
~~~

### 安装ElasticSearch

~~~shell
[root@bepigkiller ~] `docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:7.6.2
~~~

调用`docker stats`发现ElasticSearch非常占内存，需要添加额外的选项来限制内存

~~~shell
docker run -d --name elasticsearch02 -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -e ES_JAVA_OPTS="-Xms64m -Xmx512m"  elasticsearch:7.6.2

# 可选项
-e ES_JAVA_OPTS="xxx" 进程占用的最小内存和最大内存
~~~

### 安装MySQL

<a name="安装MySQL">查看是否存在mysql镜像

~~~shell
docker images
~~~

启动mysql容器

~~~shell
docker run -d --name mysql -p 3306:3306 -v /home/mysql/conf:/etc/mysql/conf.d -v /home/mysql/data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=root 3218b38490ce

-d: 参数
-p: 端口映射
-v: 目录映射
-e: 环境，设置密码
~~~

启动后使用SQLYOG连接，会出现问题。

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112281821467.png" alt="image-20211228182153425" style="zoom:80%;float:left" />

执行下面三步可解决问题

~~~shelll
1. [root@bepigkiller home]# docker exec -it mysql /bin/bash
2. root@95c46b888abe:/# mysql --user=root --password
    Enter password: 
    Welcome to the MySQL monitor.  Commands end with ; or \g.
    Your MySQL connection id is 11
    Server version: 8.0.27 MySQL Community Server - GPL

    Copyright (c) 2000, 2021, Oracle and/or its affiliates.

    Oracle is a registered trademark of Oracle Corporation and/or its
    affiliates. Other names may be trademarks of their respective
    owners.

    Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.
3. mysql> ALTER  USER  'root'  IDENTIFIED  WITH  mysql_native_password  BY  'root';
~~~

创建renren-fast数据库

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112281940202.png" alt="image-20211228194019164" style="float:left" />

容器内文件目录与linux目录里文件同步更新

![image-20211228194058419](https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202112281940457.png)

</a>

<font color=orange>Linux下mysql是区分大小写的，同时mysql 8.0 版本开始初始化后就不能修改配置文件</font>

因此`不能`在mysql容器内`\etc\mysql\my.conf`修改配置文件，添加`lower_case_table_names=1`(8之前的版本可以)

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201051537007.png" alt="image-20220105153701951" style="float:left" />

![image-20220105174719982](https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201051747035.png)

### 实战TomCat

1. 准备镜像，tomcat以及jdk的压缩包

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202201031025492.png" alt="image-20220103102511449" style="float:left" />

2. 编写dockerfile文件，官方命名`Dockerfile`，build会自动寻找这个文件，就不需要-f指定了

~~~shell
[root@bepigkiller loubei] cat Dockerfile
    FROM centos
	# 操作者
    MAINTAINER loubei<870965470@qq.com>
    # 复制readme文件
    COPY readme.txt /usr/local/readme.txt
    # 添加tomcat、jdk压缩包,压缩至/user/local目录下
    ADD jdk8.tar.gz /usr/local
    ADD apache-tomcat-9.0.56.tar.gz /usr/local
    # 导入vim
    RUN yum -y install vim
    # 配置工作目录
    ENV MYPATH usr/local
    WORKDIR $MYPATH
    # 配置环境变量
    ENV JAVA_HOME /usr/local/jdk1.8.0_144
    ENV CLASSPATH $JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
    ENV CATALINA_HOME /usr/local/apache-tomcat-9.0.56
    ENV CATALINA_BASE /usr/local/apache-tomcat-9.0.56
    ENV PATH $PATH:$JAVA_HOME/bin:$CATALINA_HOME/lib:$CATALINA_HOME/bin
    # 暴露端口
    EXPOSE 8080
    # 启动容器后执行的命令，启动tomcat及日志
    CMD /usr/local/apache-tomcat-9.0.56/bin/startup.sh && tail -F /usr/local/apache-tomcat-9.0.56/bin/logs/catalina.out
~~~

3. 构建镜像

~~~shell
[root@bepigkiller loubei] docker build -t mytomcat:1.0 .
~~~

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202201031052538.png" alt="image-20220103105210491" style="float:left" />

4. 启动容器

~~~shell
[root@bepigkiller loubei] docker run -d -p 9090:8080 --name loutomcat -v /home/loubei/build/tomcat/test:/usr/local/apache-tomcat-9.0.56/webapps/test -v /home/loubei/build/tomcat/logs:/usr/local/apache-tomcat-9.0.56/logs mytomcat:1.0  
456748d0da27eb215d9ed1f844700c87c04926873554cd7998ea38cf4ae03718
~~~

**主机与容器的数据挂载已经完成**

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202201031110629.png" alt="image-20220103111012586" style="float:left" />

**进入容器**

~~~shell
[root@bepigkiller tomcat] docker exec -it loutomcat /bin/bash
[root@456748d0da27 local] ls
# 容器内部已有解压完成的tomcat和jdk
aegis  apache-tomcat-9.0.56  bin  etc  games  include  jdk1.8.0_144  lib  lib64  libexec  readme.txt  sbin  share  src
~~~

5. 访问容器

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202201031117660.png" alt="image-20220103111705595" style="zoom:95%;float:left" />

6. 发布项目（由于数据卷的挂载，可以直接在本地发布项目了）

~~~shell
[root@bepigkiller test] pwd
/home/loubei/build/tomcat/test
# 在test目录下创建WEB-INF目录
[root@bepigkiller test] mkdir WEB-INF
# 在WEB-INF目录下创建web.xml文件
[root@bepigkiller WEB-INF] vim web.xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app version="2.4" 
	 xmlns="http://java.sun.com/xml/ns/j2ee" 
	 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	 xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee 
	 http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd">
</web-app>
# 在test目录下创建index.html
[root@bepigkiller test] vim index.jsp 
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
    <!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>你好,Docker</title>
</head>
<body>
Hello World!<br/>
<% System.out.println("-----my test web logs -------"); %>
</body>
</html>
~~~

<img src="https://raw.githubusercontent.com/loubei1210-leib/Pic/main/img/202201031141596.png" alt="image-20220103114127548" style="zoom:95%;float:left" />

7. 查看访问日志

~~~shell
[root@bepigkiller tomcat] cd logs
[root@bepigkiller logs] ls
catalina.2022-01-03.log  catalina.out  host-manager.2022-01-03.log  localhost.2022-01-03.log  localhost_access_log.2022-01-03.txt  manager.2022-01-03.log
[root@bepigkiller logs] cat catalina.out
# 从日志中可以查到刚刚的访问记录
03-Jan-2022 03:35:18.757 INFO [Catalina-utility-2] org.apache.catalina.core.StandardContext.reload Reloading Context with name [/test] is completed
-----my test web logs -------
~~~

### Springboot打包镜像

1. 构建项目

2. 打包应用

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201041659272.png" alt="image-20220104165921219" style="zoom:95%;float:left" />

构建完成之后会在target目录下生成jar包

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201041711132.png" alt="image-20220104171122084" style="float:left" />

可以直接打开终端，输入`java -jar dommo-1.0.0.jar`，查看是否能运行

确保能运行之后，下载插件docker

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201041715678.png" alt="image-20220104171553619" style="zoom:80%;float:left" />

3. 编写Dockfile

~~~dockerfile
FROM java:8

COPY *.jar /app.jar

CMD ["--server.port=8383--"]

EXPOSE 8383

ENTRYPOINT ["java","-jar","app.jar"]
~~~

将jar包和Dockerfile上传到服务器

![image-20220104172424614](https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201041724675.png)

4. 构建镜像

~~~shell
[root@bepigkiller project] ls
Dockerfile  dommo-1.0.0.jar
[root@bepigkiller project] docker build -t dommo:1.0 .
Sending build context to Docker daemon  25.93MB
Step 1/5 : FROM java:8
8: Pulling from library/java
5040bd298390: Pull complete 
fce5728aad85: Pull complete 
76610ec20bf5: Pull complete 
60170fec2151: Pull complete 
e98f73de8f0d: Pull complete 
11f7af24ed9c: Pull complete 
49e2d6393f32: Pull complete 
bb9cdec9c7f3: Pull complete 
Digest: sha256:c1ff613e8ba25833d2e1940da0940c3824f03f802c449f3d1815a66b7f8c0e9d
Status: Downloaded newer image for java:8
 ---> d23bdf5b1b1b
Step 2/5 : COPY *.jar /app.jar
 ---> 4fcc86365d69
Step 3/5 : CMD ["--server.port=8383"]
 ---> Running in f4aa2d9ca3c7
Removing intermediate container f4aa2d9ca3c7
 ---> 8125ac2711ec
Step 4/5 : EXPOSE 8383
 ---> Running in 24785764215a
Removing intermediate container 24785764215a
 ---> 8a99d1665893
Step 5/5 : ENTRYPOINT ["java","-jar","app.jar"]
 ---> Running in 69c6db0b3996
Removing intermediate container 69c6db0b3996
 ---> 792ed68b225c
Successfully built 792ed68b225c
Successfully tagged dommo:1.0
~~~

5. 运行容器

~~~shell
[root@bepigkiller project] docker run -d -P --name dommo-Web dommo:1.0
8309b586a004487eebed515d33a2092a318a86117bf64e63fddcc686d3f5e171
~~~

6. 访问

~~~shell
[root@bepigkiller project] curl localhost:49162/hello
hello world[root@bepigkiller project]# 
~~~

### 实战WordPress

> Compose一键启动

开源项目：

1. 下载docker-compose.yml文件
2. 如果需要，编写Dockerfile文件
3. 一键启动

[Quickstart: Compose and WordPress | Docker Documentation](https://docs.docker.com/samples/wordpress/)

~~~shell
version: "3.8"
    
services:
  db:
    image: mysql:5.7
    volumes:
      - db_data:/var/lib/mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: somewordpress
      MYSQL_DATABASE: wordpress
      MYSQL_USER: wordpress
      MYSQL_PASSWORD: wordpress
    
  wordpress:
    depends_on:
      - db
    image: wordpress:latest
    volumes:
      - wordpress_data:/var/www/html
    ports:
      - "8000:80"
    restart: always
    environment:
      WORDPRESS_DB_HOST: db
      WORDPRESS_DB_USER: wordpress
      WORDPRESS_DB_PASSWORD: wordpress
      WORDPRESS_DB_NAME: wordpress
volumes:
  db_data: {}
  wordpress_data: {}
~~~

![image-20220105010304134](https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201050103197.png)

**直接访问**

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201050113518.png" alt="image-20220105011353434" style="zoom:80%;float:left" />

## 部署项目

2. 编写Dockerfile文件

~~~java
FROM java:8
EXPOSE 8080

VOLUME /tmp
ADD renren-fast.jar  /app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-jar","/app.jar"]
~~~

2. 编写docker-compose.yml文件

~~~java
version: "3"
services:
  nginx:
    image: nginx:latest
    ports:
    - "80:80"
    volumes:
    - /home/nginx/html:/usr/share/nginx/html
    - /home/nginx/nginx.conf:/etc/nginx/nginx.conf
    privileged: true # 解决文件调用的权限问题
  mysql:
    image: mysql:8.0.27
    ports:
    - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=root
    command: ["--lower_case_table_names=1"]    
  renren-fast:
    build: .
    ports:
      - "8080:8080"
    environment:
      - spring.profiles.active=dev
    depends_on:
      - mysql
~~~

4. 修改application-dev.yml中的mysql路径

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201051333839.png" alt="image-20220105133320714" style="float:left" />

5. 修改前端生产环境文件中对后端的API请求路径

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201101458361.png" alt="image-20220110145755684" style="zoom:80%;float:left" />

6. 前端文件的打包`npm run build`

<font color=orange>打包完成之后会生成dist目录</font>

<img src="https://raw.githubusercontent.com/Leibgo/Pic/main/img/202201101459048.png" alt="image-20220110145932998" style="float:left" />

7. 将dist目录下的所有文件上传到服务器`/home/project/medical_health/nginx/html`

8. 编写nginx.conf文件

~~~shell
[root@bepigkiller nginx] cat nginx.conf
events{
	worker_connections 1024;
}
http{
	server {
        	listen 80;
			server_name 47.98.179.176;
        	location / {
                	root /usr/share/nginx/html;
                	try_files $uri $uri/ /index.html;
                	index index.html index.htm;
        	}
	
	}
	// 出现css样式
	include   mime.types;
	default_type  application/octet-stream;
}
~~~

9. 将后端项目打包成jar包，同Dockerfile、docker-compose.yml放进服务器

~~~shell
[root@bepigkiller medical_health] ls
docker-compose.yml  Dockerfile  renren-fast.jar
~~~

10. 启动项目，并将数据库文件导入

