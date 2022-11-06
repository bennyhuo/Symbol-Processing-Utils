package com.bennyhuo.kotlin.processor.module.common

import com.bennyhuo.kotlin.processor.module.LibraryIndex

/**
 * Created by benny.
 */
interface IndexLoader {

    val annotations: Set<String>

    fun getTypeElement(typeName: String): UniTypeElement?

    fun getIndexes(): List<LibraryIndex>

    fun findAnnotatedElementsByTypeName(enclosingTypeName: String): Collection<Pair<String, UniElement>> {
        val enclosingTypeElement = getTypeElement(enclosingTypeName)!!
        return findAnnotatedElements(enclosingTypeElement)
    }

    fun findAnnotatedElements(
        element: UniElement,
    ): Collection<Pair<String, UniElement>> {
        return element.enclosedElements.filter {
            // TypeElements are already found.
            !it.isClassOrInterface
        }.flatMap {
            findAnnotatedElements(it)
        } + ((element as? UniExecutableElement)?.parameters?.flatMap {
            findAnnotatedElements(it)
        } ?: emptyList()) + annotations.filter { annotation ->
            element.annotations.any { it == annotation }
        }.map {
            it to element
        }
    }

    fun load(): Map<String, List<UniElement>> {
        return getIndexes().flatMap {
            it.value.flatMap { findAnnotatedElementsByTypeName(it) }
        }.fold(HashMap<String, ArrayList<UniElement>>()) { acc, pair ->
            acc.also { map ->
                map.getOrPut(pair.first) { ArrayList() }.add(pair.second)
            }
        }
    }
}