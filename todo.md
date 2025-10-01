# Alibaba Cloud Java SDK Refactor TODOs

## Phase 0 – Preparation
- [ ] Inventory existing tenants, provider usage, and traffic to prioritize migration scope.
- [x] Bootstrap Spring Boot multi-module project with baseline dependencies and build automation.
- [x] Wire Alibaba Cloud Model Studio Java SDK credentials loading via configurable providers.
- [x] Normalize current YAML provider configs into shared schema consumable by Java service.

## Phase 1 – ASR Capability Migration
- [x] Implement `/asr/recognize` REST endpoint backed by Paraformer client abstraction.
- [x] Provide audio ingestion pipeline handling WAV validation, resampling, and chunk assembly.
- [ ] Replace NestJS `AlibabaaASRProvider` with proxy calls to the Java ASR API.
- [x] Capture latency, accuracy, and error metrics for ASR flows.

## Phase 2 – Remaining Provider Migration
- [ ] Add text, image, and TTS adapters leveraging Alibaba SDK unified interface.
- [ ] Define standard response contracts shared between Node gateway and Java service.
- [ ] Enforce tenant-aware routing, rate limiting, and AK/SK isolation across providers.

## Phase 3 – Optimization & Operations
- [ ] Evaluate consolidating gateway responsibilities fully into Java stack.
- [ ] Integrate asynchronous processing via message queue for long-running tasks.
- [ ] Finalize CI/CD, observability dashboards, and failover playbooks.
