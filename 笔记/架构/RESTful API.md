# RESTful API

## 1. HTTP Method

HTTP 常用方法：

- GET：获取某个资源。
- POST：创建一个新的资源。
- PUT：<font color=red>**替换**</font>某个已有的资源。
- PATCH： <font color=red>**修改**</font>某个已有的资源。
- DELETE：删除某个资源。

关于PATCH和PUT，特别说明一下：

PATCH注重于对已知资源进行<font color=red>**局部更新**</font>，PUT注重于对已知资源的**<font color=red>替换</font>**。

前后端开发中如果需要更新数据库中的某条用户的name：

1. PUT：前端传修改name后的完整用户信息给后端，表示该请求是一个替换
2. PATCH：前端只传了name给后端，表示该请求是一个局部更新

相对于PUT的更新操作，PATCH节约了网络资源。但是某些业务需求下PATCH可能不是幂等的

## 2. RESTful API的设计

RESTful API 设计最重要的一个原则：nouns（not verbs），名词（非动词）

<font color=red>**URI 和 HTTP Method 没有任何关系，但是 URI（URL） + HTTP"动词" 共同构成了RESTful API**</font>

CRUD 简单 URI：

- `GET /users` - 获取用户列表
- `GET /users/1` - 获取 Id 为 1 的用户
- `POST /users` - 创建一个用户
- `PUT /users/1` - 替换 Id 为 1 的用户
- `PATCH /users/1` - 修改 Id 为 1 的用户
- `DELETE /users/1` - 删除 Id 为 1 的用户

上面是对某一种资源进行操作的 URI，那如果是有关联的资源，或者称为级联的资源，该如何设计 URI 呢？比如某一用户下的产品：

- `GET /users/1/products` - 获取 Id 为 1 用户下的产品列表
- `GET /users/1/products/2` - 获取 Id 为 1 用户下 Id 为 2 的产品
- `POST /users/1/products` - 在 Id 为 1 用户下，创建一个产品
- `PUT /users/1/products/2` - 在 Id 为 1 用户下，替换 Id 为 2 的产品
- `PATCH /users/1/products/2` - 修改 Id 为 1 的用户下 Id 为 2 的产品
- `DELETE /users/1/products/2` - 删除 Id 为 1 的用户下 Id 为 2 的产品

在设计API时，会进行一些查询操作，比如分页和排序等，这些查询参数（Query）放在哪？该放在URI吗？例如下面这样

~~~json
URI = api/orders/users/{userId}/{pageIndex}/{pageSize}
~~~

API的本意是想获取用户id = 1 下的订单分页查询数据，看看github API是如何设计的：

~~~java
"current_user_repositories_url": "https://api.github.com/user/repos{?type,page,per_page,sort}"
~~~

差别是不是很大？而且我们设计的 URI 表达也比较混乱，我们完善下：

~~~java
URI = /api/orders/users/{userId}/?page=1&per_page=20
~~~

RESTful API需要使用URI来<font color=red>**唯一标识资源**</font>的，除此之外的查询参数可以放于Query中。

但是需要知道Query的作用同样也是<font color=red>**（identify a resouce）标识资源**</font>，换句话说，我也可以这样设计：

~~~java
URI = /api/orders/users/?userId=1&page=1&per_page=20
~~~

哪个设计才是好的？

**将userId放在URI的设计是更出色的**，两个URI都足够简洁，但` /api/orders/users/{userId}`更能起到"顾名思义"的效果，能让开发者更能明白接口的含义是获取某个用户的订单分页数据而不是所有的用户分页数据。

不管怎样，接口一定要简洁，能让别人看得懂

GitHub API（规范参考）：[https://api.github.com](https://api.github.com/)

```json
{
  "current_user_url": "https://api.github.com/user",
  "current_user_authorizations_html_url": "https://github.com/settings/connections/applications{/client_id}",
  "authorizations_url": "https://api.github.com/authorizations",
  "code_search_url": "https://api.github.com/search/code?q={query}{&page,per_page,sort,order}",
  "emails_url": "https://api.github.com/user/emails",
  "emojis_url": "https://api.github.com/emojis",
  "events_url": "https://api.github.com/events",
  "feeds_url": "https://api.github.com/feeds",
  "following_url": "https://api.github.com/user/following{/target}",
  "gists_url": "https://api.github.com/gists{/gist_id}",
  "hub_url": "https://api.github.com/hub",
  "issue_search_url": "https://api.github.com/search/issues?q={query}{&page,per_page,sort,order}",
  "issues_url": "https://api.github.com/issues",
  "keys_url": "https://api.github.com/user/keys",
  "notifications_url": "https://api.github.com/notifications",
  "organization_repositories_url": "https://api.github.com/orgs/{org}/repos{?type,page,per_page,sort}",
  "organization_url": "https://api.github.com/orgs/{org}",
  "public_gists_url": "https://api.github.com/gists/public",
  "rate_limit_url": "https://api.github.com/rate_limit",
  "repository_url": "https://api.github.com/repos/{owner}/{repo}",
  "repository_search_url": "https://api.github.com/search/repositories?q={query}{&page,per_page,sort,order}",
  "current_user_repositories_url": "https://api.github.com/user/repos{?type,page,per_page,sort}",
  "starred_url": "https://api.github.com/user/starred{/owner}{/repo}",
  "starred_gists_url": "https://api.github.com/gists/starred",
  "team_url": "https://api.github.com/teams",
  "user_url": "https://api.github.com/users/{user}",
  "user_organizations_url": "https://api.github.com/user/orgs",
  "user_repositories_url": "https://api.github.com/users/{user}/repos{?type,page,per_page,sort}",
  "user_search_url": "https://api.github.com/search/users?q={query}{&page,per_page,sort,order}"
}
```

## 3. 为什么要用RESTful API

现在再提一个需求：根据用户id获取用户姓名

RESTful风格的API是这样的

~~~java
uri = /api/user/{userId}
~~~

但如果不是RESTful风格的，是下面这样的类RPC的**面向过程**调用

~~~java
uri = /user/getNameById
~~~

这就会带来**<font color=red> "URL膨胀" </font>**问题，举个例子：如果我现在想要根据用户id获取用户性别呢？

如果是RESTful风格，那么不需要重新写，因为返回的用户信息已经包含了性别。但如果是RPC调用就需另写一个URI`getGenderById`

RESTful风格的API设计避免了URI的个数膨胀，但我们也发现RESTful风格的潜在问题：

**<font color=red>我只想要姓名，你怎么返回给我整个用户信息？</font>**

使用RESTful风格的API在应对这种情况时需要丢弃用户的其他信息，这便是一种"过度获取"（OverFetching）。**可以通过增加缓存来解决这类问题。**

## 4. 实战

基础的资源路径：

- 创建 Create：`POST /students`
- 查询 Query：`GET /students?age=0,10&name=姓,&q=搜索`
- 获取 Get： `GET /students/{studentId}`
- 删除 Destroy：`DELETE /students/{studentId}`
- 替换 Update：`PUT /students/{studentsId}`
- 补丁 Patch(部分更新)：`PATCH /students/{studentsId}`
- 替换单个属性 Update Field：`PUT /students/{studentId}/{fieldName}`，请求的 `payload` 就是对象的字段的值
- 班级的对应的方式与 `Student` 的一样，`POST /classes` 等
- 班级下的学生： `POST /classes/{classId}/students` 向班级中添加学生
- 查询班级下的学生：`GET /classes/{classId}/students?age=0,10&name=,姓,&q=搜索`
- 获取班级下学生的详细信息：`GET /classes/{classId}/students/{studentId}`
- ……

我们将接口分为以下几种类型：

1. 创建资源：Create，统一为 `POST /resource-names`
2. 查询资源：Query，统一为 `GET /resource-names?q=查询关键字&age=0,10&name=,姓&limit=10&offset=0`
3. 获取资源：Get，统一为 `GET /resource-names/{recourceId}`
4. 删除资源：Destroy，统一为 `DELETE /resource-names/{recourceId}`
5. 替换资源：Update（全量更新），统一为 `PUT /resource-names/{recourceId}`
6. 部分更新：Patch（打补丁），统一为 `PATCH /resouce-names/{resourceId}/{?fieldName}`
7. 操作请求：Action（比如通过审核、激活等对资源的操作）统一为 `POST /resouce-names/{resourceId}/action-name`
8. 大型查询：Maga Query，比如我们希望从磁带库中查询某一个时间段的数据，可能查询需要很长的时间，那么按以下方式完成：
   a. 创建查询： `POST /queries`，创建一个新的查询，`payload` 为查询所需要的所有配置参数信息，接口返回 `queryId`
   b. 通过 `queryId` 获取状态（或者通过 WebSocket 直接通知响起方）， `GET /queries/{queryId}/status`
   c. 通过 `queryId` 获取查询结果 `GET /queries/{queryId}/rows?limit=10&offset=0&q=搜索`，查询结果得到之后，同样可以对结果进行二次搜索

关于查询参数的规范：

- `age=0,10` 表示查询 `age` 为 `[0, 10)` 区间的数据， `,` 区分起始，同样的还可应用于 *时间*、*日期* 等类型的数据
- `age=,10;20,30;40`表示查询 `age` 为 `[0, 10)`以及`[20,30)`以及大于 `40` 的数据， `,` 前面表示开始值，若不提供，表示所有小于 `,` 右侧的值的值，`,` 右侧表示结束值，若没有，则表示所有大于 `,` 左侧的值，`;` 表示多个值的区分。
- `age=10` 表示 `age===10` 的值
- `name=姓,` 表示以 `姓` 开头的值，类似 `LIKE %姓`
- `name=,名` 表示以 `名` 结尾的值
- `name=姓名` 表示 `name` 完全等于 `姓名` 的值
- `limit` 此次查询多少条数据
- `offset` 从 0 开始的偏移量
- `q` 万能的搜索关键字，后端根据需求选择性使用

## 参考

[RESTful API URI 设计的一些总结 - 田园里的蟋蟀 - 博客园 (cnblogs.com)](https://www.cnblogs.com/xishuai/p/restful-webapi-uri-design.html)

[RESTful API URI 设计: 查询（Query）和标识（Identify） - 田园里的蟋蟀 - 博客园 (cnblogs.com)](https://www.cnblogs.com/xishuai/p/designing-rest-api-uri-query-and-identify.html)

[如何设计一个多条件查询的restfulAPI？ - SegmentFault 思否](https://segmentfault.com/q/1010000040792283)