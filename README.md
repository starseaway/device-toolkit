# Device Toolkit 设备工具包

<div align="center">
  <img src="device-toolkit-logo.svg" width="500" alt="device-toolkit-logo">
</div>

![Version](https://img.shields.io/badge/version-2.0.1-blue)
![License](https://img.shields.io/badge/license-Apache%202.0-green)
![API](https://img.shields.io/badge/API-19%2B-brightgreen)

## 一、模块简介

Device Toolkit 是一个专注于 Android 设备能力封装的库，整合了日常开发中高频使用的设备相关的逻辑功能（屏幕密度、设备信息、存储、系统状态、网络、电池等），提供统一入口，开箱即用。

本库无侵入设计，不依赖特定架构，任何项目均可接入；你无需重复编写零碎的功能逻辑，只需初始化一次即可直接调用各类功能，大幅提升开发效率，也让代码更整洁易维护。

---

## 二、SDK 适用范围

- Android SDK 版本：Min SDK 19（Android 4.4）及以上

---

## 三、集成方式

### 1. 根据 Gradle 版本或项目配置自行选择在合适的位置添加仓库地址
```groovy
maven {
    // jitpack仓库
    url 'https://jitpack.io' 
}
```

### 2. 在 `build.gradle` (Module 级) 中添加依赖：
```groovy
dependencies {
    implementation 'com.github.starseaway:device-toolkit:2.0.1'
}
```

```kotlin
dependencies {
    implementation("com.github.starseaway:device-toolkit:2.0.1")
}
```

### 3. 初始化模块
```kotlin
class AppApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        /*
         * 由于本模块中多个类依赖 Context，故提供一个全局 Application 级上下文，
         * 避免在各个工具类中重复传递 Context，这样能提高代码的简洁性。
         *
         * `DeviceContext` 内含多种设备相关工具类所需的上下文信息，如获取系统服务、
         * 资源、包管理器等，简化调用方式，提高代码可读性和可维护性。
         */
        DeviceContext.init(this)
    }
}
```

---

## 四、模块结构

```
src/main/
├── AndroidManifest.xml
└── java/com/xinyi/device/
    ├── DeviceContext.java               # 全局上下文入口，统一管理 Application Context
    │
    ├── app/
    │   └── AppManager.java              # 应用管理入口，封装常见的 App 级操作能力
    │
    ├── battery/
    │   ├── BatteryLevelListener.java    # 电量变化回调接口
    │   ├── BatteryLevelMonitor.java     # 电池电量监听器
    │   └── BatteryReceiver.java         # 电池广播接收实现
    │
    ├── info/
    │   ├── DeviceInfo.java              # 设备基础信息
    │   ├── MemoryInfo.java              # 内存使用情况
    │   ├── StorageInfo.java             # 存储空间信息
    │   └── SystemMonitor.java           # 系统综合监控器
    │
    ├── net/
    │   └── NetworkStatusMonitor.java    # 网络状态监听
    │
    └── utils/
        ├── DensityUtil.java             # 屏幕密度与单位转换
        ├── IntentUtil.java              # 常用系统 Intent 封装
        ├── SettingsNavigator.java       # 系统设置页面跳转封装
        ├── ShellUtil.java               # Shell 命令执行（支持结果回调）
        └── SystemUtil.java              # 系统级能力工具
```

## 五、快速开始

下面对部分功能类做简单示例

### 1. 应用能力

```kotlin
// 获取当前应用版本
val versionName = AppManager.getAppVersionName()

// 判断某个应用是否已安装
val isInstalled = AppManager.isInstallApp("com.example.app")

// 打开指定应用
AppManager.openApp("com.example.app")
```

### 2. 设备信息

```kotlin
// 设备基础信息
val model = DeviceInfo.getDeviceModel()
val brand = DeviceInfo.getDeviceBrand()
val androidVersion = DeviceInfo.getAndroidVersion()

// 系统 API 等级
val sdkInt = DeviceInfo.getSDK_INT()
```

### 3. 电池监听

```kotlin
val monitor = BatteryLevelMonitor { level, scale, isCharging ->
    val percent = level * 100 / scale
    // 当前电量百分比
}

// 注册监听
monitor.register()

// 在合适时机（如 onDestroy）取消注册
monitor.unregister()
```

### 4. 网络状态监听

```kotlin
val monitor = NetworkStatusMonitor(object : NetworkStatusMonitor.OnNetworkStatusMonitor {
    override fun networkIsConnected(network: Network) {
        // 网络已连接
    }

    override fun networkHasBeenDisconnected(network: Network) {
        // 网络已断开
    }
})

// 注册监听（Android 5.0+，5.0 以下需要静态注册广播实现）
monitor.register()

// 取消监听
monitor.unregister()
```

### 5. 存储信息

```kotlin
// 获取内部存储信息
val internal = StorageInfo.getInternalStorageInfo()
// internal[0] 总空间
// internal[1] 已用空间
// internal[2] 可用空间

// 判断空间是否充足
val enough = StorageInfo.isInternalSpaceEnough(100 * 1024 * 1024) // 100MB
```

### 6. 屏幕适配

```kotlin
// dp -> px
val px = DensityUtil.dip2px(16f)

// 获取真实屏幕尺寸（包含系统栏）
val width = DensityUtil.getRealScreenWidth()
val height = DensityUtil.getRealScreenHeight()
```

### 7. Shell 执行

> ⚠️ 注意：执行 Shell 命令可能会影响设备，尤其在 Root 状态下，请谨慎操作

```kotlin
// 1. 判断设备是否开启 ADB
val adbEnabled = ShellUtil.isAdbEnabled()

// 2. 判断设备是否 Root
val isRoot = ShellUtil.isCheckRoot()

// 3. 执行多条命令
val commands = listOf(
    "cd /sdcard",
    "mkdir test_dir",
    "touch test_dir/demo.txt"
)

val result = ShellUtil.execCommand(commands, isRoot)

if (result.result == 0) {
    println("执行成功：${result.successMsg}")
} else {
    println("执行失败：${result.errorMsg}")
}
```

## 六、版本变更记录

### V2.0.1 (2026-03-31)
- build: 修改 agp 构建版本

### V2.0.0 (2026-03-27)
- 调整整体目录结构
- 从该版本开始，正式在 GitHub 开源

### V1.4.2 (2025-08-26)
- 调整电池监听实现
- 兼容 Android SDK 19 + Java 8 以下环境
- 修复 R8 优化场景下的潜在崩溃问题

### V1.4.0 (2025-08-19)
- 添加了系统内存相关的能力
- 系统存储相关的信息获取能力升级
- 添加了CPU、系统负载、电池等信息的获取
- 提供电池电量监听能力

### V1.3.0 (2025-07-14)
- 支持获取设备真实屏幕宽高（更精确）

### V1.2.2 (2025-06-16)
- 补充更多设备信息接口
- 优化部分方法可读性

### V1.2.0 (2025-06-13)
- 扩展设备信息获取能力

### V1.1.0 (2025-05-30)
- 新增网络状态监视器

### V1.0.2 (2025-05-29)
- 修改未初始化的报错提示信息

### V1.0.1 (2025-05-29)
- 新增系统设置跳转工具类

### V1.0.0 (2025-04-03)
- 初始版本，包含基础的设备信息、存储管理、系统监控等工具类。