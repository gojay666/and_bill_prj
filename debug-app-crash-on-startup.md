# 调试会话: app-crash-on-startup

**状态**: [OPEN]
**现象**: 应用启动时显示 Android Logo 几秒后闪退到桌面
**环境**: 真机/模拟器 Android 8.0+, Debug APK

## 假设

| # | 假设 | 验证状态 |
|---|------|---------|
| H1 | LoginActivity.onCreate() 中某个步骤抛出未捕获异常 | 待验证 |
| H2 | AppPreferences 构造函数在 EncryptedSharedPreferences 初始化时崩溃 | 待验证 |
| H3 | ViewBinding 生成的 ActivityLoginBinding 因布局 ID 不匹配导致 NullPointerException | 待验证 |
| H4 | AuthViewModel 初始化时 Room Database 打开失败 | 待验证 |
| H5 | checkSavedLoginState() 跳转到 MainActivity，但 MainActivity 崩溃 | 待验证 |

## 插桩计划

在 LoginActivity.onCreate() 的每个关键步骤前后添加 try-catch 日志上报。

## 日志

| 时间 | 事件 | 详情 |
|------|------|------|
| - | - | - |

## 结论

待定
