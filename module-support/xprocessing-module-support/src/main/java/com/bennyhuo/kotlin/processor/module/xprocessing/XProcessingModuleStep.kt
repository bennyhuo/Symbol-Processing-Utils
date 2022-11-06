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

    private val symbolsForIndex = HashSet<XElement>()

    final override fun process(
        env: XProcessingEnv,
        elementsByAnnotation: Map<String, Set<XElement>>
    ): Set<XElement> {
        val moduleType = parseModuleType(processorName, env.options, supportedModuleTypes)
        val deferredSymbols = HashSet<XElement>()
        if (moduleType == MODULE_MAIN || moduleType == MODULE_MIXED) {
            val elementsFromLibrary = XProcessingIndexLoader(env, annotationsForIndex).loadUnwrap()
            processMain(env, elementsByAnnotation, elementsFromLibrary)
        }

        if (moduleType == MODULE_LIBRARY || moduleType == MODULE_MIXED) {
            symbolsForIndex.addAll(elementsByAnnotation.filterKeys {
                it in annotationsForIndex
            }.values.flatten())
        }

        if (moduleType == MODULE_LIBRARY) {
            processLibrary(env, elementsByAnnotation)
        }

        return deferredSymbols
    }

    abstract fun processMain(
        env: XProcessingEnv,
        elementsByAnnotation: Map<String, Set<XElement>>,
        elementsByAnnotationFromLibrary: Map<String, Set<XElement>>
    ): Set<XElement>

    open fun processLibrary(
        env: XProcessingEnv,
        elementsByAnnotation: Map<String, Set<XElement>>
    ): Set<XElement> = emptySet()

    override fun processOver(
        env: XProcessingEnv,
        elementsByAnnotation: Map<String, Set<XElement>>
    ) {
        super.processOver(env, elementsByAnnotation)
        XProcessingIndexGenerator(env, processorName).generate(symbolsForIndex.map {
            it.toUniElement()
        })
        symbolsForIndex.clear()
    }

}