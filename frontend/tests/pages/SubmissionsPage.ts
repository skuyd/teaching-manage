import { Page, Locator, expect } from '@playwright/test'
import * as path from 'path'
import * as fs from 'fs'
import * as os from 'os'

/**
 * Page Object for Submissions Page
 * Handles assignment submission operations
 */
export class SubmissionsPage {
  readonly page: Page

  // Header elements
  readonly pageHeader: Locator
  readonly backButton: Locator

  // Selection elements
  readonly lessonSelect: Locator
  readonly lessonOptions: Locator

  // Action buttons
  readonly submitButton: Locator
  readonly resubmitButton: Locator

  // My submission card (student view)
  readonly mySubmissionCard: Locator
  readonly submissionInfo: Locator
  readonly viewDetailButton: Locator
  readonly deleteButton: Locator

  // Submissions table (teacher view)
  readonly submissionsTable: Locator
  readonly submissionRows: Locator

  // Upload dialog
  readonly uploadDialog: Locator
  readonly uploadArea: Locator
  readonly uploadInput: Locator
  readonly uploadSubmitButton: Locator
  readonly uploadCancelButton: Locator

  // Resubmit dialog
  readonly resubmitDialog: Locator
  readonly resubmitArea: Locator
  readonly resubmitInput: Locator
  readonly resubmitSubmitButton: Locator
  readonly resubmitCancelButton: Locator

  // Status elements
  readonly gradedTag: Locator
  readonly ungradedTag: Locator
  readonly loadingIndicator: Locator

  constructor(page: Page) {
    this.page = page

    // Header
    this.pageHeader = page.locator('.page-title')
    this.backButton = page.locator('.el-page-header__back')

    // Selection
    this.lessonSelect = page.locator('.el-select').first()
    this.lessonOptions = page.locator('.el-select-dropdown__item')

    // Actions
    this.submitButton = page.locator('button').filter({ hasText: '提交作业' })
    this.resubmitButton = page.locator('button').filter({ hasText: '重新提交' })

    // My submission (student)
    this.mySubmissionCard = page.locator('.list-card').filter({ hasText: '我的提交' })
    this.submissionInfo = page.locator('.el-descriptions')
    this.viewDetailButton = page.locator('button').filter({ hasText: '查看详情' })
    this.deleteButton = page.locator('button').filter({ hasText: '删除' })

    // Submissions table (teacher)
    this.submissionsTable = page.locator('.el-table')
    this.submissionRows = page.locator('.el-table__body-wrapper .el-table__row')

    // Upload dialog
    this.uploadDialog = page.locator('.el-dialog').filter({ hasText: '提交作业' })
    this.uploadArea = this.uploadDialog.locator('.el-upload-dragger')
    this.uploadInput = this.uploadDialog.locator('input[type="file"]')
    this.uploadSubmitButton = this.uploadDialog.locator('.el-dialog__footer button').filter({ hasText: '提交' })
    this.uploadCancelButton = this.uploadDialog.locator('.el-dialog__footer button').filter({ hasText: '取消' })

    // Resubmit dialog
    this.resubmitDialog = page.locator('.el-dialog').filter({ hasText: '重新提交作业' })
    this.resubmitArea = this.resubmitDialog.locator('.el-upload-dragger')
    this.resubmitInput = this.resubmitDialog.locator('input[type="file"]')
    this.resubmitSubmitButton = this.resubmitDialog.locator('.el-dialog__footer button').filter({ hasText: '重新提交' })
    this.resubmitCancelButton = this.resubmitDialog.locator('.el-dialog__footer button').filter({ hasText: '取消' })

    // Status
    this.gradedTag = page.locator('.el-tag').filter({ hasText: '已评分' })
    this.ungradedTag = page.locator('.el-tag').filter({ hasText: '未评分' })
    this.loadingIndicator = page.locator('.el-loading-mask')
  }

  /**
   * Navigate to submissions page
   */
  async goto() {
    await this.page.goto('/submissions')
    await this.page.waitForLoadState('networkidle')
  }

  /**
   * Wait for loading to complete
   */
  async waitForLoad() {
    const loadingMask = this.page.locator('.el-loading-mask')
    if (await loadingMask.isVisible()) {
      await loadingMask.waitFor({ state: 'hidden', timeout: 10000 })
    }
  }

  /**
   * Select a lesson from dropdown
   */
  async selectLesson(lessonTitle: string) {
    await this.lessonSelect.click()
    await this.page.waitForTimeout(300)

    const option = this.page.locator('.el-select-dropdown__item').filter({ hasText: lessonTitle })
    await option.click()

    await this.waitForLoad()
  }

  /**
   * Open submit assignment dialog
   */
  async openSubmitDialog() {
    await this.submitButton.click()
    await this.uploadDialog.waitFor({ state: 'visible' })
  }

  /**
   * Create a test file and return its path
   */
  async createTestFile(content: string, filename: string): Promise<string> {
    const tempDir = os.tmpdir()
    const filePath = path.join(tempDir, filename)
    fs.writeFileSync(filePath, content)
    return filePath
  }

  /**
   * Submit an assignment with a file
   */
  async submitAssignment(fileContent: string, filename: string = 'test_submission.py') {
    await this.openSubmitDialog()

    // Create test file
    const filePath = await this.createTestFile(fileContent, filename)

    // Upload file
    await this.uploadInput.setInputFiles(filePath)
    await this.page.waitForTimeout(500)

    // Submit
    await this.uploadSubmitButton.click()

    // Wait for dialog to close
    await this.uploadDialog.waitFor({ state: 'hidden', timeout: 30000 })

    // Clean up temp file
    try {
      fs.unlinkSync(filePath)
    } catch {
      // Ignore cleanup errors
    }

    await this.waitForLoad()
  }

  /**
   * Open resubmit dialog
   */
  async openResubmitDialog() {
    await this.resubmitButton.click()
    await this.resubmitDialog.waitFor({ state: 'visible' })
  }

  /**
   * Resubmit an assignment with a file
   */
  async resubmitAssignment(fileContent: string, filename: string = 'test_resubmission.py') {
    await this.openResubmitDialog()

    // Create test file
    const filePath = await this.createTestFile(fileContent, filename)

    // Upload file
    await this.resubmitInput.setInputFiles(filePath)
    await this.page.waitForTimeout(500)

    // Submit
    await this.resubmitSubmitButton.click()

    // Wait for dialog to close
    await this.resubmitDialog.waitFor({ state: 'hidden', timeout: 30000 })

    // Clean up temp file
    try {
      fs.unlinkSync(filePath)
    } catch {
      // Ignore cleanup errors
    }

    await this.waitForLoad()
  }

  /**
   * View submission detail
   */
  async viewSubmissionDetail() {
    await this.viewDetailButton.click()
    await this.page.waitForURL(/\/submissions\/\d+/)
  }

  /**
   * Delete my submission
   */
  async deleteSubmission() {
    await this.deleteButton.click()

    // Confirm deletion
    const confirmButton = this.page.locator('.el-popconfirm button').filter({ hasText: '确定' })
    await confirmButton.click()

    await this.waitForLoad()
  }

  /**
   * Check if my submission exists
   */
  async hasMySubmission(): Promise<boolean> {
    return await this.mySubmissionCard.isVisible()
  }

  /**
   * Check if submission is graded
   */
  async isSubmissionGraded(): Promise<boolean> {
    return await this.gradedTag.first().isVisible()
  }

  /**
   * Get submission count from table (teacher view)
   */
  async getSubmissionCount(): Promise<number> {
    return await this.submissionRows.count()
  }

  /**
   * Click view button for a submission by submitter name (teacher view)
   */
  async viewSubmissionByStudent(studentName: string) {
    const row = this.submissionRows.filter({ hasText: studentName })
    await row.locator('button').filter({ hasText: '查看' }).click()
    await this.page.waitForURL(/\/submissions\/\d+/)
  }

  /**
   * Verify page loaded
   */
  async verifyPageLoaded() {
    await expect(this.pageHeader).toContainText('作业管理')
    await expect(this.lessonSelect).toBeVisible()
  }

  /**
   * Go back
   */
  async goBack() {
    await this.backButton.click()
    await this.page.waitForLoadState('networkidle')
  }
}
