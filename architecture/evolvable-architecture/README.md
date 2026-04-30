# Skill Name: Evolvable Architecture

## Description
This skill defines the architecture guidelines and module interaction rules for a multi-module, evolvable system. It provides clear rules for layer responsibilities, internal module calls, cross-module calls, microservices/RPC integration, and input adapters (Controllers, MQ listeners, RPC endpoints).  
The skill ensures **single-direction dependencies, modular independence, and future evolvable microservices**, supported by ArchUnit rules for enforcement.

---

## Layers and Responsibilities

| Layer | Responsibility | Allowed Dependencies | Forbidden Content |
|-------|----------------|--------------------|-----------------|
| **api** | Stable module contract exposed for *other* modules to depend on. Contains interfaces, DTOs, Commands, Queries, and Events. This is the **only** layer that can be imported by other logical modules. | None | Controller, business implementation, technical infrastructure |
| **application** | Use case orchestration, transaction boundaries | domain, infrastructure, api (cross-module) | Controller, HTTP/MQ protocols |
| **domain** | Core domain model and rules | None | HTTP, MQ, Controller, cross-module dependency |
| **infrastructure** | Technical implementation: DB, cache, third-party services | domain, application | Business logic, Controller, cross-module dependency |
| **interfaces** | Input adapters: Controllers, MQ consumers, RPC implementation (Provider) | application, api | Core business logic, cross-module orchestration, direct domain/infrastructure access |

---

## Controller / Input Adapter Guidelines

- **Controllers are never in api or application layers.**  
- Place Controllers in **interfaces** layer or adapter module (`module-interfaces` / `module-adapter-web`).  
- Controllers call **application services**, never domain or repository directly.  
- `interfaces` layer may include:
  - HTTP Controllers (`@RestController`)
  - MQ Consumers (`@KafkaListener`, `@RabbitListener`)
  - RPC Implementation (`@DubboService`, `@GrpcService` - implements interfaces defined in `api` layer)
  - Protocol-specific DTOs and exception mapping  
- `interfaces` layer must not contain:
  - Core business logic  
  - Cross-module orchestration  
  - Repository or external client logic  

**Example: OrderController**

```java
@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderApplicationService orderApp;

    @PostMapping
    public OrderDTO createOrder(@RequestBody CreateOrderCommand cmd) {
        return orderApp.create(cmd);
    }
}
```

---

## Multi-Client Design in Interfaces

When a system needs to serve multiple client types (e.g., Web, Admin, Mobile) while sharing the same underlying business logic, the `interfaces` layer must be cleanly partitioned. 

To maintain clear boundaries without creating excessive physical modules, **client-specific input adapters must be separated by package** within the `interfaces` module/layer.

### Package Separation Rules

1. **Client-Specific Packages**: Inside the `interfaces` layer, create dedicated packages for each client type.
   - Example: `com.example.order.interfaces.web`
   - Example: `com.example.order.interfaces.admin`
   - Example: `com.example.order.interfaces.mobile`
2. **Independent DTOs**: Each client package MUST define its own Request/Response DTOs. **Never share DTOs across different clients**, even if they look identical initially, because client requirements will inevitably evolve independently.
3. **No Cross-Client Dependencies**: Controllers and DTOs in the `web` package must not depend on classes in the `admin` package, and vice versa.
4. **Shared Application Logic**: All client controllers should delegate business operations to the same underlying `application` service layer. The core business logic remains unified and agnostic of the client type.

### Selective Component Scanning for Deployment

This package structure allows you to build client-specific Boot applications (e.g., an Admin-only deployment) by selectively scanning the corresponding packages. This ensures that an Admin deployment does not accidentally expose Web or Mobile API endpoints, enhancing security and reducing deployment footprint.

```java
// Example: In boot-admin module, only expose admin endpoints
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.example.order.interfaces.admin", // Only scan Admin controllers
    "com.example.order.application",
    "com.example.order.domain",
    "com.example.order.infrastructure"
})
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
```
