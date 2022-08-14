package com.bennyhuo.kotlin.processor.module.xprocessing

import androidx.room.compiler.processing.XElement
import androidx.room.compiler.processing.XProcessingEnv
import androidx.room.compiler.processing.XProcessingStep
import com.bennyhuo.kotlin.processor.module.common.MODULE_LIBRARY
import com.bennyhuo.kotlin.processor.module.common.MODULE_MAIN
import com.bennyhuo.kotlin.processor.module.common.MODULE_MIXED
import com.bennyhuo.kotlin.processor.module.common.parseModuleType

/**
 * Created by benny.
 */
abstract class XProcessingModuleStep : XProcessingStep {

    abstract val processorName: String

    open val annotationsForIndex: Set<String>
        get() = annotations()

    open val supportedModuleTypes: Set<Int> = setOf(MODULE_MAIN, MODULE_LIBRARY, MODULE_MIXED)

    final override fun process(env: XProcessingEnv, elementsByAnnotation: Map<String, Set<XElement>>): Set<XElement> {
        return when(parseModuleType(processorName, env.options, supportedModuleTypes)) {
            MODULE_MAIN -> {
                val elementsFromLibrary = XProcessingIndexLoader(env, annotationsForIndex).loadUnwrap()
                processMain(env, elementsByAnnotation.mapValues {
                    it.value + elementsFromLibrary.getOrDefault(it.key, emptySet())
                })
            }
            MODULE_LIBRARY -> {
                processLibrary(env, elementsByAnnotation)
            }
            MODULE_MIXED -> {
                val elementsFromLibrary = XProcessingIndexLoader(env, annotationsForIndex).loadUnwrap()
                processMain(env, elementsByAnnotation.mapValues {
                    it.value + elementsFromLibrary.getOrDefault(it.key, emptySet())
                }) + processLibrary(env, elementsByAnnotation)
            }
            else -> emptySet()
        }
    }


    abstract fun processMain(env: XProcessingEnv, elementsByAnnotation: Map<String, Set<XElement>>): Set<XElement>

    open fun processLibrary(env: XProcessingEnv, elementsByAnnotation: Map<String, Set<XElement>>): Set<XElement> {
        XProcessingIndexGenerator(env, processorName).generate(elementsByAnnotation.values.flatMap {
            it.map { it.toUniElement() }
        })
        return emptySet()
    }

}