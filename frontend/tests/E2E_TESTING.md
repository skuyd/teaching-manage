# E2E Testing Guide for Teaching Management System

## Overview

This document describes the End-to-End (E2E) testing setup for the Teaching Management System using Playwright.

## Test Structure

```
tests/
├── e2e/                              # E2E test specs
│   ├── auth/
│   │   └── login.spec.ts             # Authentication tests
│   ├── subjects/
│   │   └── subject-management.spec.ts # Subject CRUD tests
│   ├── submissions/
│   │   └── submission-workflow.spec.ts # Assignment submission tests
│   ├── grades/
│   │   └── grading-workflow.spec.ts   # Grading tests
│   ├── groups/
│   │   └── group-management.spec.ts   # Group management tests
│   ├── workflows/
│   │   └── complete-teaching-workflow.spec.ts # Full workflow tests
│   └── auth.setup.ts                  # Auth state setup
├── pages/                            # Page Object Models
│   ├── LoginPage.ts
│   ├── DashboardPage.ts
│   ├── SubjectsPage.ts
│   ├── SubmissionsPage.ts
│   ├── GradesPage.ts
│   ├── GroupsPage.ts
│   └── index.ts
├── fixtures/
│   └── test-data.ts                  # Test data and helpers
└── E2E_TESTING.md                    # This file
```

## Prerequisites

1. Node.js 18+
2. Frontend dev server running on `http://localhost:5173`
3. Backend API running on `http://localhost:8080`

## Installation

```bash
# Install dependencies
npm install

# Install Playwright browsers
npx playwright install
```

## Running Tests

### Basic Commands

```bash
# Run all E2E tests
npm run test:e2e

# Run tests with UI mode (interactive)
npm run test:e2e:ui

# Run tests in headed mode (visible browser)
npm run test:e2e:headed

# Debug tests step by step
npm run test:e2e:debug

# Generate test code from browser actions
npm run test:e2e:codegen

# View HTML test report
npm run test:e2e:report
```

### Run Specific Browser

```bash
# Chromium only
npm run test:e2e:chromium

# Firefox only
npm run test:e2e:firefox

# WebKit only
npm run test:e2e:webkit
```

### Run Specific Tests

```bash
# Run authentication tests only
npx playwright test tests/e2e/auth/

# Run a specific test file
npx playwright test tests/e2e/subjects/subject-management.spec.ts

# Run tests matching a pattern
npx playwright test -g "login"
```

## Test Users

The following test users are defined in `tests/fixtures/test-data.ts`:

| Role | Username | Password | Description |
|------|----------|----------|-------------|
| Admin | admin | admin123 | System administrator |
| Teacher | teacher01 | teacher123 | Instructor account |
| Student | student01 | student123 | Student account |
| Student | student02 | student123 | Second student (for group tests) |

## Page Object Model

Each page has a corresponding Page Object that encapsulates:
- Element locators
- Page actions
- Assertions

Example usage:
```typescript
import { LoginPage } from '../../pages/LoginPage'

test('should login successfully', async ({ page }) => {
  const loginPage = new LoginPage(page)
  await loginPage.goto()
  await loginPage.login('teacher01', 'teacher123')
  await expect(page).toHaveURL('/')
})
```

## Test Data

### Generating Unique Test Data

Use helper functions to avoid test data conflicts:

```typescript
import { generateUniqueSubjectName, generateUniqueUsername } from '../../fixtures/test-data'

const subjectName = generateUniqueSubjectName('TestSubject')
// Result: TestSubject_test_1234567890
```

### Test File Content

Pre-defined file content for submission tests:

```typescript
import { testFileContent } from '../../fixtures/test-data'

await submissionsPage.submitAssignment(testFileContent.python, 'homework.py')
```

## Authentication Setup

Authentication states are saved in `playwright/.auth/`:
- `teacher.json` - Teacher auth state
- `student.json` - Student auth state
- `admin.json` - Admin auth state

Tests use these pre-authenticated states to avoid logging in for every test.

## Test Reports

After running tests, reports are generated in:
- `playwright-report/` - HTML report (open with `npm run test:e2e:report`)
- `playwright-results.xml` - JUnit XML report (for CI)
- `playwright-results.json` - JSON report

## Artifacts

On test failure, the following are captured:
- Screenshots: `test-results/[test-name]/[step]-screenshot.png`
- Videos: `test-results/[test-name]/video.webm`
- Traces: `test-results/[test-name]/trace.zip`

## CI/CD Integration

Example GitHub Actions workflow:

```yaml
name: E2E Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: 18

      - name: Install dependencies
        run: npm ci

      - name: Install Playwright browsers
        run: npx playwright install --with-deps

      - name: Run E2E tests
        run: npm run test:e2e

      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: playwright-report
          path: playwright-report/
          retention-days: 30
```

## Writing New Tests

### Test Structure

```typescript
import { test, expect } from '@playwright/test'
import { LoginPage } from '../../pages/LoginPage'

test.describe('Feature Name', () => {
  test.beforeEach(async ({ page }) => {
    // Setup: login, navigate, etc.
  })

  test('should do something', async ({ page }) => {
    // Arrange
    // Act
    // Assert
  })

  test.afterEach(async ({ page }) => {
    // Cleanup if needed
  })
})
```

### Best Practices

1. **Use Page Objects** - Encapsulate page interactions
2. **Test Isolation** - Each test should be independent
3. **Data Cleanup** - Clean up test data after tests
4. **Wait Properly** - Use proper waits instead of arbitrary timeouts
5. **Meaningful Assertions** - Assert on user-visible outcomes
6. **Screenshot on Failure** - Already configured in playwright.config.ts

### Skipped Tests

Some tests are marked with `test.skip()` because they require:
- Specific test data to exist
- Complete backend integration
- Specific subject/lesson IDs

Enable these tests as the system grows.

## Troubleshooting

### Tests Failing to Find Elements

1. Check element locators in Page Objects
2. Use `npx playwright test --debug` to step through
3. Add `await page.pause()` to inspect the page

### Authentication Issues

1. Delete `playwright/.auth/*.json` files
2. Re-run the auth setup tests
3. Check test user credentials

### Timeout Issues

1. Increase timeout in `playwright.config.ts`
2. Check network conditions
3. Ensure backend is responsive

### Flaky Tests

1. Add proper waits (`waitForLoadState`, `waitForResponse`)
2. Use `test.retry(2)` for known flaky tests
3. Investigate race conditions

## Key User Journeys Tested

1. **Authentication Flow**
   - Login success/failure
   - Role-based redirects
   - Logout

2. **Teacher Workflow**
   - Create/edit/delete subjects
   - Add lessons to subjects
   - View submissions
   - Grade assignments

3. **Student Workflow**
   - Browse subjects and lessons
   - Join/create groups
   - Submit assignments
   - View grades

4. **Admin Workflow**
   - View system statistics
   - Manage users

## Support

For issues with the E2E testing setup, check:
1. Playwright documentation: https://playwright.dev/docs/intro
2. Project issues on GitHub
