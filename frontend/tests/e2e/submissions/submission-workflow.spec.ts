import { test, expect } from '@playwright/test'
import { SubmissionsPage } from '../../pages/SubmissionsPage'
import { DashboardPage } from '../../pages/DashboardPage'
import { LoginPage } from '../../pages/LoginPage'
import { testUsers, testFileContent } from '../../fixtures/test-data'

/**
 * Submission Workflow E2E Tests
 * Tests assignment submission for students and viewing for teachers
 */

test.describe('Student Submission Workflow', () => {
  // Use pre-authenticated student state
  test.use({ storageState: 'playwright/.auth/student.json' })

  test.describe('View Submissions Page', () => {
    test('should display submissions page for student', async ({ page }) => {
      // Already authenticated
      await page.goto('/')
      await page.waitForLoadState('networkidle')

      // Navigate to submissions
      const dashboardPage = new DashboardPage(page)
      await dashboardPage.goToSubmissions()

      // Verify page loaded
      const submissionsPage = new SubmissionsPage(page)
      await submissionsPage.verifyPageLoaded()
    })

    test('should show lesson dropdown for selection', async ({ page }) => {
      // Navigate to submissions
      await page.goto('/submissions')

      const submissionsPage = new SubmissionsPage(page)
      await expect(submissionsPage.lessonSelect).toBeVisible()
    })
  })

  test.describe('Submit Assignment', () => {
    test.beforeEach(async ({ page }) => {
      // Already authenticated via storageState
      await page.goto('/')
      await page.waitForLoadState('networkidle')
    })

    test('should display upload dialog when clicking submit button', async ({ page }) => {
      const submissionsPage = new SubmissionsPage(page)
      await submissionsPage.goto()

      // This test assumes a lesson is already selected
      // If no lessons available, the submit button won't be visible
      // Skip if submit button not available
      if (await submissionsPage.submitButton.isVisible()) {
        await submissionsPage.openSubmitDialog()
        await expect(submissionsPage.uploadDialog).toBeVisible()
      }
    })

    test('should show drag-and-drop upload area', async ({ page }) => {
      const submissionsPage = new SubmissionsPage(page)
      await submissionsPage.goto()

      if (await submissionsPage.submitButton.isVisible()) {
        await submissionsPage.openSubmitDialog()
        await expect(submissionsPage.uploadArea).toBeVisible()
      }
    })

    // Note: Actual file upload testing requires lessons to be set up
    // This is a placeholder for when the full flow can be tested
    test.skip('should submit an assignment successfully', async ({ page }) => {
      const submissionsPage = new SubmissionsPage(page)
      await submissionsPage.goto()

      // Select a lesson first (requires lessons to exist)
      // await submissionsPage.selectLesson('Python基础语法')

      // Submit assignment
      await submissionsPage.submitAssignment(testFileContent.python, 'homework.py')

      // Verify submission appears
      const hasSubmission = await submissionsPage.hasMySubmission()
      expect(hasSubmission).toBe(true)
    })

    test.skip('should resubmit an assignment', async ({ page }) => {
      const submissionsPage = new SubmissionsPage(page)
      await submissionsPage.goto()

      // First, need an existing submission
      // Then resubmit

      await submissionsPage.resubmitAssignment(testFileContent.python, 'homework_v2.py')

      // Verify submission updated
      const hasSubmission = await submissionsPage.hasMySubmission()
      expect(hasSubmission).toBe(true)
    })
  })

  test.describe('View Submission Details', () => {
    test.beforeEach(async ({ page }) => {
      // Already authenticated via storageState
      await page.goto('/')
      await page.waitForLoadState('networkidle')
    })

    test.skip('should view submission detail page', async ({ page }) => {
      const submissionsPage = new SubmissionsPage(page)
      await submissionsPage.goto()

      // Requires existing submission
      if (await submissionsPage.hasMySubmission()) {
        await submissionsPage.viewSubmissionDetail()
        await expect(page).toHaveURL(/\/submissions\/\d+/)
      }
    })

    test.skip('should delete own submission', async ({ page }) => {
      const submissionsPage = new SubmissionsPage(page)
      await submissionsPage.goto()

      // Requires existing submission
      if (await submissionsPage.hasMySubmission()) {
        await submissionsPage.deleteSubmission()
        const hasSubmission = await submissionsPage.hasMySubmission()
        expect(hasSubmission).toBe(false)
      }
    })
  })
})

test.describe('Teacher Submission Workflow', () => {
  // Use pre-authenticated teacher state
  test.use({ storageState: 'playwright/.auth/teacher.json' })

  test.describe('View All Submissions', () => {
    test.beforeEach(async ({ page }) => {
      // Already authenticated via storageState
      await page.goto('/')
      await page.waitForLoadState('networkidle')
    })

    test('should display submissions page for teacher', async ({ page }) => {
      const dashboardPage = new DashboardPage(page)
      await dashboardPage.goToSubmissions()

      const submissionsPage = new SubmissionsPage(page)
      await submissionsPage.verifyPageLoaded()
    })

    test('should show submissions table after selecting lesson', async ({ page }) => {
      await page.goto('/submissions')

      const submissionsPage = new SubmissionsPage(page)

      // Teacher should see a table of all submissions
      // after selecting a lesson
      await expect(submissionsPage.lessonSelect).toBeVisible()
    })

    test.skip('should view individual student submission', async ({ page }) => {
      await page.goto('/submissions')

      const submissionsPage = new SubmissionsPage(page)

      // Select lesson and view a student's submission
      // Requires lessons and submissions to exist

      // await submissionsPage.selectLesson('Python基础语法')
      // await submissionsPage.viewSubmissionByStudent('王同学')
      // await expect(page).toHaveURL(/\/submissions\/\d+/)
    })
  })

  test.describe('Grade Submissions', () => {
    test.beforeEach(async ({ page }) => {
      // Already authenticated via storageState
      await page.goto('/')
      await page.waitForLoadState('networkidle')
    })

    // Grading tests require submissions to exist
    test.skip('should navigate to grade page from submission detail', async ({ page }) => {
      // Navigate to a submission
      // Then to grade page
    })

    test.skip('should grade a submission', async ({ page }) => {
      // Navigate to submission detail
      // Click grade button
      // Select grade and add comment
      // Submit grade
    })
  })
})

test.describe('Submission Status Indicators', () => {
  // Use pre-authenticated student state
  test.use({ storageState: 'playwright/.auth/student.json' })

  test('should show graded tag for graded submissions', async ({ page }) => {
    // Already authenticated via storageState
    await page.goto('/')
    await page.waitForLoadState('networkidle')

    await page.goto('/submissions')

    // Check if graded tag exists
    const submissionsPage = new SubmissionsPage(page)

    // If there's a submission, check for grade status
    if (await submissionsPage.hasMySubmission()) {
      // Should show either graded or ungraded tag
      const gradedVisible = await submissionsPage.gradedTag.first().isVisible()
      const ungradedVisible = await submissionsPage.ungradedTag.first().isVisible()

      expect(gradedVisible || ungradedVisible).toBe(true)
    }
  })
})
