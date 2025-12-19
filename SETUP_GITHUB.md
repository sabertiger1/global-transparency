# GitHub Actions 自动编译设置指南

## 前置要求

由于项目需要 Gradle Wrapper，如果本地没有，需要先初始化：

### 方法1：使用Android Studio（推荐）
1. 用 Android Studio 打开项目
2. Android Studio 会自动下载并配置 Gradle Wrapper

### 方法2：手动生成 Gradle Wrapper
```bash
# 如果已安装 Gradle
gradle wrapper --gradle-version 8.2

# 或者使用已安装的 Gradle
gradle wrapper
```

这会生成以下文件：
- `gradlew` (Unix/Linux/Mac)
- `gradlew.bat` (Windows)
- `gradle/wrapper/gradle-wrapper.jar`
- `gradle/wrapper/gradle-wrapper.properties` (已创建)

## GitHub Actions 工作流

项目已配置 GitHub Actions 自动编译，工作流文件位于：
`.github/workflows/android-build.yml`

### 触发条件
- 推送到 `main` 或 `master` 分支
- 创建 Pull Request
- 手动触发（workflow_dispatch）

### 编译产物
编译完成后，APK 文件会作为 Artifact 上传，可在 Actions 页面下载：
- 路径：`app/build/outputs/apk/debug/app-debug.apk`
- 保留时间：30天

## 使用步骤

1. **初始化 Git 仓库**（如果还没有）
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   ```

2. **创建 GitHub 仓库**
   - 在 GitHub 上创建新仓库
   - 不要初始化 README、.gitignore 或 license（项目已有）

3. **推送代码**
   ```bash
   git remote add origin https://github.com/你的用户名/仓库名.git
   git branch -M main
   git push -u origin main
   ```

4. **查看编译结果**
   - 访问 GitHub 仓库的 Actions 标签页
   - 点击最新的工作流运行
   - 在 Artifacts 部分下载 APK

## 注意事项

1. **Gradle Wrapper 文件**
   - `gradlew` 和 `gradlew.bat` 需要可执行权限
   - 如果这些文件不存在，GitHub Actions 会失败
   - 建议在本地用 Android Studio 打开一次项目，自动生成这些文件

2. **签名配置**
   - 当前配置为 Debug 版本，无需签名
   - 如需 Release 版本，需要配置签名密钥（建议使用 GitHub Secrets）

3. **依赖下载**
   - GitHub Actions 会自动下载所有依赖
   - 首次编译可能需要较长时间

## 故障排除

### 问题：Gradle Wrapper 找不到
**解决**：确保 `gradlew` 和 `gradlew.bat` 文件存在并已提交到仓库

### 问题：编译失败
**解决**：
1. 检查 Actions 日志查看具体错误
2. 确保所有依赖版本兼容
3. 检查 Android SDK 版本要求

### 问题：APK 未生成
**解决**：
1. 检查编译日志确认是否成功
2. 确认 Artifacts 上传步骤执行成功
3. 检查文件路径是否正确

