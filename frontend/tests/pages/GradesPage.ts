import { Page, Locator, expect } from '@playwright/test'

/**
 * Page Object for Grades Management Page
 * Handles grading operations (teacher view)
 */
export class GradesPage {
  readonly page: Page

  // Grade form elements
  readonly gradeForm: Locator
  readonly gradeRadioGroup: Locator
  readonly gradeA: Locator
  readonly gradeB: Locator
  readonly gradeC: Locator
  readonly gradeD: Locator
  readonly commentTextarea: Locator
  readonly quickComments: Locator
  readonly submitButton: Locator
  readonly cancelButton: Locator

  // Status elements
  readonly successMessage: Locator
  readonly errorMessage: Locator
  readonly loadingIndicator: Locator

  constructor(page: Page) {
    this.page = page

    // Grade form
    this.gradeForm = page.locator('.grade-form-card')
    this.gradeRadioGroup = page.locator('.grade-radio-group')
    this.gradeA = page.locator('.el-radio-button').filter({ hasText: 'A' })
    this.gradeB = page.locator('.el-radio-button').filter({ hasText: 'B' })
    this.gradeC = page.locator('.el-radio-button').filter({ hasText: 'C' })
    this.gradeD = page.locator('.el-radio-button').filter({ hasText: 'D' })
    this.commentTextarea = page.locator('textarea[placeholder*="评语"]')
    this.quickComments = page.locator('.comment-tag')
    this.submitButton = page.locator('.grade-form-card button').filter({ hasText: /提交|更新/ })
    this.cancelButton = page.locator('.grade-form-card button').filter({ hasText: '取消' })

    // Status
    this.successMessage = page.locator('.el-message--success')
    this.errorMessage = page.locator('.el-message--error')
    this.loadingIndicator = page.locator('.el-loading-mask')
  }

  /**
   * Navigate to grades page
   */
  async goto() {
    await this.page.goto('/grades')
    await this.page.waitForLoadState('networkidle')
  }

  /**
   * Select a grade level
   */
  async selectGrade(grade: 'A' | 'B' | 'C' | 'D') {
    const gradeButton = {
      A: this.gradeA,
      B: this.gradeB,
      C: this.gradeC,
      D: this.gradeD
    }[grade]

    await gradeButton.click()
  }

  /**
   * Enter comment for grade
   */
  async enterComment(comment: string) {
    await this.commentTextarea.fill(comment)
  }

  /**
   * Click a quick comment to add it
   */
  async addQuickComment(commentText: string) {
    const tag = this.quickComments.filter({ hasText: commentText })
    await tag.click()
  }

  /**
   * Submit the grade
   */
  async submitGrade() {
    await this.submitButton.click()
    await this.page.waitForTimeout(500)
  }

  /**
   * Cancel grading
   */
  async cancelGrade() {
    await this.cancelButton.click()
  }

  /**
   * Complete grading workflow
   */
  async gradeSubmission(grade: 'A' | 'B' | 'C' | 'D', comment?: string) {
    await this.selectGrade(grade)

    if (comment) {
      await this.enterComment(comment)
    }

    await this.submitGrade()
  }

  /**
   * Check if success message is shown
   */
  async hasSuccessMessage(): Promise<boolean> {
    try {
      await this.successMessage.waitFor({ state: 'visible', timeout: 5000 })
      return true
    } catch {
      return false
    }
  }

  /**
   * Check if error message is shown
   */
  async hasErrorMessage(): Promise<boolean> {
    try {
      await this.errorMessage.waitFor({ state: 'visible', timeout: 5000 })
      return true
    } catch {
      return false
    }
  }

  /**
   * Verify grade form is visible
   */
  async verifyGradeFormVisible() {
    await expect(this.gradeForm).toBeVisible()
    await expect(this.gradeRadioGroup).toBeVisible()
    await expect(this.commentTextarea).toBeVisible()
  }
}

/**
 * Page Object for Grade Summary Page
 * Shows grade statistics and summaries
 */
export class GradeSummaryPage {
  readonly page: Page

  // Header elements
  readonly pageHeader: Locator
  readonly viewTypeSelect: Locator
  readonly lessonSelect: Locator
  readonly studentSelect: Locator

  // Statistics elements
  readonly statisticsSection: Locator
  readonly gradedCountStat: Locator
  readonly averageGradeStat: Locator
  readonly passRateStat: Locator
  readonly exportButton: Locator

  // Distribution chart
  readonly distributionChart: Locator
  readonly gradeBarA: Locator
  readonly gradeBarB: Locator
  readonly gradeBarC: Locator
  readonly gradeBarD: Locator

  // Data table
  readonly dataTable: Locator
  readonly tableRows: Locator

  constructor(page: Page) {
    this.page = page

    // Header
    this.pageHeader = page.locator('.header h2')
    this.viewTypeSelect = page.locator('.filters .el-select').first()
    this.lessonSelect = page.locator('.filters .el-select').nth(1)
    this.studentSelect = page.locator('.filters .el-select').nth(1)

    // Statistics
    this.statisticsSection = page.locator('.statistics')
    this.gradedCountStat = page.locator('.el-statistic').filter({ hasText: '已评分数' })
    this.averageGradeStat = page.locator('.el-statistic').filter({ hasText: '平均成绩' })
    this.passRateStat = page.locator('.el-statistic').filter({ hasText: '及格率' })
    this.exportButton = page.locator('button').filter({ hasText: '导出Excel' })

    // Distribution chart
    this.distributionChart = page.locator('.distribution-chart')
    this.gradeBarA = page.locator('.grade-bar').filter({ hasText: 'A' })
    this.gradeBarB = page.locator('.grade-bar').filter({ hasText: 'B' })
    this.gradeBarC = page.locator('.grade-bar').filter({ hasText: 'C' })
    this.gradeBarD = page.locator('.grade-bar').filter({ hasText: 'D' })

    // Data table
    this.dataTable = page.locator('.el-table')
    this.tableRows = page.locator('.el-table__body-wrapper .el-table__row')
  }

  /**
   * Navigate to grade summary page
   */
  async goto() {
    await this.page.goto('/grade-summary')
    await this.page.waitForLoadState('networkidle')
  }

  /**
   * Select view type (lesson or student)
   */
  async selectViewType(viewType: 'lesson' | 'student') {
    await this.viewTypeSelect.click()
    await this.page.waitForTimeout(300)

    const optionText = viewType === 'lesson' ? '按课程' : '按学员'
    const option = this.page.locator('.el-select-dropdown__item').filter({ hasText: optionText })
    await option.click()

    await this.page.waitForTimeout(500)
  }

  /**
   * Select a lesson (when view type is 'lesson')
   */
  async selectLesson(lessonTitle: string) {
    await this.lessonSelect.click()
    await this.page.waitForTimeout(300)

    const option = this.page.locator('.el-select-dropdown__item').filter({ hasText: lessonTitle })
    await option.click()

    await this.page.waitForLoadState('networkidle')
  }

  /**
   * Select a student (when view type is 'student')
   */
  async selectStudent(studentName: string) {
    await this.studentSelect.click()
    await this.page.waitForTimeout(300)

    const option = this.page.locator('.el-select-dropdown__item').filter({ hasText: studentName })
    await option.click()

    await this.page.waitForLoadState('networkidle')
  }

  /**
   * Export grades to Excel
   */
  async exportGrades() {
    // Wait for download event
    const downloadPromise = this.page.waitForEvent('download')
    await this.exportButton.click()
    const download = await downloadPromise

    return download.suggestedFilename()
  }

  /**
   * Get grade distribution values
   */
  async getGradeDistribution(): Promise<{ A: number; B: number; C: number; D: number }> {
    const getCount = async (bar: Locator): Promise<number> => {
      const countText = await bar.locator('.grade-count').textContent()
      return parseInt(countText || '0', 10)
    }

    return {
      A: await getCount(this.gradeBarA),
      B: await getCount(this.gradeBarB),
      C: await getCount(this.gradeBarC),
      D: await getCount(this.gradeBarD)
    }
  }

  /**
   * Get row count in data table
   */
  async getTableRowCount(): Promise<number> {
    return await this.tableRows.count()
  }

  /**
   * Verify grade summary page loaded
   */
  async verifyPageLoaded() {
    await expect(this.pageHeader).toContainText('成绩汇总')
    await expect(this.viewTypeSelect).toBeVisible()
  }

  /**
   * Verify statistics are displayed
   */
  async verifyStatisticsDisplayed() {
    await expect(this.statisticsSection).toBeVisible()
    await expect(this.gradedCountStat).toBeVisible()
    await expect(this.averageGradeStat).toBeVisible()
    await expect(this.passRateStat).toBeVisible()
  }

  /**
   * Verify distribution chart is displayed
   */
  async verifyDistributionChartDisplayed() {
    await expect(this.distributionChart).toBeVisible()
  }
}
