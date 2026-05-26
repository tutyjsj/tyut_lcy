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
