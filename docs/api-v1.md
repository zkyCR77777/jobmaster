# 求职高手 Smart Pact API 接口文档 v1

- 文档版本：`v1-draft`
- 文档状态：`草案 / 待后端实现对齐`
- 编写日期：`2026-03-07`
- 适用范围：`MVP 阶段 Android 客户端接入真实 API 前的接口契约定义`

## 1. 文档目的

本文档基于以下内容整理：

- 产品需求文档 `docs/smart-pact-system-prd.md`
- 当前 Android 原型中的页面结构与 mock 数据模型
- 当前 FastAPI 后端骨架路由与 SQLModel 数据模型

本文档的目标不是描述“当前已实现接口”，而是定义一版**后端与 Android 客户端后续共同遵循的目标接口契约**。

当前仓库现状：

- Android 客户端仍以本地 mock 驱动为主
- 后端已有 FastAPI 路由骨架，但大多为占位实现
- 因此需要先统一接口命名、资源边界、请求/响应结构，再开始替换客户端 mock

## 2. 设计原则

### 2.1 基础路径

- API 基础前缀：`/api/v1`
- 健康检查保留：`/health`

说明：当前后端骨架使用的是 `/api/jobs`、`/api/delivery`、`/api/company`、`/api/contract`、`/api/chat`。正式实现阶段建议统一迁移到带版本号的 `/api/v1/...`。

### 2.2 资源命名

- 集合资源统一使用复数：`/jobs`、`/resumes`、`/deliveries`、`/contracts`
- 对“分析任务”“生成任务”这类异步能力，使用任务型资源建模：
  - `POST /company-reports`
  - `POST /resumes/{resume_id}/customizations`
  - `POST /contracts`

### 2.3 数据格式

- 默认返回 `application/json`
- 文件上传使用 `multipart/form-data`
- AI 对话流式输出使用 `text/event-stream`
- 时间统一使用 `ISO 8601`，示例：`2026-03-07T10:30:00+08:00`
- 主键 `id` 默认使用 `UUID` 字符串

### 2.4 通用响应包裹

除 SSE 外，所有成功响应统一使用以下结构：

```json
{
  "code": 0,
  "message": "ok",
  "data": {},
  "request_id": "0d7c9df5-6d94-4f8b-9fd6-1fc92c7c1e31"
}
```

失败响应建议：

```json
{
  "code": 40001,
  "message": "参数校验失败",
  "data": null,
  "request_id": "0d7c9df5-6d94-4f8b-9fd6-1fc92c7c1e31"
}
```

### 2.5 分页结构

列表接口统一使用：

```json
{
  "items": [],
  "page": 1,
  "page_size": 20,
  "total": 120
}
```

### 2.6 异步任务结构

涉及解析、AI 分析、文件处理的接口统一返回任务状态：

```json
{
  "id": "uuid",
  "status": "processing",
  "progress": 65,
  "current_stage": "风险分析",
  "stages": [
    {"name": "文档解析", "status": "done"},
    {"name": "条款识别", "status": "done"},
    {"name": "风险分析", "status": "processing"},
    {"name": "生成报告", "status": "pending"}
  ]
}
```

## 3. 认证约定

### 3.1 认证方式

- 采用 `Bearer Token`
- 请求头：`Authorization: Bearer <access_token>`

### 3.2 MVP 建议

MVP 阶段可以采用“轻认证”策略：

- `register/login/refresh` 保留
- Android 首轮接入时允许使用固定测试账号
- 非核心阶段不优先做复杂权限模型

## 4. 枚举与字典约定

### 4.1 模块标识 `module`

- `eagle`：鹰眼猎手
- `phantom`：幻影投递官
- `investigator`：深网调查员
- `guardian`：契约卫士

### 4.2 投递状态 `delivery_status`

- `pending`：等待中
- `delivering`：投递中
- `delivered`：已投递
- `viewed`：已查看
- `written_test`：笔试
- `interview`：面试
- `offer`：已发 offer
- `rejected`：已拒绝

说明：该枚举同时兼容当前 Android UI 状态和 PRD 中的投递阶段。

### 4.3 企业风险等级 `company_risk_level`

- `low`
- `medium`
- `high`

### 4.4 合同风险等级 `contract_risk_level`

- `safe`
- `warning`
- `danger`

### 4.5 异步任务状态 `task_status`

- `pending`
- `processing`
- `done`
- `failed`

## 5. 首页与总览接口

首页不建议让客户端并行拼装多个零散接口，建议提供聚合接口。

### 5.1 获取首页总览

- 方法：`GET`
- 路径：`/api/v1/dashboard/home`
- 用途：驱动首页问候、通知数量、Hero 数据、模块卡片、智能体动态

响应示例：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "greeting": "下午好",
    "app_name": "求职高手",
    "notification_count": 3,
    "hero_stats": {
      "new_jobs_today": 24,
      "match_success_rate": 89,
      "interview_invites": 5
    },
    "modules": [
      {
        "module": "eagle",
        "title": "鹰眼猎手",
        "subtitle": "智能职位雷达",
        "description": "全天候扫描优质职位",
        "stats_text": "12 个新机会",
        "pending_count": 12
      }
    ],
    "agent_feed": [
      {
        "module": "phantom",
        "description": "智能投递一键直达",
        "stats_text": "8 份待投递"
      }
    ]
  },
  "request_id": "uuid"
}
```

## 6. 认证接口

### 6.1 用户注册

- 方法：`POST`
- 路径：`/api/v1/auth/register`

请求体：

```json
{
  "email": "test@example.com",
  "password": "12345678",
  "nickname": "小林"
}
```

### 6.2 用户登录

- 方法：`POST`
- 路径：`/api/v1/auth/login`

请求体：

```json
{
  "email": "test@example.com",
  "password": "12345678"
}
```

响应 `data`：

```json
{
  "access_token": "jwt-token",
  "token_type": "bearer",
  "expires_in": 3600,
  "user": {
    "id": "uuid",
    "email": "test@example.com",
    "nickname": "小林"
  }
}
```

### 6.3 刷新 Token

- 方法：`POST`
- 路径：`/api/v1/auth/refresh`

## 7. 鹰眼猎手：岗位接口

### 7.1 获取岗位列表

- 方法：`GET`
- 路径：`/api/v1/jobs`

查询参数：

- `keyword`：关键词
- `company`：公司名
- `location`：城市
- `tag`：技能标签，可重复
- `min_match`：最低匹配度
- `is_new`：是否仅新岗位
- `sort`：`match_desc | latest_desc | salary_desc`
- `page`
- `page_size`

响应 `data`：

```json
{
  "items": [
    {
      "id": "uuid",
      "title": "高级前端工程师",
      "company": "字节跳动",
      "location": "北京",
      "salary_range": "40-60K",
      "match_score": 95,
      "is_new": true,
      "published_at": "2026-03-07T10:20:00+08:00",
      "tags": ["React", "TypeScript"]
    }
  ],
  "page": 1,
  "page_size": 20,
  "total": 1
}
```

### 7.2 获取岗位详情

- 方法：`GET`
- 路径：`/api/v1/jobs/{job_id}`

响应 `data`：

```json
{
  "id": "uuid",
  "title": "高级前端工程师",
  "company": "字节跳动",
  "company_url": "https://jobs.bytedance.com",
  "location": "北京",
  "salary_range": "40-60K",
  "job_type": "校招",
  "description": "岗位职责内容",
  "requirements": "任职要求内容",
  "tags": ["React", "TypeScript"],
  "source_url": "https://jobs.bytedance.com/xxx",
  "published_at": "2026-03-07T10:20:00+08:00",
  "last_seen_at": "2026-03-07T10:30:00+08:00",
  "is_new": true
}
```

### 7.3 获取岗位匹配分析

- 方法：`POST`
- 路径：`/api/v1/jobs/{job_id}/match`

请求体：

```json
{
  "resume_id": "uuid"
}
```

响应 `data`：

```json
{
  "job_id": "uuid",
  "resume_id": "uuid",
  "match_score": 92,
  "summary": "整体匹配度较高，前端框架经验与岗位要求高度一致。",
  "strengths": ["React 经验充分", "有性能优化项目"],
  "gaps": ["缺少大型中台项目案例"],
  "suggestions": ["补充数据指标", "突出跨团队协作经历"]
}
```

### 7.4 获取推荐岗位

- 方法：`GET`
- 路径：`/api/v1/jobs/recommendations`
- 查询参数：`limit`

## 8. 幻影投递官：简历与投递接口

MVP 阶段建议**先做投递记录管理，不直接做复杂自动投递**。第三方官网表单自动提交可以作为后续增强功能。

### 8.1 上传基础简历

- 方法：`POST`
- 路径：`/api/v1/resumes`
- 内容类型：`multipart/form-data`
- 表单字段：
  - `file`
  - `title`（可选）
  - `is_base`（可选，默认 `true`）

响应 `data`：

```json
{
  "id": "uuid",
  "title": "我的基础简历",
  "file_name": "resume.pdf",
  "status": "done",
  "parsed_summary": "已识别教育背景、项目经历、技能关键词。"
}
```

### 8.2 获取简历列表

- 方法：`GET`
- 路径：`/api/v1/resumes`

### 8.3 创建定制简历任务

- 方法：`POST`
- 路径：`/api/v1/resumes/{resume_id}/customizations`

请求体：

```json
{
  "job_id": "uuid"
}
```

响应 `data`：

```json
{
  "id": "uuid",
  "resume_id": "uuid",
  "job_id": "uuid",
  "status": "processing",
  "progress": 35,
  "current_stage": "优化项目经验描述",
  "stages": [
    {"name": "分析目标职位要求", "status": "done"},
    {"name": "提取关键技能", "status": "done"},
    {"name": "优化项目经验描述", "status": "processing"},
    {"name": "生成定制化自我介绍", "status": "pending"}
  ]
}
```

### 8.4 获取定制简历任务详情

- 方法：`GET`
- 路径：`/api/v1/resume-customizations/{customization_id}`

### 8.5 获取投递列表

- 方法：`GET`
- 路径：`/api/v1/deliveries`

查询参数：

- `status`
- `page`
- `page_size`

响应 `data`：

```json
{
  "items": [
    {
      "id": "uuid",
      "job_id": "uuid",
      "resume_id": "uuid",
      "company": "字节跳动",
      "position": "高级前端工程师",
      "status": "delivered",
      "updated_at": "2026-03-07T10:30:00+08:00"
    }
  ],
  "page": 1,
  "page_size": 20,
  "total": 1
}
```

### 8.6 新建投递记录

- 方法：`POST`
- 路径：`/api/v1/deliveries`

请求体：

```json
{
  "job_id": "uuid",
  "resume_id": "uuid",
  "channel": "official_site",
  "note": "MVP 先只记录手动投递"
}
```

### 8.7 更新投递状态

- 方法：`PATCH`
- 路径：`/api/v1/deliveries/{delivery_id}`

请求体：

```json
{
  "status": "interview",
  "note": "已收到一面通知"
}
```

### 8.8 获取投递统计

- 方法：`GET`
- 路径：`/api/v1/deliveries/stats`

响应 `data`：

```json
{
  "total": 18,
  "pending": 5,
  "delivering": 1,
  "delivered": 8,
  "viewed": 2,
  "written_test": 1,
  "interview": 1,
  "offer": 0,
  "rejected": 1
}
```

## 9. 深网调查员：企业分析接口

企业风险分析本质上是“输入企业名，产出分析结果”的异步任务，建议建模为 `company-reports`。

### 9.1 创建企业分析任务

- 方法：`POST`
- 路径：`/api/v1/company-reports`

请求体：

```json
{
  "company_name": "字节跳动"
}
```

响应 `data`：

```json
{
  "id": "uuid",
  "company_name": "字节跳动",
  "status": "processing",
  "progress": 40,
  "current_stage": "舆情分析",
  "sources": ["工商信息", "舆情分析", "员工评价", "财务状况"]
}
```

### 9.2 获取企业分析列表

- 方法：`GET`
- 路径：`/api/v1/company-reports`

响应项字段建议：

- `id`
- `name`
- `industry`
- `size`
- `rating`
- `risk_level`
- `growth`
- `salary_range`
- `risks`
- `positives`
- `updated_at`

### 9.3 获取企业分析详情

- 方法：`GET`
- 路径：`/api/v1/company-reports/{report_id}`

响应 `data`：

```json
{
  "id": "uuid",
  "name": "字节跳动",
  "industry": "互联网科技",
  "size": "100000+",
  "rating": 4.2,
  "risk_level": "low",
  "growth": 23,
  "salary_range": "45K",
  "basic_profile": {
    "registered_capital": "xxx",
    "established_date": "2012-03-09",
    "legal_representative": "xxx",
    "status": "存续"
  },
  "risk_breakdown": {
    "judicial": "low",
    "operational": "low",
    "public_opinion": "medium"
  },
  "summary": "整体风险可控，但加班强度较高。",
  "risks": ["加班文化较重", "竞争压力大"],
  "positives": ["技术栈先进", "晋升通道清晰", "年终奖丰厚"]
}
```

## 10. 契约卫士：合同解析接口

合同解析是典型的上传 + 异步处理 + 明细查询场景。

### 10.1 上传合同并发起解析

- 方法：`POST`
- 路径：`/api/v1/contracts`
- 内容类型：`multipart/form-data`
- 表单字段：
  - `file`

响应 `data`：

```json
{
  "id": "uuid",
  "file_name": "劳动合同示例.pdf",
  "status": "processing",
  "progress": 10,
  "current_stage": "文档解析"
}
```

### 10.2 获取合同解析摘要

- 方法：`GET`
- 路径：`/api/v1/contracts/{contract_id}`

响应 `data`：

```json
{
  "id": "uuid",
  "file_name": "劳动合同示例.pdf",
  "status": "done",
  "progress": 100,
  "overall_score": 72,
  "risk_level": "warning",
  "summary": "合同整体可签，但竞业限制和保密责任需重点确认。",
  "summary_counts": {
    "safe": 2,
    "warning": 1,
    "danger": 1
  },
  "stages": [
    {"name": "文档解析", "status": "done"},
    {"name": "条款识别", "status": "done"},
    {"name": "风险分析", "status": "done"},
    {"name": "生成报告", "status": "done"}
  ]
}
```

### 10.3 获取条款列表

- 方法：`GET`
- 路径：`/api/v1/contracts/{contract_id}/clauses`

响应 `data`：

```json
{
  "items": [
    {
      "id": "uuid",
      "title": "竞业限制条款",
      "content": "员工在离职后 24 个月内...",
      "risk_level": "warning",
      "explanation": "竞业限制期限较长。",
      "suggestion": "建议协商缩短至 12 个月。"
    }
  ]
}
```

### 10.4 获取合同整体报告

- 方法：`GET`
- 路径：`/api/v1/contracts/{contract_id}/report`

响应 `data`：

```json
{
  "overall_score": 72,
  "risk_level": "warning",
  "summary": "存在 1 条高风险条款，建议修改后再签署。",
  "key_risks": ["保密义务赔偿责任过重", "竞业限制期限偏长"],
  "recommendation": "建议与 HR 协商修改竞业和保密条款后再决定签署。"
}
```

## 11. AI 对话接口

AI 对话建议采用“会话 + 消息 + SSE 流”的结构，而不是单个无状态接口。

### 11.1 创建会话

- 方法：`POST`
- 路径：`/api/v1/chat/sessions`

响应 `data`：

```json
{
  "session_id": "uuid",
  "welcome_message": "你好！我是求职高手 AI 助手。"
}
```

### 11.2 获取会话列表

- 方法：`GET`
- 路径：`/api/v1/chat/sessions`

### 11.3 获取消息历史

- 方法：`GET`
- 路径：`/api/v1/chat/sessions/{session_id}/messages`

### 11.4 发送用户消息

- 方法：`POST`
- 路径：`/api/v1/chat/sessions/{session_id}/messages`

请求体：

```json
{
  "content": "帮我找高薪远程工作",
  "current_module": "eagle"
}
```

响应 `data`：

```json
{
  "message_id": "uuid",
  "detected_module": "eagle"
}
```

### 11.5 SSE 流式返回 AI 回复

- 方法：`GET`
- 路径：`/api/v1/chat/sessions/{session_id}/stream?message_id={message_id}`
- 返回类型：`text/event-stream`

建议事件类型：

- `routing`：意图识别结果
- `delta`：流式文本片段
- `message`：整段消息完成
- `done`：本轮结束
- `error`：异常

SSE 示例：

```text
event: routing
data: {"module":"eagle"}

event: delta
data: {"content":"好的，我正在扫描"}

event: delta
data: {"content":"全网招聘信息..."}

event: done
data: {"message_id":"uuid"}
```

## 12. 健康检查

### 12.1 健康检查

- 方法：`GET`
- 路径：`/health`

响应：

```json
{
  "status": "ok"
}
```

## 13. Android 页面到接口映射

为方便后续替换 mock，建议按页面拆解接入：

| 页面 | 当前主要 mock 内容 | 建议替换接口 |
|---|---|---|
| 首页 | 问候语、通知数、Hero 统计、模块卡片、动态列表 | `GET /api/v1/dashboard/home` |
| AI 对话 | 快捷指令、消息历史、模拟回复 | `POST /api/v1/chat/sessions`、`GET /api/v1/chat/sessions/{session_id}/messages`、`POST /api/v1/chat/sessions/{session_id}/messages`、`GET /api/v1/chat/sessions/{session_id}/stream` |
| 鹰眼猎手 | 岗位列表、匹配度、标签 | `GET /api/v1/jobs`、`GET /api/v1/jobs/{job_id}`、`POST /api/v1/jobs/{job_id}/match` |
| 幻影投递官 | 简历优化步骤、投递队列、统计 | `POST /api/v1/resumes`、`POST /api/v1/resumes/{resume_id}/customizations`、`GET /api/v1/resume-customizations/{customization_id}`、`GET /api/v1/deliveries`、`GET /api/v1/deliveries/stats` |
| 深网调查员 | 数据源进度、企业风险画像 | `POST /api/v1/company-reports`、`GET /api/v1/company-reports`、`GET /api/v1/company-reports/{report_id}` |
| 契约卫士 | 文件上传、解析进度、风险摘要、条款详解 | `POST /api/v1/contracts`、`GET /api/v1/contracts/{contract_id}`、`GET /api/v1/contracts/{contract_id}/clauses`、`GET /api/v1/contracts/{contract_id}/report` |

## 14. 当前后端骨架与目标契约的主要差异

### 14.1 路由层差异

当前骨架：

- `/api/jobs`
- `/api/delivery`
- `/api/company`
- `/api/contract`
- `/api/chat`

建议目标：

- `/api/v1/jobs`
- `/api/v1/resumes`
- `/api/v1/deliveries`
- `/api/v1/company-reports`
- `/api/v1/contracts`
- `/api/v1/chat/sessions`
- `/api/v1/dashboard/home`

### 14.2 模型层差异

后续后端实现前建议先补齐这些 DTO / Schema：

- 岗位列表项：补 `match_score`、`is_new`、`published_at`
- 投递状态：统一 `delivering / offer / written_test` 等枚举
- 企业分析详情：补 `rating`、`growth`、`salary_range`、`risks[]`、`positives[]`
- 合同条款：从通用 `analysis_result` JSON 中拆出稳定条款结构
- 聊天：增加 `chat_session`、`chat_message` 概念
- 首页：增加聚合返回 DTO

## 15. 推荐实施顺序

### 阶段 1：先固化后端契约

- 定义 `schemas/` 下的请求与响应模型
- 在 FastAPI 中明确每个接口的 `response_model`
- 先把 OpenAPI 文档跑起来，再进入客户端对接

### 阶段 2：优先接入高价值页面

建议优先级：

1. 首页聚合接口
2. 岗位列表与详情
3. 企业分析
4. 合同解析
5. AI 对话
6. 简历与投递

### 阶段 3：替换客户端 mock

- 保留 UI 动效相关本地状态
- 替换业务数据来源为 Repository + API
- 页面改为“加载中 / 成功 / 失败”三态

## 16. 说明

本文档是当前项目进入“Android mock 改真实 API”前的接口基线。

下一步建议：

- 先根据本文档补后端 `schemas`
- 再统一 FastAPI 路由命名与返回结构
- 最后开始 Android 数据层接入与页面逐步替换
