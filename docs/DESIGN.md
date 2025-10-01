# 阿里云 Java SDK 重构规划

## 1. 现有项目全景

### 1.1 技术栈与运行形态
- **后端框架**：NestJS + Express；以 `GatewayModule` 为核心加载路由、意图识别、处理管道和多租户模块。
- **配置中心**：`configs/providers.yaml`、`routes.yaml`、`intents.yaml` 等 YAML 文件，支持按租户加载不同 Provider。
- **AI Provider 管理**：`ProviderManagerService` 负责注册、选路、限流与统一调用。目前集成 OpenAI、通义千问、Claude、Stable Diffusion、Whisper，自定义 `AlibabaaASRProvider` 以 WebSocket 调用阿里云百炼。
- **多租户能力**：`MultiTenantModule` 和 `TenantAwareGatewayController` 提供租户识别、专属配置与 API 入口映射。
- **前端体验**：`public/` 下静态页面（`index.html`、`voice-demo.html` 等）演示文本对话、语音对话、Swagger 文档入口。
- **临时存储**：`temp/` 目录承载中转文件、文档抓取等临时数据。

### 1.2 当前 ASR 流程（手工实现）
1. 浏览器录音（MediaRecorder ➜ WebM/Opus）。
2. 前端用 Web Audio 重新采样到 16kHz PCM WAV，并以 base64 形式贴到 `POST /api/asr/recognize`。
3. Nest 控制器解包后调用 `AlibabaaASRProvider`：
   - 手动解码 WAV，降混为单声道。
   - 手工拼装 WebSocket `run-task`/`finish-task` 请求。
   - 解析 `result-generated`、`task-failed` 事件并聚合文本。

### 1.3 运行痛点
- **高耦合实现**：大量底层协议处理（WebSocket 握手、音频解码、消息格式）直接写在 Provider 中，维护成本高。
- **数据转换链路长**：前端和后端同时参与音频转码，流程复杂且容易引入采样或格式错误。
- **监控排障困难**：虽然新增了日志，但缺乏系统化指标和错误码对照，排查需要翻阅文档并手动分析。
- **与阿里官方能力脱节**：没有利用 SDK 内置的鉴权、模型管理、资源托管、异步任务轮询等功能；对模型版本了解有限（此前配置了过期的 `paraformer-v1`）。
- **语言栈割裂**：计划接入更多阿里云服务（多模态、知识检索等）时，需要重复编写 TypeScript 版协议客户端。

## 2. 阿里云 Java SDK 价值速览
（参考官方文档：<https://help.aliyun.com/zh/model-studio/install-sdk>）

- **统一封装**：提供抢先封装的 REST/WebSocket 客户端、模型上下文管理、鉴权 Token/AK 管理。
- **丰富模型支持**：覆盖实时语音（Paraformer 系列）、通义千问、图像生成等；参数校验与错误码对齐官方文档。
- **异步任务编排**：SDK 自带轮询器、回调处理工具，简化任务状态管理。
- **生态兼容**：基于 Java，可无缝整合 Spring Boot、Sentinel、Prometheus 等企业常用组件。
- **多实例治理**：内置连接池、限流、熔断策略，降低网关需要重复造轮子的部分。

## 3. 重构目标
1. **降低自研成本**：把底层协议/模型适配交给官方 SDK，团队主要关注业务编排与多租户治理。
2. **统一技术栈**：关键 AI 调用迁移至 Java 服务，方便与企业现有 Java 体系集成。
3. **增强可观测性**：利用 SDK 和 Java 生态提供的监控手段，提升调试与运维效率。
4. **提升可扩展性**：为后续引入多模态、知识库检索、流式多路复用等能力预留架构空间。

## 4. 目标架构蓝图

```
┌──────────────────────────────────────────────────────────┐
│                    前端体验层（保留）                     │
│  - public/index.html / voice-demo.html                    │
│  - 录音、UI、租户选择                                      │
└───────────────▲──────────────────────────────────────────┘
                │HTTP(S)
┌───────────────┴──────────────────────────────────────────┐
│         Node.js API Gateway（NestJS，逐步瘦身）             │
│  - 负责认证、租户识别、路由                                 │
│  - 将 AI 调用转发至 Java AI Service                        │
│  - 维护 YAML 配置（或迁移至数据库）                         │
└───────────────▲──────────────────────────────────────────┘
                │gRPC/REST
┌───────────────┴──────────────────────────────────────────┐
│           Java AI Service（Spring Boot + Alibaba SDK）    │
│  - SDK 封装的 ASR/TTS/LLM 客户端                          │
│  - 音频处理、模型选择、热词管理                            │
│  - 统一鉴权、监控、熔断                                    │
└───────────────▲──────────────────────────────────────────┘
                │SDK（官方）
┌───────────────┴──────────────────────────────────────────┐
│              Alibaba Model Studio & 其它 AI 服务           │
└──────────────────────────────────────────────────────────┘
```

## 5. Java 服务模块规划

| 模块               | 功能                                       | 关键技术/依赖                                         |
| ------------------ | ------------------------------------------ | ----------------------------------------------------- |
| `api-gateway`      | 提供 REST/gRPC API，接收 NestJS 转发的请求 | Spring MVC / gRPC, Bean Validation                    |
| `sdk-integrations` | 封装阿里云各模型客户端，提供统一接口       | Alibaba Model Studio Java SDK, Lombok                 |
| `audio-pipeline`   | 处理音频编解码、格式转换、分片上传         | Java Sound API / FFmpeg Wrapper（可选）               |
| `config-center`    | 管理租户、模型、热词、限流配置             | Spring ConfigurationProperties + Nacos/Consul（可选） |
| `observability`    | 指标上报、日志、追踪                       | Micrometer, Sleuth, Logback                           |
| `security`         | AK/SK 管理、临时 Token、签名               | Alibaba Credentials Provider, Vault/KMS               |

> 注：若企业已有 Java 基础设施，可直接对接内部 SSO、配置中心、消息队列等。

## 6. 迁移路线建议

### 阶段 0：准备
- 评估现有租户、模型、接口调用量，确定需要迁移的优先级（先从 ASR 起步）。
- 建立 Java 服务骨架（Spring Boot）并引入 Alibaba SDK，打通基础鉴权。
- 整理现有 YAML 配置，转换为 Java 服务能消费的格式（JSON/YAML/数据库）。

### 阶段 1：ASR 能力迁移
1. **实现 Java ASR API**：
   - 对接 Paraformer 实时/离线接口。
   - 提供统一的 `/asr/recognize` `/asr/stream` REST/gRPC 接口。
   - 内置音频格式检测、自动重采样、热词管理。
2. **Nest 网关改造**：
   - `TenantAwareGatewayController` 中的 ASR 调用改为请求 Java 服务。
   - 简化 TypeScript 侧的 `AlibabaaASRProvider`（逐步废弃）。
3. **联调**：
   - 确认前端仍以 WAV base64 提交；Java 服务负责最终转 SDK 需要的 ByteStream。
   - 新增日志/指标，验证识别准确率和延迟。

### 阶段 2：其它 AI 能力迁移
- **文本、图像、TTS**：逐步把现有 Provider 改为 Java SDK 调用，Node 仅保留编排。
- **统一调用协议**：设计标准化响应格式，确保旧客户端无需大改。
- **多租户策略**：在 Java 服务中实现租户隔离（独立配置、AK/SK、限流规则）。

### 阶段 3：优化与收敛
- 评估是否将整个网关重写为 Java（若 Node 侧只剩基础转发，可逐步替换）。
- 引入消息队列/事件总线，为异步任务、通知、日志收集提供支撑。
- 落地 DevOps：CI/CD、容器化、灰度发布。

## 7. 配置与部署建议

| 维度          | 现状           | 调整建议                                                     |
| ------------- | -------------- | ------------------------------------------------------------ |
| Provider 配置 | YAML 静态文件  | 抽象为配置服务或数据库，Java & Node 共用；考虑配置热更新     |
| 秘钥管理      | `.env` 读取    | 统一接入企业密钥管理（KMS、Vault），避免散落在多套服务       |
| 日志指标      | 控制台日志为主 | Java 侧输出结构化日志，接入链路追踪；Node 侧保留关键事件转发 |
| 部署方式      | Node 单体      | Java 服务独立部署（容器/K8s），与 Node 网关一起编排          |
| 临时文件      | 本地 `temp/`   | Java 侧直接支持 OSS 上传/临时链接，减少本地 IO               |

## 8. 风险与缓解

| 风险         | 影响                                | 缓解方案                                     |
| ------------ | ----------------------------------- | -------------------------------------------- |
| 技术栈跨度   | 团队需掌握 Java/Spring 生态         | 先行搭建 PoC，安排培训/Pair Programming      |
| 迁移期间双写 | 新旧实现需并存，易产生不一致        | 通过 Feature Flag 控制，灰度发布，逐租户迁移 |
| SDK 限制     | 官方 SDK 仍在迭代，可能缺少特定功能 | 与阿里云支持沟通，必要时保留自研兜底实现     |
| 性能瓶颈     | Java 服务新增一跳网络调用           | 选择低延迟协议（gRPC），做好连接池与压测     |
| 配置同步     | Node & Java 双端读取配置            | 搭建统一配置中心或 API，以拉模式同步         |

## 9. 下一步行动清单
1. 建立 `ai-gateway-java` 仓库（Spring Boot 模板 + Alibaba SDK），提交基础骨架。
2. 整理现有 `providers.yaml`，定义统一的 Provider Schema，给 Node/Java 共用。
3. 设计 Java ASR API 的接口契约（请求/响应、错误码、超时策略）。
4. 制定音频处理策略（直接透传 WAV、或在 Java 侧做重采样）。
5. 规划监控指标：QPS、识别耗时、错误分布、模型命中率。
6. 排期与资源评估，设定阶段里程碑（PoC、灰度、全面切换）。

---

通过以上规划，我们可以把现有 Node 网关的灵活路由、多租户优势与阿里云 Java SDK 的稳定可靠相结合，在保证业务连续性的同时，快速引入官方生态提供的高级能力，支撑更大规模、更复杂的 AI 接入场景。