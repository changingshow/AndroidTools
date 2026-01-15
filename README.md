# 权限监控 (Permission Monitor)

一款帮助您了解和管理手机应用权限的 Android 应用。

![Android](https://img.shields.io/badge/Android-5.0%2B-green)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-purple)

## 功能特性

- 📱 **应用扫描** - 扫描所有已安装的第三方应用
- 🔍 **权限查看** - 查看每个应用申请的权限及授予状态
- ⚠️ **风险评估** - 按危险等级分类权限（低/中/高/危险）
- 📊 **统计概览** - 显示扫描结果统计（已扫描/安全/有风险）
- ⚙️ **快捷管理** - 一键跳转系统设置管理应用权限
- 🔎 **搜索筛选** - 支持按名称搜索，可切换显示系统应用

## 截图

| 主界面 | 应用详情 | 关于页面 |
|:---:|:---:|:---:|
| 应用列表与统计 | 权限详情与管理 | 应用信息与说明 |

## 技术栈

| 技术 | 说明 |
|-----|------|
| Kotlin | 开发语言 |
| Jetpack Compose | 现代声明式 UI 框架 |
| Material 3 | Google 最新设计规范 |
| MVVM | 架构模式 |
| Navigation Compose | 页面导航 |
| Coroutines + Flow | 异步处理与状态管理 |

## 项目结构

```
app/src/main/java/com/permissionmonitor/
├── MainActivity.kt              # 主 Activity
├── data/
│   ├── model/                   # 数据模型
│   │   ├── AppInfo.kt
│   │   └── PermissionDetail.kt
│   ├── repository/              # 数据仓库
│   │   └── AppRepository.kt
│   └── source/                  # 数据源
│       ├── AppDataSource.kt
│       └── PermissionClassifier.kt
└── ui/
    ├── components/              # 可复用组件
    │   ├── AppListItem.kt
    │   ├── EmptyStateView.kt
    │   └── StatCard.kt
    ├── navigation/              # 导航配置
    │   └── NavGraph.kt
    ├── screen/                  # 页面
    │   ├── AppListScreen.kt
    │   ├── AppDetailScreen.kt
    │   └── AboutScreen.kt
    ├── theme/                   # 主题
    │   └── Theme.kt
    └── viewmodel/               # 视图模型
        └── AppListViewModel.kt
```

## 构建说明

### 环境要求

- JDK 17+
- Android SDK 34
- Gradle 8.2

### 本地构建

```bash
# 克隆项目
git clone https://github.com/changingshow/AndroidTools.git
cd AndroidTools/PermissionMonitor

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK（需要签名配置）
./gradlew assembleRelease
```

构建产物位置：
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

### GitHub Actions 自动构建

本项目配置了 GitHub Actions，推送代码后自动构建：

1. 进入 GitHub 仓库 → **Actions** 标签
2. 点击最新的 **Build APK** workflow
3. 在 **Artifacts** 区域下载 APK

## 签名配置

### 生成签名密钥

```bash
keytool -genkey -v \
    -keystore release-key.jks \
    -storetype JKS \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -alias release
```

### 配置签名（app/build.gradle.kts）

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("../release-key.jks")
        storePassword = "your_password"
        keyAlias = "release"
        keyPassword = "your_password"
    }
}
```

> ⚠️ **注意**：请妥善保管签名密钥，丢失后将无法更新已发布的应用。

## 权限说明

| 权限 | 用途 |
|-----|------|
| `QUERY_ALL_PACKAGES` | 查询设备上已安装的应用列表 |
| `PACKAGE_USAGE_STATS` | 获取应用使用统计（可选） |

## 开发指南

### 添加新页面

1. 在 `ui/screen/` 创建新的 Composable 函数
2. 在 `NavGraph.kt` 添加路由
3. 如需要，创建对应的 ViewModel

### 权限分类规则

```kotlin
// PermissionClassifier.kt
DANGEROUS: 相机、麦克风、精确位置、短信、通话记录等
HIGH: 存储、粗略位置、联系人等  
MEDIUM: 读取手机状态等
LOW: 其他权限
```

## 常见问题

**Q: 为什么应用列表为空？**

A: 需要授予「查询已安装应用」权限。点击「去授权」按钮，在系统设置中允许权限后返回即可。

**Q: Release APK 无法安装？**

A: Release 版本需要签名。确保已正确配置签名密钥，或使用 Debug 版本进行测试。

**Q: 如何贡献代码？**

A: Fork 本项目，创建功能分支，提交 PR。

## 版本历史

### v1.0
- 初始版本
- 应用扫描与权限查看
- 风险等级分类
- 权限引导页面
- 关于页面

## 许可证

MIT License

## 致谢

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
