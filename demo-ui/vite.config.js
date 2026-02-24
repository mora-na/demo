import {defineConfig, loadEnv} from "vite";
import vue from "@vitejs/plugin-vue";
import AutoImport from "unplugin-auto-import/vite";
import Components from "unplugin-vue-components/vite";
import {ElementPlusResolver} from "unplugin-vue-components/resolvers";

export default defineConfig(({mode}) => {
    const env = loadEnv(mode, process.cwd(), "");
    const proxyTarget = env.VITE_API_PROXY_TARGET || "http://localhost:8080";
    const targetUrl = new URL(proxyTarget);
    const targetHostHeader = targetUrl.host;

    return {
        plugins: [
            vue(),
            AutoImport({
                resolvers: [ElementPlusResolver()],
                dts: "src/auto-imports.d.ts"
            }),
            Components({
                resolvers: [ElementPlusResolver({importStyle: "css"})],
                dts: "src/components.d.ts"
            })
        ],
        css: {
            preprocessorOptions: {
                scss: {
                    api: "modern-compiler"
                },
                sass: {
                    api: "modern-compiler"
                }
            }
        },
        build: {
            rollupOptions: {
                output: {
                    manualChunks(id) {
                        if (!id.includes("node_modules")) {
                            return;
                        }
                        const normalized = id.split("\\").join("/");
                        if (normalized.includes("/node_modules/element-plus/")) {
                            return "vendor-element-plus";
                        }
                        if (normalized.includes("/node_modules/sm-crypto/")) {
                            return "vendor-crypto";
                        }
                        if (
                            normalized.includes("/node_modules/vue/") ||
                            normalized.includes("/node_modules/@vue/") ||
                            normalized.includes("/node_modules/vue-router/") ||
                            normalized.includes("/node_modules/pinia/") ||
                            normalized.includes("/node_modules/vue-i18n/")
                        ) {
                            return "vendor-vue";
                        }
                        if (normalized.includes("/node_modules/lucide-vue-next/")) {
                            return "vendor-icons";
                        }
                        return undefined;
                    }
                }
            }
        },
        server: {
            port: 5173,
            proxy: {
                "/prod-api": {
                    target: proxyTarget,
                    changeOrigin: true,
                    rewrite: (path) => path.replace(/^\/prod-api/, ""),
                    configure(proxy) {
                        proxy.on("proxyReq", (proxyReq) => {
                            // Ensure IPv6 host header is bracketed (URL.host already does this).
                            proxyReq.setHeader("host", targetHostHeader);
                        });
                    }
                },
                "/auth": {
                    target: proxyTarget,
                    changeOrigin: true,
                    configure(proxy) {
                        proxy.on("proxyReq", (proxyReq) => {
                            // Ensure IPv6 host header is bracketed (URL.host already does this).
                            proxyReq.setHeader("host", targetHostHeader);
                        });
                    }
                }
            }
        }
    };
});
