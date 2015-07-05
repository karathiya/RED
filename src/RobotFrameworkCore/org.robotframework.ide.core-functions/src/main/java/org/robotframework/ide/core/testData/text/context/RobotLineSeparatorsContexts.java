package org.robotframework.ide.core.testData.text.context;

import java.util.List;

import com.google.common.collect.LinkedListMultimap;


/**
 * Collector for all possible line separators.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see AggregatedOneLineRobotContexts
 * @see ContextBuilder
 */
public class RobotLineSeparatorsContexts implements IContextElement {

    private LinkedListMultimap<IContextElementType, IContextElement> handledElements = LinkedListMultimap
            .create();
    private IContextElementType type = ComplexRobotContextType.SEPARATORS;
    private IContextElement parentContext = null;


    public void addNextSeparators(final List<IContextElement> separators) {
        if (separators != null && !separators.isEmpty()) {
            handledElements.putAll(separators.get(0).getType(), separators);
        }
    }


    public LinkedListMultimap<IContextElementType, IContextElement> getFoundSeperatorsExcludeType() {
        return handledElements;
    }


    public List<IContextElement> getPipeSeparators() {
        return handledElements.get(SimpleRobotContextType.PIPE_SEPARATED);
    }


    public List<IContextElement> getWhitespaceSeparators() {
        return handledElements
                .get(SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED);
    }


    @Override
    public IContextElementType getType() {
        return type;
    }


    @Override
    public void setParent(IContextElement context) {
        this.parentContext = context;
    }


    @Override
    public IContextElement getParent() {
        return this.parentContext;
    }
}
