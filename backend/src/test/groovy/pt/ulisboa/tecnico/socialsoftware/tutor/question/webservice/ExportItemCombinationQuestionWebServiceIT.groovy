package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Item
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemGroup
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExportItemCombinationQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def course
    def courseExecution
    def item1
    def item2
    def item3
    def itemGroup1
    def itemGroup2
    def question
    def teacher
    def student
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
                User.Role.STUDENT, false, AuthUser.Type.EXTERNAL)
        student.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        student.addCourse(courseExecution)
        courseExecution.addUser(student)
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

    def "export course questions with assigned teacher"() {
        given: 'an assigned teacher'
        createdUserLogin(USER_1_EMAIL,USER_1_PASSWORD)
        and: 'prepare request response'
        restClient.handler.failure = { resp, reader ->
            [response:resp, reader:reader]
        }
        restClient.handler.success = { resp, reader ->
            [response:resp, reader:reader]
        }

        when: "the web service is invoked"
        def map = restClient.get(
                path: "/courses/" + courseExecution.getId() + "/questions/export",
                requestContentType: "application/json"
        )

        then: "the response status is OK"
        assert map['response'].status == HttpStatus.SC_OK
    }



    def "cannot export course questions with no user logged in"() {
        given: 'prepare request response'
        restClient.handler.failure = { resp, reader ->
            [response:resp, reader:reader]
        }
        restClient.handler.success = { resp, reader ->
            [response:resp, reader:reader]
        }

        when: "the web service is invoked"
        def map = restClient.get(
                path: "/courses/" + courseExecution.getId() + "/questions/export",
                requestContentType: "application/json"
        )

        then: "the response status is 403 - denied"
        assert map['response'].status == HttpStatus.SC_FORBIDDEN
    }


    def "cannot export course questions without assigned teacher"() {
        given: 'a demo unassigned teacher'
        demoTeacherLogin()
        and: 'prepare request response'
        restClient.handler.failure = { resp, reader ->
            [response:resp, reader:reader]
        }
        restClient.handler.success = { resp, reader ->
            [response:resp, reader:reader]
        }

        when: "the web service is invoked"
        def map = restClient.get(
                path: "/courses/" + courseExecution.getId() + "/questions/export",
                requestContentType: "application/json"
        )

        then: "the response status is 403 - denied"
        assert map['response'].status == HttpStatus.SC_FORBIDDEN
    }

    def "cannot export course questions with an enrolled student"() {
        given: 'an enrolled student'
        createdUserLogin(USER_2_EMAIL,USER_2_PASSWORD)
        and: 'prepare request response'
        restClient.handler.failure = { resp, reader ->
            [response:resp, reader:reader]
        }
        restClient.handler.success = { resp, reader ->
            [response:resp, reader:reader]
        }

        when: "the web service is invoked"
        def map = restClient.get(
                path: "/courses/" + courseExecution.getId() + "/questions/export",
                requestContentType: "application/json"
        )

        then: "the response status is 403 - denied"
        assert map['response'].status == HttpStatus.SC_FORBIDDEN
    }

    def cleanup() {
        persistentCourseCleanup()
        
        userRepository.deleteById(teacher.getId())
        userRepository.deleteById(student.getId())
        courseExecutionRepository.deleteById(courseExecution.getId())

        courseRepository.deleteById(course.getId())
    }
}
