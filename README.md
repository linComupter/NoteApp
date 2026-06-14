# NoteApp

一款基于 Kotlin Multiplatform + Compose Multiplatform 开发的跨平台便签应用，支持 Desktop（Windows/macOS/Linux）和 Android。

## 功能特性

- 卡片式便签列表，支持彩色主题
- 富文本编辑器（WYSIWYG）与 Markdown 源码模式自由切换
- 本地 SQLite 持久化存储
- 支持新建、编辑、删除便签
- 预留暗色模式接口

## 技术栈

| 技术 | 说明 |
|---|---|
| Kotlin Multiplatform | 跨平台业务逻辑共享 |
| Compose Multiplatform 1.8.0 | 跨平台 UI |
| SQLDelight 2.0.2 | 本地 SQLite 数据库 |
| Koin 4.0.0 | 依赖注入 |
| richeditor-compose | 富文本编辑器 |
| multiplatform-markdown-renderer | Markdown 渲染 |
| Jetpack Navigation Compose | 页面导航 |

## 项目结构

```
NoteApp/
├── shared/          # KMP 共享模块：领域模型、用例、数据库、DI
└── composeApp/      # Compose 多平台 UI 模块：页面、ViewModel、主题、导航
```

## 环境要求

- JDK：**JetBrains Runtime (JBR) 17**（标准 JDK 会导致 Skiko 原生库崩溃）
- Android SDK：API 24+
- Gradle：8.11+

### 安装 JBR 17

在 IntelliJ IDEA 中：**File → Project Structure → SDKs → + → Download JDK → Vendor: JetBrains Runtime → Version: 17**

下载完成后，在 `gradle.properties` 中配置：

```properties
org.gradle.java.home=C:/Users/<用户名>/.jdks/jbr-17.x.x.x
```

## 构建与运行

```bash
# 运行 Desktop 版本
./gradlew :composeApp:run

# 构建 Android APK
./gradlew :composeApp:assembleDebug

# 打包 Desktop 可执行目录（无需安装程序）
./gradlew :composeApp:createDistributable

# 运行测试
./gradlew :shared:commonTest
./gradlew :composeApp:commonTest
```

## 截图

> Desktop 版本运行效果

![便签列表](docs/screenshots/home.png)
![编辑页面](docs/screenshots/edit.png)

## License

MIT
