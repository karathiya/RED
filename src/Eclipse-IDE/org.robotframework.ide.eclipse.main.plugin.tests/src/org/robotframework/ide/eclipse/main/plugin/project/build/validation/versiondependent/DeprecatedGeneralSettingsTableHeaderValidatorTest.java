/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.SuiteFileProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.MockReporter.Problem;

import com.google.common.collect.Range;

public class DeprecatedGeneralSettingsTableHeaderValidatorTest {

    @Test
    public void outdatedTableHeaderIsReported() throws CoreException {
        final RobotVersion version = new RobotVersion(3, 0);
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().setVersion(version)
                .appendLine("*** Metadata ***")
                .appendLine("Library  Collections")
                .build();

        final Collection<Problem> problems = validate(fileModel);
        assertThat(problems).containsExactly(
                new Problem(SuiteFileProblem.DEPRECATED_TABLE_HEADER, new ProblemPosition(1, Range.closed(0, 16))));
    }

    private Collection<Problem> validate(final RobotSuiteFile fileModel) throws CoreException {
        final MockReporter reporter = new MockReporter();
        final Optional<RobotSettingsSection> section = fileModel.findSection(RobotSettingsSection.class);
        final SettingTable table = section.get().getLinkedElement();
        new DeprecatedGeneralSettingsTableHeaderValidator(fileModel.getFile(), table, reporter)
                .validate(new NullProgressMonitor());
        return reporter.getReportedProblems();
    }
}
