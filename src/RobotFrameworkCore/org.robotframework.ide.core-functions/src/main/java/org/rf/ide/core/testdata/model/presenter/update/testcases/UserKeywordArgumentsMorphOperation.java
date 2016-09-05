package org.rf.ide.core.testdata.model.presenter.update.testcases;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseUnknownSettings;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class UserKeywordArgumentsMorphOperation extends UserKeywordElementMorphOperation {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_ARGUMENTS;
    }

    @Override
    public TestCaseUnknownSettings insert(final TestCase testCase, final int index,
            final AModelElement<?> modelElement) {
        final KeywordArguments arguments = (KeywordArguments) modelElement;
        
        final TestCaseUnknownSettings unkownSetting = testCase.newUnknownSettings();
        unkownSetting.getDeclaration().setText(arguments.getDeclaration().getText());
        for (final RobotToken arg : arguments.getArguments()) {
            unkownSetting.addArgument(arg.getText());
        }
        for (final RobotToken comment : arguments.getComment()) {
            unkownSetting.addCommentPart(comment);
        }
        return unkownSetting;
    }
}
