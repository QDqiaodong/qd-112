export type ToolStatus = 'AVAILABLE' | 'IN_USE' | 'MAINTENANCE' | 'LOANED' | 'LOST'
export type MaintenanceType = 'CLEAN' | 'OIL' | 'TIGHTEN' | 'INSPECT' | 'REPAIR' | 'OTHER'
export type TrackType = 'USAGE' | 'MAINTENANCE' | 'INVENTORY' | 'STATUS_CHANGE'

export interface Tool {
  id: number
  name: string
  model: string
  brand: string
  categoryId: number
  subCategoryId: number
  purpose: string
  specification: string
  location: string
  purchaseDate: string
  price: number
  maintenanceCycleDays: number
  status: ToolStatus
  lastMaintenanceDate: string
  nextMaintenanceDate: string
  createTime: string
  updateTime: string
}

export interface ToolDTO {
  name: string
  model?: string
  brand?: string
  categoryId: number
  subCategoryId?: number
  purpose?: string
  specification?: string
  location: string
  purchaseDate?: string
  price?: number
  maintenanceCycleDays?: number
}

export interface UsageRecord {
  id: number
  toolId: number
  useDate: string
  durationMinutes: number
  scenario: string
  operator: string
  remarks: string
  createTime: string
}

export interface UsageRecordDTO {
  toolId: number
  useDate: string
  durationMinutes?: number
  scenario?: string
  operator?: string
  remarks?: string
}

export interface MaintenanceRecord {
  id: number
  toolId: number
  maintenanceType: MaintenanceType
  maintenanceDate: string
  operator: string
  cost: number
  laborCost: number
  partsCost: number
  otherCost: number
  partsReplaced: string
  description: string
  nextMaintenanceDate: string
  createTime: string
}

export interface MaintenanceRecordDTO {
  toolId: number
  maintenanceType: MaintenanceType
  maintenanceDate: string
  operator?: string
  cost?: number
  laborCost?: number
  partsCost?: number
  otherCost?: number
  partsReplaced?: string
  description?: string
  nextMaintenanceDate?: string
}

export interface Inventory {
  id: number
  inventoryDate: string
  totalTools: number
  checkedTools: number
  availableTools: number
  loanedTools: number
  maintenanceTools: number
  lostTools: number
  mismatchedTools: number
  uncheckedTools: number
  completed: boolean
  completeTime: string
  operator: string
  remarks: string
  createTime: string
}

export interface InventoryDTO {
  inventoryDate?: string
  operator?: string
  remarks?: string
}

export interface InventoryItem {
  id: number
  inventoryId: number
  toolId: number
  expectedStatus: string
  actualStatus: string
  checked: boolean
  remarks: string
  toolName: string
  toolModel: string
  toolBrand: string
  categoryId: number
  categoryName: string
  location: string
  snapshotStatus: string
}

export interface InventoryItemDTO {
  toolId: number
  actualStatus?: string
  checked?: boolean
  remarks?: string
}

export interface DifferenceItem {
  toolId: number
  toolName: string
  toolModel: string
  toolBrand: string
  expectedStatus: string
  actualStatus: string
  checked: boolean
  categoryName: string
  location: string
  differenceType: string
  remarks: string
}

export interface DifferenceGroup {
  groupKey: string
  groupType: string
  items: DifferenceItem[]
  totalCount: number
  checkedCount: number
  uncheckedCount: number
  mismatchedCount: number
  lostCount: number
  completionPercent: number
}

export interface InventoryProgress {
  totalTools: number
  checkedCount: number
  uncheckedCount: number
  mismatchedCount: number
  lostCount: number
  checkedPercent: number
  uncheckedPercent: number
  mismatchedPercent: number
  lostPercent: number
}

export interface InventoryCompletionResult {
  success: boolean
  message: string
  totalUpdated: number
  statusMismatchCount: number
  lostCount: number
  checkedCount: number
  uncheckedCount: number
}

export interface Category {
  id: number
  name: string
  code: string
  parentId: number
  level: number
  sortOrder: number
  description: string
  children: Category[]
}

export interface CategoryTreeNode {
  id: number
  name: string
  code: string
  parentId: number
  level: number
  sortOrder: number
  description: string
  toolCount: number
  maintenanceItemCount: number
  maintenanceItemTotal: number
  maintenanceCoverageRate: number
  defaultCycleDays: number
  children: CategoryTreeNode[]
}

export interface StatusTransitionResult {
  valid: boolean
  message: string
  reason: string
}

export interface CategoryDeletionCheck {
  canDelete: boolean
  toolCount: number
  subCategoryCount: number
  maintenanceRecordCount: number
  message: string
}

export interface MaintenanceItem {
  id: number
  name: string
  code: string
  description: string
  defaultCycleDays: number
  createTime: string
}

export interface MaintenanceItemWithSource {
  item: MaintenanceItem
  sourceType: string
  sourceCategoryId: string
  sourceCategoryName: string
  effectiveCycleDays: number
  cycleSource: string
  enabled: boolean
  overridden: boolean
  overriddenCycleDays: number
  remarks: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface MaintenanceTrack {
  type: TrackType
  recordId: number
  actionDate: string
  actionTime: string
  actionName: string
  description: string
  durationMinutes: number
  cost: number
  operator: string
  oldStatus?: ToolStatus
  newStatus?: ToolStatus
  maintenanceType?: MaintenanceType
  inventoryChecked?: boolean
  inventoryActualStatus?: string
}

export interface StatsOverview {
  statusCounts: Record<ToolStatus, number>
  dueMaintenanceCount: number
  categoryStats?: { name: string; value: number }[]
  locationStats?: { name: string; value: number }[]
  recentActivities?: { id: number; type: string; content: string; time: string }[]
  dueMaintenanceTools?: { id: number; name: string; nextMaintenanceDate: string }[]
}

export interface ScenarioTool {
  toolId: number
  toolName: string
  totalMinutes: number
  usageCount: number
  lastUseDate: string
}

export interface ScenarioAnalysis {
  scenario: string
  totalMinutes: number
  usageCount: number
  lastUseDate: string
  topTools: ScenarioTool[]
}

export interface ToolAvailabilityScore {
  toolId: number
  totalScore: number
  grade: 'A' | 'B' | 'C' | 'D' | 'E' | '-'

  statusScore: number
  statusDetail: string

  maintenanceScore: number
  maintenanceDetail: string
  nextMaintenanceDate: string
  overdueDays: number

  usageScore: number
  usageDetail: string
  usageCount30Days: number
  usageCount90Days: number
  lastUseDate: string

  inventoryScore: number
  inventoryDetail: string
  lastInventoryDate: string
  lastInventoryChecked: boolean
  lastInventoryActualStatus: string
  lastInventoryExpectedStatus: string
}

export type PartType = 'BLADE' | 'BATTERY' | 'DRILL_BIT' | 'MOTOR' | 'BEARING' | 'SEAL' | 'CABLE' | 'FILTER' | 'OTHER'

export interface PartReplacement {
  id: number
  toolId: number
  partName: string
  partType: string
  replacementDate: string
  cost: number
  operator: string
  supplier: string
  remarks: string
  createTime: string
}

export interface PartReplacementDTO {
  toolId: number
  partName: string
  partType?: string
  replacementDate: string
  cost?: number
  operator?: string
  supplier?: string
  remarks?: string
}

export interface ToolWithScore {
  tool: Tool
  availabilityScore: ToolAvailabilityScore
}
