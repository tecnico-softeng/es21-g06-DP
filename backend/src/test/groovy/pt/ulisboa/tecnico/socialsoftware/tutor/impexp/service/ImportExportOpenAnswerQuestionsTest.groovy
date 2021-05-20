package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenAnswerQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto

@DataJpaTest
class ImportExportOpenAnswerQuestionsTest extends SpockTest {
    def questionDto
    def setup() {
        questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())

        def image = new ImageDto()
        image.setUrl(IMAGE_1_URL)
        image.setWidth(20)
        questionDto.setImage(image)

        questionDto.getQuestionDetailsDto().setCorrectAnswer(OPEN_ANSWER_CORRECT_ANSWER)
    }

    def "Export open answer question to latex"() {
        given: 'a question'
        questionService.createQuestion(externalCourse.getId(), questionDto)

        when:
        def questionsLatex = questionService.exportQuestionsToLatex()
        print questionsLatex
        then:
        questionsLatex != null
    }

    def 'Export and import to XML'() {
        given: 'a question'
        def questionId = questionService.createQuestion(externalCourse.getId(), questionDto).getId()

        and: 'a xml with one question'
        def questionsXml = questionService.exportQuestionsToXml()

        and: 'a clean database'
        questionService.removeQuestion(questionId)

        when:
        questionService.importQuestionsFromXml(questionsXml)

        then:
        questionRepository.findQuestions(externalCourse.getId()).size() == 1
        def questionResult = questionService.findQuestions(externalCourse.getId()).get(0)
        questionResult.getKey() == null
        questionResult.getTitle() == QUESTION_1_TITLE
        questionResult.getContent() == QUESTION_1_CONTENT
        questionResult.getStatus() == Question.Status.AVAILABLE.name()
        def imageResult = questionResult.getImage()
        imageResult.getWidth() == 20
        imageResult.getUrl() == IMAGE_1_URL
        questionResult.getQuestionDetailsDto().getCorrectAnswer() == OPEN_ANSWER_CORRECT_ANSWER
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
