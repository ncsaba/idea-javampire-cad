package com.javampire.cad.psi

import com.intellij.psi.PsiElement
import com.javampire.cad.PartConfigMemberInfo
import com.jetbrains.python.PyElementTypes
import com.jetbrains.python.psi.PyExpression
import com.jetbrains.python.psi.impl.PyNamedParameterImpl
import com.jetbrains.python.psi.impl.stubs.PyNamedParameterStubImpl
import com.jetbrains.python.psi.stubs.PyNamedParameterStub
import com.jetbrains.python.psi.types.PyType
import com.jetbrains.python.psi.types.TypeEvalContext

class ConfigInitParameter(private val myMemberInfo: PartConfigMemberInfo) : PyNamedParameterImpl(
    createVirtualStub(
        myMemberInfo
    )
) {
    override fun getType(context: TypeEvalContext, key: TypeEvalContext.Key): PyType? {
        return myMemberInfo.memberType
    }

    override fun getDefaultValue(): PyExpression? {
        return myMemberInfo.configValue
    }

    override fun getParent(): PsiElement? {
        return null
    }

    companion object {
        private fun createVirtualStub(partConfigMemberInfo: PartConfigMemberInfo): PyNamedParameterStub {
            return PyNamedParameterStubImpl(
                partConfigMemberInfo.name,
                false,
                false,
                partConfigMemberInfo.configValue?.text,
                null,
                null,
                null,
                PyElementTypes.NAMED_PARAMETER
            )
        }
    }
}
