package com.bennyhuo.kotlin.processor.module.common

import com.bennyhuo.kotlin.processor.module.utils.OPTION_KEY_MODULE_TYPE

/**
 * Created by benny.
 */
const val MODULE_MAIN = 0
const val MODULE_LIBRARY = 1
const val MODULE_MIXED = 2

fun parseModuleType(
    processorName: String,
    options: Map<String, String>,
    supportedModuleTypes: Set<Int>
): Int {
    // return the only supported type as the default ignoring the args.
    if (supportedModuleTypes.size == 1) return supportedModuleTypes.first()

    val argument = options["${processorName}.$OPTION_KEY_MODULE_TYPE"]
    return when (argument) {
        "library" -> MODULE_LIBRARY
        "mixed" -> MODULE_MIXED
        "main" -> MODULE_MAIN
        else -> MODULE_MAIN
    }.also {
        if (it !in supportedModuleTypes) {
            throw IllegalArgumentException("Unsupported module type: $argument.")
        }
    }
}