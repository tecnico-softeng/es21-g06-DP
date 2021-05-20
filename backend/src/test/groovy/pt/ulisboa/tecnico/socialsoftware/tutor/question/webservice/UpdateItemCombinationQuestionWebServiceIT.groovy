package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import com.fasterxml.jackson.databind.ObjectMapper;
import groovyx.net.http.RESTClient
import groovyx.net.http.HttpResponseException
import groovy.json.JsonOutput
import org.apache.http.HttpStatus
import org.hibernate.Hibernate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Item
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemGroup
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemGroupDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UpdateItemCombinationQuestionWebServiceIT extends SpockTest{
    @LocalServerPort
    private int port

    def course
    def courseExecution
    def teacher
    def student
    def question
    def response
    def itemGroup1
    def itemGroup2
    def item1
    def item2
    def item3
    def questionDetails

    def setup() {
        restClient = new RESTClient("http://localhost:" + port)

        course = new Course(COURSE_1_NAME, Course.Type.EXTERNAL)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.EXTERNAL, LOCAL_DATE_TOMORROW)
        courseExecutionRepository.save(courseExecution)

        teacher = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.EXTERNAL)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        teacher.addCourse(courseExecution)
        courseExecution.addUser(teacher)
        userRepository.save(teacher)

        student = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        student.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        userRepository.save(student)

        question = new Question()
        question.setCourse(course)
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        questionDetails = new ItemCombinationQuestion()

        question.setQuestionDetails(questionDetails)
       
        itemGroup1 = new ItemGroup()
        itemGroup2 = new ItemGroup()

        item1 = new Item()
        item1.setContent(OPTION_1_CONTENT)
        item1.setSequence(0)
        item1.setItemGroup(itemGroup1)

        item2 = new Item()
        item2.setContent(OPTION_2_CONTENT)
        item2.setSequence(0)
        item2.setItemGroup(itemGroup2)
        def combinations = new HashSet<Integer>()
        combinations.add(item1.getSequence())
        item2.setCombinations(combinations)

        item3 = new Item()
        item3.setContent(OPTION_1_CONTENT)
        item3.setSequence(1)
        item3.setItemGroup(itemGroup1)

        itemGroup1.getItems().add(item1)
        itemGroup1.getItems().add(item3)
        itemGroup1.setSequence(0)
        itemGroup1.setQuestionDetails(questionDetails)
        questionDetails.getItemGroups().add(itemGroup1)

        itemGroup2.getItems().add(item2)
        itemGroup2.setSequence(1)
        itemGroup2.setQuestionDetails(questionDetails)
        questionDetails.getItemGroups().add(itemGroup2)

        itemGroupRepository.save(itemGroup2)
        itemGroupRepository.save(itemGroup1)

        itemRepository.save(item1)
        itemRepository.save(item3)
        itemRepository.save(item2)

        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)
    }

    def "edit an item combination question with a teacher with correct permissions"() {
        given: "an edited questionDto"
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        
        and: "a teacher with correct permissions logged in"
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD) 
        
        and: "an edited itemCombinationQuestionDto with one correct combination and new item to group"
        def itemCombQuestionDto = new ItemCombinationQuestionDto(questionDetails)
        
        def itemGroups = new ArrayList<ItemGroupDto>()
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
        itemGroups.add(itemGroupDto1)
        itemGroups.add(itemGroupDto2)
        questionDto.getQuestionDetailsDto().setItemGroups(itemGroups)

        when:
        def mapper = new ObjectMapper()
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == 200
        
        and: "if it responds with the correct updated question"
        def respResult = response.data
        respResult.id != null
        respResult.title == questionDto.getTitle()
        respResult.content == questionDto.getContent()
        respResult.status == Question.Status.AVAILABLE.name()
        respResult.questionDetailsDto.type == "item_combination"
        respResult.questionDetailsDto.itemGroups.size() == 2
        respResult.questionDetailsDto.itemGroups.get(0).items.size() == 2
        respResult.questionDetailsDto.itemGroups.get(0).items.get(0).content == OPTION_1_CONTENT
        respResult.questionDetailsDto.itemGroups.get(0).items.get(0).combinations.contains(0)

        and: "if the correct updated question is in the repository"
        questionRepository.count() == 1L
        def repoResult = questionRepository.findAll().get(0)
        respResult.title == questionDto.getTitle()
        respResult.content == questionDto.getContent()
        respResult.status == Question.Status.AVAILABLE.name()
        def repoItemComb = questionService.findQuestionById(repoResult.id)
        repoItemComb.questionDetailsDto.itemGroups.size() == 2
        def resItemGroup = repoItemComb.questionDetailsDto.itemGroups.get(0)
        resItemGroup.items.size() == 2
        def repoItem = repoItemComb.questionDetailsDto.itemGroups.get(0).items.get(0)
        repoItem.content == OPTION_1_CONTENT
        repoItem.combinations.contains(0)
    }

    def "update item combination question with wrong parameters"() {
        given: "a question"
        def questionDto = new QuestionDto(question)
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto(questionDetails))

        and: "a teacher with correct permissions logged in"
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD) 

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
        
        when:
        def mapper = new ObjectMapper()
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "exception is thrown"
        HttpResponseException e = thrown(HttpResponseException)
        e.response.data["message"] == ErrorMessage.ITEM_GROUPS_CANT_BE_EMPTY.label
    }

    def "cannot update item combination question with no user logged in"() {
        given: "a question"
        def questionDto = new QuestionDto(question)
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto(questionDetails))

        when:
        def mapper = new ObjectMapper()
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "error 403 is thrown"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
    }


    def "cannot update item combination question with a student logged in"() {
        given: "a question"
        def questionDto = new QuestionDto(question)
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto(questionDetails))

        and: "a student logged in"
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD) 

        when:
        def mapper = new ObjectMapper()
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "error 403 is thrown"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
    }

    def "cannot update item combination question with a teacher with no permissions logged in"() {
        given: "a question"
        def questionDto = new QuestionDto(question)
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto(questionDetails))

        and: "a demo teacher logged in"     
        demoTeacherLogin()

        when:
        def mapper = new ObjectMapper()
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "error 403 is thrown"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
    }

    def cleanup() {
        persistentCourseCleanup()

        userRepository.deleteById(teacher.getId())
        userRepository.deleteById(student.getId())

        courseExecutionRepository.deleteById(courseExecution.getId())

        courseRepository.deleteById(course.getId())
    }
}
