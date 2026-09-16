package com.third.li

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * 程序化 DSL 示例入口。
 */
@SpringBootApplication
class DslApplication

fun main(args: Array<String>) {
    runApplication<DslApplication>(*args)
}
