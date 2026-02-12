/// <reference types="vite/client" />
declare module "sm-crypto";
declare module "*.vue" {
    import type {DefineComponent} from "vue";
    const component: DefineComponent<{}, {}, unknown>;
  export default component;
}
