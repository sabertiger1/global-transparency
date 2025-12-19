# 悬浮窗显示应用

这是一个Android应用，可以在屏幕上显示半透明的悬浮图片，类似全局透明壁纸的效果，且不影响屏幕操作。

## 功能特点

- ✅ 悬浮窗显示PNG图片
- ✅ 半透明效果（可调节透明度）
- ✅ 横屏显示支持
- ✅ 触摸穿透（不影响屏幕操作）
- ✅ 前台服务保活
- ✅ 适配Android 15
- ✅ **APP内完整控制**：位置、透明度、尺寸均可调整
- ✅ **自动保存设置**：所有调整自动保存，重启后恢复
- ✅ **GitHub Actions 自动编译**：无需本地编译环境

## 设备信息

- 目标设备：红米K80 Pro
- Android版本：Android 15
- 分辨率：3200×1440
- 屏幕尺寸：6.67英寸

## 技术实现

### 核心组件

1. **MainActivity**: 主界面，用于启动/停止悬浮窗服务
2. **FloatingWindowService**: 前台服务，负责创建和管理悬浮窗
3. **WindowManager**: 用于创建系统级悬浮窗

### 关键技术点

- **悬浮窗权限**: `SYSTEM_ALERT_WINDOW` - 需要用户手动授权
- **前台服务**: 使用 `FOREGROUND_SERVICE_SPECIAL_USE` 保持服务运行
- **触摸穿透**: 使用 `FLAG_NOT_TOUCHABLE` 标志
- **横屏适配**: 自动检测屏幕方向，旋转图片实现横屏显示

## 使用方法

### 本地编译

1. **使用 Android Studio**
   - 用 Android Studio 打开项目
   - 点击 Build > Make Project
   - 或运行 `./gradlew assembleDebug`

2. **命令行编译**
   ```bash
   ./gradlew assembleDebug
   ```

3. **安装应用**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### GitHub Actions 自动编译（推荐）

项目已配置 GitHub Actions，无需本地编译环境：

1. **推送代码到 GitHub**
   ```bash
   git push origin main
   ```

2. **查看编译结果**
   - 访问仓库的 Actions 标签页
   - 下载编译好的 APK

详细设置请参考 [SETUP_GITHUB.md](SETUP_GITHUB.md)

3. **授予权限**
   - 首次启动应用时，点击"开始显示悬浮窗"
   - 系统会弹出悬浮窗权限请求，点击"允许"
   - 返回应用，悬浮窗会自动显示

4. **控制悬浮窗**
   - **开始显示**: 点击"开始显示悬浮窗"按钮
   - **停止显示**: 点击"停止悬浮窗"按钮
   - 也可以通过通知栏返回应用

## 配置说明

### APP内调整（推荐）

所有设置都可以在APP内通过按钮调整，**设置会自动保存**：

1. **位置调整**：使用上下左右按钮微调位置
2. **透明度调整**：使用 +/- 按钮调整透明度（10%-100%）
3. **尺寸调整**：使用宽度/高度按钮调整图片大小

### 代码调整（高级）

如需修改默认值或步长，可在代码中调整：

**透明度步长**（`FloatingWindowService.kt`）：
```kotlin
private const val ALPHA_STEP = 0.05f // 每次调整5%
```

**位置步长**：
```kotlin
private const val MOVE_STEP = 20 // 每次移动20像素
```

**尺寸步长**：
```kotlin
private const val SIZE_STEP = 50 // 每次调整50像素
```

## 权限说明

应用需要以下权限：

- `SYSTEM_ALERT_WINDOW`: 悬浮窗权限（必须手动授权）
- `FOREGROUND_SERVICE`: 前台服务权限
- `FOREGROUND_SERVICE_SPECIAL_USE`: 特殊用途前台服务（Android 14+）
- `WAKE_LOCK`: 唤醒锁定（保持服务运行）

## 注意事项

1. **首次使用**: 必须授予悬浮窗权限，否则无法显示
2. **电池优化**: 建议将应用加入电池优化白名单，避免被系统杀死
3. **MIUI系统**: 红米手机可能需要额外在"应用管理"中开启"显示在其他应用的上层"权限
4. **Android 15**: 已适配Android 15的新权限模型

## 项目结构

```
app/
├── src/main/
│   ├── java/com/floatingoverlay/
│   │   ├── MainActivity.kt          # 主界面
│   │   └── FloatingWindowService.kt # 悬浮窗服务
│   ├── res/
│   │   ├── drawable/
│   │   │   └── ganjiang_aiming.png  # 悬浮显示的图片
│   │   ├── layout/
│   │   │   └── activity_main.xml    # 主界面布局
│   │   └── values/                  # 资源文件
│   └── AndroidManifest.xml          # 应用清单
└── build.gradle                     # 构建配置
```

## 开发环境

- Android Studio Hedgehog | 2023.1.1 或更高版本
- JDK 8 或更高版本
- Android SDK 26+ (最低支持)
- Android SDK 35 (目标版本)
- Kotlin 1.9.22

## 常见问题

**Q: 悬浮窗不显示？**
A: 检查是否已授予悬浮窗权限，在设置->应用->悬浮窗显示->权限中查看

**Q: 应用被系统杀死？**
A: 将应用加入电池优化白名单，并在MIUI中关闭"后台限制"

**Q: 图片显示不正确？**
A: 检查图片是否已正确复制到 `app/src/main/res/drawable/` 目录

**Q: 如何调整图片大小？**
A: 修改 `FloatingWindowService.kt` 中的 `width` 和 `height` 参数

## 许可证

本项目仅供学习使用。

