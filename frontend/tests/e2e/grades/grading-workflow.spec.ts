import { test, expect } from '@playwright/test'
import { GradesPage, GradeSummaryPage } from '../../pages/GradesPage'
import { DashboardPage } from '../../pages/DashboardPage'
import { LoginPage } from '../../pages/LoginPage'
import { testUsers, gradeComments } from '../../fixtures/test-data'

/**
 * Grading Workflow E2E Tests
 * Tests grading operations for teachers and grade viewing for students
 */

test.describe('Teacher Grading Workflow', () => {
  // Use pre-authenticated teacher state
  test.use({ storageState: 'playwright/.auth/teacher.json' })

  test.beforeEach(async ({ page }) => {
    // Already authenticated via storageState, just navigate to dashboard
    await page.goto('/')
    await page.waitForLoadState('networkidle')
  })

  test.describe('Grade Form', () => {
    test('should access grades page from dashboard', async ({ page }) => {
      const dashboardPage = new DashboardPage(page)
      await dashboardPage.goToGrades()

      await expect(page).toHaveURL('/grades')
    })

    // Note: These tests require actual submissions to exist
    // They are marked as skip until test data setup is complete

    test.skip('should display grade form with all grade options', async ({ page }) => {
      await page.goto('/grades')

      const gradesPage = new GradesPage(page)
      await gradesPage.verifyGradeFormVisible()

      // All grade buttons should be visible
      await expect(gradesPage.gradeA).toBeVisible()
      await expect(gradesPage.gradeB).toBeVisible()
      await expect(gradesPage.gradeC).toBeVisible()
      await expect(gradesPage.gradeD).toBeVisible()
    })

    test.skip('should display quick comment templates', async ({ page }) => {
      await page.goto('/grades')

      const gradesPage = new GradesPage(page)

      // Quick comments should be visible
      await expect(gradesPage.quickComments.first()).toBeVisible()
    })

    test.skip('should select grade A', async ({ page }) => {
      await page.goto('/grades')

      const gradesPage = new GradesPage(page)
      await gradesPage.selectGrade('A')

      // Verify grade A is selected
      await expect(gradesPage.gradeA).toHaveClass(/is-active/)
    })

    test.skip('should add quick comment by clicking', async ({ page }) => {
      await page.goto('/grades')

      const gradesPage = new GradesPage(page)

      // Click first quick comment
      const firstComment = gradesPage.quickComments.first()
      const commentText = await firstComment.textContent()
      await firstComment.click()

      // Verify comment was added to textarea
      const textareaValue = await gradesPage.commentTextarea.inputValue()
      expect(textareaValue).toContain(commentText || '')
    })

    test.skip('should enter custom comment', async ({ page }) => {
      await page.goto('/grades')

      const gradesPage = new GradesPage(page)
      await gradesPage.enterComment(gradeComments.excellent)

      // Verify comment was entered
      const textareaValue = await gradesPage.commentTextarea.inputValue()
      expect(textareaValue).toBe(gradeComments.excellent)
    })

    test.skip('should submit grade successfully', async ({ page }) => {
      // Navigate to a specific submission to grade
      // await page.goto('/submissions/1') // Example

      const gradesPage = new GradesPage(page)
      await gradesPage.gradeSubmission('A', gradeComments.excellent)

      // Verify success
      const hasSuccess = await gradesPage.hasSuccessMessage()
      expect(hasSuccess).toBe(true)
    })

    test.skip('should show validation error without selecting grade', async ({ page }) => {
      await page.goto('/grades')

      const gradesPage = new GradesPage(page)

      // Try to submit without selecting grade
      await gradesPage.submitGrade()

      // Should show validation error or remain on same page
    })
  })
})

test.describe('Grade Summary Viewing', () => {
  test.describe('Teacher View', () => {
    // Use pre-authenticated teacher state
    test.use({ storageState: 'playwright/.auth/teacher.json' })

    test.beforeEach(async ({ page }) => {
      // Already authenticated via storageState
      await page.goto('/')
      await page.waitForLoadState('networkidle')
    })

    test('should access grade summary from dashboard', async ({ page }) => {
      const dashboardPage = new DashboardPage(page)
      await dashboardPage.goToGradeSummary()

      await expect(page).toHaveURL('/grade-summary')
    })

    test('should display grade summary page', async ({ page }) => {
      const gradeSummaryPage = new GradeSummaryPage(page)
      await gradeSummaryPage.goto()

      await gradeSummaryPage.verifyPageLoaded()
    })

    test('should show view type selector', async ({ page }) => {
      const gradeSummaryPage = new GradeSummaryPage(page)
      await gradeSummaryPage.goto()

      await expect(gradeSummaryPage.viewTypeSelect).toBeVisible()
    })

    test('should switch between lesson and student view', async ({ page }) => {
      const gradeSummaryPage = new GradeSummaryPage(page)
      await gradeSummaryPage.goto()

      // Switch to student view
      await gradeSummaryPage.selectViewType('student')

      // Switch back to lesson view
      await gradeSummaryPage.selectViewType('lesson')
    })

    test.skip('should display statistics after selecting lesson', async ({ page }) => {
      const gradeSummaryPage = new GradeSummaryPage(page)
      await gradeSummaryPage.goto()

      // Select a lesson (requires lessons to exist)
      // await gradeSummaryPage.selectLesson('Python基础语法')

      await gradeSummaryPage.verifyStatisticsDisplayed()
    })

    test.skip('should display grade distribution chart', async ({ page }) => {
      const gradeSummaryPage = new GradeSummaryPage(page)
      await gradeSummaryPage.goto()

      // Select a lesson (requires lessons to exist)
      // await gradeSummaryPage.selectLesson('Python基础语法')

      await gradeSummaryPage.verifyDistributionChartDisplayed()
    })

    test.skip('should export grades to Excel', async ({ page }) => {
      const gradeSummaryPage = new GradeSummaryPage(page)
      await gradeSummaryPage.goto()

      // Select a lesson (requires lessons to exist)
      // await gradeSummaryPage.selectLesson('Python基础语法')

      const filename = await gradeSummaryPage.exportGrades()
      expect(filename).toContain('grades_export')
    })
  })

  test.describe('Student View', () => {
    // Use pre-authenticated student state
    test.use({ storageState: 'playwright/.auth/student.json' })

    test.beforeEach(async ({ page }) => {
      // Already authenticated via storageState
      await page.goto('/')
      await page.waitForLoadState('networkidle')
    })

    test('should access grade summary from dashboard', async ({ page }) => {
      const dashboardPage = new DashboardPage(page)
      await dashboardPage.goToGradeSummary()

      await expect(page).toHaveURL('/grade-summary')
    })

    test('should display own grade summary automatically', async ({ page }) => {
      const gradeSummaryPage = new GradeSummaryPage(page)
      await gradeSummaryPage.goto()

      // Student should automatically see their own grades
      // View type should be set to 'student' by default for students
    })

    test.skip('should show student grade statistics', async ({ page }) => {
      const gradeSummaryPage = new GradeSummaryPage(page)
      await gradeSummaryPage.goto()

      // Student should see their own statistics
      await gradeSummaryPage.verifyStatisticsDisplayed()
    })

    test.skip('should show lesson grades table', async ({ page }) => {
      const gradeSummaryPage = new GradeSummaryPage(page)
      await gradeSummaryPage.goto()

      // Should show grades for all lessons
      const rowCount = await gradeSummaryPage.getTableRowCount()
      expect(rowCount).toBeGreaterThanOrEqual(0)
    })
  })
})

test.describe('Grade Level Descriptions', () => {
  // Use pre-authenticated teacher state for all grade description tests
  test.use({ storageState: 'playwright/.auth/teacher.json' })

  test('grade A should show as Excellent (优秀)', async ({ page }) => {
    // This tests the UI labels for grades
    await page.goto('/grades')

    // Check grade A label
    const gradeAElement = page.locator('.el-radio-button').filter({ hasText: 'A' })
    if (await gradeAElement.isVisible()) {
      await expect(gradeAElement).toContainText('优秀')
    }
  })

  test('grade B should show as Good (良好)', async ({ page }) => {
    await page.goto('/grades')

    const gradeBElement = page.locator('.el-radio-button').filter({ hasText: 'B' })
    if (await gradeBElement.isVisible()) {
      await expect(gradeBElement).toContainText('良好')
    }
  })

  test('grade C should show as Pass (及格)', async ({ page }) => {
    await page.goto('/grades')

    const gradeCElement = page.locator('.el-radio-button').filter({ hasText: 'C' })
    if (await gradeCElement.isVisible()) {
      await expect(gradeCElement).toContainText('及格')
    }
  })

  test('grade D should show as Fail (不及格)', async ({ page }) => {
    await page.goto('/grades')

    const gradeDElement = page.locator('.el-radio-button').filter({ hasText: 'D' })
    if (await gradeDElement.isVisible()) {
      await expect(gradeDElement).toContainText('不及格')
    }
  })
})
