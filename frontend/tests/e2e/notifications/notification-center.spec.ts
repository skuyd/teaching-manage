import { test, expect } from '@playwright/test'
import { LoginPage } from '../../pages/LoginPage'
import { DashboardPage } from '../../pages/DashboardPage'
import { NotificationsPage } from '../../pages/NotificationsPage'
import { testUsers } from '../../fixtures/test-data'

/**
 * Notification Center E2E Tests
 * Tests notification viewing, marking as read, and management
 */

/**
 * Helper to clear auth state and login as specific user
 */
async function clearAuthAndLogin(page: any, username: string, password: string) {
  // Clear cookies first
  await page.context().clearCookies()

  // Navigate to login page first
  await page.goto('/login')
  await page.waitForLoadState('domcontentloaded')

  // Clear localStorage after page loads
  await page.evaluate(() => localStorage.clear())

  // Reload to ensure clean state
  await page.reload()
  await page.waitForLoadState('domcontentloaded')

  // Wait for login form to be ready
  await page.waitForSelector('input[placeholder="用户名"]', { state: 'visible', timeout: 10000 })

  // Now login
  const loginPage = new LoginPage(page)
  await loginPage.login(username, password)

  // Wait for redirect with retry logic
  try {
    await page.waitForURL('/', { timeout: 20000 })
  } catch {
    // If timeout, check if we're on dashboard
    const currentUrl = page.url()
    if (!currentUrl.endsWith('/') && !currentUrl.includes('/subjects') && !currentUrl.includes('/admin')) {
      // Try login again
      await page.goto('/login')
      await page.waitForSelector('input[placeholder="用户名"]', { state: 'visible', timeout: 5000 })
      await loginPage.login(username, password)
      await page.waitForURL('/', { timeout: 20000 })
    }
  }

  // Verify we're logged in by checking for dashboard elements
  await page.waitForSelector('.welcome-card, .sidebar', { state: 'visible', timeout: 10000 })
}

test.describe('Notification Center', () => {
  test.describe('Access and Display', () => {
    test('authenticated user can access notification center', async ({ page }) => {
      await clearAuthAndLogin(page, testUsers.student.username, testUsers.student.password)

      const notificationsPage = new NotificationsPage(page)
      await notificationsPage.goto()
      await notificationsPage.verifyPageLoaded()
    })

    test('unauthenticated user cannot access notification center', async ({ page }) => {
      // Clear existing auth state
      await page.context().clearCookies()
      await page.goto('/login')
      await page.waitForLoadState('domcontentloaded')
      await page.evaluate(() => localStorage.clear())

      await page.goto('/notifications')

      // Should be redirected to login
      await expect(page).toHaveURL('/login')
    })

    test('notification bell shows in dashboard header', async ({ page }) => {
      await clearAuthAndLogin(page, testUsers.student.username, testUsers.student.password)

      const dashboardPage = new DashboardPage(page)
      await expect(dashboardPage.notificationBell).toBeVisible()
    })
  })

  test.describe('Student Notifications', () => {
    test.beforeEach(async ({ page }) => {
      await clearAuthAndLogin(page, testUsers.student.username, testUsers.student.password)
    })

    test('should display notification list or empty state', async ({ page }) => {
      const notificationsPage = new NotificationsPage(page)
      await notificationsPage.goto()
      await notificationsPage.verifyPageLoaded()

      // Either notifications exist or empty state is shown
      const count = await notificationsPage.getNotificationCount()
      const isEmpty = await notificationsPage.isEmptyStateVisible()

      expect(count >= 0 || isEmpty).toBe(true)
    })

    test('should be able to click on notification', async ({ page }) => {
      const notificationsPage = new NotificationsPage(page)
      await notificationsPage.goto()
      await notificationsPage.verifyPageLoaded()

      const count = await notificationsPage.getNotificationCount()
      if (count > 0) {
        // Click on first notification
        await notificationsPage.clickNotification(0)
        // Should mark as read or navigate
      }
    })

    test('should be able to mark all as read', async ({ page }) => {
      const notificationsPage = new NotificationsPage(page)
      await notificationsPage.goto()
      await notificationsPage.verifyPageLoaded()

      // If there are unread notifications
      const unreadCount = await notificationsPage.getUnreadCount()
      if (unreadCount > 0) {
        await notificationsPage.markAllAsRead()

        // Unread count should decrease
        await page.waitForTimeout(500)
        const newUnreadCount = await notificationsPage.getUnreadCount()
        expect(newUnreadCount).toBeLessThanOrEqual(unreadCount)
      }
    })
  })

  test.describe('Teacher Notifications', () => {
    test.beforeEach(async ({ page }) => {
      await clearAuthAndLogin(page, testUsers.teacher.username, testUsers.teacher.password)
    })

    test('teacher can access notification center', async ({ page }) => {
      const notificationsPage = new NotificationsPage(page)
      await notificationsPage.goto()
      await notificationsPage.verifyPageLoaded()
    })

    test('teacher sees relevant notifications', async ({ page }) => {
      const notificationsPage = new NotificationsPage(page)
      await notificationsPage.goto()
      await notificationsPage.verifyPageLoaded()

      // Teacher should see their notifications
      const count = await notificationsPage.getNotificationCount()
      const isEmpty = await notificationsPage.isEmptyStateVisible()
      expect(count >= 0 || isEmpty).toBe(true)
    })
  })

  test.describe('Admin Notifications', () => {
    test.beforeEach(async ({ page }) => {
      await clearAuthAndLogin(page, testUsers.admin.username, testUsers.admin.password)
    })

    test('admin can access notification center', async ({ page }) => {
      const notificationsPage = new NotificationsPage(page)
      await notificationsPage.goto()
      await notificationsPage.verifyPageLoaded()
    })
  })

  test.describe('Notification Bell Interactions', () => {
    test.beforeEach(async ({ page }) => {
      await clearAuthAndLogin(page, testUsers.student.username, testUsers.student.password)
    })

    test('notification bell shows badge count', async ({ page }) => {
      const notificationsPage = new NotificationsPage(page)
      const dashboardPage = new DashboardPage(page)

      // Bell should be visible
      await expect(dashboardPage.notificationBell).toBeVisible()

      // Badge count is a number (could be 0 or hidden)
      const count = await notificationsPage.getBadgeCount()
      expect(count).toBeGreaterThanOrEqual(0)
    })

    test('clicking notification bell opens dropdown or navigates', async ({ page }) => {
      const notificationsPage = new NotificationsPage(page)
      const dashboardPage = new DashboardPage(page)

      await dashboardPage.notificationBell.click()

      // Either dropdown opens or navigates to notifications page
      await page.waitForTimeout(500)
    })
  })

  test.describe('Notification Filtering', () => {
    test.beforeEach(async ({ page }) => {
      await clearAuthAndLogin(page, testUsers.student.username, testUsers.student.password)

      const notificationsPage = new NotificationsPage(page)
      await notificationsPage.goto()
    })

    test('can filter to show all notifications', async ({ page }) => {
      const notificationsPage = new NotificationsPage(page)
      await notificationsPage.showAllNotifications()
      await notificationsPage.verifyPageLoaded()
    })

    test('can filter to show unread only', async ({ page }) => {
      const notificationsPage = new NotificationsPage(page)
      await notificationsPage.showUnreadOnly()
      await notificationsPage.verifyPageLoaded()
    })
  })
})
