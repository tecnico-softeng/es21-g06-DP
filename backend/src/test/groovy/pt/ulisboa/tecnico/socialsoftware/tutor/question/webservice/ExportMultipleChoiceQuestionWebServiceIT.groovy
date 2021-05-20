package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import groovyx.net.http.RESTClient
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
class ExportMultipleChoiceQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def course
    def courseExecution
    def teacher
    def questionDto
    def map
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
        optionDto.setCorrect(true)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

    }

    def "export course single selection questions with assigned teacher"() {
        given: 'an assigned teacher'
        createdUserLogin(USER_1_EMAIL,USER_1_PASSWORD)

        and: "a single selection question"
        questionDto.getQuestionDetailsDto().setChoiceType(ChoiceType.SINGLE_SELECTION)

        and: 'prepare request response'
        restClient.handler.failure = { resp, reader ->
            [response:resp, reader:reader]
        }
        restClient.handler.success = { resp, reader ->
            [response:resp, reader:reader]
        }

        when: "the web service is invoked"
        map = restClient.get(
                path: "/courses/" + courseExecution.getId() + "/questions/export",
                requestContentType: "application/json"
        )

        then: "the response status is OK"
        map['response'].status == HttpStatus.SC_OK
        map['reader'] != null
    }

    def "export course multiple selection questions with assigned teacher"() {
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
        map = restClient.get(
                path: "/courses/" + courseExecution.getId() + "/questions/export",
                requestContentType: "application/json"
        )

        then: "the response status is OK"
        map['response'].status == HttpStatus.SC_OK
        map['reader'] != null
    }

    def "export course sorting questions with assigned teacher"() {
        given: 'an assigned teacher'
        createdUserLogin(USER_1_EMAIL,USER_1_PASSWORD)

        and: "a sorting question"
        questionDto.getQuestionDetailsDto().getOptions().get(0).setOrder(null)
        questionDto.getQuestionDetailsDto().getOptions().get(1).setOrder(null)

        and: 'prepare request response'
        restClient.handler.failure = { resp, reader ->
            [response:resp, reader:reader]
        }
        restClient.handler.success = { resp, reader ->
            [response:resp, reader:reader]
        }

        when: "the web service is invoked"
        map = restClient.get(
                path: "/courses/" + courseExecution.getId() + "/questions/export",
                requestContentType: "application/json"
        )

        then: "the response status is OK"
        map['response'].status == HttpStatus.SC_OK
        map['reader'] != null
    }

    def "cannot export questions without a teacher logged in"() {
        given: 'prepare request response'
        restClient.handler.failure = { resp, reader ->
            [response:resp, reader:reader]
        }
        restClient.handler.success = { resp, reader ->
            [response:resp, reader:reader]
        }

        when: "the web service is invoked"
        map = restClient.get(
                path: "/courses/" + courseExecution.getId() + "/questions/export",
                requestContentType: "application/json"
        )

        then: "error 403 is thrown"
        map['response'].status == HttpStatus.SC_FORBIDDEN
    }

    def "student cannot export questions"() {
        given: "a student logged in"
        createdUserLogin(USER_2_USERNAME, USER_2_PASSWORD)

        and: 'prepare request response'
        restClient.handler.failure = { resp, reader ->
            [response:resp, reader:reader]
        }
        restClient.handler.success = { resp, reader ->
            [response:resp, reader:reader]
        }

        when: "the web service is invoked"
        map = restClient.get(
                path: "/courses/" + courseExecution.getId() + "/questions/export",
                requestContentType: "application/json"
        )

        then: "error 403 is thrown"
        map['response'].status == HttpStatus.SC_FORBIDDEN
    }

    def "teacher not assigned to course cannot export questions"() {
        given: "a demo teacher logged in"
        demoTeacherLogin()

        and: 'prepare request response'
        restClient.handler.failure = { resp, reader ->
            [response:resp, reader:reader]
        }
        restClient.handler.success = { resp, reader ->
            [response:resp, reader:reader]
        }

        when: "the web service is invoked"
        map = restClient.get(
                path: "/courses/" + courseExecution.getId() + "/questions/export",
                requestContentType: "application/json"
        )

        then: "error 403 is thrown"
        map['response'].status == HttpStatus.SC_FORBIDDEN
    }

    def cleanup() {
        persistentCourseCleanup()

        userRepository.deleteById(teacher.getId())
        userRepository.deleteById(student.getId())
        courseExecutionRepository.deleteById(courseExecution.getId())

        courseRepository.deleteById(course.getId())
    }
}