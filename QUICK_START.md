# 快速开始指南

## 功能确认

✅ **图片位置调整**：APP内有上下左右四个按钮，可以微调位置  
✅ **透明度调整**：APP内有增加/减少透明度按钮，实时显示当前值  
✅ **图片尺寸调整**：APP内有宽度和高度调整按钮，分别控制图片大小  
✅ **自动保存**：所有调整都会自动保存到SharedPreferences，重启APP后自动恢复

## 使用步骤

### 1. 首次使用

1. 安装APK到手机
2. 打开应用，点击"开始显示悬浮窗"
3. 授予悬浮窗权限（系统会弹出提示）
4. 悬浮窗会自动显示

### 2. 调整设置

所有调整都在APP主界面完成：

- **位置调整**：点击 ↑ ↓ ← → 按钮微调位置
- **透明度调整**：点击 +/- 按钮调整透明度，实时显示百分比
- **尺寸调整**：
  - 宽度调整：点击"增加宽度 +"或"减小宽度 -"
  - 高度调整：点击"增加高度 +"或"减小高度 -"
  - 实时显示当前尺寸（像素）

### 3. 设置自动保存

- 每次调整后，设置会自动保存
- 关闭APP后重新打开，设置会自动恢复
- 无需手动保存操作

## GitHub Actions 编译

### 前提条件

项目需要 Gradle Wrapper 文件才能使用 GitHub Actions。如果这些文件不存在：

1. **使用 Android Studio**（推荐）
   - 用 Android Studio 打开项目
   - 会自动生成 `gradlew` 和 `gradlew.bat`

2. **手动生成**
   ```bash
   gradle wrapper --gradle-version 8.2
   ```

### 上传到 GitHub

1. 创建 GitHub 仓库
2. 推送代码：
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git remote add origin https://github.com/你的用户名/仓库名.git
   git push -u origin main
   ```

3. 查看编译结果
   - 访问仓库的 Actions 标签页
   - 等待编译完成
   - 下载 APK 文件

详细说明请参考 [SETUP_GITHUB.md](SETUP_GITHUB.md)




