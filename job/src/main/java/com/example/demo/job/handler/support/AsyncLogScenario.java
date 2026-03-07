package com.example.demo.job.handler.support;

/**
 * Numbered log scenarios for AsyncLogTestJobHandler.
 */
public enum AsyncLogScenario {

    RUN_START("RUN", "6.0.1", "执行开始",
            "进入 AsyncLogTestJobHandler.execute，输出本次调用上下文摘要"),
    RUN_ALL_COMPLETED("RUN", "6.0.2", "等待完成",
            "所有异步/回调场景都在超时时间内执行完成"),
    RUN_WAIT_TIMEOUT("RUN", "6.0.3", "等待超时",
            "等待异步/回调场景完成超过超时时间，至少一个分支未按预期结束"),
    RUN_WAIT_INTERRUPTED("RUN", "6.0.4", "等待被中断",
            "主线程在等待异步/回调场景结束时被中断"),
    RUN_WAIT_ERROR("RUN", "6.0.5", "等待异常",
            "异步/回调场景中至少有一个分支抛出了异常"),
    RUN_FINISHED("RUN", "6.0.6", "执行结束",
            "execute 已完成场景派发、等待与收尾日志输出"),

    SYNC_DIRECT("SYNC", "6.1.1-A", "同步方法",
            "主线程直接打印日志，验证 @LogCollect 在同步调用链中自动生效"),

    SPRING_ASYNC_DEFAULT("SPRING_ASYNC_DEFAULT_CONFIGURER", "6.1.2-A",
            "Spring @Async（默认 AsyncConfigurer）",
            "未启用自定义 AsyncConfigurer 时，验证默认 @Async 异步线程自动透传 LogCollect 上下文"),
    SPRING_ASYNC_DEFAULT_SKIP("SPRING_ASYNC_DEFAULT_CONFIGURER", "6.1.2-S",
            "Spring @Async（默认 AsyncConfigurer）",
            "当前已启用自定义 AsyncConfigurer，同一轮不再验证默认 @Async 路径"),

    SPRING_ASYNC_CUSTOM("SPRING_ASYNC_CUSTOM_CONFIGURER", "6.1.3-A",
            "Spring @Async（自定义 AsyncConfigurer）",
            "启用自定义 AsyncConfigurer 且配置 TaskDecorator，验证默认 @Async 仍可透传 LogCollect 上下文"),
    SPRING_ASYNC_CUSTOM_SKIP("SPRING_ASYNC_CUSTOM_CONFIGURER", "6.1.3-S",
            "Spring @Async（自定义 AsyncConfigurer）",
            "当前未启用 profile job-async-custom，自定义 AsyncConfigurer 路径未生效"),

    SPRING_THREAD_POOL_ASYNC("SPRING_ASYNC_QUALIFIED", "6.1.4-A",
            "Spring ThreadPoolTaskExecutor",
            "通过 @Async(\"jobAsyncExecutor\") 指定 Spring 线程池，验证框架自动透传"),
    SPRING_THREAD_POOL_LISTENABLE_TASK("SPRING_LISTENABLE", "6.1.4-B",
            "Spring ThreadPoolTaskExecutor",
            "通过 ThreadPoolTaskExecutor.submitListenable 提交任务，验证 Spring 线程池自动透传"),
    SPRING_THREAD_POOL_LISTENABLE_SUCCESS("SPRING_LISTENABLE", "6.1.4-C",
            "Spring ThreadPoolTaskExecutor",
            "ListenableFuture 成功回调线程不固定，先 wrap 回调后验证上下文仍可恢复"),
    SPRING_THREAD_POOL_LISTENABLE_ERROR("SPRING_LISTENABLE", "6.1.4-D",
            "Spring ThreadPoolTaskExecutor",
            "ListenableFuture 失败回调线程不固定，先 wrap 回调后验证异常分支也可恢复上下文"),

    CF_SPRING_POOL_TASK("CF_SPRING_POOL", "6.1.5-A",
            "CompletableFuture + Spring 池",
            "CompletableFuture.runAsync 显式使用 Spring 线程池，验证任务体自动透传"),
    CF_SPRING_POOL_CALLBACK_SUCCESS("CF_SPRING_POOL", "6.1.5-B",
            "CompletableFuture + Spring 池",
            "whenComplete 成功回调线程不固定，先 wrap 回调后验证上下文恢复"),
    CF_SPRING_POOL_CALLBACK_ERROR("CF_SPRING_POOL", "6.1.5-C",
            "CompletableFuture + Spring 池",
            "whenComplete 异常回调线程不固定，先 wrap 回调后验证异常分支上下文恢复"),

    WEBFLUX_MONO("WEBFLUX_MONO", "6.1.6-A", "WebFlux Mono/Flux",
            "Mono.publishOn 切换到 Reactor 调度线程，验证框架 Hook/自动传播"),
    WEBFLUX_FLUX("WEBFLUX_FLUX", "6.1.6-B", "WebFlux Mono/Flux",
            "Flux.publishOn 切换到 Reactor 调度线程，验证框架 Hook/自动传播"),
    WEBFLUX_NOTE("WEBFLUX_MONO_FLUX", "6.1.6-N", "WebFlux Mono/Flux",
            "Boot 2.7 + Reactor 3.4.x 依赖框架 Hook；Boot 3.x / Reactor 3.5.3+ 为全自动传播，本次日志仅代表当前运行时"),
    WEBFLUX_SETUP_ERROR("WEBFLUX_MONO_FLUX", "6.1.6-E", "WebFlux Mono/Flux",
            "Reactor 场景在创建或订阅阶段失败，说明当前运行时无法完成该探针"),

    SPRING_BEAN_EXECUTOR_SERVICE("SPRING_BEAN_EXECUTOR_SERVICE", "6.1.7-A",
            "Spring Bean ExecutorService",
            "通过 Spring Bean 形式注入 ExecutorService，验证 BeanPostProcessor 自动包装后的上下文透传"),

    MANUAL_EXECUTOR("MANUAL_EXECUTOR", "6.1.8-A", "手动 ExecutorService",
            "手动创建线程池后使用工具类 wrapExecutorService 包装，验证非 Spring 托管线程池的一行接入"),

    RAW_THREAD("RAW_THREAD", "6.1.9-A", "new Thread()",
            "直接创建线程后使用 newDaemonThread 包装，验证子线程可恢复采集上下文"),

    THIRD_PARTY_CALLBACK("THIRD_PARTY_CALLBACK", "6.1.10-A", "第三方库回调",
            "Caffeine removalListener 在第三方回调线程触发前先 wrap，验证未知线程入口的一行接入"),

    FORK_JOIN_COMMON_POOL("FORK_JOIN", "6.1.11-A", "ForkJoinPool / parallelStream",
            "提交到 ForkJoinPool.commonPool 前先 wrapRunnable，验证 commonPool 场景的一行接入"),
    FORK_JOIN_PARALLEL_STREAM("FORK_JOIN", "6.1.11-B", "ForkJoinPool / parallelStream",
            "parallelStream().forEach 前先 wrapConsumer，验证 parallelStream 场景的一行接入"),

    NESTED_LOGCOLLECT("NESTED_LOGCOLLECT", "6.1.12-A", "嵌套 @LogCollect",
            "在已有采集上下文中再次进入 @LogCollect，验证栈式隔离"),
    NESTED_LOGCOLLECT_SKIP("NESTED_LOGCOLLECT", "6.1.12-S", "嵌套 @LogCollect",
            "当前 jobContext 为空，无法调用嵌套 @LogCollect 探针"),

    SERVLET_ASYNC_ACTIVE("SERVLET_ASYNC_CONTEXT", "6.1.13-A", "Servlet AsyncContext",
            "当前通过 WebAsyncTask/Servlet 异步请求线程触发 execute，用于验证 Servlet AsyncContext 场景"),
    SERVLET_ASYNC_NOTE("SERVLET_ASYNC_CONTEXT", "6.1.13-S", "Servlet AsyncContext",
            "当前不是 Servlet AsyncContext 请求线程；请改用 /jobs/async-log/servlet-async 验证该场景");

    private final String stage;
    private final String code;
    private final String title;
    private final String reason;

    AsyncLogScenario(String stage, String code, String title, String reason) {
        this.stage = stage;
        this.code = code;
        this.title = title;
        this.reason = reason;
    }

    public String stage() {
        return stage;
    }

    String message() {
        return message(null, null);
    }

    String message(String implementation) {
        return message(implementation, null);
    }

    public String message(String implementation, String extra) {
        StringBuilder builder = new StringBuilder();
        builder.append("[").append(code).append("] ").append(title);
        if (hasText(implementation)) {
            builder.append(" | 实现=").append(implementation);
        }
        builder.append(" | 原因=").append(reason);
        if (hasText(extra)) {
            builder.append(" | ").append(extra);
        }
        return builder.toString();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
