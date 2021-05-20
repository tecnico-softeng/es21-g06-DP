package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.json.JsonOutput
import groovyx.net.http.RESTClient
import groovyx.net.http.HttpResponseException
import org.apache.http.HttpStatus
import org.hibernate.Hibernate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemGroupDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Item
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemGroup
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RemoveItemCombinationQuestionWebServiceIT extends SpockTest {
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

    def "remove an item combination question with a teacher with correct permissions"() {   
        given: "a teacher logged in"
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)    

        when:
        response = restClient.delete(
                path: '/questions/' + question.getId(),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == 200

        and: "the question is removed"
        questionRepository.count() == 0L
        imageRepository.count() == 0L
        itemGroupRepository.count() == 0L
        itemRepository.count() == 0L
    }

    def "remove one of two questions in repository with a teacher with correct permissions"() {
        given: "a teacher logged in"
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: 'a new question'
        def question2 = new Question()
        question2.setCourse(course)
        question2.setKey(2)
        question2.setTitle(QUESTION_1_TITLE)
        question2.setContent(QUESTION_1_CONTENT)
        question2.setStatus(Question.Status.AVAILABLE)
        def questionDetails2 = new ItemCombinationQuestion()

        question2.setQuestionDetails(questionDetails2)

        def itemGroup3 = new ItemGroup()
        def itemGroup4 = new ItemGroup()

        def item4 = new Item()
        item4.setContent(OPTION_1_CONTENT)
        item4.setSequence(0)
        item4.setItemGroup(itemGroup3)

        def item5 = new Item()
        item5.setContent(OPTION_2_CONTENT)
        item5.setSequence(0)
        item5.setItemGroup(itemGroup4)

        itemGroup3.getItems().add(item4)
        itemGroup3.setSequence(0)
        itemGroup3.setQuestionDetails(questionDetails2)
        questionDetails2.getItemGroups().add(itemGroup3)

        itemGroup4.getItems().add(item5)
        itemGroup4.setSequence(1)
        itemGroup4.setQuestionDetails(questionDetails2)
        questionDetails2.getItemGroups().add(itemGroup4)

        itemGroupRepository.save(itemGroup4)
        itemGroupRepository.save(itemGroup3)

        itemRepository.save(item4)
        itemRepository.save(item5)

        questionDetailsRepository.save(questionDetails2)
        questionRepository.save(question2)

        when:
        response = restClient.delete(
                path: '/questions/' + question2.getId(),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == 200

        and: "the correct question is removed"
        questionRepository.count() == 1L
        def repoResult = questionRepository.findAll().get(0)
        repoResult.getId() != null
        repoResult.getTitle() == question.getTitle()
        repoResult.getContent() == question.getContent()
        repoResult.getStatus() == Question.Status.AVAILABLE
        def repoItemComb = questionService.findQuestionById(repoResult.id)
        repoItemComb.questionDetailsDto.itemGroups.size() == 2
        repoItemComb.questionDetailsDto.itemGroups.get(0).items.size() == 2
        repoItemComb.questionDetailsDto.itemGroups.get(0).items.get(0).content == item1.getContent()
        repoItemComb.questionDetailsDto.itemGroups.get(1).items.get(0).combinations.size() == 1
        repoItemComb.questionDetailsDto.itemGroups.get(1).items.get(0).combinations.contains(0)
    }

    def "cannot remove item combination question with no user logged in"() {
        given: "no log in"

        when:
        response = restClient.delete(
                path: '/questions/' + question.getId(),
                requestContentType: 'application/json'
        )

        then: "error 403 is thrown"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
    }


    def "cannot remove item combination question with a student logged in"() {
        given: "a student logged in"
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD) 

        when:
        response = restClient.delete(
                path: '/questions/' + question.getId(),
                requestContentType: 'application/json'
        )

        then: "error 403 is thrown"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
    }

    def "cannot remove item combination question with a teacher with no permissions logged in"() {
        given: "a demo teacher logged in"     
        demoTeacherLogin()

        when:
        response = restClient.delete(
                path: '/questions/' + question.getId(),
                requestContentType: 'application/json'
        )

        then: "error 403 is thrown"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
    }

    def "cannot remove item combination question that does not exist"() {
        given: "a teacher logged in"
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        when:
        response = restClient.delete(
                path: '/questions/' + 21,
                requestContentType: 'application/json'
        )

        then: "error 403 is thrown"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
        and: 'question remains in repository'
        questionRepository.count() == 1L
        questionDetailsRepository.count() == 1L
        itemGroupRepository.count() == 2L
        itemRepository.count() == 3L
    }

    def cleanup() {
        persistentCourseCleanup()

        userRepository.deleteById(teacher.getId())
        userRepository.deleteById(student.getId())
        courseExecutionRepository.deleteById(courseExecution.getId())

        courseRepository.deleteById(course.getId())
    }
}
