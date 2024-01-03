package com.javampire.cad.impl

import com.intellij.openapi.diagnostic.Logger
import com.javampire.cad.PartConfigInfo
import com.javampire.cad.PartConfigInfoReader
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.types.*
import java.util.*

class PartTypeProvider : PyTypeProviderBase() {
    override fun getCallableType(callable: PyCallable, context: TypeEvalContext): PyType? {
        LOG.warn("getCallableType: $callable, $context")
        if (callable !is PyPossibleClassMember) {
            return null
        }
        val pyClass: PyClass = (callable as PyPossibleClassMember).containingClass ?: return null
        if (! isPart(pyClass)) return null

        if (callable == pyClass.findInitOrNew(false, null)) {
            val info: PartConfigInfo = PartConfigInfoReader(pyClass).read()
            LOG.warn("part config info: $info")
            val originalParameters: Array<PyParameter> = callable.parameterList.parameters
            val configParameters: List<PyNamedParameter> = info.getConfigInitParameters()

            // Get the index of the **kwargs arguments, so that config parameters
            // can be inserted before it.
            var originalKeywordContainerIndex = originalParameters.size
            for (i in originalParameters.indices) {
                val namedParameter: PyNamedParameter? = originalParameters[i].asNamed
                if (namedParameter != null && namedParameter.isKeywordContainer) {
                    originalKeywordContainerIndex = i
                    break
                }
            }

            // Collect argument names in order to avoid producing duplicates.
            // Config parameters lose to any parameter named in the original __init__.
            // This does not prevent duplicates if the original definition contains them, but that
            // does not really matter as the IDE will complain loudly in that case anyway.
            val unavailableParameterNames = HashSet<String?>(originalParameters.size)
            for (originalParameter in originalParameters) {
                unavailableParameterNames.add(originalParameter.name)
            }

            val allInitParameters: ArrayList<PyCallableParameter> =
                ArrayList<PyCallableParameter>(originalParameters.size + configParameters.size)

            for (i in 0 until originalKeywordContainerIndex) {
                allInitParameters.add(PyCallableParameterImpl.psi(originalParameters[i]))
            }

            for (syntheticParameter in configParameters) {
                if (unavailableParameterNames.add(syntheticParameter.name)) {
                    allInitParameters.add(PyCallableParameterImpl.psi(syntheticParameter))
                }
            }

            for (i in originalKeywordContainerIndex until originalParameters.size) {
                allInitParameters.add(PyCallableParameterImpl.psi(originalParameters[i]))
            }
            return PyFunctionTypeImpl(
                callable, Collections.unmodifiableList(allInitParameters)
            )
        }

        return null
    }
    companion object {
        private val LOG = Logger.getInstance(PartTypeProvider::class.java.name)
    }
}
