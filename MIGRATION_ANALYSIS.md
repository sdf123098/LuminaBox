# LuminaBox 迁移到 Minecraft 1.21.1 分析报告

## 当前状态
- **源版本**: Minecraft 26.1.2
- **目标版本**: Minecraft 1.21.1
- **项目结构**: 多平台模组 (common, fabric, neoforge)

## 主要API差异

### 1. 网络API (高风险)

#### 当前实现
```java
// CustomPacketPayload.java
public record PlayMusicPayload(String trackId, String sourceUrl, boolean isPlay) implements CustomPacketPayload {
    public static final Type<PlayMusicPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("luminabox", "play_music"));
    public static final StreamCodec<FriendlyByteBuf, PlayMusicPayload> CODEC = StreamCodec.of(...);
}
```

#### 1.21.1 差异
- `CustomPacketPayload` 接口在1.21.1中存在，但API可能有变化
- `StreamCodec` 在1.21.1中可能不存在或API不同
- `Identifier.fromNamespaceAndPath()` 可能需要改为 `new ResourceLocation()`

#### 需要修改的文件
- `common/src/main/java/com/luminabox/network/PlayMusicPayload.java`
- `common/src/main/java/com/luminabox/network/FileUploadPayload.java`
- `common/src/main/java/com/luminabox/network/SyncPlaylistPayload.java`
- `fabric/src/main/java/com/luminabox/fabric/LuminaBoxFabric.java`
- `neoforge/src/main/java/com/luminabox/neoforge/LuminaBoxNeoForge.java`

### 2. GUI API (中等风险)

#### 当前实现
```java
// MusicPlayerScreen.java
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
```

#### 1.21.1 差异
- `GuiGraphicsExtractor` 在1.21.1中不存在，需要改为 `GuiGraphics`
- `MouseButtonEvent` 在1.21.1中不存在，需要使用不同的鼠标事件处理方式
- 渲染方法签名可能有变化

#### 需要修改的文件
- `common/src/main/java/com/luminabox/client/gui/MusicPlayerScreen.java`

### 3. Mixin目标 (中等风险)

#### 当前Mixin
- `MusicManagerMixin` - 注入到 `net.minecraft.client.sounds.MusicManager`
- `PauseScreenMixin` - 注入到 `net.minecraft.client.gui.screens.PauseScreen`

#### 1.21.1 差异
- 这些类在1.21.1中应该存在
- 方法签名可能有变化（如 `tick()` 方法）
- 需要验证具体的注入点

### 4. 构建系统 (高风险)

#### 当前配置
- Minecraft: 26.1.2
- Fabric Loom: 1.17.11
- NeoForge: 26.1.2.76
- Java: 25

#### 1.21.1 配置
- Minecraft: 1.21.1
- Fabric Loom: 1.7.x 或 1.8.x
- NeoForge: 21.0.x
- Java: 17 或 21

## 迁移步骤

### 第一阶段：构建系统降级
1. 修改 `gradle.properties` 中的版本号
2. 降级 `build.gradle.kts` 中的插件版本
3. 修改 Java 版本要求

### 第二阶段：API适配
1. 网络API适配
   - 替换 `StreamCodec` 为1.21.1兼容的序列化方式
   - 修改 `Identifier` 为 `ResourceLocation`
   - 更新 Payload 注册代码

2. GUI API适配
   - 替换 `GuiGraphicsExtractor` 为 `GuiGraphics`
   - 修改鼠标事件处理
   - 更新渲染方法

3. Mixin验证
   - 验证 `MusicManager` 类的方法签名
   - 验证 `PauseScreen` 类的方法签名
   - 调整注入点

### 第三阶段：依赖库更新
1. 验证 LavaPlayer 兼容性
2. 验证其他第三方库兼容性
3. 更新依赖版本

### 第四阶段：功能测试
1. 测试本地音乐播放
2. 测试网络流媒体
3. 测试GUI界面
4. 测试快捷键
5. 测试配置系统

## 预计工作量
- **构建系统降级**: 2-3小时
- **API适配**: 4-6小时
- **依赖库更新**: 1-2小时
- **功能测试**: 2-3小时
- **总计**: 9-14小时

## 风险评估
- **高风险**: 网络API、构建系统
- **中等风险**: GUI API、Mixin目标
- **低风险**: 依赖库、配置系统

## 建议
1. 先创建一个分支进行迁移试验
2. 逐步降级构建系统
3. 逐个验证功能模块
4. 参考1.21.1的Fabric/NeoForge文档
