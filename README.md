# LanV Tool

Vivo/OriginOS 系统属性管理工具。需要 Root 权限。

## 功能

通过 `getprop` / `setprop` 读写系统属性，支持内存融合、显示、充电、游戏等分类的参数调整。首次启动自动备份原始值，可随时还原。

## 技术栈

- Kotlin + Jetpack Compose
- Material3 动态主题
- 三层架构：UI → Domain → Data
- SharedPreferences 持久化备份

## 构建

```bash
./gradlew assembleRelease
```

输出：`app/build/outputs/apk/release/app-release.apk`

## 要求

- Android 9.0+ (API 28)
- 已 Root 的设备
- Vivo/OriginOS 系统（部分属性仅限 Vivo）

## 说明

本项目从零手写完成，未使用代码生成工具。每个属性都经过实机测试验证。
