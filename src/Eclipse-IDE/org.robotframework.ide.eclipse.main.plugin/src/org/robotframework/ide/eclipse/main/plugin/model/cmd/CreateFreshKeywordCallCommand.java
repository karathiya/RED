/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;


public class CreateFreshKeywordCallCommand extends EditorCommand {

    private final RobotCodeHoldingElement<?> parent;

    private final String name;

    private final List<String> args;

    private final String comment;

    private final int index;

    public CreateFreshKeywordCallCommand(final RobotCodeHoldingElement<?> parent) {
        this(parent, parent.getChildren().size());
    }

    public CreateFreshKeywordCallCommand(final RobotCodeHoldingElement<?> parent, final int index) {
        this(parent, index, "", new ArrayList<>(), "");
    }

    public CreateFreshKeywordCallCommand(final RobotCodeHoldingElement<?> parent, final int index,
            final String name, final List<String> args, final String comment) {
        this.parent = parent;
        this.name = name;
        this.args = args;
        this.comment = comment;
        this.index = index;
    }

    @Override
    public void execute() throws CommandExecutionException {
        RobotKeywordCall newCall;
        if (isEmptyContent(name, args, comment)) {
            newCall = parent.createEmpty(index, newArrayList(name));
        } else {
            final List<String> tokens = new ArrayList<>();
            tokens.add(name);
            tokens.addAll(args);
            tokens.add(comment);
            newCall = parent.createKeywordCall(index, tokens);
        }
        RedEventBroker.using(eventBroker)
                .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA)
                .to(newCall)
                .send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, parent);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        final RobotKeywordCall call = parent.getChildren().get(index);
        return newUndoCommands(new DeleteKeywordCallCommand(newArrayList(call)));
    }

    private static boolean isEmptyContent(final String name, final List<String> args, final String comment) {
        return args.isEmpty() && comment.isEmpty() && Pattern.matches("^[\\s]*$", name);
    }
}
