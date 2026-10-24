# XYJ Android Framework

用于 Windows 原生开发的 Android 项目，包含语音辅导、拍照批卷与相关构建配置。

本仓库保持独立 Git，Windows 入口依赖上两级宿主的 scripts 目录；脱离该工作区后不能直接使用这两个入口。详见 [工作区开发说明](WINDOWS_WORKSPACE.md)。

## 安装环境

让 Agent 按 [环境安装手册](docs/AGENT_SETUP.md) 执行，也可直接复制下面的提示词。

需要 JDK 17、Android SDK 36、Build Tools 35.0.0、platform-tools 和 Gradle 8.14.3。工具统一放入宿主工作区 `.tools/`，不需要 Android Studio；工具和缓存不提交 Git。

## 给 Agent 的安装提示词

复制以下内容发送给 Agent：

```text
请为当前项目安装并验证 Windows 开发环境。先阅读 README.md 和 docs/AGENT_SETUP.md，然后实际执行安装，不要只给我操作建议。

将 JDK、Android SDK、platform-tools（包含 adb）和本地 Gradle 放在宿主工作区 .tools 目录，设置隐藏属性并确认 Git 忽略；使用项目内工具，不依赖其他盘的已有安装。不修改系统长期环境变量或执行策略。已有完整安装先检查再复用，不覆盖未知文件。

下载使用官方来源并核验校验值，检查每步退出码。自动下载失败时尝试官方渠道；若必须由浏览器下载，明确告诉我下载地址和保存位置，不使用残缺文件。

运行 windows_build.bat -BuildOnly，确认构建成功并检查 APK。然后检测手机；有已授权设备时安装验证，无设备或未授权则如实报告。不要擅自卸载应用、清除数据、替换签名密钥或修改系统驱动。

遇到问题修正通用安装步骤，但文档不要写入本机绝对路径、设备序列号、个人信息或本次调试流水账。最后报告安装内容、构建结果、设备验证结果和未完成项。
```

## 日常使用

在项目根目录打开 PowerShell：

```powershell
# 编译并安装到手机
.\windows_build.bat
# 仅编译
.\windows_build.bat -BuildOnly
# 查看应用日志（先在手机打开应用，Ctrl+C 停止）
.\windows_simulation.bat
```

多台设备时追加 `-Serial 设备序列号`。两个入口仅为本次进程放行 PowerShell 脚本，不修改系统长期执行策略。

产物位于 `app/build/outputs/apk/debug/app-debug.apk`。编译成功不代表安装成功；安装失败应查看前面的 adb 原始错误。日志入口不会启动模拟器。

## 项目结构

- `app/src/main/`：应用代码、清单与资源。
- `app/build.gradle.kts`：应用构建配置。
- `gradle/`：依赖版本与 Gradle Wrapper 文件。
- `windows-host.json`：宿主构建入口使用的任务和产物配置。
- `docs/`：Agent 环境安装指南。
- `version.properties`：应用版本。
- 工具与下载包：共用宿主 `.tools/`。
- Gradle 缓存：共用宿主 `.gradle-home/`。

## 豆包语音辅导配置

辅导页已接入火山引擎豆包 Dialog 语音 SDK。鉴权参数不要提交到仓库，建议写在本机 `local.properties`：

```properties
doubao.appId=你的 AppID
doubao.token=你的 Access Token
```

可选配置：

```properties
doubao.uid=student-001
doubao.appKey=默认使用 doubao.appId，通常不用填
doubao.resourceId=volc.speech.dialog
doubao.dialogAddress=wss://openspeech.bytedance.com
doubao.dialogUri=/api/v3/realtime/dialogue
doubao.botName=豆包
doubao.aecModelPath=/absolute/path/to/aec.model
doubao.debugPath=/absolute/path/to/existing/log/dir
doubao.recorderPath=/absolute/path/to/existing/recorder/dir
doubao.playerPath=/absolute/path/to/existing/player/dir
doubao.logLevel=WARN
```

如果控制台只显示 `APP ID`、`Access Token`、`Secret Key`，这里先只填 `APP ID` 和 `Access Token`。当前 SDK 报错中的 `X-Api-App-Key` 期望值是 `APP ID`，所以 `doubao.appKey` 默认也会使用 `doubao.appId`；不要把 `Secret Key` 填到 `doubao.appKey`，否则会出现 `invalid X-Api-App-Key`。

不配置 `doubao.appId`、`doubao.token` 时，辅导页会保留题目和本地引导文案，但语音会话会显示配置缺失提示。开启 AEC 时需要额外提供文档里的 `aec.model` 文件路径。

## 豆包多模态批卷配置

批卷页使用后置摄像头预览，翻转进入批卷页后等待 5 秒自动拍照。配置豆包多模态参数后，App 会按 Responses API 把答案图片、题干、标准答案和批卷提示词发送给模型；未配置时批卷会失败并提示配置缺失。

接口触发条件、请求结构和返回示例见 [大模型接口设计](docs/05-llm-api-design.md)。

建议写入本机 `local.properties`：

```properties
doubao.multimodal.apiKey=你的火山方舟 API Key
doubao.multimodal.model=你的多模态模型或 Endpoint ID
doubao.multimodal.endpoint=https://ark.cn-beijing.volces.com/api/v3/responses
```

图片按文档使用 `input_image.image_url`，传入 `data:image/jpeg;base64,...` 形式；输出使用 `text.format.type=json_schema`，字段为 `score`、`feedback`、`reason`、`suggestion`。

## 豆包辅导会话使用方式

```
启动会话
-> SESSION_STARTED
-> 播放开场白 SayHello
-> 后面就靠学生语音和 AI 对话继续
```
- App 在 `StartSession.dialog.system_role` 写入本题上下文和辅导规则。
- `system_role` 包含阶段、题目来源、标题、题干、知识点、辅导要求和“不要直接给答案”等规则。
- 固定辅导提示词从 `app/src/main/assets/prompts/` 下的 txt 文件读取。
- `tutoring_context_prompt.txt` 渲染 `system_role`，`tutoring_speaking_style.txt` 渲染 `speaking_style`。
- 新会话启动成功后，App 通过 `tutoring_opening_prompt.txt` 播放开场白，不再朗读完整题干。
- 后面靠学生语音和 AI 对话继续。
- 同一题停留期间不会反复设置上下文；切到下一题会重建会话并设置下一题上下文。

## 常见问题

- 缺少 Java 或 SDK：按安装手册检查目录层级。
- Gradle 自动下载失败：支持浏览器下载 ZIP，再校验并使用本地包，详见安装手册。
- adb 列表为空：检查 USB 链路、调试授权与 Windows 驱动，不等同于编译环境失败。
- 签名不一致：复用原调试密钥；卸载旧应用会清除数据，不应自动卸载。

## TODO

- 进入错题讲解后根据错题的类型，举一反三出同类型的新试题
- USB接口
