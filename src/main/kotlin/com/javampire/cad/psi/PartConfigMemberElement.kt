package com.javampire.cad.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.javampire.cad.PartConfigMemberInfo
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyPossibleClassMember
import com.jetbrains.python.psi.PyTypedElement
import com.jetbrains.python.psi.types.PyType
import com.jetbrains.python.psi.types.TypeEvalContext
import javax.swing.Icon

class PartConfigMemberElement(propertyMember: PartConfigMemberInfo) :
    ASTWrapperPsiElement(propertyMember.definitionEntry.node), PyTypedElement, PyPossibleClassMember {
    private val myPropertyMember: PartConfigMemberInfo = propertyMember

    override fun getContainingClass(): PyClass = myPropertyMember.definitionClass

    override fun getType(typeEvalContext: TypeEvalContext, key: TypeEvalContext.Key): PyType? {
        return myPropertyMember.memberType
    }

    override fun getIcon(flags: Int): Icon {
        return icons.PythonPsiApiIcons.PropertySetter
    }
}
