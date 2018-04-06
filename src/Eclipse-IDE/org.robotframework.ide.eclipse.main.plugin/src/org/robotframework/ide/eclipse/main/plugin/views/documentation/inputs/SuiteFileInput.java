/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import static com.google.common.collect.Lists.newArrayList;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.libraries.Documentation;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.WorkspaceFileUri;

import com.google.common.io.Files;

public class SuiteFileInput extends InternalElementInput<RobotSuiteFile> {

    public SuiteFileInput(final RobotSuiteFile suiteFile) {
        super(suiteFile);
    }

    @Override
    public URI getInputUri() throws URISyntaxException {
        return WorkspaceFileUri.createShowSuiteDocUri(element.getSuiteFile().getFile());
    }

    @Override
    protected String createHeader() {
        return suiteHeader(element);
    }

    private static String suiteHeader(final RobotSuiteFile suiteFile) {
        final Optional<URI> imgUri = RedImages.getRobotFileImageUri();

        final IFile file = suiteFile.getFile();
        final URI srcHref = WorkspaceFileUri.createFileUri(file);
        final String srcLabel = file.getFullPath().toString();

        final String source = Formatters.formatHyperlink(srcHref, srcLabel);
        return Formatters.formatSimpleHeader(imgUri, suiteFile.getName(),
                newArrayList("Source", source));
    }

    @Override
    protected Documentation createDocumentation() {
        return element.createDocumentation();
    }

    @Override
    public String provideRawText() throws DocumentationInputGenerationException {
        return provideRawText(element);
    }

    private static String provideRawText(final RobotSuiteFile suiteFile) {
        final StringBuilder builder = new StringBuilder();
        builder.append("Name: ").append(Files.getNameWithoutExtension(suiteFile.getName())).append("\n");
        builder.append("Source: ").append(suiteFile.getFile().getFullPath().toString()).append("\n");
        builder.append(suiteFile.getDocumentation());
        return builder.toString();
    }

    public static class SuiteFileOnSettingInput extends InternalElementInput<RobotSetting> {

        public SuiteFileOnSettingInput(final RobotSetting docSetting) {
            super(docSetting);
        }

        @Override
        public URI getInputUri() throws URISyntaxException {
            return WorkspaceFileUri.createShowSuiteDocUri(element.getSuiteFile().getFile());
        }

        @Override
        protected String createHeader() {
            return SuiteFileInput.suiteHeader(element.getSuiteFile());
        }

        @Override
        protected Documentation createDocumentation() {
            return element.getSuiteFile().createDocumentation();
        }

        @Override
        public String provideRawText() throws DocumentationInputGenerationException {
            return SuiteFileInput.provideRawText(element.getSuiteFile());
        }
    }
}