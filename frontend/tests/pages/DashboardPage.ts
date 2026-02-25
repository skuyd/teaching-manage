import { Page, Locator, expect } from '@playwright/test'

/**
 * Page Object for Dashboard Page
 * Main navigation hub after login
 */
export class DashboardPage {
  readonly page: Page

  // Navigation elements
  readonly sidebar: Locator
  readonly logo: Locator
  readonly menuItems: {
    dashboard: Locator
    subjects: Locator
    submissions: Locator
    grades: Locator
    gradeSummary: Locator
    users: Locator
  }

  // User info elements
  readonly userInfo: Locator
  readonly welcomeText: Locator
  readonly roleTag: Locator
  readonly logoutButton: Locator
  readonly notificationBell: Locator

  // Content elements
  readonly welcomeCard: Locator
  readonly statsGrid: Locator
  readonly statCards: Locator

  // Mobile elements
  readonly menuToggle: Locator
  readonly sidebarOverlay: Locator

  constructor(page: Page) {
    this.page = page

    // Navigation
    this.sidebar = page.locator('.sidebar')
    this.logo = page.locator('.logo')
    this.menuItems = {
      dashboard: page.locator('.el-menu-item').filter({ hasText: '仪表板' }),
      subjects: page.locator('.el-menu-item').filter({ hasText: '学科管理' }),
      submissions: page.locator('.el-menu-item').filter({ hasText: '作业管理' }),
      grades: page.locator('.el-menu-item').filter({ hasText: '评分管理' }),
      gradeSummary: page.locator('.el-menu-item').filter({ hasText: '成绩汇总' }),
      users: page.locator('.el-menu-item').filter({ hasText: '用户管理' })
    }

    // User info
    this.userInfo = page.locator('.user-info')
    this.welcomeText = page.locator('.welcome')
    this.roleTag = page.locator('.role-tag')
    this.logoutButton = page.locator('button').filter({ hasText: '退出登录' })
    this.notificationBell = page.locator('.notification-bell, .bell-button').first()

    // Content
    this.welcomeCard = page.locator('.welcome-card')
    this.statsGrid = page.locator('.stats-grid')
    this.statCards = page.locator('.stat-card')

    // Mobile
    this.menuToggle = page.locator('.menu-toggle')
    this.sidebarOverlay = page.locator('.sidebar-overlay')
  }

  /**
   * Navigate to dashboard
   */
  async goto() {
    await this.page.goto('/')
    await this.page.waitForLoadState('networkidle')
  }

  /**
   * Logout from the system
   */
  async logout() {
    await this.logoutButton.click()
    await this.page.waitForURL('/login')
  }

  /**
   * Navigate to subjects page
   */
  async goToSubjects() {
    await this.menuItems.subjects.click()
    await this.page.waitForURL('/subjects')
  }

  /**
   * Navigate to submissions page
   */
  async goToSubmissions() {
    await this.menuItems.submissions.click()
    await this.page.waitForURL('/submissions')
  }

  /**
   * Navigate to grades page
   */
  async goToGrades() {
    await this.menuItems.grades.click()
    await this.page.waitForURL('/grades')
  }

  /**
   * Navigate to grade summary page
   */
  async goToGradeSummary() {
    await this.menuItems.gradeSummary.click()
    await this.page.waitForURL('/grade-summary')
  }

  /**
   * Navigate to users page (admin only)
   */
  async goToUsers() {
    await this.menuItems.users.click()
    await this.page.waitForURL('/admin/users')
  }

  /**
   * Get current user role from UI
   */
  async getUserRole(): Promise<string> {
    const roleText = await this.roleTag.textContent()
    return roleText?.trim() || ''
  }

  /**
   * Get welcome text
   */
  async getWelcomeText(): Promise<string> {
    return await this.welcomeText.textContent() || ''
  }

  /**
   * Check if subjects menu is visible (teacher/admin only)
   */
  async isSubjectsMenuVisible(): Promise<boolean> {
    return await this.menuItems.subjects.isVisible()
  }

  /**
   * Check if grades menu is visible (teacher/admin only)
   */
  async isGradesMenuVisible(): Promise<boolean> {
    return await this.menuItems.grades.isVisible()
  }

  /**
   * Check if users menu is visible (admin only)
   */
  async isUsersMenuVisible(): Promise<boolean> {
    return await this.menuItems.users.isVisible()
  }

  /**
   * Get stats card values
   */
  async getStatValues(): Promise<{ label: string; value: string }[]> {
    const cards = await this.statCards.all()
    const stats: { label: string; value: string }[] = []

    for (const card of cards) {
      const value = await card.locator('.stat-value').textContent() || ''
      const label = await card.locator('.stat-label').textContent() || ''
      stats.push({ label: label.trim(), value: value.trim() })
    }

    return stats
  }

  /**
   * Verify dashboard loaded successfully
   */
  async verifyDashboardLoaded() {
    await expect(this.welcomeCard).toBeVisible()
    await expect(this.statsGrid).toBeVisible()
    await expect(this.userInfo).toBeVisible()
  }

  /**
   * Verify admin dashboard elements
   */
  async verifyAdminDashboard() {
    await expect(this.roleTag).toContainText('管理员')
    await expect(this.menuItems.users).toBeVisible()
    await expect(this.menuItems.subjects).toBeVisible()
  }

  /**
   * Verify teacher dashboard elements
   */
  async verifyTeacherDashboard() {
    await expect(this.roleTag).toContainText('教员')
    await expect(this.menuItems.subjects).toBeVisible()
    await expect(this.menuItems.grades).toBeVisible()
    // Users menu should not be visible for teachers
    await expect(this.menuItems.users).not.toBeVisible()
  }

  /**
   * Verify student dashboard elements
   */
  async verifyStudentDashboard() {
    await expect(this.roleTag).toContainText('学员')
    // Students should not see subjects management
    await expect(this.menuItems.subjects).not.toBeVisible()
    // Students should not see grades management
    await expect(this.menuItems.grades).not.toBeVisible()
    // But should see grade summary
    await expect(this.menuItems.gradeSummary).toBeVisible()
  }

  /**
   * Open mobile menu (for responsive testing)
   */
  async openMobileMenu() {
    if (await this.menuToggle.isVisible()) {
      await this.menuToggle.click()
      await this.page.waitForTimeout(300)
    }
  }

  /**
   * Close mobile menu
   */
  async closeMobileMenu() {
    if (await this.sidebarOverlay.isVisible()) {
      await this.sidebarOverlay.click()
      await this.page.waitForTimeout(300)
    }
  }
}
