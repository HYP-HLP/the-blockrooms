# The Blockrooms — 项目进度整理

> 整理时间：2026-08-19（基于当前工作区 + Git 历史）

## 一、项目概况

| 项目 | 内容 |
|---|---|
| 名称 | The Blockrooms（Minecraft 类 Backrooms 模组） |
| 平台 | NeoForge `21.11.44` / Minecraft `1.21.11` / Java 21 / Parchment 2025.12.20 |
| ModID | `blockrooms`，包名 `name.blockrooms` |
| 已提交版本 | HEAD 的 `gradle.properties` = `0.1.6`；最后一次提交 2026-08-17（Merge） |
| 工作区版本 | `0.1.7`（未提交）；已打包 `build/libs/blockrooms-0.1.7.jar`（2026-08-19 22:34） |
| 外部依赖 | JEI `27.30.0.76`（可选依赖，已接入，见第五节） |
| 仓库 | GitHub：HYP-HLP/the-blockrooms（已合并 PR #2~#8） |

## 二、版本历程（约 60 个提交）

- **0.0.1~0.0.5（早期）**：Ruby 工具套装与长矛、加热铁块、BlockLevel0 维度生成器、BlockLevel0 环境音效、穿墙（noclip）传送（0.0.2 错误合成台 error crafting table、0.0.3 探测器火把等 bug 修复）。
- **0.0.6**：新增 BlockLevel4 维度（噪声地形 + y=-50 基岩底 + 抑制洞穴生成）。
- **0.0.7~0.0.8**：BlockLevel4 更多特性、项目符号/方块弹射物（block_projectile / item_projectile）逻辑与代码优化。
- **0.0.9~0.0.10**：唱片弹射物（disc）、GunBow 更多分支。
- **0.1.0**：新增 The Gallery（画廊）维度与嗜血僵尸（Blood-Thirsty Zombie）。
- **0.1.1~0.1.2**：颤抖效果（Trembling）、Blockrooms 版本标题界面。
- **0.1.3（2026-08-13）**：新增 BlockLevel1 维度（含石英 Beta 基地结构），PR #8 合并。
- **0.1.4（2026-08-17，d2b7239「Added New UI」）**：域层信息面板 + 生存难度 HUD 系统入库（见第四节）。
- **0.1.4~0.1.6 修复提交（2026-08-16~17）**：
  - `ed8ed4c` Fix day-night cycle in BlockLevel 1：昼夜传感灯修复起步 + 嗜血僵尸自定义渲染器/新贴图 + 刷怪蛋；
  - `b2d59a5` Fix day-night cycle in BlockLevel 1, again；
  - `74ecf8a` 移除错误合成配方类型中的未用字段；
  - `9e1699c` Extra Update：新增无序错误合成 `ErrorCraftingShapelessRecipe`、BlockLevel3Generator 起步、梯子→木棍配方。
- **0.1.7（工作区，2026-08-19 未提交）**：见第五节。

## 三、已实现内容（HEAD 已提交状态）

### 维度（4 个已注册，见 `ModLevels` / `ModGenerators`）
| 维度 | 生成器 | 说明 |
|---|---|---|
| blocklevel0 | BlockLevel0Generator（程序化扁平） | 5 格高天花板房间：橡木木板+棕色地毯地板、石质天花板、红石灯图案照明，固定生物群系 |
| blocklevel1 | BlockLevel1Generator | 石英大厅/通廊房间 + 昼夜传感灯 + 石英 Beta 基地结构 |
| blocklevel4 | BlockLevel4Generator（继承 NoiseBasedChunkGenerator） | 类主世界地形，基岩底抬升至 y=-50，多噪声生物群系 |
| the_gallery | TheGalleryGenerator | 画廊维度 |

> BlockLevel2 生成器骨架已在 HEAD 内（0.1.4 起步），**工作区已将其完成并注册**（未提交）；BlockLevel3 起步（已提交）；5/6/7/8/Hub 仅骨架。

### 结构（`ModStructures`）
- `bmeg_outpost`（BlockLevel0 前哨站）、`quartz_beta_base`（BlockLevel1 Beta 基地）——均含 Piece。

### 方块（`ModBlocks`）
加热铁块、错误合成台、石制合成台、探测器火把/墙上火把/红石灯、石英电梯、软圆石、加工软圆石

### 物品（`ModItems`）
杏仁水桶、Ruby 工具套装（剑/铲/镐/斧/锄/长矛）、线斧、萤石灯笼、GunBow、嗜血僵尸刷怪蛋

### 实体（`ModEntities`）
item_projectile、block_projectile、blood_zombie（任意光照可生成）

### 音效 / 效果
`music.beginning`、`music.blockrooms.blocklevel1`；trembling（颤抖，负面）

### 核心机制
1. **Noclip 穿墙传送**：窒息伤害触发，按维度+方块路由；也可按 **N 键** 主动触发。
2. **石英电梯**：站上后按跳跃/潜行，向该方向最多 16 格搜索另一块石英电梯并传送。
3. **GunBow**：每 20 tick 15% 概率吞入背包随机物品；右键发射，按弹药类型分发。
4. **动态光照**（客户端）：手持萤石粉泛洪光照，2 个 mixin 注入渲染。
5. **错误合成**：专属配方类型 + 界面；取产物 5% 恶心；不在配方书解锁。
6. **BlockLevel4 规则**：掉落表替换、白天怪物免伤、玩家中毒无限时长、离开清除。
7. **HUD 系统**（0.1.4）：见第四节。

### Mixin（9 个，`blockrooms.mixins.json`）
ChunkGeneratorMixin、StructureManagerMixin、RecipeCraftingHolderMixin、ResultSlotMixin、ResultSlotInvoker、DebugScreenOverlayMixin、EntityRendererMixin、LevelRendererMixin

### 网络协议
`NoclipPayload`（N 键）、`ElevatorTeleportPayload`（bool 方向）——均 `playToServer`，STREAM_CODEC。

## 四、域层信息 + 生存难度 HUD（0.1.4，已提交）

- **域层信息**：进入新域层时右下角逐字打字机显示介绍；尺寸自适应内容，超限出现滚动条（滚轮滚动，打字自动跟随）；显示完停留后淡出。
- **生存难度**：域层内右上角常驻面板：safe / security / entity 三项，可独立配色；离开域层自动隐藏。
- **语言联动**：切换客户端语言自动重载重显。
- **完全数据驱动**：`assets/blockrooms/level_info/<语言>.json`（内置 en_us / zh_cn，当前语言缺失回退 en_us；资源包可覆盖、任意维度可加条目）；所有字段可选，颜色 `#rrggbb` 等三种格式；`difficulty` 缺省 = 该层不显示难度面板。
- **配置**（Client 页 / `client.toml`）：`level_info_enabled / type_speed / line_delay / hold_ticks / panel_width(100-400) / panel_rows(2-12)`、`level_difficulty_enabled`；JSON 内字段优先于全局配置。
- **实现**：`client/hud/LevelInfoData|Manager|Layer`、`client/hud/DifficultyLayer`、`event/client/LevelInfoHandler`、`BlockroomsClient` 注册两个 GuiLayer（`blockrooms:level_info` / `blockrooms:level_difficulty`）。

## 五、当前未提交工作区（0.1.7 阶段，2026-08-19）

> 约 46 个文件改动（+905/-247），部分文件已 `git add` 暂存，大部分仍在工作区。以下按主题整理。

### 1. BlockLevel2「隧道」维度（**模板化生成**，已完成）
- **数据**：`dimension/blocklevel2.json`、`dimension_type/blocklevel2.json`（固定时间、无天空光、雾色 #2e2a26、不可睡觉）、`worldgen/biome/blocklevel2.json`、`loot_table/gameplay/blocklevel2.json`。
- **生成器** `BlockLevel2Generator`（重写）：世界 = **16×16×6 结构模板拼块**（每区块一个 NBT 模板，y=0..5），模板在 **FEATURES 阶段**（`applyBiomeDecoration`，与结构同一阶段）放置；**边哈希**决定四边隧道口开闭（70% 开口，边以「西/北区块+东/南方向」唯一标识，相邻区块必然一致）；`bl2_corridor_x / corner / tjunction / crossroads / deadend` 五个模板（用户搭建），按口模式选模板 + 旋转。
- **关键修复**：`placeInWorld` 旋转绕世界原点导致模板偏移到相邻区块（虚空）——按旋转方向**补偿 offset(+15)** 使 16×16 精确覆盖本区块；`filterBlocks` 是"保留匹配"语义（不能拿全部方块）；模板旧格式（palette+blocks 单调色板）加载正常。
- **容器**：模板内箱子/潜影盒统一写入战利品表 + 空隧道列按哈希兜底生成（箱子 2.3%/潜影盒 0.8%）。
- 调试日志 `BL2-TPL` 已降级 debug。

### 2. 传送方块系统（TeleporterBlock，完成）
- 方块（无碰撞、instabreak、纯黑纹理）+ 物品 + 语言 + 方块实体 `TeleporterBlockEntity`（`ModBlockEntities`）。
- **数据格式**：`targets[]` NBT（`dimension` + 可选 `pos`），与 `STANDARD_TARGET` 同构、兼容结构 NBT，地图作者可直接嵌入；按顺序取第一个有效目标。
- **触发**：实体包围盒重叠即传送（`getEntityInsideCollisionShape` 返回整格 + `entityInside`），每实体 10 tick 冷却（持久数据 `blockrooms.last_teleporter_use`）；玩家走 `TeleportUtils.teleportPlayer`，非玩家实体找安全点。
- **API**：`util/TeleporterApi.java` 提供 5 个重载（目标维度/绝对落点/设置 targets），供结构与事件代码放置传送方块。
- 工具脚本：`docs/gen_black_png.cjs`（无依赖生成纯黑 16×16 RGBA 纹理）、`docs/parse_nbt.cjs`（结构 NBT 解析调试）。

### 3. 新结构（`ModStructures`）
- **oak_exit**（BlockLevel0 橡木出口）：NBT 模板结构（`data/blockrooms/structure/oak_exit.nbt`），1/16 区块、随机旋转、锚点 y=0；structure_set `random_spread spacing=1`。
- **quartz_door / spruce_door**（BlockLevel2 的门，各 1/8 区块）：`BlockLevel2DoorPiece` 生成 3×3×2 门洞——石英门洞后放**指向 BlockLevel1 的传送方块**（dy 0-1 两层，用户改）；云杉门洞后传送方块**目标留空**（The Void 未实现）。
- **门嵌墙**：`fitsInWall` 校验（门框实心 + 门洞两侧至少一侧空气）；anchor 不满足时**区块内搜索可嵌墙位置**（保证调试时门总是生成）；`structure_set` 加 `locate_offset [8,1,8]`（locate 指向区块中心 y=1，原版默认指向区块西北角 y=0）。
- **通用 NBT 模板结构体系**（用户搭建）：`TemplateScatterStructure`（按模板 ID/尺寸/盐值散布，`/place structure` 可放）+ `NbtTemplatePiece`（模板懒加载、跨区块、box 裁剪）+ 已注册：abandoned_camp / blocklevel7_villager_cottage / raft / seabed_cave / void_boat。

### 4. 画廊重做（The Gallery，已完成）
- **X 轴无限廊道 + Z 轴无限多条平行廊道**：廊道内部 z∈[1,4]（每 24 格一条），墙与间隔区实心橡木木板，y=5 基岩顶；X 方向无封墙（去掉旧版 32 格分段）。
- **画链接**：每面墙每 24 X 格挂 **4×3 矮画**（可穿过），`GalleryPassageHandler` 按画 facing 判定西/东墙 → 传送到 Z 相邻廊道（x/y 不变，z±24，20 tick 冷却）；4×4 大画 + 物品展示框装饰（1%）。
- 删除旧 `PaintingPortalHandler`（画不再随机传送）；`FakePainting` 待处理。

### 5. BlockLevel1 昼夜光照重构（进行中）
- `BlockLevel1LightHandler` 重写：**按区块缓存探测方块位置**（区块加载时全块扫描一次；放置/破坏事件增量维护；昼夜切换时惰性校验并清理过期条目），不再每周期全块重扫；切换改为 `scheduleTick` 延迟处理（直接 setBlock 已注释，**实验性**）。
- 新增 `BlockLevel1IgniteHandler`：白天点燃 `BURN_IN_DAYLIGHT` 生物（每 20 tick 检查，13000~24000 tick 除外）。⚠️ 注意：**未限定维度**，全维度生效——需确认是否只应作用于 BlockLevel1。
- `BlockLevel1Generator`：房间高度临时固定为 5；火把/门墙写入改 `UPDATE_ALL`。
- 探测方块（DetectorTorch / DetectorRedstoneLamp 等）在 ed8ed4c 中已有配合改动。

### 6. 新物品 / 消耗品 / 配方
- **软圆石**：可食用，自定义 `ConsumeEffect` → `DamageEffect(2.0, 概率可选)`（`item/consumables/ModConsumeEffects` 注册 codec+stream codec），食用受 2 点伤害。
- **加工软圆石**：食物（营养 3，饱和度 1/6）+ 食用后 36 秒抗性提升。
- **木棍捆**（stick_bundle）：新物品 + 3 个配方（木棍捆合成、拆回木棍、加工软圆石），2 个方块战利品表，`mineable/pickaxe` 标签补充。
- **嗜血僵尸**：自定义 `BloodZombieRenderer` + 新贴图（替换原版僵尸渲染）。

### 7. JEI 集成（可选依赖，不装 JEI 照常运行）
- `build.gradle`：blamejared Maven + `compileOnly` API / `localRuntime` 完整 JEI。
- `jei/BlockroomsJeiPlugin` + `ErrorCraftingRecipeCategory`：错误合成专属分类、合成站、一键转移（槽位 1-9/10/11-46）。
- **配方来源**：`RecipesReceivedEvent` 同步的客户端 `RecipeMap`（`ClientPacketListener.recipes()` 是 ClientRecipeContainer，`RecipeAccess` 无配方查询 API——事件是唯一可靠来源），`IRecipeManagerPlugin` 动态提供；**focus 过滤**（点哪个物品显示哪个配方：OUTPUT 匹配产物 / INPUT 匹配原料）。
- **关键修复**：① `OnDatapackSyncEvent.sendRecipes` 显式注册配方类型（否则客户端收不到）；② `ErrorCraftingRecipe/Shapeless.getSerializer()` 返回模组 serializer（否则网络解码成原版 ShapedRecipe，错误配方混进原版「合成」分类）；③ 分类布局支持 shapeless 流式填充 + 有序配方空气格保留槽位。
- **禁用原版配方**：数据包覆盖 `data/minecraft/recipe/<id>.json` + `neoforge:never` 条件（示例：禁用原版蛋糕配方，配合错误蛋糕配方）。
- `JEI-DIAG` 诊断日志已清理。

### 8. 材质工具与黑石潜影贝
- **无依赖 Node PNG 脚本**（手写 PNG 解码/编码，支持原版 4-bit 索引色 + 越界调色板容错）：
  - `docs/gen_processed_cobblestone.cjs`——加工软圆石：圆石底材 + 裂纹石砖黑色裂纹（亮度阈值提取 + 压暗 + 软过渡）；
  - `docs/gen_blackstone_shulker.cjs`——黑石潜影贝：外壳 = 黑石纹理无缝平铺（去掉明暗调制避免错位），内部贝 = 灰度×0.4 黑化；外壳/贝用**染色纹理做掩码**（shulker_black.png 白色=贝、黑色=外壳——MC 官方区分方式）。
- **黑石潜影贝**：`BlackstoneShulker`（extends Shulker）+ `BlackstoneShulkerRenderer`（覆写 getTextureLocation）+ `ModEntities` 注册 + attributes（`Shulker.createAttributes()`）——**1.21.11 潜影贝纹理由渲染器按 DyeColor 决定，无 getTexture() 钩子**，必须自定义渲染器。
- 原版材质源临时目录 `.tmp-tex/`（可复用换色）。

### 9. 其它改动
- 石英电梯按键判定重写（`ModKeyHandler`：GLFW_PRESS + 键位比对）。
- `level_info` 双语 JSON 新增 blocklevel2 条目（Class 3）；错误配方扩充（iron_boots/iron_pickaxe 等，21 个）。
- `AdvancementGrantHandler`：`player.getServer()` → `level().getServer()`、`getAdvancements().get(Identifier)`（1.21.11 API）。
- 调试临时目录 `.tmp-jei/`、`.tmp-mc/`、`.tmp-nf/`、`.tmp-tex/` —— **未跟踪，勿提交，建议删除**。

## 六、构建与验证

- 打包产物 `build/libs/blockrooms-0.1.7.jar`（2026-08-19 22:34 构建成功）。
- 构建命令：`gradlew build`（需 `JAVA_HOME=<MC 运行时 java-runtime-delta>` + `-g D:\Projects\.gradle`）。
- 新 API 适配备忘：NeoForge 21.11 用 `RegisterGuiLayersEvent` + `GuiLayer`；`ResourceKey.identifier()` 取代 `location()`；`MouseScrollingEvent` 坐标为缩放后 GUI 坐标；1.21.11 移除 `Recipe.getResultItem`（模组在错误配方类显式暴露 `resultItem()`）。

## 七、待办 / 建议下一步

1. **提交 0.1.7 工作区**：统一版本号与提交标题；删除 `.tmp-*` 调试目录再提交。
2. **BL1 区域重做**（用户已给出完整区域设计）：石英回廊（枢纽，中央大厅/档案室/居住单元/观景台）、谐振腔室（晶体矿脉/共鸣深井/废弃实验室）、深板岩拱廊（大集市/石质密室群/暗巷）、熔炉厅（主熔炉阵列/材料仓库/锻造工坊）、砂岩回廊（铭文厅/崩塌甬道/祭坛遗迹，未知文字墙）、真菌林（发光蘑菇谷/菌丝洞穴/腐化池，荧光蘑菇/孢子致病）、隐藏区（红石迷宫+红石心脏升级、观星台安全屋不刷怪）。方案：模板拼块 + 区域分布系统（中心枢纽 + 环带分区）+ 材质替换（共享几何模板按区域换材质）。模板清单待与用户确认（几何 5 + 特色 21 + 特殊结构）。
3. **BL0 模板化重做**（至少 10 种结构，用户搭建模板，生成器按 BL2 模式改造）。
4. **BL2 收尾**：箱子/潜影盒兜底生成效果待用户确认；后续维度默认模板化生成。
5. **补齐门结构目标**：spruce_door 指向 The Void / BlockLevel!（未实现）；完成 `FakePainting` 逻辑（或删除）。
6. **接入 BlockLevel3**（已起步）及 5/6/7/8/Hub 生成器。
7. **更新文档**：`README.md` 仅一行；旧的 CLAUDE.md 等 AI 配置文档已过时。
