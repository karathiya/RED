/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.rf.ide.core.environment.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.red.junit.PreferenceUpdater;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.RunConfigurationProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

public class RobotLaunchConfigurationTest {

    private static final String PROJECT_NAME = RobotLaunchConfigurationTest.class.getSimpleName();

    @Rule
    public ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Rule
    public RunConfigurationProvider runConfigurationProvider = new RunConfigurationProvider(
            RobotLaunchConfiguration.TYPE_ID);

    @Rule
    public PreferenceUpdater preferenceUpdater = new PreferenceUpdater();

    @Test
    public void nullVariablesArrayIsReturned_whenThereAreNoVariablesDefined() throws Exception {
        // this mean that the process will inherit environment variables from parent process

        final ILaunchConfiguration config = mock(ILaunchConfiguration.class);
        when(config.getAttribute(eq(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES),
                ArgumentMatchers.<Map<String, String>> isNull())).thenReturn(null);

        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);

        assertThat(robotConfig.getEnvironmentVariables()).isNull();
    }

    @Test
    public void onlyVariablesFromConfigAreReturned_whenTheyAreDefinedAndOverrideIsEnabled() throws Exception {
        final Map<String, String> vars = ImmutableMap.of("VAR1", "x", "VAR2", "y", "VAR3", "z");

        final ILaunchConfiguration config = mock(ILaunchConfiguration.class);
        when(config.getAttribute(eq(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES),
                ArgumentMatchers.<Map<String, String>> isNull())).thenReturn(vars);
        when(config.getAttribute(eq(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES), anyBoolean())).thenReturn(false);

        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);

        assertThat(robotConfig.getEnvironmentVariables()).containsExactly("VAR1=x", "VAR2=y", "VAR3=z");
    }

    @Test
    public void inheritedVariablesFromCurrentProcessAndConfigVariablesAreReturned_whenTheyAreDefinedAndAppendingIsEnabled()
            throws Exception {
        final Map<String, String> vars = ImmutableMap.of("VAR1", "x", "VAR2", "y", "VAR3", "z");

        final ILaunchConfiguration config = mock(ILaunchConfiguration.class);
        when(config.getAttribute(eq(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES),
                ArgumentMatchers.<Map<String, String>> isNull())).thenReturn(vars);
        when(config.getAttribute(eq(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES), anyBoolean())).thenReturn(true);

        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);

        final String[] envVars = robotConfig.getEnvironmentVariables();
        assertThat(envVars.length).isGreaterThan(3);
        assertThat(envVars).containsSequence("VAR1=x", "VAR2=y", "VAR3=z");
    }

    @Test
    public void defaultConfigurationObtained_whenDefaultConfigurationIsCreated() throws CoreException {
        final ILaunchConfiguration config = RobotLaunchConfiguration
                .prepareDefault(asList(projectProvider.getFile("Resource")));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);

        assertThat(robotConfig.getName()).isEqualTo("Resource");
        assertThat(robotConfig.getProjectName()).isEqualTo(PROJECT_NAME);
        assertThat(robotConfig.getSuitePaths().keySet()).containsExactly("Resource");
        assertThat(robotConfig.getUnselectedSuitePaths()).isEmpty();
        assertThat(robotConfig.getRobotArguments()).isEqualTo("");
        assertThat(robotConfig.isIncludeTagsEnabled()).isFalse();
        assertThat(robotConfig.isExcludeTagsEnabled()).isFalse();
        assertThat(robotConfig.getIncludedTags()).isEmpty();
        assertThat(robotConfig.getExcludedTags()).isEmpty();
        assertThat(robotConfig.getConfigurationVersion())
                .isEqualTo(RobotLaunchConfiguration.CURRENT_CONFIGURATION_VERSION);

        assertThat(robotConfig.isUsingRemoteAgent()).isFalse();
        assertThat(robotConfig.getAgentConnectionHost()).isEqualTo("127.0.0.1");
        assertThat(robotConfig.getAgentConnectionPort()).isBetween(1, 65_535);
        assertThat(robotConfig.getAgentConnectionTimeout()).isEqualTo(30);

        assertThat(robotConfig.isUsingInterpreterFromProject()).isTrue();
        assertThat(robotConfig.getInterpreter()).isEqualTo(SuiteExecutor.Python);
        assertThat(robotConfig.getExecutableFilePath()).isEqualTo("");
        assertThat(robotConfig.getExecutableFileArguments()).isEqualTo("");

        assertThat(robotConfig.getEnvironmentVariables()).contains("PYTHONIOENCODING=utf8");
    }

    @Test
    public void defaultConfigurationObtained_whenCustomConfigurationIsFilledWithDefaults() throws CoreException {
        final ILaunchConfiguration config = RobotLaunchConfiguration
                .prepareDefault(asList(projectProvider.getFile("Resource")));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);

        robotConfig.setRobotArguments("arguments");
        robotConfig.setProjectName(PROJECT_NAME);
        robotConfig.setSuitePaths(ImmutableMap.of("selected", asList("value"), "unselected", asList()));
        robotConfig.setUnselectedSuitePaths(newHashSet("unselected"));
        robotConfig.setIsIncludeTagsEnabled(true);
        robotConfig.setIsExcludeTagsEnabled(true);
        robotConfig.setExcludedTags(asList("excluded"));
        robotConfig.setIncludedTags(asList("included"));

        robotConfig.setUsingRemoteAgent(true);
        robotConfig.setAgentConnectionHostValue("1.2.3.4");
        robotConfig.setAgentConnectionPortValue("987");
        robotConfig.setAgentConnectionTimeoutValue("123");
        robotConfig.setInterpreter(SuiteExecutor.PyPy);
        robotConfig.setInterpreterArguments("-a");
        robotConfig.setExecutableFilePath("path");
        robotConfig.setExecutableFileArguments("-new");

        robotConfig.fillDefaults();

        assertThat(robotConfig.getProjectName()).isEqualTo("");
        assertThat(robotConfig.getSuitePaths()).isEmpty();
        assertThat(robotConfig.getUnselectedSuitePaths()).isEmpty();
        assertThat(robotConfig.getRobotArguments()).isEqualTo("");
        assertThat(robotConfig.isIncludeTagsEnabled()).isFalse();
        assertThat(robotConfig.isExcludeTagsEnabled()).isFalse();
        assertThat(robotConfig.getIncludedTags()).isEmpty();
        assertThat(robotConfig.getExcludedTags()).isEmpty();
        assertThat(robotConfig.getConfigurationVersion())
                .isEqualTo(RobotLaunchConfiguration.CURRENT_CONFIGURATION_VERSION);

        assertThat(robotConfig.isUsingRemoteAgent()).isFalse();
        assertThat(robotConfig.getAgentConnectionHost()).isEqualTo("127.0.0.1");
        assertThat(robotConfig.getAgentConnectionPort()).isBetween(1, 65_535);
        assertThat(robotConfig.getAgentConnectionTimeout()).isEqualTo(30);

        assertThat(robotConfig.isUsingInterpreterFromProject()).isTrue();
        assertThat(robotConfig.getInterpreter()).isEqualTo(SuiteExecutor.Python);
        assertThat(robotConfig.getInterpreterArguments()).isEmpty();
        assertThat(robotConfig.getExecutableFilePath()).isEmpty();
        assertThat(robotConfig.getExecutableFileArguments()).isEmpty();

        assertThat(robotConfig.getEnvironmentVariables()).contains("PYTHONIOENCODING=utf8");
    }

    @Test
    public void onlySelectedTestCasesAreUsed_inConfigurationForSelectedTestCases() throws CoreException {
        final IResource res1 = projectProvider.getFile("Resource1");
        final IResource res2 = projectProvider.getFile("Resource2");
        final List<String> casesForRes1 = asList("case1", "case3");
        final List<String> casesForRes2 = asList("case1");

        final ILaunchConfiguration config = RobotLaunchConfiguration
                .prepareForSelectedTestCases(ImmutableMap.of(res1, casesForRes1, res2, casesForRes2));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);

        assertThat(robotConfig.getProjectName()).isEqualTo(PROJECT_NAME);
        assertThat(robotConfig.getSuitePaths())
                .isEqualTo(ImmutableMap.of("Resource1", casesForRes1, "Resource2", casesForRes2));
        assertThat(robotConfig.getUnselectedSuitePaths()).isEmpty();
        assertThat(robotConfig.getRobotArguments()).isEmpty();
        assertThat(robotConfig.isIncludeTagsEnabled()).isFalse();
        assertThat(robotConfig.isExcludeTagsEnabled()).isFalse();
        assertThat(robotConfig.getIncludedTags()).isEmpty();
        assertThat(robotConfig.getExcludedTags()).isEmpty();
        assertThat(robotConfig.getInterpreter()).isEqualTo(SuiteExecutor.Python);
        assertThat(robotConfig.getInterpreterArguments()).isEmpty();
        assertThat(robotConfig.getExecutableFilePath()).isEmpty();
        assertThat(robotConfig.getExecutableFileArguments()).isEmpty();

        assertThat(robotConfig.getEnvironmentVariables()).contains("PYTHONIOENCODING=utf8");
    }

    @Test
    public void defaultConfigurationObtained_whenDefaultValuesAreDefinedInPreferences() throws Exception {
        preferenceUpdater.setValue(RedPreferences.LAUNCH_ADDITIONAL_INTERPRETER_ARGUMENTS, "-a -b -c");
        preferenceUpdater.setValue(RedPreferences.LAUNCH_ADDITIONAL_ROBOT_ARGUMENTS, "-d -e -f");
        preferenceUpdater.setValue(RedPreferences.LAUNCH_EXECUTABLE_FILE_PATH, "/path/to/script");
        preferenceUpdater.setValue(RedPreferences.LAUNCH_ADDITIONAL_EXECUTABLE_FILE_ARGUMENTS, "-g -h -i");
        preferenceUpdater.setValue(RedPreferences.LAUNCH_ENVIRONMENT_VARIABLES, new ObjectMapper()
                .writeValueAsString(ImmutableMap.of("VAR_1", "some value", "VAR_2", "1234", "EMPTY_VAR", "")));

        final ILaunchConfiguration config = RobotLaunchConfiguration
                .prepareDefault(asList(projectProvider.getFile("Resource")));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);

        assertThat(robotConfig.getName()).isEqualTo("Resource");
        assertThat(robotConfig.getProjectName()).isEqualTo(PROJECT_NAME);
        assertThat(robotConfig.getSuitePaths().keySet()).containsExactly("Resource");
        assertThat(robotConfig.getUnselectedSuitePaths()).isEmpty();
        assertThat(robotConfig.getRobotArguments()).isEqualTo("-d -e -f");
        assertThat(robotConfig.isIncludeTagsEnabled()).isFalse();
        assertThat(robotConfig.isExcludeTagsEnabled()).isFalse();
        assertThat(robotConfig.getIncludedTags()).isEmpty();
        assertThat(robotConfig.getExcludedTags()).isEmpty();
        assertThat(robotConfig.getConfigurationVersion())
                .isEqualTo(RobotLaunchConfiguration.CURRENT_CONFIGURATION_VERSION);

        assertThat(robotConfig.isUsingRemoteAgent()).isFalse();
        assertThat(robotConfig.getAgentConnectionHost()).isEqualTo("127.0.0.1");
        assertThat(robotConfig.getAgentConnectionPort()).isBetween(1, 65_535);
        assertThat(robotConfig.getAgentConnectionTimeout()).isEqualTo(30);

        assertThat(robotConfig.isUsingInterpreterFromProject()).isTrue();
        assertThat(robotConfig.getInterpreter()).isEqualTo(SuiteExecutor.Python);
        assertThat(robotConfig.getInterpreterArguments()).isEqualTo("-a -b -c");
        assertThat(robotConfig.getExecutableFilePath()).isEqualTo("/path/to/script");
        assertThat(robotConfig.getExecutableFileArguments()).isEqualTo("-g -h -i");

        assertThat(robotConfig.getEnvironmentVariables()).contains("VAR_1=some value", "VAR_2=1234", "EMPTY_VAR=");
    }

    @Test
    public void suitesObtained_whenSuitesCollectedFromConfiguration() throws CoreException {
        final List<IResource> resources = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            final IResource res = projectProvider.getFile("Resource " + i + ".fake");
            resources.add(res);
        }
        final ILaunchConfiguration config = RobotLaunchConfiguration.prepareDefault(resources);
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);
        final Map<IResource, List<String>> obtainedSuites = robotConfig.collectSuitesToRun();
        assertThat(obtainedSuites).hasSameSizeAs(resources);
        for (int i = 0; i < resources.size(); i++) {
            assertThat(obtainedSuites).containsKey(resources.get(i));
        }
    }

    @Test
    public void emptySuitesObtained_whenProjectNameIsEmpty() throws CoreException {
        final List<IResource> resources = asList(projectProvider.getFile("Resource 1.fake"));
        final ILaunchConfiguration config = RobotLaunchConfiguration.prepareDefault(resources);
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);
        robotConfig.setProjectName("");
        assertThat(robotConfig.collectSuitesToRun()).isEmpty();
    }

    @Test
    public void selectedSuitesAreReturned() throws CoreException {
        final IResource res1 = projectProvider.getFile("Resource1");
        final IResource res2 = projectProvider.getFile("Resource2");
        final IResource unselected1 = projectProvider.getFile("Unselected1");
        final IResource unselected2 = projectProvider.getFile("Unselected2");

        final ILaunchConfiguration config = RobotLaunchConfiguration.prepareForSelectedTestCases(
                ImmutableMap.of(res1, asList(), res2, asList(), unselected1, asList(), unselected2, asList()));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);

        robotConfig.setUnselectedSuitePaths(newHashSet("Unselected1", "Unselected2"));

        assertThat(robotConfig.getSuitePaths()).isEqualTo(ImmutableMap.of("Resource1", asList(), "Resource2", asList(),
                "Unselected1", asList(), "Unselected2", asList()));
        assertThat(robotConfig.getSelectedSuitePaths())
                .isEqualTo(ImmutableMap.of("Resource1", asList(), "Resource2", asList()));
        assertThat(robotConfig.getUnselectedSuitePaths()).containsOnly("Unselected1", "Unselected2");
    }

    @Test
    public void robotProjectObtainedFromConfiguration_whenProjectInWorkspace() throws CoreException {
        final ILaunchConfiguration config = RobotLaunchConfiguration
                .prepareDefault(asList(projectProvider.getFile("Resource")));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);

        assertThat(robotConfig.getProject()).isEqualTo(projectProvider.getProject());
    }

    @Test
    public void whenProjectNotInWorkspace_coreExceptionIsThrown() throws CoreException {
        final ILaunchConfiguration config = RobotLaunchConfiguration
                .prepareDefault(asList(projectProvider.getFile("Resource")));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);

        robotConfig.setProjectName("not_existing");

        assertThatExceptionOfType(CoreException.class).isThrownBy(robotConfig::getProject)
                .withMessage("Project 'not_existing' cannot be found in workspace")
                .withNoCause();
    }

    @Test
    public void whenProjectIsClosed_coreExceptionIsThrown() throws CoreException {
        projectProvider.getProject().close(null);

        final ILaunchConfiguration config = RobotLaunchConfiguration
                .prepareDefault(asList(projectProvider.getFile("Resource")));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);

        assertThatExceptionOfType(CoreException.class).isThrownBy(robotConfig::getProject)
                .withMessage("Project '" + PROJECT_NAME + "' is currently closed")
                .withNoCause();
    }

    @Test
    public void whenProjectIsEmpty_coreExceptionIsThrown() throws CoreException {
        final ILaunchConfiguration config = RobotLaunchConfiguration
                .prepareDefault(asList(projectProvider.getFile("Resource")));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);

        robotConfig.setProjectName("");

        assertThatExceptionOfType(CoreException.class).isThrownBy(robotConfig::getProject)
                .withMessage("Project cannot be empty")
                .withNoCause();
    }

    @Test
    public void failedOrNonExecutedSuitePathsAreAddedToSuitePathsArgument_whenAskedForRerunWithoutExistingArguments()
            throws CoreException {
        final ILaunchConfiguration config = RobotLaunchConfiguration
                .prepareDefault(asList(projectProvider.getFile("Resource")));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);

        final Map<String, List<String>> failedOrNonExecutedSuitePaths = new HashMap<>();
        failedOrNonExecutedSuitePaths.put("Suite1", asList("test1", "test2"));
        failedOrNonExecutedSuitePaths.put("Suite2", asList("test1", "test2"));

        RobotLaunchConfiguration.fillForFailedOrNonExecutedTestsRerun(robotConfig.asWorkingCopy(),
                failedOrNonExecutedSuitePaths);

        assertThat(robotConfig.getRobotArguments()).isEmpty();
        assertThat(robotConfig.getSuitePaths()).hasSize(2);
        assertThat(robotConfig.getSuitePaths()).containsEntry("Suite1", asList("test1", "test2"));
        assertThat(robotConfig.getSuitePaths()).containsEntry("Suite2", asList("test1", "test2"));
        assertThat(robotConfig.getUnselectedSuitePaths()).isEmpty();
    }

    @Test
    public void failedOrNonExecutedSuitePathsAreAddedToSuitePathsArgument_whenAskedForRerunWithExistingArguments()
            throws CoreException {
        final ILaunchConfiguration config = RobotLaunchConfiguration
                .prepareDefault(asList(projectProvider.getFile("Resource")));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);

        final Map<String, List<String>> failedOrNonExecutedSuitePaths = new HashMap<>();
        failedOrNonExecutedSuitePaths.put("Suite", asList("test"));

        robotConfig.setRobotArguments("-a -b -c");
        RobotLaunchConfiguration.fillForFailedOrNonExecutedTestsRerun(robotConfig.asWorkingCopy(),
                failedOrNonExecutedSuitePaths);

        assertThat(robotConfig.getRobotArguments()).contains("-a -b -c");
        assertThat(robotConfig.getSuitePaths()).hasSize(1);
        assertThat(robotConfig.getSuitePaths()).containsEntry("Suite", asList("test"));
        assertThat(robotConfig.getUnselectedSuitePaths()).isEmpty();
    }

    @Test
    public void environmentVariableIsAdded() throws CoreException {
        final ILaunchConfiguration config = RobotLaunchConfiguration
                .prepareDefault(asList(projectProvider.getFile("Resource")));
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(config);

        robotConfig.addEnvironmentVariable("VAR1", "value1");
        robotConfig.addEnvironmentVariable("VAR2", "value2");

        assertThat(robotConfig.getEnvironmentVariables()).contains("VAR1=value1", "VAR2=value2");
    }

    @Test
    public void linkedResourcesPathsAttributeIsSetInConfig_whenLinkedResourcesExist() throws Exception {
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(projectProvider.getProject());
        final Map<IResource, List<String>> linkedResources = new HashMap<IResource, List<String>>();
        final IResource resource = projectProvider.createFile("resource");
        linkedResources.put(resource, new ArrayList<>());
        robotConfig.setLinkedResourcesPaths(linkedResources);

        assertThat(robotConfig.getLinkedResourcesPaths()).containsExactly("/" + PROJECT_NAME + "/resource");
    }

    @Test
    public void linkedResourcesPathsAttributeIsEmptyInConfig_whenLinkedResourcesIsEmpty() throws Exception {
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(projectProvider.getProject());
        final Map<IResource, List<String>> linkedResources = new HashMap<IResource, List<String>>();
        robotConfig.setLinkedResourcesPaths(linkedResources);

        assertThat(robotConfig.getLinkedResourcesPaths()).isEmpty();
    }

    private RobotLaunchConfiguration createRobotLaunchConfiguration(final IProject project) throws CoreException {
        final ILaunchConfiguration configuration = runConfigurationProvider.create("robot");
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.fillDefaults();
        robotConfig.setProjectName(project.getName());
        return robotConfig;
    }
}
