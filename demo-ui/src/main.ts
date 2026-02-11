import {createApp} from "vue";
import ElementPlus from "element-plus";
import zhCn from "element-plus/dist/locale/zh-cn.mjs";
import {createPinia} from "pinia";
import App from "./App.vue";
import "element-plus/dist/index.css";
import "./style.css";

const app = createApp(App);
app.use(createPinia());
app.use(ElementPlus, {locale: zhCn});
app.mount("#app");
