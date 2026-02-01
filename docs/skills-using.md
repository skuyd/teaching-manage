# Skills 对比分析与使用指南

本文档对比分析用户 Skills 和 Plugin Skills 中功能相近的部分，并提供详细的使用建议。

## 目录

- [概览](#概览)
- [1. 代码审查类](#1-代码审查类)
- [2. 测试驱动开发类](#2-测试驱动开发类)
- [3. 计划/规划类](#3-计划规划类)
- [4. 验证/完成类](#4-验证完成类)
- [5. 技能学习/创建类](#5-技能学习创建类)
- [6. 并行/编排类](#6-并行编排类)
- [7. 代码探索/架构类](#7-代码探索架构类)
- [8. 调试类](#8-调试类)
- [9. 头脑风暴/创意类](#9-头脑风暴创意类)
- [10. Git 工作流类](#10-git-工作流类)
- [综合使用建议](#综合使用建议)

---

## 概览

### Plugin Skills（带命名空间前缀）

| 命名空间 | Skills |
|----------|--------|
| **feature-dev** | feature-dev, code-architect, code-explorer, code-reviewer |
| **ralph-loop** | cancel-ralph, help, ralph-loop |
| **superpowers** | brainstorming, dispatching-parallel-agents, executing-plans, finishing-a-development-branch, receiving-code-review, requesting-code-review, subagent-driven-development, systematic-debugging, test-driven-development, using-git-worktrees, using-superpowers, verification-before-completion, writing-plans, writing-skills |

### 用户 Skills（无命名空间前缀）

| 类别 | Skills |
|------|--------|
| **开发模式/框架** | backend-patterns, frontend-patterns, springboot-patterns, springboot-security, springboot-tdd, jpa-patterns, postgres-patterns, clickhouse-io, coding-standards |
| **测试相关** | tdd-workflow, tdd, e2e, test-coverage, software-testing, eval, eval-harness |
| **Go 语言** | go-build, go-review, go-test |
| **学习/演化** | continuous-learning, continuous-learning-v2, learn, evolve, instinct-export, instinct-import, instinct-status |
| **文档/代码管理** | docx, update-docs, update-codemaps, code-review, refactor-clean |
| **安全** | security-review |
| **浏览器/自动化** | agent-browser |
| **工作流** | plan, orchestrate, checkpoint, verify, build-fix, skill-create, strategic-compact, iterative-retrieval |

**总计**: Plugin skills 约 20 个，用户 skills 约 40 个。

---

## 1. 代码审查类

### 相关 Skills

| 类型 | Skills |
|------|--------|
| **用户 Skill** | `code-review` |
| **Plugin Skill** | `superpowers:requesting-code-review`<br>`superpowers:receiving-code-review`<br>`feature-dev:code-reviewer` |

### 详细对比

| 对比项 | 用户版 `code-review` | Plugin 版 |
|--------|---------------------|-----------|
| **流程设计** | 单一审查流程，一次性完成 | 分为"请求审查"和"接收审查"两个独立阶段 |
| **角色区分** | 不区分审查发起方/接收方 | 明确区分：发起方准备代码供审查，接收方处理反馈 |
| **反馈处理** | 直接实施建议 | 强调技术严谨性，要求验证反馈的正确性后再实施 |
| **适用规模** | 适合小型改动、个人项目 | 适合团队协作、需要多轮审查的场景 |
| **检查深度** | 通用检查 | `feature-dev:code-reviewer` 专注 bugs、逻辑错误、安全漏洞、代码质量 |

### 使用建议

| 场景 | 推荐 Skill | 理由 |
|------|------------|------|
| 个人项目快速审查 | `code-review` | 流程简单，一步到位 |
| 提交 PR 前自检 | `superpowers:requesting-code-review` | 模拟他人视角审查自己的代码 |
| 收到同事审查意见后 | `superpowers:receiving-code-review` | 避免盲目同意，先验证建议的正确性 |
| 主要项目步骤完成后 | `feature-dev:code-reviewer` | 深度审查，对照计划和编码标准 |
| 安全敏感代码 | `feature-dev:code-reviewer` | 包含安全漏洞检查 |

### 组合使用模式

```
开发完成 → superpowers:requesting-code-review（自检）
    ↓
提交审查 → 收到反馈
    ↓
superpowers:receiving-code-review（验证反馈有效性）
    ↓
feature-dev:code-reviewer（最终深度审查）
```

---

## 2. 测试驱动开发类

### 相关 Skills

| 类型 | Skills |
|------|--------|
| **用户 Skill** | `tdd`<br>`tdd-workflow`<br>`test-coverage`<br>`e2e`<br>`software-testing` |
| **Plugin Skill** | `superpowers:test-driven-development` |

### 详细对比

| 对比项 | 用户版（多个 skill） | Plugin 版 |
|--------|---------------------|-----------|
| **粒度** | 细分为多个独立 skill | 统一的 TDD 流程 |
| **覆盖率** | `test-coverage` 单独检查 | 内置 80%+ 覆盖率要求 |
| **端到端测试** | `e2e` 专门处理 Playwright 测试 | 不单独处理 E2E |
| **测试类型** | 各类型分开管理 | 统一包含单元、集成、E2E |
| **灵活性** | 可按需组合 | 一体化流程，必须完整执行 |
| **测试报告** | `software-testing` 生成详细报告（含 Word） | 不生成独立报告文档 |

### 各 Skill 职责

| Skill | 主要职责 |
|-------|----------|
| `tdd` | 强制先写测试再实现，确保 80%+ 覆盖率 |
| `tdd-workflow` | 完整 TDD 工作流，包含单元、集成、E2E |
| `test-coverage` | 专注覆盖率分析和报告 |
| `e2e` | Playwright 端到端测试生成和执行 |
| `software-testing` | 系统测试方案和测试报告（Markdown + Word） |
| `superpowers:test-driven-development` | 实现任何功能/修复前的统一 TDD 流程 |

### 使用建议

| 场景 | 推荐 Skill | 理由 |
|------|------------|------|
| 新功能开发（标准流程） | `superpowers:test-driven-development` | 统一流程，确保规范 |
| 只需单元测试 | `tdd` | 轻量级，快速迭代 |
| 需要详细覆盖率报告 | `test-coverage` | 专门的覆盖率分析 |
| Web 应用 UI 测试 | `e2e` | Playwright 专用，支持截图/视频 |
| 正式测试文档交付 | `software-testing` | 生成专业测试报告文档 |
| 遗留代码补测试 | `tdd` + `test-coverage` | 先补测试再检查覆盖率 |

### 组合使用模式

```
新功能开发:
superpowers:test-driven-development（主流程）
    ↓
e2e（补充 UI 测试）
    ↓
test-coverage（最终覆盖率检查）

正式项目交付:
software-testing（生成完整测试方案和报告）
```

---

## 3. 计划/规划类

### 相关 Skills

| 类型 | Skills |
|------|--------|
| **用户 Skill** | `plan` |
| **Plugin Skill** | `superpowers:writing-plans`<br>`superpowers:executing-plans` |

### 详细对比

| 对比项 | 用户版 `plan` | Plugin 版 |
|--------|--------------|-----------|
| **流程阶段** | 计划和执行一体化 | 明确分离：写计划 → 用户确认 → 执行计划 |
| **用户参与** | 计划后可能直接执行 | 强制等待用户 CONFIRM 后才能动代码 |
| **审查检查点** | 无明确检查点 | 执行阶段有 review checkpoints |
| **风险评估** | 可能包含 | `writing-plans` 明确要求评估风险 |
| **适用场景** | 通用规划 | 多步骤实现任务，需要明确规格 |

### 使用建议

| 场景 | 推荐 Skill | 理由 |
|------|------------|------|
| 快速原型/小改动 | `plan` | 流程简单，快速开始 |
| 重要功能实现 | `superpowers:writing-plans` | 强制用户确认，避免方向错误 |
| 已有批准的计划文档 | `superpowers:executing-plans` | 专注执行，有检查点 |
| 探索性开发 | `plan` | 灵活调整，不需严格审批 |
| 团队协作项目 | Plugin 版组合 | 确保计划被审核后再执行 |

### 组合使用模式

```
收到需求/规格:
superpowers:writing-plans（制定详细计划）
    ↓
用户审核确认
    ↓
superpowers:executing-plans（分阶段执行，带检查点）
    ↓
superpowers:verification-before-completion（完成前验证）
```

---

## 4. 验证/完成类

### 相关 Skills

| 类型 | Skills |
|------|--------|
| **用户 Skill** | `verify`<br>`build-fix` |
| **Plugin Skill** | `superpowers:verification-before-completion` |

### 详细对比

| 对比项 | 用户版 | Plugin 版 |
|--------|--------|-----------|
| **触发时机** | 手动调用 | 声明工作完成/修复/通过**之前**必须调用 |
| **核心原则** | 通用验证 | "证据先于断言"（evidence before assertions） |
| **构建修复** | `build-fix` 专门处理 | 不专门处理构建问题 |
| **严格程度** | 灵活 | 严格：必须运行验证命令并确认输出 |
| **防止误报** | 一般 | 明确防止"声称完成但实际未完成"的情况 |

### 使用建议

| 场景 | 推荐 Skill | 理由 |
|------|------------|------|
| 构建失败需修复 | `build-fix` | 专门处理构建错误 |
| 日常开发验证 | `verify` | 通用验证，灵活使用 |
| 提交代码前 | `superpowers:verification-before-completion` | 严格确保所有测试通过 |
| 创建 PR 前 | `superpowers:verification-before-completion` | 必须有证据证明工作完成 |
| CI/CD 流程 | `verify` + `build-fix` | 构建和验证分开处理 |

### 组合使用模式

```
开发完成:
build-fix（确保构建通过）
    ↓
verify（运行测试和检查）
    ↓
superpowers:verification-before-completion（最终确认，准备提交）
```

---

## 5. 技能学习/创建类

### 相关 Skills

| 类型 | Skills |
|------|--------|
| **用户 Skill** | `skill-create`<br>`continuous-learning`<br>`continuous-learning-v2`<br>`learn`<br>`evolve`<br>`instinct-export`<br>`instinct-import`<br>`instinct-status` |
| **Plugin Skill** | `superpowers:writing-skills` |

### 详细对比

| 对比项 | 用户版（学习生态系统） | Plugin 版 |
|--------|----------------------|-----------|
| **设计理念** | 完整的学习演化链 | 专注技能文件编写 |
| **学习机制** | instinct（本能）→ 演化 → skill | 无自动学习机制 |
| **来源** | 从会话/Git历史自动提取 | 手动编写 |
| **共享能力** | `instinct-export/import` 支持团队共享 | 不涉及共享 |
| **置信度** | `instinct-status` 显示置信度评分 | 无置信度概念 |
| **适用对象** | 自动化学习和演化 | 明确知道要创建什么技能 |

### 各 Skill 职责

| Skill | 主要职责 |
|-------|----------|
| `learn` | 从当前会话提取可复用模式 |
| `continuous-learning` | 自动从会话提取模式保存为技能 |
| `continuous-learning-v2` | 基于 instinct 的学习系统，通过 hooks 观察会话 |
| `evolve` | 将相关 instincts 聚类为 skills/commands/agents |
| `skill-create` | 分析本地 Git 历史生成 SKILL.md |
| `instinct-status` | 显示所有已学习的 instincts 及置信度 |
| `instinct-export/import` | 导出/导入 instincts 用于团队共享 |
| `superpowers:writing-skills` | 创建/编辑技能文件，部署前验证 |

### 使用建议

| 场景 | 推荐 Skill | 理由 |
|------|------------|------|
| 从项目历史提取模式 | `skill-create` | 分析 Git 历史自动生成 |
| 实时学习工作模式 | `continuous-learning-v2` | 自动观察和学习 |
| 手动创建新技能 | `superpowers:writing-skills` | 有明确的技能定义需求 |
| 查看已学习内容 | `instinct-status` | 了解当前学习状态 |
| 团队共享最佳实践 | `instinct-export` → `instinct-import` | 跨项目/团队共享 |
| 将零散学习整合 | `evolve` | 将 instincts 聚类为正式技能 |

### 组合使用模式

```
建立学习生态:
continuous-learning-v2（持续观察学习）
    ↓
instinct-status（定期检查学习成果）
    ↓
evolve（将成熟的 instincts 演化为 skills）
    ↓
superpowers:writing-skills（完善和验证技能）
    ↓
instinct-export（分享给团队）

快速创建技能:
skill-create（从 Git 历史提取）
    或
superpowers:writing-skills（手动编写）
```

---

## 6. 并行/编排类

### 相关 Skills

| 类型 | Skills |
|------|--------|
| **用户 Skill** | `orchestrate` |
| **Plugin Skill** | `superpowers:dispatching-parallel-agents`<br>`superpowers:subagent-driven-development` |

### 详细对比

| 对比项 | 用户版 `orchestrate` | Plugin 版 |
|--------|---------------------|-----------|
| **任务分发** | 统一编排 | 区分跨会话和当前会话 |
| **会话管理** | 不明确区分 | `dispatching`: 2+ 独立任务到不同会话<br>`subagent`: 当前会话内执行 |
| **依赖处理** | 统一处理 | 明确要求"无共享状态或顺序依赖" |
| **适用规模** | 通用 | 针对不同规模任务有专门方案 |

### 使用建议

| 场景 | 推荐 Skill | 理由 |
|------|------------|------|
| 多个完全独立的任务 | `superpowers:dispatching-parallel-agents` | 分发到不同会话并行执行 |
| 当前会话内并行开发 | `superpowers:subagent-driven-development` | 在同一上下文中管理子任务 |
| 复杂工作流编排 | `orchestrate` | 统一编排，灵活处理依赖 |
| 有顺序依赖的任务 | `orchestrate` | 可以处理任务间依赖 |

### 组合使用模式

```
大型项目并行开发:
superpowers:writing-plans（制定计划，识别独立任务）
    ↓
判断任务独立性:
├── 完全独立 → superpowers:dispatching-parallel-agents
└── 有部分依赖 → superpowers:subagent-driven-development
    或
└── 复杂依赖 → orchestrate
```

---

## 7. 代码探索/架构类

### 相关 Skills

| 类型 | Skills |
|------|--------|
| **用户 Skill** | `backend-patterns`<br>`frontend-patterns`<br>`springboot-patterns`<br>`jpa-patterns`<br>`postgres-patterns`<br>`clickhouse-io`<br>`springboot-security`<br>`springboot-tdd` |
| **Plugin Skill** | `feature-dev:code-explorer`<br>`feature-dev:code-architect`<br>`feature-dev:feature-dev` |

### 详细对比

| 对比项 | 用户版（技术栈模式） | Plugin 版（开发阶段） |
|--------|---------------------|---------------------|
| **组织方式** | 按技术栈分类 | 按开发阶段分类 |
| **覆盖范围** | 特定技术的最佳实践 | 通用开发流程 |
| **深度** | 深入特定技术细节 | 关注架构和流程 |
| **灵活性** | 按需选择技术栈 | 统一的探索→设计→开发流程 |

### 各 Skill 职责

| Skill | 主要职责 |
|-------|----------|
| `backend-patterns` | Node.js、Express、Next.js API 后端模式 |
| `frontend-patterns` | React、Next.js、状态管理、性能优化 |
| `springboot-patterns` | Spring Boot 架构、REST API、数据访问 |
| `springboot-security` | Spring Security 认证授权、安全最佳实践 |
| `springboot-tdd` | Spring Boot 的 TDD（JUnit 5、Mockito） |
| `jpa-patterns` | JPA/Hibernate 实体设计、查询优化 |
| `postgres-patterns` | PostgreSQL 查询优化、索引、安全 |
| `clickhouse-io` | ClickHouse 分析查询、数据工程 |
| `feature-dev:code-explorer` | 深度分析代码库，追踪执行路径，映射架构 |
| `feature-dev:code-architect` | 设计功能架构，提供实现蓝图 |
| `feature-dev:feature-dev` | 引导式功能开发，理解代码库和架构 |

### 使用建议

| 场景 | 推荐 Skill | 理由 |
|------|------------|------|
| Spring Boot 项目开发 | `springboot-patterns` + `springboot-security` | 专门的 Spring 最佳实践 |
| React/Next.js 前端 | `frontend-patterns` | 前端专用模式 |
| PostgreSQL 优化 | `postgres-patterns` | 数据库专用指南 |
| 理解陌生代码库 | `feature-dev:code-explorer` | 系统性探索和映射 |
| 设计新功能架构 | `feature-dev:code-architect` | 提供完整实现蓝图 |
| 端到端功能开发 | `feature-dev:feature-dev` | 引导式完整流程 |
| 数据分析平台 | `clickhouse-io` | 高性能分析专用 |

### 组合使用模式

```
新项目/新功能开发:
feature-dev:code-explorer（理解现有代码）
    ↓
feature-dev:code-architect（设计架构）
    ↓
选择技术栈 skill:
├── Spring Boot → springboot-patterns + jpa-patterns
├── Node.js → backend-patterns
├── React → frontend-patterns
└── 数据库 → postgres-patterns 或 clickhouse-io
    ↓
feature-dev:feature-dev（引导式开发）
```

---

## 8. 调试类

### 相关 Skills

| 类型 | Skills |
|------|--------|
| **用户 Skill** | ❌ 无直接对应 |
| **Plugin Skill** | `superpowers:systematic-debugging` |

### 详细分析

| 对比项 | 用户版 | Plugin 版 |
|--------|--------|-----------|
| **可用性** | 无专门调试 skill | 有系统化调试流程 |
| **方法论** | 依赖通用开发经验 | 结构化调试步骤 |
| **触发时机** | - | 遇到 bug、测试失败、意外行为时 |
| **核心原则** | - | 在提出修复方案**之前**先系统分析 |

### 使用建议

| 场景 | 推荐 Skill | 理由 |
|------|------------|------|
| 任何 bug 修复 | `superpowers:systematic-debugging` | 避免盲目猜测，系统分析 |
| 测试失败 | `superpowers:systematic-debugging` | 先理解失败原因再修复 |
| 意外行为 | `superpowers:systematic-debugging` | 结构化排查 |
| 构建失败 | `build-fix` | 专门处理构建问题 |

### 推荐工作流

```
发现问题:
superpowers:systematic-debugging（系统分析）
    ↓
定位根因
    ↓
superpowers:test-driven-development（先写测试验证问题）
    ↓
实施修复
    ↓
superpowers:verification-before-completion（确认修复有效）
```

---

## 9. 头脑风暴/创意类

### 相关 Skills

| 类型 | Skills |
|------|--------|
| **用户 Skill** | ❌ 无直接对应 |
| **Plugin Skill** | `superpowers:brainstorming` |

### 详细分析

| 对比项 | 用户版 | Plugin 版 |
|--------|--------|-----------|
| **可用性** | 无专门 skill | 有结构化头脑风暴流程 |
| **触发时机** | - | 任何创意工作**之前**必须调用 |
| **核心目的** | - | 探索用户意图、需求和设计 |
| **强制性** | - | 创建功能、构建组件、添加功能前**必须**使用 |

### 使用建议

| 场景 | 推荐 Skill | 理由 |
|------|------------|------|
| 创建新功能 | `superpowers:brainstorming` | 先探索意图再实现 |
| 构建新组件 | `superpowers:brainstorming` | 理解需求和设计 |
| 修改现有行为 | `superpowers:brainstorming` | 明确变更目标 |
| 简单 bug 修复 | 跳过 | 目标明确，无需头脑风暴 |

### 推荐工作流

```
收到新需求:
superpowers:brainstorming（探索意图和需求）
    ↓
superpowers:writing-plans（制定实现计划）
    ↓
开始实现
```

---

## 10. Git 工作流类

### 相关 Skills

| 类型 | Skills |
|------|--------|
| **用户 Skill** | ❌ 无直接对应 |
| **Plugin Skill** | `superpowers:using-git-worktrees`<br>`superpowers:finishing-a-development-branch` |

### 详细分析

| 对比项 | 用户版 | Plugin 版 |
|--------|--------|-----------|
| **Worktree 管理** | 无 | `using-git-worktrees` 创建隔离工作区 |
| **分支完成** | 无 | `finishing-a-development-branch` 处理合并/PR/清理 |
| **隔离开发** | 依赖手动操作 | 智能目录选择和安全验证 |
| **完成选项** | 依赖手动操作 | 结构化选项：merge / PR / cleanup |

### 使用建议

| 场景 | 推荐 Skill | 理由 |
|------|------------|------|
| 需要隔离的功能开发 | `superpowers:using-git-worktrees` | 不影响当前工作区 |
| 执行实现计划前 | `superpowers:using-git-worktrees` | 创建隔离环境 |
| 实现完成，测试通过 | `superpowers:finishing-a-development-branch` | 指导如何完成分支 |
| 决定 merge 还是 PR | `superpowers:finishing-a-development-branch` | 提供结构化选项 |

### 推荐工作流

```
开始新功能:
superpowers:using-git-worktrees（创建隔离工作区）
    ↓
开发和测试
    ↓
superpowers:verification-before-completion（验证完成）
    ↓
superpowers:finishing-a-development-branch（选择完成方式）
```

---

## 综合使用建议

### 按开发阶段推荐

| 开发阶段 | 推荐 Skills 组合 |
|----------|------------------|
| **需求理解** | `superpowers:brainstorming` |
| **代码探索** | `feature-dev:code-explorer` + 技术栈 patterns |
| **架构设计** | `feature-dev:code-architect` |
| **计划制定** | `superpowers:writing-plans` |
| **隔离开发** | `superpowers:using-git-worktrees` |
| **TDD 开发** | `superpowers:test-driven-development` 或 `tdd` + `e2e` |
| **调试问题** | `superpowers:systematic-debugging` |
| **构建修复** | `build-fix` |
| **代码审查** | `superpowers:requesting-code-review` → `superpowers:receiving-code-review` |
| **完成验证** | `superpowers:verification-before-completion` |
| **分支完成** | `superpowers:finishing-a-development-branch` |
| **知识沉淀** | `continuous-learning-v2` → `evolve` → `superpowers:writing-skills` |

### 完整开发流程示例

```
┌─────────────────────────────────────────────────────────────────┐
│                        完整开发流程                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. 需求阶段                                                     │
│     └── superpowers:brainstorming                               │
│                                                                 │
│  2. 探索阶段                                                     │
│     ├── feature-dev:code-explorer                               │
│     └── 技术栈 patterns (springboot/frontend/backend...)        │
│                                                                 │
│  3. 设计阶段                                                     │
│     └── feature-dev:code-architect                              │
│                                                                 │
│  4. 计划阶段                                                     │
│     └── superpowers:writing-plans                               │
│         └── 等待用户确认                                         │
│                                                                 │
│  5. 准备阶段                                                     │
│     └── superpowers:using-git-worktrees                         │
│                                                                 │
│  6. 开发阶段                                                     │
│     ├── superpowers:executing-plans                             │
│     ├── superpowers:test-driven-development                     │
│     └── 遇到问题 → superpowers:systematic-debugging             │
│                                                                 │
│  7. 测试阶段                                                     │
│     ├── e2e (UI 测试)                                           │
│     └── test-coverage (覆盖率检查)                               │
│                                                                 │
│  8. 审查阶段                                                     │
│     ├── superpowers:requesting-code-review                      │
│     ├── feature-dev:code-reviewer                               │
│     └── superpowers:receiving-code-review (处理反馈)            │
│                                                                 │
│  9. 验证阶段                                                     │
│     ├── build-fix (确保构建通过)                                 │
│     └── superpowers:verification-before-completion              │
│                                                                 │
│  10. 完成阶段                                                    │
│      └── superpowers:finishing-a-development-branch             │
│                                                                 │
│  11. 知识沉淀                                                    │
│      ├── continuous-learning-v2                                 │
│      ├── evolve                                                 │
│      └── superpowers:writing-skills                             │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 快速参考卡片

| 我想要... | 使用这个 Skill |
|-----------|---------------|
| 理解新需求 | `superpowers:brainstorming` |
| 探索陌生代码 | `feature-dev:code-explorer` |
| 设计功能架构 | `feature-dev:code-architect` |
| 制定实现计划 | `superpowers:writing-plans` |
| 执行已批准的计划 | `superpowers:executing-plans` |
| 创建隔离开发环境 | `superpowers:using-git-worktrees` |
| 写测试再写代码 | `superpowers:test-driven-development` |
| 运行 E2E 测试 | `e2e` |
| 检查测试覆盖率 | `test-coverage` |
| 修复神秘 bug | `superpowers:systematic-debugging` |
| 修复构建失败 | `build-fix` |
| 提交前自检 | `superpowers:requesting-code-review` |
| 处理审查反馈 | `superpowers:receiving-code-review` |
| 深度代码审查 | `feature-dev:code-reviewer` |
| 确认工作完成 | `superpowers:verification-before-completion` |
| 完成分支工作 | `superpowers:finishing-a-development-branch` |
| 学习工作模式 | `continuous-learning-v2` |
| 创建新技能 | `superpowers:writing-skills` |
| 分享学习成果 | `instinct-export` |

---

*文档生成时间: 2026-01-31*
