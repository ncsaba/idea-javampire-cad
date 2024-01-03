package com.javampire.cad.impl

import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.javampire.cad.PartConfigInfo
import com.javampire.cad.PartConfigInfoReader
import com.jetbrains.python.codeInsight.PyCustomMember
import com.jetbrains.python.psi.resolve.PyResolveContext
import com.jetbrains.python.psi.types.PyClassMembersProviderBase
import com.jetbrains.python.psi.types.PyClassType
import com.jetbrains.python.psi.types.PyClassTypeImpl
import com.jetbrains.python.psi.types.TypeEvalContext
import java.util.stream.Collectors

class PartMembersProvider : PyClassMembersProviderBase() {

    override fun getMembers(
        clazz: PyClassType,
        location: PsiElement?,
        typeEvalContext: TypeEvalContext
    ): Collection<PyCustomMember> {
        LOG.warn("getMembers: $clazz, $location, $typeEvalContext")
        if (isGettingCompletionVariants()) {
            LOG.warn("getMembers: is getting completion variants")
            return emptyList()
        } else {
            val typeInfo: PartConfigInfo = PartConfigInfoReader(clazz.pyClass).read()
            return typeInfo.getResolveMap().entries.stream()
                .map { e: Map.Entry<String, PsiElement> ->
                    computeMember(e.key, e.value, clazz)
                }
                .collect(Collectors.toList())
        }
    }

    override fun resolveMember(
        type: PyClassType,
        name: String,
        location: PsiElement?,
        resolveContext: PyResolveContext
    ): PsiElement? {
        LOG.warn("resolveMember: $type, $name, $location, $resolveContext")
        val typeInfo: PartConfigInfo = PartConfigInfoReader(type.pyClass).read()
        return typeInfo.getResolveMap()[name]
    }

    private fun isGettingCompletionVariants(): Boolean {
        // HACK: Inspect stack trace to return empty collection if doing code completion.
        val stackTrace = Throwable().stackTrace
        for (i in 2 until stackTrace.size) {
            val current = stackTrace[i]
            if (PyClassTypeImpl::class.java.getName() != current.className) {
                return false
            } else if ("getCompletionVariants" == current.methodName) {
                return true
            }
        }
        return false
    }

    private fun computeMember(name: String, element: PsiElement, clazz: PyClassType): PyCustomMember {
        return PyCustomMember(name, clazz.name) { clazz }
    }
    companion object {
        private val LOG = Logger.getInstance(PartMembersProvider::class.java.name)
    }
}
