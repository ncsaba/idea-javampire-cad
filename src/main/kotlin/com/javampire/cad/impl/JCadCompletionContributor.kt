package com.javampire.cad.impl

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import com.javampire.cad.PartConfigInfo
import com.javampire.cad.PartConfigInfoReader
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.codeInsight.completion.getTypeEvalContext
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.PyTypedElement

class JCadCompletionContributor : CompletionContributor() {
    init {
        LOG.warn("Completion init called")
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(PyTokenTypes.IDENTIFIER)
                .afterLeaf(PlatformPatterns.psiElement(PyTokenTypes.DOT))
                .withParent(PlatformPatterns.psiElement(PyReferenceExpression::class.java)),
            FieldCompletionProvider
        )
    }
    companion object {
        private val LOG = Logger.getInstance(JCadCompletionContributor::class.java.name)
    }

    private object FieldCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet
        ) {
            val element = parameters.position
            val typeEvalContext = parameters.getTypeEvalContext()
            val pyType = (element.parent?.firstChild as? PyTypedElement)?.let { typeEvalContext.getType(it) }
                ?: return
            val pyClass = (pyType.declarationElement as? PyClass) ?: return
            if (isPart(pyClass, typeEvalContext)) {
                val typeInfo: PartConfigInfo = PartConfigInfoReader(pyClass).read()
                typeInfo.getLookupElements().forEach {
                    LOG.warn("Adding lookup element: $it")
                    result.addElement(PrioritizedLookupElement.withPriority(it, 10000.0))
                }
            }
        }
    }
}