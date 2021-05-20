package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.json.JsonOutput
import groovyx.net.http.RESTClient
import groovyx.net.http.HttpResponseException
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ChoiceType
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateMultipleChoiceQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def course
    def courseExecution
    def teacher
    def questionDto
    def response
    def student

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

        student = new User(USER_2_NAME, USER_2_USERNAME, USER_2_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        student.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        userRepository.save(student)

        questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDto.getQuestionDetailsDto().setChoiceType(ChoiceType.MULTIPLE_SELECTION)

        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(false)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

    }

    def "create multiple choice single selection question for course execution"() {
        given: "a teacher logged in"
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)
        and: "a single selection question"
        questionDto.getQuestionDetailsDto().setChoiceType(ChoiceType.SINGLE_SELECTION)

        when:
        def mapper = new ObjectMapper()
        response = restClient.post(
                path: '/courses/' + courseExecution.getId() + '/questions',
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == HttpStatus.SC_OK
        and: "the correct data is sent"
        def question = response.data
        question.id != null
        question.status == Question.Status.AVAILABLE.name()
        question.title == QUESTION_1_TITLE
        question.content == QUESTION_1_CONTENT
        question.questionDetailsDto.options.size() == 2
        def option = question.questionDetailsDto.options.get(0)
        option.correct
        option.content == OPTION_1_CONTENT
        def option2 = question.questionDetailsDto.options.get(1)
        !option2.correct
        option2.content == OPTION_2_CONTENT

        and: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getQuestionDetails().getOptions().size() == 2

        def resOption = result.getQuestionDetails().getOptions().get(0)
        resOption.getContent() == OPTION_1_CONTENT
        resOption.isCorrect()

        def resOption2 = result.getQuestionDetails().getOptions().get(1)
        resOption2.getContent() == OPTION_2_CONTENT
        !resOption2.isCorrect()
    }


    def "create multiple choice question for course execution"() {
        given: "a teacher logged in"
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        when:
        def mapper = new ObjectMapper()
        response = restClient.post(
                path: '/courses/' + courseExecution.getId() + '/questions',
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status ==  HttpStatus.SC_OK
        and: "the correct data is sent"
        def question = response.data
        question.id != null
        question.status == Question.Status.AVAILABLE.name()
        question.title == QUESTION_1_TITLE
        question.content == QUESTION_1_CONTENT
        question.questionDetailsDto.options.size() == 2
        def option = question.questionDetailsDto.options.get(0)
        option.correct
        option.content == OPTION_1_CONTENT
        def option2 = question.questionDetailsDto.options.get(1)
        !option2.correct
        option2.content == OPTION_2_CONTENT

        and: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getQuestionDetails().getOptions().size() == 2

        def resOption = result.getQuestionDetails().getOptions().get(0)
        resOption.getContent() == OPTION_1_CONTENT
        resOption.isCorrect()

        def resOption2 = result.getQuestionDetails().getOptions().get(1)
        resOption2.getContent() == OPTION_2_CONTENT
        !resOption2.isCorrect()
    }

    def "create multiple choice sorting question for course execution"() {
        given: "a teacher logged in"
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)
        and: "a sorting question with sorting options"
        questionDto.getQuestionDetailsDto().setChoiceType(ChoiceType.SORTING)
        questionDto.getQuestionDetailsDto().getOptions().get(0).setOrder(1)
        questionDto.getQuestionDetailsDto().getOptions().get(1).setOrder(2)

        when:
        def mapper = new ObjectMapper()
        response = restClient.post(
                path: '/courses/' + courseExecution.getId() + '/questions',
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == HttpStatus.SC_OK
        and: "the correct data is sent"
        def question = response.data
        question.id != null
        question.status == Question.Status.AVAILABLE.name()
        question.title == QUESTION_1_TITLE
        question.content == QUESTION_1_CONTENT
        question.questionDetailsDto.options.size() == 2
        def option = question.questionDetailsDto.options.get(0)
        option.correct
        option.content == OPTION_1_CONTENT
        def option2 = question.questionDetailsDto.options.get(1)
        !option2.correct
        option2.content == OPTION_2_CONTENT

        and: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getQuestionDetails().getOptions().size() == 2

        def resOption = result.getQuestionDetails().getOptions().get(0)
        resOption.getContent() == OPTION_1_CONTENT
        resOption.isCorrect()

        def resOption2 = result.getQuestionDetails().getOptions().get(1)
        resOption2.getContent() == OPTION_2_CONTENT
        !resOption2.isCorrect()
    }

    def "cannot create multiple choice question for course execution without a teacher logged in"() {
        when:
        def mapper = new ObjectMapper()
        response = restClient.post(
                path: '/courses/' + courseExecution.getId() + '/questions',
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "error 403 is thrown"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

        and: "question does not exist"
        questionRepository.count() == 0L

    }

    def "student cannot create question"() {
        given: "a student logged in"
        createdUserLogin(USER_2_USERNAME, USER_2_PASSWORD)

        when:
        def mapper = new ObjectMapper()
        response = restClient.post(
                path: '/courses/' + courseExecution.getId() + '/questions',
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "error 403 is thrown"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

        and: "question does not exist"
        questionRepository.count() == 0L
    }

    def "teacher not assigned to course cannot create question"() {
        given: "a demo teacher logged in"
        demoTeacherLogin()

        when:
        def mapper = new ObjectMapper()
        response = restClient.post(
                path: '/courses/' + courseExecution.getId() + '/questions',
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "error 403 is thrown"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

        and: "question does not exist"
        questionRepository.count() == 0L
    }


    def cleanup() {
        persistentCourseCleanup()

        userRepository.deleteById(teacher.getId())
        userRepository.deleteById(student.getId())
        courseExecutionRepository.deleteById(courseExecution.getId())

        courseRepository.deleteById(course.getId())
    }
}