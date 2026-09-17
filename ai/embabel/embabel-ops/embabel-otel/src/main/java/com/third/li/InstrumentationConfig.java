package com.third.li;

import com.embabel.agent.api.event.observation.AgentInstrumentation;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import kotlin.jvm.functions.Function0;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.function.Supplier;

/**
 * 把**真正的** {@link AgentInstrumentation} 接进 Micrometer Observation。
 *
 * <h3>为什么需要这个模块</h3>
 * 框架核心**默认不产生任何 span**：{@code AgentPlatformConfiguration} 注册的是
 * {@code NoOpAgentInstrumentation}，注释原话是
 * *"the core creates no span until an observability module contributes an
 * {@code AgentInstrumentation} adapter (registered {@code @Primary})"*。
 *
 * <p>也就是说：**"接入 OpenTelemetry"在 Embabel 里就是提供一个
 * {@code @Primary AgentInstrumentation} Bean**。本模块用 Micrometer 的
 * {@link ObservationRegistry} + 自定义 handler 演示这条路径；
 * 生产上把 handler 换成 OTLP exporter 即可，**Agent 代码一行不用改**。
 *
 * <h3>关键点</h3>
 * <ul>
 *   <li>必须标 {@code @Primary}：框架用 by-type / {@code getIfUnique} 解析，
 *       没有 {@code @Primary} 时两个同类型 Bean 会让它解析不出来（退回 no-op）。</li>
 *   <li>实现就是把框架给的 {@link Observation.Context} 交给
 *       {@code Observation.createNotStarted(...).observe(work)}——
 *       与框架内部 {@code Observations.observeOrSkip} 的写法一致
 *       （我们用的是公开 API，不依赖那个 {@code @InternalObservabilityApi}）。</li>
 *   <li>span 名由注册的 {@code ObservationConvention} 决定；没注册时用占位名，
 *       所以本模块的 handler 从 {@code context.getName()} 取值并打印。</li>
 * </ul>
 */
@Configuration
public class InstrumentationConfig {

    private static final Logger log = LoggerFactory.getLogger(InstrumentationConfig.class);

    /** 框架内部使用的占位 span 名（见 {@code Observations.PLACEHOLDER_NAME}）。 */
    private static final String PLACEHOLDER_NAME = "embabel.operation";

    @Bean
    public ObservationRegistry observationRegistry(SpanRecorder recorder) {
        ObservationRegistry registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(recorder);
        log.info("ObservationRegistry 已就绪，handler={}", recorder.getClass().getSimpleName());
        return registry;
    }

    /**
     * 注意 Bean **名**不能叫 {@code agentInstrumentation}——框架已经用这个名字注册了 no-op 版本，
     * 同名会直接启动失败（BeanDefinitionOverrideException）。这里用不同的名字 + {@code @Primary}：
     * 按类型注入时 {@code @Primary} 胜出。
     */
    @Bean
    @Primary
    public AgentInstrumentation micrometerAgentInstrumentation(ObservationRegistry registry) {
        // 注意泛型签名必须与接口一致（接口用的是 ? extends），否则 Java 认为是不同的方法
        return new AgentInstrumentation() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T observe(
                    Function0<? extends Observation.Context> contextSupplier,
                    Function0<? extends T> work) {
                Supplier<Observation.Context> context = () -> contextSupplier.invoke();
                return (T) Observation.createNotStarted(PLACEHOLDER_NAME, context, registry)
                        .observe(() -> work.invoke());
            }
        };
    }
}
