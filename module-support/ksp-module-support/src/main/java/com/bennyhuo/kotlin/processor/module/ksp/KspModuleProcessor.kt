package com.bennyhuo.kotlin.processor.module.ksp

import com.bennyhuo.kotlin.processor.module.common.MODULE_LIBRARY
import com.bennyhuo.kotlin.processor.module.common.MODULE_MAIN
import com.bennyhuo.kotlin.processor.module.common.MODULE_MIXED
import com.bennyhuo.kotlin.processor.module.common.parseModuleType
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated

/**
 * Created by benny.
 */
abstract class KspModuleProcessor(
    val env: SymbolProcessorEnvironment
) : SymbolProcessor {

    abstract val annotationsForIndex: Set<String>
    abstract val processorName: String
    open val supportedModuleTypes: Set<Int> = setOf(MODULE_MAIN, MODULE_LIBRARY, MODULE_MIXED)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotatedSymbols = annotationsForIndex.associateWith { resolver.getSymbolsWithAnnotation(it).toSet() }
        return when (parseModuleType(processorName, env.options, supportedModuleTypes)) {
            MODULE_MAIN -> {
                val elementsFromLibrary = KspIndexLoader(resolver, annotationsForIndex).loadUnwrapped()
                processMain(resolver, annotatedSymbols.mapValues {
                    it.value + elementsFromLibrary.getOrDefault(it.key, emptySet())
                })
            }
            MODULE_LIBRARY -> {
                processLibrary(resolver, annotatedSymbols)
            }
            MODULE_MIXED -> {
                val elementsFromLibrary = KspIndexLoader(resolver, annotationsForIndex).loadUnwrapped()
                processMain(resolver, annotatedSymbols.mapValues {
                    it.value + elementsFromLibrary.getOrDefault(it.key, emptySet())
                }) + processLibrary(resolver, annotatedSymbols)
            }
            else -> emptyList()
        }
    }

    abstract fun processMain(resolver: Resolver, annotatedSymbols: Map<String, Set<KSAnnotated>>): List<KSAnnotated>

    open fun processLibrary(
        resolver: Resolver,
        annotatedSymbols: Map<String, Set<KSAnnotated>>
    ): List<KSAnnotated> {
        KspIndexGenerator(env, processorName).generate(annotatedSymbols.values.flatMap {
            it.map { it.toUniElement() }
        })
        return emptyList()
    }

}