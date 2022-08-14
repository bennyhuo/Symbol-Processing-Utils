package com.bennyhuo.kotlin.processor.module.apt

import com.bennyhuo.kotlin.processor.module.LibraryIndex
import com.bennyhuo.kotlin.processor.module.common.IndexGenerator
import com.bennyhuo.kotlin.processor.module.common.UniElement
import com.bennyhuo.kotlin.processor.module.utils.PACKAGE_NAME
import com.bennyhuo.kotlin.processor.module.utils.generateName
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

/**
 * Created by benny.
 */
internal class AptIndexGenerator(
    private val env: ProcessingEnvironment,
    private val processorName: String
) : IndexGenerator {

    override fun generate(elements: Collection<UniElement>) {
        if (elements.isEmpty()) return

        val sortedElementNames = elements.map { it.enclosingTypeName }.distinct().sortedBy { it }

        val indexName = "${processorName.capitalize()}Index_${generateName(sortedElementNames)}"
        val typeSpec = TypeSpec.classBuilder(indexName)
            .addAnnotation(
                AnnotationSpec.builder(LibraryIndex::class.java)
                    .addMember(
                        "value", "{${sortedElementNames.joinToString { "\$S" }}}",
                        *sortedElementNames.toTypedArray()
                    ).build()
            ).also { typeBuilder ->
                elements.forEach {
                    typeBuilder.addOriginatingElement(it.unwrap<Element>())
                }
            }
            .build()

        JavaFile.builder(PACKAGE_NAME, typeSpec).build().writeTo(env.filer)
    }
}