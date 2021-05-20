package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenAnswerQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenAnswerQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto

@DataJpaTest
class UpdateOpenAnswerQuestionTest extends SpockTest {
    def question

    def setup() {
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
        question.setImage(image)
        def questionDetails = new OpenAnswerQuestion()
        questionDetails.setCorrectAnswer(OPEN_ANSWER_CORRECT_ANSWER)
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)
    }

    def "update an open answer question"(){
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
        and: "add correct answer"
        questionDto.getQuestionDetailsDto().setCorrectAnswer(OPEN_ANSWER_CORRECT_ANSWER2)
 
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
        result.getImage() != null
        and: 'correct answer is changed'
        result.getQuestionDetails().getCorrectAnswer() == OPEN_ANSWER_CORRECT_ANSWER2

    }

    def "update an open answer question without correct answer"() {
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
    
        when:
        questionService.updateQuestion(question.getId(), questionDto)
    
        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.NO_CORRECT_ANSWER
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
    
}