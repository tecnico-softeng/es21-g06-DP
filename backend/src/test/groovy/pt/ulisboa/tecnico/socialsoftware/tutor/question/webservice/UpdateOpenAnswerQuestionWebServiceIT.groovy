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
class UpdateOpenAnswerQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def course
    def courseExecution
    def assignedTeacher
    def student
    def response
    def questionDto
    def updatedQuestionDto
    def incorrectQuestionDto
    def createdQuestion

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
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
        questionDto.getQuestionDetailsDto().setCorrectAnswer(OPEN_ANSWER_CORRECT_ANSWER)

        createdQuestion = questionService.createQuestion(course.getId(), questionDto)

        updatedQuestionDto = new QuestionDto()
        updatedQuestionDto.setKey(2)
        updatedQuestionDto.setTitle(QUESTION_2_TITLE)
        updatedQuestionDto.setContent(QUESTION_2_CONTENT)
        updatedQuestionDto.setStatus(Question.Status.AVAILABLE.name())
        updatedQuestionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
        updatedQuestionDto.getQuestionDetailsDto().setCorrectAnswer(OPEN_ANSWER_CORRECT_ANSWER2)

        incorrectQuestionDto = new QuestionDto()
        incorrectQuestionDto.setTitle(QUESTION_2_TITLE)
        incorrectQuestionDto.setContent(QUESTION_2_CONTENT)
        incorrectQuestionDto.setStatus(Question.Status.AVAILABLE.name())
        incorrectQuestionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
    }

    def "update open answer question"(){
        given: "an assigned teacher"
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        def mapper = new ObjectMapper()
        when:
        response = restClient.put(path: "/questions/" + createdQuestion.getId(),
                body: mapper.writeValueAsString(updatedQuestionDto),
                requestContentType: 'application/json')

        then: "question is updated"
        response != null
        response.status == HttpStatus.SC_OK
        and: "the question is in the repository"
        questionRepository.count() == 1L
        questionDetailsRepository.count() == 1L
        and: "the question is correctly updated in the repository"
        def result = questionService.findQuestionByKey(1)
        result.getId() != null
        result.getStatus() == Question.Status.AVAILABLE.name()
        result.getTitle() == QUESTION_2_TITLE
        result.getContent() == QUESTION_2_CONTENT
        result.getQuestionDetailsDto().getCorrectAnswer() == OPEN_ANSWER_CORRECT_ANSWER2
        and: "the returned fields are correct"
        def data = response.data
        data.title == QUESTION_2_TITLE
        data.content == QUESTION_2_CONTENT
        data.status == Question.Status.AVAILABLE.name()
        data.questionDetailsDto.type == Question.QuestionTypes.OPEN_ANSWER_QUESTION
        data.questionDetailsDto.correctAnswer == OPEN_ANSWER_CORRECT_ANSWER2
    }

    def "cant update to an incorrect open answer question"(){
        given: "an assigned teacher"
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        def mapper = new ObjectMapper()
        when:
        response = restClient.put(path: "/questions/" + createdQuestion.getId(),
                body: mapper.writeValueAsString(incorrectQuestionDto),
                requestContentType: 'application/json')

        then:
        HttpResponseException e = thrown(HttpResponseException)
        e.response.data["message"] == ErrorMessage.NO_CORRECT_ANSWER.label
        and: "the question is unchanged in the repository"
        def result = questionService.findQuestionByKey(1)
        result.getId() != null
        result.getStatus() == Question.Status.AVAILABLE.name()
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getQuestionDetailsDto().getCorrectAnswer() == OPEN_ANSWER_CORRECT_ANSWER
    }

    def "cant update open answer question with unassigned teacher"(){
        given: "an unassigned teacher"
        demoTeacherLogin()

        def mapper = new ObjectMapper()
        when:
        response = restClient.put(path: "/questions/" + createdQuestion.getId(),
                body: mapper.writeValueAsString(updatedQuestionDto),
                requestContentType: 'application/json')
        then:
        HttpResponseException e = thrown(HttpResponseException)
        e.response.status == HttpStatus.SC_FORBIDDEN
        and: "the question is unchanged in the repository"
        def result = questionService.findQuestionByKey(1)
        result.getId() != null
        result.getStatus() == Question.Status.AVAILABLE.name()
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getQuestionDetailsDto().getCorrectAnswer() == OPEN_ANSWER_CORRECT_ANSWER
    }

    def "cant update open answer question with student"(){
        given: "an enrolled student"
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        def mapper = new ObjectMapper()
        when:
        response = restClient.put(path: "/questions/" + createdQuestion.getId(),
                body: mapper.writeValueAsString(updatedQuestionDto),
                requestContentType: 'application/json')
        then:
        HttpResponseException e = thrown(HttpResponseException)
        e.response.status == HttpStatus.SC_FORBIDDEN
        and: "the question is unchanged in the repository"
        def result = questionService.findQuestionByKey(1)
        result.getId() != null
        result.getStatus() == Question.Status.AVAILABLE.name()
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getQuestionDetailsDto().getCorrectAnswer() == OPEN_ANSWER_CORRECT_ANSWER
    }


    def cleanup() {
        persistentCourseCleanup()
        userRepository.deleteById(assignedTeacher.getId())
        userRepository.deleteById(student.getId())

        courseExecutionRepository.deleteById(courseExecution.getId())

        courseRepository.deleteById(course.getId())
    }
}