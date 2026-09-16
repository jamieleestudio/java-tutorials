-- pgvector 扩展：embabel-vector-store 依赖它做向量检索。
-- 应用启动时也会执行 CREATE EXTENSION IF NOT EXISTS vector，
-- 这里放在初始化脚本里是为了让扩展在数据库创建时就可见（也便于手动验证）。
CREATE EXTENSION IF NOT EXISTS vector;
