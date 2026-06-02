import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: {
      title: '首页 - 鱼皮AI超级智能体应用平台',
      description: '鱼皮AI超级智能体应用平台提供AI恋爱大师和AI超级智能体服务，满足您的各种AI对话需求'
    }
  },
  {
    path: '/teaching-assistant',
    name: 'TeachingAssistant',
    component: () => import('../views/TeachingAssistant.vue'),
    meta: {
      title: 'AI知识教学助手 - 鱼皮AI超级智能体应用平台',
      description: 'AI知识教学助手专注于Java/Spring/AI开发领域的教学辅导，支持知识检索、练习题生成和学习路径推荐'
    }
  },
  {
    path: '/super-agent',
    name: 'SuperAgent',
    component: () => import('../views/SuperAgent.vue'),
    meta: {
      title: 'AI超级智能体 - 鱼皮AI超级智能体应用平台',
      description: 'AI超级智能体是鱼皮AI超级智能体应用平台的全能助手，能解答各类专业问题，提供精准建议和解决方案'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局导航守卫，设置文档标题
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title
  }
  next()
})

export default router 