const UNSPECIFIED_LOCATION = '未指定位置'
const STANDARD_SEPARATOR = '-'

const ROOM_ALIASES: Record<string, string> = {
  '工具间': '车间',
  '工作室': '车间',
  '工作间': '车间',
  '储物间': '仓库',
  '储藏室': '仓库',
  '储藏间': '仓库',
  '杂物间': '仓库',
  '杂物室': '仓库',
  '库房': '仓库',
  '地下': '地下室',
  '地下车库': '车库',
  '车房': '车库'
}

const CABINET_ALIASES: Record<string, string> = {
  '工具柜': '',
  '储物柜': '',
  '零件柜': '',
  '文件柜': '',
  '柜子': '',
  '一号柜': '1',
  '二号柜': '2',
  '三号柜': '3',
  '四号柜': '4',
  '五号柜': '5',
  '1号柜': '1',
  '2号柜': '2',
  '3号柜': '3',
  '4号柜': '4',
  '5号柜': '5',
  'A柜': 'A',
  'B柜': 'B',
  'C柜': 'C',
  'D柜': 'D'
}

export function normalizeLocation(location: string | null | undefined): string {
  if (!location || !location.trim()) {
    return ''
  }

  let normalized = location.trim()
  normalized = normalized.replace(/\s+/g, ' ')

  const parts = normalized.split(/[-_/]/)
  if (parts.length <= 1) {
    return normalized.trim()
  }

  const result: string[] = []
  parts.forEach((part, index) => {
    const trimmed = part.trim()
    if (!trimmed) return
    result.push(normalizePart(trimmed, index))
  })

  return result.join(STANDARD_SEPARATOR)
}

function normalizePart(part: string, index: number): string {
  if (!part) return part

  let normalized = part

  if (index === 0) {
    for (const [alias, standard] of Object.entries(ROOM_ALIASES)) {
      if (part === alias) {
        normalized = standard
        break
      }
    }
  } else if (index === 1) {
    for (const [alias, standard] of Object.entries(CABINET_ALIASES)) {
      if (part === alias) {
        normalized = standard
        break
      }
    }
    if (normalized && normalized.length === 1) {
      normalized = normalized.toUpperCase()
    }
  }

  return normalized
}

export function normalizeLocationForDisplay(location: string | null | undefined): string {
  const normalized = normalizeLocation(location)
  if (!normalized) {
    return UNSPECIFIED_LOCATION
  }
  return normalized
}

export function isUnspecifiedLocation(location: string | null | undefined): boolean {
  return !location || !location.trim()
}

export function getUnspecifiedLocationLabel(): string {
  return UNSPECIFIED_LOCATION
}

export interface ParsedLocation {
  room: string
  cabinet: string
  shelf: string
  cell: string
}

export function parseLocation(location: string): ParsedLocation | null {
  if (!location) return null
  const parts = location.split(/[-_/]/).map(p => p.trim()).filter(Boolean)
  if (parts.length < 3) return null
  return {
    room: parts[0] || '未分类',
    cabinet: parts[1] || 'A',
    shelf: parts[2] || '1',
    cell: parts[3] || '1'
  }
}
