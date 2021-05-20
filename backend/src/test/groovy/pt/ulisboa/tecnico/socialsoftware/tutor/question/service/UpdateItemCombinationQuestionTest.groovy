package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Item
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemGroup
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemGroupDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@DataJpaTest
class UpdateItemCombinationQuestionTest extends SpockTest {
    def question
    def item1
    def item2
    def item3
    def itemGroup1
    def itemGroup2
    def user
    def questionDetails

    def setup() {
        user = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        user.addCourse(externalCourseExecution)
        userRepository.save(user)

        question = new Question()
        question.setCourse(externalCourse)
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        questionDetails = new ItemCombinationQuestion()

        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        item1 = new Item()
        item1.setContent(OPTION_1_CONTENT)
        item1.setSequence(0)
        itemRepository.save(item1)

        item3 = new Item()
        item3.setContent(OPTION_1_CONTENT)
        item3.setSequence(1)
        itemRepository.save(item3)

        itemGroup1 = new ItemGroup()
        itemGroup1.getItems().add(item1)
        itemGroup1.getItems().add(item3)
        itemGroup1.setSequence(0)
        itemGroup1.setQuestionDetails(questionDetails)
        itemGroupRepository.save(itemGroup1)

        item2 = new Item()
        item2.setContent(OPTION_2_CONTENT)
        item2.setSequence(0)
        def combinations = new HashSet<Integer>()
        combinations.add(item1.getSequence())
        item2.setCombinations(combinations)
        itemRepository.save(item2)

        itemGroup2 = new ItemGroup()
        itemGroup2.getItems().add(item2)
        itemGroup2.setSequence(1)
        itemGroup2.setQuestionDetails(questionDetails)
        itemGroupRepository.save(itemGroup2)

        questionDetails.getItemGroups().add(itemGroup1)
        questionDetails.getItemGroups().add(itemGroup2)
    }

    def "update an item"() {
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto(questionDetails))

        and: '1 changed item'
        def items = new ArrayList<ItemDto>()
        def itemDto = new ItemDto(item1)
        itemDto.setContent(OPTION_2_CONTENT)
        items.add(itemDto);
        questionDto.getQuestionDetailsDto().getItemGroups().get(0).setItems(items)

        when: 'create question'
        questionService.updateQuestion(question.getId(), questionDto)

        then: 'an item is changed'
        def result = questionRepository.findAll().get(0)
        result.getQuestionDetails().getItemGroups().get(0).getItems().size() == 1
        def resItem = result.getQuestionDetails().getItemGroups().get(0).getItems().get(0)
        resItem.getContent() == OPTION_2_CONTENT
    }

    def "update item combination question correct combination and add new item to group"() {
        given: "a question"
        def questionDto = new QuestionDto(question)
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto(questionDetails))
        and: '1 added correct answer'
        def itemGroups2 = new ArrayList<ItemGroupDto>()
        def itemGroupDto1 = new ItemGroupDto(itemGroup1)
        def itemGroupDto2 = new ItemGroupDto(itemGroup2)
        def items2 = new ArrayList<ItemDto>()
        def combinations = new HashSet<Integer>()
        def itemDto3 = new ItemDto(item1)
        def itemDto4 = new ItemDto()
        itemDto4.setContent(OPTION_2_CONTENT)
        combinations.add(0)
        itemDto3.setCombinations(combinations)
        items2.add(itemDto3)
        items2.add(itemDto4)
        itemGroupDto1.setItems(items2)
        itemGroups2.add(itemGroupDto1)
        itemGroups2.add(itemGroupDto2)
        questionDto.getQuestionDetailsDto().setItemGroups(itemGroups2)

        when: 'create question'
        questionService.updateQuestion(question.getId(), questionDto)

        then: "an item combination is changed"
        def result = questionRepository.findAll().get(0)
        def resItem = result.getQuestionDetails().getItemGroups().get(0).getItems().get(0)
        resItem.getCombinations().contains(0)
        resItem.getCombinations().size() == 1
        def resItemGroup = result.getQuestionDetails().getItemGroups().get(0)
        resItemGroup.getItems().size() == 2
    }

    def "update item combination question with empty group"() {
        given: "a question"
        def questionDto = new QuestionDto(question)
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())
        and: '1 changed group'
        def items = new ArrayList<ItemDto>()
        def itemGroups = new ArrayList<ItemGroupDto>()
        def itemGroupDto1 = new ItemGroupDto(itemGroup1)
        def itemGroupDto2 = new ItemGroupDto(itemGroup2)
        def itemsEmpty = new ArrayList<ItemDto>()
        def itemDto2 = new ItemDto(item2)
        itemDto2.setCombinations(new HashSet<Integer>())
        items.add(itemDto2)
        itemGroupDto2.setItems(items)
        itemGroupDto1.setItems(itemsEmpty);
        itemGroups.add(itemGroupDto1)
        itemGroups.add(itemGroupDto2)
        questionDto.getQuestionDetailsDto().setItemGroups(itemGroups)
        
        when: 'create question'
        questionService.updateQuestion(question.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.ITEM_GROUPS_CANT_BE_EMPTY
    }
    
    def "update item combination question with combination index out of bounds"() {
        given: "a question"
        def questionDto = new QuestionDto(question)
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())
        
        and: '1 added combination'
        def itemGroupDto1 = new ItemGroupDto(itemGroup1)
        def itemGroupDto2 = new ItemGroupDto(itemGroup2)
        def itemGroups = new ArrayList<ItemGroupDto>()
        def items = new ArrayList<ItemDto>()
        def combinations = new HashSet<Integer>()
        def itemDto = new ItemDto(item1)
        combinations.add(21)
        itemDto.setCombinations(combinations);     
        items.add(itemDto);
        itemGroupDto1.setItems(items)
        itemGroups.add(itemGroupDto1)
        itemGroups.add(itemGroupDto2)
        questionDto.getQuestionDetailsDto().setItemGroups(itemGroups)
               
        when: 'create question'
        questionService.updateQuestion(question.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.COMB_INDEX_OUT_OF_BOUNDS
    }

    def "remove an item and it's respective combinations from a group"() {
        given: "a question"
        def questionDto = new QuestionDto(question)
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())

        and: '1 removed item'
        def itemGroupDto1 = new ItemGroupDto(itemGroup1)
        def itemGroupDto2 = new ItemGroupDto(itemGroup2)
        def itemGroups = new ArrayList<ItemGroupDto>()
        def items = new ArrayList<ItemDto>()
        def combinations = new HashSet<Integer>()
        def itemDto1 = new ItemDto(item1)
        def index = 0
        for(ItemDto itemDto : itemGroupDto1.getItems()) {
            if(itemDto1.getId() == itemDto.getId()) {
                break;
            }
            index++;
        }
        itemGroupDto1.getItems().remove(index)
        
        for(ItemDto itemDto2 : itemGroupDto2.getItems()) {
            if(itemDto2.getCombinations().contains(itemDto1.getSequence())) {
                itemDto2.getCombinations().remove(itemDto1.getSequence())
            }
        }

        itemGroups.add(itemGroupDto1)
        itemGroups.add(itemGroupDto2)
        questionDto.getQuestionDetailsDto().setItemGroups(itemGroups)
               
        when: 'create question'
        questionService.updateQuestion(question.getId(), questionDto)

        then: "item and it's combinations are removed"
        def result = questionRepository.findAll().get(0)
        def resItem = result.getQuestionDetails().getItemGroups().get(0).getItems().get(0)
        resItem.getSequence() != item1.getSequence()
        def resItem2 = result.getQuestionDetails().getItemGroups().get(1).getItems().get(0)
        resItem2.getCombinations().isEmpty()
        
    }
  

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
