package com.bennyhuo.kotlin.processor.module.xprocessing

import androidx.room.compiler.processing.XElement
import androidx.room.compiler.processing.XFiler
import androidx.room.compiler.processing.XProcessingEnv
import androidx.room.compiler.processing.addOriginatingElement
import com.bennyhuo.kotlin.processor.module.LibraryIndex
import com.bennyhuo.kotlin.processor.module.common.IndexGenerator
import com.bennyhuo.kotlin.processor.module.common.UniElement
import com.bennyhuo.kotlin.processor.module.utils.PACKAGE_NAME
import com.bennyhuo.kotlin.processor.module.utils.generateName
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec

/**
 * Created by benny.
 */
internal class XProcessingIndexGenerator(
    private val env: XProcessingEnv,
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
                    typeBuilder.addOriginatingElement(it.unwrap<XElement>())
                }
            }
            .build()

        env.filer.write(JavaFile.builder(PACKAGE_NAME, typeSpec).build(), XFiler.Mode.Aggregating)
    }
}