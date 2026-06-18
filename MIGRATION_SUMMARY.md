# LuminaBox 1.21.1 迁移工作总结

## 项目结构
- **源版本**: Minecraft 26.1.2
- **目标版本**: Minecraft 1.21.1
- **项目类型**: 多平台模组 (common, fabric, neoforge)
- **主要功能**: 自定义背景音乐播放器

## 已完成工作

### 1. 项目复制
- 已创建 `LuminaBox-1.21.1` 文件夹
- 已复制所有项目文件（排除 .git, build, .gradle, .codegraph）

### 2. CodeGraph 索引建立
- 已初始化 CodeGraph
- 已建立全量索引（56个文件，960个节点，904条边）

### 3. API 使用分析
通过 CodeGraph 查询，发现以下关键API使用：

#### 网络API
- `CustomPacketPayload` - 用于自定义数据包
- `StreamCodec` - 用于数据序列化
- `PayloadTypeRegistry` - Fabric API
- `RegisterPayloadHandlersEvent` - NeoForge API

#### GUI API
- `GuiGraphicsExtractor` - 26.x新增的渲染类
- `MouseButtonEvent` - 26.x新增的鼠标事件类
- `Screen` - 基础GUI类
- `Button` - 按钮组件

#### 资源定位
- `Identifier.fromNamespaceAndPath()` - 26.x的资源定位方式

#### Mixin目标
- `MusicManager` - 音乐管理器
- `PauseScreen` - 暂停屏幕

## 主要迁移挑战

### 1. 高风险项
- **网络API**: `StreamCodec`、`CustomPacketPayload` 在1.21.1中可能不存在或API不同
- **GUI API**: `GuiGraphicsExtractor`、`MouseButtonEvent` 在1.21.1中不存在
- **资源定位**: `Identifier.fromNamespaceAndPath()` 需要改为 `new ResourceLocation()`

### 2. 中等风险项
- **Mixin目标**: 方法签名可能有变化
- **构建系统**: 需要降级多个组件

### 3. 低风险项
- **依赖库**: LavaPlayer等第三方库应该兼容
- **配置系统**: 基础JSON配置应该兼容

## 预计工作量

| 阶段 | 工作内容 | 预计时间 |
|------|----------|----------|
| 1 | 构建系统配置 | 1-2小时 |
| 2 | API适配 | 3-4小时 |
| 3 | 依赖库更新 | 1-2小时 |
| 4 | 功能测试 | 2-3小时 |
| **总计** | | **7-11小时** |

## 下一步行动

1. **立即执行**: 修改 `gradle.properties` 和 `build.gradle.kts`
2. **优先处理**: 网络API适配（影响最大）
3. **并行处理**: GUI API适配
4. **最后验证**: 功能测试

## 风险评估
- **成功概率**: 70-80%
- **主要风险**: API变化可能导致重写部分代码
- **缓解措施**: 参考1.21.1的Fabric/NeoForge文档，逐步验证

## 建议
1. 创建Git分支进行迁移试验
2. 先降级构建系统，确保能编译
3. 逐个修复API兼容性问题
4. 每完成一个功能模块就进行测试
