package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemGroupDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto

@DataJpaTest
class ImportExportItemCombinationQuestionTest extends SpockTest {
    Integer questionId
    def setup() {
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        def itemCombQuestionDto = new ItemCombinationQuestionDto()

        def itemDto1 = new ItemDto()
        itemDto1.setContent(OPTION_1_CONTENT)
        itemDto1.setSequence(0)

        def itemDto3 = new ItemDto()
        itemDto3.setContent(OPTION_1_CONTENT)
        itemDto3.setSequence(1)

        def itemGroupDto1 = new ItemGroupDto()
        itemGroupDto1.getItems().add(itemDto1)
        itemGroupDto1.getItems().add(itemDto3)
        itemGroupDto1.setSequence(0)

        def itemDto2 = new ItemDto()
        itemDto2.setContent(OPTION_2_CONTENT)
        itemDto2.setSequence(0)

        def itemDto4 = new ItemDto()
        itemDto4.setContent(OPTION_2_CONTENT)
        itemDto4.setSequence(1)


        def combinations1 = new HashSet<Integer>()
        combinations1.add(itemDto2.getSequence())
        itemDto1.setCombinations(combinations1)

        def combinations2 = new HashSet<Integer>()
        combinations2.add(itemDto1.getSequence())
        combinations2.add(itemDto3.getSequence())
        itemDto2.setCombinations(combinations2)

        def combinations3 = new HashSet<Integer>()
        combinations3.add(itemDto2.getSequence())
        itemDto3.setCombinations(combinations3)

        def itemGroupDto2 = new ItemGroupDto()
        itemGroupDto2.getItems().add(itemDto2)
        itemGroupDto2.getItems().add(itemDto4)
        itemGroupDto2.setSequence(1)

        itemCombQuestionDto.getItemGroups().add(itemGroupDto1)
        itemCombQuestionDto.getItemGroups().add(itemGroupDto2)
        
        questionDto.setQuestionDetailsDto(itemCombQuestionDto)

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

        def itemCombQuestionDetailsDto = (ItemCombinationQuestionDto) questionResult.getQuestionDetailsDto()
        itemCombQuestionDetailsDto.getItemGroups().size() == 2
        def resItem = itemCombQuestionDetailsDto.getItemGroups().get(1).getItems().get(0)
        resItem.getContent() == OPTION_2_CONTENT
        resItem.getCombinations().size() == 2
        resItem.getSequence() == 0
    }

    def 'export to latex'() {
        when:
        def questionsLatex = questionService.exportQuestionsToLatex()
        then:
        questionsLatex != null
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
