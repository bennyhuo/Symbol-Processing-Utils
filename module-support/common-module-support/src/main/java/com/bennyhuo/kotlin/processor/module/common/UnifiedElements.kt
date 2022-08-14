package com.bennyhuo.kotlin.processor.module.common

/**
 * Created by benny.
 */
interface UniElement {
    val enclosedElements: List<UniElement>

    val isClassOrInterface: Boolean

    val isExecutable: Boolean

    val annotations: List<String>

    val enclosingTypeName: String

    /**
     * APT: Element
     * KSP: KSAnnotated
     * X: XElement
     */
    val rawElement: Any

    fun <T> unwrap() = rawElement as T
}

interface UniTypeElement : UniElement {

    val qualifiedName: String

}

interface UniExecutableElement : UniElement {
    val parameters: List<UniElement>
}