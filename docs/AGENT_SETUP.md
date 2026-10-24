# Windows 环境准备

本项目作为独立 Git 仓库放在宿主工作区的 apps 目录下。使用宿主的 [环境安装指南](../../../docs/AGENT_SETUP.md) 安装工具；不要在本仓库重复安装工具或恢复旧 scripts 目录。

构建与日志使用本仓库根目录的 windows_build.bat、windows_simulation.bat，具体说明见 [工作区开发说明](../WINDOWS_WORKSPACE.md)。SDK 路径写入本仓库 local.properties，服务配置保持在本仓库内；工具与缓存由宿主共用。

脱离宿主工作区独立使用时，需要重新提供工具环境和启动入口。docs/superpowers 中的旧计划是历史记录，其中的 ./gradlew 命令不是当前 Windows 入口。
