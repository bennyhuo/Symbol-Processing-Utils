package com.bennyhuo.kotlin.processor.module.ksp

import com.bennyhuo.kotlin.processor.module.LibraryIndex
import com.bennyhuo.kotlin.processor.module.common.IndexLoader
import com.bennyhuo.kotlin.processor.module.common.UniElement
import com.bennyhuo.kotlin.processor.module.common.UniTypeElement
import com.bennyhuo.kotlin.processor.module.utils.PACKAGE_NAME
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Created by benny.
 */
internal class KspIndexLoader(
    private val resolver: Resolver,
    override val annotations: Set<String>
) : IndexLoader {

    override fun findAnnotatedElementsByTypeName(enclosingTypeName: String): Collection<Pair<String, UniElement>> {
        val declarationName = DeclarationName.parse(enclosingTypeName)
        return resolver.getDeclarations(declarationName).flatMap {
            findAnnotatedElements(it.toUniElement())
        }
    }

    override fun getTypeElement(typeName: String): UniTypeElement? {
        return resolver.getClassDeclarationByName(typeName)?.toUniElement()
    }

    override fun getIndexes(): List<LibraryIndex> {
        return resolver.getDeclarationsFromPackage(PACKAGE_NAME)
            .filterIsInstance<KSClassDeclaration>()
            .mapNotNull {
                it.getAnnotationsByType(LibraryIndex::class).firstOrNull()
            }.toList()
    }

    fun loadUnwrapped() = load().mapValues {
        it.value.map { it.unwrap<KSAnnotated>() }
    }
}