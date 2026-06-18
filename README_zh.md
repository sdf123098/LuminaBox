<div align="center">
  <img src="common/src/main/resources/assets/luminabox/icon.png" alt="LuminaBox Icon" width="128"/>
  <h1>流光音匣 (LuminaBox)</h1>
  <p>一款能够重新定义你的听觉体验、支持本地与网络音乐播放的 Minecraft 模组。</p>

  [**English**](README.md) | [**简体中文**](README_zh.md)
</div>

---

## 🎵 简介
**流光音匣 (LuminaBox)** 是一款功能强大的 Minecraft 背景音乐管理模组。无论你是想用本地 MP3 替换原版背景音乐，还是想直接在游戏内搜索并播放 Bilibili 的网络音乐，流光音匣都能通过其精美的游戏内 UI 面板为你提供一站式体验。

## ✨ 功能特性
- **游戏内音乐播放器：** 拥有一个功能齐全、可自由拖拽与缩放的自定义 UI，无需切出游戏即可控制播放、音量和列表。
- **本地音频支持：** 直接将电脑中的音乐文件添加到你的 Minecraft 世界中播放。
- **网络流媒体支持：** 原生支持在游戏内搜索并播放 Bilibili 的音乐（基于 LavaPlayer）。
- **自定义播放列表：** 轻松创建和管理你的本地与网络播放列表。
- **环境触发机制：** 根据你所处的环境、群系或服务器事件自动切换合适的背景音乐。
- **原版音乐替换：** 可选开启完全覆盖并替换 Minecraft 原版的背景音乐。
- **多加载器支持：** 同时支持 **Fabric** 与 **NeoForge**。
- **多语言支持：** 已支持包括简体中文、繁体中文、英文、日语等 9 种语言。

## 📥 安装指南

### 运行需求
- Minecraft 1.21.x
- [Fabric Loader](https://fabricmc.net/) 或 [NeoForge](https://neoforged.net/)

### 安装步骤
1. 下载最新的 `luminabox-[loader]-[version].jar` 文件。
2. 将该 `.jar` 文件放入你的 `.minecraft/mods` 文件夹中。
3. 启动游戏即可体验！

## 🎮 使用说明

1. **打开播放器：** 按下绑定的快捷键（可在控制中设置）或点击流光音匣图标，打开音乐播放器 UI。
2. **本地音乐：** 切换到“本地音乐”选项卡，添加你电脑上的音频文件。
3. **网络音乐：** 切换到“搜索”选项卡，输入关键字搜索你喜欢的音乐，点击 `[+]` 按钮将其添加到网络播放列表中。
4. **模组设置：** 在“设置”选项卡中可以开启/关闭原版音乐替换，以及查看音频调试日志等。

## 🌐 网络代理设置（用于网络音乐）
如果你在播放网络音乐（尤其是 Bilibili）时遇到 `SocketTimeoutException` 或连接超时报错，可以通过配置 HTTP 代理解决：
1. 打开流光音匣 UI 中的 **设置 (Settings)** 选项卡。
2. 在 HTTP 代理一栏输入你的代理 **主机地址 (Host)**（例如 `127.0.0.1`）和 **端口 (Port)**（例如 `7890`）。
3. 点击 **保存**。
4. **重启 Minecraft 客户端**，代理设置即刻生效。

## 🛠️ 从源码构建
如果你想自行编译流光音匣：
```bash
git clone https://github.com/yourusername/LuminaBox.git
cd LuminaBox
./gradlew clean build
```
编译成功后，打包好的 jar 文件将位于 `fabric/build/libs/` 和 `neoforge/build/libs/` 目录下。

## 📄 开源协议
本项目采用 MIT 开源协议。
