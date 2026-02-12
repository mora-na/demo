import {createApp} from "vue";
import ElementPlus from "element-plus";
import {createPinia} from "pinia";
import App from "./App.vue";
import {i18n} from "./i18n";
import "element-plus/dist/index.css";
import "./style.css";

const app = createApp(App);
app.use(createPinia());
app.use(i18n);
app.use(ElementPlus);
app.mount("#app");
