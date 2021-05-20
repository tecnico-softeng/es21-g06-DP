package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenAnswerQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExportOpenAnswerQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def course
    def courseExecution
    def question
    def assignedTeacher
    def student
    def questionDetails

    def setup() {
        restClient = new RESTClient("http://localhost:" + port)

        course = new Course(COURSE_1_NAME, Course.Type.EXTERNAL)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.EXTERNAL, LOCAL_DATE_TOMORROW)
        courseExecutionRepository.save(courseExecution)

        assignedTeacher = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.EXTERNAL)
        assignedTeacher.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        assignedTeacher.addCourse(courseExecution)
        courseExecution.addUser(assignedTeacher)
        userRepository.save(assignedTeacher)

        student = new User(USER_3_NAME, USER_3_EMAIL, USER_3_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.EXTERNAL)
        student.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        student.addCourse(courseExecution)
        courseExecution.addUser(student)
        userRepository.save(student)

        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        question.setCourse(course)
        questionDetails = new OpenAnswerQuestion()
        questionDetails.setCorrectAnswer(OPEN_ANSWER_CORRECT_ANSWER)
        question.setQuestionDetails(questionDetails)
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

    def "export 2 course questions with assigned teacher"() {
        given: 'an assigned teacher'
        createdUserLogin(USER_1_EMAIL,USER_1_PASSWORD)
        and: 'a second question'
        def question2 = new Question()
        question2.setKey(1)
        question2.setTitle(QUESTION_2_TITLE)
        question2.setContent(QUESTION_2_CONTENT)
        question2.setStatus(Question.Status.AVAILABLE)
        question2.setCourse(course)
        questionDetails = new OpenAnswerQuestion()
        questionDetails.setCorrectAnswer(OPEN_ANSWER_CORRECT_ANSWER2)
        question2.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question2)
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
        createdUserLogin(USER_3_EMAIL,USER_1_PASSWORD)
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
        userRepository.deleteById(assignedTeacher.getId())
        userRepository.deleteById(student.getId())
        courseExecutionRepository.deleteById(courseExecution.getId())

        courseRepository.deleteById(course.getId())
    }
}