<template>
  <div class="flex h-screen">
    <aside class="w-60 bg-[#1f2937] text-white flex flex-col shrink-0">
      <div class="h-16 flex items-center justify-center border-b border-gray-700">
        <Wrench :size="24" class="text-accent mr-2" />
        <h1 class="text-lg font-bold">家用工具台账</h1>
      </div>
      <nav class="flex-1 py-4">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="flex items-center px-6 py-3 text-sm transition-colors duration-200"
          :class="isActive(item.path) ? 'bg-primary text-white' : 'text-gray-300 hover:bg-gray-700 hover:text-white'"
        >
          <component :is="item.icon" :size="20" class="mr-3" />
          <span>{{ item.label }}</span>
        </router-link>
      </nav>
    </aside>
    <main class="flex-1 overflow-auto bg-gray-100">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { Wrench, Hammer, Clock, Shield, ClipboardList, FolderTree, LayoutGrid, CalendarDays, BarChart3, GitBranch, Replace } from 'lucide-vue-next'

const route = useRoute()

const navItems = [
  { path: '/', label: '首页', icon: Wrench },
  { path: '/tools', label: '工具管理', icon: Hammer },
  { path: '/location-matrix', label: '位置矩阵', icon: LayoutGrid },
  { path: '/usage', label: '使用记录', icon: Clock },
  { path: '/scenario-analysis', label: '场景分析', icon: BarChart3 },
  { path: '/maintenance', label: '保养管理', icon: Shield },
  { path: '/maintenance-calendar', label: '保养日程月历', icon: CalendarDays },
  { path: '/part-replacement', label: '零件更换', icon: Replace },
  { path: '/inventory', label: '盘点管理', icon: ClipboardList },
  { path: '/categories', label: '分类管理', icon: FolderTree },
  { path: '/category-tree', label: '分类树维护', icon: GitBranch }
]

function isActive(path: string): boolean {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}
</script>
