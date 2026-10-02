# Northstar 运营管理平台 · 后端

Spring Boot 4 服务，为 Northstar 管理端提供认证、权限、组织、审计和文件接口。接口前缀 `/api/v1`，Token 只走 `Authorization` Header。

## 技术栈

- Java 17、Spring Boot 4.1
- MyBatis-Plus、Sa-Token、Hutool、Lombok
- MySQL 8（默认）或 H2（profile `h2`）

## 本地启动

需要 JDK 17。默认连接本机 MySQL：`localhost:3306/northstar`，账号 `root / root`（仅本地演示，上线务必改掉）。

```powershell
# 可选：用 Docker 起 MySQL
docker compose up -d

$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
.\mvnw.cmd -DskipTests spring-boot:run
```

切 H2 时设置 `spring.profiles.active=h2`。

服务端口 `8080`。前端开发地址默认 `http://localhost:8081`。

## 演示账号

| 账号 | 密码 | 说明 |
| --- | --- | --- |
| admin | Admin@123456 | 超级管理员 |
| ops | Ops@123456 | 运营经理 |
| reviewer | Reviewer@123456 | 审核员，首次登录需改密 |
| member | Member@123456 | 普通成员 |
| zhao | Member@123456 | 已冻结 |

空库首次启动会自动建表并写入种子数据。
