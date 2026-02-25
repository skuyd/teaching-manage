import { Page, Locator, expect } from '@playwright/test'

/**
 * Page Object for Notifications Page
 * Handles notification center interactions
 */
export class NotificationsPage {
  readonly page: Page

  // Page elements
  readonly pageTitle: Locator
  readonly notificationList: Locator
  readonly notificationItems: Locator
  readonly emptyState: Locator
  readonly markAllReadButton: Locator
  readonly refreshButton: Locator

  // Notification bell (in header)
  readonly notificationBell: Locator
  readonly notificationBadge: Locator
  readonly notificationDropdown: Locator

  // Individual notification elements
  readonly unreadNotifications: Locator
  readonly readNotifications: Locator

  // Filter tabs
  readonly allTab: Locator
  readonly unreadTab: Locator

  constructor(page: Page) {
    this.page = page

    // Page elements - use notification-center container for specificity
    this.pageTitle = page.locator('.notification-center h2').filter({ hasText: /通知中心/ })
    this.notificationList = page.locator('.notification-center .notification-list').first()
    this.notificationItems = page.locator('.notification-center .notification-card')
    this.emptyState = page.locator('.notification-center .el-empty').first()
    this.markAllReadButton = page.locator('.notification-center button').filter({ hasText: /全部已读/ })
    this.refreshButton = page.locator('.notification-center button').filter({ hasText: /刷新/ })

    // Notification bell (in header)
    this.notificationBell = page.locator('.notification-bell, .bell-button').first()
    this.notificationBadge = page.locator('.notification-badge .el-badge__content, .el-badge__content')
    this.notificationDropdown = page.locator('.notification-dropdown')

    // Notification status
    this.unreadNotifications = page.locator('.notification-center .notification-card.unread')
    this.readNotifications = page.locator('.notification-center .notification-card.read')

    // Filter tabs
    this.allTab = page.locator('.notification-center .el-tabs__item').filter({ hasText: /全部通知/ })
    this.unreadTab = page.locator('.notification-center .el-tabs__item').filter({ hasText: /未读通知/ })
  }

  /**
   * Navigate to notifications page
   */
  async goto() {
    await this.page.goto('/notifications')
    await this.page.waitForLoadState('networkidle')
  }

  /**
   * Verify page loaded correctly
   */
  async verifyPageLoaded() {
    // Wait for page title first
    await expect(this.pageTitle).toBeVisible({ timeout: 5000 })

    // Wait for tabs to be visible (which means content is loaded)
    const tabsVisible = await this.page.locator('.notification-center .el-tabs').isVisible()
    expect(tabsVisible).toBe(true)
  }

  /**
   * Get notification count
   */
  async getNotificationCount(): Promise<number> {
    return await this.notificationItems.count()
  }

  /**
   * Get unread notification count
   */
  async getUnreadCount(): Promise<number> {
    return await this.unreadNotifications.count()
  }

  /**
   * Mark all notifications as read
   */
  async markAllAsRead() {
    if (await this.markAllReadButton.isVisible()) {
      await this.markAllReadButton.click()
      await this.page.waitForTimeout(500)
    }
  }

  /**
   * Click on a notification
   */
  async clickNotification(index: number) {
    const notification = this.notificationItems.nth(index)
    await notification.click()
    await this.page.waitForTimeout(300)
  }

  /**
   * Open notification bell dropdown
   */
  async openNotificationBell() {
    await this.notificationBell.click()
    await expect(this.notificationDropdown).toBeVisible({ timeout: 3000 })
  }

  /**
   * Get badge count from notification bell
   */
  async getBadgeCount(): Promise<number> {
    const isVisible = await this.notificationBadge.isVisible()
    if (!isVisible) return 0

    const text = await this.notificationBadge.textContent()
    return parseInt(text || '0', 10)
  }

  /**
   * Filter by all notifications
   */
  async showAllNotifications() {
    if (await this.allTab.isVisible()) {
      await this.allTab.click()
      await this.page.waitForTimeout(300)
    }
  }

  /**
   * Filter by unread notifications only
   */
  async showUnreadOnly() {
    if (await this.unreadTab.isVisible()) {
      await this.unreadTab.click()
      await this.page.waitForTimeout(300)
    }
  }

  /**
   * Delete a notification
   */
  async deleteNotification(index: number) {
    const notification = this.notificationItems.nth(index)
    const deleteButton = notification.locator('button').filter({ hasText: /删除/ })
    if (await deleteButton.isVisible()) {
      await deleteButton.click()
      await this.page.waitForTimeout(500)
    }
  }

  /**
   * Check if empty state is shown
   */
  async isEmptyStateVisible(): Promise<boolean> {
    return await this.emptyState.isVisible()
  }
}
