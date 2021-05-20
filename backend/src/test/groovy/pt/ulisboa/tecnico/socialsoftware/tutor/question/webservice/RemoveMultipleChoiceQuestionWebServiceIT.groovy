package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ChoiceType
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RemoveMultipleChoiceQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def course
    def courseExecution
    def question
    def response
    def teacher1
    def student
    def questionDetails

    def setup() {
        restClient = new RESTClient("http://localhost:" + port)

        course = new Course(COURSE_1_NAME, Course.Type.EXTERNAL)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.EXTERNAL, LOCAL_DATE_TOMORROW)
        courseExecutionRepository.save(courseExecution)

        student = new User(USER_2_NAME, USER_2_USERNAME, USER_2_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        student.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        userRepository.save(student)

        teacher1 = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.EXTERNAL)
        teacher1.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        teacher1.addCourse(courseExecution)
        courseExecution.addUser(teacher1)
        userRepository.save(teacher1)


        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        question.setCourse(course)
        questionDetails = new MultipleChoiceQuestion()

        questionDetails.setChoiceType(ChoiceType.MULTIPLE_SELECTION);
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        options.add(optionDto)
        questionDetails.setOptions(options)
        question.setQuestionDetails(questionDetails)

        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

    }

    def "remove Multiple Choice Question"() {
        given:

        createdUserLogin(USER_1_USERNAME,USER_1_PASSWORD)
        when:

        response = restClient.delete(
                path: '/questions/' + question.getId(),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == 200
        and: "the question is removeQuestion"
        questionRepository.count() == 0L
        imageRepository.count() == 0L
        optionRepository.count() == 0L
    }

    def "remove one of two questions in repository by assigned teacher"() {
        given:
        createdUserLogin(USER_1_USERNAME,USER_1_PASSWORD)
        and: 'another question'
        def question2 = new Question()
        question2.setKey(2)
        question2.setTitle(QUESTION_2_TITLE)
        question2.setContent(QUESTION_2_CONTENT)
        question2.setStatus(Question.Status.AVAILABLE)
        question2.setCourse(course)
        def questionDetails2 = new MultipleChoiceQuestion()
        questionDetails2.setChoiceType(ChoiceType.SORTING)
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(2)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setOrder(1)
        optionDto.setCorrect(true)
        options.add(optionDto)
        questionDetails2.setOptions(options)

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
        response.status == 200
        and: "the correct question is removed"
        questionRepository.count() == 1L
        questionDetailsRepository.count() == 1L
        questionDetailsRepository.findById(questionDetails.getId()).get().getChoiceType() == ChoiceType.MULTIPLE_SELECTION
        optionRepository.count() == 2L
    }



    def "cannot remove a question as a student"() {
        given:
        createdUserLogin(USER_2_USERNAME,USER_2_PASSWORD)
        when:

        response = restClient.delete(
                path: '/questions/' + question.getId(),
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
        and: 'the question continues in the database'
        questionRepository.count() == 1L
        questionDetailsRepository.count() == 1L
        questionDetailsRepository.findById(questionDetails.getId()).get().getChoiceType() == ChoiceType.MULTIPLE_SELECTION
        optionRepository.count() == 2L
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
        questionDetailsRepository.findById(questionDetails.getId()).get().getChoiceType() == ChoiceType.MULTIPLE_SELECTION
        optionRepository.count() == 2L
    }

    def "cannot remove a non-existing question"() {
        given:

        createdUserLogin(USER_1_USERNAME,USER_1_PASSWORD)
        when:

        response = restClient.delete(
                path: '/questions/' + 999,
                requestContentType: 'application/json'
        )

        then: "403 is thrown"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
        and: 'question remains in repository'
        questionRepository.count() == 1L
        questionDetailsRepository.count() == 1L
        questionDetailsRepository.findById(questionDetails.getId()).get().getChoiceType() == ChoiceType.MULTIPLE_SELECTION
        optionRepository.count() == 2L
    }

    def cleanup() {
        persistentCourseCleanup()
        userRepository.deleteById(student.getId())
        userRepository.deleteById(teacher1.getId())

        courseExecutionRepository.deleteById(courseExecution.getId())

        courseRepository.deleteById(course.getId())
    }
}