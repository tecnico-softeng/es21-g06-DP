package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import groovyx.net.http.RESTClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import groovyx.net.http.HttpResponseException
import org.apache.http.HttpStatus
import com.fasterxml.jackson.databind.ObjectMapper;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenAnswerQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateOpenAnswerQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def course
    def courseExecution
    def assignedTeacher
    def response
    def response2
    def questionDto
    def questionDto2
    def incorrectQuestionDto
    def student

    def setup(){
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

        student = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        student.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        student.addCourse(courseExecution)
        courseExecution.addUser(student)
        userRepository.save(student)

        questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto ())
        questionDto.getQuestionDetailsDto().setCorrectAnswer(OPEN_ANSWER_CORRECT_ANSWER)

        questionDto2 = new QuestionDto()
        questionDto2.setKey(2)
        questionDto2.setTitle(QUESTION_2_TITLE)
        questionDto2.setContent(QUESTION_2_CONTENT)
        questionDto2.setStatus(Question.Status.AVAILABLE.name())
        questionDto2.setQuestionDetailsDto(new OpenAnswerQuestionDto ())
        questionDto2.getQuestionDetailsDto().setCorrectAnswer(OPEN_ANSWER_CORRECT_ANSWER2)

        incorrectQuestionDto = new QuestionDto()
        incorrectQuestionDto.setTitle(QUESTION_2_TITLE)
        incorrectQuestionDto.setContent(QUESTION_2_CONTENT)
        incorrectQuestionDto.setStatus(Question.Status.AVAILABLE.name())
        incorrectQuestionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto ())
    }

    def "create open answer question"(){
        given:
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        def mapper = new ObjectMapper()
        when:
        response = restClient.post(path: "/courses/" + course.getId() + "/questions",
                                    body: mapper.writeValueAsString(questionDto),
                                    requestContentType: 'application/json')

        then:
        response != null
        response.status == HttpStatus.SC_OK
        and: "the correct questions is created"
        questionRepository.count() == 1L
        questionDetailsRepository.count() == 1L
        and: "the question is in the repository"
        def result = questionService.findQuestionByKey(1)
        result.getId() != null
        result.getStatus() == Question.Status.AVAILABLE.name()
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        and: "the question fields are correct"
        def data = response.data
        data.title == QUESTION_1_TITLE
        data.content == QUESTION_1_CONTENT
        data.status == Question.Status.AVAILABLE.name()
        data.questionDetailsDto.type == Question.QuestionTypes.OPEN_ANSWER_QUESTION
        data.questionDetailsDto.correctAnswer == OPEN_ANSWER_CORRECT_ANSWER
    }

    def "create two open answer questions"(){
        given:
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        def mapper = new ObjectMapper()

        when:
        response = restClient.post(path: "/courses/" + course.getId() + "/questions",
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json')
        response2 = restClient.post(path: "/courses/" + course.getId() + "/questions",
                body: mapper.writeValueAsString(questionDto2),
                requestContentType: 'application/json')

        then:
        response != null
        response2 != null
        response.status == HttpStatus.SC_OK
        response2.status == HttpStatus.SC_OK
        and: "the correct question is created"
        questionRepository.count() == 2L
        questionDetailsRepository.count() == 2L
        and: "the first question is in the repository"
        def result = questionService.findQuestionByKey(1)
        result.getId() != null
        result.getStatus() == "AVAILABLE"
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        and: "the second question is in the repository"
        def resultTwo = questionService.findQuestionByKey(2)
        resultTwo.getId() != null
        resultTwo.getStatus() == "AVAILABLE"
        resultTwo.getTitle() == QUESTION_2_TITLE
        resultTwo.getContent() == QUESTION_2_CONTENT
        and: "the first question fields are correct"
        def data = response.data
        data.title == QUESTION_1_TITLE
        data.content == QUESTION_1_CONTENT
        data.status == Question.Status.AVAILABLE.name()
        data.questionDetailsDto.type == Question.QuestionTypes.OPEN_ANSWER_QUESTION
        data.questionDetailsDto.correctAnswer == OPEN_ANSWER_CORRECT_ANSWER
        and: "the second question fields are correct"
        def data2 = response2.data
        data2.title == QUESTION_2_TITLE
        data2.content == QUESTION_2_CONTENT
        data2.status == Question.Status.AVAILABLE.name()
        data2.questionDetailsDto.type == Question.QuestionTypes.OPEN_ANSWER_QUESTION
        data2.questionDetailsDto.correctAnswer == OPEN_ANSWER_CORRECT_ANSWER2
    }

    def "cant create an incorrect open answer question"(){
        given:
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        def mapper = new ObjectMapper()

        when:
        response = restClient.post(path: "/courses/" + course.getId() + "/questions",
                body: mapper.writeValueAsString(incorrectQuestionDto),
                requestContentType: 'application/json')

        then:
        HttpResponseException e = thrown(HttpResponseException)
        e.response.data["message"] == ErrorMessage.NO_CORRECT_ANSWER.label
        and: "no question created"
        questionRepository.count() == 0L
        questionDetailsRepository.count() == 0L
    }

    def "cant create open answer question with unassigned teacher"(){
        given: "an unassigned teacher"
        demoTeacherLogin()

        def mapper = new ObjectMapper()

        when:
        response = restClient.post(path: "/courses/" + course.getId() + "/questions",
                                    body: mapper.writeValueAsString(questionDto),
                                    requestContentType: 'application/json')
        then:
        HttpResponseException e = thrown(HttpResponseException)
        e.response.status == HttpStatus.SC_FORBIDDEN
        and: "no question created"
        questionRepository.count() == 0L
        questionDetailsRepository.count() == 0L
    }

    def "cant create open answer question with student"(){
        given: "a student"
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        def mapper = new ObjectMapper()

        when:
        response = restClient.post(path: "/courses/" + course.getId() + "/questions",
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json')
        then:
        HttpResponseException e = thrown(HttpResponseException)
        e.response.status == HttpStatus.SC_FORBIDDEN
        and: "no question created"
        questionRepository.count() == 0L
        questionDetailsRepository.count() == 0L
    }


    def cleanup() {
        persistentCourseCleanup()
        userRepository.deleteById(assignedTeacher.getId())
        userRepository.deleteById(student.getId())

        courseExecutionRepository.deleteById(courseExecution.getId())

        courseRepository.deleteById(course.getId())
    }
}