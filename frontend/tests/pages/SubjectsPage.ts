import { Page, Locator, expect } from '@playwright/test'

/**
 * Page Object for Subjects Management Page
 * Handles subject CRUD operations
 */
export class SubjectsPage {
  readonly page: Page

  // Header elements
  readonly pageHeader: Locator
  readonly createButton: Locator

  // Search elements
  readonly searchInput: Locator
  readonly searchButton: Locator

  // Table elements
  readonly subjectsTable: Locator
  readonly tableRows: Locator
  readonly emptyState: Locator
  readonly loadingIndicator: Locator

  // Pagination
  readonly pagination: Locator
  readonly pageSize: Locator
  readonly prevPage: Locator
  readonly nextPage: Locator

  // Dialog elements
  readonly dialog: Locator
  readonly dialogTitle: Locator
  readonly dialogCloseButton: Locator
  readonly dialogConfirmButton: Locator
  readonly dialogCancelButton: Locator

  // Form elements
  readonly nameInput: Locator
  readonly descriptionInput: Locator
  readonly isGroupedSwitch: Locator
  readonly minMembersInput: Locator
  readonly maxMembersInput: Locator
  readonly dateRangePicker: Locator

  constructor(page: Page) {
    this.page = page

    // Header
    this.pageHeader = page.locator('.header h2')
    this.createButton = page.locator('button').filter({ hasText: '新增学科' })

    // Search
    this.searchInput = page.locator('input[placeholder="搜索学科名称"]')
    this.searchButton = page.locator('.el-input-group__append button')

    // Subject cards (the UI uses cards, not table)
    this.subjectsTable = page.locator('.subject-cards')
    this.tableRows = page.locator('.subject-card')
    this.emptyState = page.locator('.el-empty')
    this.loadingIndicator = page.locator('.el-loading-mask')

    // Pagination
    this.pagination = page.locator('.el-pagination')
    this.pageSize = page.locator('.el-pagination .el-select')
    this.prevPage = page.locator('.el-pagination .btn-prev')
    this.nextPage = page.locator('.el-pagination .btn-next')

    // Dialog
    this.dialog = page.locator('.el-dialog')
    this.dialogTitle = page.locator('.el-dialog__title')
    this.dialogCloseButton = page.locator('.el-dialog__headerbtn')
    this.dialogConfirmButton = page.locator('.el-dialog__footer button').filter({ hasText: '确定' })
    this.dialogCancelButton = page.locator('.el-dialog__footer button').filter({ hasText: '取消' })

    // Form
    this.nameInput = page.locator('input[placeholder="请输入学科名称"]')
    this.descriptionInput = page.locator('textarea[placeholder="请输入学科描述"]')
    this.isGroupedSwitch = page.locator('.el-switch')
    this.minMembersInput = page.locator('.el-input-number').first()
    this.maxMembersInput = page.locator('.el-input-number').last()
    this.dateRangePicker = page.locator('.el-date-editor--daterange')
  }

  /**
   * Navigate to subjects page
   */
  async goto() {
    await this.page.goto('/subjects')
    await this.page.waitForLoadState('networkidle')
    await this.waitForTableLoad()
  }

  /**
   * Wait for table to finish loading
   */
  async waitForTableLoad() {
    await this.page.waitForTimeout(500)
    // Wait for loading mask to disappear
    const loadingMask = this.page.locator('.el-loading-mask')
    if (await loadingMask.isVisible()) {
      await loadingMask.waitFor({ state: 'hidden', timeout: 10000 })
    }
  }

  /**
   * Open create subject dialog
   */
  async openCreateDialog() {
    await this.createButton.click()
    await this.dialog.waitFor({ state: 'visible' })
  }

  /**
   * Fill subject form
   */
  async fillSubjectForm(data: {
    name: string
    description?: string
    isGrouped?: boolean
    minMembers?: number
    maxMembers?: number
    startDate?: string
    endDate?: string
  }) {
    await this.nameInput.fill(data.name)

    if (data.description) {
      await this.descriptionInput.fill(data.description)
    }

    if (data.isGrouped !== undefined) {
      const isCurrentlyGrouped = await this.isGroupedSwitch.locator('input').isChecked()
      if (isCurrentlyGrouped !== data.isGrouped) {
        await this.isGroupedSwitch.click()
        // Wait for input-number fields to appear/disappear
        await this.page.waitForTimeout(300)
      }
    }

    if (data.isGrouped && data.minMembers !== undefined) {
      // Wait for input-number to be visible
      await this.minMembersInput.waitFor({ state: 'visible' })

      // Get the input element
      const minInput = this.minMembersInput.locator('input')

      // Set value directly using JavaScript and trigger input/change events
      await minInput.evaluate((el, value) => {
        (el as HTMLInputElement).value = String(value);
        el.dispatchEvent(new Event('input', { bubbles: true }));
        el.dispatchEvent(new Event('change', { bubbles: true }));
        el.dispatchEvent(new Event('blur', { bubbles: true }));
      }, data.minMembers)
      await this.page.waitForTimeout(100)
    }

    if (data.isGrouped && data.maxMembers !== undefined) {
      await this.maxMembersInput.waitFor({ state: 'visible' })

      const maxInput = this.maxMembersInput.locator('input')

      // Set value directly using JavaScript and trigger input/change events
      await maxInput.evaluate((el, value) => {
        (el as HTMLInputElement).value = String(value);
        el.dispatchEvent(new Event('input', { bubbles: true }));
        el.dispatchEvent(new Event('change', { bubbles: true }));
        el.dispatchEvent(new Event('blur', { bubbles: true }));
      }, data.maxMembers)
      await this.page.waitForTimeout(100)
    }

    // Date range would require more complex handling
    // Skipping for now as it's optional
  }

  /**
   * Create a new subject
   */
  async createSubject(data: {
    name: string
    description?: string
    isGrouped?: boolean
    minMembers?: number
    maxMembers?: number
  }) {
    // Ensure any existing dialog is closed first
    await this.closeDialog()

    await this.openCreateDialog()
    await this.fillSubjectForm(data)
    await this.dialogConfirmButton.click()

    // Wait for success message to appear, then for dialog to close
    const successMsg = this.page.locator('.el-message--success')
    try {
      await successMsg.waitFor({ state: 'visible', timeout: 15000 })
    } catch {
      // Success message might not appear if there's an error - check if dialog has error message
      const errorMsg = this.page.locator('.el-form-item__error, .el-message--error')
      if (await errorMsg.isVisible()) {
        throw new Error('Form submission failed - validation error visible')
      }
    }

    // Wait for dialog to close and table to refresh
    // Use longer timeout as form submission might take time (30s for slow APIs)
    await this.dialog.waitFor({ state: 'hidden', timeout: 30000 })
    await this.waitForTableLoad()
  }

  /**
   * Search for subjects by keyword
   */
  async searchSubjects(keyword: string) {
    await this.searchInput.fill(keyword)
    await this.searchButton.click()
    await this.waitForTableLoad()
  }

  /**
   * Clear search and show all subjects
   */
  async clearSearch() {
    await this.searchInput.clear()
    await this.searchButton.click()
    await this.waitForTableLoad()
  }

  /**
   * Get subject count from table
   */
  async getSubjectCount(): Promise<number> {
    return await this.tableRows.count()
  }

  /**
   * Get subject card by name
   */
  getSubjectRow(name: string): Locator {
    return this.page.locator('.subject-card').filter({ has: this.page.locator('h3', { hasText: name }) })
  }

  /**
   * Click edit button for a subject
   */
  async editSubject(name: string) {
    const card = this.getSubjectRow(name)
    await card.locator('.card-footer button').filter({ hasText: '编辑' }).click()
    await this.dialog.waitFor({ state: 'visible' })
  }

  /**
   * Click on a subject card to view details
   */
  async viewSubjectDetail(name: string) {
    const card = this.getSubjectRow(name)
    // Click on the card header (not footer buttons) to navigate to detail
    await card.locator('.card-header').click()
    await this.page.waitForURL(/\/subjects\/\d+/)
  }

  /**
   * Delete a subject
   */
  async deleteSubject(name: string) {
    const card = this.getSubjectRow(name)
    await card.locator('.card-footer button').filter({ hasText: '删除' }).click()

    // Wait for confirmation dialog (API fetches stats first, so it takes time)
    // The button text is "确定删除" not "确定"
    const confirmButton = this.page.locator('.el-message-box__btns button').filter({ hasText: /确定删除|确定/ })
    await confirmButton.waitFor({ state: 'visible', timeout: 15000 })
    await confirmButton.click()

    // Wait for success message
    const successMessage = this.page.locator('.el-message').filter({ hasText: '删除成功' })
    await successMessage.waitFor({ state: 'visible', timeout: 10000 })

    // Wait for cards to reload
    await this.page.waitForTimeout(1000)
    await this.waitForTableLoad()
  }

  /**
   * Check if subject exists in cards
   */
  async subjectExists(name: string): Promise<boolean> {
    const row = this.getSubjectRow(name)
    // Wait for subject to appear with timeout
    try {
      await row.waitFor({ state: 'visible', timeout: 5000 })
      return true
    } catch {
      return false
    }
  }

  /**
   * Get pagination total text
   */
  async getPaginationTotal(): Promise<string> {
    const totalElement = this.page.locator('.el-pagination__total')
    return await totalElement.textContent() || ''
  }

  /**
   * Verify subjects page loaded
   */
  async verifyPageLoaded() {
    await expect(this.pageHeader).toContainText('学科管理')
    await expect(this.createButton).toBeVisible()
    await expect(this.searchInput).toBeVisible()
  }

  /**
   * Close dialog if open
   */
  async closeDialog() {
    if (await this.dialog.isVisible()) {
      await this.dialogCancelButton.click()
      await this.dialog.waitFor({ state: 'hidden' })
    }
  }
}
