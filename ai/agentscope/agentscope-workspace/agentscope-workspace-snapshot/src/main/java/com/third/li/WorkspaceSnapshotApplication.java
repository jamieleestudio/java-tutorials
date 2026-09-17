package com.third.li;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** workspace-snapshot 示例入口。 */
@SpringBootApplication
public class WorkspaceSnapshotApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(WorkspaceSnapshotApplication.class).run(args);
    }
}