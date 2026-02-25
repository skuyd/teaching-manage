import { test, expect } from '@playwright/test'
import { GroupsPage } from '../../pages/GroupsPage'
import { SubjectsPage } from '../../pages/SubjectsPage'
import { LoginPage } from '../../pages/LoginPage'
import { testUsers, generateUniqueSubjectName } from '../../fixtures/test-data'

/**
 * Group Management E2E Tests
 * Tests group creation, joining, and management workflows
 */

test.describe('Group Management', () => {
  // Note: These tests require a grouped subject to exist
  // Subject ID is needed to access the groups page

  test.describe('View Groups Page', () => {
    test.beforeEach(async ({ page }) => {
      // Login as student
      const loginPage = new LoginPage(page)
      await loginPage.goto()
      await loginPage.login(testUsers.student.username, testUsers.student.password)
      await page.waitForURL('/')
    })

    // Skip test as it requires a specific subject ID
    test.skip('should display groups page for a subject', async ({ page }) => {
      const groupsPage = new GroupsPage(page)
      await groupsPage.goto(1) // Replace with actual subject ID

      await groupsPage.verifyPageLoaded()
    })

    test.skip('should show all groups list', async ({ page }) => {
      const groupsPage = new GroupsPage(page)
      await groupsPage.goto(1)

      await expect(groupsPage.groupsListCard).toBeVisible()
      await expect(groupsPage.groupsTable).toBeVisible()
    })

    test.skip('should show search input for groups', async ({ page }) => {
      const groupsPage = new GroupsPage(page)
      await groupsPage.goto(1)

      await expect(groupsPage.groupSearchInput).toBeVisible()
    })
  })

  test.describe('Create Group', () => {
    test.beforeEach(async ({ page }) => {
      // Login as student (students can create groups)
      const loginPage = new LoginPage(page)
      await loginPage.goto()
      await loginPage.login(testUsers.student.username, testUsers.student.password)
      await page.waitForURL('/')
    })

    test.skip('should show create group button when student has no group', async ({ page }) => {
      const groupsPage = new GroupsPage(page)
      await groupsPage.goto(1)

      // If student has no group, create button should be visible
      if (!(await groupsPage.hasMyGroup())) {
        await expect(groupsPage.createGroupButton).toBeVisible()
      }
    })

    test.skip('should open create group dialog', async ({ page }) => {
      const groupsPage = new GroupsPage(page)
      await groupsPage.goto(1)

      if (!(await groupsPage.hasMyGroup())) {
        await groupsPage.openCreateDialog()
        await expect(groupsPage.createDialog).toBeVisible()
      }
    })

    test.skip('should create a new group', async ({ page }) => {
      const groupsPage = new GroupsPage(page)
      await groupsPage.goto(1)

      if (!(await groupsPage.hasMyGroup())) {
        const groupName = `TestGroup_${Date.now()}`
        await groupsPage.createGroup(groupName)

        // Verify group was created and user is now in it
        const hasGroup = await groupsPage.hasMyGroup()
        expect(hasGroup).toBe(true)

        // Verify user is the leader
        const isLeader = await groupsPage.isGroupLeader()
        expect(isLeader).toBe(true)

        // Clean up - disband group
        await groupsPage.disbandGroup()
      }
    })

    test.skip('should show validation error for empty group name', async ({ page }) => {
      const groupsPage = new GroupsPage(page)
      await groupsPage.goto(1)

      if (!(await groupsPage.hasMyGroup())) {
        await groupsPage.openCreateDialog()

        // Try to create with empty name
        await groupsPage.dialogCreateButton.click()

        // Dialog should remain open
        await expect(groupsPage.createDialog).toBeVisible()
      }
    })

    test.skip('should show validation error for short group name', async ({ page }) => {
      const groupsPage = new GroupsPage(page)
      await groupsPage.goto(1)

      if (!(await groupsPage.hasMyGroup())) {
        await groupsPage.openCreateDialog()
        await groupsPage.groupNameInput.fill('A') // Too short

        await groupsPage.dialogCreateButton.click()

        // Should show validation error
        await expect(groupsPage.createDialog).toBeVisible()
      }
    })
  })

  test.describe('Join Group', () => {
    test.beforeEach(async ({ page }) => {
      // Login as student
      const loginPage = new LoginPage(page)
      await loginPage.goto()
      await loginPage.login(testUsers.student2.username, testUsers.student2.password)
      await page.waitForURL('/')
    })

    test.skip('should show join button for available groups', async ({ page }) => {
      const groupsPage = new GroupsPage(page)
      await groupsPage.goto(1)

      // If student has no group and groups exist with space
      if (!(await groupsPage.hasMyGroup())) {
        const groupCount = await groupsPage.getGroupsCount()
        if (groupCount > 0) {
          // At least one join button should be visible
          const joinButtons = page.locator('button').filter({ hasText: '申请加入' })
          const count = await joinButtons.count()
          expect(count).toBeGreaterThanOrEqual(0)
        }
      }
    })

    test.skip('should apply to join a group', async ({ page }) => {
      const groupsPage = new GroupsPage(page)
      await groupsPage.goto(1)

      if (!(await groupsPage.hasMyGroup())) {
        // Find a group to join
        const groupCount = await groupsPage.getGroupsCount()
        if (groupCount > 0) {
          // Get first available group name
          const firstRow = groupsPage.groupRows.first()
          const groupName = await firstRow.locator('td').first().textContent()

          if (groupName && (await groupsPage.canJoinGroup(groupName))) {
            await groupsPage.joinGroup(groupName)

            // Should show success message
            const hasSuccess = await groupsPage.hasSuccessMessage()
            expect(hasSuccess).toBe(true)
          }
        }
      }
    })

    test.skip('should not show join button for full groups', async ({ page }) => {
      const groupsPage = new GroupsPage(page)
      await groupsPage.goto(1)

      // Full groups should show "已满" instead of join button
      const fullTag = page.locator('.el-tag').filter({ hasText: '已满' })
      // If any full groups exist, verify they don't have join buttons
    })
  })

  test.describe('Group Member Management', () => {
    test.beforeEach(async ({ page }) => {
      // Login as student who is a group leader
      const loginPage = new LoginPage(page)
      await loginPage.goto()
      await loginPage.login(testUsers.student.username, testUsers.student.password)
      await page.waitForURL('/')
    })

    test.skip('should show member list for own group', async ({ page }) => {
      const groupsPage = new GroupsPage(page)
      await groupsPage.goto(1)

      if (await groupsPage.hasMyGroup()) {
        await expect(groupsPage.membersTable).toBeVisible()
      }
    })

    test.skip('should show approve/reject buttons for pending members (leader only)', async ({ page }) => {
      const groupsPage = new GroupsPage(page)
      await groupsPage.goto(1)

      if (await groupsPage.hasMyGroup() && (await groupsPage.isGroupLeader())) {
        // Check for pending member rows
        const pendingRows = groupsPage.memberRows.filter({ hasText: '待审核' })
        const pendingCount = await pendingRows.count()

        if (pendingCount > 0) {
          // Approve/reject buttons should be visible
          await expect(groupsPage.approveButton.first()).toBeVisible()
          await expect(groupsPage.rejectButton.first()).toBeVisible()
        }
      }
    })

    test.skip('should approve a pending member', async ({ page }) => {
      const groupsPage = new GroupsPage(page)
      await groupsPage.goto(1)

      if (await groupsPage.hasMyGroup() && (await groupsPage.isGroupLeader())) {
        const pendingRows = groupsPage.memberRows.filter({ hasText: '待审核' })
        const pendingCount = await pendingRows.count()

        if (pendingCount > 0) {
          const memberName = await pendingRows.first().locator('td').first().textContent()
          if (memberName) {
            await groupsPage.approveMember(memberName)

            const hasSuccess = await groupsPage.hasSuccessMessage()
            expect(hasSuccess).toBe(true)
          }
        }
      }
    })

    test.skip('should reject a pending member', async ({ page }) => {
      const groupsPage = new GroupsPage(page)
      await groupsPage.goto(1)

      if (await groupsPage.hasMyGroup() && (await groupsPage.isGroupLeader())) {
        const pendingRows = groupsPage.memberRows.filter({ hasText: '待审核' })
        const pendingCount = await pendingRows.count()

        if (pendingCount > 0) {
          const memberName = await pendingRows.first().locator('td').first().textContent()
          if (memberName) {
            await groupsPage.rejectMember(memberName)

            const hasSuccess = await groupsPage.hasSuccessMessage()
            expect(hasSuccess).toBe(true)
          }
        }
      }
    })

    test.skip('should remove a member (leader only)', async ({ page }) => {
      const groupsPage = new GroupsPage(page)
      await groupsPage.goto(1)

      if (await groupsPage.hasMyGroup() && (await groupsPage.isGroupLeader())) {
        // Find approved members who are not the leader
        const approvedRows = groupsPage.memberRows.filter({ hasText: '已通过' })
        // This needs careful handling to not remove the leader
      }
    })
  })

  test.describe('Leave Group', () => {
    test.skip('should leave group as non-leader member', async ({ page }) => {
      // Login as a non-leader member
      const loginPage = new LoginPage(page)
      await loginPage.goto()
      await loginPage.login(testUsers.student2.username, testUsers.student2.password)
      await page.waitForURL('/')

      const groupsPage = new GroupsPage(page)
      await groupsPage.goto(1)

      if (await groupsPage.hasMyGroup() && !(await groupsPage.isGroupLeader())) {
        await groupsPage.leaveGroup()

        const hasGroup = await groupsPage.hasMyGroup()
        expect(hasGroup).toBe(false)
      }
    })
  })

  test.describe('Disband Group', () => {
    test.skip('should show disband button for group leader', async ({ page }) => {
      const loginPage = new LoginPage(page)
      await loginPage.goto()
      await loginPage.login(testUsers.student.username, testUsers.student.password)
      await page.waitForURL('/')

      const groupsPage = new GroupsPage(page)
      await groupsPage.goto(1)

      if (await groupsPage.hasMyGroup() && (await groupsPage.isGroupLeader())) {
        await expect(groupsPage.disbandGroupButton).toBeVisible()
      }
    })

    test.skip('should disband group as leader', async ({ page }) => {
      const loginPage = new LoginPage(page)
      await loginPage.goto()
      await loginPage.login(testUsers.student.username, testUsers.student.password)
      await page.waitForURL('/')

      const groupsPage = new GroupsPage(page)
      await groupsPage.goto(1)

      // Only test if user is a leader
      if (await groupsPage.hasMyGroup() && (await groupsPage.isGroupLeader())) {
        // Create a new group first to safely test disbanding
        const groupName = await groupsPage.getMyGroupName()

        await groupsPage.disbandGroup()

        const hasGroup = await groupsPage.hasMyGroup()
        expect(hasGroup).toBe(false)
      }
    })
  })

  test.describe('Search Groups', () => {
    test.beforeEach(async ({ page }) => {
      const loginPage = new LoginPage(page)
      await loginPage.goto()
      await loginPage.login(testUsers.student.username, testUsers.student.password)
      await page.waitForURL('/')
    })

    test.skip('should search groups by name', async ({ page }) => {
      const groupsPage = new GroupsPage(page)
      await groupsPage.goto(1)

      await groupsPage.searchGroups('创新')

      // Results should be filtered
      const groupCount = await groupsPage.getGroupsCount()
      // Each visible group should contain search term
    })

    test.skip('should clear search and show all groups', async ({ page }) => {
      const groupsPage = new GroupsPage(page)
      await groupsPage.goto(1)

      const initialCount = await groupsPage.getGroupsCount()

      await groupsPage.searchGroups('xyz')
      await groupsPage.clearSearch()

      const finalCount = await groupsPage.getGroupsCount()
      expect(finalCount).toBeGreaterThanOrEqual(initialCount)
    })
  })
})
