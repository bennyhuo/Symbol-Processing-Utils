package com.bennyhuo.kotlin.processor.module.ksp

import com.bennyhuo.kotlin.processor.module.LibraryIndex
import com.bennyhuo.kotlin.processor.module.common.IndexGenerator
import com.bennyhuo.kotlin.processor.module.common.UniElement
import com.bennyhuo.kotlin.processor.module.utils.PACKAGE_NAME
import com.bennyhuo.kotlin.processor.module.utils.generateName
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.writeTo


/**
 * Created by benny.
 */
internal class KspIndexGenerator(
    private val env: SymbolProcessorEnvironment
) : IndexGenerator {

    override fun generate(elements: Collection<UniElement>) {
        if (elements.isEmpty()) return

        val sortedElementNames = elements.map { it.enclosingTypeName }.sortedBy { it }

        val indexName = "LibraryIndex_${generateName(sortedElementNames)}"
        val typeSpec = TypeSpec.classBuilder(indexName)
            .addAnnotation(
                AnnotationSpec.builder(LibraryIndex::class.java)
                    .addMember(
                        "value", "{${sortedElementNames.joinToString { "\$S" }}}",
                        *sortedElementNames.toTypedArray()
                    ).build()
            ).also { typeBuilder ->
                elements.mapNotNull {
                    (it.rawElement as KSAnnotated).containingFile
                }.forEach {
                    typeBuilder.addOriginatingKSFile(it)
                }
            }
            .build()

        FileSpec.builder(PACKAGE_NAME, typeSpec.name!!).build().writeTo(env.codeGenerator, true)
    }
}