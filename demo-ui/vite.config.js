import {defineConfig, loadEnv} from "vite";
import vue from "@vitejs/plugin-vue";

export default defineConfig(({mode}) => {
    const env = loadEnv(mode, process.cwd(), "");
    const proxyTarget = env.VITE_API_PROXY_TARGET || "http://localhost:8080";
    const targetUrl = new URL(proxyTarget);
    const targetHostHeader = targetUrl.host;

    return {
        plugins: [vue()],
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
