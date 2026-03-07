# 求职高手 Android 客户端

基于 Jetpack Compose 的原生安卓应用，当前为 UI 原型与交互演示阶段，使用本地 mock 数据驱动。

## 页面说明

| 页面 | 功能 |
|------|------|
| 首页 | 问候、状态卡片、AI 对话入口、智能体动态、模块宫格导航 |
| AI 对话 | 全局覆盖浮层，快捷指令、消息气泡、上下文分发 |
| 鹰眼猎手 | 雷达扫描动效、职位发现与匹配度列表 |
| 幻影投递官 | 简历优化进度、投递队列与状态追踪 |
| 深网调查员 | 数据源分析进度、企业风险画像 |
| 契约卫士 | 合同上传、条款扫描、风险摘要、条款详解 |

## 代码结构

```
app/src/main/java/com/example/client/
├── MainActivity.kt
├── data/Models.kt
├── navigation/Navigation.kt
└── ui/
    ├── components/Components.kt
    ├── screens/
    │   ├── HomeScreen.kt
    │   ├── MainScreen.kt
    │   ├── HawkEyeScreen.kt
    │   ├── DeliveryScreen.kt
    │   ├── InvestigatorScreen.kt
    │   └── GuardianScreen.kt
    └── theme/
```

## 运行

1. Android Studio 打开本目录
2. Gradle 同步
3. 连接设备，运行 app

最低 SDK 26，目标 SDK 36。
