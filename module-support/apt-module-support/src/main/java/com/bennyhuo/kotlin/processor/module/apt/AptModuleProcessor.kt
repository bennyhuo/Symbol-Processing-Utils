package com.bennyhuo.kotlin.processor.module.apt

import com.bennyhuo.kotlin.processor.module.common.MODULE_LIBRARY
import com.bennyhuo.kotlin.processor.module.common.MODULE_MAIN
import com.bennyhuo.kotlin.processor.module.common.MODULE_MIXED
import com.bennyhuo.kotlin.processor.module.common.parseModuleType
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

/**
 * Created by benny.
 */
abstract class AptModuleProcessor : AbstractProcessor() {

    lateinit var env: ProcessingEnvironment

    abstract val annotationsForIndex: Set<String>
    abstract val processorName: String
    open val supportedModuleTypes: Set<Int> = setOf(MODULE_MAIN, MODULE_LIBRARY, MODULE_MIXED)

    val moduleType by lazy {
        parseModuleType(processorName, env.options, supportedModuleTypes)
    }

    private val symbolsForIndex = HashSet<Element>()

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        this.env = processingEnv
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        if (roundEnv.processingOver()) {
            AptIndexGenerator(env, processorName).generate(symbolsForIndex.map {
                it.toUniElement()
            })
            symbolsForIndex.clear()
        } else {
            val annotatedSymbols = annotations.associate {
                it.qualifiedName.toString() to roundEnv.getElementsAnnotatedWith(it)
            }

            if (moduleType == MODULE_MAIN || moduleType == MODULE_MIXED) {
                val elementsFromLibrary = AptIndexLoader(env, annotationsForIndex).loadUnwrap()
                processMain(roundEnv, annotatedSymbols.mapValues {
                    it.value + elementsFromLibrary.getOrDefault(it.key, emptyList())
                })
            }

            if (moduleType == MODULE_LIBRARY || moduleType == MODULE_MIXED) {
                symbolsForIndex.addAll(annotatedSymbols.filterKeys {
                    it in annotationsForIndex
                }.values.flatten())
            }

            if (moduleType == MODULE_LIBRARY) {
                processLibrary(roundEnv, annotatedSymbols)
            }
        }
        return false
    }


    abstract fun processMain(
        roundEnv: RoundEnvironment,
        annotatedSymbols: Map<String, Set<Element>>
    )

    open fun processLibrary(
        roundEnv: RoundEnvironment,
        annotatedSymbols: Map<String, Set<Element>>
    ) = Unit

}