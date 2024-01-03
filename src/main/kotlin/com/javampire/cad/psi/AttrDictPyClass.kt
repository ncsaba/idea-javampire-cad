package com.javampire.cad.psi

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.javampire.cad.PartConfigMemberInfo
import com.javampire.cad.impl.getConfigType
import com.jetbrains.python.psi.AccessDirection
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyDictLiteralExpression
import com.jetbrains.python.psi.PyExpression
import com.jetbrains.python.psi.impl.ResolveResultList
import com.jetbrains.python.psi.resolve.PyResolveContext
import com.jetbrains.python.psi.resolve.RatedResolveResult
import com.jetbrains.python.psi.types.PyType
import com.jetbrains.python.psi.types.TypeEvalContext


// extends
class AttrDictPyClass(
    val containingClass: PyClass,
    val configValue: PyExpression,
    val configItems: MutableList<PartConfigMemberInfo> = mutableListOf()
) : PyType {

    constructor(
        containingClass: PyClass, configValue: PyExpression, context: TypeEvalContext
    ) : this(
        containingClass, configValue
    ) {
        // configValue is one of PyDictLiteralExpression or PyCallExpression
        when (configValue) {
            is PyDictLiteralExpression -> {
                // iterate over the PyDictLiteralExpression entries
                configValue.elements.forEach { element ->
                    val key = element.key
                    val value = element.value
                    val elementType = getConfigType(containingClass, value, context)
                    LOG.warn("AttrDictPyClass: key: $key, value: $value, elementType: $elementType")
                    val memberInfo = PartConfigMemberInfo(
                        containingClass, element,
                        key.text.trim('"', '\''),
                        elementType, value
                    )
                    configItems.add(memberInfo)
                }
            }
        }
    }

    override fun resolveMember(
        name: String,
        location: PyExpression?,
        direction: AccessDirection,
        context: PyResolveContext
    ): MutableList<out RatedResolveResult> {
        val result = ResolveResultList()
        configItems.forEach { member ->
            if (member.name == name) {
                result.poke(member.definitionEntry, RatedResolveResult.RATE_HIGH)
            }
        }
        return result
    }

    override fun getCompletionVariants(
        completionPrefix: String?, location: PsiElement?, context: ProcessingContext?
    ): Array<out LookupElement> {
        val result = ArrayList<LookupElement>()
        configItems.forEach { member ->
            result.add(LookupElementBuilder.create(member.name).withTypeText(member.memberType?.name))
        }
        return result.toTypedArray()
    }

    override fun getName(): String {
        return "AttrDict"
    }

    override fun isBuiltin(): Boolean {
        return false
    }

    override fun assertValid(message: String?) {
    }

    companion object {
        private val LOG = Logger.getInstance(AttrDictPyClass::class.java.name)
    }
}