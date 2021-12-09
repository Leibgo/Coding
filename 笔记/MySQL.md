

# 高性能MySQL

~~~java
//MySQL最重要、最与众不同的特性是它的存储引擎架构，这种架构的设计将查询处理(Query Processing)及其他系统任务(Server Task)和数据的存储/提取相分离
~~~

![img](https://s2.loli.net/2021/12/07/gUvNeoPd7zCFnql.png)

## 第一章

### 并发

#### 读写锁

在处理并发读或者写时，可以实现一个有两种类型的锁组成的锁系统来解决问题。

这两种类型的锁通常被称为共享锁(shared lock)和排他锁(exclusive lock)，也叫读锁(read lock)和写锁(write lock)。

读锁是共享的，或者说相互是不阻塞的。多个客户在同一时刻可以同时读取同一个资源，而互不干扰。写锁则是排他的，也就是说一个写锁会阻塞其他的写锁和读锁。

这是出于安全的考虑，只有这样才能确保在给定的时间里，只有一个用户能执行写入，并防止其他用户读取正在写入的同一资源。

#### 表锁

表锁是MySQL中最基本的锁策略，并且是开销最小的的策略，它会锁定整张表。存储引擎会管理自己的锁，MySQL本身也会使用各种有效的表锁来实现不同的目的。

#### 行级锁

行级锁可以最大程度地支持并发处理(同时也带来了最大的锁开销)，行级锁只在存储引擎层实现，而MySQL服务器层没有实现。

## 第五章 索引

~~~sql
Select first_name from actor where user_id = 1
~~~

如果在user_id列上建有索引，则MySQL将使用该索引找到user_id为1的行，也就是说，MySQL先在索引上按值进行查找，然后返回所有包含该值的数据行。

![image-20211209200527177](https://s2.loli.net/2021/12/09/TYyRJoWvwrbI2Ne.png)

索引(在MySQL中也叫作 "键（Key）" )是<font color=orange>存储引擎用于快速找到记录的一种数据结构</font>，这是索引的基本功能。

索引可以包含一个或者多个列的值，如果包含多个列，那么列的顺序十分重要。

### 5.1 B-Tree索引

使用B-Tree数据结构来存储数据，实际上很多存储引擎使用的是B+Tree。

存储引擎以不同的方式使用B-Tree的索引，如MyIsam索引通过数据的物理地址引用被索引的行

<img src="https://image.z.itpub.net/zitpub.net/JPG/2021-11-26/41EFDB33BF7C687D7F83D65A3FC1C20D.jpg" alt="img" style="zoom: 50%;float:left" />

而InnoDB则根据主键引用被索引的行。            

<img src="https://img-blog.csdnimg.cn/20200516191817713.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2JfeF9w,size_16,color_FFFFFF,t_70" alt="img" style="zoom: 40%;float:left" />

叶子节点的数据区保存的就是真实的数据，在通过索引进行检索的时候，命中叶子节点，就可以直接从叶子节点中取出行数据。

在InnoDB中，还使用辅助索引来帮助检索，主键索引的叶子节点保存的是真正的数据，辅助索引叶子节点的数据区保存的是主键索引关键字的值。（在聚簇索引中有提到）

<img src="https://s2.loli.net/2021/12/08/acBGdirjbMzqwFP.png" alt="img" style="zoom: 40%;float:left" />

~~~sql
CREATE TABLE People (
	last_name varchar(50) not null,
    first_name varchar(50) not null,
    dob date not null,
    gender enum('m','f') not null,
    key(last_name, first_name,dob)
);
~~~

B-Tree索引适用于全键值、键值范围和键前缀查找，其中键前缀查找只适用于<font color=orange>根据最左前缀的查找</font>。有如下几种查询方式

*前缀匹配*：和索引中的所有列进行匹配

*匹配最左前缀*：和索引的左边连续一个或几个列进行匹配

*匹配列前缀*：只匹配索引列的值的开头部分

*匹配范围值*：范围列可以用到索引（必须是最左前缀），例如last_name 在 Allen 和 Barrymore之间

*精确匹配某一列并范围匹配另一列*：即第一列first_name全匹配，第二列first_name范围匹配

*只访问索引的匹配*：即查询只需要访问索引，无需访问数据行

下面是B-Tree索引的限制，可以看出上述的查询方式都符合必须符合这些限制

- 如果不是按照索引的最左列开始查找，则无法使用索引。上述表中如果查找first_name=“xxx”则无法使用索引
- 不能跳过索引中的列，使用 last_name = "xxx" 和 dob = "1991-12-13" 这两个条件查询时，如果不指定first_name，则mysql只能使用索引的第一列
- 如果查询中有某个列的范围查询，则其右边所有列都无法使用索引优化查找

这些限制都和索引列的顺序有关，在优化性能时，可能需要使用相同的列但顺序不同的索引来满足不同类型的查询需求。

### 5.2 聚簇索引

聚簇索引并不是一种单独的索引类型，而是一种数据存储格式。<font color=orange>InnoDB的聚簇索引实际上在同一个结构中保存了B-Tree索引和数据行。</font>当表有聚簇索引时，它的数据行实际上存放在索引的叶子页中。

<img src="https://s2.loli.net/2021/12/09/FvD2Pc4gmQxh3KB.png" alt="image-20211209163817738" style="float:left" />

InnoDB将通过主键聚集数据（主键索引就是聚簇索引，非主键索引就是二级索引），如果没有定义主键，InnoDB会选择一个唯一的非空索引代替，如果没有这样的索引，InnoDB会隐式定义一个主键作为聚簇索引。

聚簇索引将索引和数据保存在同一个B-Tree中，因此从聚簇索引中获取数据通常比在非聚簇索引中查找要快。

因为<font color=orange>在二级索引的叶子结点中包含了引用行的主键列</font>，因此二级索引访问需要两次索引查找，第一次是通过二级索引查找行的主键值，第二次是根据这个值去聚簇索引中查找对应的行。

**InnoDB和MyISAM的数据分布对比**

聚簇索引和非聚簇索引的数据分布有区别，不同的存储引擎也有区别。

~~~sql
CREAT TABLE layout_test (
	col1 int not null,
    col2 int not null,
    PRIMARY KEY(col1)
    KEY(col2)
);
~~~

<img src="https://s2.loli.net/2021/12/09/eMHBtJ2z1dUwoYI.png" alt="image-20211209164201093" style="float:left" />

**MyISAM的数据分布**

MyISAM叶子结点存储的是物理地址，这里以行号表示。

<img src="https://s2.loli.net/2021/12/09/1YmNqzr7oKQV8xs.png" alt="image-20211209164735031" style="zoom:80%;float:left" />

MyISAM表的主键分布

<img src="https://s2.loli.net/2021/12/09/qbdDhg3iT5971PE.png" alt="image-20211209164812199" style="float:left" />

MyISAM表的col2列索引的分布

事实上，MyISAM表中主键索引和其他索引在结构上没什么区别。

**InnoDB的数据分布**

因为InnoDB支持聚簇索引，所以使用非常不同的方式存储同样的数据

<img src="https://s2.loli.net/2021/12/09/8KHrht4DvbU1fZT.png" alt="image-20211209185020213" style="float:left" />

聚簇索引的每一个叶子结点都包含了主键值，事务ID，用于事务和MVCC的回滚指针以及剩余的所有列(这里是col2)

还有一点和MyISAM不同的是，InnoDB的二级索引和聚簇索引很不相同。InnoDB二级索引的叶子结点存储的不是"行指针"，而是主键值。

<img src="https://s2.loli.net/2021/12/09/wONpX4jco9vtPeJ.png" alt="image-20211209185349827" style="float:left" />

这样的策略减少了当出现行移动或者数据页分裂时二级索引的维护工作，InnoDB在行移动时无需更新二级索引中的这个"指针"。

<img src="C:\Users\PSJ\AppData\Roaming\Typora\typora-user-images\image-20211209190104396.png" alt="image-20211209190104396" style="zoom:80%;float:left" />

**在InnoDB表中插入行**

使用AUTO_INCREMENT自增列，可以保证数据行是按顺序写入，对于根据主键做关联操作的性能也会更好。

| 表名          | 行数    | 时间(秒) | 索引大小（MB） |
| ------------- | ------- | -------- | -------------- |
| userinfo      | 1000000 | 137      | 342            |
| userinfo_uuid | 1000000 | 180      | 544            |
| userinfo      | 3000000 | 1233     | 1036           |
| userinfo_uuid | 3000000 | 4525     | 1707           |

<font color=orange>注意到向UUID主键插入行不仅花费的时间更长，而且索引占用的空间也更大。这一方面是由于主键字段更长；另一方面毫无疑问是由于页分裂和碎片导致的。</font>

最好避免随机的（不连续且值的分布范围非常大）聚簇索引，特别是对于I/O密集型的应用。例如，从性能的角度来考虑，使用UUID来作为聚簇索引会非常糟糕，它使得主键索引的插入变得完全随机，使数据没有任何聚集特性。

<img src="https://s2.loli.net/2021/12/09/JKYwz5sgcduX2N8.png" alt="image-20211209190655751" style="float: left; " />

向聚簇索引插入顺序的索引值，因为主键的值是顺序的，所以InnoDB把每一条记录都存储在上一条记录的后面，当达到页的最大填充因子时，下一条记录会被写入新的页中。一旦数据按这种顺序的方式加载，主键页就会近似于被顺序的记录填满，这正是期望的结果。

<img src="https://s2.loli.net/2021/12/09/RADgZwydHGX5uIs.png" alt="image-20211209190745670" style="float:left" />

使用UUID聚簇索引的表插入数据，因为新行的主键值不一定之前插入的大，所以InnoDB无法简单地总是把新行插入到索引的最后，而是需要为新的行寻找合适的位置，通常是已有数据的中间位置，并且分配空间。这会增加很多的额外工作，并导致数据分布不够优化。

<font  color=orange>使用InnoDB时应该尽可能地按主键顺序插入数据，并且尽可能地使用单调增加的聚簇索引的值来插入新行</font>

### 5.3 覆盖索引

如果一个索引包含所有需要查询的字段的值，我们就称之为"覆盖索引"。

覆盖索引是非常有用的工具，能够极大地提高性能。

- 索引条目通常远小于数据行的大小。如果只需读取索引，那MySQL就会极大地减少数据访问量。
- 由于InnoDB的聚簇索引，覆盖索引对于InnoDB表特别有用。InnoDB的二级索引在叶子节点保存了主键值，所以如果主键索引能够覆盖查询，就可以避免对主键索引的二次查询。

MySQL查询优化器会在执行查询前判断是否有一个索引能进行覆盖。

~~~sql
# 添加索引
ALTER TABLE list_song ADD INDEX index_all (song_id, song_list_id)
# 查看查询语句是否使用了覆盖索引
EXPLAIN SELECT song_id, song_list_id FROM list_song WHERE song_id = 1
~~~

<img src="https://s2.loli.net/2021/12/09/FcE5kZqhCKAlNdo.png" alt="image-20211209201944067" style="float:left" />

