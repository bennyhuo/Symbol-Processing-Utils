package com.bennyhuo.kotlin.processor.module.xprocessing

import androidx.room.compiler.processing.XFiler
import androidx.room.compiler.processing.XProcessingEnv
import com.bennyhuo.kotlin.processor.module.LibraryIndex
import com.bennyhuo.kotlin.processor.module.common.IndexGenerator
import com.bennyhuo.kotlin.processor.module.common.UniElement
import com.bennyhuo.kotlin.processor.module.utils.PACKAGE_NAME
import com.bennyhuo.kotlin.processor.module.utils.generateName
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Element

/**
 * Created by benny.
 */
internal class XProcessingIndexGenerator(
    private val env: XProcessingEnv
) : IndexGenerator {

    override fun generate(elements: Collection<UniElement>) {
        if (elements.isEmpty()) return

        val sortedElementNames = elements.map { it.enclosingTypeName }.distinct().sortedBy { it }

        val indexName = "LibraryIndex_${generateName(sortedElementNames)}"
        val typeSpec = TypeSpec.classBuilder(indexName)
            .addAnnotation(
                AnnotationSpec.builder(LibraryIndex::class.java)
                    .addMember(
                        "value", "{${sortedElementNames.joinToString { "\$S" }}}",
                        *sortedElementNames.toTypedArray()
                    ).build()
            ).also { typeBuilder ->
                elements.forEach {
                    typeBuilder.addOriginatingElement(it.rawElement as Element)
                }
            }
            .build()

        env.filer.write(JavaFile.builder(PACKAGE_NAME, typeSpec).build(), XFiler.Mode.Aggregating)
    }
}