import { Page, Locator, expect } from '@playwright/test'

/**
 * Page Object for Login Page
 * Handles authentication-related actions
 */
export class LoginPage {
  readonly page: Page

  // Login form elements
  readonly usernameInput: Locator
  readonly passwordInput: Locator
  readonly loginButton: Locator
  readonly loginTab: Locator

  // Register form elements
  readonly registerTab: Locator
  readonly registerUsernameInput: Locator
  readonly registerPasswordInput: Locator
  readonly registerNameInput: Locator
  readonly registerEmailInput: Locator
  readonly registerButton: Locator

  // UI elements
  readonly pageTitle: Locator
  readonly errorMessage: Locator
  readonly successMessage: Locator

  constructor(page: Page) {
    this.page = page

    // Login form - simple form without tabs
    this.loginTab = page.locator('h1, .title').filter({ hasText: '教学管理系统' }) // No tabs, use title as fallback
    this.usernameInput = page.locator('input[placeholder="用户名"]')
    this.passwordInput = page.locator('input[placeholder="密码"]')
    this.loginButton = page.locator('button').filter({ hasText: '登录' })

    // Register form (may not exist in current UI)
    this.registerTab = page.locator('.el-tabs__item').filter({ hasText: '注册' })
    this.registerUsernameInput = page.locator('input[placeholder="用户名"]').nth(1)
    this.registerPasswordInput = page.locator('input[placeholder="密码"]').nth(1)
    this.registerNameInput = page.locator('input[placeholder="姓名"]')
    this.registerEmailInput = page.locator('input[placeholder*="邮箱"]')
    this.registerButton = page.locator('button').filter({ hasText: '注册' })

    // UI
    this.pageTitle = page.locator('h1, .title')
    this.errorMessage = page.locator('.el-message--error')
    this.successMessage = page.locator('.el-message--success')
  }

  /**
   * Navigate to login page
   */
  async goto() {
    await this.page.goto('/login')
    await this.page.waitForLoadState('networkidle')
  }

  /**
   * Login with credentials
   */
  async login(username: string, password: string) {
    // Wait for login form to be visible
    await this.usernameInput.waitFor({ state: 'visible', timeout: 10000 })

    // Fill credentials
    await this.usernameInput.fill(username)
    await this.passwordInput.fill(password)

    // Submit
    await this.loginButton.click()
  }

  /**
   * Login and wait for navigation
   */
  async loginAndWait(username: string, password: string) {
    await this.login(username, password)

    // Wait for either success message or navigation
    await Promise.race([
      this.page.waitForURL('/', { timeout: 10000 }),
      this.page.waitForURL('/login', { timeout: 10000 })
    ])
  }

  /**
   * Switch to register tab
   */
  async switchToRegister() {
    await this.registerTab.click()
    await this.page.waitForTimeout(300)
  }

  /**
   * Register a new user
   */
  async register(username: string, password: string, name: string, email?: string) {
    await this.switchToRegister()

    await this.registerUsernameInput.fill(username)
    await this.registerPasswordInput.fill(password)
    await this.registerNameInput.fill(name)
    if (email) {
      await this.registerEmailInput.fill(email)
    }

    await this.registerButton.click()
  }

  /**
   * Check if login page is visible
   */
  async isVisible(): Promise<boolean> {
    return await this.pageTitle.isVisible()
  }

  /**
   * Check if error message is displayed
   */
  async hasError(): Promise<boolean> {
    return await this.errorMessage.isVisible()
  }

  /**
   * Get error message text
   */
  async getErrorMessage(): Promise<string> {
    await this.errorMessage.waitFor({ state: 'visible', timeout: 5000 })
    return await this.errorMessage.textContent() || ''
  }

  /**
   * Check if success message is displayed
   */
  async hasSuccess(): Promise<boolean> {
    try {
      await this.successMessage.waitFor({ state: 'visible', timeout: 5000 })
      return true
    } catch {
      return false
    }
  }

  /**
   * Get success message text
   */
  async getSuccessMessage(): Promise<string> {
    await this.successMessage.waitFor({ state: 'visible', timeout: 5000 })
    return await this.successMessage.textContent() || ''
  }

  /**
   * Verify login page elements are present
   */
  async verifyPageElements() {
    await expect(this.pageTitle).toContainText('教学管理系统')
    await expect(this.usernameInput).toBeVisible()
    await expect(this.passwordInput).toBeVisible()
    await expect(this.loginButton).toBeVisible()
  }
}
