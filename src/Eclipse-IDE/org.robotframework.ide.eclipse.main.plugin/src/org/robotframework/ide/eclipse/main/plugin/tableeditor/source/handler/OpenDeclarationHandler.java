/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

import java.util.Optional;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditorConfiguration;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.OpenDeclarationHandler.E4OpenDeclarationHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

/**
 * @author Michal Anglart
 */
public class OpenDeclarationHandler extends DIParameterizedHandler<E4OpenDeclarationHandler> {

    public OpenDeclarationHandler() {
        super(E4OpenDeclarationHandler.class);
    }

    public static class E4OpenDeclarationHandler {

        @Execute
        public void openDeclaration(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor) {
            final SourceViewer viewer = editor.getSourceEditor().getViewer();
            final IRegion region = new Region(viewer.getTextWidget().getCaretOffset(), 0);
            final SuiteSourceEditorConfiguration configuration = editor.getSourceEditor().getViewerConfiguration();
            final IHyperlinkDetector[] detectors = configuration.getHyperlinkDetectors(() -> true);

            getHyperlink(viewer, region, detectors).ifPresent(IHyperlink::open);
        }
    }

    private static Optional<IHyperlink> getHyperlink(final ITextViewer viewer, final IRegion hyperlinkRegion,
            final IHyperlinkDetector... detectors) {
        for (final IHyperlinkDetector detector : detectors) {
            final Optional<IHyperlink> hyperlink = detect(viewer, hyperlinkRegion, detector);
            if (hyperlink.isPresent()) {
                return hyperlink;
            }
        }
        return Optional.empty();
    }

    private static Optional<IHyperlink> detect(final ITextViewer viewer, final IRegion hyperlinkRegion,
            final IHyperlinkDetector detector) {
        final IHyperlink[] hyperlinks = detector.detectHyperlinks(viewer, hyperlinkRegion, false);
        if (hyperlinks != null && hyperlinks.length > 0) {
            return Optional.of(hyperlinks[0]);
        }
        return Optional.empty();
    }
}
