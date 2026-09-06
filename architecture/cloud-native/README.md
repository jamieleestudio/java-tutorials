# ⑥ 云原生架构 (Cloud-Native Architecture)

## 概述

⑤ 的**平台化升级**：治理中间件全部下沉到 K8s 平台，应用回归普通 Spring Boot（无 Spring Cloud），真正 12-Factor。

## 与 ⑤ 的核心差异（中间件 → 平台能力）

| 能力 | ⑤ 微服务 | ⑥ 云原生 |
|---|---|---|
| 服务发现 | Nacos | **K8s Service DNS**（http://payment-service:8080） |
| 配置管理 | Nacos 配置中心 | **ConfigMap/Secret → 环境变量**（12-Factor） |
| 网关 | Spring Cloud Gateway | **K8s Ingress** |
| 健康检查 | Actuator 端点 | Actuator + **liveness/readiness 探针**（自愈） |
| 弹性伸缩 | 无 | **HPA**（CPU 70% 自动扩缩 2→6 副本） |
| 负载均衡 | 客户端 LB | **K8s Service**（平台级） |
| 镜像 | 无 | **Jib 分层构建**（无 Docker 守护进程） |
| 部署 | 手动 | **Helm Chart 一键部署** |

## 模块结构

```
cloud-native/
├── shared-kernel/
├── order-{domain,application,bootstrap}       bootstrap: K8s DNS RPC + 探针 + Jib
├── payment-{api,domain,application,bootstrap}
├── product-{api,domain,application,bootstrap}
└── deploy/
    ├── docker/          3 个 Dockerfile（多阶段构建，备用方案）
    ├── k8s/             Deployment+Service+探针 ×3、ingress、configmap、secret、hpa
    └── helm/ecommerce/  Chart: values + templates（deployment/service/ingress/secret/hpa）
```

## 云原生要点

- **统一 8080 端口**（K8s 容器惯例），JVM `-XX:MaxRAMPercentage=75` 适配容器内存限制
- **配置全环境变量化**：`SPRING_DATASOURCE_URL`、`PAYMENT_SERVICE_URL` 等由平台注入
- **探针**：`/actuator/health/liveness`（重启决策）、`/actuator/health/readiness`（摘流决策）
- **Order RPC**：RestClient 基址 = K8s Service DNS（`http://payment-service:8080`），无注册中心

## 构建镜像（Jib，无需 Docker）

```bash
cd architecture/cloud-native
mvn compile jib:build -pl order-bootstrap -am    # 推送 java-tutorials/cn-order-bootstrap:1.0
# 或构建到 Docker daemon：mvn compile jib:docker-build -pl order-bootstrap -am
```

## 部署

```bash
# 原生 manifests
kubectl apply -f architecture/cloud-native/deploy/k8s/

# 或 Helm（推荐）
helm install ecommerce architecture/cloud-native/deploy/helm/ecommerce
kubectl get pods,hpa,ingress
```

## 测试

```bash
mvn test   # 15 个：8 领域 + 1 流程(@MockBean远程服务) + 6 ArchUnit
```
本地无需 K8s/Kafka/Nacos——平台能力全部在部署层。