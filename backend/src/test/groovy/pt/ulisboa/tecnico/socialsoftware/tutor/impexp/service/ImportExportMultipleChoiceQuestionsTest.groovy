package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ChoiceType
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto

@DataJpaTest
class ImportExportMultipleChoiceQuestionsTest extends SpockTest {
    def questionId

    def setup() {
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        def image = new ImageDto()
        image.setUrl(IMAGE_1_URL)
        image.setWidth(20)
        questionDto.setImage(image)

        def optionDto = new OptionDto()
        optionDto.setSequence(0)
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setSequence(1)
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(false)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        questionId = questionService.createQuestion(externalCourse.getId(), questionDto).getId()
    }

    def 'export and import questions to xml'() {
        given: 'a xml with questions'
        def questionsXml = questionService.exportQuestionsToXml()
        print questionsXml
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
        questionResult.getQuestionDetailsDto().getOptions().size() == 2
        def optionOneResult = questionResult.getQuestionDetailsDto().getOptions().get(0)
        def optionTwoResult = questionResult.getQuestionDetailsDto().getOptions().get(1)
        optionOneResult.getSequence() + optionTwoResult.getSequence() == 1
        optionOneResult.getContent() == OPTION_1_CONTENT
        optionTwoResult.getContent() == OPTION_1_CONTENT
        !(optionOneResult.isCorrect() && optionTwoResult.isCorrect())
        optionOneResult.isCorrect() || optionTwoResult.isCorrect()
    }

    def 'export to latex'() {
        when:
        def questionsLatex = questionService.exportQuestionsToLatex()

        then:
        questionsLatex != null
    }



    def "export multiple selection question to xml"(){
        given:"an updated multiple choice question"
        def questionDto = questionService.findQuestions(externalCourse.getId()).get(0)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto(ChoiceType.MULTIPLE_SELECTION))

        def optionDto = new OptionDto()
        def options = new ArrayList<OptionDto>()

            
        optionDto.setSequence(0)
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        options.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setSequence(1)
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(false)
        options.add(optionDto)
        
        optionDto = new OptionDto()
        optionDto.setSequence(2)
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(true)
        options.add(optionDto)

        questionDto.getQuestionDetailsDto().setOptions(options)

        questionService.updateQuestion(questionId, questionDto)

        and:"a xml with the question"
        def questionsXml = questionService.exportQuestionsToXml()
        print questionsXml
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
        questionResult.getQuestionDetailsDto().getChoiceType() == ChoiceType.MULTIPLE_SELECTION
        def imageResult = questionResult.getImage()
        imageResult.getWidth() == 20
        imageResult.getUrl() == IMAGE_1_URL
        questionResult.getQuestionDetailsDto().getOptions().size() == 3
        def optionOneResult = questionResult.getQuestionDetailsDto().getOptions().get(0)
        def optionTwoResult = questionResult.getQuestionDetailsDto().getOptions().get(1)
        def optionThreeResult = questionResult.getQuestionDetailsDto().getOptions().get(2)
        optionOneResult.getSequence() + optionTwoResult.getSequence() + optionThreeResult.getSequence() == 3
        optionOneResult.getContent() == OPTION_1_CONTENT
        optionTwoResult.getContent() == OPTION_1_CONTENT
        optionThreeResult.getContent() == OPTION_2_CONTENT
        optionOneResult.isCorrect() && !optionTwoResult.isCorrect() && optionThreeResult.isCorrect()
    }

    def "export sorting question to xml"(){
        given:"an updated sorting question"
        def questionDto = questionService.findQuestions(externalCourse.getId()).get(0)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto(ChoiceType.SORTING))
        def options = new ArrayList<OptionDto>()

        def optionDto2 = new OptionDto()
        optionDto2.setSequence(0)
        optionDto2.setContent(OPTION_1_CONTENT)
        optionDto2.setCorrect(true)
        options.add(optionDto2)
        optionDto2 = new OptionDto()
        optionDto2.setSequence(1)
        optionDto2.setContent(OPTION_1_CONTENT)
        optionDto2.setCorrect(false)
        options.add(optionDto2)
        
        optionDto2 = new OptionDto()
        optionDto2.setContent(OPTION_1_CONTENT)
        optionDto2.setCorrect(true)
        optionDto2.setOrder(1)
        options.add(optionDto2)
        optionDto2 = new OptionDto()
        optionDto2.setContent(OPTION_2_CONTENT)
        optionDto2.setCorrect(true)
        optionDto2.setOrder(2)
        options.add(optionDto2)
        

        
        questionDto.getQuestionDetailsDto().setOptions(options)

        questionService.updateQuestion(questionId, questionDto)

        and:"a xml with the question"
        def questionsXml = questionService.exportQuestionsToXml()
        print questionsXml
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
        questionResult.getQuestionDetailsDto().getChoiceType() == ChoiceType.SORTING
        def imageResult = questionResult.getImage()
        imageResult.getWidth() == 20
        imageResult.getUrl() == IMAGE_1_URL
        questionResult.getQuestionDetailsDto().getOptions().size() == 4

        def optionOneResult = questionResult.getQuestionDetailsDto().getOptions().get(0)
        def optionTwoResult = questionResult.getQuestionDetailsDto().getOptions().get(1)
        def optionThreeResult = questionResult.getQuestionDetailsDto().getOptions().get(2)
        def optionFourResult = questionResult.getQuestionDetailsDto().getOptions().get(3)

        optionOneResult.getOrder() == null
        optionTwoResult.getOrder() == null
        !optionTwoResult.isCorrect()

        optionThreeResult.getContent() == OPTION_1_CONTENT
        optionFourResult.getContent() == OPTION_2_CONTENT
        optionOneResult.isCorrect() && optionThreeResult.isCorrect() && optionFourResult.isCorrect()
        optionThreeResult.getOrder() == 1
        optionFourResult.getOrder() == 2
    }

    def 'export multiple selection to latex'() {
        given:"an updated multiple choice question"
        def questionDto = questionService.findQuestions(externalCourse.getId()).get(0)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto(ChoiceType.MULTIPLE_SELECTION))

        def optionDto = new OptionDto()
        def options = new ArrayList<OptionDto>()
        optionDto.setSequence(2)
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(true)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        questionService.updateQuestion(questionId, questionDto)

        when:
        def questionsLatex = questionService.exportQuestionsToLatex()
        print questionsLatex

        then:
        questionsLatex != null
    }

    def 'export sorting to latex'() {
        given:"an updated sorting question"
        def questionDto = questionService.findQuestions(externalCourse.getId()).get(0)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto(ChoiceType.SORTING))

        def optionDto2 = new OptionDto()
        optionDto2.setContent(OPTION_1_CONTENT)
        optionDto2.setCorrect(true)
        optionDto2.setOrder(2)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto2)
        optionDto2 = new OptionDto()
        optionDto2.setContent(OPTION_2_CONTENT)
        optionDto2.setCorrect(true)
        optionDto2.setOrder(1)
        options.add(optionDto2)
        questionDto.getQuestionDetailsDto().setOptions(options)

        questionService.updateQuestion(questionId, questionDto)

        when:
        def questionsLatex = questionService.exportQuestionsToLatex()
        print questionsLatex

        then:
        questionsLatex != null
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}