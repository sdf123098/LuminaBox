# LuminaBox 1.21.1 迁移计划

## 第一阶段：构建系统配置

### 1. 修改 gradle.properties
```properties
# Gradle
org.gradle.jvmargs=-Xmx2G
org.gradle.daemon=true
org.gradle.java.home=C:/Program Files/Zulu/zulu-17
org.gradle.java.installations.paths=C:/Program Files/Zulu/zulu-17

# Minecraft - 支持 1.21.1
minecraft_version=1.21.1
minecraft_version_range=[1.21.1,1.21.2)
minecraft_version_range_neoforge=[1.21.1,1.21.2)

# Fabric
fabric_loader_version=0.15.11
fabric_api_version=0.100.4+1.21.1
fabric_loom_version=1.7-SNAPSHOT

# NeoForge
neoforge_version=21.0.167
neoforge_version_range=[21.0,)
neoforge_loader_version_range=[4,)

# Mod
mod_version=1.0.0
maven_group=com.luminabox
archives_base_name=luminabox-common
mod_id=luminabox
mod_name=Dynamic BGM
mod_license=MIT
mod_description=Custom background music mod for Minecraft
mod_author=T.H.E Herta
mod_url=https://github.com/luminabox/luminabox

# Dependencies
java_version=17
jlayer_version=1.0.1
vorbisspi_version=1.0.3-2
jorbis_version=0.0.17-2
tritonus_version=0.3.7-2
tritonus_all_version=0.3.7-1
jflac_version=1.5.2
jaad_version=0.9.4
mp3spi_version=1.9.5.4
```

### 2. 修改 build.gradle.kts (根目录)
```kotlin
plugins {
    java
    id("net.fabricmc.fabric-loom") version "1.7-SNAPSHOT" apply false
    id("net.neoforged.moddev") version "2.0.141" apply false
}
```

## 第二阶段：API适配

### 1. 网络API修改

#### PlayMusicPayload.java
```java
package com.luminabox.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PlayMusicPayload(String trackId, String sourceUrl, boolean isPlay) implements CustomPacketPayload {
    public static final Type<PlayMusicPayload> TYPE = new Type<>(new ResourceLocation("luminabox", "play_music"));

    public static final StreamCodec<FriendlyByteBuf, PlayMusicPayload> CODEC = StreamCodec.of(
        (buf, val) -> {
            buf.writeUtf(val.trackId);
            buf.writeUtf(val.sourceUrl);
            buf.writeBoolean(val.isPlay);
        },
        buf -> new PlayMusicPayload(buf.readUtf(), buf.readUtf(), buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
```

### 2. GUI API修改

#### MusicPlayerScreen.java
- 将 `GuiGraphicsExtractor` 替换为 `GuiGraphics`
- 将 `MouseButtonEvent` 替换为标准鼠标事件处理
- 更新渲染方法签名

### 3. Mixin验证

#### MusicManagerMixin.java
- 验证 `MusicManager` 类的方法签名
- 验证 `tick()` 方法是否存在
- 验证 `stopPlaying()` 方法是否存在

#### PauseScreenMixin.java
- 验证 `PauseScreen` 类的方法签名
- 验证 `init()` 方法是否存在

## 第三阶段：依赖库更新

### 1. LavaPlayer 验证
- 验证 LavaPlayer 2.2.6 是否兼容 Java 17
- 验证 Bilibili 插件兼容性

### 2. 其他依赖
- 验证所有第三方库的兼容性
- 更新版本号（如需要）

## 第四阶段：功能测试

### 1. 基础功能
- [ ] 模组加载
- [ ] 配置文件读取
- [ ] 快捷键注册

### 2. 音频功能
- [ ] 本地音乐播放
- [ ] 网络流媒体播放
- [ ] 音量控制
- [ ] 播放模式切换

### 3. GUI功能
- [ ] 音乐播放器界面
- [ ] 标签页切换
- [ ] 搜索功能
- [ ] 播放列表管理

### 4. 网络功能
- [ ] 服务器同步
- [ ] 文件上传
- [ ] 播放列表同步

## 预计时间
- **构建系统配置**: 1-2小时
- **API适配**: 3-4小时
- **依赖库更新**: 1-2小时
- **功能测试**: 2-3小时
- **总计**: 7-11小时

## 风险点
1. **网络API变化**: 可能需要重写网络层
2. **GUI API变化**: 可能需要重写GUI渲染
3. **Mixin目标变化**: 可能需要调整注入点
4. **依赖库兼容性**: 可能需要寻找替代方案
