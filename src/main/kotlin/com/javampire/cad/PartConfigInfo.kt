package com.javampire.cad

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.PsiElement
import com.javampire.cad.psi.ConfigInitParameter
import com.jetbrains.python.psi.PyNamedParameter
import java.util.*

class PartConfigInfo internal constructor(
    val members: Collection<PartConfigMemberInfo>
) {
    private var myLookupElements: List<LookupElement>? = null
    private var myResolveMap: Map<String, PsiElement>? = null
    private var myConfigInitParameters: List<PyNamedParameter>? = null

    fun getLookupElements(): List<LookupElement> {
        if (myLookupElements == null) {
            if (members.isEmpty()) {
                myLookupElements = emptyList()
                return myLookupElements!!
            }

            val lookupElements: MutableList<LookupElement?> = ArrayList<LookupElement?>(members.size * 2)

            for (memberInfo in members) {
                memberInfo.fillLookupElementsList(lookupElements)
            }

            myLookupElements = Collections.unmodifiableList<LookupElement>(lookupElements)
        }

        return myLookupElements!!
    }

    fun getResolveMap(): Map<String, PsiElement> {
        if (myResolveMap == null) {
            if (members.isEmpty()) {
                myResolveMap = emptyMap()
                return myResolveMap!!
            }

            val resolveMap: MutableMap<String, PsiElement> = HashMap(
                members.size * 2
            )

            for (memberInfo in members) {
                memberInfo.fillPsiElementMap(resolveMap)
            }

            myResolveMap = Collections.unmodifiableMap(resolveMap)
        }

        return myResolveMap!!
    }

    fun getConfigInitParameters(): List<PyNamedParameter> {
        if (myConfigInitParameters == null) {
            val parameters: ArrayList<PyNamedParameter> = ArrayList<PyNamedParameter>(members.size)

            for (memberInfo in members) {
                parameters.add(ConfigInitParameter(memberInfo))
            }

            myConfigInitParameters = Collections.unmodifiableList(parameters)
        }

        return myConfigInitParameters!!
    }

    override fun toString(): String {
        return "PartConfigInfo: members: $members"
    }
}
