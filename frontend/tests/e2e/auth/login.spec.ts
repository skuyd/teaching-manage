import { test, expect } from '@playwright/test'
import { LoginPage } from '../../pages/LoginPage'
import { DashboardPage } from '../../pages/DashboardPage'
import { testUsers, generateUniqueUsername } from '../../fixtures/test-data'

/**
 * Authentication E2E Tests
 * Tests login, logout, and registration flows
 */

test.describe('User Authentication', () => {
  test.describe('Login Flow', () => {
    test('should display login page correctly', async ({ page }) => {
      const loginPage = new LoginPage(page)
      await loginPage.goto()

      // Verify all login elements are present
      await loginPage.verifyPageElements()
    })

    test('should login successfully with valid teacher credentials', async ({ page }) => {
      const loginPage = new LoginPage(page)
      await loginPage.goto()

      // Login with teacher credentials
      await loginPage.login(testUsers.teacher.username, testUsers.teacher.password)

      // Wait for navigation to dashboard
      await page.waitForURL('/', { timeout: 10000 })

      // Verify dashboard is loaded
      const dashboardPage = new DashboardPage(page)
      await dashboardPage.verifyTeacherDashboard()
    })

    test('should login successfully with valid student credentials', async ({ page }) => {
      const loginPage = new LoginPage(page)
      await loginPage.goto()

      // Login with student credentials
      await loginPage.login(testUsers.student.username, testUsers.student.password)

      // Wait for navigation to dashboard
      await page.waitForURL('/', { timeout: 10000 })

      // Verify dashboard is loaded
      const dashboardPage = new DashboardPage(page)
      await dashboardPage.verifyStudentDashboard()
    })

    test('should login successfully with valid admin credentials', async ({ page }) => {
      const loginPage = new LoginPage(page)
      await loginPage.goto()

      // Login with admin credentials
      await loginPage.login(testUsers.admin.username, testUsers.admin.password)

      // Wait for navigation to dashboard
      await page.waitForURL('/', { timeout: 10000 })

      // Verify dashboard is loaded
      const dashboardPage = new DashboardPage(page)
      await dashboardPage.verifyAdminDashboard()
    })

    test('should show error message with invalid credentials', async ({ page }) => {
      const loginPage = new LoginPage(page)
      await loginPage.goto()

      // Try to login with invalid credentials
      await loginPage.login('invalid_user', 'wrong_password')

      // Wait for and verify error message appears
      await expect(loginPage.errorMessage).toBeVisible({ timeout: 5000 })

      // Should remain on login page
      await expect(page).toHaveURL('/login')
    })

    test('should show error with empty username', async ({ page }) => {
      const loginPage = new LoginPage(page)
      await loginPage.goto()

      // Try to login without username
      await loginPage.login('', 'password123')

      // Should remain on login page
      await expect(page).toHaveURL('/login')
    })

    test('should show error with short password', async ({ page }) => {
      const loginPage = new LoginPage(page)
      await loginPage.goto()

      // Try to login with short password (less than 6 chars)
      await loginPage.login('teacher01', '123')

      // Should remain on login page due to validation
      await expect(page).toHaveURL('/login')
    })

    test('should redirect logged-in user from login page to dashboard', async ({ page }) => {
      // First login
      const loginPage = new LoginPage(page)
      await loginPage.goto()
      await loginPage.login(testUsers.teacher.username, testUsers.teacher.password)
      await page.waitForURL('/')

      // Try to navigate to login page
      await page.goto('/login')

      // Should be redirected back to dashboard
      await expect(page).toHaveURL('/')
    })
  })

  test.describe('Logout Flow', () => {
    test('should logout successfully', async ({ page }) => {
      // First login
      const loginPage = new LoginPage(page)
      await loginPage.goto()
      await loginPage.login(testUsers.teacher.username, testUsers.teacher.password)
      await page.waitForURL('/')

      // Logout
      const dashboardPage = new DashboardPage(page)
      await dashboardPage.logout()

      // Should be on login page
      await expect(page).toHaveURL('/login')
    })

    test('should clear auth state after logout', async ({ page }) => {
      // First login
      const loginPage = new LoginPage(page)
      await loginPage.goto()
      await loginPage.login(testUsers.teacher.username, testUsers.teacher.password)
      await page.waitForURL('/')

      // Logout
      const dashboardPage = new DashboardPage(page)
      await dashboardPage.logout()

      // Try to access protected route
      await page.goto('/subjects')

      // Should be redirected to login
      await expect(page).toHaveURL('/login')
    })
  })

  test.describe('Registration Flow', () => {
    test('should display registration form', async ({ page }) => {
      const loginPage = new LoginPage(page)
      await loginPage.goto()

      // Switch to register tab
      await loginPage.switchToRegister()

      // Verify registration form elements
      await expect(loginPage.registerUsernameInput).toBeVisible()
      await expect(loginPage.registerPasswordInput).toBeVisible()
      await expect(loginPage.registerNameInput).toBeVisible()
      await expect(loginPage.registerEmailInput).toBeVisible()
      await expect(loginPage.registerButton).toBeVisible()
    })

    test('should register a new user successfully', async ({ page }) => {
      const loginPage = new LoginPage(page)
      await loginPage.goto()

      // Generate unique username to avoid conflicts
      const uniqueUsername = generateUniqueUsername('newuser')

      // Register new user
      await loginPage.register(
        uniqueUsername,
        'password123',
        'Test User',
        `${uniqueUsername}@test.com`
      )

      // Should show success message and auto-switch to login tab
      await expect(loginPage.successMessage).toBeVisible({ timeout: 5000 })
    })

    test('should show validation error for short username', async ({ page }) => {
      const loginPage = new LoginPage(page)
      await loginPage.goto()

      // Try to register with short username (less than 3 chars)
      await loginPage.register('ab', 'password123', 'Test User')

      // Should remain on registration form
      await expect(page).toHaveURL('/login')
      // Registration should fail validation
    })

    test('should show validation error for short password', async ({ page }) => {
      const loginPage = new LoginPage(page)
      await loginPage.goto()

      // Try to register with short password
      await loginPage.register('newuser123', '123', 'Test User')

      // Should remain on registration form
      await expect(page).toHaveURL('/login')
    })

    test('should show validation error for missing name', async ({ page }) => {
      const loginPage = new LoginPage(page)
      await loginPage.goto()

      // Try to register without name
      await loginPage.register('newuser123', 'password123', '')

      // Should remain on registration form due to validation
      await expect(page).toHaveURL('/login')
    })
  })

  test.describe('Role-based Redirection', () => {
    test('admin should see user management menu', async ({ page }) => {
      const loginPage = new LoginPage(page)
      await loginPage.goto()
      await loginPage.login(testUsers.admin.username, testUsers.admin.password)
      await page.waitForURL('/')

      const dashboardPage = new DashboardPage(page)
      const isUsersVisible = await dashboardPage.isUsersMenuVisible()
      expect(isUsersVisible).toBe(true)
    })

    test('teacher should see subjects and grades menu', async ({ page }) => {
      const loginPage = new LoginPage(page)
      await loginPage.goto()
      await loginPage.login(testUsers.teacher.username, testUsers.teacher.password)
      await page.waitForURL('/')

      const dashboardPage = new DashboardPage(page)
      expect(await dashboardPage.isSubjectsMenuVisible()).toBe(true)
      expect(await dashboardPage.isGradesMenuVisible()).toBe(true)
      expect(await dashboardPage.isUsersMenuVisible()).toBe(false)
    })

    test('student should not see subjects management menu', async ({ page }) => {
      const loginPage = new LoginPage(page)
      await loginPage.goto()
      await loginPage.login(testUsers.student.username, testUsers.student.password)
      await page.waitForURL('/')

      const dashboardPage = new DashboardPage(page)
      expect(await dashboardPage.isSubjectsMenuVisible()).toBe(false)
      expect(await dashboardPage.isGradesMenuVisible()).toBe(false)
    })
  })

  test.describe('Protected Routes', () => {
    test('should redirect to login when accessing protected route without auth', async ({ page }) => {
      // Try to access subjects page without logging in
      await page.goto('/subjects')

      // Should be redirected to login
      await expect(page).toHaveURL('/login')
    })

    test('should redirect to login when accessing dashboard without auth', async ({ page }) => {
      // Try to access dashboard without logging in
      await page.goto('/')

      // Should be redirected to login
      await expect(page).toHaveURL('/login')
    })

    test('should redirect to login when accessing submissions without auth', async ({ page }) => {
      // Try to access submissions without logging in
      await page.goto('/submissions')

      // Should be redirected to login
      await expect(page).toHaveURL('/login')
    })
  })
})
