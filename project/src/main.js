import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
// 在整个App.vue中可以使用路由
//导入路由配置
import router from '/src/router/router.js'
//导入pinia
import { createPinia } from 'pinia'
//引入Element-ui组件
import ElementPlus from 'element-plus'
import zhCn from "element-plus/es/locale/lang/zh-cn";
import 'element-plus/dist/index.css'
//绑定路由对象
createApp(App)
.use(router)
.use(createPinia())
.use(ElementPlus, {
  locale: zhCn,
})
.mount('#app')
