package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.cmd.MoveKeywordCallDownCommand;
import org.robotframework.ide.eclipse.main.plugin.cmd.MoveKeywordDefinitionDownCommand;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.MoveKeywordsDownHandler.E4MoveKeywordDownHandler;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Optional;

public class MoveKeywordsDownHandler extends DIHandler<E4MoveKeywordDownHandler> {

    public MoveKeywordsDownHandler() {
        super(E4MoveKeywordDownHandler.class);
    }

    public static class E4MoveKeywordDownHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public Object moveDown(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            final Optional<RobotKeywordCall> maybeKeywordCall = Selections.getOptionalFirstElement(selection,
                    RobotKeywordCall.class);
            final Optional<RobotKeywordDefinition> maybeKeywordDef = Selections.getOptionalFirstElement(selection,
                    RobotKeywordDefinition.class);

            if (maybeKeywordCall.isPresent()) {
                commandsStack.execute(new MoveKeywordCallDownCommand(maybeKeywordCall.get()));
            } else if (maybeKeywordDef.isPresent()) {
                commandsStack.execute(new MoveKeywordDefinitionDownCommand(maybeKeywordDef.get()));
            }
            return null;
        }
    }
}
