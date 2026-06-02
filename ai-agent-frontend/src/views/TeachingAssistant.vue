<template>
  <div class="teaching-assistant-container">
    <div class="header">
      <div class="back-button" @click="goBack">返回</div>
      <h1 class="title">AI 知识教学助手</h1>
      <div class="chat-id">会话ID: {{ chatId }}</div>
    </div>

    <div class="content-wrapper">
      <!-- 左侧快捷功能面板 -->
      <div class="sidebar">
        <div class="sidebar-section">
          <div class="section-title">快捷功能</div>
          <button class="feature-btn" @click="openModal('explain')">
            <span class="btn-icon">💡</span>解释概念
          </button>
          <button class="feature-btn" @click="openModal('exercise')">
            <span class="btn-icon">✏️</span>生成练习题
          </button>
          <button class="feature-btn" @click="openModal('path')">
            <span class="btn-icon">🗺️</span>学习路径
          </button>
          <button class="feature-btn" @click="openModal('summary')">
            <span class="btn-icon">📋</span>知识总结
          </button>
        </div>

        <div class="sidebar-section">
          <div class="section-title">难度选择</div>
          <div class="radio-group">
            <label class="radio-item" :class="{ active: selectedLevel === 'beginner' }">
              <input type="radio" v-model="selectedLevel" value="beginner" />入门
            </label>
            <label class="radio-item" :class="{ active: selectedLevel === 'intermediate' }">
              <input type="radio" v-model="selectedLevel" value="intermediate" />进阶
            </label>
            <label class="radio-item" :class="{ active: selectedLevel === 'advanced' }">
              <input type="radio" v-model="selectedLevel" value="advanced" />专家
            </label>
          </div>
        </div>

        <div class="sidebar-section">
          <div class="section-title">知识主题</div>
          <div class="radio-group">
            <label class="radio-item" :class="{ active: selectedTopic === 'java' }">
              <input type="radio" v-model="selectedTopic" value="java" />Java
            </label>
            <label class="radio-item" :class="{ active: selectedTopic === 'spring' }">
              <input type="radio" v-model="selectedTopic" value="spring" />Spring
            </label>
            <label class="radio-item" :class="{ active: selectedTopic === 'ai' }">
              <input type="radio" v-model="selectedTopic" value="ai" />AI/LLM
            </label>
          </div>
        </div>
      </div>

      <!-- 右侧对话区域 -->
      <div class="chat-area">
        <ChatRoom
          :messages="messages"
          :connection-status="connectionStatus"
          ai-type="teaching"
          @send-message="sendMessage"
        />
      </div>
    </div>

    <!-- 快捷功能弹窗 -->
    <div v-if="modalVisible" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <span class="modal-title">{{ modalConfig.title }}</span>
          <button class="modal-close" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <div v-for="field in modalConfig.fields" :key="field.key" class="form-item">
            <label class="form-label">{{ field.label }}</label>
            <input
              v-if="field.type === 'text'"
              v-model="modalForm[field.key]"
              class="form-input"
              :placeholder="field.placeholder"
            />
            <select v-else-if="field.type === 'select'" v-model="modalForm[field.key]" class="form-input">
              <option v-for="opt in field.options" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="modal-cancel" @click="closeModal">取消</button>
          <button class="modal-confirm" @click="submitModal">确认</button>
        </div>
      </div>
    </div>

    <div class="footer-container">
      <AppFooter />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import ChatRoom from '../components/ChatRoom.vue'
import AppFooter from '../components/AppFooter.vue'
import {
  chatWithTeachingAssistant,
  explainConcept,
  generateExercise,
  getLearningPath,
  summarizeTopic
} from '../api'

useHead({
  title: 'AI知识教学助手 - 鱼皮AI超级智能体应用平台',
  meta: [
    { name: 'description', content: 'AI知识教学助手专注于Java/Spring/AI开发领域的教学辅导' },
    { name: 'keywords', content: 'AI教学,Java,Spring,RAG,大模型,编程学习' }
  ]
})

const router = useRouter()
const messages = ref([])
const chatId = ref('')
const connectionStatus = ref('disconnected')
const selectedLevel = ref('intermediate')
const selectedTopic = ref('java')
const modalVisible = ref(false)
const modalType = ref('')
const modalForm = ref({})
let eventSource = null

// 弹窗配置
const modalConfigs = {
  explain: {
    title: '💡 解释概念',
    fields: [
      { key: 'concept', type: 'text', label: '概念名称', placeholder: '如：Spring IoC、HashMap、RAG' },
      { key: 'level', type: 'select', label: '难度层次', options: [
        { value: 'beginner', label: '入门' },
        { value: 'intermediate', label: '进阶' },
        { value: 'advanced', label: '专家' }
      ]}
    ]
  },
  exercise: {
    title: '✏️ 生成练习题',
    fields: [
      { key: 'topic', type: 'text', label: '知识主题', placeholder: '如：Java集合、Spring AOP、Prompt工程' },
      { key: 'difficulty', type: 'select', label: '题目难度', options: [
        { value: 'easy', label: '简单' },
        { value: 'medium', label: '中等' },
        { value: 'hard', label: '困难' }
      ]}
    ]
  },
  path: {
    title: '🗺️ 学习路径推荐',
    fields: [
      { key: 'topic', type: 'text', label: '学习目标', placeholder: '如：Java后端开发、Spring AI应用' },
      { key: 'currentLevel', type: 'select', label: '当前水平', options: [
        { value: 'beginner', label: '入门（零基础）' },
        { value: 'intermediate', label: '进阶（有基础）' },
        { value: 'advanced', label: '专家（深入研究）' }
      ]}
    ]
  },
  summary: {
    title: '📋 知识点总结',
    fields: [
      { key: 'topic', type: 'text', label: '总结主题', placeholder: '如：Java集合框架、Spring核心、RAG技术' }
    ]
  }
}

const modalConfig = ref(modalConfigs.explain)

const openModal = (type) => {
  modalType.value = type
  modalConfig.value = modalConfigs[type]
  // 预填当前选择的难度和主题
  modalForm.value = {
    level: selectedLevel.value,
    currentLevel: selectedLevel.value,
    topic: selectedTopic.value
  }
  modalVisible.value = true
}

const closeModal = () => {
  modalVisible.value = false
}

const submitModal = () => {
  const form = modalForm.value
  closeModal()
  let sseCall = null
  let displayMessage = ''

  if (modalType.value === 'explain') {
    const levelLabel = { beginner: '入门', intermediate: '进阶', advanced: '专家' }[form.level] || form.level
    displayMessage = `请以【${levelLabel}】级别解释：${form.concept}`
    sseCall = () => explainConcept(form.concept, form.level, chatId.value)
  } else if (modalType.value === 'exercise') {
    const diffLabel = { easy: '简单', medium: '中等', hard: '困难' }[form.difficulty] || form.difficulty
    displayMessage = `为【${form.topic}】生成${diffLabel}难度练习题`
    sseCall = () => generateExercise(form.topic, form.difficulty, chatId.value)
  } else if (modalType.value === 'path') {
    const levelLabel = { beginner: '入门', intermediate: '进阶', advanced: '专家' }[form.currentLevel] || form.currentLevel
    displayMessage = `我是${levelLabel}水平，推荐【${form.topic}】的学习路径`
    sseCall = () => getLearningPath(form.topic, form.currentLevel, chatId.value)
  } else if (modalType.value === 'summary') {
    displayMessage = `总结【${form.topic}】的核心知识点`
    sseCall = () => summarizeTopic(form.topic, chatId.value)
  }

  if (sseCall) {
    sendSSE(displayMessage, sseCall)
  }
}

const addMessage = (content, isUser) => {
  messages.value.push({ content, isUser, time: new Date().getTime() })
}

const sendSSE = (displayMessage, sseCallFn) => {
  addMessage(displayMessage, true)
  if (eventSource) eventSource.close()

  const aiMessageIndex = messages.value.length
  addMessage('', false)
  connectionStatus.value = 'connecting'
  eventSource = sseCallFn()

  eventSource.onmessage = (event) => {
    const data = event.data
    if (data && data !== '[DONE]') {
      if (aiMessageIndex < messages.value.length) {
        messages.value[aiMessageIndex].content += data
      }
    }
    if (data === '[DONE]') {
      connectionStatus.value = 'disconnected'
      eventSource.close()
    }
  }

  eventSource.onerror = () => {
    connectionStatus.value = 'error'
    eventSource.close()
  }
}

const sendMessage = (message) => {
  sendSSE(message, () => chatWithTeachingAssistant(message, chatId.value))
}

const goBack = () => router.push('/')

const generateChatId = () => 'teach_' + Math.random().toString(36).substring(2, 10)

onMounted(() => {
  chatId.value = generateChatId()
  addMessage('你好！我是 AI 知识教学助手，专注于 Java、Spring 和 AI 开发领域的教学辅导。\n\n你可以直接提问，也可以使用左侧快捷功能：\n- 💡 **解释概念**：按难度层次讲解技术概念\n- ✏️ **生成练习题**：生成配套练习和参考答案\n- 🗺️ **学习路径**：制定个性化学习计划\n- 📋 **知识总结**：系统梳理知识体系', false)
})

onBeforeUnmount(() => {
  if (eventSource) eventSource.close()
})
</script>

<style scoped>
.teaching-assistant-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f0f9ff;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: linear-gradient(135deg, #0ea5e9, #10b981);
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 10;
}

.back-button {
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: opacity 0.2s;
}
.back-button:hover { opacity: 0.8; }
.back-button:before { content: '←'; margin-right: 8px; }

.title { font-size: 20px; font-weight: bold; margin: 0; }
.chat-id { font-size: 14px; opacity: 0.8; }

.content-wrapper {
  display: flex;
  flex: 1;
  gap: 0;
}

/* 左侧面板 */
.sidebar {
  width: 200px;
  min-width: 200px;
  background: white;
  border-right: 1px solid #e0f2fe;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-section { display: flex; flex-direction: column; gap: 8px; }

.section-title {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding-bottom: 4px;
  border-bottom: 1px solid #e0f2fe;
}

.feature-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border: 1px solid #e0f2fe;
  border-radius: 8px;
  background: #f8fafc;
  color: #334155;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
}
.feature-btn:hover {
  background: #e0f2fe;
  border-color: #0ea5e9;
  color: #0369a1;
}
.btn-icon { font-size: 16px; }

.radio-group { display: flex; flex-direction: column; gap: 6px; }

.radio-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #475569;
  transition: background 0.2s;
}
.radio-item:hover { background: #f0f9ff; }
.radio-item.active { background: #e0f2fe; color: #0369a1; font-weight: 500; }
.radio-item input { accent-color: #0ea5e9; }

/* 右侧对话区域 */
.chat-area {
  flex: 1;
  padding: 16px;
  overflow: hidden;
  min-height: calc(100vh - 56px - 180px);
  margin-bottom: 16px;
}

/* 弹窗 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal {
  background: white;
  border-radius: 12px;
  width: 420px;
  max-width: 90vw;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
}
.modal-title { font-size: 16px; font-weight: 600; color: #1e293b; }
.modal-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #94a3b8;
  line-height: 1;
}
.modal-close:hover { color: #475569; }

.modal-body { padding: 20px; display: flex; flex-direction: column; gap: 16px; }

.form-item { display: flex; flex-direction: column; gap: 6px; }
.form-label { font-size: 14px; font-weight: 500; color: #374151; }
.form-input {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  color: #1f2937;
  outline: none;
  transition: border-color 0.2s;
}
.form-input:focus { border-color: #0ea5e9; box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.1); }

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 20px;
  border-top: 1px solid #e2e8f0;
}
.modal-cancel {
  padding: 8px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: white;
  color: #374151;
  cursor: pointer;
  font-size: 14px;
}
.modal-cancel:hover { background: #f9fafb; }
.modal-confirm {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #0ea5e9, #10b981);
  color: white;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}
.modal-confirm:hover { opacity: 0.9; }

.footer-container { margin-top: auto; }

@media (max-width: 768px) {
  .sidebar { display: none; }
  .header { padding: 12px 16px; }
  .title { font-size: 18px; }
  .chat-id { font-size: 12px; }
}

@media (max-width: 480px) {
  .chat-id { display: none; }
}
</style>
