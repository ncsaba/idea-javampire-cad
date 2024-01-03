package com.javampire.cad.impl

import com.intellij.util.Processor
import com.javampire.cad.psi.AttrDictPyClass
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

fun classEquals(pyClass: PyClass, classQName: String): Boolean {
    return pyClass.qualifiedName == classQName
}

fun getConfigType(containingClass: PyClass, expression: PyExpression?, typeEvalContext: TypeEvalContext): PyType? {
    val builtinCache = PyBuiltinCache.getInstance(expression)
    when (expression) {
        is PyNumericLiteralExpression -> return builtinCache.getClass("float")?.let { typeEvalContext.getType(it) }
        is PyStringLiteralExpression -> return builtinCache.getClass("str")?.let { typeEvalContext.getType(it) }
        is PyDictLiteralExpression, is PyCallExpression -> return AttrDictPyClass(containingClass, expression, typeEvalContext)
        is PyListLiteralExpression -> return builtinCache.getClass("list")?.let { typeEvalContext.getType(it) }
        is PyBoolLiteralExpression -> return builtinCache.getClass("bool")?.let { typeEvalContext.getType(it) }
    }
    return PySyntheticType(expression?.javaClass?.simpleName ?: "Unknown", expression)
}
