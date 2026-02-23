# 开发指南 - Teaching Manage

## 环境要求

### 后端
- **Java**: 17+
- **Maven**: 3.8+

### 前端
- **Node.js**: 18+
- **npm**: 9+

### 开发工具 (推荐)
- **IDE**: IntelliJ IDEA / VS Code
- **数据库查看**: DB Browser for SQLite

---

## 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd teaching-manage
```

### 2. 启动后端

```bash
cd backend

# 首次运行（自动初始化数据库）
mvn spring-boot:run

# 后端运行在 http://localhost:8080
```

### 3. 启动前端

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 前端运行在 http://localhost:5173
```

### 4. 访问应用

打开浏览器访问 http://localhost:5173

**默认账户:**
| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 教员 | teacher | teacher123 |
| 学员 | student | student123 |

---

## 后端开发

### 项目结构

```
backend/
├── pom.xml                     # Maven 配置
├── src/main/java/com/teaching/
│   ├── Application.java        # 启动类
│   ├── controller/             # REST 控制器
│   ├── service/                # 业务服务
│   │   └── impl/              # 服务实现
│   ├── mapper/                 # MyBatis Mapper
│   ├── entity/                 # 数据库实体
│   ├── dto/                    # 数据传输对象
│   ├── enums/                  # 枚举类型
│   ├── config/                 # 配置类
│   ├── security/               # 安全模块
│   ├── exception/              # 异常处理
│   ├── task/                   # 定时任务
│   ├── util/                   # 工具类
│   └── init/                   # 初始化器
└── src/main/resources/
    ├── application.yml         # 应用配置
    └── schema.sql              # 数据库初始化
```

### 添加新功能流程

#### 1. 创建实体

```java
// entity/NewEntity.java
@Data
@TableName("t_new_entity")
public class NewEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    // 其他字段...
}
```

#### 2. 创建 Mapper

```java
// mapper/NewEntityMapper.java
@Mapper
public interface NewEntityMapper extends BaseMapper<NewEntity> {
    // 自定义查询方法...
}
```

#### 3. 创建 DTO

```java
// dto/NewEntityDTO.java
@Data
public class NewEntityDTO {
    private Long id;
    private String name;
}

// dto/CreateNewEntityRequest.java
@Data
public class CreateNewEntityRequest {
    @NotBlank(message = "名称不能为空")
    private String name;
}
```

#### 4. 创建 Service

```java
// service/NewEntityService.java
public interface NewEntityService {
    NewEntityDTO create(CreateNewEntityRequest request);
    NewEntityDTO getById(Long id);
    List<NewEntityDTO> list();
}

// service/impl/NewEntityServiceImpl.java
@Service
@RequiredArgsConstructor
public class NewEntityServiceImpl implements NewEntityService {
    private final NewEntityMapper newEntityMapper;

    @Override
    public NewEntityDTO create(CreateNewEntityRequest request) {
        NewEntity entity = new NewEntity();
        entity.setName(request.getName());
        newEntityMapper.insert(entity);
        return toDTO(entity);
    }
    // 其他方法实现...
}
```

#### 5. 创建 Controller

```java
// controller/NewEntityController.java
@RestController
@RequestMapping("/api/new-entities")
@RequiredArgsConstructor
public class NewEntityController {
    private final NewEntityService newEntityService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<NewEntityDTO> create(@Valid @RequestBody CreateNewEntityRequest request) {
        return Result.success(newEntityService.create(request));
    }

    @GetMapping("/{id}")
    public Result<NewEntityDTO> getById(@PathVariable Long id) {
        return Result.success(newEntityService.getById(id));
    }
}
```

### 常用命令

```bash
# 运行应用
mvn spring-boot:run

# 运行测试
mvn test

# 运行单个测试
mvn test -Dtest=UserServiceTest

# 构建 JAR
mvn clean package

# 跳过测试构建
mvn clean package -DskipTests

# 清理
mvn clean
```

### 配置说明

`application.yml` 关键配置:

```yaml
# 服务器端口
server:
  port: 8080

# 数据库配置
spring:
  datasource:
    url: jdbc:sqlite:./data/teaching.db
  sql:
    init:
      mode: always  # 首次运行后改为 'never'

# JWT 配置
jwt:
  secret: teaching-manage-secret-key-must-be-at-least-256-bits-long
  expiration: 86400000  # 24 小时

# 文件上传
file:
  upload-dir: ./uploads

# MyBatis-Plus
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: delFlag
      logic-delete-value: 1
      logic-not-delete-value: 0
```

---

## 前端开发

### 项目结构

```
frontend/
├── package.json
├── vite.config.ts
├── tsconfig.json
├── src/
│   ├── main.ts                 # 入口文件
│   ├── App.vue                 # 根组件
│   ├── api/                    # API 服务
│   │   ├── types.ts           # 类型定义
│   │   └── *.ts               # API 模块
│   ├── views/                  # 页面组件
│   ├── components/             # 通用组件
│   ├── stores/                 # Pinia 状态
│   ├── router/                 # 路由配置
│   ├── directives/             # 自定义指令
│   ├── utils/                  # 工具函数
│   └── styles/                 # 样式文件
└── tests/                      # 测试文件
```

### 添加新页面流程

#### 1. 创建 API 服务

```typescript
// api/newEntity.ts
import request from '@/utils/request'
import type { Result } from './types'

export interface NewEntityDTO {
  id: number
  name: string
}

export function getNewEntities(): Promise<Result<NewEntityDTO[]>> {
  return request.get('/new-entities')
}

export function createNewEntity(data: { name: string }): Promise<Result<NewEntityDTO>> {
  return request.post('/new-entities', data)
}
```

#### 2. 创建页面组件

```vue
<!-- views/newEntity/NewEntityList.vue -->
<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>实体列表</span>
          <el-button type="primary" @click="handleCreate" v-role="'ADMIN'">
            新建
          </el-button>
        </div>
      </template>

      <el-table :data="entities" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getNewEntities, type NewEntityDTO } from '@/api/newEntity'

const entities = ref<NewEntityDTO[]>([])
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getNewEntities()
    entities.value = res.data
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
```

#### 3. 添加路由

```typescript
// router/index.ts
{
  path: '/new-entities',
  component: () => import('@/views/newEntity/NewEntityList.vue'),
  meta: {
    requiresAuth: true,
    roles: ['ADMIN']
  }
}
```

### 常用命令

```bash
# 安装依赖
npm install

# 开发服务器
npm run dev

# 生产构建
npm run build

# 预览构建结果
npm run preview

# 运行单元测试
npm run test

# 运行 E2E 测试
npm run test:e2e

# 代码检查
npm run lint
```

### API 请求封装

`utils/request.ts` 提供统一的请求处理：

```typescript
import axios from 'axios'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器 - 添加 Token
request.interceptors.request.use(config => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

// 响应拦截器 - 统一错误处理
request.interceptors.response.use(
  response => {
    const { code, message } = response.data
    if (code !== 200) {
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message))
    }
    return response.data
  },
  error => {
    if (error.response?.status === 401) {
      // 跳转登录
    }
    return Promise.reject(error)
  }
)
```

---

## 测试

### 后端测试

```bash
# 运行所有测试
mvn test

# 运行指定测试类
mvn test -Dtest=UserServiceTest

# 运行指定测试方法
mvn test -Dtest=UserServiceTest#testCreateUser
```

### 前端单元测试

```bash
# 运行测试
npm run test

# 监视模式
npm run test -- --watch

# 测试覆盖率
npm run test -- --coverage
```

### E2E 测试 (Playwright)

```bash
# 运行所有 E2E 测试
npm run test:e2e

# 头部模式运行
npm run test:e2e:headed

# 调试模式
npm run test:e2e:debug

# 生成测试代码
npm run test:e2e:codegen
```

---

## 调试技巧

### 后端调试

1. **IDE 调试**: 在 IntelliJ IDEA 中以 Debug 模式运行
2. **日志**: 修改 `application.yml` 中的日志级别

```yaml
logging:
  level:
    com.teaching: DEBUG
    org.springframework.security: DEBUG
```

### 前端调试

1. **Vue DevTools**: 安装浏览器扩展
2. **网络请求**: 使用浏览器开发者工具的 Network 面板
3. **Console**: 查看控制台输出

### 数据库调试

使用 DB Browser for SQLite 打开 `./data/teaching.db` 查看数据。

---

## 部署

### 后端部署

```bash
# 构建 JAR
mvn clean package -DskipTests

# 运行 JAR
java -jar target/teaching-manage-1.0.0.jar
```

### 前端部署

```bash
# 构建
npm run build

# 输出目录: dist/
# 使用 Nginx 或其他静态服务器托管
```

### 环境变量

生产环境应使用环境变量覆盖敏感配置：

```bash
# 后端
export JWT_SECRET=your-production-secret
export SPRING_PROFILES_ACTIVE=prod

# 运行
java -jar teaching-manage-1.0.0.jar
```

---

## 常见问题

### Q: 数据库初始化失败

**A**: 确保 `./data/` 目录存在且有写权限。首次运行后，将 `spring.sql.init.mode` 改为 `never`。

### Q: 前端请求 404

**A**: 检查 Vite 代理配置 (`vite.config.ts`)，确保后端服务正在运行。

### Q: JWT Token 过期

**A**: 默认 24 小时过期。可在 `application.yml` 中调整 `jwt.expiration`。

### Q: 文件上传失败

**A**: 检查文件大小限制。可在 `application.yml` 中调整：

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB
```
