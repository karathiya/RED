/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.rf.ide.core.execution.server.response.EvaluateExpression.ExpressionType;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.AssistantContext;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.KeywordCallsAssistProcessor;


class KeywordCallsInShellAssistProcessor extends KeywordCallsAssistProcessor {

    KeywordCallsInShellAssistProcessor(final AssistantContext assist) {
        super(assist);
    }

    @Override
    public List<String> getApplicableContentTypes() {
        return newArrayList(IDocument.DEFAULT_CONTENT_TYPE);
    }

    @Override
    protected boolean shouldShowProposals(final IDocument document, final int offset, final String lineContent)
            throws BadLocationException {
        final ShellDocument shellDocument = (ShellDocument) document;
        return shellDocument.getMode() == ExpressionType.ROBOT
                && shellDocument.isInEditEnabledRegion(offset) && shellDocument.isExpressionPromptLine(offset)
                && DocumentUtilities.getNumberOfCellSeparators(lineContent, assist.isTsvFile()) == 0;
    }

    @Override
    protected List<? extends ICompletionProposal> computeProposals(final IDocument document, final int offset,
            final int cellLength, final String userContent, final boolean atTheEndOfLine) throws BadLocationException {
        if (assist.getModel() == null) {
            return null;
        }
        final ShellDocument shellDocument = (ShellDocument) document;

        // user content contains the prompt prefix which needs to be removed for further processing
        final int prefixLength = shellDocument.getPromptLenght(offset).get().intValue();
        return super.computeProposals(document, offset, cellLength - prefixLength, userContent.substring(prefixLength),
                atTheEndOfLine);
    }

}
