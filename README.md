# 环境网格化综合管理系统 — 项目概述与技术方案

> 太原理工大学 课程设计/毕业设计项目（TYUT）

---

## 一、项目概述

### 1.1 项目名称
环境网格化综合管理系统

### 1.2 项目定位
本系统作为XX三级统筹项目建设的重要中枢，通过先进技术提供智能决策，实现问题快速响应，建立环境全面监管。以更智慧、更精准、更便捷、更科学地成为企业的督导、政府的管家，助力政府部门实现科学化、精细化的环境监管。

### 1.3 核心理念
- **以监管问题为导向**：实现从问题发现、分级、甄别、调度、处置直至关闭的全生命周期管理
- **以污染源为靶向**：形成联动基本信息、许可证、物联监管、综合业务等全方位数据整合
- **"人防"+"技防"**：利用物联网、人工智能、大数据等技术提供"智慧天眼"
- **"横向到边、纵向到底"**：汇集各方数据，形成科学的问题甄别和智能的问题调度

---

## 二、技术架构

### 2.1 技术选型总览

| 层面 | 技术选型 | 版本 |
|------|---------|------|
| **后端框架** | Spring Boot | 4.0.6 |
| **Java** | JDK | 21 |
| **构建工具** | Maven | - |
| **ORM** | MyBatis-Plus | 3.5+ |
| **数据库** | MySQL | 8.0 |
| **缓存** | Redis | 7.x |
| **Web 前端** | Vue 3 + Element Plus | Vue 3.4+ |
| **小程序** | 微信原生开发 | - |
| **GIS 地图** | 高德地图 JS API + 高德小程序 SDK | 2.0 |
| **实时推送** | WebSocket（Spring WebSocket） | - |
| **文件存储** | 本地文件系统 + MinIO | - |
| **接口文档** | Knife4j（Swagger 增强） | - |

### 2.2 系统架构图

```
┌─────────────────────────────────────────────────────────┐
│                    前端层（展示层）                        │
│  ┌─────────────────┐      ┌───────────────────┐          │
│  │ Vue 3 管理后台   │      │  微信小程序         │          │
│  │ (Element Plus)   │      │  (微信原生)         │          │
│  └────────┬────────┘      └────────┬──────────┘          │
│           │     HTTP/HTTPS         │                      │
├───────────┼───────────────────────┼──────────────────────┤
│           │        网关层          │                      │
│           │   Nginx / API Gateway  │                      │
├───────────┼───────────────────────┼──────────────────────┤
│           │      后端服务层        │                      │
│  ┌────────┴───────────────────────┴────────┐            │
│  │         Spring Boot 4.0.6               │            │
│  │  ┌──────────┬──────────┬──────────┐      │            │
│  │  │ 用户管理  │ 网格管理  │ 问题管理  │      │            │
│  │  ├──────────┼──────────┼──────────┤      │            │
│  │  │ 任务调度  │ 台账管理  │ 考评管理  │      │            │
│  │  ├──────────┼──────────┼──────────┤      │            │
│  │  │ 地图GIS  │ 配置管理  │ 通讯录   │      │            │
│  │  └──────────┴──────────┴──────────┘      │            │
│  │         MyBatis-Plus / WebSocket         │            │
│  └───────────────┬─────────────────────────┘            │
│                  │                                       │
├──────────────────┼──────────────────────────────────────┤
│                  │         数据层                        │
│  ┌───────────────┼───────────────────────────┐          │
│  │    MySQL 8.0  │  Redis 7.x  │  MinIO/文件  │          │
│  └───────────────┴─────────────┴─────────────┘          │
└─────────────────────────────────────────────────────────┘
```

---

## 三、开发环境

### 3.1 开发工具
| 工具 | 说明 |
|------|------|
| **IDE** | IntelliJ IDEA / VS Code |
| **数据库管理** | Navicat / DBeaver |
| **接口测试** | Postman / Apifox |
| **微信开发者工具** | 小程序开发与调试 |
| **Git** | 版本控制 |

### 3.2 需要注册的第三方服务
1. **高德开发者账号**：获取 Web JS API Key 和小程序 SDK Key
2. **微信小程序 AppID**：微信公众平台注册

### 3.3 项目结构规划

```
tuyt/
├── pom.xml
├── docs/                          # 项目文档
│   ├── 01-项目概述与技术方案.md
│   ├── 02-功能需求文档.md
│   ├── 03-数据库设计.md
│   └── 04-API接口文档.md
├── src/main/java/com/ruoyi/tuyt/
│   ├── TuytApplication.java
│   ├── common/                    # 公共类
│   │   ├── config/                # 配置类
│   │   ├── exception/             # 异常处理
│   │   ├── result/                # 统一返回
│   │   └── utils/                 # 工具类
│   ├── modules/                   # 业务模块
│   │   ├── system/                # 系统管理（用户、角色、权限）
│   │   ├── grid/                  # 网格管理
│   │   ├── pollution/             # 污染源档案
│   │   ├── problem/               # 环境问题管理
│   │   ├── dispatch/              # 综合指挥调度
│   │   ├── task/                  # 任务管理
│   │   ├── assessment/            # 考评管理
│   │   ├── map/                   # GIS地图
│   │   ├── contact/               # 通讯录
│   │   └── config/                # 配置管理
│   └── websocket/                 # WebSocket
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   └── mapper/                    # MyBatis XML
├── tuyt-web/                      # Vue 3 前端项目（独立）
└── tuyt-miniapp/                  # 微信小程序（独立）
```

---

## 四、开发计划

### 第一阶段：基础架构搭建（预计 3-5 天）
- [ ] 完善 Spring Boot 项目配置，引入 MyBatis-Plus、Redis、WebSocket
- [ ] 搭建 Vue 3 前端项目框架（Element Plus + 路由 + 布局）
- [ ] 搭建微信小程序项目框架
- [ ] 设计数据库并生成表结构
- [ ] 完成用户认证模块（登录、权限、JWT）

### 第二阶段：网格与档案管理（预计 5-7 天）
- [ ] 网格管理（新建、维护、删除、查询）
- [ ] 网格地图（高德地图集成、区域展示）
- [ ] 污染源档案管理（一企一档）

### 第三阶段：环境问题管理（预计 7-10 天）
- [ ] 问题来源整合
- [ ] 问题分类分级
- [ ] 问题汇总与地图展示
- [ ] 问题统计与网格排名
- [ ] 问题台账

### 第四阶段：指挥调度（预计 7-10 天）
- [ ] 问题甄别（相似问题合并、升降级、关闭）
- [ ] 任务调度（手动派发、自动派发）
- [ ] 任务处理、督办、催办、退回、撤销
- [ ] 任务台账与跟踪
- [ ] 任务报表

### 第五阶段：考评与辅助功能（预计 5-7 天）
- [ ] 考评规则/指标/项管理
- [ ] 网格绩效考评
- [ ] 检查项与检查模板配置
- [ ] 通讯录管理

### 第六阶段：小程序开发（预计 7-10 天）
- [ ] 现场执法人员任务处理
- [ ] 电子标签扫描签到
- [ ] 区域巡查轨迹记录
- [ ] 问题上报与处理
- [ ] 我的工作（待办/运转/完结件）

### 第七阶段：联调与完善（预计 3-5 天）
- [ ] Web 端与后端联调
- [ ] 小程序与后端联调
- [ ] 实时推送功能测试
- [ ] Bug 修复与优化
# 环境网格化综合管理系统 — 功能需求文档

> 太原理工大学 课程设计/毕业设计项目（TYUT）

---

## 一、我的工作

"我的工作"指该账号用户所配置权限范围内的所有任务。

### 1.1 我的待办件
- 包含该账号用户所有待办任务
- 可通过条件查询对待办件进行筛选
- 支持任务处理、转交等操作

### 1.2 我的运转件
- 包含已转交（未处理）/退回（未审核）的任务
- 可通过任务编号、任务标题进行查询
- 查看任务处理信息和流转信息
- 系统需更新任务动态

### 1.3 我的完结件
- 包含该账号用户所有已完结任务
- 可通过任务编号、任务标题进行查询
- 查看任务处理信息和流转信息

---

## 二、网格划分及管理

按照网格化管理模式对辖区环保监管对象进行分级管理。三级网格：市级 → 区县级 → 乡镇/街道。

### 2.1 新建网格
- 录入：网格名称、网格级别、上级网格、责任单位、分管领导、网格责任人、网格责任人电话
- 添加污染源企业清单（支持批量添加某一区域、某一监管类型的企业）
- **新增监管企业**：弹出污染源列表，支持批量勾选，可通过企业名称、监管类型、企业类型筛选
- **监管企业列表**：显示企业名称、污染源地址、监管类型、企业类型、巡查人员、操作（删除）
- 支持批量删除监管企业、为监管企业设置巡查人员

### 2.2 网格维护
- 修改网格基本信息（名称、级别、上级网格等）
- 添加或删除污染源企业
- 维护企业巡查人员

### 2.3 删除网格
- 选择网格后删除，删除前需确认

### 2.4 网格地图
- 同步显示当前选择网格区域信息与企业信息
- 左侧树状图选择网格，右侧地图展示网格范围
- 上级网格展示整个区域及所有下级网格+企业点位
- 下级网格展示单个网格范围和企业点位
- 支持镇街区域划分（置灰可选中并弹出新增网格页面）
- 划分好的网格彩色展示，标注网格名称
- 点击网格可下钻到子网格地图
- 工业园区默认建成网格
- 支持多选镇街进行网格新增

### 2.5 网格查询
- 根据企业名称查询其所在网格

---

## 三、档案管理

### 3.1 污染源档案
实现对所有污染源对象的管理：
- **固定源**：工业企业等固定排放源
- **面源**：农业面源、生活面源等
- **移动源**：机动车、船舶等移动排放源

### 3.2 核心功能
- 不同污染源对象具有不同的数据档案
- 根据数据流转流程建立动态更新机制
- 开放数据服务，提高数据共享和业务协同
- 多维度关联分析
- 全面建档，动态管理

---

## 四、环境问题管理

### 4.1 问题来源整合

**数据来源渠道：**

| 来源类别 | 具体来源 | 展示方式 |
|---------|---------|---------|
| 公众投诉信访 | 废水、废气、噪声、固危废、放辐射、其他 | 环形图 |
| 现场监察 | 日常巡查、发现、现场踏勘 | 环形图 |
| 在线监测 | 在线监测报警、实验室监测、视频报警、工况报警、设备故障 | 环形图 |
| 横纵向交办 | 其他委办局交办 | - |
| 自定义添加 | 手动添加问题 | - |

**数据对接必要字段：**
1. 问题等级（I级、II级、III级）
2. 报警时间（精确到时分秒）
3. 问题来源（数据字典）
4. 问题类型（数据字典）
5. 污染类型（数据字典）
6. 问题描述
7. 事发地点
8. 事发企业（可为空）

系统提供标准对接接口，供其他系统进行数据调用。

### 4.2 问题分类分级

**污染类型分类：**
- 废水污染
- 废气污染
- 噪声污染
- 固危废污染
- 放辐射污染
- 其他

**问题严重程度分级：**

| 级别 | 名称 | 标识颜色 |
|------|------|---------|
| I级 | 严重 | 🔴 红色 |
| II级 | 较严重 | 🟡 黄色 |
| III级 | 一般 | 🔵 蓝色 |

### 4.3 问题汇总（地图展示）
- 地图等级 9-12 级时显示各镇街问题总数
- 放大显示逐级区划问题总数
- 地图等级 ≥14 级时显示问题具体分布点位
- 点击点位展示：事发企业、事发地点、问题描述、报警时间、问题来源
- 同一点位多个问题以列表形式展示
- 事发企业可跳转至一企一档
- 如企业有视频监控/在线监测/工况监控，显示对应按钮
- 最近发生的问题点位突出显示

### 4.4 问题统计

**统计维度切换按钮：**区域分布、污染类型、问题来源（默认选中区域分布）

**问题总数展示：**
- 今日/本月可切换（默认今日）
- 圆形卡片展示，支持动画效果（圆形边框旋转）
- 实时更新数据（不刷新页面）
- 新问题产生时有动画效果

**污染类型统计：**
- 切换时圆形波扩散动态效果
- 展示各污染类型名称、数量、百分比
- 类型：废水、废气、噪声、固危废、放辐射、其他

**问题来源统计：**
- 切换时圆形波扩散动态效果
- 展示各来源名称、数量、百分比
- 来源：公众投诉、现场监察、智能分析、自动监督

### 4.5 网格排名
- 默认展示本月所有网格排名情况
- 按问题总量由高至低排名（本月/今日可切换）
- 问题总数相同时按待处理数排名
- 右侧面板展示，每页默认3个
- **查询**：支持按网格名称、网格负责人模糊查询
- **排序**：支持前五名、最后五名快速排序
- **信息展示**：排名（图标）、网格名称、网格负责人、下级网格、企业数、问题总数、待处理问题数、问题占比环形图
- **下钻**：点击下级网格数量可进入下一级排名列表
- 点击问题数量可跳转至该网格问题列表

---

## 五、综合指挥调度

### 5.1 问题甄别

**基本信息展现：**
- 问题来源（小类）、问题源信息、报警时间、问题等级
- 事发区域、事发地点、污染类型
- 问题详情（默认最多两行，超出省略号，悬停显示完整）
- 事发企业（可点击进入一企一档）
- 环境信用等级（企业信用评价）

**信息可修改字段：**事发地点（支持重新标点）、污染类型、事发区域、问题详情、问题等级

**相似问题合并：**
- 默认展示同一污染源、同一来源的问题（除待处理问题外），按事发时间倒序
- 可对同一污染源/来源/投诉人/时间/地点进行筛选过滤
- 确认相似后可合并，避免任务重复派发

**问题关闭：**
- 可单个或批量关闭
- 填写关闭原因（必填）
- 只能关闭状态为：待处理、已处理、处理完成的问题

**问题升降级：**
- 支持在"严重、较严重、一般"之间升降级

### 5.2 任务调度

#### 手动派发
- 填写任务派发信息：任务类型、处理单位、处理人员、处理期限等
- 派发后推送至处理人员移动端
- 提示："任务提交后，将立即派发给处理人员，请仔细填写！"

#### 自动派发
- 提前配置各类任务对应的任务类型、处理单位、所属网格、处理人员、处理期限
- 问题产生时根据配置自动派发

#### 任务时间管控
- 派发任务包含处理期限字段
- 执行人员需在期限内处理并反馈

#### 任务退回
- 处理退回任务列表
- 查看退回原因、退回人、退回时间、建议处理人员
- 填写审核信息后进行审核
- 退回可选择建议执行人、写明退回理由

#### 任务派发（巡查计划）
- **日常巡查**：定期自动派发，配置检查周期、检查模板、监管企业清单
- **停产巡查**：针对停产企业，与原日常巡查机制一致
- 企业状态变更时自动切换巡查类型
- 支持新增、删除、查询巡查计划

#### 任务处理
- 填写处理意见：日期（默认当前）、现场检查结论、现场处置建议
- 整改情况：整改完成/限期整改（须填写整改时间）
- 生产经营情况：关闭/停产/正常生产
- 添加协作人：选择同行人员、填写是否到场、执法文号
- 上传附件：大门照片（必填）、排口照片、治理设施照片、风险单元照片、取证音频、取证视频、其他附件
- 支持进入行政处罚立案流程

#### 任务督办与催办
- **督办**：重要任务、超期任务 → 填写督办消息
- **催办**：临期、超期任务 → 填写催办消息

#### 任务撤销
- 勾选任务 → 查看详情 → 填写撤销原因 → 撤销

#### 任务导出
- 导出字段：任务单号、任务标题、任务类型、紧急程度、开始/结束时间、派发时间、发起人、处理单位、任务状态、完成时间、检查模板、任务内容、抄送人、企业地址、任务执行人

**任务查询条件：**

| 字段 | 类型 | 说明 |
|------|------|------|
| 任务单号 | 输入框 | 模糊查询 |
| 任务标题 | 输入框 | - |
| 任务类型 | 下拉框 | 数据字典 |
| 派发时间 | 时间控件 | 范围选择 |
| 处理单位 | 下拉框 | 网格配置信息 |
| 处理人员 | 下拉框 | 显示待办任务数量 |
| 紧急程度 | 下拉框 | 一般/紧急/特急 |
| 截止时间 | 时间控件 | 范围选择 |
| 企业名称 | 输入框 | - |
| 任务状态 | 下拉框 | 已拟定/已派发/已签收/已完成/已撤销/已退回 |
| 超期类型 | 单选 | 超期任务/即将超期任务 |

#### 任务跟踪
- 任务接收、处理状态跟踪管理
- 临期、超期任务自动提醒
- 现场执法记录、监测报告关联查询与归档
- 支持根据处理结果对环境问题升降级或关闭

---

## 六、业务数据管理

### 6.1 台账管理

#### 问题台账
- 条件查询所有问题
- **问题编辑**：修改事发企业、事发地点（必填）、污染类型（必填）、问题详情（必填）、事发区域（必填），修改后记录日志
- **问题关闭**：单个/批量关闭，填写关闭原因
- **问题导出**：Excel 形式下载
- **问题列表**：事发企业、报警时间、问题详情、问题来源、问题等级、处理状态、处罚状态、操作
- **问题动态**：动态流形式展示，包括预警、修改、派发、处理关闭、合并等操作记录

#### 任务台账
- 包含所有任务的列表管理
- 任务状态对应可执行操作：

| 状态 | 可执行操作 |
|------|-----------|
| 已拟定 | 修改、删除、发布 |
| 已派发 | 督办、催办、撤销 |
| 已签收 | 督办、催办、撤销 |
| 已完成 | 查看 |
| 已撤销 | 查看 |
| 已退回 | 查看 |

- **查询**：多条件筛选
- **新增**：填写后暂存或发布
- **修改**：已拟定状态可修改
- **删除**：已拟定状态可删除
- **督办/催办/撤销**：已派发、已签收状态
- **发布**：已拟定 → 已派发

### 6.2 报表管理
- 根据任务期限、派发时间、处理单位筛选
- 默认显示当月数据
- 统计维度：单位名称、任务总数、任务完成情况（待处理、已完成、完成率、超期数、督办数）、任务类型
- 支持下钻查看下级机构统计数据
- 点击数据查看对应任务列表
- 展现形式：饼状图、柱状图、折线图、GIS展示

---

## 七、考评管理

### 7.1 网格绩效考评
- 预置考评项、评分规则与全过程跟踪记录
- 支持月度、季度、年度考核打分
- 按考评时间、考评人条件筛选

### 7.2 考评结果查询
- 网格人员通过 PC 端、移动端查看自身考评结果

### 7.3 考评规则设置
- 内置考核指标计分规则
- 按规则名称、版本、指标类别筛选
- 支持添加、维护考核指标计分规则

### 7.4 考评指标管理
- 新增、修改、删除、查询考评指标
- 内容：评价指标名称、考评类型、是否有效、创建时间

### 7.5 考评项管理
- 灵活设置考评项
- 每个考评项可对应一个或多个考评指标
- 考评对象：网格、网格员（分别考评）
- 考评维度：问题发生后处理情况、响应率、处置及时率、处置效率等

---

## 八、电子地图

### 8.1 基础功能

#### 专题图层
- 污染源点位
- 周围敏感点
- 感知设备
- 人员位置
- 支持快速定位及周边专题图层查询

#### 查询分析

| 功能 | 说明 |
|------|------|
| 企业信息 | 地图上查看企业基本信息 |
| 一企一档 | 链接至企业档案 |
| 周边查询 | 查询周边企业 |
| 图层查看 | 不同图层勾选展示 |
| 关键字查询 | 高级搜索 |
| 缓冲区查询 | 指定数据范围查询 |
| 业务库关联 | GIS与业务数据关联 |
| 图层查询 | 按图层编号查询 |
| 坐标反解 | 坐标 → 地名 |
| 全景图访问 | Web端全景展示 |
| 地名库查询 | 地名数据查询 |
| 区划查询 | 区划地图服务 |

#### 地图标绘

| 工具 | 说明 |
|------|------|
| 漫游 | 移动地图 |
| 框选 | 区域放大/缩小 |
| 测距 | 距离测量 |
| 3D | 3D视图 |
| 影像 | 影像地图 |
| 放大/缩小 | 比例缩放 |
| 直线 | 绘制直线 |
| 折线 | 绘制折线 |
| 面积 | 面积测量 |
| 文字 | 添加文本 |
| 还原 | 还原视图 |
| 清除 | 清除标记 |
| 查询 | 条件查询企业 |
| 在附近找 | 附近敏感点/污染源 |

### 8.2 人员定位
- 网格员当前位置实时定位
- 方便进行人员调度

---

## 九、配置管理

### 9.1 考评模板配置
- 月度考评模板
- 半年考评模板
- 年度考评模板
- 考评时选择模板 → 填入考评项 → 自动计算评分

### 9.2 通讯录
- 记录相关人员电话信息（委办局相关人员）
- 表单字段：类型（单位/个人）、姓名、电话、组织机构、职位、照片、操作（拨号）
- 操作：新增、删除、修改、查询
- 支持登录坐席后通过拨号键呼出

### 9.3 检查项
- 新建、修改、删除检查项目
- 字段：检查项目名称、项目类型、监控点类型、录入方式、状态
- 添加检查选项清单
- 支持条件查询（项目名称、项目类型、录入方式、状态）

### 9.4 检查模板配置
- 为检查项配置选项内容
- 填写：选项名称、录入类型、是否正常（异常时需填写问题类别、处理期限单位、处理期限）、备注
- 支持添加子检查项
- 问题类别：该检查项产生的问题类别
- 子检查项可批量添加

### 9.5 网格管理（配置层面）
- 按行政区划分级：区政府 → 镇（街）和科技园管委会 → 村（社区） → 企业网格
- 管理网格基本信息及辖区污染源企业信息

---

## 十、功能清单汇总

| 模块 | 子功能 |
|------|--------|
| 我的工作 | 待办件、运转件、完结件 |
| 网格划分及管理 | 档案管理、新建网格、网格维护、删除网格、网格地图、网格查询 |
| 环境问题管理 | 问题来源整合、问题分类分级、问题汇总、问题统计、网格排名 |
| 综合指挥调度 | 问题甄别、任务调度（手动/自动）、任务派发、任务处理、任务追踪 |
| 业务数据管理 | 问题台账、任务台账、报表管理 |
| 考评管理 | 网格绩效考评、考评结果查询、考评规则/指标/项管理 |
| 电子地图 | 专题图层、查询分析、地图标绘、人员定位 |
| 配置管理 | 考评模板、通讯录、检查项、检查模板、网格管理配置 |
| 小程序 | 任务处理、电子标签签到、轨迹记录、问题上报、我的工作 |
# 环境网格化综合管理系统 — 数据库设计文档

> 太原理工大学 课程设计/毕业设计项目（TYUT）

---

## 一、数据库设计说明

- **数据库名**：`tuyt_env_grid`
- **字符集**：utf8mb4
- **排序规则**：utf8mb4_general_ci
- **ORM**：MyBatis-Plus
- **主键策略**：雪花算法（ASSIGN_ID）

---

## 二、核心表设计

### 2.1 系统管理

#### sys_user（用户表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| username | varchar(50) | 是 | 用户名 |
| password | varchar(255) | 是 | 密码（BCrypt加密） |
| real_name | varchar(50) | 是 | 真实姓名 |
| phone | varchar(20) | - | 手机号 |
| email | varchar(100) | - | 邮箱 |
| avatar | varchar(255) | - | 头像 |
| org_id | bigint | - | 所属组织机构ID |
| status | tinyint | 是 | 状态（0禁用 1正常） |
| create_time | datetime | 是 | 创建时间 |
| update_time | datetime | - | 更新时间 |
| deleted | tinyint | 是 | 逻辑删除（0未删除 1已删除） |

#### sys_role（角色表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| role_name | varchar(50) | 是 | 角色名称 |
| role_code | varchar(50) | 是 | 角色编码 |
| remark | varchar(255) | - | 备注 |
| create_time | datetime | 是 | 创建时间 |
| update_time | datetime | - | 更新时间 |

#### sys_user_role（用户-角色关联表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| user_id | bigint | 是 | 用户ID |
| role_id | bigint | 是 | 角色ID |

#### sys_organization（组织机构表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| org_name | varchar(100) | 是 | 机构名称 |
| parent_id | bigint | - | 上级机构ID |
| level | tinyint | 是 | 层级（1市级 2区县级 3乡镇街道） |
| sort | int | - | 排序 |
| create_time | datetime | 是 | 创建时间 |

---

### 2.2 网格管理

#### grid_info（网格信息表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| grid_name | varchar(100) | 是 | 网格名称 |
| grid_level | tinyint | 是 | 网格级别（1市级 2区县级 3乡镇街道） |
| parent_id | bigint | - | 上级网格ID |
| org_id | bigint | 是 | 责任单位ID |
| leader | varchar(50) | - | 分管领导 |
| responsible_person | varchar(50) | 是 | 网格责任人 |
| responsible_phone | varchar(20) | - | 责任人电话 |
| polygon_data | text | - | 网格区域坐标（GeoJSON） |
| status | tinyint | 是 | 状态（0禁用 1正常） |
| create_time | datetime | 是 | 创建时间 |
| update_time | datetime | - | 更新时间 |
| deleted | tinyint | 是 | 逻辑删除 |

#### grid_enterprise（网格-企业关联表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| grid_id | bigint | 是 | 网格ID |
| enterprise_id | bigint | 是 | 企业ID |
| inspector_id | bigint | - | 巡查人员ID |
| create_time | datetime | 是 | 创建时间 |

---

### 2.3 污染源档案

#### enterprise（企业/污染源表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| enterprise_code | varchar(50) | 是 | 企业编码 |
| enterprise_name | varchar(200) | 是 | 企业名称 |
| address | varchar(500) | - | 企业地址 |
| longitude | decimal(10,7) | - | 经度 |
| latitude | decimal(10,7) | - | 纬度 |
| pollution_type | varchar(50) | - | 污染类型 |
| supervise_type | varchar(50) | - | 监管类型（国控/省控/市控重点/区属重点/一般） |
| enterprise_type | varchar(50) | - | 企业类型 |
| legal_person | varchar(50) | - | 法定代表人 |
| legal_phone | varchar(20) | - | 法人电话 |
| credit_level | varchar(20) | - | 环境信用等级 |
| production_status | tinyint | - | 生产经营状态（1正常 2停产 3关闭） |
| source_type | varchar(20) | - | 源类型（fixed/area/mobile） |
| has_video | tinyint | - | 是否有视频监控（0否 1是） |
| has_monitor | tinyint | - | 是否有在线监测（0否 1是） |
| has_working | tinyint | - | 是否有工况监控（0否 1是） |
| status | tinyint | 是 | 状态（0禁用 1正常） |
| create_time | datetime | 是 | 创建时间 |
| update_time | datetime | - | 更新时间 |
| deleted | tinyint | 是 | 逻辑删除 |

---

### 2.4 环境问题管理

#### env_problem（环境问题表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| problem_no | varchar(50) | 是 | 问题编号 |
| problem_level | varchar(10) | 是 | 问题等级（I严重/II较严重/III一般） |
| alarm_time | datetime | 是 | 报警时间 |
| problem_source | varchar(50) | 是 | 问题来源 |
| problem_source_detail | varchar(100) | - | 具体来源小类 |
| problem_type | varchar(50) | 是 | 问题类型 |
| pollution_type | varchar(50) | 是 | 污染类型 |
| problem_desc | text | 是 | 问题描述 |
| address | varchar(500) | 是 | 事发地点 |
| longitude | decimal(10,7) | - | 经度 |
| latitude | decimal(10,7) | - | 纬度 |
| enterprise_id | bigint | - | 事发企业ID |
| area_code | varchar(20) | - | 事发区域编码 |
| area_name | varchar(200) | - | 事发区域名称 |
| merge_id | bigint | - | 合并目标问题ID |
| close_reason | varchar(500) | - | 关闭原因 |
| handle_status | varchar(20) | 是 | 处理状态（pending待处理/processed已处理/done处理完成/closed已关闭） |
| penalty_status | varchar(20) | - | 处罚状态 |
| create_user_id | bigint | 是 | 创建人ID |
| create_time | datetime | 是 | 创建时间 |
| update_time | datetime | - | 更新时间 |
| deleted | tinyint | 是 | 逻辑删除 |

#### env_problem_log（问题动态日志表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| problem_id | bigint | 是 | 问题ID |
| operation_type | varchar(30) | 是 | 操作类型（warn/edit/dispatch/process/close/merge/upgrade） |
| content | text | - | 操作内容 |
| operator_id | bigint | 是 | 操作人ID |
| create_time | datetime | 是 | 操作时间 |

---

### 2.5 任务管理

#### task_info（任务信息表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| task_no | varchar(50) | 是 | 任务单号 |
| task_title | varchar(200) | 是 | 任务标题 |
| task_type | varchar(30) | 是 | 任务类型（数据字典） |
| urgency | varchar(10) | 是 | 紧急程度（NORMAL一般/URGENT紧急/CRITICAL特急） |
| start_time | datetime | - | 开始时间 |
| deadline | datetime | - | 截止时间 |
| dispatch_time | datetime | - | 派发时间 |
| initiator_id | bigint | 是 | 发起人ID |
| handler_id | bigint | - | 处理人ID |
| handler_unit_id | bigint | - | 处理单位ID |
| grid_id | bigint | - | 所属网格ID |
| enterprise_id | bigint | - | 关联企业ID |
| problem_id | bigint | - | 关联问题ID |
| check_template_id | bigint | - | 检查模板ID |
| task_content | text | - | 任务内容 |
| cc_users | varchar(500) | - | 抄送人（JSON数组） |
| status | varchar(20) | 是 | 状态（DRAFT/DISPATCHED/SIGNED/DONE/REVOKED/RETURNED） |
| finish_time | datetime | - | 完成时间 |
| create_time | datetime | 是 | 创建时间 |
| update_time | datetime | - | 更新时间 |
| deleted | tinyint | 是 | 逻辑删除 |

#### task_process（任务处理记录表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| task_id | bigint | 是 | 任务ID |
| process_date | date | 是 | 处理日期 |
| conclusion | text | 是 | 现场检查结论 |
| suggestion | text | 是 | 现场处置建议 |
| rectification | varchar(20) | 是 | 整改情况（done/limited） |
| rectification_deadline | datetime | - | 限期整改时间 |
| production_status | varchar(20) | - | 生产经营情况（closed/stopped/normal） |
| is_signin | tinyint | - | 是否签到（0否 1是） |
| signin_time | datetime | - | 签到时间 |
| handler_id | bigint | 是 | 处理人ID |
| create_time | datetime | 是 | 处理时间 |

#### task_collaborator（任务协作人表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| process_id | bigint | 是 | 处理记录ID |
| user_id | bigint | 是 | 协作人ID |
| is_present | tinyint | - | 是否到场（0否 1是） |
| enforcement_no | varchar(50) | - | 执法文号 |

#### task_attachment（任务附件表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| task_id | bigint | 是 | 任务ID |
| process_id | bigint | - | 处理记录ID |
| file_type | varchar(30) | 是 | 附件类型（door/outlet/treatment/risk/audio/video/other） |
| file_name | varchar(255) | 是 | 文件名 |
| file_path | varchar(500) | 是 | 文件路径 |
| file_size | bigint | - | 文件大小 |
| create_time | datetime | 是 | 上传时间 |

#### task_transfer（任务流转记录表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| task_id | bigint | 是 | 任务ID |
| transfer_type | varchar(20) | 是 | 类型（dispatch/transfer/return/urge/supervise/revoke/audit） |
| from_user_id | bigint | - | 发起人ID |
| to_user_id | bigint | - | 目标人ID |
| reason | text | - | 原因/内容 |
| suggest_handler | varchar(100) | - | 建议处理人 |
| suggest_unit | varchar(100) | - | 建议处理单位 |
| audit_result | varchar(20) | - | 审核结果 |
| create_time | datetime | 是 | 操作时间 |

#### task_patrol_plan（巡查计划配置表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| plan_title | varchar(200) | 是 | 计划标题 |
| plan_type | varchar(20) | 是 | 配置类型（daily/stop） |
| check_template_id | bigint | - | 检查模板ID |
| check_cycle | int | 是 | 检查周期（天） |
| start_time | datetime | 是 | 启用时间 |
| last_exec_time | datetime | - | 最后执行时间 |
| next_exec_time | datetime | - | 下次执行时间 |
| status | tinyint | 是 | 状态（0停用 1启用） |
| content | text | - | 检查内容 |
| create_time | datetime | 是 | 创建时间 |
| update_time | datetime | - | 更新时间 |

---

### 2.6 考评管理

#### assess_template（考评模板表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| template_name | varchar(100) | 是 | 模板名称 |
| template_type | varchar(20) | 是 | 类型（monthly/semi_yearly/yearly） |
| status | tinyint | 是 | 状态（0无效 1有效） |
| create_time | datetime | 是 | 创建时间 |

#### assess_indicator（考评指标表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| indicator_name | varchar(100) | 是 | 指标名称 |
| assess_type | varchar(30) | 是 | 考评类型（grid/staff） |
| is_valid | tinyint | 是 | 是否有效（0否 1是） |
| create_time | datetime | 是 | 创建时间 |

#### assess_rule（考评规则表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| rule_name | varchar(100) | 是 | 规则名称 |
| rule_version | varchar(20) | - | 规则版本 |
| indicator_id | bigint | 是 | 关联指标ID |
| indicator_category | varchar(50) | - | 指标类别 |
| score_standard | text | 是 | 评分标准（JSON） |
| create_time | datetime | 是 | 创建时间 |

#### assess_result（考评结果表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| template_id | bigint | 是 | 模板ID |
| target_id | bigint | 是 | 被考评对象ID |
| target_type | varchar(20) | 是 | 对象类型（grid/staff） |
| assess_time | varchar(20) | 是 | 考评时间（如"2024-01"） |
| total_score | decimal(5,2) | 是 | 总分 |
| assessor_id | bigint | 是 | 考评人ID |
| create_time | datetime | 是 | 创建时间 |

#### assess_detail（考评明细表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| result_id | bigint | 是 | 考评结果ID |
| indicator_id | bigint | 是 | 指标ID |
| score | decimal(5,2) | 是 | 得分 |
| remark | varchar(500) | - | 备注 |

---

### 2.7 配置管理

#### check_item（检查项目表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| item_name | varchar(200) | 是 | 检查项目名称 |
| item_type | varchar(50) | - | 项目类型 |
| monitor_type | varchar(50) | - | 监控点类型 |
| input_type | varchar(30) | - | 录入方式 |
| status | tinyint | 是 | 状态（0无效 1有效） |
| create_time | datetime | 是 | 创建时间 |

#### check_template（检查模板表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| template_name | varchar(200) | 是 | 模板名称 |
| item_id | bigint | 是 | 检查项ID |
| option_name | varchar(200) | 是 | 选项名称 |
| input_type | varchar(30) | - | 录入类型 |
| is_normal | tinyint | 是 | 是否正常（0异常 1正常） |
| problem_category | varchar(50) | - | 问题类别（异常时填写） |
| process_days | int | - | 处理期限（天） |
| parent_id | bigint | - | 父级ID（子检查项） |
| remark | varchar(500) | - | 备注 |
| sort | int | - | 排序 |
| create_time | datetime | 是 | 创建时间 |

#### contact（通讯录表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| contact_type | varchar(10) | 是 | 类型（unit/person） |
| name | varchar(50) | 是 | 姓名/单位名称 |
| phone | varchar(20) | 是 | 电话 |
| org_id | bigint | - | 组织机构ID |
| org_name | varchar(100) | - | 组织机构名称 |
| position | varchar(100) | - | 职位 |
| photo | varchar(255) | - | 照片路径 |
| create_time | datetime | 是 | 创建时间 |

---

### 2.8 字典管理

#### sys_dict_type（字典类型表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| dict_name | varchar(100) | 是 | 字典名称 |
| dict_code | varchar(100) | 是 | 字典编码 |
| status | tinyint | 是 | 状态 |

#### sys_dict_data（字典数据表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | bigint | 是 | 主键 |
| dict_type_id | bigint | 是 | 字典类型ID |
| dict_label | varchar(100) | 是 | 字典标签 |
| dict_value | varchar(100) | 是 | 字典值 |
| sort | int | - | 排序 |
| status | tinyint | 是 | 状态 |

---

## 三、数据字典枚举

### 3.1 问题等级（problem_level）
| 值 | 名称 | 颜色 |
|----|------|------|
| I | 严重 | #FF0000 |
| II | 较严重 | #FFA500 |
| III | 一般 | #0066FF |

### 3.2 污染类型（pollution_type）
| 值 | 名称 |
|----|------|
| WASTE_WATER | 废水污染 |
| WASTE_GAS | 废气污染 |
| NOISE | 噪声污染 |
| SOLID_HAZARDOUS_WASTE | 固危废污染 |
| RADIATION | 放辐射污染 |
| OTHER | 其他 |

### 3.3 问题来源（problem_source）
| 值 | 名称 |
|----|------|
| PUBLIC_COMPLAINT | 公众投诉 |
| SITE_INSPECTION | 现场监察 |
| ONLINE_MONITOR | 智能分析 |
| CROSS_ASSIGN | 自动监督 |
| MANUAL | 手动添加 |

### 3.4 任务状态（task_status）
| 值 | 名称 |
|----|------|
| DRAFT | 已拟定 |
| DISPATCHED | 已派发 |
| SIGNED | 已签收 |
| DONE | 已完成 |
| REVOKED | 已撤销 |
| RETURNED | 已退回 |

### 3.5 监管类型（supervise_type）
| 值 | 名称 |
|----|------|
| NATIONAL | 国控重点 |
| PROVINCIAL | 省控重点 |
| CITY | 市控重点 |
| DISTRICT | 区属重点 |
| GENERAL | 一般 |

### 3.6 紧急程度（urgency）
| 值 | 名称 |
|----|------|
| NORMAL | 一般 |
| URGENT | 紧急 |
| CRITICAL | 特急 |

### 3.7 处理状态（handle_status）
| 值 | 名称 |
|----|------|
| PENDING | 待处理 |
| PROCESSED | 已处理 |
| DONE | 处理完成 |
| CLOSED | 已关闭 |

### 3.8 网格级别（grid_level）
| 值 | 名称 |
|----|------|
| 1 | 市级 |
| 2 | 区县级 |
| 3 | 乡镇/街道 |

### 3.9 污染源类型（source_type）
| 值 | 名称 |
|----|------|
| FIXED | 固定源 |
| AREA | 面源 |
| MOBILE | 移动源 |
# 环境网格化综合管理系统 — API 接口文档

> 太原理工大学 课程设计/毕业设计项目（TYUT）

---

## 一、接口规范

### 1.1 基础路径
```
开发环境：http://localhost:8080/api
生产环境：http://[域名]/api
```

### 1.2 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1716624000000
}
```

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录/Token过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 1.3 分页请求参数

```json
{
  "pageNum": 1,
  "pageSize": 10,
  // ... 其他查询条件
}
```

### 1.4 分页响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 10
  }
}
```

---

## 二、系统管理接口

### 2.1 用户登录

**POST** `/sys/login`

```json
// 请求
{
  "username": "admin",
  "password": "123456"
}

// 响应
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "realName": "管理员",
      "avatar": "",
      "roles": ["ADMIN"],
      "orgName": "环保局"
    }
  }
}
```

### 2.2 获取当前用户信息

**GET** `/sys/user/info`

```json
// 响应
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "管理员",
    "roles": ["调度员"],
    "permissions": ["problem:view", "problem:edit", "task:dispatch"],
    "orgId": 1,
    "orgName": "XX市环保局"
  }
}
```

### 2.3 用户管理

**分页查询** `GET` `/sys/user/page?pageNum=1&pageSize=10&realName=张三&status=1`

**新增** `POST` `/sys/user`

**修改** `PUT` `/sys/user`

**删除** `DELETE` `/sys/user/{id}`

**重置密码** `PUT` `/sys/user/{id}/reset-pwd`

### 2.4 角色管理

**GET** `/sys/role/list` - 角色列表

**POST** `/sys/role` - 新增角色

**PUT** `/sys/role` - 修改角色

**DELETE** `/sys/role/{id}` - 删除角色

### 2.5 组织机构管理

**GET** `/sys/org/tree` - 机构树

**POST** `/sys/org` - 新增机构

**PUT** `/sys/org` - 修改机构

**DELETE** `/sys/org/{id}` - 删除机构

### 2.6 字典管理

**GET** `/sys/dict/type/list` - 字典类型列表

**GET** `/sys/dict/data/{dictCode}` - 根据字典编码获取数据

---

## 三、网格管理接口

### 3.1 网格树

**GET** `/grid/tree`

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "gridName": "XX市",
      "gridLevel": 1,
      "children": [
        {
          "id": 2,
          "gridName": "XX区",
          "gridLevel": 2,
          "responsiblePerson": "张三",
          "children": [
            {
              "id": 3,
              "gridName": "XX街道",
              "gridLevel": 3,
              "responsiblePerson": "李四",
              "polygonData": "..."
            }
          ]
        }
      ]
    }
  ]
}
```

### 3.2 网格分页查询

**GET** `/grid/page?pageNum=1&pageSize=10&gridName=XX&gridLevel=2`

### 3.3 网格详情

**GET** `/grid/{id}`

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "gridName": "XX区",
    "gridLevel": 2,
    "parentId": 0,
    "parentName": "XX市",
    "orgId": 1,
    "orgName": "XX区环保局",
    "leader": "王五",
    "responsiblePerson": "张三",
    "responsiblePhone": "13800138000",
    "polygonData": "{...}",
    "enterpriseCount": 50,
    "enterpriseList": []
  }
}
```

### 3.4 新增网格

**POST** `/grid`

```json
{
  "gridName": "XX街道",
  "gridLevel": 3,
  "parentId": 2,
  "orgId": 1,
  "leader": "王五",
  "responsiblePerson": "张三",
  "responsiblePhone": "13800138000",
  "polygonData": "{...}"
}
```

### 3.5 修改网格

**PUT** `/grid`

### 3.6 删除网格

**DELETE** `/grid/{id}`

### 3.7 网格-企业关联

**GET** `/grid/{gridId}/enterprises` - 网格下企业列表

**POST** `/grid/{gridId}/enterprises` - 批量添加企业

```json
{
  "enterpriseIds": [1, 2, 3]
}
```

**DELETE** `/grid/{gridId}/enterprises` - 批量移除企业

**PUT** `/grid/{gridId}/enterprises/{enterpriseId}/inspector` - 设置巡查人员

```json
{
  "inspectorId": 10
}
```

### 3.8 根据企业查网格

**GET** `/grid/query-by-enterprise?enterpriseId=1`

---

## 四、污染源（企业）档案接口

### 4.1 企业分页查询

**GET** `/enterprise/page?pageNum=1&pageSize=10&enterpriseName=XX&superviseType=NATIONAL&enterpriseType=XX&gridId=1`

### 4.2 企业详情（一企一档）

**GET** `/enterprise/{id}`

### 4.3 新增/修改/删除企业

**POST** `/enterprise`

**PUT** `/enterprise`

**DELETE** `/enterprise/{id}`

### 4.4 企业列表（用于选择弹窗）

**GET** `/enterprise/list?enterpriseName=XX&superviseType=XX`

---

## 五、环境问题管理接口

### 5.1 问题分页查询

**GET** `/problem/page?pageNum=1&pageSize=10&problemNo=&problemLevel=&problemSource=&pollutionType=&handleStatus=&alarmTimeStart=2024-01-01&alarmTimeEnd=2024-12-31&enterpriseName=`

### 5.2 问题详情

**GET** `/problem/{id}`

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "problemNo": "P20240101001",
    "problemLevel": "I",
    "alarmTime": "2024-01-01 10:30:00",
    "problemSource": "PUBLIC_COMPLAINT",
    "problemSourceDetail": "12369热线",
    "pollutionType": "WASTE_WATER",
    "problemDesc": "XX企业排放废水超标",
    "address": "XX市XX区XX路XX号",
    "longitude": 112.1234567,
    "latitude": 37.1234567,
    "enterpriseId": 1,
    "enterpriseName": "XX化工有限公司",
    "areaName": "XX区",
    "handleStatus": "PENDING",
    "logs": []
  }
}
```

### 5.3 新增/修改问题

**POST** `/problem`

**PUT** `/problem`

```json
{
  "problemLevel": "I",
  "alarmTime": "2024-01-01 10:30:00",
  "problemSource": "PUBLIC_COMPLAINT",
  "pollutionType": "WASTE_WATER",
  "problemDesc": "问题描述",
  "address": "事发地点",
  "longitude": 112.1234567,
  "latitude": 37.1234567,
  "enterpriseId": 1,
  "areaCode": "140100",
  "areaName": "XX区"
}
```

### 5.4 问题操作

**PUT** `/problem/{id}/level` - 升降级

```json
{
  "problemLevel": "II"
}
```

**PUT** `/problem/{id}/close` - 关闭问题

```json
{
  "closeReason": "原因"
}
```

**POST** `/problem/{id}/merge` - 合并问题

```json
{
  "mergeTargetId": 2
}
```

### 5.5 问题动态日志

**GET** `/problem/{id}/logs`

### 5.6 问题统计

**GET** `/problem/statistics?period=today` (period: today/month)

```json
{
  "code": 200,
  "data": {
    "totalCount": 156,
    "regionDistribution": [
      {"areaName": "XX区", "count": 50}
    ],
    "pollutionTypeDistribution": [
      {"type": "WASTE_WATER", "typeName": "废水污染", "count": 40, "percent": 25.6}
    ],
    "sourceDistribution": [
      {"source": "PUBLIC_COMPLAINT", "sourceName": "公众投诉", "count": 60, "percent": 38.5}
    ]
  }
}
```

### 5.7 网格排名

**GET** `/problem/grid-ranking?period=month&pageNum=1&pageSize=3&keyword=网格名称&rankType=top5`

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "rank": 1,
        "gridId": 1,
        "gridName": "XX街道",
        "responsiblePerson": "张三",
        "childGridCount": 5,
        "enterpriseCount": 50,
        "totalProblemCount": 30,
        "pendingProblemCount": 10,
        "problemPercent": 60.5
      }
    ]
  }
}
```

### 5.8 地图问题汇总

**GET** `/problem/map-summary?zoomLevel=12&areaCode=140100`

```json
{
  "code": 200,
  "data": [
    {
      "areaCode": "140101",
      "areaName": "XX区",
      "totalCount": 25,
      "points": []    // zoom >= 14 时返回点位
    }
  ]
}
```

### 5.9 问题导出

**POST** `/problem/export`

---

## 六、对外数据对接接口

### 6.1 问题数据推送（标准接口）

**POST** `/api/external/problem/push`

> 此接口供其他系统调用，Header 需携带 `Authorization: Bearer {system-token}`

```json
{
  "problemLevel": "I",
  "alarmTime": "2024-01-01 10:30:00",
  "problemSource": "ONLINE_MONITOR",
  "problemType": "废水超标",
  "pollutionType": "WASTE_WATER",
  "problemDesc": "COD在线监测超标",
  "address": "XX市XX区XX路XX号",
  "enterpriseId": 1
}
```

---

## 七、任务调度接口

### 7.1 任务分页查询

**GET** `/task/page?pageNum=1&pageSize=10&taskNo=&taskTitle=&taskType=&dispatchTimeStart=&dispatchTimeEnd=&handlerUnitId=&handlerId=&urgency=&deadlineStart=&deadlineEnd=&enterpriseName=&status=&overdueType=`

### 7.2 任务详情

**GET** `/task/{id}`

### 7.3 新增/修改任务

**POST** `/task`

```json
{
  "taskTitle": "XX企业废水超标检查",
  "taskType": "emergency_check",
  "urgency": "URGENT",
  "deadline": "2024-01-05 18:00:00",
  "handlerId": 10,
  "handlerUnitId": 2,
  "gridId": 1,
  "enterpriseId": 1,
  "problemId": 1,
  "taskContent": "请立即前往检查",
  "ccUsers": "5,6"
}
```

**PUT** `/task`

### 7.4 任务操作

**PUT** `/task/{id}/dispatch` - 派发

**PUT** `/task/{id}/revoke` - 撤销

```json
{
  "reason": "撤销原因"
}
```

**PUT** `/task/{id}/urge` - 催办

```json
{
  "reason": "催办原因"
}
```

**PUT** `/task/{id}/supervise` - 督办

```json
{
  "reason": "督办原因"
}
```

**DELETE** `/task/{id}` - 删除（仅DRAFT状态）

### 7.5 任务退回审核

**GET** `/task/returned/page?pageNum=1&pageSize=10` - 退回任务列表

**PUT** `/task/{id}/audit-return` - 审核退回

```json
{
  "auditResult": "APPROVED",
  "newHandlerId": 15,
  "newHandlerUnitId": 3
}
```

### 7.6 任务处理

**POST** `/task/{id}/process`

```json
{
  "processDate": "2024-01-03",
  "conclusion": "现场检查发现废水处理设施运行异常",
  "suggestion": "责令限期整改",
  "rectification": "limited",
  "rectificationDeadline": "2024-01-20",
  "productionStatus": "normal",
  "isSignin": true,
  "signinTime": "2024-01-03 09:30:00",
  "collaborators": [
    {
      "userId": 11,
      "isPresent": true,
      "enforcementNo": "ZF20240101"
    }
  ],
  "attachments": [
    {
      "fileType": "door",
      "fileName": "大门照片.jpg",
      "filePath": "/uploads/2024/01/xxx.jpg"
    }
  ]
}
```

### 7.7 任务流转记录

**GET** `/task/{id}/transfers`

### 7.8 任务导出

**POST** `/task/export`

```json
{
  // 同查询参数
}
```

### 7.9 巡查计划管理

**分页查询** `GET` `/patrol-plan/page?pageNum=1&pageSize=10&planType=daily&status=1`

**新增** `POST` `/patrol-plan`

```json
{
  "planTitle": "XX区日常巡查计划",
  "planType": "daily",
  "checkTemplateId": 1,
  "checkCycle": 7,
  "startTime": "2024-01-01",
  "status": 1,
  "content": "巡查内容描述",
  "enterpriseIds": [1, 2, 3, 4]
}
```

**修改** `PUT` `/patrol-plan`

**删除** `DELETE` `/patrol-plan/{id}`

---

## 八、任务报表接口

**GET** `/task/report?dispatchTimeStart=2024-01-01&dispatchTimeEnd=2024-01-31&handlerUnitId=1`

```json
{
  "code": 200,
  "data": {
    "summary": {
      "totalTasks": 100,
      "doneTasks": 80,
      "completionRate": 80.0,
      "overdueTasks": 5,
      "supervisedTasks": 3
    },
    "categoryDistribution": [
      {"type": "emergency_check", "typeName": "应急检查", "count": 30}
    ],
    "orgDetails": [
      {
        "orgId": 1,
        "orgName": "XX区环保局",
        "totalTasks": 50,
        "pendingCount": 5,
        "doneCount": 40,
        "completionRate": 80.0,
        "overdueCount": 3,
        "supervisedCount": 2,
        "children": []
      }
    ]
  }
}
```

---

## 九、考评管理接口

### 9.1 考评模板

**GET** `/assess/template/list` - 模板列表

**POST** `/assess/template` - 新增

**PUT** `/assess/template` - 修改

**DELETE** `/assess/template/{id}` - 删除

### 9.2 考评指标

**GET** `/assess/indicator/page?pageNum=1&pageSize=10`

**POST** `/assess/indicator`

**PUT** `/assess/indicator`

**DELETE** `/assess/indicator/{id}`

### 9.3 考评规则

**GET** `/assess/rule/page?pageNum=1&pageSize=10&ruleName=&ruleVersion=&indicatorCategory=`

**POST** `/assess/rule`

**PUT** `/assess/rule`

**DELETE** `/assess/rule/{id}`

### 9.4 考评结果

**POST** `/assess/result` - 提交考评

```json
{
  "templateId": 1,
  "targetId": 5,
  "targetType": "staff",
  "assessTime": "2024-01",
  "details": [
    {
      "indicatorId": 1,
      "score": 90.0,
      "remark": "表现良好"
    }
  ]
}
```

**GET** `/assess/result/page?pageNum=1&pageSize=10&assessTime=2024-01&targetType=grid`

**GET** `/assess/result/my?assessTime=2024-01` - 查看自己的考评结果

---

## 十、配置管理接口

### 10.1 检查项

**GET** `/check/item/page?pageNum=1&pageSize=10&itemName=&itemType=&status=1`

**POST** `/check/item`

**PUT** `/check/item`

**DELETE** `/check/item/{id}`

### 10.2 检查模板配置

**GET** `/check/template/list?itemId=1` - 某检查项的模板选项

**POST** `/check/template`

```json
{
  "templateName": "废水检查模板",
  "itemId": 1,
  "optionName": "COD在线监测",
  "inputType": "NUMBER",
  "isNormal": 0,
  "problemCategory": "WASTE_WATER",
  "processDays": 3,
  "remark": "",
  "children": []
}
```

**PUT** `/check/template`

**DELETE** `/check/template/{id}`

### 10.3 通讯录

**GET** `/contact/page?pageNum=1&pageSize=10&name=&contactType=&orgId=`

**POST** `/contact`

**PUT** `/contact`

**DELETE** `/contact/{id}`

---

## 十一、"我的工作"接口

### 11.1 我的待办件

**GET** `/my-work/pending?pageNum=1&pageSize=10&taskNo=&taskTitle=`

### 11.2 我的运转件

**GET** `/my-work/processing?pageNum=1&pageSize=10&taskNo=&taskTitle=`

### 11.3 我的完结件

**GET** `/my-work/done?pageNum=1&pageSize=10&taskNo=&taskTitle=`

---

## 十二、实时推送接口（WebSocket）

### 12.1 连接地址

```
ws://localhost:8080/ws/notification?token={jwt_token}
```

### 12.2 推送消息类型

```json
{
  "type": "NEW_PROBLEM",
  "title": "新问题预警",
  "content": "XX企业发生废水超标报警",
  "data": {
    "problemId": 1,
    "problemLevel": "I"
  },
  "timestamp": 1716624000000
}
```

**消息类型：**

| type | 说明 |
|------|------|
| NEW_PROBLEM | 新问题预警 |
| TASK_DISPATCHED | 新任务派发 |
| TASK_URGED | 任务催办 |
| TASK_SUPERVISED | 任务督办 |
| TASK_RETURNED | 任务退回 |
| TASK_OVERDUE | 任务超期提醒 |
| TASK_NEAR_OVERDUE | 任务临期提醒 |

---

## 十三、文件上传接口

**POST** `/file/upload`

```
Content-Type: multipart/form-data
file: [二进制文件]
```

```json
{
  "code": 200,
  "data": {
    "fileName": "大门照片.jpg",
    "filePath": "/uploads/2024/01/xxx.jpg",
    "fileSize": 102400,
    "url": "http://localhost:8080/uploads/2024/01/xxx.jpg"
  }
}
```

**GET** `/file/download/{fileName}` - 文件下载
# 05-项目启动指南

> 环境网格化综合管理系统（TUYT）—— 从零开始运行项目

---

## 一、环境要求

| 环境 | 版本要求 | 说明 |
|------|---------|------|
| **JDK** | 21 | [下载地址](https://adoptium.net/zh-CN/download/) |
| **Maven** | 3.6+ | [下载地址](https://maven.apache.org/download.cgi) |
| **MySQL** | 8.0+ | 本地安装并运行 |
| **Redis** | 7.x | 本地安装并运行，无密码（默认 6379 端口） |
| **Node.js** | 18+ | [下载地址](https://nodejs.org/zh-cn/)（Vite 6 要求） |
| **npm** | — | 随 Node.js 自带 |

### 环境检查

```bash
# 检查各环境是否安装成功
java -version          # 应显示 21
mvn -v                 # 应显示 Maven 3.x 及 Java 21
node -v                # 应显示 v18 或更高
npm -v                 # 应显示 npm 版本
mysql --version        # 应显示 8.0.x
redis-cli ping         # 应返回 PONG
```

---

## 二、项目结构

```
tuyt/
├── tuyt-common/         公共模块（工具类、异常、响应封装）
├── tuyt-framework/      框架模块（JWT、拦截器、Redis、WebSocket 等配置）
├── tuyt-business/       业务模块（Entity、Mapper、Service）
├── tuyt-admin/          启动模块（入口类、Controller、配置文件）
├── tuyt-web/            前端项目（Vue 3 + Vite）
└── docs/                项目文档
    ├── 01-项目概述与技术方案.md
    ├── 02-功能需求文档.md
    ├── 03-数据库设计.md
    ├── 04-API接口文档.md
    ├── 05-项目启动指南.md     ← 本文档
    └── init_data.sql         初始化 SQL 脚本
```

**模块依赖链**：`tuyt-admin` → `tuyt-business` → `tuyt-framework` → `tuyt-common`

---

## 三、数据库初始化

### 3.1 确保 MySQL 服务已启动

### 3.2 执行 SQL 脚本

使用任意 MySQL 客户端（Navicat、DBeaver、命令行等）执行 `docs/init_data.sql`：

```bash
# 命令行方式（在项目根目录下）
mysql -u root -p < docs/init_data.sql
```

该脚本会完成：
- 创建数据库 `tuyt_env_grid`（字符集 utf8mb4）
- 创建 20 张业务表
- 插入初始数据：

**组织结构**：太原市生态环境局 → 6 个区环保局 → 20 个街道环保办

**预置角色**：

| 角色 | 账号 | 密码 | 说明 |
|------|------|------|------|
| 超级管理员 | `admin` | `123456` | 系统最高权限 |
| 网格长 | `grid_leader` | `123456` | 网格管理负责人 |
| 巡查员 | `inspector` | `123456` | 日常巡查人员 |
| 调度员 | `dispatcher` | `123456` | 任务调度分配 |
| 数据分析员 | `analyst` | `123456` | 数据统计分析 |

> 密码在数据库中以 MD5 加密存储。

---

## 四、后端启动

### 4.1 修改数据库/Redis 配置（如需要）

编辑 `tuyt-admin/src/main/resources/application.yml`：

```yaml
# 数据库配置 —— 修改为你本机的用户名和密码
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tuyt_env_grid?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root              # ← 修改为你的 MySQL 用户名
    password: lcy20051022       # ← 修改为你的 MySQL 密码

  # Redis 配置 —— 如有密码请添加
  data:
    redis:
      host: localhost
      port: 6379
      # password: your_password  # ← 如果 Redis 有密码，取消注释并修改
```

### 4.2 Maven 编译安装

在项目根目录 `tuyt/` 下执行（首次运行需要，后续如果只改代码无需每次执行）：

```bash
cd tuyt
mvn clean install -DskipTests
```

### 4.3 启动后端服务

#### 方式一：IDE 启动（推荐）

在 IntelliJ IDEA 中打开 `tuyt` 目录，找到 `tuyt-admin/src/main/java/com/ruoyi/tuyt/TuytApplication.java`，右键运行 `main` 方法。

#### 方式二：命令行启动

```bash
cd tuyt-admin
mvn spring-boot:run
```

### 4.4 验证后端启动

启动成功后控制台输出类似：

```
Started TuytApplication in X.XXX seconds
API Document: http://localhost:8080/doc.html
```

访问以下地址确认：
- **API 文档**：[http://localhost:8080/doc.html](http://localhost:8080/doc.html)
- **健康检查**：[http://localhost:8080/health](http://localhost:8080/health) → 返回 `{"code":200,"msg":"ok"}`

---

## 五、前端启动

### 5.1 安装依赖

```bash
cd tuyt-web
npm install
```

### 5.2 启动开发服务器

```bash
npm run dev
```

### 5.3 验证前端启动

启动成功后控制台输出类似：

```
VITE v6.x.x  ready in XXX ms
➜  Local:   http://localhost:5173/
```

浏览器访问 [http://localhost:5173](http://localhost:5173)，使用预置账号登录即可。

### 5.4 前后端通信说明

前端请求路径 `/api/**` 通过 Vite 代理转发到后端 `http://localhost:8080`，配置见 `tuyt-web/vite.config.js`：

```js
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    rewrite: (path) => path.replace(/^\/api/, '')
  }
}
```

> 即前端请求 `/api/login` → 实际请求 `http://localhost:8080/login`

---

## 六、快速启动清单

| 步骤 | 操作 | 命令 |
|------|------|------|
| 1 | 启动 MySQL | 确保 MySQL 服务运行 |
| 2 | 启动 Redis | 确保 Redis 服务运行（`redis-cli ping` 验证） |
| 3 | 初始化数据库 | `mysql -u root -p < docs/init_data.sql` |
| 4 | 编译后端 | `cd tuyt && mvn clean install -DskipTests` |
| 5 | 启动后端 | IDE 运行 `TuytApplication` 或 `mvn spring-boot:run` |
| 6 | 安装前端依赖 |  cd "E:\资料\学校\大三下\大课设\object\tuyt\tuyt-web"
                    npm install
                    npm run dev

# 1. 杀掉所有旧的 Node 进程
taskkill /F /IM node.exe

# 2. 重新启动前端
cd "E:\资料\学校\大三下\大课设\object\tuyt\tuyt-web"
npm run dev

# 3. 浏览器打开显示的地址（应该是 http://localhost:5173/）
# 然后 Ctrl+Shift+R 硬刷新


| 7 | 启动前端 | `cd tuyt-web && npm run dev` |
| 8 | 访问系统 | 浏览器打开 `http://localhost:5173` |
| 9 | 登录 | 账号 `admin`，密码 `123456` |

---

## 七、常用链接

| 说明 | 地址 |
|------|------|
| 前端页面 | [http://localhost:5173](http://localhost:5173) |
| 后端 API 文档 | [http://localhost:8080/doc.html](http://localhost:8080/doc.html) |
| 健康检查 | [http://localhost:8080/health](http://localhost:8080/health) |
| Swagger UI | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |

---

## 八、常见问题

### Q1：Maven 依赖下载慢

在 `pom.xml` 同级目录或 Maven `settings.xml` 中配置阿里云镜像：

```xml
<mirror>
  <id>aliyun</id>
  <mirrorOf>central</mirrorOf>
  <name>Aliyun Maven</name>
  <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```

### Q2：`npm install` 很慢或失败

配置淘宝镜像：

```bash
npm config set registry https://registry.npmmirror.com
npm install
```

### Q3：端口被占用

**后端 8080 端口**：修改 `application.yml` 中 `server.port` 为其他端口（如 8081），同时修改 `vite.config.js` 中的代理 `target`。

**前端 5173 端口**：Vite 会自动尝试下一个可用端口（5174、5175...），或手动修改 `vite.config.js` 中 `server.port`。

### Q4：数据库连接失败

1. 确认 MySQL 服务已启动
2. 确认 `application.yml` 中用户名密码正确
3. 确认数据库 `tuyt_env_grid` 已创建（执行了 `init_data.sql`）
4. 确认 MySQL 8.0 驱动兼容（项目已使用 `mysql-connector-j 8.3.0`）

### Q5：Redis 连接失败

1. 确认 Redis 服务已启动（`redis-cli ping` → `PONG`）
2. 如果 Redis 设置了密码，在 `application.yml` 中添加 `password` 配置
3. Windows 用户可使用 [Memurai](https://www.memurai.com/) 或 WSL 中运行 Redis

### Q6：登录成功后页面空白

打开浏览器开发者工具（F12）→ Console，查看是否有报错。常见原因：
- 后端未启动 → 检查前端代理是否正确
- CORS 跨域 → 确认后端已配置跨域（项目已内置）
- Token 获取失败 → 检查请求拦截器中 `localStorage` 的 key 是否为 `tuyt_token`

### Q7：前端页面样式异常

确认 Element Plus 按需导入配置正确，重新执行 `npm run dev`。

### Q8：WebSocket 连接失败

检查 `vite.config.js` 中的 WebSocket 代理配置是否正确：
```js
proxy: {
  '/ws': {
    target: 'ws://localhost:8080',
    ws: true
  }
}
```

### Q9：运行时遇到"找不到符号"或"程序包不存在"

在项目根目录重新执行 Maven 编译：

```bash
mvn clean install -DskipTests
```

---

## 九、技术栈速览

| 层面 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 21 |
| 框架 | Spring Boot | 3.3.6 |
| ORM | MyBatis-Plus | 3.5.7 |
| 数据库 | MySQL | 8.0+ |
| 缓存 | Redis | 7.x |
| 连接池 | Druid | 1.2.23 |
| API 文档 | Knife4j (OpenAPI 3) | 4.5.0 |
| 认证 | JWT (JJWT) | 0.12.5 |
| 工具 | Hutool / Fastjson2 | — |
| 前端框架 | Vue 3 | 3.5+ |
| 构建工具 | Vite | 6.0+ |
| UI 库 | Element Plus | 2.9+ |
| 图表 | ECharts | 5.5+ |
| 地图 | 高德地图 JSAPI | — |
