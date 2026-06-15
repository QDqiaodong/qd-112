import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: MainLayout,
      children: [
        {
          path: '',
          name: 'Dashboard',
          component: () => import('@/pages/Dashboard/index.vue')
        },
        {
          path: 'tools',
          name: 'ToolList',
          component: () => import('@/pages/Tools/ToolList.vue')
        },
        {
          path: 'tools/add',
          name: 'ToolAdd',
          component: () => import('@/pages/Tools/ToolForm.vue')
        },
        {
          path: 'tools/:id',
          name: 'ToolDetail',
          component: () => import('@/pages/Tools/ToolDetail.vue')
        },
        {
          path: 'tools/:id/edit',
          name: 'ToolEdit',
          component: () => import('@/pages/Tools/ToolForm.vue')
        },
        {
          path: 'usage',
          name: 'UsageList',
          component: () => import('@/pages/Usage/index.vue')
        },
        {
          path: 'maintenance',
          name: 'MaintenanceList',
          component: () => import('@/pages/Maintenance/index.vue')
        },
        {
          path: 'inventory',
          name: 'InventoryList',
          component: () => import('@/pages/Inventory/index.vue')
        },
        {
          path: 'categories',
          name: 'CategoryManager',
          component: () => import('@/pages/Categories/index.vue')
        },
        {
          path: 'location-matrix',
          name: 'LocationMatrix',
          component: () => import('@/pages/LocationMatrix/index.vue')
        }
      ]
    }
  ]
})

export default router
