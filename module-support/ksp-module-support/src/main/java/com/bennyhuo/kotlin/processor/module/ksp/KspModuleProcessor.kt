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

    val moduleType by lazy {
        parseModuleType(processorName, env.options, supportedModuleTypes)
    }

    private val symbolsForIndex = HashSet<KSAnnotated>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotatedSymbols = annotationsForIndex.associateWith {
            resolver.getSymbolsWithAnnotation(it).toSet()
        }

        val deferredSymbols = ArrayList<KSAnnotated>()

        if (moduleType == MODULE_MAIN || moduleType == MODULE_MIXED) {
            val elementsFromLibrary = KspIndexLoader(resolver, annotationsForIndex).loadUnwrapped()
            deferredSymbols += processMain(resolver, annotatedSymbols.mapValues {
                it.value + elementsFromLibrary.getOrDefault(it.key, emptySet())
            })
        }

        if (moduleType == MODULE_LIBRARY || moduleType == MODULE_MIXED) {
            symbolsForIndex.addAll(annotatedSymbols.values.flatten())
        }

        if (moduleType == MODULE_LIBRARY) {
            processLibrary(resolver, annotatedSymbols)
        }

        return deferredSymbols
    }

    abstract fun processMain(
        resolver: Resolver,
        annotatedSymbols: Map<String, Set<KSAnnotated>>
    ): List<KSAnnotated>

    open fun processLibrary(
        resolver: Resolver,
        annotatedSymbols: Map<String, Set<KSAnnotated>>
    ): List<KSAnnotated> = emptyList()

    override fun finish() {
        super.finish()
        KspIndexGenerator(env, processorName).generate(symbolsForIndex.map {
            it.toUniElement()
        })
        symbolsForIndex.clear()
    }

}