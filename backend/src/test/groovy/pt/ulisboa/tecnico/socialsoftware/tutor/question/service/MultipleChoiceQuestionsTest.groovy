package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ChoiceType
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.*


@DataJpaTest
class MultipleChoiceQuestionsTest extends SpockTest {

    def "create a multiple choice question with two correct options"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDto.getQuestionDetailsDto().setChoiceType(ChoiceType.MULTIPLE_SELECTION)

        and: 'two options'
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getQuestionDetails().getOptions().size() == 2

        def resOption = result.getQuestionDetails().getOptions().get(0)
        resOption.getContent() == OPTION_1_CONTENT
        resOption.isCorrect()

        def resOption2 = result.getQuestionDetails().getOptions().get(1)
        resOption2.getContent() == OPTION_1_CONTENT
        resOption2.isCorrect()
    }

    def "create a multiple choice question with three ordered options"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDto.getQuestionDetailsDto().setChoiceType(ChoiceType.SORTING)

        and: 'three options'
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(1)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)

        optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(2)
        options.add(optionDto)

        optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(false)
        optionDto.setOrder(null)
        options.add(optionDto)

        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        def rawResult = questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the correct data is sent back"
        rawResult instanceof QuestionDto
        def result = (QuestionDto) rawResult
        result.getId() != null
        result.getStatus() == Question.Status.AVAILABLE.toString()
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getImage() == null
        result.getQuestionDetailsDto().getOptions().size() == 3
        result.getQuestionDetailsDto().getOptions().get(0).getContent() == OPTION_1_CONTENT
        result.getQuestionDetailsDto().getChoiceType() == ChoiceType.SORTING

        then: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def repoResult = questionRepository.findAll().get(0)

        externalCourse.getQuestions().contains(repoResult)
        def resOption = repoResult.getQuestionDetails().getOptions().get(0)
        resOption.getContent() == OPTION_1_CONTENT
        resOption.isCorrect()
        resOption.getOrder() == 1
        def resOption2 = repoResult.getQuestionDetails().getOptions().get(2)
        resOption2.getContent() == OPTION_1_CONTENT
        !resOption2.isCorrect()
        resOption2.getOrder() == null
    }

    def "cannot create a multiple choice question without Correct Answers"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDto.getQuestionDetailsDto().setChoiceType(ChoiceType.MULTIPLE_SELECTION)

        and: 'two options'
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(false)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(false)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.NO_CORRECT_OPTION
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
