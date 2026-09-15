package com.third.li

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

/**
 * 工作流原语示例入口（Kotlin）。
 */
@SpringBootApplication
class WorkflowsApplication

fun main(args: Array<String>) {
    SpringApplicationBuilder(WorkflowsApplication::class.java).run(*args)
}
