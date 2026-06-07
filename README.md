# 迈开腿 (MaiKaiTui) - 旅游服务平台

> 探索世界，迈开腿 —— 发现热门景点，规划完美旅程，让每一步都精彩

## 项目简介

迈开腿是一个全栈旅游服务平台，采用 **Spring Cloud Alibaba 微服务架构**，包含管理后台、Web 门户和微信小程序三端。平台集成 **DeepSeek 大语言模型** 提供 AI 智能旅行助手，支持景区浏览、在线购票、收藏管理、智能推荐等核心功能。

- **开发周期**: 2026.05.22 – 2026.06.02
- **开发团队**: 迈开腿开发团队
- **项目文档**: 参见 `doc/` 目录（需求分析、系统设计、测试报告、用户手册 等6份）

---

## 功能模块总览

### 一、三端应用

| 端 | 目录 | 技术栈 | 用户群体 | 核心功能 |
|---|------|--------|----------|----------|
| 管理后台 | `maikaitui-admin/` | Vue 3 + Element Plus + ECharts | 管理员/商户/导游 | 数据仪表盘、系统管理、旅游业务管理、AI 控制台 |
| Web 门户 | `maikaitui-web/` | Vue 3 + Pinia + Swiper + Leaflet | 普通用户 | 景区浏览、在线购票、收藏评论、AI 旅行助手、地图导航 |
| 微信小程序 | `maikaitui-miniapp/` | uni-app (Vue 3) | 微信用户 | 首页推荐、景区探索、订单行程、AI 助手、个人中心 |

### 二、后端微服务（7个模块，111个Java类）

| 模块 | 端口 | 类数 | 职责 |
|------|------|------|------|
| `maikaitui-common` | - | 12 | 公共基础：统一响应、JWT工具、异常处理、MyBatis配置、Redis配置、AOP日志 |
| `maikaitui-gateway` | 8080 | 5 | API网关：路由转发、JWT鉴权、CORS跨域、Sentinel限流 |
| `maikaitui-auth` | 8100 | 8 | 认证服务：用户注册登录、BCrypt加密、JWT签发、Token刷新 |
| `maikaitui-system` | 8200 | 34 | 系统管理：用户/角色/菜单(RBAC)、数据字典、操作日志、XXL-JOB调度 |
| `maikaitui-tourism` | 8300 | 38 | 旅游核心：景区CRUD、地区/分类管理、订单系统、收藏评论、仪表盘统计、小程序接口 |
| `maikaitui-file` | 8400 | 5 | 文件服务：上传/下载/删除、文件校验、阿里云OSS集成 |
| `maikaitui-ai` | 8500 | 9 | AI服务：DeepSeek对话、MongoDB会话管理、Spring AI集成、定时清理 |

### 三、基础设施

| 组件 | 端口 | 用途 |
|------|------|------|
| MySQL 8.0 | 3306 | 业务数据（16张表：8张系统表 + 8张业务表） |
| Redis 7 | 6379 | 缓存 / Token黑名单 |
| MongoDB 7 | 27017 | AI对话会话记录 |
| Nacos 3.1.0 | 8848 | 服务注册 + 配置中心（6个服务配置） |
| Sentinel 1.8.10 | 8080 | 流量控制 + 熔断降级 |
| XXL-JOB 3.4.0 | 8082 | 分布式任务调度（数据清理） |
| Seata | - | 分布式事务（预留） |
| 阿里云 OSS | - | 图片/视频/文档对象存储 |
| DeepSeek API | - | AI大语言模型（deepseek-v4-pro） |

### 四、辅助工具

| 工具 | 目录 | 技术 | 说明 |
|------|------|------|------|
| 景区数据爬虫 | `scraper_attractions/` | Python + Playwright | 6源聚合爬虫，自动去重，导出JSON/CSV |

---

## 技术架构

```
┌──────────────────────────────────────────────────────────────────┐
│                        Client Layer                               │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────────────┐ │
│  │  Admin UI    │  │  Web Portal  │  │  Mini Program (微信小程序) │ │
│  │  Vue3+Ele+   │  │  Vue3+Sass   │  │  uni-app Vue3            │ │
│  │  ECharts     │  │  Swiper+Leaf │  │                          │ │
│  └──────┬───────┘  └──────┬───────┘  └────────────┬─────────────┘ │
└─────────┼─────────────────┼───────────────────────┼───────────────┘
          └─────────────────┼───────────────────────┘
                            │  HTTP/REST (JSON)
┌───────────────────────────┼───────────────────────────────────────┐
│              Spring Cloud Gateway (8080)                           │
│         CORS · JWT 鉴权 · 路由转发 · Sentinel 限流                  │
└───────────────────────────┼───────────────────────────────────────┘
                            │
          ┌─────────────────┼────────────────────────────┐
          │                 │                            │
┌─────────┼─────────────────┼────────────────────────────┼──────────┐
│                           │  Service Layer                        │
│  ┌──────────┐ ┌──────────┐│┌──────────┐ ┌──────────┐ ┌────────┐  │
│  │  Auth    │ │  System  │││ Tourism  │ │   File   │ │   AI   │  │
│  │  :8100   │ │  :8200   │││  :8300   │ │  :8400   │ │ :8500  │  │
│  │ 认证登录  │ │ 用户管理  │││ 景区/订单 │ │ 文件上传  │ │DeepSeek│  │
│  │ JWT令牌  │ │ 角色/菜单 │││ 收藏/评论 │ │  OSS存储  │ │MongoDB │  │
│  └────┬─────┘ └────┬─────┘│└────┬─────┘ └────┬─────┘ └───┬────┘  │
│       │            │      │     │            │          │        │
└───────┼────────────┼──────┼─────┼────────────┼──────────┼────────┘
        │            │      │     │            │          │
┌───────┼────────────┼──────┼─────┼────────────┼──────────┼────────┐
│                           │  Infrastructure Layer                 │
│  ┌────────────────────────┼──────────────────────────────────┐   │
│  │  Nacos 3.1.0 (:8848)   │  服务注册 · 配置中心                │   │
│  └────────────────────────┼──────────────────────────────────┘   │
│  ┌────────────────────────┼──────────────────────────────────┐   │
│  │  Sentinel 1.8.10       │  流量控制 · 熔断降级 · 系统监控     │   │
│  └────────────────────────┼──────────────────────────────────┘   │
│  ┌────────────────────────┼──────────────────────────────────┐   │
│  │  XXL-JOB 3.4.0 (:8082) │  分布式任务调度 · 定时数据清理      │   │
│  └────────────────────────┼──────────────────────────────────┘   │
└────────────────────────────┼──────────────────────────────────────┘
                             │
┌────────────────────────────┼──────────────────────────────────────┐
│                            │  Data Layer                          │
│  ┌──────────────┐  ┌───────┴──────┐  ┌──────────────────┐        │
│  │  MySQL 8.0   │  │  Redis 7     │  │  MongoDB 7       │        │
│  │  :3306       │  │  :6379       │  │  :27017          │        │
│  │  业务数据     │  │  缓存/Session │  │  AI对话记录       │        │
│  └──────────────┘  └──────────────┘  └──────────────────┘        │
│  ┌──────────────────────────────────────────────────────┐        │
│  │  阿里云 OSS (对象存储)  ·  DeepSeek API (大语言模型)    │        │
│  └──────────────────────────────────────────────────────┘        │
└───────────────────────────────────────────────────────────────────┘
```

---

## 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.6 | 基础框架 |
| Spring Cloud | 2023.0.1 | 微服务治理 |
| Spring Cloud Alibaba | 2023.0.1.0 | Nacos + Sentinel 集成 |
| Spring Cloud Gateway | - | API 网关，统一路由和鉴权 |
| Spring Security | - | 安全认证框架 |
| Spring AI | 2.0.0-M6 | DeepSeek 大模型集成 |
| MyBatis Plus | 3.5.5 | ORM 框架，雪花ID + 逻辑删除 |
| MySQL | 8.0 | 关系型数据库（业务数据） |
| Redis | 7.x | 缓存数据库 |
| MongoDB | 7.x | 文档数据库（AI 对话记录） |
| JWT (jjwt) | 0.12.3 | 无状态 Token 认证，24h 过期 |
| XXL-JOB | 3.4.0 | 分布式任务调度（数据清理） |
| Nacos | 3.1.0 | 服务注册 + 配置中心 |
| Sentinel | 1.8.10 | 流量控制 + 熔断降级 |
| Hutool | 5.8.25 | Java 工具库 |
| Docker Compose | - | MySQL + Redis + MongoDB 编排 |

### 管理后台 (maikaitui-admin)
| 技术 | 说明 |
|------|------|
| Vue 3 | Composition API 开发模式 |
| Vite | 极速构建工具 |
| Element Plus | 企业级 UI 组件库 |
| Pinia | 状态管理（持久化插件 pinia-plugin-persistedstate） |
| Vue Router | 路由管理（含导航守卫 + keep-alive 缓存） |
| ECharts | 数据可视化图表 |
| Axios | HTTP 请求封装 |

### Web 门户 (maikaitui-web)
| 技术 | 说明 |
|------|------|
| Vue 3 | Composition API |
| Vite | 构建工具 |
| Pinia | 状态管理 |
| Swiper | 轮播组件（首页 Hero + 热门景点） |
| Leaflet | 地图展示（景区位置导航） |
| Animate.css | CSS 动画库（页面过渡动画） |
| VueUse | Vue 组合式工具函数集 |
| Sass | CSS 预处理器 |
| Marked | Markdown 渲染（AI 对话消息） |

### 微信小程序 (maikaitui-miniapp)
| 技术 | 说明 |
|------|------|
| uni-app | 跨端开发框架 |
| Vue 3 | Composition API（SSR模式） |
| 微信小程序 | 运行平台 |
| HBuilderX | 开发工具 |

### 辅助工具
| 工具 | 说明 |
|------|------|
| scraper_attractions/ | Python 景区数据爬虫（6大来源多源聚合） |
| Playwright | 无头浏览器渲染（爬虫反检测） |

---

## 项目结构

```
maikaitui/
├── maikaitui-backend/                 # 后端微服务父工程 (Spring Cloud Alibaba)
│   ├── pom.xml                        # 父 POM，统一依赖管理 (SB 3.2.6, Java 17)
│   ├── docker-compose.yml             # MySQL 8.0 + Redis 7 + MongoDB 7 编排
│   ├── sql/init.sql                   # 数据库初始化脚本（建表+种子数据）
│   ├── nacos-configs/                 # Nacos 配置文件（各服务 yaml）
│   │   ├── maikaitui-auth.yaml        # 认证服务配置 (MySQL/Redis/JWT)
│   │   ├── maikaitui-system.yaml      # 系统管理配置 (含XXL-JOB执行器)
│   │   ├── maikaitui-tourism.yaml     # 旅游服务配置
│   │   ├── maikaitui-file.yaml        # 文件服务配置 (阿里云OSS)
│   │   ├── maikaitui-gateway.yaml     # 网关路由配置 (5条路由规则)
│   │   └── maikaitui-ai.yaml          # AI服务配置 (DeepSeek/MongoDB/XXL-JOB)
│   │
│   ├── maikaitui-common/              # 公共模块（所有服务共享）
│   │   └── src/main/java/com/maikaitui/common/
│   │       ├── core/                  # Result 统一响应, BaseEntity 基础实体
│   │       ├── security/              # JWT 工具 (Token 生成/验证/解析), JwtUserDetails
│   │       ├── exception/             # BizException + GlobalExceptionHandler
│   │       ├── mybatis/               # MyBatis Plus 配置 (分页/自动填充/逻辑删除)
│   │       ├── redis/                 # Redis 配置
│   │       ├── annotation/            # 自定义注解 (@SysLog)
│   │       ├── aspect/                # AOP 切面 (SysLogAspect)
│   │       ├── config/                # Jackson 序列化配置
│   │       └── utils/                 # SpringContextHolder 工具类
│   │
│   ├── maikaitui-gateway/             # API 网关 (:8080)
│   │   └── src/main/java/com/maikaitui/gateway/
│   │       ├── config/                # CorsConfig, GatewayBeanConfig
│   │       ├── filter/                # AuthGlobalFilter (JWT鉴权+白名单)
│   │       └── handler/               # SentinelFallbackHandler
│   │
│   ├── maikaitui-auth/                # 认证服务 (:8100)
│   │   └── src/main/java/com/maikaitui/auth/
│   │       ├── config/                # SecurityConfig + JwtAuthenticationFilter
│   │       ├── controller/            # AuthController (登录/注册/刷新/个人信息)
│   │       ├── entity/                # SysUser 实体
│   │       ├── mapper/                # SysUserMapper
│   │       └── service/               # AuthService + AuthServiceImpl
│   │
│   ├── maikaitui-system/              # 系统管理 (:8200) 34个类
│   │   └── src/main/java/com/maikaitui/system/
│   │       ├── config/                # XxlJobConfig (执行器:9998)
│   │       ├── controller/            # User/Role/Menu/Dict/Log 5个控制器
│   │       ├── entity/                # User/Role/Menu/Dict/DictData/Log + 关联表 8个实体
│   │       ├── mapper/                # 8个 MyBatis Mapper 接口
│   │       ├── service/               # 5组 Service + ServiceImpl
│   │       └── scheduler/             # CleanupScheduler (定时清理)
│   │
│   ├── maikaitui-tourism/             # 旅游核心 (:8300) 38个类
│   │   └── src/main/java/com/maikaitui/tourism/
│   │       ├── controller/            # Attraction/Category/Comment/Dashboard/Favorite/
│   │       │                          #   Miniapp/Order/Region 8个控制器
│   │       ├── entity/                # Attraction/Category/Comment/Favorite/Order/Region 6个实体
│   │       ├── mapper/                # 6个 MyBatis Mapper
│   │       ├── service/               # 8组 Service + ServiceImpl
│   │       ├── dto/                   # DashboardData DTO
│   │       └── vo/                    # CommentVO, FavoriteVO
│   │
│   ├── maikaitui-file/                # 文件服务 (:8400)
│   │   └── src/main/java/com/maikaitui/file/
│   │       ├── config/                # FileUploadConfig
│   │       ├── controller/            # FileController (上传/下载/删除)
│   │       └── service/               # FileService + FileServiceImpl (OSS集成)
│   │
│   └── maikaitui-ai/                  # AI 服务 (:8500) 9个类
│       └── src/main/java/com/maikaitui/ai/
│           ├── config/                # CommonConfiguration + XxlJobConfig (执行器:9997)
│           ├── controller/            # AiChatController (对话/历史/清除)
│           ├── document/              # ChatSessionDocument (MongoDB文档)
│           ├── entity/dto/            # AiChatRequest DTO
│           ├── jobhandler/            # ChatCleanupJob (XXL-JOB清理过期会话)
│           ├── repository/            # ChatSessionRepository (MongoDB)
│           └── service/               # AiCacheService (缓存服务)
│
├── maikaitui-web/                     # Web 门户 (Vue 3 + Vite)
│   └── src/
│       ├── views/
│       │   ├── home/Home.vue          # 首页（Hero轮播 + 热门景区 + 分类导航）
│       │   ├── attraction/
│       │   │   ├── AttractionList.vue  # 景区列表（搜索/筛选/排序/分页）
│       │   │   ├── AttractionDetail.vue# 景区详情（沉浸式: 图集/介绍/评论/推荐）
│       │   │   └── MapNav.vue         # 地图导航（Leaflet景区位置）
│       │   ├── user/
│       │   │   ├── UserProfile.vue    # 个人信息（查看/编辑）
│       │   │   ├── UserOrders.vue     # 我的订单（待支付/已支付/已取消 选项卡）
│       │   │   └── UserFavorites.vue  # 我的收藏
│       │   ├── ai/AiChat.vue          # AI对话全屏页（沉浸式）
│       │   ├── login/Login.vue        # 登录/注册
│       │   └── NotFound.vue           # 404页面
│       ├── components/
│       │   ├── layout/
│       │   │   ├── AppHeader.vue      # 顶部导航（桌面菜单/移动端汉堡菜单/搜索/用户下拉）
│       │   │   └── AppFooter.vue      # 底部（品牌/快速链接/帮助中心/社交媒体/版权）
│       │   └── common/
│       │       ├── AttractionCard.vue # 景区卡片（图片/分类标签/收藏/评分/价格）
│       │       ├── SearchBar.vue      # 搜索栏
│       │       ├── StarRating.vue     # 星级评分展示
│       │       ├── LoginModal.vue     # 登录/注册模态框（Teleported）
│       │       ├── TicketModal.vue    # 购票弹窗（数量/日期/联系人）
│       │       ├── BackToTop.vue      # 回到顶部按钮
│       │       └── FloatingAiChat.vue # 浮动AI聊天（侧边面板420px: 会话列表/消息/快捷提问/Markdown渲染）
│       ├── router/index.js            # 路由（含导航守卫 + 沉浸模式）
│       ├── stores/                    # Pinia状态管理
│       │   ├── user.js                # 用户Token/信息/登录状态
│       │   ├── attraction.js          # 热门景区/当前景区/推荐
│       │   └── aiChat.js              # AI面板开关/上下文(reactive)
│       ├── api/index.js               # 全部API函数（认证/景区/订单/收藏/评论/文件/AI）
│       └── utils/
│           ├── request.js             # Axios封装（Token/用户头/401处理/自定义通知事件）
│           └── travel.js              # 工具库（数据规范化/图片解析/树展平/价格格式化/离线回退数据）
│
├── maikaitui-admin/                   # 管理后台 (Vue 3 + Element Plus)
│   └── src/
│       ├── views/
│       │   ├── dashboard/Dashboard.vue # 仪表盘（ECharts数据统计图表）
│       │   ├── login/Login.vue         # 登录
│       │   ├── system/                 # 系统管理模块
│       │   │   ├── user/UserManagement.vue    # 用户管理（CRUD + 角色分配 + 启用/禁用）
│       │   │   ├── role/RoleManagement.vue    # 角色管理（CRUD + 菜单权限分配）
│       │   │   ├── menu/MenuManagement.vue    # 菜单管理（树形结构 + 权限标识）
│       │   │   ├── dict/DictManagement.vue    # 字典管理（类型+数据项）
│       │   │   └── log/LogManagement.vue      # 操作日志查看
│       │   ├── tourism/                # 旅游管理模块
│       │   │   ├── attraction/AttractionManagement.vue # 景区管理（CRUD + 热门设置）
│       │   │   ├── region/RegionManagement.vue         # 地区管理（4级树形）
│       │   │   ├── category/CategoryManagement.vue     # 分类管理（树形）
│       │   │   ├── order/OrderManagement.vue           # 订单管理（列表+状态更新）
│       │   │   └── comment/CommentManagement.vue       # 评论管理（列表+删除审核）
│       │   └── ai/AiChat.vue           # AI 控制台
│       ├── components/
│       │   ├── layout/
│       │   │   ├── Layout.vue          # 主布局（可折叠侧边栏220px/64px + 头部 + keep-alive内容区）
│       │   │   ├── Sidebar.vue         # 侧边栏（菜单路由渲染 + Logo品牌）
│       │   │   └── HeaderBar.vue       # 顶栏（面包屑/折叠按钮/用户头像下拉）
│       │   └── common/ImageUpload.vue  # 图片上传组件（自动/手动双模式）
│       ├── router/index.js             # 路由（auth守卫/动态title）
│       ├── stores/
│       │   ├── user.js                 # Token/用户信息/权限（持久化）
│       │   └── app.js                  # 侧边栏状态/活跃菜单（持久化）
│       ├── api/                        # API函数（auth/system/tourism/file）
│       │   ├── auth.js                 # 登录/用户信息/刷新Token
│       │   ├── system.js               # 用户/角色/菜单/字典/日志 CRUD
│       │   ├── tourism.js              # 景区/地区/分类/订单/评论/仪表盘 CRUD
│       │   └── file.js                 # 文件上传
│       └── utils/request.js            # Axios封装（Token拦截/401处理）
│
├── maikaitui-miniapp/                 # 微信小程序 (uni-app Vue 3)
│   └── src/
│       ├── pages/
│       │   ├── index/index.vue         # 首页（热门景区/分类导航/推荐）
│       │   ├── attraction/
│       │   │   ├── list.vue            # 探索景点（搜索/筛选/排序/分页）
│       │   │   └── detail.vue          # 景点详情（图集/介绍/购票/收藏/评论）
│       │   ├── favorites/index.vue     # 我的收藏
│       │   ├── orders/index.vue        # 我的订单（行程Tab）
│       │   ├── ai/index.vue            # AI旅行助手（DeepSeek对话/青色主题）
│       │   ├── user/index.vue          # 个人中心（信息/设置）
│       │   └── login/index.vue         # 登录/注册
│       ├── pages.json                  # 页面路由 + 底部TabBar配置（5个Tab）
│       ├── api/index.js                # 全部API函数（含小程序专属: getHomeData/getUserStats/miniappRecommend）
│       ├── store/index.js              # 状态管理(reactive): Token/UserInfo/游客模式
│       └── utils/
│           ├── request.js              # uni.request封装（Token/401/分页解析/排序映射）
│           └── travel.js               # 工具库（数据规范化/图片解析/格式化/离线回退+本地图标）
│
├── scraper_attractions/               # 景区数据爬虫 (Python + Playwright)
│   ├── config.py                      # 配置中心（MAX_ITEMS/超时/延迟/反检测）
│   ├── main.py                        # 入口（CLI参数/多源并发调度/合并去重/导出JSON+CSV）
│   ├── models.py                      # 数据模型（ScenicSpot + ScrapeResult dataclass）
│   └── scrapers/
│       ├── __init__.py                # 爬虫注册表
│       ├── base_scraper.py            # 抽象基类（浏览器生命周期/反检测/文本提取/图片提取/滚动/去重）
│       ├── baike_scraper.py           # 百度百科爬虫（32个硬编码景点/结构化信息框提取）
│       ├── ctrip_scraper.py           # 携程爬虫（搜索API → 详情页）
│       ├── mafengwo_scraper.py        # 马蜂窝爬虫（30个硬编码POI ID/直接详情页）
│       ├── ourtour_scraper.py         # 中旅旅行爬虫（产品列表页卡片提取）
│       ├── qyer_scraper.py            # 穷游网爬虫（19个城市拼音/城市POI列表遍历）
│       └── tripadvisor_scraper.py     # 猫途鹰爬虫（20个英文关键词搜索 → 搜索结果列表）
│
├── doc/                               # 📁 项目文档
│   ├── 1.0系统需求分析报告.docx       # 系统需求分析 (8章)
│   ├── 2.0系统设计说明书.docx          # 系统设计说明书
│   ├── 3.0代码评审.docx               # 代码评审报告
│   ├── 4.0测试计划.docx               # 测试计划
│   ├── 4.1项目测试报告.docx           # 项目测试报告 (通过率 95.7%)
│   ├── 5.0用户操作手册.docx           # 用户操作手册
│   └── 周报.docx                      # 周报
│
├── nacos3.1.0/                        # Nacos 注册中心/配置中心 (:8848)
├── sentinel-dashboard-1.8.10.jar      # Sentinel 控制台
├── xxl-job-3.4.0/                     # XXL-JOB 任务调度中心 (:8082)
├── seata-server/                      # Seata 分布式事务 (预留)
└── README.md
```

---

## 微服务模块详解

### maikaitui-common（公共模块）— 12个类
所有微服务共享的基础模块，通过 `@ComponentScan` 自动装配：

| 组件 | 类 | 说明 |
|------|-----|------|
| 统一响应 | `Result<T>` | `{code, message, data}` 标准格式 |
| 基础实体 | `BaseEntity` | 自动填充 createTime / updateTime |
| JWT工具 | `JwtTokenProvider` | Token生成/验证/解析，支持 UserDetails |
| JWT用户 | `JwtUserDetails` | JWT载荷用户信息模型 |
| 全局异常 | `BizException` + `GlobalExceptionHandler` | 业务异常 + 统一拦截返回 |
| MyBatis配置 | `MyBatisPlusConfig` | 分页插件 + 雪花算法ID(ASSIGN_ID) + MetaObjectHandler |
| Jackson配置 | `JacksonConfig` | Long→String序列化防JS精度丢失 |
| Redis配置 | `RedisConfig` | 序列化 + 连接池 |
| 操作日志 | `@SysLog` + `SysLogAspect` | 注解+AOP自动记录操作日志 |
| Spring工具 | `SpringContextHolder` | ApplicationContext持有者 |

### maikaitui-gateway（API 网关 — 端口 8080）— 5个类
- **AuthGlobalFilter**: 全局JWT鉴权过滤器，白名单放行 `/api/auth/login`, `/api/auth/register`, GET `/api/tourism/attraction/**`
- **CorsConfig**: 全局CORS跨域配置
- **GatewayBeanConfig**: 网关Bean配置
- **SentinelFallbackHandler**: Sentinel限流/熔断降级处理
- 解析用户信息通过 Header 传递：`X-User-Id`, `X-Username`

**5条路由规则**（均通过 Nacos 负载均衡 `lb://`）：

| 路由前缀 | 目标服务 |
|---------|----------|
| `/api/auth/**` | maikaitui-auth |
| `/api/system/**` | maikaitui-system |
| `/api/tourism/**` | maikaitui-tourism |
| `/api/file/**` | maikaitui-file |
| `/api/ai/**` | maikaitui-ai |

### maikaitui-auth（认证服务 — 端口 8100）— 8个类
- **SecurityConfig**: Spring Security 配置（禁用CSRF/无状态会话/密码编码器BCrypt）
- **JwtAuthenticationFilter**: JWT认证过滤器，从请求头提取Token并设置SecurityContext
- **AuthController**: 4个端点
  - `POST /api/auth/login` — 用户名密码登录 → 验证BCrypt → 生成JWT
  - `POST /api/auth/register` — 注册（用户名/密码/昵称）→ BCrypt加密 → 存入sys_user
  - `POST /api/auth/refresh-token` — Token刷新续签
  - `GET /api/auth/user-info` — 获取当前用户信息
- **SysUser**: 用户实体（id/username/password/nickname/phone/email/avatar/status）
- **AuthServiceImpl**: 业务逻辑实现

### maikaitui-system（系统管理服务 — 端口 8200）— 34个类

**RBAC 权限模型**：用户 → 角色 → 菜单（5级角色 × 菜单粒度控制）

| 控制器 | 端点 | 功能 |
|--------|------|------|
| `SysUserController` | `/api/system/user/**` | 用户CRUD + 角色分配 + 状态启用/禁用 + 软删除 + 分页查询 |
| `SysRoleController` | `/api/system/role/**` | 角色CRUD + 菜单权限分配（role_menu关联表） |
| `SysMenuController` | `/api/system/menu/**` | 菜单树形CRUD + 权限标识 + 图标配置 + 排序 |
| `SysDictController` | `/api/system/dict/**` | 字典类型CRUD + 字典数据项CRUD（类型-数据两级管理） |
| `SysLogController` | `/api/system/log/list` | 操作日志分页查询（@SysLog自动记录的审计日志） |

**8张数据库表**: sys_user, sys_role, sys_menu, sys_user_role, sys_role_menu, sys_dict_type, sys_dict_data, sys_log

**定时任务**: CleanupScheduler + XxlJobConfig（执行器端口 9998），定时清理过期数据

### maikaitui-tourism（旅游核心服务 — 端口 8300）— 38个类

**8个控制器对应8个业务模块**：

| 控制器 | 核心功能 |
|--------|----------|
| `AttractionController` | 景区CRUD + 热门设置(isHot) + 浏览量统计(viewCount) + 多条件搜索(关键词/地区/分类/价格区间/评分) + 分页排序(热门/评分/价格/最新) |
| `RegionController` | 地区树形管理（国家→省份→城市→区县4级） |
| `CategoryController` | 分类树形管理（自然风光/历史古迹/主题乐园/博物馆/美食街区/海滩度假/登山徒步） |
| `OrderController` | 下单→支付→取消流程，订单号格式MKT+时间戳+4位随机码，状态: pending/paid/completed/cancelled |
| `CommentController` | 评分(1-5星)+文字评论，支持管理端审核删除 |
| `FavoriteController` | 添加/取消/列表，唯一键(user_id, attraction_id, deleted)防重复+支持恢复 |
| `DashboardController` | 仪表盘统计：景区总数/订单总数/用户总数/成交总额/近期订单趋势 |
| `MiniappController` | 小程序专属接口：首页聚合数据(getHomeData)/用户统计(getUserStats)/推荐(miniappRecommend) |

**区域推荐引擎**：浏览某景区 → 查询同region_id的景区按评分降序 → 不足时补充同category_id的跨地区推荐

**6张业务表**: tourism_attraction, tourism_region, tourism_category, tourism_order, tourism_comment, tourism_favorite

### maikaitui-file（文件服务 — 端口 8400）— 5个类
- `POST /api/file/upload` — 文件上传（支持jpg/jpeg/png/gif/bmp/webp/mp4/avi/doc/docx/pdf）
- `GET /api/file/download/{fileName}` — 文件下载
- `DELETE /api/file/{fileName}` — 文件删除
- 阿里云 OSS 集成（Endpoint: oss-cn-hangzhou / Bucket: scenicc）

### maikaitui-ai（AI 服务 — 端口 8500）— 9个类

**DeepSeek 大模型集成**（Spring AI + OpenAI 兼容协议）：

| 组件 | 类 | 说明 |
|------|-----|------|
| 对话控制器 | `AiChatController` | POST `/api/ai/chat`（支持streaming）/ GET 历史 / DELETE 清除 |
| 请求DTO | `AiChatRequest` | 消息内容 + sessionId（可选，用于续接会话） |
| MongoDB文档 | `ChatSessionDocument` | 会话ID + 用户ID + 消息列表（role/content/timestamp） |
| MongoDB仓库 | `ChatSessionRepository` | Spring Data MongoDB 仓库接口 |
| 缓存服务 | `AiCacheService` | AI响应缓存，提升重复问题响应速度 |
| 配置 | `CommonConfiguration` | RestClient / Spring AI ChatClient Bean |
| 定时清理 | `ChatCleanupJob` + `XxlJobConfig` | XXL-JOB handler，定期清理过期对话（执行器端口9997） |

**对话流程**：
```
用户提问 → POST /api/ai/chat {message, sessionId?}
  → Gateway JWT鉴权 → X-User-Id Header传递
  → 查询/创建 MongoDB chat_session 文档
  → 构建消息上下文: System Prompt(旅行助手角色) + 历史消息 + 当前问题
  → 调用 DeepSeek API (deepseek-v4-pro, base_url: https://api.deepseek.com)
  → Streaming 响应 / 非Streaming 响应
  → 保存 assistant 回复到 MongoDB
  → 返回 Markdown 格式响应（前端用 marked 渲染）
```

---

## 前端应用详解

### 管理后台 (maikaitui-admin) — 16个页面

#### 路由结构（需登录）
```
/login                              → 登录页
/                                   → 重定向到 /dashboard
/dashboard                          → 数据仪表盘（ECharts图表）
/system/user                        → 用户管理（CRUD + 角色分配 + 状态切换）
/system/role                        → 角色管理（CRUD + 菜单权限树分配）
/system/menu                        → 菜单管理（树形CRUD + 权限标识 + 图标）
/system/dict                        → 字典管理（类型 + 数据项两级）
/system/log                         → 操作日志（分页查看）
/tourism/attraction                 → 景区管理（CRUD + 热门标记 + 图片上传）
/tourism/region                     → 地区管理（4级树形）
/tourism/category                   → 分类管理（树形）
/tourism/order                      → 订单管理（列表 + 状态更新 + 取消）
/tourism/comment                    → 评论管理（审核 + 删除）
/ai/chat                            → AI 控制台
```

#### 核心组件
- **Layout.vue**: Element Plus 经典后台布局，可折叠侧边栏(220px↔64px)，`<keep-alive>` 缓存所有页面，fade-transform 过渡动画
- **Sidebar.vue**: 动态渲染路由菜单，Logo品牌"迈开腿"，collapse 模式显示图标
- **HeaderBar.vue**: 面包屑导航，折叠开关，用户头像下拉（个人信息/修改密码/退出登录确认）
- **ImageUpload.vue**: 图片上传组件，支持自动上传和延迟批量上传两种模式

#### 状态管理 (Pinia + 持久化)
- **user store**: token / userInfo(id, username, nickname, avatar, roles, permissions) / login / logout / getUserInfo
- **app store**: sidebarCollapsed / activeMenu

### Web 门户 (maikaitui-web) — 10个页面 + 9个组件

#### 路由结构
```
/                                   → 首页（Hero轮播 + 热门景区Top5 + 分类导航）
/attractions                        → 景区列表（搜索/筛选/排序/分页）
/attraction/:id                     → 景区详情（沉浸式: 图集/介绍/价格/评论/推荐）
/attraction/:id/nav                 → 地图导航（沉浸式: Leaflet地图定位）
/user/profile                       → 个人信息（需登录: 查看编辑）
/user/orders                        → 我的订单（需登录: 待支付/已支付/已取消Tab）
/user/favorites                     → 我的收藏（需登录）
/ai                                 → AI对话全屏（沉浸式）
/login                              → 登录/注册
/:pathMatch(.*)*                    → 404
```

#### 核心组件
| 组件 | 功能 |
|------|------|
| `AppHeader.vue` | 固定顶栏：Logo + 桌面导航(首页/景区/景点/攻略/AI对话/关于我们) + 搜索栏 + 用户下拉(需登录时) 或 登录/注册按钮 + 移动端汉堡菜单全屏覆盖 |
| `AppFooter.vue` | 品牌介绍/快速导航链接/帮助中心/社交媒体(微信/微博/抖音)/版权法律信息 |
| `AttractionCard.vue` | 景区卡片：封面图 + 分类标签 + 收藏切换(♡/♥) + 名称/地区/评分/价格 |
| `TicketModal.vue` | 购票弹窗(Teleported)：数量选择(1-10) + 游览日期 + 联系人姓名/电话 |
| `LoginModal.vue` | 登录/注册模态框(Teleported)：用户名密码表单 + Tab切换 |
| `FloatingAiChat.vue` | 浮动AI气泡 → 侧边面板(420px)：会话历史侧栏 + 消息列表 + 打字指示器 + 快捷提问 + Markdown渲染(marked) + 景区上下文感知 |
| `StarRating.vue` | 星级评分展示组件 |
| `SearchBar.vue` | 搜索输入组件 |
| `BackToTop.vue` | 回到顶部浮动按钮 |

#### 状态管理
- **user store** (Pinia): token / userInfo(safe JSON parse) / isLoggedIn / login / register / logout / fetchUserInfo
- **attraction store** (Pinia): hotAttractions / currentAttraction / recommendations / fetch actions
- **aiChat store** (reactive): panelOpen / context(attractionId/name) / open/close/toggle/setContext

#### 沉浸模式 (Immersive Mode)
景区详情、地图导航、AI对话页面支持沉浸模式：隐藏Header/Footer，全屏体验，通过路由 `meta.immersive: true` 控制

#### 工具库 (travel.js)
- 5个离线圈存景点（九寨沟/黄山/张家界/桂林漓江/西湖）+ 6个分类
- `normalizeAttraction()`: API数据规范化（保证最少8张图片，所有字段有默认值）
- `flattenTree()`: 嵌套树展平
- `formatPrice()`: "¥199" 或 "免费"
- `formatCount()`: 大数格式化 "1.2万"
- `regionMap` / `categoryMap`: ID→名称映射表

### 微信小程序 (maikaitui-miniapp) — 8个页面 + 5个Tab

#### 页面结构 (pages.json声明式路由)
```
pages/index/index           → 首页（热门景区/分类/推荐，下拉刷新，自定义导航）
pages/attraction/list       → 探索景点（搜索/排序:热门-评分-价格-最新/分页）
pages/attraction/detail     → 景点详情（图集/购票/收藏/评论，自定义导航）
pages/favorites/index       → 我的收藏
pages/orders/index          → 我的订单/行程
pages/ai/index              → AI旅行助手（青色主题导航 #1fb5a7）
pages/user/index            → 个人中心
pages/login/index           → 登录/注册（自定义导航）
```

#### 底部TabBar（5个Tab，绿色主题 #1f8f3a）
| Tab | 页面 | 图标含义 |
|-----|------|---------|
| 首页 | pages/index/index | 主页 |
| 景区 | pages/attraction/list | 探索 |
| 发现 | pages/ai/index | AI助手 |
| 行程 | pages/orders/index | 订单 |
| 我的 | pages/user/index | 个人 |

#### 特色功能
- **游客模式**: 默认开启，未登录也可浏览景区和AI对话
- **本地图标**: 分类使用本地 `/static/icons/home/*.png` 图标
- **小程序专属API**: getHomeData(首页聚合) / getUserStats(用户统计) / miniappRecommend(推荐)
- **离线回退**: 4个离线景点 + 8个离线分类（含iconKind/iconImage路径）

---

## 景区数据爬虫详解

### 架构设计

采用 **策略模式** 实现多源爬虫：

```
BaseScraper (抽象基类)
├── 浏览器管理: Playwright Chromium 无头/有头模式
├── 反检测: --disable-blink-features=AutomationControlled + UA伪装 + navigator覆盖
├── 工具方法: _text(多选择器回退) / _images(过滤图标Logo) / _scroll(平滑滚动) / _sleep(随机延迟)
├── 去重算法: 大小写不敏感名称去重
│
├── BaikeScraper      百度百科 — 32个硬编码景点 → 结构化信息框提取
├── MafengwoScraper   马蜂窝   — 30个硬编码POI ID → 直接详情页
├── QyerScraper        穷游网   — 19个城市拼音 → 城市POI列表遍历
├── CtripScraper       携程     — 搜索API → 搜索结果 → 详情页
├── TripAdvisorScraper 猫途鹰   — 20个英文关键词 → 搜索结果列表
└── OurtourScraper     中旅旅行 — 产品列表页 → 卡片提取
```

### 爬虫策略对比

| 爬虫 | 目标数 | 策略类型 | 提取方式 | 特点 |
|------|--------|----------|----------|------|
| 百度百科 | 32 | 硬编码直达 | 信息框结构化 | 数据最规范，票价/开放时间/级别准确 |
| 马蜂窝 | 30 | 硬编码POI | 详情页选择器 | 图片丰富，用户评价多 |
| 穷游网 | 19城市 | 遍历发现 | 城市POI卡片 | 覆盖广，城市→景点层次清晰 |
| 携程 | 动态 | 搜索驱动 | 搜索+详情 | 灵活，可按关键词搜索 |
| 猫途鹰 | 20 | 英文搜索 | 搜索结果 | 国际化视角，外国游客数据 |
| 中旅旅行 | 单页 | 列表提取 | 产品卡片 | 最简单，适合快速补充 |

### 数据模型与输出

```python
@dataclass
class ScenicSpot:
    name: str              # 景区名称
    ticket_price: str|None # 门票价格
    opening_hours: str|None# 开放时间
    images: list[str]      # 图片URL列表
    description: str|None  # 简介
    rating_level: str|None # 景区级别 (5A/4A/3A)
```

输出: `output/spots_YYYYMMDD_HHMMSS.json` + `output/spots_YYYYMMDD_HHMMSS.csv`

### 使用方式

```bash
cd scraper_attractions
pip install -r requirements.txt
playwright install chromium

# 全部6源聚合，总共50条
python main.py -m 50

# 指定关键词
python main.py -k 北京 -m 20

# 指定来源
python main.py -s baike,qyer,mafengwo

# 显示浏览器（调试模式）
python main.py -m 10 --no-headless
```

---

## 数据库设计

### 系统模块表（sys_*）
| 表名 | 说明 | 核心字段 |
|------|------|----------|
| sys_user | 用户表 | id, username, password(BCrypt), nickname, phone, email, avatar, status |
| sys_role | 角色表 | id, role_name, role_code (super_admin/admin/merchant/user/tour_guide) |
| sys_menu | 菜单权限表 | id, parent_id(树形), menu_name, path, component, permission, icon |
| sys_user_role | 用户-角色关联 | user_id, role_id |
| sys_role_menu | 角色-菜单关联 | role_id, menu_id |
| sys_dict_type | 字典类型表 | dict_name, dict_type |
| sys_dict_data | 字典数据表 | dict_type, label, value, sort |
| sys_log | 操作日志表 | user_id, operation, method, params, ip, create_time |

### 旅游模块表（tourism_*）
| 表名 | 说明 | 核心字段 |
|------|------|----------|
| tourism_attraction | 景区表 | id, name, description, region_id, category_id, price, rating, cover_image, images, address, opening_hours, view_count, is_hot |
| tourism_region | 地区表 | id, name, parent_id(树形), level(1国家/2省份/3城市/4区县) |
| tourism_category | 分类表 | id, name, icon, parent_id(树形) |
| tourism_comment | 评论表 | id, attraction_id, user_id, content, rating(1-5) |
| tourism_order | 订单表 | id, order_no(MKT开头), user_id, attraction_id, quantity, total_price, order_status(pending/paid/completed/cancelled), visit_date, contact_name, contact_phone |
| tourism_favorite | 收藏表 | id, user_id, attraction_id, deleted(逻辑删除) |

### 用户角色体系
| 角色 | 编码 | 权限范围 |
|------|------|---------|
| 超级管理员 | super_admin | 全部权限：系统管理 + 旅游管理 + AI 控制台 + 仪表盘 |
| 管理员 | admin | 系统管理（用户管理、内容审核） |
| 商户 | merchant | 管理自有景区、查看订单 |
| 导游 | tour_guide | 管理个人资料、接受预订 |
| 普通用户 | user | 浏览景区、下单购票、评论、收藏 |

### 设计特点
- **雪花算法 ID**: MyBatis Plus `ASSIGN_ID`，19位 Long，Jackson 全局序列化为 String 防 JS 精度丢失
- **逻辑删除**: `@TableLogic`，deleted=0 正常 / deleted=1 已删除
- **自动填充**: `MetaObjectHandler` 自动填充 createTime / updateTime
- **唯一键设计**: 收藏表 `UNIQUE(user_id, attraction_id, deleted)` 支持软删除后恢复

---

## 快速开始

### 环境要求
| 环境 | 版本要求 |
|------|----------|
| JDK | 17+ (推荐 GraalVM JDK 17) |
| Maven | 3.8+ |
| Node.js | 18+ |
| Docker | Docker Compose (MySQL/Redis/MongoDB) |
| Python | 3.9+ (仅爬虫工具需要) |
| HBuilderX | 最新版 (仅小程序开发需要) |

### 1. 启动基础设施（MySQL + Redis + MongoDB）

```bash
cd maikaitui-backend
docker-compose up -d
```

启动后自动完成：
- MySQL (3306): 创建 `maikaitui` 库 + 执行 `sql/init.sql` 建表 + 插入种子数据
- Redis (6379): 缓存服务
- MongoDB (27017): AI 对话记录存储

### 2. 启动 Nacos（服务注册 & 配置中心）

```bash
cd nacos3.1.0/bin
# Windows
startup.cmd -m standalone
# Linux/Mac
sh startup.sh -m standalone
```

> Nacos 控制台: http://127.0.0.1:8848/nacos (账号: nacos / nacos)

### 3. 编译并启动后端服务

```bash
cd maikaitui-backend

# 编译全部模块
mvn clean install -DskipTests

# 按顺序启动（确保 Nacos 已启动）
# 1. 网关
cd maikaitui-gateway && mvn spring-boot:run &

# 2. 业务服务（可并行启动）
cd ../maikaitui-auth && mvn spring-boot:run &
cd ../maikaitui-system && mvn spring-boot:run &
cd ../maikaitui-tourism && mvn spring-boot:run &
cd ../maikaitui-file && mvn spring-boot:run &
cd ../maikaitui-ai && mvn spring-boot:run &
```

> 在 IDEA 中打开 `maikaitui-backend`，直接运行各模块的 `*Application.java` main 方法即可。

各服务端口：
| 服务 | 端口 | 说明 |
|------|------|------|
| maikaitui-gateway | 8080 | API 网关（前端统一入口） |
| maikaitui-auth | 8100 | 认证服务 |
| maikaitui-system | 8200 | 系统管理 |
| maikaitui-tourism | 8300 | 旅游核心业务 |
| maikaitui-file | 8400 | 文件服务 |
| maikaitui-ai | 8500 | AI 对话服务 |

### 4. 启动 XXL-JOB（可选，定时任务调度）

```bash
cd xxl-job-3.4.0/bin
# Windows
startup.cmd
```

> XXL-JOB 控制台: http://127.0.0.1:8082/xxl-job-admin (账号: admin / 123456)

### 5. 启动 Sentinel Dashboard（可选，流量监控）

```bash
java -jar sentinel-dashboard-1.8.10.jar
```

> Sentinel 控制台: http://127.0.0.1:8080 (账号: sentinel / sentinel)

### 6. 启动管理后台

```bash
cd maikaitui-admin
npm install
npm run dev
```

> 访问: http://localhost:3000  
> 默认管理员: admin / 123456

### 7. 启动 Web 门户

```bash
cd maikaitui-web
npm install
npm run dev
```

> 访问: http://localhost:5173

### 8. 运行微信小程序（可选）

1. 使用 HBuilderX 打开 `maikaitui-miniapp` 目录
2. 修改 `src/api/index.js` 中的 `BASE_URL` 为后端实际地址
3. 在 `manifest.json` 中配置微信小程序 AppID
4. 点击运行 → 运行到小程序模拟器 → 微信开发者工具

---

## API 接口概览

所有接口通过 Gateway (8080) 统一访问。统一响应格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { }
}
```

### 认证接口 `/api/auth/**`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/auth/login | 用户登录，返回 JWT + 用户信息 | 否 |
| POST | /api/auth/register | 用户注册（用户名/密码/昵称） | 否 |
| POST | /api/auth/refresh-token | 刷新令牌（24h有效期） | 是 |
| GET | /api/auth/user-info | 获取当前登录用户信息 | 是 |

### 系统管理接口 `/api/system/**`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST/PUT/DELETE | /api/system/user/** | 用户CRUD + 角色分配 + 分页查询 |
| GET/POST/PUT/DELETE | /api/system/role/** | 角色CRUD + 菜单权限树分配 |
| GET/POST/PUT/DELETE | /api/system/menu/** | 菜单树形CRUD + 权限标识 + 图标 |
| GET/POST/PUT/DELETE | /api/system/dict/type/** | 字典类型管理 |
| GET/POST/PUT/DELETE | /api/system/dict/data/** | 字典数据项管理 |
| GET | /api/system/log/list | 操作日志分页查询 |

### 旅游服务接口 `/api/tourism/**`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/tourism/attraction/list | 景区列表（分页+多条件筛选+排序） |
| GET | /api/tourism/attraction/hot | 热门景区 Top N（viewCount+isHot综合） |
| GET | /api/tourism/attraction/{id} | 景区详情（自动增加viewCount） |
| GET | /api/tourism/attraction/{id}/recommendations | 区域推荐（同地区高评分，不足跨地区补充） |
| POST/PUT/DELETE | /api/tourism/attraction/** | 景区管理（管理端CRUD） |
| GET | /api/tourism/region/tree | 地区树（4级：国家→省→市→区县） |
| GET | /api/tourism/category/tree | 分类树（父-子层级） |
| GET/POST | /api/tourism/comment/** | 评论列表/添加评论(1-5星) |
| DELETE | /api/tourism/comment/{id} | 删除评论（管理端审核） |
| POST | /api/tourism/order | 创建订单（景区ID/数量/日期/联系人） |
| GET | /api/tourism/order/list | 我的订单列表（按状态筛选） |
| GET | /api/tourism/order/admin/list | 管理端订单列表 |
| PUT | /api/tourism/order/{id}/pay | 支付订单（pending→paid） |
| PUT | /api/tourism/order/{id}/cancel | 取消订单（软删除） |
| POST | /api/tourism/favorite | 添加收藏（唯一键去重+恢复） |
| GET | /api/tourism/favorite/list | 我的收藏列表 |
| DELETE | /api/tourism/favorite/{id} | 取消收藏（软删除） |
| GET | /api/tourism/favorite/check | 检查是否已收藏 |
| GET | /api/tourism/dashboard | 仪表盘统计（景区数/订单数/用户数/成交额） |

### 小程序专属接口 `/api/tourism/miniapp/**`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/tourism/miniapp/home | 首页聚合数据（热门+推荐+分类） |
| GET | /api/tourism/miniapp/stats | 用户统计数据 |
| GET | /api/tourism/miniapp/recommend | 小程序专属推荐算法 |

### 文件服务接口 `/api/file/**`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/file/upload | 文件上传（类型校验：图片/视频/文档） |
| GET | /api/file/download/{fileName} | 文件下载 |
| DELETE | /api/file/{fileName} | 文件删除 |

### AI 服务接口 `/api/ai/**`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/ai/chat | AI 对话（body: {message, sessionId?}，支持streaming） |
| GET | /api/ai/chat/history | 获取对话历史（按sessionId查询） |
| GET | /api/ai/chat/sessions | 获取用户所有会话列表 |
| DELETE | /api/ai/chat/clear | 清除指定会话或全部对话历史 |
| DELETE | /api/ai/chat/session/{id} | 删除指定会话 |
| GET | /api/ai/recommend | AI 旅行推荐（景点/行程/攻略） |
| GET | /api/ai/health | 健康检查 |

---

## 核心业务流程

### 购票流程
```
用户浏览景区详情 → 点击"立即购票"(仅付费景区)
  → 弹窗: 选数量(1-10)/游览日期/填联系人(姓名+电话)
  → [立即支付] → 订单状态=paid → 订单完成
  → [稍后支付] → 订单状态=pending → 可在"我的订单"中支付或取消
  → 订单号格式: MKT + yyyyMMddHHmmss + 4位随机码
  → 取消: 软删除(deleted=1)，可在"已取消"Tab 查看
```

### 收藏流程
```
景区详情/卡片 → 点击♡
  → 未登录 → 跳转登录页
  → 已登录 → 检查 UNIQUE(user_id, attraction_id, deleted)
  → 未收藏: INSERT (deleted=0) → ♡变♥（已收藏）
  → 曾取消: UPDATE deleted=0 (恢复收藏) → ♡变♥
  → 已收藏: 再次点击 → 软删除 → ♥变♡
```

### AI 对话流程
```
用户输入问题 → POST /api/ai/chat {message, sessionId?}
  → Gateway JWT 鉴权 → X-User-Id 传递
  → AI 服务查询/创建 MongoDB chat_session 文档
  → 构建消息上下文:
     System: "你是一个专业的旅行助手，帮助用户规划旅行、推荐景点..."
     History: 最近N轮对话记录
     Current: 用户当前问题
  → 调用 DeepSeek API (model: deepseek-v4-pro, streaming: true)
  → 流式返回 / 一次性返回 AI 回复（Markdown 格式）
  → 保存 user + assistant 消息到 MongoDB
  → 前端 marked 渲染 Markdown → 气泡展示
  → XXL-JOB 定期清理超过30天的会话记录
```

---

## 界面特色

### Web 门户
- **毛玻璃效果**: 卡片采用 `backdrop-filter: blur()` 模糊背景
- **渐变色彩**: 橙色(#FF6B35) → 金色(#FFB563) 品牌主色调
- **流畅动画**: 页面切换过渡(fade/slide)、卡片悬浮效果、平滑滚动
- **响应式布局**: 桌面三列 / 平板两列 / 手机单列自适应
- **轮播展示**: 首页 Hero 大图轮播 + Swiper 热门景区轮播
- **地图导航**: Leaflet 集成，景区位置可视化
- **沉浸模式**: 详情/地图/AI 页面全屏沉浸，隐藏Header/Footer
- **浮动AI**: 全站浮动AI气泡，点击展开侧边面板(420px)，支持会话历史/快捷提问/景区上下文
- **自定义通知**: `maikaitui:notify` DOM事件系统解耦组件通信

### 管理后台
- Element Plus 企业级 UI 组件
- 暗色侧边栏 + 亮色内容区经典布局
- ECharts 数据可视化（仪表盘统计图表）
- 表单验证 + 加载骨架屏 + 空状态全覆盖
- `<keep-alive>` 页面缓存 + fade-transform 过渡动画

### 微信小程序
- 橙色品牌 + 绿色TabBar(#1f8f3a) 双色调
- 底部5Tab导航（首页/景区/AI/行程/我的）
- 卡片式景区展示，圆角阴影风格
- 下拉刷新 + 上拉加载更多
- 游客模式：无需登录即可浏览
- 本地静态图标资源（分类图标/默认图片）

---

## 核心功能亮点

### 🎯 区域推荐引擎
浏览景区详情时，自动推荐该地区其他热门景区。算法：同地区按评分降序 → 不足时补充同分类跨地区推荐。

### 🔥 热门景区排行
`viewCount`（浏览量）+ `isHot`（人工标记）综合排序。首页展示 Top 5 热门景区。

### 🏷️ 多角色 RBAC 权限
五级角色（super_admin/admin/merchant/tour_guide/user）+ 菜单粒度权限控制。管理员可精确控制每个角色能访问的页面和操作。

### 🤖 AI 智能旅行助手
基于 DeepSeek v4-pro 大语言模型（Spring AI 2.0.0-M6 集成），支持旅行规划、景点推荐、行程建议。MongoDB 持久化会话上下文记忆，XXL-JOB 定期清理过期对话，支持 Streaming 流式响应。

### 🕐 软删除 + 定时清理
关键业务数据采用逻辑删除（deleted 字段），XXL-JOB 定时任务自动清理超过30天的已删除数据和 AI 对话历史。

### 🕷️ 多源景区数据爬虫
Python + Playwright 实现的6源聚合爬虫（穷游/马蜂窝/携程/猫途鹰/百度百科/中旅），内置反检测机制，自动去重，输出 JSON + CSV。

### 💾 离线回退机制
Web 和 Miniapp 前端均内置离线回退数据：当API不可用时使用本地预置的景区和分类数据，保证基本功能可用。

---

## 预置种子数据

系统初始化后包含：
- **管理员账号**: admin / 123456
- **5 个角色**: super_admin, admin, merchant, tour_guide, user
- **14 个菜单**: 系统管理(6) + 旅游管理(6) + AI 控制台(2)
- **8 个地区**: 北京/上海/浙江(杭州/西湖)/四川(成都)/云南(大理)/海南(三亚)
- **7 个分类**: 自然风光/历史古迹/主题乐园/博物馆/美食街区/海滩度假/登山徒步
- **8 个景点**: 西湖/故宫/大熊猫基地/大理古城/三亚亚龙湾/上海外滩/长城/灵隐寺

---

## 开发说明

### 配置管理
所有服务支持两种配置方式：
1. **本地配置**: `application.yml`（开发环境默认）
2. **Nacos 配置中心**: `nacos-configs/` 目录下的 yaml 文件（生产环境推荐）

### 添加新微服务
1. 在 `maikaitui-backend` 下创建新模块（参考 `maikaitui-ai` 结构）
2. 在父 `pom.xml` `<modules>` 中添加
3. 创建 `application.yml` 配置数据源、Redis 等
4. 在 Gateway 的 `application.yml` / Nacos 配置中添加路由规则
5. 在 `maikaitui-common` 同级创建服务代码

### 爬虫工具使用
```bash
cd scraper_attractions
pip install -r requirements.txt
playwright install chromium

# 抓取 50 条景区数据（6源聚合）
python main.py -m 50

# 按关键词搜索
python main.py -k 北京 -m 20

# 指定来源
python main.py -s baike,qyer,mafengwo

# 调试模式（显示浏览器）
python main.py -m 10 --no-headless
```
输出: `output/spots_*.json` + `output/spots_*.csv`

---

## 项目文档

| 文档 | 说明 |
|------|------|
| [1.0 系统需求分析报告](doc/1.0系统需求分析报告_完整版.docx) | 8章：引言/项目概述/总体功能/业务需求/功能需求/优先级/非功能需求/其他事项 |
| [2.0 系统设计说明书](doc/2.0系统设计说明书.docx) | 架构设计/数据库设计/接口设计/部署方案 |
| [3.0 代码评审报告](doc/3.0代码评审.docx) | 8项评审（架构/规范/数据库/接口/异常/安全/前端/定时任务） |
| [4.0 测试计划](doc/4.0测试计划.docx) | 测试策略/资源/进度/用例概述/风险应对 |
| [4.1 项目测试报告](doc/4.1项目测试报告.docx) | 116 项测试，通过率 95.7%，12 个缺陷已修复 10 个 |
| [5.0 用户操作手册](doc/5.0用户操作手册.docx) | 10章：注册登录/浏览/收藏/购票/订单/AI/个人中心/常见问题 |
| [周报](doc/周报.docx) | 项目进展周报 |

---

## License

MIT License

---

**迈开腿** —— 探索世界，迈开腿！让每一次旅行都成为美好的回忆 🏃‍♂️🌍
