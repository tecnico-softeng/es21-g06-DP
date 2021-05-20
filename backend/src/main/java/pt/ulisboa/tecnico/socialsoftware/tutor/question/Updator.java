package pt.ulisboa.tecnico.socialsoftware.tutor.question;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CodeFillInQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CodeOrderQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenAnswerQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion;

public interface Updator {
    default void update(MultipleChoiceQuestion question) {
    }

    default void update(CodeFillInQuestion question) {
    }

    default void update(CodeOrderQuestion codeOrderQuestion) {
    }

    default void update(OpenAnswerQuestion openAnswerQuestion) {
    }

    default void update(ItemCombinationQuestion itemCombinationQuestion) {
    }
}
