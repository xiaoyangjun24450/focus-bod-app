# 在 XYJ 工作区开发

本目录是独立 Git 仓库，origin 为 `git@github.com:xiaoyangjun24450/focus-bod-app.git`，适配基线为 `e734d94d8c5c0d54798542e4b5ea97b90d8b3f50`。保留原源码、历史、依赖与模块；宿主不跟踪本目录，提交/推送需在这里操作。

放置于宿主 `apps/focus-bod-app/` 后，使用宿主环境指南安装 `.tools` 中的工具。运行：

```powershell
.\windows_build.bat -BuildOnly
.\windows_build.bat
.\windows_simulation.bat
```

支持 `-Serial`。入口调用宿主 scripts/windows_repository.ps1，构建本仓库 `:app:assembleDebug`，产物为 `app/build/outputs/apk/debug/app-debug.apk`，日志从 APK 元数据读取包名。原包名 com.xyj.focuspod 保留。Gradle 依赖和版本文件仍由本仓库管理。

local.properties 位于本仓库根目录，SDK 路径由入口更新，其他配置保留。服务凭据参见原 README，使用占位符配置说明，不提交真实凭据。缺少服务配置仍可编译，但不能验证在线语音/批卷。

MCU 是源仓库已有独立子模块，Android 编译不依赖其初始化；本次未初始化，硬件联调未验证。源提交未发现仓库级 LICENSE/NOTICE，不能推断公开再分发授权。

适配后的两个 Windows 入口统一调用宿主脚本；本仓库旧 scripts 目录已删除，并更新了安装说明。windows-host.json 保存构建目标。源业务代码保持不变；运行时摄像头、麦克风、在线服务和硬件功能需要另行验收。
