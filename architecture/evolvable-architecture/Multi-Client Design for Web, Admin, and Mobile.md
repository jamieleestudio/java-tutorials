# Evolvable Architecture: Multi-Client Design for Web, Admin, and Mobile

## 1. Introduction

This document describes the approach to managing multi-client architectures, specifically when dealing with **web**, **admin**, and **mobile** client types. The goal is to **maintain clean separation of concerns** by structuring controllers based on the client type, and by ensuring that each client type is deployable independently without affecting the underlying business logic.

## 2. Architecture Overview

The system is built around the principle of **adapter-based architecture**, where:

- **Each client type (web, admin, mobile)** will have its own **Controller** layer.
- Business logic, represented by the **application** and **domain layers**, remains **shared** and independent of client type.
- **Boot modules** are used to control which client-specific controllers are loaded, allowing each client type to be deployed independently or together.

## 3. Project Structure

The project follows a **modular structure** where each module contains a distinct layer of the application. The structure supports flexible deployment and modular development.

### 3.1 Project Structure Example

```text
order
├─ boot
│  ├─ boot-monolith        # All modules and client types together (default for full deployment)
│  ├─ boot-web             # Web application deployment
│  ├─ boot-admin           # Admin panel deployment
│  └─ boot-mobile          # Mobile application deployment
├─ modules
│  ├─ order-api
│  ├─ order-application
│  ├─ order-domain
│  ├─ order-infrastructure
│  └─ order-interfaces     # Controllers are separated by package here
│     ├─ web               # Web client controllers and DTOs
│     ├─ admin             # Admin panel controllers and DTOs
│     └─ mobile            # Mobile client controllers and DTOs

## 4. Multi-Client Package Structure in Interfaces

To maintain clear boundaries without creating excessive physical modules, client-specific input adapters must be separated into different packages within the `interfaces` module.

### 4.1 Package Separation Rules

1. **Client-Specific Packages**: Inside the `interfaces` layer, create dedicated packages for each client type (e.g., `com.example.order.interfaces.web`, `com.example.order.interfaces.admin`).
2. **Independent DTOs**: Each client package should define its own Request/Response DTOs. Never share DTOs across different clients, even if they look identical initially, as client requirements evolve independently.
3. **No Cross-Client Dependencies**: Controllers in the `web` package must not depend on classes in the `admin` package, and vice versa.
4. **Shared Application Logic**: All client controllers should delegate business operations to the same underlying `application` service layer. If a client requires specific orchestration, it can be handled in a client-specific application service or use case, but the core business logic remains unified.

### 4.2 Selective Component Scanning

Boot modules control which client interfaces are exposed by selectively scanning the corresponding packages:

```java
// In boot-admin module
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.example.order.interfaces.admin",
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

This approach ensures that the admin deployment does not expose web or mobile API endpoints, enhancing security and reducing deployment footprint, while keeping the project structure manageable.
```
