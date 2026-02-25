import { test, expect } from '@playwright/test'
import { LoginPage } from '../../pages/LoginPage'
import { DashboardPage } from '../../pages/DashboardPage'
import { SubjectsPage } from '../../pages/SubjectsPage'
import { SubmissionsPage } from '../../pages/SubmissionsPage'
import { GradeSummaryPage } from '../../pages/GradesPage'
import { testUsers, generateUniqueSubjectName, testFileContent } from '../../fixtures/test-data'

/**
 * Complete Teaching Workflow E2E Tests
 * Tests end-to-end user journeys across the entire system
 */

test.describe('Complete Teaching Workflow', () => {
  /**
   * Teacher Workflow: Create Subject -> Add Lesson -> View Submissions -> Grade
   */
  test.describe('Teacher Complete Workflow', () => {
    let subjectName: string

    test.beforeEach(async ({ page }) => {
      // Login as teacher
      const loginPage = new LoginPage(page)
      await loginPage.goto()
      await loginPage.login(testUsers.teacher.username, testUsers.teacher.password)
      await page.waitForURL('/')
    })

    test('should complete teacher workflow: login -> dashboard -> subjects', async ({ page }) => {
      // Step 1: Verify dashboard loaded with teacher view
      const dashboardPage = new DashboardPage(page)
      await dashboardPage.verifyDashboardLoaded()
      await dashboardPage.verifyTeacherDashboard()

      // Step 2: Navigate to subjects
      await dashboardPage.goToSubjects()
      const subjectsPage = new SubjectsPage(page)
      await subjectsPage.verifyPageLoaded()

      // Step 3: Create a new subject
      subjectName = generateUniqueSubjectName('TeacherWorkflow')
      await subjectsPage.createSubject({
        name: subjectName,
        description: 'Complete workflow test subject',
        isGrouped: false
      })

      // Verify subject was created
      const exists = await subjectsPage.subjectExists(subjectName)
      expect(exists).toBe(true)

      // Step 4: View subject detail
      await subjectsPage.viewSubjectDetail(subjectName)
      await expect(page).toHaveURL(/\/subjects\/\d+/)

      // Step 5: Navigate back and clean up
      await page.goto('/subjects')
      await subjectsPage.waitForTableLoad()
      await subjectsPage.deleteSubject(subjectName)
    })

    test('should access all teacher menu items', async ({ page }) => {
      const dashboardPage = new DashboardPage(page)

      // Test subjects navigation
      await dashboardPage.goToSubjects()
      await expect(page).toHaveURL('/subjects')

      // Test submissions navigation
      await page.goto('/')
      await dashboardPage.goToSubmissions()
      await expect(page).toHaveURL('/submissions')

      // Test grades navigation
      await page.goto('/')
      await dashboardPage.goToGrades()
      await expect(page).toHaveURL('/grades')

      // Test grade summary navigation
      await page.goto('/')
      await dashboardPage.goToGradeSummary()
      await expect(page).toHaveURL('/grade-summary')
    })

    test('should see teacher-specific dashboard stats', async ({ page }) => {
      const dashboardPage = new DashboardPage(page)
      await dashboardPage.verifyDashboardLoaded()

      // Get stats
      const stats = await dashboardPage.getStatValues()

      // Teacher should see: my subjects, pending grades, completed grades
      const statLabels = stats.map(s => s.label)
      expect(statLabels).toContain('我的学科数')
      expect(statLabels).toContain('待评分作业')
      expect(statLabels).toContain('已评分作业')
    })
  })

  /**
   * Student Workflow: Login -> View Subjects -> Submit Assignment -> View Grades
   */
  test.describe('Student Complete Workflow', () => {
    test.beforeEach(async ({ page }) => {
      // Login as student
      const loginPage = new LoginPage(page)
      await loginPage.goto()
      await loginPage.login(testUsers.student.username, testUsers.student.password)
      await page.waitForURL('/')
    })

    test('should complete student workflow: login -> dashboard -> submissions', async ({ page }) => {
      // Step 1: Verify dashboard loaded with student view
      const dashboardPage = new DashboardPage(page)
      await dashboardPage.verifyDashboardLoaded()
      await dashboardPage.verifyStudentDashboard()

      // Step 2: Navigate to submissions
      await dashboardPage.goToSubmissions()
      const submissionsPage = new SubmissionsPage(page)
      await submissionsPage.verifyPageLoaded()

      // Step 3: Navigate to grade summary
      await page.goto('/')
      await dashboardPage.goToGradeSummary()
      const gradeSummaryPage = new GradeSummaryPage(page)
      await gradeSummaryPage.verifyPageLoaded()
    })

    test('should access all student menu items', async ({ page }) => {
      const dashboardPage = new DashboardPage(page)

      // Test submissions navigation
      await dashboardPage.goToSubmissions()
      await expect(page).toHaveURL('/submissions')

      // Test grade summary navigation
      await page.goto('/')
      await dashboardPage.goToGradeSummary()
      await expect(page).toHaveURL('/grade-summary')

      // Verify restricted menus are not visible
      expect(await dashboardPage.isSubjectsMenuVisible()).toBe(false)
      expect(await dashboardPage.isGradesMenuVisible()).toBe(false)
      expect(await dashboardPage.isUsersMenuVisible()).toBe(false)
    })

    test('should see student-specific dashboard stats', async ({ page }) => {
      const dashboardPage = new DashboardPage(page)
      await dashboardPage.verifyDashboardLoaded()

      // Get stats
      const stats = await dashboardPage.getStatValues()

      // Student should see: my subjects, submitted assignments, graded assignments, average grade
      const statLabels = stats.map(s => s.label)
      expect(statLabels).toContain('我的学科数')
      expect(statLabels).toContain('已提交作业')
      expect(statLabels).toContain('已评分作业')
      expect(statLabels).toContain('平均成绩')
    })
  })

  /**
   * Admin Workflow: Login -> Dashboard -> User Management
   */
  test.describe('Admin Complete Workflow', () => {
    test.beforeEach(async ({ page }) => {
      // Login as admin
      const loginPage = new LoginPage(page)
      await loginPage.goto()
      await loginPage.login(testUsers.admin.username, testUsers.admin.password)
      await page.waitForURL('/')
    })

    test('should complete admin workflow: login -> dashboard', async ({ page }) => {
      // Step 1: Verify dashboard loaded with admin view
      const dashboardPage = new DashboardPage(page)
      await dashboardPage.verifyDashboardLoaded()
      await dashboardPage.verifyAdminDashboard()
    })

    test('should access all admin menu items', async ({ page }) => {
      const dashboardPage = new DashboardPage(page)

      // Test subjects navigation
      await dashboardPage.goToSubjects()
      await expect(page).toHaveURL('/subjects')

      // Test submissions navigation
      await page.goto('/')
      await dashboardPage.goToSubmissions()
      await expect(page).toHaveURL('/submissions')

      // Test grade summary navigation
      await page.goto('/')
      await dashboardPage.goToGradeSummary()
      await expect(page).toHaveURL('/grade-summary')

      // Admin should have access to user management
      expect(await dashboardPage.isUsersMenuVisible()).toBe(true)
    })

    test('should see admin-specific dashboard stats', async ({ page }) => {
      const dashboardPage = new DashboardPage(page)
      await dashboardPage.verifyDashboardLoaded()

      // Get stats
      const stats = await dashboardPage.getStatValues()

      // Admin should see: total users, total subjects, total lessons, total submissions
      const statLabels = stats.map(s => s.label)
      expect(statLabels).toContain('总用户数')
      expect(statLabels).toContain('总学科数')
      expect(statLabels).toContain('总课程数')
      expect(statLabels).toContain('总提交数')
    })
  })

  /**
   * Cross-role interaction tests
   */
  test.describe('Cross-role Interactions', () => {
    test('teacher creates subject, student cannot see subjects menu', async ({ page }) => {
      // First, login as teacher and create subject
      let loginPage = new LoginPage(page)
      await loginPage.goto()
      await loginPage.login(testUsers.teacher.username, testUsers.teacher.password)
      await page.waitForURL('/')

      const subjectsPage = new SubjectsPage(page)
      await subjectsPage.goto()

      const subjectName = generateUniqueSubjectName('CrossRole')
      await subjectsPage.createSubject({
        name: subjectName,
        description: 'Cross-role test subject'
      })

      // Verify created
      expect(await subjectsPage.subjectExists(subjectName)).toBe(true)

      // Logout
      const dashboardPage = new DashboardPage(page)
      await page.goto('/')
      await dashboardPage.logout()

      // Login as student
      loginPage = new LoginPage(page)
      await loginPage.login(testUsers.student.username, testUsers.student.password)
      await page.waitForURL('/')

      // Verify student cannot see subjects menu
      const studentDashboard = new DashboardPage(page)
      expect(await studentDashboard.isSubjectsMenuVisible()).toBe(false)

      // Logout and clean up as teacher
      await studentDashboard.logout()
      await loginPage.login(testUsers.teacher.username, testUsers.teacher.password)
      await page.waitForURL('/')

      await subjectsPage.goto()
      await subjectsPage.deleteSubject(subjectName)
    })
  })

  /**
   * Session persistence tests
   */
  test.describe('Session Persistence', () => {
    test('should maintain session after page refresh', async ({ page }) => {
      // Login
      const loginPage = new LoginPage(page)
      await loginPage.goto()
      await loginPage.login(testUsers.teacher.username, testUsers.teacher.password)
      await page.waitForURL('/')

      // Verify logged in
      const dashboardPage = new DashboardPage(page)
      await dashboardPage.verifyDashboardLoaded()

      // Refresh page
      await page.reload()
      await page.waitForLoadState('networkidle')

      // Should still be on dashboard, not redirected to login
      await expect(page).toHaveURL('/')
      await dashboardPage.verifyDashboardLoaded()
    })

    test('should clear session after logout', async ({ page }) => {
      // Login
      const loginPage = new LoginPage(page)
      await loginPage.goto()
      await loginPage.login(testUsers.teacher.username, testUsers.teacher.password)
      await page.waitForURL('/')

      // Logout
      const dashboardPage = new DashboardPage(page)
      await dashboardPage.logout()

      // Verify on login page
      await expect(page).toHaveURL('/login')

      // Try to access protected route
      await page.goto('/subjects')

      // Should be redirected to login
      await expect(page).toHaveURL('/login')
    })
  })

  /**
   * Error handling tests
   */
  test.describe('Error Handling', () => {
    test('should handle 404 page gracefully', async ({ page }) => {
      // Login first
      const loginPage = new LoginPage(page)
      await loginPage.goto()
      await loginPage.login(testUsers.teacher.username, testUsers.teacher.password)
      await page.waitForURL('/')

      // Navigate to non-existent page
      await page.goto('/non-existent-page')

      // Should show 404 page or be redirected
      // Depending on implementation, check for appropriate handling
    })

    test('should handle invalid login gracefully', async ({ page }) => {
      const loginPage = new LoginPage(page)
      await loginPage.goto()

      // Try invalid login
      await loginPage.login('invalid_user_xyz', 'wrong_password_123')

      // Should show error and remain on login page
      const hasError = await loginPage.hasError()
      expect(hasError).toBe(true)
      await expect(page).toHaveURL('/login')
    })
  })
})

/**
 * Responsive Design Tests
 */
test.describe('Responsive Design', () => {
  test('should display mobile menu toggle on small screens', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 })

    // Login
    const loginPage = new LoginPage(page)
    await loginPage.goto()
    await loginPage.login(testUsers.teacher.username, testUsers.teacher.password)
    await page.waitForURL('/')

    // Check for mobile menu toggle
    const dashboardPage = new DashboardPage(page)
    await expect(dashboardPage.menuToggle).toBeVisible()
  })

  test('should open and close mobile menu', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 })

    // Login
    const loginPage = new LoginPage(page)
    await loginPage.goto()
    await loginPage.login(testUsers.teacher.username, testUsers.teacher.password)
    await page.waitForURL('/')

    const dashboardPage = new DashboardPage(page)

    // Open mobile menu
    await dashboardPage.openMobileMenu()

    // Sidebar should be visible
    await expect(dashboardPage.sidebar).toBeVisible()

    // Close mobile menu
    await dashboardPage.closeMobileMenu()
  })
})
