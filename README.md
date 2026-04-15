# PhotoCleaner

一款基于 Android + Jetpack Compose 的相册清理工具，通过左右滑动快速筛选照片，并记录操作历史，支持集中彻底删除。

## 功能特性

- 左滑删除、右滑保留，卡片式快速筛图
- 支持按最新 / 最旧 / 最大文件 / 随机排序展示
- 支持最小体积过滤（仅查看大于指定 MB 的图片）
- 操作历史记录（已删除 / 已保留）
- 一键彻底删除“待删除”照片（Android 11+ 走系统批量删除确认）
- 数据统计与重置（已删除数量、已保留数量）

## 技术栈

- Kotlin
- Jetpack Compose + Material 3
- MVVM（ViewModel + StateFlow）
- Room（本地历史记录）
- Hilt（依赖注入）
- Coroutines
- Coil（图片加载）

## 运行环境

- JDK 17
- Android Studio（建议较新版本）
- Android SDK 34
- 最低系统版本：Android 10（API 29）

## 快速开始

```bash
git clone https://github.com/<your-username>/PhotoCleaner.git
cd PhotoCleaner
chmod +x gradlew
./gradlew assembleDebug
```

首次打开应用后，请授予相册读取权限。

## 权限说明

- `READ_MEDIA_IMAGES`（Android 13+）：读取图片
- `READ_EXTERNAL_STORAGE`（Android 12 及以下）：读取图片
- `ACCESS_MEDIA_LOCATION`：访问媒体位置信息
- `DELETE_EXTERNAL_STORAGE`：旧版本系统上的删除相关能力

## 项目结构

```text
app/src/main/java/
├── com/jimmy/photocleaner/      # 应用入口（Application、MainActivity）
├── ui/                          # Compose UI（页面与组件）
├── viewmodel/                   # 业务状态与交互逻辑
├── data/
│   ├── repository/              # 媒体读取、历史记录与删除流程封装
│   ├── dao/                     # Room DAO
│   └── database/                # Room Database
└── di/                          # Hilt 依赖注入模块
```

## 说明

- 当前“左滑删除”会先记录为待删除，不会立即物理删除文件。
- 进入“历史记录”页后使用“一键彻底删除”，才会执行系统级删除流程。
