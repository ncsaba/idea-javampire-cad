package com.javampire.cad.impl

import com.intellij.util.Processor
import com.jetbrains.python.PySyntheticType
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.impl.PyBuiltinCache
import com.jetbrains.python.psi.types.PyType
import com.jetbrains.python.psi.types.TypeEvalContext

const val PART_Q_NAME = "javampire.cad.framework.part.Part"
const val ASSEMBLY_Q_NAME = "javampire.cad.framework.assembly.Assembly"

fun isPart(pyClass: PyClass, context: TypeEvalContext? = null): Boolean {
    return pyClass.isSubclass(PART_Q_NAME, context)
            && (! classEquals(pyClass, PART_Q_NAME))
            && (! classEquals(pyClass, ASSEMBLY_Q_NAME))
}

fun visitSuperClasses(
    pyClass: PyClass,
    context: TypeEvalContext? = null, seenClasses: MutableSet<PyClass>? = null,
    processor: Processor<PyClass>
) {
    val mySeenClasses = seenClasses ?: mutableSetOf()
    if (mySeenClasses.contains(pyClass)) return
    mySeenClasses.add(pyClass)
    processor.process(pyClass)
    if (classEquals(pyClass, PART_Q_NAME) or classEquals(pyClass, ASSEMBLY_Q_NAME)) {
        return
    }
    for (superClass in pyClass.getSuperClasses(context)) {
        visitSuperClasses(superClass, context, mySeenClasses, processor)
    }
}

internal fun isAssembly(pyClass: PyClass, context: TypeEvalContext?): Boolean {
    return pyClass.isSubclass(ASSEMBLY_Q_NAME, context)
            && (! classEquals(pyClass, ASSEMBLY_Q_NAME))
}

fun classEquals(pyClass: PyClass, classQName: String): Boolean {
    return pyClass.qualifiedName == classQName
}

fun getConfigType(expression: PyExpression?, typeEvalContext: TypeEvalContext): PyType? {
    val builtinCache = PyBuiltinCache.getInstance(expression)
    when (expression) {
        is PyNumericLiteralExpression -> return builtinCache.getClass("float")?.let { typeEvalContext.getType(it) }
        is PyStringLiteralExpression -> return builtinCache.getClass("str")?.let { typeEvalContext.getType(it) }
        is PyDictLiteralExpression, is PyCallExpression -> return builtinCache.getClass("dict")?.let {
            typeEvalContext.getType(it)
        }
        is PyListLiteralExpression -> return builtinCache.getClass("list")?.let { typeEvalContext.getType(it) }
        is PyBoolLiteralExpression -> return builtinCache.getClass("bool")?.let { typeEvalContext.getType(it) }
    }
    return PySyntheticType(expression?.javaClass?.simpleName ?: "Unknown", expression)
}
