import { Page, Locator, expect } from '@playwright/test'

/**
 * Page Object for User Management Page
 * Admin-only functionality for managing users
 */
export class UsersPage {
  readonly page: Page

  // Page elements
  readonly pageTitle: Locator
  readonly createUserButton: Locator
  readonly searchInput: Locator
  readonly roleFilter: Locator
  readonly userTable: Locator
  readonly tableRows: Locator
  readonly pagination: Locator

  // User form dialog elements
  readonly userFormDialog: Locator
  readonly usernameInput: Locator
  readonly passwordInput: Locator
  readonly nameInput: Locator
  readonly emailInput: Locator
  readonly roleSelect: Locator
  readonly submitButton: Locator
  readonly cancelButton: Locator

  // Action buttons in table
  readonly editButtons: Locator
  readonly deleteButtons: Locator
  readonly resetPasswordButtons: Locator

  // Confirmation dialog
  readonly confirmDialog: Locator
  readonly confirmButton: Locator
  readonly cancelConfirmButton: Locator

  constructor(page: Page) {
    this.page = page

    // Page elements
    this.pageTitle = page.locator('h2, .page-title').first()
    this.createUserButton = page.locator('button').filter({ hasText: /新增|创建|添加/ })
    this.searchInput = page.locator('input[placeholder*="搜索"], input[placeholder*="用户名"]')
    this.roleFilter = page.locator('.el-select').filter({ has: page.locator('[placeholder*="角色"]') })
    this.userTable = page.locator('.el-table')
    this.tableRows = page.locator('.el-table__body-wrapper .el-table__row')
    this.pagination = page.locator('.el-pagination')

    // User form dialog
    this.userFormDialog = page.locator('.el-dialog').filter({ has: page.locator('[class*="user"]') })
    this.usernameInput = page.locator('.el-dialog input[placeholder*="用户名"]')
    this.passwordInput = page.locator('.el-dialog input[type="password"]')
    this.nameInput = page.locator('.el-dialog input[placeholder*="姓名"]')
    this.emailInput = page.locator('.el-dialog input[placeholder*="邮箱"]')
    this.roleSelect = page.locator('.el-dialog .el-select')
    this.submitButton = page.locator('.el-dialog button').filter({ hasText: /确定|保存|提交/ })
    this.cancelButton = page.locator('.el-dialog button').filter({ hasText: /取消/ })

    // Action buttons
    this.editButtons = page.locator('.el-table button').filter({ hasText: /编辑/ })
    this.deleteButtons = page.locator('.el-table button').filter({ hasText: /删除/ })
    this.resetPasswordButtons = page.locator('.el-table button').filter({ hasText: /重置/ })

    // Confirmation dialog
    this.confirmDialog = page.locator('.el-message-box')
    this.confirmButton = page.locator('.el-message-box button').filter({ hasText: /确定|是/ })
    this.cancelConfirmButton = page.locator('.el-message-box button').filter({ hasText: /取消|否/ })
  }

  /**
   * Navigate to users page
   */
  async goto() {
    await this.page.goto('/admin/users')
    await this.page.waitForLoadState('networkidle')
  }

  /**
   * Verify page loaded correctly
   */
  async verifyPageLoaded() {
    await expect(this.userTable).toBeVisible()
    // Wait for data to load
    await this.page.waitForLoadState('networkidle')
    // Wait for table rows to appear (at least one user should exist)
    await this.tableRows.first().waitFor({ state: 'visible', timeout: 10000 }).catch(() => {
      // Table might be empty, that's okay for some tests
    })
    await this.page.waitForTimeout(300)
  }

  /**
   * Create a new user
   */
  async createUser(userData: {
    username: string
    password: string
    name: string
    email?: string
    role?: string
  }) {
    await this.createUserButton.click()
    await expect(this.usernameInput).toBeVisible()

    await this.usernameInput.fill(userData.username)
    await this.passwordInput.fill(userData.password)
    await this.nameInput.fill(userData.name)

    if (userData.email) {
      await this.emailInput.fill(userData.email)
    }

    if (userData.role) {
      await this.roleSelect.click()
      await this.page.locator('.el-select-dropdown__item').filter({ hasText: userData.role }).click()
    }

    await this.submitButton.click()
    await this.page.waitForTimeout(500)
  }

  /**
   * Search for a user
   */
  async searchUser(query: string) {
    await this.searchInput.fill(query)
    await this.searchInput.press('Enter')
    await this.page.waitForTimeout(500)
  }

  /**
   * Filter by role
   */
  async filterByRole(role: string) {
    await this.roleFilter.click()
    await this.page.locator('.el-select-dropdown__item').filter({ hasText: role }).click()
    await this.page.waitForTimeout(500)
  }

  /**
   * Get row count
   */
  async getRowCount(): Promise<number> {
    return await this.tableRows.count()
  }

  /**
   * Edit user in row
   */
  async editUser(rowIndex: number) {
    const editButton = this.editButtons.nth(rowIndex)
    await editButton.click()
    await expect(this.usernameInput).toBeVisible()
  }

  /**
   * Delete user in row
   */
  async deleteUser(rowIndex: number) {
    const deleteButton = this.deleteButtons.nth(rowIndex)
    await deleteButton.click()
    await expect(this.confirmDialog).toBeVisible()
    await this.confirmButton.click()
    await this.page.waitForTimeout(500)
  }

  /**
   * Check if user exists in table
   */
  async userExists(username: string): Promise<boolean> {
    // Wait for table data to load
    await this.page.waitForTimeout(500)
    const cell = this.page.locator('.el-table__body-wrapper').getByText(username, { exact: true }).first()
    return await cell.isVisible()
  }
}
