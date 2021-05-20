package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import groovyx.net.http.RESTClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenAnswerQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import groovyx.net.http.HttpResponseException
import org.apache.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RemoveOpenAnswerQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def course
    def courseExecution
    def question
    def response
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

    def "remove one of two questions in repository by assigned teacher"() {
        given:
        createdUserLogin(USER_1_EMAIL,USER_1_PASSWORD)
        and: 'another question'
        def question2 = new Question()
        question2.setKey(2)
        question2.setTitle(QUESTION_2_TITLE)
        question2.setContent(QUESTION_2_CONTENT)
        question2.setStatus(Question.Status.AVAILABLE)
        question2.setCourse(course)
        def questionDetails2 = new OpenAnswerQuestion()
        questionDetails2.setCorrectAnswer(OPEN_ANSWER_CORRECT_ANSWER2)
        question2.setQuestionDetails(questionDetails2)
        questionDetailsRepository.save(questionDetails2)
        questionRepository.save(question2)

        when:
        response = restClient.delete(
                path: '/questions/' + question2.getId(),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == HttpStatus.SC_OK
        and: "the correct question is removed"
        questionRepository.count() == 1L
        questionDetailsRepository.count() == 1L
        questionDetailsRepository.findById(questionDetails.getId()).get().getCorrectAnswer() == OPEN_ANSWER_CORRECT_ANSWER
    }

    def "remove a question with a teacher assigned to the course"() {
        given:

        createdUserLogin(USER_1_EMAIL,USER_1_PASSWORD)
        when:

        response = restClient.delete(
                path: '/questions/' + question.getId(),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == HttpStatus.SC_OK
        and: "the question is removed"
        questionRepository.count() == 0L
        questionDetailsRepository.count() == 0L
    }


    def "cannot remove a question without a teacher assigned to the course"() {
        given:
        demoTeacherLogin()

        when:

        response = restClient.delete(
                path: '/questions/' + question.getId(),
                requestContentType: 'application/json'
        )

        then: "403 is thrown"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
        and: 'question remains in repository'
        questionRepository.count() == 1L
        questionDetailsRepository.count() == 1L
        questionDetailsRepository.findById(questionDetails.getId()).get().getCorrectAnswer() == OPEN_ANSWER_CORRECT_ANSWER
    }

    def "cannot remove a question with a student assigned to the course"() {
        given:

        createdUserLogin(USER_3_EMAIL,USER_1_PASSWORD)
        when:

        response = restClient.delete(
                path: '/questions/' + question.getId(),
                requestContentType: 'application/json'
        )

        then: "403 is thrown"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
        and: 'question remains in repository'
        questionRepository.count() == 1L
        questionDetailsRepository.count() == 1L
        questionDetailsRepository.findById(questionDetails.getId()).get().getCorrectAnswer() == OPEN_ANSWER_CORRECT_ANSWER
    }

    def "cannot remove a non-existing question"() {
        given:

        createdUserLogin(USER_1_EMAIL,USER_1_PASSWORD)
        when:

        response = restClient.delete(
                path: '/questions/' + 123,
                requestContentType: 'application/json'
        )

        then: "403 is thrown"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
        and: 'question remains in repository'
        questionRepository.count() == 1L
        questionDetailsRepository.count() == 1L
        questionDetailsRepository.findById(questionDetails.getId()).get().getCorrectAnswer() == OPEN_ANSWER_CORRECT_ANSWER
    }

    def cleanup() {
        persistentCourseCleanup()
        userRepository.deleteById(assignedTeacher.getId())
        userRepository.deleteById(student.getId())
        courseExecutionRepository.deleteById(courseExecution.getId())

        courseRepository.deleteById(course.getId())
    }
}
