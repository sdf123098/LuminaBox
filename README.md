<div align="center">
  <img src="common/src/main/resources/assets/luminabox/icon.png" alt="LuminaBox Icon" width="128"/>
  <h1>LuminaBox</h1>
  <p>A Minecraft mod that redefines your auditory experience with custom local and online music playback.</p>

  [**English**](README.md) | [**简体中文**](README_zh.md)
</div>

---

## 🎵 Introduction
**LuminaBox** is a versatile background music manager mod for Minecraft. Whether you want to replace the vanilla background music with your own local MP3s, or stream music directly from online platforms like Bilibili, LuminaBox provides a sleek, in-game UI to manage it all.

## ✨ Features
- **In-Game Music Player:** A fully-featured, draggable, and scalable UI to control playback, volume, and playlists without leaving the game.
- **Local Audio Support:** Add your own music tracks from your computer directly into your Minecraft world.
- **Online Streaming:** Search and stream music from Bilibili natively in-game (powered by LavaPlayer).
- **Custom Playlists:** Create and manage both local and online playlists effortlessly.
- **Environmental Triggers:** Automatically switch background music based on your environment, biomes, or server events.
- **Vanilla Override:** Optionally replace all vanilla Minecraft background music with your curated tracks.
- **Multi-Loader Support:** Available for both **Fabric** and **NeoForge**.
- **Multi-Language Support:** Fully translated into 9 languages.

## 📥 Installation

### Requirements
- Minecraft 1.21.x
- [Fabric Loader](https://fabricmc.net/) OR [NeoForge](https://neoforged.net/)

### Setup
1. Download the latest `luminabox-[loader]-[version].jar` release.
2. Drop the `.jar` file into your `.minecraft/mods` folder.
3. Launch the game and enjoy!

## 🎮 Usage

1. **Open the Player:** Press the designated hotkey (configurable in controls) or click the LuminaBox icon to open the Music Player UI.
2. **Local Music:** Navigate to the Local Music tab and add files from your computer.
3. **Online Music:** Navigate to the Search tab, search for your favorite tracks, and click the `[+]` button to add them to your Online Playlist.
4. **Settings:** Toggle features like Vanilla Music Replacement and Debug Logging in the Settings tab.

## 🌐 Network Proxy (For Online Streaming)
If you experience `SocketTimeoutException` or connectivity issues when streaming online music (especially Bilibili), you can configure an HTTP Proxy:
1. Open the **Settings** tab in the LuminaBox UI.
2. Enter your Proxy **Host** (e.g., `127.0.0.1`) and **Port** (e.g., `7890`).
3. Click **Save Proxy**.
4. **Restart Minecraft** for the proxy settings to take effect.

## 🛠️ Building from Source
To build LuminaBox yourself:
```bash
git clone https://github.com/yourusername/LuminaBox.git
cd LuminaBox
./gradlew clean build
```
The compiled jars will be located in `fabric/build/libs/` and `neoforge/build/libs/`.

## 📄 License
This project is licensed under the MIT License.
