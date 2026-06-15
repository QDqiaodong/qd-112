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
  operator: string
  remarks: string
  createTime: string
}

export interface InventoryDTO {
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
}

export interface InventoryItemDTO {
  toolId: number
  actualStatus?: string
  checked?: boolean
  remarks?: string
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

export interface MaintenanceItem {
  id: number
  name: string
  code: string
  description: string
  defaultCycleDays: number
  createTime: string
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
  recentActivities?: { id: number; type: string; content: string; time: string }[]
  dueMaintenanceTools?: { id: number; name: string; nextMaintenanceDate: string }[]
}
