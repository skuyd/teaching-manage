/**
 * Test data fixtures for E2E tests
 * Contains test users, subjects, lessons, and other test data
 */

export interface TestUser {
  username: string
  password: string
  name: string
  role: 'ADMIN' | 'TEACHER' | 'STUDENT'
  email?: string
}

export interface TestSubject {
  name: string
  description: string
  isGrouped: boolean
  minMembers?: number
  maxMembers?: number
  startDate?: string
  endDate?: string
}

export interface TestLesson {
  title: string
  content: string
  deadline?: string
}

export interface TestGroup {
  name: string
}

/**
 * Test users for different roles
 */
export const testUsers: Record<string, TestUser> = {
  admin: {
    username: 'admin',
    password: 'admin123',
    name: '系统管理员',
    role: 'ADMIN',
    email: 'admin@test.com'
  },
  teacher: {
    username: 'teacher',
    password: 'teacher123',
    name: '默认教师',
    role: 'TEACHER',
    email: 'teacher@teaching.com'
  },
  teacher2: {
    username: 'teacher02',
    password: 'teacher123',
    name: '李老师',
    role: 'TEACHER',
    email: 'teacher02@test.com'
  },
  student: {
    username: 'student',
    password: 'student123',
    name: '默认学员',
    role: 'STUDENT',
    email: 'student@teaching.com'
  },
  student2: {
    username: 'student02',
    password: 'student123',
    name: '李同学',
    role: 'STUDENT',
    email: 'student02@test.com'
  },
  student3: {
    username: 'student03',
    password: 'student123',
    name: '赵同学',
    role: 'STUDENT',
    email: 'student03@test.com'
  }
}

/**
 * Test subjects
 */
export const testSubjects: Record<string, TestSubject> = {
  programming: {
    name: 'Python编程基础',
    description: '学习Python编程语言的基础知识，包括变量、数据类型、控制流等',
    isGrouped: false
  },
  webDev: {
    name: 'Web前端开发',
    description: '学习HTML、CSS、JavaScript等前端技术',
    isGrouped: true,
    minMembers: 2,
    maxMembers: 4
  },
  database: {
    name: '数据库原理与应用',
    description: '学习关系数据库理论和SQL语言',
    isGrouped: false
  },
  projectManagement: {
    name: '软件项目管理',
    description: '学习敏捷开发、项目计划与控制',
    isGrouped: true,
    minMembers: 3,
    maxMembers: 5
  }
}

/**
 * Test lessons
 */
export const testLessons: Record<string, TestLesson> = {
  pythonBasics: {
    title: 'Python基础语法',
    content: '学习Python的基本语法，包括变量定义、数据类型、运算符等'
  },
  pythonFunctions: {
    title: 'Python函数',
    content: '学习Python函数的定义和调用'
  },
  htmlBasics: {
    title: 'HTML基础',
    content: '学习HTML文档结构和常用标签'
  },
  cssBasics: {
    title: 'CSS样式',
    content: '学习CSS选择器和常用样式属性'
  }
}

/**
 * Test groups
 */
export const testGroups: Record<string, TestGroup> = {
  group1: {
    name: '第一组'
  },
  group2: {
    name: '创新小组'
  },
  group3: {
    name: '实践小组'
  }
}

/**
 * Grade options
 */
export const gradeOptions = {
  A: { value: 'A', description: '优秀' },
  B: { value: 'B', description: '良好' },
  C: { value: 'C', description: '及格' },
  D: { value: 'D', description: '不及格' }
}

/**
 * Common comments for grading
 */
export const gradeComments = {
  excellent: '代码结构清晰，逻辑严谨，功能实现完整',
  good: '整体完成较好，有小的改进空间',
  pass: '基本完成要求，但需要加强理解',
  fail: '未能完成基本要求，请重新学习相关内容'
}

/**
 * Generate unique test data suffix based on timestamp
 */
export function generateTestSuffix(): string {
  return `_test_${Date.now()}`
}

/**
 * Generate unique subject name for testing
 */
export function generateUniqueSubjectName(baseName: string): string {
  return `${baseName}${generateTestSuffix()}`
}

/**
 * Generate unique username for testing
 */
export function generateUniqueUsername(baseUsername: string): string {
  return `${baseUsername}${generateTestSuffix()}`
}

/**
 * Test file content for submission
 */
export const testFileContent = {
  python: `# Python test file
def hello_world():
    print("Hello, World!")

if __name__ == "__main__":
    hello_world()
`,
  html: `<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>Test Page</title>
</head>
<body>
    <h1>Hello, World!</h1>
</body>
</html>
`,
  javascript: `// JavaScript test file
function greet(name) {
    return \`Hello, \${name}!\`;
}

console.log(greet('World'));
`
}
