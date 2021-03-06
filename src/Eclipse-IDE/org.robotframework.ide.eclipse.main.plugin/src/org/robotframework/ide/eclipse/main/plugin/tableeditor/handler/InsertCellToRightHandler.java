/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.InsertCellCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.KeywordCallsTableValuesChangingCommandsCollector;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.InsertCellToRightHandler.E4InsertCellToRightHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class InsertCellToRightHandler extends DIParameterizedHandler<E4InsertCellToRightHandler> {

    public InsertCellToRightHandler() {
        super(E4InsertCellToRightHandler.class);
    }

    public static class E4InsertCellToRightHandler {

        @Execute
        public void insertCellToRight(final RobotEditorCommandsStack commandsStack,
                @Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection) {

            final RobotKeywordCall call = (RobotKeywordCall) selection.getFirstElement();
            final int column = editor.getSelectionLayerAccessor().getSelectedPositions()[0].getColumnPosition();
            
            if (column < call.getLinkedElement().getElementTokens().size() - 1) {
                if (call instanceof RobotSetting) {
                    commandsStack.execute(new InsertCellCommand((RobotSetting) call, column + 1));
                } else {
                    new KeywordCallsTableValuesChangingCommandsCollector().collectForInsertion(call, column + 1)
                            .ifPresent(commandsStack::execute);
                }
            }
        }
    }
}
