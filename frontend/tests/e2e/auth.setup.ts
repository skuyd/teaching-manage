import { test as setup, expect } from '@playwright/test'
import { testUsers } from '../fixtures/test-data'
import { LoginPage } from '../pages/LoginPage'
import * as fs from 'fs'
import * as path from 'path'

const authDir = 'playwright/.auth'

/**
 * Setup authentication states for different user roles
 * These are saved to files and reused across tests
 */

// Ensure auth directory exists
setup.beforeAll(async () => {
  if (!fs.existsSync(authDir)) {
    fs.mkdirSync(authDir, { recursive: true })
  }
})

/**
 * Authenticate as teacher and save state
 */
setup('authenticate as teacher', async ({ page }) => {
  const loginPage = new LoginPage(page)
  await loginPage.goto()

  // Login with teacher credentials
  await loginPage.login(testUsers.teacher.username, testUsers.teacher.password)

  // Wait for successful navigation to dashboard
  await page.waitForURL('/', { timeout: 30000 })

  // Verify we are logged in by checking welcome title
  await expect(page.locator('.welcome-title')).toContainText('欢迎', { timeout: 15000 })

  // Save authentication state
  await page.context().storageState({ path: path.join(authDir, 'teacher.json') })
})

/**
 * Authenticate as student and save state
 */
setup('authenticate as student', async ({ page }) => {
  const loginPage = new LoginPage(page)
  await loginPage.goto()

  // Login with student credentials
  await loginPage.login(testUsers.student.username, testUsers.student.password)

  // Wait for successful navigation to dashboard
  await page.waitForURL('/', { timeout: 30000 })

  // Verify we are logged in by checking welcome title
  await expect(page.locator('.welcome-title')).toContainText('欢迎', { timeout: 15000 })

  // Save authentication state
  await page.context().storageState({ path: path.join(authDir, 'student.json') })
})

/**
 * Authenticate as admin and save state
 */
setup('authenticate as admin', async ({ page }) => {
  const loginPage = new LoginPage(page)
  await loginPage.goto()

  // Login with admin credentials
  await loginPage.login(testUsers.admin.username, testUsers.admin.password)

  // Wait for successful navigation to dashboard
  await page.waitForURL('/', { timeout: 30000 })

  // Verify we are logged in by checking welcome title
  await expect(page.locator('.welcome-title')).toContainText('欢迎', { timeout: 15000 })

  // Save authentication state
  await page.context().storageState({ path: path.join(authDir, 'admin.json') })
})
