package com.javampire.cad

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.javampire.cad.psi.PartConfigMemberElement
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyExpression
import com.jetbrains.python.psi.types.PyType

class PartConfigMemberInfo internal constructor(
    val definitionClass: PyClass,
    val definitionEntry: PyExpression,
    val name: String,
    val memberType : PyType?,
    val configValue: PyExpression?
) {
    fun fillLookupElementsList(list: MutableList<LookupElement?>) {
        var propertyLookupElement: LookupElementBuilder =
            LookupElementBuilder.create(name)
                .withIcon(icons.PythonPsiApiIcons.PropertySetter)
        if (memberType != null) {
            propertyLookupElement = propertyLookupElement.withTypeText(memberType.name)
        }
        list.add(propertyLookupElement)
    }

    fun fillPsiElementMap(map: MutableMap<String, PsiElement>) {
        map.putIfAbsent(name, PartConfigMemberElement(this))
    }

    override fun toString(): String {
        return "PartConfigMemberInfo: '$name', definitionClass: $definitionClass, definitionEntry: $definitionEntry, memberType: $memberType, configValue: $configValue"
    }
}
