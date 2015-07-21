package org.robotframework.ide.eclipse.main.plugin.project.build;

import org.eclipse.core.resources.IFile;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;

public class PythonLibraryLibdocGenerator implements ILibdocGenerator {

    private final String libName;
    private final String libPath;
    private final IFile targetSpecFile;

    public PythonLibraryLibdocGenerator(final String libName, final String path, final IFile targetSpecFile) {
        this.libName = libName;
        this.libPath = path;
        this.targetSpecFile = targetSpecFile;
    }

    @Override
    public void generateLibdoc(final RobotRuntimeEnvironment runtimeEnvironment) {
        runtimeEnvironment.createLibdocForPythonLibrary(libName, libPath, targetSpecFile.getLocation().toFile());
    }

    @Override
    public String getMessage() {
        return "generating libdoc for " + libName + " library contained in " + libPath;
    }
}
