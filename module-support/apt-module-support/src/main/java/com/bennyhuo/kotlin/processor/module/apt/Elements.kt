package com.bennyhuo.kotlin.processor.module.apt

import javax.lang.model.element.Element
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement

/**
 * Created by benny.
 */
internal fun Element.getEnclosingType(): TypeElement {
    return when(this) {
        is TypeElement -> this
        is PackageElement -> throw IllegalArgumentException()
        else -> enclosingElement.getEnclosingType()
    }
}