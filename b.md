# 从零开始开发 Android App 完整指南

## 项目背景

本文基于实际开发「权限监控」App 的完整过程，记录如何在**没有 Android Studio** 的环境下，使用纯代码 + GitHub Actions 完成 Android 应用的开发、打包、签名和发布。

---

## 一、项目规划

### 1.1 需求分析

开发一个查看第三方应用权限使用情况的工具，核心功能：
- 显示已安装应用列表
- 查看每个应用的权限详情
- 权限风险等级分类
- 快捷跳转系统设置管理权限

### 1.2 技术选型

| 项目 | 选择 | 理由 |
|-----|------|------|
| 语言 | Kotlin | Android 官方推荐 |
| UI框架 | Jetpack Compose | 现代声明式 UI |
| 架构 | MVVM | 数据驱动，易维护 |
| 最低版本 | API 21 (Android 5.0) | 覆盖 99% 设备 |
| 构建工具 | Gradle 8.2 | 稳定版本 |

### 1.3 项目结构

```
PermissionMonitor/
├── app/
│   ├── build.gradle.kts          # 应用构建配置
│   ├── src/main/
│   │   ├── AndroidManifest.xml   # 应用清单
│   │   ├── java/com/permissionmonitor/
│   │   │   ├── MainActivity.kt
│   │   │   ├── data/
│   │   │   │   ├── model/        # 数据模型
│   │   │   │   ├── repository/   # 数据仓库
│   │   │   │   └── source/       # 数据源
│   │   │   └── ui/
│   │   │       ├── components/   # 可复用组件
│   │   │       ├── navigation/   # 导航配置
│   │   │       ├── screen/       # 页面
│   │   │       ├── theme/        # 主题
│   │   │       └── viewmodel/    # 视图模型
│   │   └── res/                  # 资源文件
├── build.gradle.kts              # 项目构建配置
├── settings.gradle.kts           # 项目设置
├── gradle.properties             # Gradle 属性
├── gradlew                       # Unix 构建脚本
├── gradlew.bat                   # Windows 构建脚本
└── gradle/wrapper/
    ├── gradle-wrapper.jar
    └── gradle-wrapper.properties
```

---

## 二、核心配置文件

### 2.1 settings.gradle.kts

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PermissionMonitor"
include(":app")
```

### 2.2 build.gradle.kts (项目级)

```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
}
```

### 2.3 app/build.gradle.kts

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.permissionmonitor"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.permissionmonitor"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    // 签名配置
    signingConfigs {
        create("release") {
            storeFile = file("../release-key.jks")
            storePassword = "your_password"
            keyAlias = "release"
            keyPassword = "your_password"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    
    // Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
}
```

### 2.4 AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- 查询所有已安装应用 -->
    <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES"
        tools:ignore="QueryAllPackagesPermission" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:theme="@style/Theme.PermissionMonitor">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

---

## 三、核心代码实现

### 3.1 获取应用权限列表

```kotlin
class AppDataSource(private val context: Context) {
    
    private val packageManager: PackageManager = context.packageManager
    
    fun getInstalledApps(): List<AppInfo> {
        val packages = packageManager.getInstalledPackages(
            PackageManager.GET_PERMISSIONS
        )
        
        return packages
            .filter { (it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
            .mapNotNull { createAppInfo(it) }
    }
    
    private fun createAppInfo(packageInfo: PackageInfo): AppInfo? {
        val appInfo = packageInfo.applicationInfo ?: return null
        val permissions = getPermissionDetails(packageInfo)
        
        return AppInfo(
            packageName = packageInfo.packageName,
            appName = appInfo.loadLabel(packageManager).toString(),
            icon = appInfo.loadIcon(packageManager),
            permissions = permissions,
            dangerousPermissions = permissions.count { it.isDangerous && it.isGranted }
        )
    }
    
    private fun getPermissionDetails(packageInfo: PackageInfo): List<PermissionDetail> {
        val requested = packageInfo.requestedPermissions ?: return emptyList()
        val flags = packageInfo.requestedPermissionsFlags ?: return emptyList()
        
        return requested.mapIndexed { index, permission ->
            PermissionDetail(
                name = permission,
                isGranted = (flags[index] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
            )
        }
    }
}
```

### 3.2 Compose UI 示例

```kotlin
@Composable
fun AppListScreen(
    onAppClick: (String) -> Unit,
    viewModel: AppListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("权限监控") })
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(uiState.apps) { app ->
                AppListItem(
                    appInfo = app,
                    onClick = { onAppClick(app.packageName) }
                )
            }
        }
    }
}
```

### 3.3 ViewModel

```kotlin
class AppListViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = AppRepository(application)
    private val _uiState = MutableStateFlow(AppListUiState())
    val uiState: StateFlow<AppListUiState> = _uiState.asStateFlow()
    
    init { loadApps() }
    
    fun loadApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val apps = withContext(Dispatchers.IO) {
                repository.getInstalledApps()
            }
            _uiState.value = _uiState.value.copy(
                apps = apps,
                isLoading = false
            )
        }
    }
}
```

---

## 四、生成签名密钥

### 4.1 使用 keytool 生成

```bash
keytool -genkey -v \
    -keystore release-key.jks \
    -storetype JKS \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -alias release \
    -storepass your_password \
    -keypass your_password \
    -dname "CN=AppName, OU=Dev, O=Company, L=City, ST=State, C=CN"
```

### 4.2 签名说明

| 类型 | 用途 | 来源 |
|-----|------|------|
| Debug 签名 | 开发测试 | SDK 自动生成 |
| Release 签名 | 正式发布 | 自己用 keytool 生成 |

**重要**：签名密钥丢失后无法更新已发布的应用，务必妥善保管！

---

## 五、GitHub Actions 自动构建

### 5.1 创建 workflow 文件

`.github/workflows/build.yml`:

```yaml
name: Build APK

on:
  push:
    branches: [ main ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle

    - name: Setup Android SDK
      uses: android-actions/setup-android@v3

    - name: Grant execute permission for gradlew
      run: chmod +x gradlew

    - name: Build Debug APK
      run: ./gradlew assembleDebug

    - name: Upload Debug APK
      uses: actions/upload-artifact@v4
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/*.apk

    - name: Build Release APK
      run: ./gradlew assembleRelease

    - name: Upload Release APK
      uses: actions/upload-artifact@v4
      with:
        name: app-release
        path: app/build/outputs/apk/release/*.apk
```

### 5.2 使用流程

1. **推送代码到 GitHub**
   ```bash
   git init
   git add -A
   git commit -m "Initial commit"
   git remote add origin https://github.com/username/repo.git
   git push -u origin main
   ```

2. **查看构建状态**
   - 打开 GitHub 仓库 → Actions 标签页
   - 等待 workflow 完成（约 3-5 分钟）

3. **下载 APK**
   - 点击完成的 workflow
   - 在 Artifacts 区域下载 APK
   - 解压后即可安装

### 5.3 Debug vs Release APK

| 类型 | 签名 | 体积 | 用途 |
|-----|------|------|------|
| Debug | 自动签名 | 较大 | 开发测试 |
| Release | 自定义签名 | 较小（混淆压缩） | 正式发布 |

**注意**：未签名的 Release APK 无法安装，必须配置签名。

---

## 六、常见问题与解决

### 6.1 Gradle Wrapper 脚本错误

**问题**：`Could not find or load main class`

**解决**：使用标准的 gradlew 脚本，避免特殊字符处理问题。

### 6.2 图标兼容性问题

**问题**：`<adaptive-icon> elements require SDK 26`

**解决**：
- API 26+ 使用 `mipmap-anydpi-v26/` 自适应图标
- API 26 以下使用 `mipmap-hdpi/` 等普通图标

### 6.3 权限授予后列表不刷新

**问题**：用户授权返回后数据没更新

**解决**：在 `MainActivity.onResume()` 中触发刷新

```kotlin
override fun onResume() {
    super.onResume()
    // 触发 ViewModel 重新加载
}
```

### 6.4 Kotlin 编译错误

**问题**：API 兼容性问题

**常见原因**：
- 使用了高版本 API（如 `mutableIntStateOf` 需要 Compose 1.5+）
- 使用了新版 Material Icons（如 `Icons.AutoMirrored`）

**解决**：使用兼容性更好的替代 API。

---

## 七、项目优化建议

### 7.1 用户体验

- 添加权限引导页面
- 空状态友好提示
- 加载状态动画
- 下拉刷新

### 7.2 功能扩展

- 权限使用统计（需要 `PACKAGE_USAGE_STATS` 权限）
- 应用对比功能
- 导出报告
- 定期扫描提醒

### 7.3 性能优化

- 图片缓存
- 列表分页加载
- 后台异步处理

---

## 八、总结

### 开发流程回顾

```
需求分析 → 技术选型 → 项目结构设计
    ↓
创建配置文件 (Gradle, Manifest)
    ↓
实现核心功能 (数据层 → ViewModel → UI)
    ↓
生成签名密钥
    ↓
配置 GitHub Actions
    ↓
推送代码 → 自动构建 → 下载 APK → 安装测试
    ↓
迭代优化
```

### 关键要点

1. **无需 Android Studio**：纯代码 + GitHub Actions 即可完成开发
2. **签名很重要**：Release APK 必须签名才能安装
3. **版本兼容**：注意 minSdk 与 API 的兼容性
4. **用户体验**：权限引导、加载状态、空状态处理

### 项目地址

本文示例项目：`https://github.com/changingshow/AndroidTools`

---

*本文基于实际开发过程整理，涵盖了从零开始到发布的完整流程。*
