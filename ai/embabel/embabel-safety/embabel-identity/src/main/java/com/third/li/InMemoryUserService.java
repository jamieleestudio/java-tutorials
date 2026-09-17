package com.third.li;

import com.embabel.agent.api.identity.SimpleUser;
import com.embabel.agent.api.identity.User;
import com.embabel.agent.api.identity.UserService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 极简的 {@link UserService} 实现（内存版）。
 *
 * <p>真实系统里这一步通常对接你已有的账号体系（LDAP / 数据库 / SSO）。
 * 框架只要求实现 {@code findById} / {@code findByUsername} / {@code findByEmail}
 * 与可选的 {@code provisionUser}（首次见到用户时自动开户）。
 */
@Component
public class InMemoryUserService implements UserService<SimpleUser> {

    private final List<SimpleUser> users = List.of(
            new SimpleUser("u-1001", "Alice（Acme 采购）", "alice", "alice@acme.example"),
            new SimpleUser("u-1002", "Bob（Globex 财务）", "bob", "bob@globex.example"),
            // 服务账号：用于"以更高权限代表用户执行"（runAs）
            new SimpleUser("svc-1", "Service Account", "svc-agent", "svc@internal.example"));

    @Override
    public SimpleUser findById(String id) {
        return find(user -> user.getId().equals(id));
    }

    @Override
    public SimpleUser findByUsername(String username) {
        return find(user -> user.getUsername().equals(username));
    }

    @Override
    public SimpleUser findByEmail(String email) {
        return find(user -> user.getEmail().equals(email));
    }

    /** 服务账号：runAs 的目标身份。 */
    public SimpleUser serviceAccount() {
        return findByUsername("svc-agent");
    }

    private SimpleUser find(java.util.function.Predicate<SimpleUser> predicate) {
        Optional<SimpleUser> found = users.stream().filter(predicate).findFirst();
        return found.orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }
}
