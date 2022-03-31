package xyz.fteychene.teaching.metric

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.filter.MicrometerMetrics
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds


fun metricRegistry(): PrometheusMeterRegistry =
    PrometheusMeterRegistry(PrometheusConfig.DEFAULT).apply {
        ClassLoaderMetrics().bindTo(this)
        JvmMemoryMetrics().bindTo(this)
        JvmGcMetrics().bindTo(this)
        ProcessorMetrics().bindTo(this)
        JvmThreadMetrics().bindTo(this)
    }

fun main() {
    val registry: PrometheusMeterRegistry = metricRegistry()

    Gauge.builder("coffee.drank") { Random.nextDouble(0.0, 100.0)}
        .description("Coffee drank")
        .baseUnit("cl")
        .register(registry)

    Gauge.builder("money.available") { Random.nextDouble(0.0, 10000.0) }
    .description("Money available")
        .baseUnit("euros")
        .register(registry)

    val routes = ServerFilters.MicrometerMetrics.RequestCounter(registry)
        .then(ServerFilters.MicrometerMetrics.RequestTimer(registry))
        .then(routes(
            "/health" bind Method.GET to { Response(Status.OK).body("Alive") },
            "/metrics" bind Method.GET to {
                Response(Status.OK).body(registry.scrape())
            }
        ))
    routes.asServer(Undertow(8080)).start()
}