import { Page, Locator, expect } from '@playwright/test'

/**
 * Page Object for Groups Management Page
 * Handles group creation, joining, and management
 */
export class GroupsPage {
  readonly page: Page

  // Header elements
  readonly pageHeader: Locator
  readonly createGroupButton: Locator

  // My group card
  readonly myGroupCard: Locator
  readonly myGroupName: Locator
  readonly myGroupLeader: Locator
  readonly myGroupMemberCount: Locator
  readonly myGroupRoleTag: Locator
  readonly leaveGroupButton: Locator
  readonly disbandGroupButton: Locator

  // Members section in my group
  readonly membersTable: Locator
  readonly memberRows: Locator
  readonly approveButton: Locator
  readonly rejectButton: Locator
  readonly removeMemberButton: Locator

  // All groups list
  readonly groupsListCard: Locator
  readonly groupSearchInput: Locator
  readonly groupsTable: Locator
  readonly groupRows: Locator

  // Create group dialog
  readonly createDialog: Locator
  readonly groupNameInput: Locator
  readonly dialogCreateButton: Locator
  readonly dialogCancelButton: Locator

  // Confirmation dialogs
  readonly confirmDialog: Locator
  readonly confirmButton: Locator
  readonly cancelConfirmButton: Locator

  // Status messages
  readonly successMessage: Locator
  readonly errorMessage: Locator

  constructor(page: Page) {
    this.page = page

    // Header
    this.pageHeader = page.locator('.header h2')
    this.createGroupButton = page.locator('button').filter({ hasText: '创建小组' })

    // My group card
    this.myGroupCard = page.locator('.my-group-card')
    this.myGroupName = this.myGroupCard.locator('.info-row').filter({ hasText: '小组名称' }).locator('.value')
    this.myGroupLeader = this.myGroupCard.locator('.info-row').filter({ hasText: '组长' }).locator('.value')
    this.myGroupMemberCount = this.myGroupCard.locator('.info-row').filter({ hasText: '成员数量' }).locator('.value')
    this.myGroupRoleTag = this.myGroupCard.locator('.card-header .el-tag')
    this.leaveGroupButton = page.locator('button').filter({ hasText: '退出小组' })
    this.disbandGroupButton = page.locator('button').filter({ hasText: '解散小组' })

    // Members section
    this.membersTable = this.myGroupCard.locator('.el-table')
    this.memberRows = this.membersTable.locator('.el-table__row')
    this.approveButton = page.locator('button').filter({ hasText: '批准' })
    this.rejectButton = page.locator('button').filter({ hasText: '拒绝' })
    this.removeMemberButton = page.locator('button').filter({ hasText: '移除' })

    // All groups list
    this.groupsListCard = page.locator('.groups-list-card')
    this.groupSearchInput = this.groupsListCard.locator('input[placeholder="搜索小组名称"]')
    this.groupsTable = this.groupsListCard.locator('.el-table')
    this.groupRows = this.groupsTable.locator('.el-table__body-wrapper .el-table__row')

    // Create dialog
    this.createDialog = page.locator('.el-dialog').filter({ hasText: '创建小组' })
    this.groupNameInput = this.createDialog.locator('input[placeholder="请输入小组名称"]')
    this.dialogCreateButton = this.createDialog.locator('button').filter({ hasText: '创建' })
    this.dialogCancelButton = this.createDialog.locator('button').filter({ hasText: '取消' })

    // Confirmation
    this.confirmDialog = page.locator('.el-message-box')
    this.confirmButton = this.confirmDialog.locator('button').filter({ hasText: /确定|确定解散/ })
    this.cancelConfirmButton = this.confirmDialog.locator('button').filter({ hasText: '取消' })

    // Status
    this.successMessage = page.locator('.el-message--success')
    this.errorMessage = page.locator('.el-message--error')
  }

  /**
   * Navigate to groups page for a specific subject
   */
  async goto(subjectId: number) {
    await this.page.goto(`/subjects/${subjectId}/groups`)
    await this.page.waitForLoadState('networkidle')
  }

  /**
   * Wait for page to load
   */
  async waitForLoad() {
    await this.page.waitForTimeout(500)
    const loadingMask = this.page.locator('.el-loading-mask')
    if (await loadingMask.isVisible()) {
      await loadingMask.waitFor({ state: 'hidden', timeout: 10000 })
    }
  }

  /**
   * Open create group dialog
   */
  async openCreateDialog() {
    await this.createGroupButton.click()
    await this.createDialog.waitFor({ state: 'visible' })
  }

  /**
   * Create a new group
   */
  async createGroup(name: string) {
    await this.openCreateDialog()
    await this.groupNameInput.fill(name)
    await this.dialogCreateButton.click()

    // Wait for dialog to close
    await this.createDialog.waitFor({ state: 'hidden' })
    await this.waitForLoad()
  }

  /**
   * Search for groups
   */
  async searchGroups(keyword: string) {
    await this.groupSearchInput.fill(keyword)
    await this.page.waitForTimeout(500)
  }

  /**
   * Clear search
   */
  async clearSearch() {
    await this.groupSearchInput.clear()
    await this.page.waitForTimeout(500)
  }

  /**
   * Get group row by name
   */
  getGroupRow(name: string): Locator {
    return this.groupRows.filter({ hasText: name })
  }

  /**
   * Join a group
   */
  async joinGroup(groupName: string) {
    const row = this.getGroupRow(groupName)
    await row.locator('button').filter({ hasText: '申请加入' }).click()

    // Confirm
    await this.confirmButton.click()
    await this.waitForLoad()
  }

  /**
   * Leave current group
   */
  async leaveGroup() {
    await this.leaveGroupButton.click()
    await this.confirmButton.click()
    await this.waitForLoad()
  }

  /**
   * Disband current group (leader only)
   */
  async disbandGroup() {
    await this.disbandGroupButton.click()
    await this.confirmButton.click()
    await this.waitForLoad()
  }

  /**
   * Approve a pending member
   */
  async approveMember(memberName: string) {
    const row = this.memberRows.filter({ hasText: memberName })
    await row.locator('button').filter({ hasText: '批准' }).click()
    await this.waitForLoad()
  }

  /**
   * Reject a pending member
   */
  async rejectMember(memberName: string) {
    const row = this.memberRows.filter({ hasText: memberName })
    await row.locator('button').filter({ hasText: '拒绝' }).click()
    await this.confirmButton.click()
    await this.waitForLoad()
  }

  /**
   * Remove a member (leader only)
   */
  async removeMember(memberName: string) {
    const row = this.memberRows.filter({ hasText: memberName })
    await row.locator('button').filter({ hasText: '移除' }).click()
    await this.confirmButton.click()
    await this.waitForLoad()
  }

  /**
   * Check if user has a group
   */
  async hasMyGroup(): Promise<boolean> {
    return await this.myGroupCard.isVisible()
  }

  /**
   * Check if user is group leader
   */
  async isGroupLeader(): Promise<boolean> {
    if (!(await this.hasMyGroup())) return false
    const roleText = await this.myGroupRoleTag.textContent()
    return roleText?.includes('组长') || false
  }

  /**
   * Get my group name
   */
  async getMyGroupName(): Promise<string> {
    return await this.myGroupName.textContent() || ''
  }

  /**
   * Get total groups count
   */
  async getGroupsCount(): Promise<number> {
    return await this.groupRows.count()
  }

  /**
   * Check if a group exists
   */
  async groupExists(name: string): Promise<boolean> {
    const row = this.getGroupRow(name)
    return await row.isVisible()
  }

  /**
   * Check if can join a specific group
   */
  async canJoinGroup(groupName: string): Promise<boolean> {
    const row = this.getGroupRow(groupName)
    const joinButton = row.locator('button').filter({ hasText: '申请加入' })
    return await joinButton.isVisible()
  }

  /**
   * Get member count for a group
   */
  async getGroupMemberCount(groupName: string): Promise<string> {
    const row = this.getGroupRow(groupName)
    // Member count is in the 3rd column
    const memberCell = row.locator('td').nth(2)
    return await memberCell.textContent() || ''
  }

  /**
   * Verify page loaded for a subject
   */
  async verifyPageLoaded() {
    await expect(this.pageHeader).toContainText('小组管理')
    await expect(this.groupsListCard).toBeVisible()
  }

  /**
   * Close dialog if open
   */
  async closeDialog() {
    if (await this.createDialog.isVisible()) {
      await this.dialogCancelButton.click()
      await this.createDialog.waitFor({ state: 'hidden' })
    }
  }

  /**
   * Check for success message
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
   * Check for error message
   */
  async hasErrorMessage(): Promise<boolean> {
    try {
      await this.errorMessage.waitFor({ state: 'visible', timeout: 5000 })
      return true
    } catch {
      return false
    }
  }
}
