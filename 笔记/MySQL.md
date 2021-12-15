

# 高性能MySQL

~~~java
//MySQL最重要、最与众不同的特性是它的存储引擎架构，这种架构的设计将查询处理(Query Processing)及其他系统任务(Server Task)和数据的存储/提取相分离
~~~

![img](https://s2.loli.net/2021/12/07/gUvNeoPd7zCFnql.png)

## 第一章 架构

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

### 事务

事务的四大特性：

原子性（Atomicity）：整个事务中的操作要么全部成功、要么全部失败。

一致性（Consistency）：数据库总是从一个一致性状态转换到另一个一致性状态。

隔离性（Isolation）：通常来说，一个事务所做的修改在事务提交前，对其他事务是不可见的。若隔离级别不同，前面所述会发生变化。

持久性（Durability）：一旦事务提交，则其所做的修改就会永久保存到数据库中。

隔离的四种级别：

- 未提交读(Read Uncommit)：事务中的修改，即使未提交，对其他事务也是可见的。
- 已提交读(Read Commit)：事务直到提交之前，所作的任何修改对其他事务都是不可见的。但同一事务多次做同样的查询，可能得到不同的结果。
- 可重复读(Repeatable Read)：同一事务多次读取同样的查询结果是一致的。<font color=pink>MySQL的默认事务隔离级别</font>
- 串行化(Serializable)：强制事务串行执行。会在读取的每一行数据上都加锁

~~~markdown
    脏读：事务可以提取其他事务未提交的数据
    不可重复读：同一事务多次做同样的查询，可能得到不同的结果
    幻读：事务在读取某个范围内的记录时，另外一个事务又在该范围内插入了新记录，当之前的事务再次读取该范围的记录时，会产生幻行
    
    解决幻读有两种办法：
    1.串行化,最高隔离级别，会导致大量的争锁、加锁、解锁
    2.MVCC(多版本并发控制)
~~~

| 隔离级别 | 脏读可能性 | 不可重复读可能性 | 幻读可能性 | 加锁读可能性 |
| :------: | :--------: | :--------------: | :--------: | :----------: |
| 未提交读 |    Yes     |       Yes        |    Yes     |      No      |
| 已提交读 |     No     |       Yes        |    Yes     |      No      |
| 可重复读 |     No     |        No        |    Yes     |      No      |
|  串行化  |     No     |        No        |     No     |     Yes      |

### 多版本并发控制（MVCC）

#### 总体思路

MySQL的大多数事务型存储引擎实现的都不是简单的行级锁。基于性能的考虑，一般都同时实现了多版本并发控制(MVCC)。

MVCC：通过保存数据在某个时间点的快照来实现的，也就是说，不管需要执行多长时间，每个事务看到的数据都是一致的。

InnoDB的MVCC是通过在每行记录后面保存两个隐藏的列来实现的。一个保存行的创建时间，一个保存行的过期时间。<font color=pink>当然存储的不是实际的时间值，而是系统版本号(System Version Number)。</font>

每开始一个事务，系统版本号就会递增。事务开始时刻的系统版本号会作为事务的版本号，用来和查询到的每行记录的版本号进行比较。

在`可重复读`的事务隔离级别机制下，MVCC具体的操作如下：

**SELECT**

- InnoDB只会查找早于当前事务版本的数据行，这样确保读取的行，要么是在事务开始前已经存在，要么是事务自身插入或者修改过的。
- 行的删除版本要么未定义，要么大于当前的事务版本

**INSERT**

- InnoDB为新插入的每一行保存当前系统版本号作为行版本号

**DELETE**

- InnoDB为删除的每一行保存当前系统版本号作为行删除标识

**UPDATE**

- InnoDB为插入新一行数据，保存当前系统版本号为行版本号，同时保存当前系统版本号到原来的行作为行删除标识

保存了这两个版本号，可以使大多数读操作都可以不用加锁。不足之处在于每一行都需要额外的存储空间，需要做更多的行检测工作，以及额外的维护工作。

#### 具体实现

MVCC的实现原理主要是Undo日志 ，版本链，Read View 来实现的

- Undo日志： 主要用于记录数据被修改之前的日志，在表信息修改之前先会把数据拷贝到undo log里。当事务进行回滚时可以通过undo log 里的日志进行数据还原。

- 版本链：undo日志中通过回滚指针连接形成的链表

- Read View：主要是用来做可见性判断的, 即当我们某个事务执行快照读的时候，对该记录创建一个Read View读视图，把它比作条件用来判断当前事务能够看到哪个版本的数据，既可能是当前最新的数据，也有可能是该行记录的undo log里面的某个版本的数据。

在真实的数据每行数据中，有三个隐藏的列：

- db_trx_id：记录创建这条记录/最后一次修改该记录的数据事务ID
- db_roll_pointer：回滚指针，指向这条记录的上一个版本
- db_row_id：隐含的自增ID（隐藏主键），如果数据表没有主键，InnoDB会自动以db_row_id产生一个聚簇索引
- 实际还有一个删除flag隐藏字段, 记录被更新或删除并不代表真的删除，而是删除flag变了

<img src="https://s2.loli.net/2021/12/14/G1PdB5wRKfF87Q3.png" alt="image-20211214174621604" style="float:left" />

每次对数据库记录进行改动，都会记录一条undo日志，每条undo日志也都有一个roll_pointer属性（INSERT操作对应的undo日志没有该属性，因为该记录并没有更早的版本），可以将这些undo日志都连起来，串成一个链表，所以现在的情况就像下图一样：

<img src="https://img.php.cn/upload/article/000/000/052/7d239fc720ed0f548d4d994272398847-3.png" alt="7d239fc720ed0f548d4d994272398847-3.png" style="float:left;zoom:80%;" />

## 第五章 索引

~~~sql
Select first_name from actor where user_id = 1
~~~

如果在user_id列上建有索引，则MySQL将使用该索引找到user_id为1的行，也就是说，MySQL先在索引上按值进行查找，然后返回所有包含该值的数据行。

<img src="https://s2.loli.net/2021/12/09/yGhjCHBQk6eENrf.png" alt="image-20211209203137938" style="float:left" />

索引(在MySQL中也叫作 "键（Key）" )是<font color=orange>存储引擎用于快速找到记录的一种数据结构</font>，这是索引的基本功能。

索引可以包含一个或者多个列的值，如果包含多个列，那么列的顺序十分重要。

### 5.1 B-Tree索引

使用B-Tree数据结构来存储数据，实际上很多存储引擎使用的是B+Tree。

存储引擎以不同的方式使用B-Tree的索引，如MyIsam索引通过数据的物理地址引用被索引的行

<img src="https://image.z.itpub.net/zitpub.net/JPG/2021-11-26/41EFDB33BF7C687D7F83D65A3FC1C20D.jpg" alt="img" style="zoom: 50%;float:left" />

而InnoDB则根据主键引用被索引的行。            

<img src="https://img-blog.csdnimg.cn/20200516191817713.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2JfeF9w,size_16,color_FFFFFF,t_70" alt="img" style="zoom: 40%;float:left" />

叶子节点的数据区保存的就是真实的数据，在通过索引进行检索的时候，命中叶子节点，就可以直接从叶子节点中取出行数据。

在InnoDB中，还使用辅助索引 (二级索引) 来帮助检索，主键索引的叶子节点保存的是真正的数据，辅助索引叶子节点的数据区保存的是主键索引关键字的值。

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

#### 为什么是B-Tree索引

**磁盘读取**

由于存储介质的特性，磁盘本身存取就比主存慢很多，再加上机械运动耗费，磁盘的存取速度往往是主存的几百分分之一，因此为了提高效率，要尽量减少磁盘I/O。

为了达到这个目的，磁盘往往不是严格按需读取，而是每次都会预读，即使只需要一个字节，磁盘也会从这个位置开始，顺序向后读取一定长度的数据放入内存。这样做的理论依据是计算机科学中著名的局部性原理：<font color=orange>当一个数据被用到时，其附近的数据也通常会马上被使用。</font>

由于磁盘顺序读取的效率很高（不需要寻道时间，只需很少的旋转时间），因此对于具有局部性的程序来说，预读可以提高I/O效率。

预读的长度一般为页（page）的整数倍。页是计算机管理存储器的逻辑块，硬件及操作系统往往将主存和磁盘存储区分割为连续的大小相等的块，每个存储块称为一页（在许多操作系统中，页的大小通常为4k），主存和磁盘以页为单位交换数据。当程序要读取的数据不在主存中时，会触发一个缺页异常，此时系统会向磁盘发出读盘信号，磁盘会找到数据的起始位置并向后连续读取一页或几页载入内存中，然后异常返回，程序继续运行。

**二叉树**

二叉树通过二分查找可以实现快速查询，时间复杂度是O(logn)。但如果是下图这样的二叉树，要查找值为 4 的节点，则需要遍历整颗二叉树。因为这个特性，所以二叉树不适合作为索引的数据结构。

![image-20211210143703831](https://s2.loli.net/2021/12/10/whdnVyTzmOP2fZQ.png)

**二叉平衡搜索树**

二叉平衡搜索树解决了单调链表的问题，查询效率也是O(logn)，但仍然不是索引的数据结构。

- 二叉平衡搜索树的搜索效率不高，根据树的结构，数据所处的深度决定了搜索时的IO次数。如上图中搜索id = 8的数据，需要进行3次IO。当数据量到达几百万的时候，树的高度就会很恐怖。
- 存储的数据内容太少。没有很好利用操作系统和磁盘数据交换特性，也没有利用好磁盘IO的预读能力。因为操作系统和磁盘之间一次数据交换是以页为单位的，一页大小为 4K，即每次IO操作系统会将4K数据加载进内存。<font color=orange>但是，在二叉树每个节点的结构只保存一个关键字，一个数据区，两个子节点的引用，并不能够填满4K的内容。幸幸苦苦做了一次的IO操作，却只加载了一个关键字。</font>在树的高度很高，恰好搜索的关键字又位于叶子节点或者支节点的时候，取一个关键字要做很多次的IO。

<img src="https://s2.loli.net/2021/12/10/ts3MmRTAxdWJ8Y6.png" alt="image-20211210153940160" style="zoom:80%;float:left" />

**多路平衡搜索树(B-Tree)**

B-Tree 能够很好的利用操作系统和磁盘的交互特性， MySQL为了很好的利用磁盘的预读能力，<font color=orange>将页大小设置为16K，即将一个节点（磁盘块）的大小设置为16K，一次IO将一个节点（16K）内容加载进内存。</font>这里，假设关键字类型为 int，即4字节，若每个关键字对应的数据区也为4字节，不考虑子节点引用的情况下，则上图中的每个节点大约能够存储（16 * 1024）/ 8 = 2000个关键字，共2001个路数。对于二叉树，三层高度，最多可以保存7个关键字，而对于这种有2001路的B树，三层高度能够搜索的关键字个数远远的大于二叉树。

在B-Tree保证树的平衡的过程中，每次索引字段的变化，都会导致树结构发生很大的变化，这个过程是特别浪费时间的，所以创建索引一定要创建合适的索引，而不是把所有的字段都创建索引，创建冗余索引只会在对数据进行新增，删除，修改时增加性能消耗。

![image-20211210152806481](https://s2.loli.net/2021/12/10/lFsy3qWnNegHfkI.png)

#### B-Tree和B+Tree的区别

<font color=pink>大多数存储引擎都是使用B+Tree实现索引。</font>

- B-Tree根节点、支节点和叶子节点都保存数据，B+Tree只有叶子节点保存数据。在B-Tree树中，如果节点命中会直接返回数据，在B+Tree中会继续查找，直到找到关键字对应的叶子节点。
- B+Tree的扫表能力更强。如果我们根据索引去进行数据表的扫描，B-Tree需要把整棵树扫描一遍，而B+Tree则只需要遍历它的所有叶子节点即可。
- B+Tree采用的是左闭区间，之所以这样设计是因为mysql希望它能最好的去支持自增id。根节点和支节点没有数据区，关键字对应的数据只保存在叶子节点中，<font color=orange>即只有叶子节点才保存数据。</font>
- B+Tree中叶子节点是顺序排序的，并且相邻的节点具有顺序引用的关系。<font color=orange>B+Tree天然具有排序功能。</font>

<img src="https://s2.loli.net/2021/12/10/Ik6WmLhpvAF3eoN.png" alt="img" style="zoom:118%;float:left" />

<img src="https://s2.loli.net/2021/12/10/284VbI9MLTcBSdv.png" alt="img" style="float:left" />

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
- 由于InnoDB的聚簇索引，覆盖索引对于InnoDB表特别有用。InnoDB的二级索引在叶子节点保存了主键值，所以如果覆盖索引能够覆盖查询，就可以避免对主键索引的二次查询。

MySQL查询优化器会在执行查询前判断是否有一个索引能进行覆盖。

~~~sql
# 添加索引
ALTER TABLE list_song ADD INDEX index_all (song_id, song_list_id)
# 查看查询语句是否使用了覆盖索引
EXPLAIN SELECT song_id, song_list_id FROM list_song WHERE song_id = 1
~~~

<img src="https://s2.loli.net/2021/12/09/FcE5kZqhCKAlNdo.png" alt="image-20211209201944067" style="float:left" />

**小案例**

![img](https://s2.loli.net/2021/12/10/Rdywzjpk5J3FuI8.png)

~~~sql
select id from t where name = ?
~~~

通过二级索引name，我们就可以知道id的值，不需要再去扫描id索引了。

知道了覆盖索引，<font color=orange>就知道了为什么sql中要求尽量不要使用select *，要写明具体要查询的字段。其中一个原因就是在使用到覆盖索引的情况下，不需要进入到数据区，数据就能直接返回，提升了查询效率。</font>在用不到覆盖索引的情况下，也尽可能的不要使用select *，如果行数据量特别多的情况下，可以减少数据的网络传输量。当然，这都视具体情况而定，通过select返回所有的字段，通用性会更强，一切有利必有弊。

## 第六章 查询性能优化

如何查询写的很糟糕，即使库表结构再合理，索引再合适，也无法实现高性能。

查询的生命周期大致可以按照顺序来看：从客户端，到服务器，然后在服务器上解析，生成执行计划，执行，并返回结果给客户端。其中，"执行"可以是认为整个生命周期中最重要的阶段，其中包括了大量存储引擎的API调用以及调用后的数据处理，包括排序、分组。

<img src="https://s2.loli.net/2021/12/11/YUCw64dmbGE8JBr.png" alt="img" style="float:left" />

### 6.1 优化数据访问

查询性能低下最基本的原因是访问的数据太多，可以通过减少访问的数据量的方式进行优化。

- 确认应用程序是否在检索大量不必要的数据，可能是访问了太多的行，也可能是访问了太多的列
- 确认 MySQL 服务器是否在分析大量不必要的数据行。

#### 6.1.1 减少不必要数据的请求

通常会有下面四种情况导致不必要数据的请求发生。

*1 . 查询不需要的数据*。

例如想查询100条数据，但只显示前10条数据。若不加限制，MySQL会返回全部的结果集，客户端会接收全部的数据，然后抛弃其中大部分数据。最简单有效的方法是在这样的查询后面加上 LIMIT。

*2 . 多表关联时返回全部列*。

如果需要用多表来进行关联查询，例如想查询在电影"古董局中局"中出现的演员，千万不要使用下面的写法。

~~~sql
select * from actor
inner join film_actor
inner join film
where film.title = '古董局中局';
~~~

这会返回三个表的全部数据列，正确的方式是像下面这样只取需要的行

~~~sql
select actor.* from actor ...;
~~~

*3 . 总是取出所有列*

一些DBA是严格禁止使用<font color = pink> select * </font>的写法的。这种写法会让优化器无法完成覆盖索引的优化，还会给服务器带来额外的I/O、内存和CPU的消耗。不过这种方式虽然会浪费数据库的资源，但却能简化服务层的开发，提高代码的复用性。在部分业务场景下，获取并缓存所有列的查询，会比多个独立的只获取部分列的查询更有好处。

*4 . 重复查询相同的数据*

比较好的解决方案是在首次查询时将这个数据缓存起来，需要的时候从缓存中取出，这样性能显然会更好。与第三点类似。

#### 6.1.2 MySQL扫描了额外的记录

在确定查询只返回需要的数据后，接下来看看服务器为了返回结果是否扫描了过多的行。对于MySQL，最简单的衡量查询开销的三个指标如下：

- 响应时间
- 扫描的行数
- 返回的行数

这三个指标可以大致反应MySQL在内部执行查询时需访问多少数据，并可以大致推算出查询运行的时间。

**响应时间**

响应时间 = 服务时间 + 排队时间。 服务时间是指数据库处理这个查询花了多长时间。排队时间是指服务器因为等待某些资源而没有真正执行查询的时间，可能是等待I/O操作完成，也可能是等待行锁。

**扫描的行数和返回的行数**

查看该查询扫描的行数是非常有帮助的，在一定程度上能够说明该查询的效率高不高。

理想情况下扫描的行数和返回的行数应该是相同的。但实际上这种情况不多，例如在做一个关联查询时，服务器必须要扫描多行才能生成结果集中的一行。

扫描的行数对返回的行数的比率通常很小，一般在1:1和10:1之间，不过有时候也可能会很大。

**扫描的行数和访问类型**

MySQL有好几种访问类型可以查找并返回一行结果。有些访问类型需要扫描许多行才能返回一行结果，也有些方式可能无需扫描就能返回结果。

在EXPLAIN语句中的 type 类型反应了访问类型，从全表扫描、索引扫描、范围扫描、唯一索引扫描、引用扫描、常数扫描等。速度从慢到快，扫描行数从多到少。

如果查询没有办法找到合适的访问类型，那么解决的最好办法通常是增加一个索引。<font color=orange>索引让MySQL以最高效，扫描行数最少的方式找到需要的数据。</font>

~~~sql
# 使用主键索引查询
EXPLAIN SELECT * FROM song_list WHERE id = 1;
# 普通查询
EXPLAIN SELECT * FROM song_list WHERE title = '年轻之歌 有关爱与挑衅';
~~~

<img src="https://s2.loli.net/2021/12/11/RCuZBLdG1rnAMVK.png" alt="image-20211211162143886" style="float:left" />

使用主键索引查询，访问类型是常数扫描，MySQL预估只需扫描1条记录就可以完成这个查询。

<img src="https://s2.loli.net/2021/12/11/ojpSDrLZ1YztvHl.png" alt="image-20211211162219508" style="float:left" />

如果不使用合适的索引进行查询，访问类型就变成了全表扫描（ALL），MySQL预估需要扫描 84 条记录来完成这个查询。"Using Where"表示MySQL通过 where条件来筛选存储引擎返回的数据。

一般MySQL使用如下三种方式应用 where 条件

- 在索引中使用where条件过滤不必要的条件，在存储引擎层完成。
- 使用索引覆盖，直接从索引中过滤不必要的数据并返回命中的结果。
- 从数据表中返回结果，过滤不满足条件的记录（在Extra 列中出现 Using Where），在MySQL服务器层完成，需要先从数据表读取数据然后过滤。

如果查询扫描大量的数据却只返回少量的行，可以尝试下面的技巧去优化。

- 使用覆盖索引，把所有需要的列都放到索引上。这样存储引擎无须回表获取对应行就可以返回结果。
- 改变库表结构。
- <font color = orange>重写这个复杂的查询，让MySQL优化器能够以更优化的方式这个查询。(本章后续的重点)</font>

### 6.2 重构查询的方式

有时候可以将查询转换一种写法，在查询结果不发生改变的情况下，性能能更加出色。

#### 6.2.1 复杂查询或多个简单查询

设计查询时需要考虑的重要问题是：能否将一个复杂查询分成多个简单查询。

如果分成多个简单的查询，就需要多次执行建立客户端与服务器的连接、查询解析、优化等工作。

对于MySQL来说，运行多个小查询不是问题，但服务器响应数据给客户端就慢的多了。在其他条件都相同的情况下，使用尽可能少的查询是更好的。

但有时候，将一个大查询分解为多个小查询也是很有必要的。当然在一个查询能够胜任时还写成多个独立的查询是不明智的。

#### 6.2.2 切分查询

有时候对一个大查询使用"分治"的思想，将大查询切分为多个小查询，每个查询的功能完全一样，只完成一个小部分，每次只返回一小部分查询结果。

删除旧的数据就是一个很好的例子。清除大量数据时，如果用一个大语句一次性完成的话，则可能需要一次锁住很多数据，占满整个事务日志，耗尽系统资源，阻塞很多小的但重要的查询。

将一个大的 DELETE 语句切分成较小的查询，尽可能小的影响MySQL性能。

~~~sql
# 一次性删除
Delete from xxx
# 切分删除
rows_affected = 0
do{
	rows_affected = do_query(
    	"Delete from xxx limit 1000"
    )
}while rows_affected > 0
~~~

如果每次删除数据后都暂停一会再做下一次删除，这样可以将服务器原本一次性的压力分散到一个很长的时间段中，可以降低对服务器的影响，还可以减少删除时锁的持有时间。

#### 6.2.3 分解关联查询

很多高性能的应用都会对关联查询进行分解，<font color=pink>阿里巴巴开发手册中规定，查询语句不允许超过四个表的关联。</font>可以将每一个表进行一次单表查询，然后将结果在应用程序中进行关联。

~~~sql
select * from tag
  join tag_post on tage_post.tag_id = tag.id
  join post on tag_post.post_id = post.id
  where tag.tag = 'mysql';
~~~

可以分解成下面这些查询完成

~~~sql
select * from tag where tag = 'mysql';
select * from tag_post where tag_id = 1234;
select * from post where id in (123, 456, 9098, 8904);
~~~

用分解关联查询的方式重构查询有如下的优势：

- 让缓存的效率更高，可以方便的缓存单表查询对应的结果对象。
- 执行单个查询可以减少锁的竞争
- 在应用层做关联，可以更容易对数据库进行拆分，做到高性能与可扩展。（Java应用中的VO层，外键对应一个实体）
- 减少冗余数据的查询。在应用层做关联，意味着对于某条记录应用只需要查询一次，而在数据库中做关联查询，则可能需要重复地访问一部分数据

### 6.3 查询的基础

很多查询优化工作实际上就是遵循一些原则让优化器能够按照预想的合理方式运行。

<img src="https://s2.loli.net/2021/12/11/YUCw64dmbGE8JBr.png" alt="img" style="float:left" />

大体上分为5个步骤：

1. 客户端发送一条查询给服务器
2. 服务器检查查询缓存，如果命中就立刻返回缓存里的结果。否则进入下一阶段
3. 服务器进行SQL解析，预处理，再有优化器生成对应的执行计划
4. MySQL根据优化器生成的执行计划，调用存储引擎的API来执行查询
5. 将结果返回给客户端

#### 6.3.1 客户端 / 服务器通信协议

通信协议是"半双工"的，意味着在任何一个时刻，要么是由服务器给客户端发送数据，要么是客户端向服务器端发送数据，这两个动作不能同时发生。

多数连接MySQL的库函数可以获得全部结果集并缓存在内存里。<font color=orange>MySQL通常需要等所有的数据都发送给客户端才能释放这条查询所占用的资源，所以接收全部结果并缓存通常可以减少服务器的压力，让查询能够早点结束，早点释放相应的资源。</font>

多数连接MySQL的库函数从MySQL获取数据时，<font color=orange>看起来像是从MySQL服务器获取数据，实际上是从这个库函数的缓存获取数据。</font>例如：MyBatis的一级缓存

**查询连接状态**

~~~sql
SHOW FULL PROCESSLIST
~~~

使用上述指令可以查看MySQL连接的状态。

<img src="https://s2.loli.net/2021/12/12/9wdOLEfpQrlIqni.png" alt="image-20211212085647643" style="float:left" />

#### 6.3.2 查询缓存

在解析一个SQL语句前，如果查询缓存是打开的，那么MySQL会优先检查这个查询是否命中查询缓存中的数据。

如果当前的查询恰好命中了查询缓存，那么MySQL会检查一次用户权限，如果没问题MySQL会跳过所有阶段，<font color=orange>从缓存中拿到当前查询需要访问的表数据并返回给客户端。</font>这种情况下，查询不会被解析，不用生成执行计划，不会被执行。

#### 6.3.3 查询优化处理

查询的生命周期的下一步是将一个SQL转换成一个执行计划，MySQL再依照这个执行计划和存储引擎进行交互。这包括多个子阶段：解析SQL、预处理、优化  SQL执行计划。

**语法解析器和预处理**

MySQL解析器将使用 MySQL 语法规则验证和解析查询，例如，它将验证是否使用了错误的关键字。通过关键字对SQL语句进行解析，生成一颗 "解析树"。

预处理器则根据 MySQL 规则进一步检查解析树是否合法，例如检查数据表和数据列是否存在，还会解析名字和别名，看看是否有歧义。

**查询优化器**

语法树被认为是合法后，由优化器将其转换为执行计划。<font color=orange>一条查询可以有很多种执行计划，最后都返回相同的结果。优化器的作用是找到其中最好的执行计划。</font>

MySQL是基于成本的优化器，它将尝试预测一个查询使用某种执行计划时的成本，并选择其中成本最小的一个。成本为需要读取的数据页，读取的数据页越少越好，不过读取的记录数通常能够很好的反应一个查询的成本。

~~~sql
# 查询语句
SELECT COUNT(*) FROM actor
# 查询成本
SHOW STATUS LIKE 'Last_query_cost'
~~~

<img src="https://s2.loli.net/2021/12/12/Dy5guAMpEIki2wN.png" alt="image-20211212154529467" style="zoom:140%;float:left" />

查询优化器使用了很多优化策略来生成一个最优的执行计划。策略可以简单的分为两种：一种是静态优化，另一种是动态优化。

静态优化直接对解析树进行分析，并完成优化，例如将 WHERE 条件转换成另一种等价形式。静态优化不依赖于特别的数值，在第一次完成后就有效，可以认为是"编译时优化"。

动态优化则和查询的上下文有关，例如 WHERE 条件中的取值、索引对应的数据行数。这需要在每次查询的时候都重新评估，可以认为这是"运行时优化"。

<font color=orange>MySQL对查询的静态优化只做一次，但对查询的动态优化则需要在每次执行时都重新评估。</font>

MySQL能够处理的优化类型(P210)

- <font color=pink>重新定义关联表的顺序</font>
- 将外连接转为内连接
- 优化 Count()、MIN()、MAX()，在B+Tree索引中索引中，执行MIN()时优化器只需取读取叶子节点中的第一条记录。
- 预估并转换为常数表达式
- 覆盖索引扫描
- ...

~~~sql
EXPLAIN SELECT MAX(id) FROM song_list
~~~

<img src="https://s2.loli.net/2021/12/12/qmAZ69JNLyH8Rpr.png" alt="image-20211212144132567" style="float:left" />

**MySQL如何执行关联查询**

使用回溯的思想，先查找第一个表记录，再嵌套查找下一个关联表，如果最后一个关联表无法找到更多的行以后，回到上一层关联表继续查找。

查看下面的内连接：

~~~sql
SELECT t1.col1, t2.col2
FROM tb1 t1
INNER JOIN tb2 t2 
ON t1.col3 = t2.col3
WHERE t1.column1 IN (5, 6)
~~~

下面的伪代码来演示MySQL如何完成这条关联查询

~~~sql
outer_iter = iterator over t1 while col1 in (5, 6) 
outer_row = outer_iter.next
while outer_row 
    inner_iter = iterator over t2 where col3 = outer_row.col3
    inner_row = inner_iter.next
    while inner_row 
        output [ outer_row.col1, inner_row.col2 ] 
        inner_row = inner_iter.next
    end 
    outer_row = outer_iter.next
end
~~~

<img src="https://s2.loli.net/2021/12/12/7JmkpINaRfYAXLS.png" alt="image-20211212120610884" style="zoom:150%;float:left" />

对于外连接上面的过程同样适用

~~~sql
outer_iter = iterator over t1 while col1 in (5, 6) 
outer_row = outer_iter.next
while outer_row 
    inner_iter = iterator over t2 where col3 = outer_row.col3
    inner_row = inner_iter.next
    if inner_row 
    	while inner_row 
        	output [ outer_row.col1, inner_row.col2 ] 
        	inner_row = inner_iter.next
    	end
    else
    	output[ outer_row.col1, null]
    end
    outer_row = outer_iter.next
end
~~~

**执行计划**

MySQL不会生成查询字节码来执行查询。MySQL生成查询的一棵指令树，然后通过存储引擎执行完成这棵指令树并返回结果。

MySQL总是从一个表开始一直嵌套循环、回溯完成所有表的关联。

<img src="https://s2.loli.net/2021/12/12/e13YU9yqn8XRJEC.png" alt="image-20211212143600590" style="zoom:120%;float:left" />

**关联查询优化器**

<font color=orange>MySQL优化器最重要的一部分就是关联查询优化，他决定了多个表关联时的顺序。</font>

通常多表关联时，可以有多种不同的关联顺序来获得相同的执行结构。

关联查询优化通过评估不同顺序时的成本来选择一个代价最小的关联顺序。

~~~sql
EXPLAIN SELECT
  film.`film_id`,
  film.`title`,
  film.`release_year`,
  actor.`actor_id`,
  actor.`first_name`,
  actor.`last_name` 
FROM film 
JOIN film_actor 
ON film.`film_id` = film_actor.`film_id`
JOIN actor
ON film_actor.`actor_id` = actor.`actor_id`
~~~

如果仅按语法的顺序看，MySQL应该先从film表开始用，使用 film_actor 的 film_id查找对应的actor_id值，然后再根据actor表的主键找到对应的记录。

但MySQL实际上是将关联的顺序倒转过来执行的。

![image-20211212152751446](https://s2.loli.net/2021/12/12/Rg5TxrAjb2J6EwX.png)

我们按照原本语法的顺序执行看看有什么不同。

~~~sql
EXPLAIN SELECT STRAIGHT_JOIN ...
~~~

![image-20211212152716025](https://s2.loli.net/2021/12/12/d4Xufoq1J3ORPiw.png)

<font color=orange>关联顺序倒转后的第一个关联表只需要扫描很少的行数，第二个表和第三个表都是根据索引查询，速度很快。倒转的关联顺序会让查询进行更少的嵌套循环和回溯操作。</font>

关联优化器会尝试在所有的关联顺序中选择一个成本最小的来生成执行计划树。但如果关联表的数量一多，那搜索成本就太高了。

MySQL优化器有很多启发式的优化策略来加速执行计划的生成。

有时各个查询的顺序不能随意安排，这时关联优化器可以根据这些规则大大减少搜索空间，例如左连接、相关子查询。

这是因为后面的表的查询需要依赖于前面表的查询结果。这种表的依赖关系可以帮助优化器大大减少需要扫描的执行计划数量。

#### 6.3.4 查询执行引擎

在解析后优化阶段，MySQL将生成查询对应的执行计划。

查询执行引擎则根据这个执行计划来完成整个查询。

<font color=orange>执行计划是个数据结构，而不是和其他的关系型数据库那样会生成对应的字节码</font>

MySQL简单地根据执行计划给出的指令逐步执行。在根据执行计划查询的过程中，有大量的操作需要通过调用存储引擎实现的接口来完成，这些接口也就是我们称为 "Handler API" 的接口。查询中的每一个表由一个handler的实例表示。

MySQL在优化阶段为每一张表创建了一个handler实例，优化器根据这些实例的接口可以获取表的相关信息，包括表的所有列名、索引统计信息等等。

#### 6.3.5 返回结果给客户端

查询执行的最后一个阶段是将结果返回给客户端。

<font color=orange>即使查询不需要返回结果集给客户端，MySQL仍然会返回这个查询的一些信息，如该查询影响到的行数。</font>

如果查询可以被缓存，那么MySQL在这个阶段也会将结果存放到查询缓存中。

MySQL将结果集返回给客户端是一个增量、逐步返回的过程。当开始生成第一条结果时，MySQL就可以开始向客户端逐步返回结果集了。

这样处理有两个好处：

- 服务器无需储存太多的结果，也就不会消耗太多内存。
- MySQL客户端可以第一时间获得返回的结果。

结果集中的每一行都会以一个满足MySQL客户端 / 服务器通信协议的封包发送，再通过TCP协议进行传输。<font color=orange>TCP协议可能会对MySQL的封包进行缓存并进行批量处理传输。</font>

# MySQL50题

~~~markdown
 Union规则：1.UNION 操作符用于连接两个以上的 SELECT 语句的结果组合到一个结果集合中。多个 SELECT 语句会删除重复的数据。
           2.Union不允许连接的多表都使用 `Order By`，但允许部分表使用。
           3.两张表垂直的拼接
 题目：19, 22, 25
 Join规则：1.两张表横向的拼接
          2.必须要有on语句作为拼接条件
 对于某种查询要求,子查询和连接查询都可以实现
~~~

1、查询"01"课程比"02"课程成绩高的学生的信息及课程分数

~~~sql
SELECT student.*, sc1.s_score AS 01课程, sc2.s_score AS 02课程
FROM student
JOIN (SELECT score.`s_score`, score.`s_id` FROM score WHERE c_id = 1) sc1
ON student.`s_id` = sc1.`s_id`
JOIN (SELECT score.`s_score`, score.`s_id` FROM score WHERE c_id = 2) sc2
ON student.`s_id` = sc2.`s_id`
WHERE sc1.s_score > sc2.s_score
~~~

2、查询"01"课程比"02"课程成绩低的学生的信息及课程分数

~~~sql
SELECT student.*, sc1.s_score AS 课程01, sc2.s_score AS 课程2
FROM student
JOIN (SELECT score.`s_id`, score.`s_score` FROM score WHERE c_id = 1) sc1
ON student.`s_id` = sc1.s_id
JOIN (SELECT score.`s_id`, score.`s_score` FROM score WHERE c_id = 2) sc2
ON student.`s_id` = sc2.s_id
WHERE sc1.s_score < sc2.s_score
~~~

3、查询平均成绩大于等于60分的同学的学生编号、学生姓名和平均成绩

~~~sql
SELECT student.`s_id`, student.`s_name`, ROUND(AVG(score.`s_score`),2) AS 平均成绩
FROM student
JOIN score
ON score.`s_id` = student.`s_id`
GROUP BY score.`s_id`
HAVING 平均成绩 > 60
~~~

4：查询平均成绩小于60分的同学的学生编号、学生姓名和平均成绩（包括有成绩和无成绩的）

~~~sql
SELECT student.`s_id`, student.`s_name`, IFNULL(ROUND(AVG(score.`s_score`),2),0) AS 平均成绩
FROM student
LEFT JOIN score
ON student.`s_id` = score.`s_id`
GROUP BY student.`s_id`
HAVING 平均成绩 < 60
~~~

5、查询所有同学的学生编号、学生姓名、选课总数、所有课程的总成绩

~~~sql
SELECT student.`s_id`, student.`s_name`, COUNT(score.`s_id`) AS 选课总数, IFNULL(SUM(score.`s_score`),0) AS 总分
FROM student
LEFT JOIN score
ON score.`s_id` = student.`s_id`
GROUP BY student.`s_id`
~~~

6、查询所有"李"姓老师的数量

~~~sql
SELECT COUNT(*)
FROM teacher
WHERE t_name LIKE '李%'
~~~

7、询学过"张三"老师授课的同学的信息

~~~sql
SELECT student.*
FROM student
JOIN score
ON student.`s_id` = score.`s_id`
JOIN course
ON course.`c_id` = score.`c_id`
JOIN teacher
ON teacher.`t_id` = course.`t_id`
WHERE teacher.t_name = '张三'
########################################################
SELECT student.*
FROM student
WHERE student.`s_id` IN(
	SELECT score.`s_id`
	FROM score
	WHERE score.`c_id` = (
		SELECT course.`c_id`
		FROM course
		JOIN teacher 
		ON course.`t_id` = teacher.`t_id`
		WHERE teacher.`t_name` = '张三'
	)
)
~~~

8、查询没询学过"张三"老师授课的同学的信息

~~~sql
SELECT student.*
FROM student
WHERE student.`s_id` NOT IN(
	SELECT score.`s_id`
	FROM score
	WHERE score.`c_id` = (
		SELECT course.`c_id`
		FROM course
		JOIN teacher 
		ON course.`t_id` = teacher.`t_id`
		WHERE teacher.`t_name` = '张三'
	)
)
~~~

9、查询学过编号“01”并且也学过编号"02"的课程的同学的信息

~~~sql
SELECT student.* 
FROM 
student
JOIN 
(SELECT s_id FROM score WHERE c_id = 1) s1
ON student.`s_id` = s1.s_id
JOIN
(SELECT s_id FROM score WHERE c_id = 2) s2
ON student.`s_id` = s2.s_id
~~~

10、查询学过编号“01”并且没学过编号"02"的课程的同学的信息

~~~sql
SELECT student.* 
FROM 
student
JOIN 
(SELECT s_id FROM score WHERE c_id = 1) s1
ON student.`s_id` = s1.s_id
WHERE student.`s_id` NOT IN
(SELECT s_id FROM score WHERE c_id = 2)
~~~

11、查询没有学全所有课程的同学的信息

~~~sql
SELECT student.*
FROM student
LEFT JOIN (
	SELECT s_id, COUNT(*) AS 选课数 
	FROM score 
	GROUP BY s_id 
)AS sum_course
ON student.`s_id` = sum_course.s_id
WHERE 选课数 < (SELECT COUNT(*) FROM course) || 选课数 IS NULL
~~~

12、查询至少有一门课与学号为01的同学所学相同的学生信息

~~~sql
SELECT DISTINCT student.*
FROM student
JOIN
score
ON score.`s_id` = student.`s_id`
WHERE c_id IN
(
SELECT c_id FROM score WHERE s_id = 1
)
AND student.`s_id` != 1
~~~

13、查询和01号同学学习的课程完全相同的其他同学的信息

~~~sql
SELECT student.*
FROM student
WHERE s_id IN(
SELECT s_id
FROM score
GROUP BY s_id
HAVING COUNT(*) = (SELECT COUNT(*) FROM score WHERE s_id = 1)
)
AND s_id != 1
~~~

14、查询没学过"张三"老师讲授的任一门课程的学生的姓名

~~~sql
SELECT student.`s_name`
FROM student
WHERE student.`s_id` NOT IN(
	SELECT DISTINCT s_id FROM score
	WHERE score.`c_id` IN  (
		SELECT course.`c_id`
		FROM course
		JOIN teacher
		ON course.`t_id` = teacher.`t_id`
		WHERE teacher.`t_name` = '张三'
	)
)
~~~

15、查询两门及其以上不及格课程的同学的学号、姓名及其平均成绩

~~~sql
SELECT student.`s_id`, student.`s_name`, ROUND(avg_score,2)
FROM student
JOIN
(
	SELECT s_id, AVG(score.`s_score`) avg_score
	FROM score
	GROUP BY score.`s_id`
) ave_score
ON student.`s_id` = ave_score.s_id
WHERE student.`s_id` IN (
	SELECT score.`s_id`
	FROM score
	WHERE s_score < 60
	GROUP BY s_id
	HAVING COUNT(*) >= 2
)
~~~

16、检索‘01’课程小于60，按分数进行降序排列的学生信息

~~~sql
SELECT student.*, s_score
FROM student
JOIN(
    SELECT s_id, s_score
    FROM score
    WHERE c_id = 1 
    AND s_score < 60
) sc
ON student.`s_id` = sc.s_id
ORDER BY s_score DESC
~~~

**17**、按平均成绩从高到低显示所有学生的所有课程的成绩以及平均成绩

~~~sql
SELECT 
	student.`s_id`,student.`s_name`,
	SUM(CASE c_id WHEN 1 THEN s_score ELSE 0 END) 语文,
	SUM(CASE c_id WHEN 2 THEN s_score ELSE 0 END) 数学,
	SUM(CASE c_id WHEN 3 THEN s_score ELSE 0 END) 英语,
	ROUND(IFNULL(AVG(s_score),0),2) 平均成绩
FROM student
LEFT JOIN score
ON student.`s_id` = score.`s_id`
GROUP BY score.`s_id`
ORDER BY 平均成绩 DESC
~~~

18、查询各科成绩最高分、最低分和平均分，以如下形式显示

~~~sql
SELECT course.`c_id`, course.`c_name`,MAX(s_score) 最高分, MIN(s_score) 最低分, ROUND(AVG(s_score),2) 平均分,
	SUM(CASE WHEN s_score >= 60 THEN 1 ELSE 0 END) / COUNT(*) * 100 AS 及格率,
	SUM(CASE WHEN s_score BETWEEN 70 AND 80 THEN 1 ELSE 0 END) / COUNT(*) * 100 AS 中等率,
	SUM(CASE WHEN s_score BETWEEN 80 AND 90 THEN 1 ELSE 0 END) / COUNT(*) * 100 AS 优良率,
	SUM(CASE WHEN s_score >= 90 THEN 1 ELSE 0 END) / COUNT(*) * 100 AS 优秀率
FROM course
JOIN score
ON course.`c_id` = score.`c_id`
GROUP BY score.`c_id`
~~~

19、按各科成绩进行排序并显示排名

~~~sql
SELECT s1.*, rank() over(ORDER BY score.`s_score` DESC) AS 排名
FROM(
	SELECT student.*, course.`c_id`, course.`c_name`, score.`s_score` 
	FROM student
	JOIN score ON score.`s_id` = student.`s_id`
	JOIN course ON course.`c_id` = score.`c_id`
	WHERE course.`c_id` = 1
) s1
UNION
(
SELECT s2.*, rank() over(ORDER BY score.`s_score` DESC) AS 排名
FROM(
	SELECT student.*, course.`c_id`, course.`c_name`, score.`s_score` 
	FROM student
	JOIN score ON score.`s_id` = student.`s_id`
	JOIN course ON course.`c_id` = score.`c_id`
	WHERE course.`c_id` = 2
) s2
)
UNION
(
SELECT s3.*, rank() over(ORDER BY score.`s_score` DESC) AS 排名
FROM(
	SELECT student.*, course.`c_id`, course.`c_name`, score.`s_score` 
	FROM student
	JOIN score ON score.`s_id` = student.`s_id`
	JOIN course ON course.`c_id` = score.`c_id`
	WHERE course.`c_id` = 3
) s3
)
~~~

20、查询学生的总成绩并排名

~~~sql
SELECT student.*, IFNULL(总分,0)
FROM student
LEFT JOIN(
	SELECT s_id, SUM(score.`s_score`)AS 总分
	FROM score
	GROUP BY score.`s_id`
) t
ON student.`s_id` = t.s_id
ORDER BY 总分 DESC
~~~

21、查询不同老师所教不同课程平均分从高到低显示

~~~sql
SELECT teacher.`t_id`, teacher.`t_name`, course.c_id, course.`c_name`, 平均分
FROM teacher
JOIN course
ON teacher.`t_id` = course.`t_id`
JOIN(
	SELECT c_id, AVG(s_score)平均分
	FROM score
	GROUP BY c_id
) cj
ON course.`c_id` = cj.`c_id`
ORDER BY 平均分 DESC
~~~

**22**、查询所有课程的成绩第2名到第3名的学生信息及该课程成绩

~~~sql
SELECT student.*, s1.c_id, s1.`s_score`
FROM student
JOIN
(
	SELECT s_id, c_id, s_score
	FROM score
	WHERE c_id = 1
	ORDER BY s_score DESC LIMIT 1, 2
) s1
ON student.`s_id` = s1.s_id
UNION
(
SELECT student.*, s2.c_id,s2.`s_score`
FROM student
JOIN
(
	SELECT s_id, c_id, s_score
	FROM score
	WHERE c_id = 2
) s2
ON student.`s_id` = s2.s_id
ORDER BY s_score DESC LIMIT 1, 2
)
UNION
(
SELECT student.*, s3.c_id, s3.s_score
FROM student
JOIN
(
	SELECT s_id, c_id, s_score
	FROM score
	WHERE c_id = 3
)s3
ON student.`s_id` = s3.s_id
ORDER BY s_score DESC LIMIT 1, 2
)
~~~

23、统计各科成绩各分段人数:课程编号，课程名次，[100-85],[85-70],[60-70],[0-60]

~~~sql
SELECT score.`c_id`,course.`c_name`,
	SUM(CASE WHEN score.`s_score` BETWEEN 85 AND 100 THEN 1 ELSE 0 END)/COUNT(*) * 100 AS 阶段1,
	SUM(CASE WHEN score.`s_score` BETWEEN 70 AND 85 THEN 1 ELSE 0 END)/COUNT(*) * 100 AS 阶段2,
	SUM(CASE WHEN score.`s_score` BETWEEN 60 AND 70 THEN 1 ELSE 0 END)/COUNT(*) * 100 AS 阶段3,
	SUM(CASE WHEN score.`s_score` BETWEEN 0 AND 60 THEN 1 ELSE 0 END)/COUNT(*) * 100 AS 阶段4
FROM score
JOIN course
ON course.`c_id` = score.`c_id`
GROUP BY score.`c_id`
~~~

**24**、查询学生的平均成绩及其名次

~~~sql
SELECT t1.*, rank() over (ORDER BY 平均成绩 DESC) AS 排名
FROM(
	SELECT student.`s_id` AS id, student.`s_name` AS NAME, AVG(score.`s_score`) AS 平均成绩
	FROM student
	JOIN score
	ON student.`s_id` = score.`s_id`
	GROUP BY score.`s_id`
	ORDER BY AVG(score.`s_score`)
) AS t1
~~~

25、查询各科成绩前三名的记录

~~~sql
SELECT student.*, s1.s_id, s1.c_id, s1.s_score
FROM student
JOIN
(
SELECT s_id, c_id, s_score FROM score WHERE score.`c_id` = 1 ORDER BY s_score DESC LIMIT 3
) s1
ON student.`s_id` =  s1.s_id
UNION
(
SELECT student.*, s2.s_id, s2.c_id, s2.s_score
FROM student
JOIN
(
SELECT s_id, c_id, s_score FROM score WHERE score.`c_id` = 2
) s2
ON student.`s_id` =  s2.s_id
ORDER BY s_score DESC LIMIT 3
)
UNION
(
SELECT student.*, s3.s_id, s3.c_id, s3.s_score
FROM student
JOIN
(
SELECT s_id, c_id, s_score FROM score WHERE score.`c_id` = 3
) s3
ON student.`s_id` =  s3.s_id
ORDER BY s_score DESC LIMIT 3
)
~~~

26、查询每门课程被选修的学生数

~~~sql
SELECT COUNT(*) 选课人数
FROM 
score
GROUP BY c_id
~~~

27、查询出只有两门课程的全部学生的学号和姓名

~~~sql
SELECT student.`s_id`, student.`s_name`
FROM student
JOIN score
ON student.`s_id` = score.`s_id`
GROUP BY student.`s_id`
HAVING COUNT(*) = 2;
~~~

28、查询男生、女生人数

~~~sql
SELECT student.`s_sex`, COUNT(*)
FROM student
GROUP BY s_sex
~~~

29、查询名字中含有"风"字的学生信息

~~~sql
SELECT *
FROM student
WHERE student.`s_name` LIKE '%风%'
~~~

30、查询同名同性学生名单，并统计同名人数

~~~sql
SELECT s_name, COUNT(*)AS 人数
FROM student
GROUP BY s_name, s_sex
HAVING 人数 > 1
~~~

31、查询1990年出生的学生名单

~~~sql
SELECT student.*
FROM student
WHERE YEAR(s_birth) = '1990'
~~~

32、查询每门课的平均成绩、结果按平均成绩降序排序

~~~sql
SELECT course.`c_id`, course.`c_name`,AVG(score.`s_score`) 平均成绩
FROM course
JOIN score
ON course.`c_id` = score.`c_id`
GROUP BY course.`c_id`
ORDER BY 平均成绩 DESC
~~~

33、查询平均成绩大于等于85的所有学生的学号、姓名和平均成绩

~~~sql
SELECT student.*, score.`s_score`
FROM student
JOIN score
ON student.`s_id` = score.`s_id`
GROUP BY student.`s_id`
HAVING AVG(score.`s_score`) >= 85
~~~

34、查询课程名称为"数学"，且分数低于60的学生姓名和分数

~~~sql
SELECT student.`s_name`, score.`s_score`
FROM course
JOIN score
ON course.`c_id` = score.`c_id`
JOIN student
ON student.`s_id` = score.`s_id`
WHERE course.`c_name` = '数学'
AND score.`s_score` < 60
~~~

35、查询所有学生的课程及分数情况

~~~sql
SELECT student.*,
	SUM(CASE WHEN c_id = 1 THEN s_score ELSE 0 END) 语文成绩,
	SUM(CASE WHEN c_id = 2 THEN s_score ELSE 0 END) 数学成绩,
	SUM(CASE WHEN c_id = 3 THEN s_score ELSE 0 END) 英语成绩
FROM student
JOIN score
ON student.`s_id` = score.`s_id`
GROUP BY student.`s_id`
~~~

36、查询任何一门课程成绩在70分以上的姓名、课程名称和分数

~~~sql
SELECT student.`s_name`, course.`c_name`, score.`s_score`
FROM student
JOIN score
ON score.`s_id` = student.`s_id`
JOIN course
ON course.`c_id` = score.`c_id`
WHERE score.`s_score` > 70
~~~

37、查询不及格的课程

~~~sql
SELECT DISTINCT student.*
FROM student
JOIN score
ON student.`s_id` = score.`s_id`
JOIN course
ON course.`c_id` = score.`c_id`
WHERE score.`s_score` < 60
~~~

38、查询课程编号为01且课程成绩在80分以上的学生的学号和姓名

~~~sql
SELECT student.*
FROM student
JOIN score
ON student.`s_id` = score.`s_id`
WHERE score.`c_id` = 1
AND score.`s_score` >= 80
~~~

39、求每门课程的学生人数

~~~sql
SELECT course.`c_id`, course.`c_name`, COUNT(*) 报名人数
FROM course
JOIN score
ON course.`c_id` = score.`c_id`
GROUP BY course.`c_id`
~~~

40、查询选修"张三"老师所授课程的学生中，成绩最高的学生信息及其成绩

~~~sql
SELECT student.*, MAX(score.`s_score`) AS 最高分
FROM student
JOIN score
ON student.`s_id` = student.`s_id`
JOIN course
ON course.`c_id` = score.`c_id`
JOIN teacher
ON teacher.`t_id` = course.`t_id`
WHERE teacher.`t_name` = '张三'
~~~

**41**、查询不同课程成绩相同的学生的学生编号、课程编号、学生成绩

~~~sql
SELECT * 
FROM score
WHERE score.`s_score`
IN
(
SELECT score.`s_score`
FROM score
GROUP BY score.`s_score`
HAVING COUNT(*) > 1
)
~~~

42、查询每门功成绩最好的前三名

~~~sql
(
SELECT student.`s_id`, student.`s_name`, course.`c_name`, score.`s_score`
FROM student
JOIN score
ON score.`s_id` = student.`s_id`
JOIN course
ON score.`c_id` = course.`c_id`
WHERE course.`c_id` = 1
ORDER BY score.`s_score` DESC LIMIT 3
)
UNION
(
SELECT student.`s_id`, student.`s_name`, course.`c_name`, score.`s_score`
FROM student
JOIN score
ON score.`s_id` = student.`s_id`
JOIN course
ON score.`c_id` = course.`c_id`
WHERE course.`c_id` = 2
ORDER BY score.`s_score` DESC LIMIT 3
)
UNION
(
SELECT student.`s_id`, student.`s_name`, course.`c_name`, score.`s_score`
FROM student
JOIN score
ON score.`s_id` = student.`s_id`
JOIN course
ON score.`c_id` = course.`c_id`
WHERE course.`c_id` = 3
ORDER BY score.`s_score` DESC LIMIT 3
)
~~~

43、统计每门课程的学生选修人数（超过5人的课程才统计）

~~~sql
SELECT score.`c_id`, COUNT(*) 选修人数
FROM score
GROUP BY score.`c_id`
HAVING COUNT(*) > 5
~~~

44、检索至少选修两门课程的学生学号

~~~sql
SELECT student.`s_id`
FROM student
WHERE student.`s_id`
IN
(
SELECT score.`s_id`
FROM score
GROUP BY score.`s_id`
HAVING COUNT(*) >= 2
)
~~~

45、查询选修了全部课程的学生信息

~~~sql
SELECT student.*
FROM student
WHERE student.`s_id` IN
(
SELECT score.`s_id`
FROM score
GROUP BY score.`s_id`
HAVING COUNT(*) = 3
)
~~~

46、查询各学生的年龄

~~~sql
SELECT student.`s_id`, student.`s_name`, YEAR(CURRENT_DATE()) - YEAR(student.`s_birth`) AS 年龄
FROM student
~~~

47、查询本周过生日的学生

~~~sql
SELECT * FROM student WHERE WEEK(student.`s_birth`) =  WEEK(CURRENT_DATE())
~~~

48、查询下周过生日的学生

~~~sql
SELECT * FROM student WHERE WEEK(student.`s_birth`) = WEEK(CURRENT_DATE()) + 1
~~~

49、查询本月过生日的学生

~~~sql
SELECT *
FROM student
WHERE MONTH(student.`s_birth`) = MONTH(CURRENT_DATE())
~~~

50、查询下月过生日的学生

~~~sql
SELECT * 
FROM student
WHERE MONTH(student.`s_birth`) = '12'
~~~