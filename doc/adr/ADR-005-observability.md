# ADR-005 — Observability Stack

## Status
Accepted

## Context
training-metrics-api is a portfolio project targeting Java/Spring Boot backend
missions in banking environments (instant payments). Observability is a
prerequisite in these contexts: real-time incident diagnosis, strict SLAs,
and regulatory audit requirements.

The three pillars of observability are addressed: logs, metrics, and traces.

## Decisions

### Structured Logs
- Format: ECS (Elastic Common Schema) via `logging.structured.format.console=ecs`
- Native Spring Boot 4, no additional dependency required
- Compatible with both ELK and Loki stacks
- MDC automatically populated by Micrometer Tracing (traceId, spanId)

### Metrics
- Spring Boot Actuator + Micrometer (included in spring-boot-starter-actuator)
- Exposed endpoints: health, metrics, info, loggers
- Management port: 8081 (network isolation, following production best practice)
- Endpoints are public on port 8081 — in production this port would be
  blocked at the network/firewall level
- Prometheus not included: no metrics storage backend in this project.
  In production: add /actuator/prometheus + Prometheus + Grafana.

### Traces
- Micrometer Tracing + OpenTelemetry bridge
- Backend: Jaeger v2 (single Docker container, image jaegertracing/jaeger:2)
- Jaeger UI: http://localhost:16686
- OTLP export endpoint: http://localhost:4318
- Sampling probability: 1.0 (100% of requests traced — acceptable for a
  standalone project, should be reduced in production)
- traceId and spanId automatically injected into logs via MDC
- Log/trace correlation enabled: search by traceId across both systems

## Alternatives Considered
- Zipkin: historical standard, superseded by OpenTelemetry + Jaeger
- Prometheus: out of scope without metrics storage requirements
- Actuator on main port (8080): security anti-pattern, management port
  must be isolated in production
- Admin account to secure Actuator: out of scope for phase 2
- Grafana stack (Loki + Tempo): coherent modern alternative to ELK + Jaeger,
  but Jaeger is more commonly found in banking environments

## Consequences
- JSON structured logs are less human-readable in local dev — acceptable tradeoff
- Port 8081 must be documented in README and docker-compose
- Three new dependencies added: spring-boot-starter-actuator,
  micrometer-tracing-bridge-otel, opentelemetry-exporter-otlp
- Jaeger container added to docker-compose.yml
