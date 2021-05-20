package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ChoiceType
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@DataJpaTest
class UpdateMultipleChoiceQuestionTest extends SpockTest {
    def question
    def optionOK
    def optionKO
    def user

    def setup() {
        user = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        user.addCourse(externalCourseExecution)
        userRepository.save(user)

        and: 'an image'
        def image = new Image()
        image.setUrl(IMAGE_1_URL)
        image.setWidth(20)
        imageRepository.save(image)

        given: "create a question"
        question = new Question()
        question.setCourse(externalCourse)
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        question.setNumberOfAnswers(2)
        question.setNumberOfCorrect(1)
        question.setImage(image)
        def questionDetails = new MultipleChoiceQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: 'two options'
        optionOK = new Option()
        optionOK.setContent(OPTION_1_CONTENT)
        optionOK.setCorrect(true)
        optionOK.setSequence(0)
        optionOK.setQuestionDetails(questionDetails)
        optionRepository.save(optionOK)

        optionKO = new Option()
        optionKO.setContent(OPTION_2_CONTENT)
        optionKO.setCorrect(false)
        optionKO.setSequence(1)
        optionKO.setQuestionDetails(questionDetails)
        optionRepository.save(optionKO)
    }

    def "update a question with two options true in a multiple selection"() {
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto(ChoiceType.MULTIPLE_SELECTION))


        and: '1 changed option'
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionOK)
        options.add(optionDto)
        optionDto = new OptionDto(optionKO)
        optionDto.setCorrect(true)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)
        optionRepository.count();
        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question is changed"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() == question.getId()
        result.getTitle() == QUESTION_2_TITLE
        result.getContent() == QUESTION_2_CONTENT

        and: 'are not changed'
        result.getStatus() == Question.Status.AVAILABLE
        result.getNumberOfAnswers() == 2
        result.getNumberOfCorrect() == 1
        result.getDifficulty() == 50
        result.getImage() != null

        and: 'an option is changed'
        result.getQuestionDetails().getOptions().size() == 2
        result.getQuestionDetails().getOptions().stream().filter({ option -> option.isCorrect()}).count() == 2
    }

    def "update a question to single selection"() {
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        and: 'no changed options'
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionOK)
        options.add(optionDto)
        optionDto = new OptionDto(optionKO)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)
        optionRepository.count();
        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question is changed"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() == question.getId()
        result.getTitle() == QUESTION_2_TITLE
        result.getContent() == QUESTION_2_CONTENT

        and: 'are not changed'
        result.getNumberOfCorrect() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getNumberOfAnswers() == 2
        result.getDifficulty() == 50
        result.getImage() != null

        and: 'no option is changed'
        result.getQuestionDetails().getOptions().size() == 2
        def resOptionOne = result.getQuestionDetails().getOptions().stream().filter({ option -> option.isCorrect()}).findAny().orElse(null)
        resOptionOne.getContent() == OPTION_1_CONTENT
        def resOptionTwo = result.getQuestionDetails().getOptions().stream().filter({ option -> !option.isCorrect()}).findAny().orElse(null)
        resOptionTwo.getContent() == OPTION_2_CONTENT
    }

    def "update question with two options true"() {
        given: 'a question'
        def questionDto = new QuestionDto(question)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        def optionDto = new OptionDto(optionOK)
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto(optionKO)
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.ONE_CORRECT_OPTION_NEEDED
    }

    def "update a question to sorting"() {
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto(ChoiceType.SORTING))

        and: 'changed options'
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionOK)
        optionDto.setOrder(2)
        optionDto.setContent(OPTION_2_CONTENT)
        options.add(optionDto)
        optionDto = new OptionDto(optionKO)
        optionDto.setCorrect(true)
        optionDto.setOrder(1)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)
        optionRepository.count();
        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question is not changed"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() == question.getId()
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        
        and: 'are not changed'
        result.getStatus() == Question.Status.AVAILABLE
        result.getNumberOfAnswers() == 2
        result.getNumberOfCorrect() == 1
        result.getDifficulty() == 50
        result.getImage() != null

        and: 'an option is changed'
        result.getQuestionDetails().getOptions().size() == 2
        result.getQuestionDetails().getOptions().stream().filter({ option -> option.getOrder() != null}).count() == 2
    }

    def "update a question with two correct options to single selection "(){
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())


        and: '1 changed option'
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionOK)
        options.add(optionDto)
        optionDto = new OptionDto(optionKO)
        optionDto.setCorrect(true)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.ONE_CORRECT_OPTION_NEEDED

    }

    def "update a question with a single sortable option to sorting"() {
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto(ChoiceType.SORTING))

        and: '1 changed option'
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionOK)
        optionDto.setOrder(1);
        options.add(optionDto)
        optionDto = new OptionDto(optionKO)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.AT_LEAST_TWO_ORDERED_OPTIONS_NEEDED
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
