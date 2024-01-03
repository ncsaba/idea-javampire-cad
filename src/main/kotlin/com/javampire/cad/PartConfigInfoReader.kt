package com.javampire.cad

import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.javampire.cad.impl.getConfigType
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.types.TypeEvalContext
import java.util.*

class PartConfigInfoReader(
    private val myPyClass: PyClass
) : CachedValueProvider<PartConfigInfo>
{
    fun read(): PartConfigInfo {
        return CachedValuesManager.getCachedValue(myPyClass, this)
    }

    override fun compute(): CachedValueProvider.Result<PartConfigInfo> {
        val typeEvalContext = TypeEvalContext.codeCompletion(myPyClass.project, myPyClass.containingFile)
        val configDescriptor = myPyClass.findClassAttribute(
            "CONFIG_DESCRIPTOR", false,
            typeEvalContext
        )

        val partConfigMemberInfoList = ArrayList<PartConfigMemberInfo>()
        val seenNames = HashSet<String>()

        val assignedValue = configDescriptor?.findAssignedValue()
        val createInfo: (String?, PyExpression?, PyExpression) -> Unit = { name, value, entry ->
            LOG.warn("Name: name, Value: $value")
            if (name != null && value != null) {
                val memberInfo = PartConfigMemberInfo(
                    myPyClass, entry, name,
                    getConfigType(value, typeEvalContext),
                    value
                )
                LOG.warn("Found member info: $memberInfo")
                if (seenNames.add(memberInfo.name)) {
                    partConfigMemberInfoList.add(memberInfo)
                }
            }
        }
        if (assignedValue is PyCallExpression) {
            // dict instantiation handling
            LOG.warn("Call expression found: assignedValue: $assignedValue")
            assignedValue.argumentList?.children?.forEach { entry ->
                if (entry is PyKeywordArgument) {
                    createInfo(entry.name, entry.valueExpression, entry)
                }
            }
        } else if (assignedValue is PyDictLiteralExpression) {
            // literal dict handling
            assignedValue.children.forEach { entry ->
                if (entry is PyKeyValueExpression) {
                    createInfo(entry.key.text.trim('"', '\''), entry.value, entry)
                }
            }
        }
        // need to look up all super classes, and merge the hierarchy
        // - pycharm does that too, but not well, so we do it here
        myPyClass.getSuperClasses(typeEvalContext).forEach {superClass ->
            val typeInfo: PartConfigInfo = PartConfigInfoReader(superClass).read()
            typeInfo.members.forEach {configInfo ->
                if (seenNames.add(configInfo.name)) {
                    partConfigMemberInfoList.add(configInfo)
                }
            }
        }
        // TODO: add perhaps some default ones, which are not declared anywhere, like axis and alignment ?
        return cacheResult(PartConfigInfo(Collections.unmodifiableList(partConfigMemberInfoList)))
    }

    private fun cacheResult(partConfigInfo: PartConfigInfo): CachedValueProvider.Result<PartConfigInfo> {
        // The dependency argument means that the cache value will be invalidated when the file
        // containing myPyClass is changed.
        return CachedValueProvider.Result.createSingleDependency(partConfigInfo, myPyClass)
    }

    companion object {
        private val LOG = Logger.getInstance(PartConfigInfoReader::class.java.name)
    }
}
