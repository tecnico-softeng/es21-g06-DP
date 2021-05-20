package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import com.fasterxml.jackson.databind.ObjectMapper
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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UpdateMultipleChoiceQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def course
    def courseExecution
    def teacher
    def questionDtoSingle
    def questionDtoMultiple
    def questionDtoSorting
    def questionSingle
    def questionMultiple
    def questionSorting
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

        questionDtoSingle = new QuestionDto()
        questionDtoSingle.setKey(1)
        questionDtoSingle.setTitle(QUESTION_1_TITLE)
        questionDtoSingle.setContent(QUESTION_1_CONTENT)
        questionDtoSingle.setStatus(Question.Status.AVAILABLE.name())
        questionDtoSingle.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDtoSingle.getQuestionDetailsDto().setChoiceType(ChoiceType.SINGLE_SELECTION)

        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(false)
        options.add(optionDto)
        questionDtoSingle.getQuestionDetailsDto().setOptions(options)

        questionSingle = new Question(course, questionDtoSingle)
        questionDetailsRepository.save(questionSingle.getQuestionDetails())
        questionRepository.save(questionSingle)

        questionDtoMultiple = new QuestionDto()
        questionDtoMultiple.setKey(2)
        questionDtoMultiple.setTitle(QUESTION_1_TITLE)
        questionDtoMultiple.setContent(QUESTION_1_CONTENT)
        questionDtoMultiple.setStatus(Question.Status.AVAILABLE.name())
        questionDtoMultiple.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDtoMultiple.getQuestionDetailsDto().setChoiceType(ChoiceType.MULTIPLE_SELECTION)

        optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(false)
        options.add(optionDto)
        questionDtoMultiple.getQuestionDetailsDto().setOptions(options)

        questionMultiple = new Question(course, questionDtoMultiple)
        questionDetailsRepository.save(questionMultiple.getQuestionDetails())
        questionRepository.save(questionMultiple)

        questionDtoSorting = new QuestionDto()
        questionDtoSorting.setKey(3)
        questionDtoSorting.setTitle(QUESTION_1_TITLE)
        questionDtoSorting.setContent(QUESTION_1_CONTENT)
        questionDtoSorting.setStatus(Question.Status.AVAILABLE.name())
        questionDtoSorting.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDtoSorting.getQuestionDetailsDto().setChoiceType(ChoiceType.SORTING)

        optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(2)
        options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(false)
        optionDto.setOrder(1)
        options.add(optionDto)
        questionDtoSorting.getQuestionDetailsDto().setOptions(options)

        questionSorting = new Question(course, questionDtoSorting)
        questionDetailsRepository.save(questionSorting.getQuestionDetails())
        questionRepository.save(questionSorting)
    }

    def "updates single selection multiple choice question"() {
        given: "a teacher logged in"
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)
        
        and: "an updated question"
        questionDtoSingle.setTitle(QUESTION_2_TITLE)

        def option = questionSingle.getQuestionDetails().getOptions().get(0)
        option.setCorrect(false)
        option.setContent(OPTION_2_CONTENT)
        def options = new ArrayList<OptionDto>()
        options.add(new OptionDto(option))
        def option2 = questionSingle.getQuestionDetails().getOptions().get(1)
        option2.setCorrect(true)
        option2.setContent(OPTION_1_CONTENT)
        options.add(new OptionDto(option2))
        questionDtoSingle.getQuestionDetailsDto().setOptions(options)

        when:
        def mapper = new ObjectMapper()
        response = restClient.put(
                path: '/questions/' + questionSingle.getId(),
                body: mapper.writeValueAsString(questionDtoSingle),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == HttpStatus.SC_OK

        and: "question remains existent"
        questionRepository.count() == 3L

        and: "the correct data is sent"
        def question = response.data
        question.id != null
        question.status == Question.Status.AVAILABLE.name()
        question.title == QUESTION_2_TITLE
        question.content == QUESTION_1_CONTENT

        question.questionDetailsDto.options.size() == 2
        def responseOption = question.questionDetailsDto.options.get(0)
        !responseOption.correct
        responseOption.content == OPTION_2_CONTENT
        def responseOption2 = question.questionDetailsDto.options.get(1)
        responseOption2.correct
        responseOption2.content == OPTION_1_CONTENT

        and: "the question is changed"
        def result = questionService.findQuestionByKey(1)
        result.getId() != null
        result.getStatus() == Question.Status.AVAILABLE.name()
        result.getTitle() == QUESTION_2_TITLE
        result.questionDetailsDto.options.size() == 2

        def resOption = result.questionDetailsDto.options.get(0)
        resOption.getContent() == OPTION_2_CONTENT
        !resOption.isCorrect()

        def resOption2 = result.questionDetailsDto.options.get(1)
        resOption2.getContent() == OPTION_1_CONTENT
        resOption2.isCorrect()
    }

    def "updates multiple selection multiple choice question"() {
        given: "a teacher logged in"
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)
        
        and: "an updated question"
        questionDtoMultiple.setTitle(QUESTION_2_TITLE)

        def option = questionMultiple.getQuestionDetails().getOptions().get(0)
        option.setCorrect(false)
        option.setContent(OPTION_2_CONTENT)
        def options = new ArrayList<OptionDto>()
        options.add(new OptionDto(option))
        def option2 = questionMultiple.getQuestionDetails().getOptions().get(1)
        option2.setCorrect(true)
        option2.setContent(OPTION_1_CONTENT)
        options.add(new OptionDto(option2))
        questionDtoMultiple.getQuestionDetailsDto().setOptions(options)

        when:
        def mapper = new ObjectMapper()
        response = restClient.put(
                path: '/questions/' + questionMultiple.getId(),
                body: mapper.writeValueAsString(questionDtoMultiple),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == HttpStatus.SC_OK

        and: "question remains existent"
        questionRepository.count() == 3L

        and: "the correct data is sent"
        def question = response.data
        question.id != null
        question.status == Question.Status.AVAILABLE.name()
        question.title == QUESTION_2_TITLE
        question.content == QUESTION_1_CONTENT

        question.questionDetailsDto.options.size() == 2
        def responseOption = question.questionDetailsDto.options.get(0)
        !responseOption.correct
        responseOption.content == OPTION_2_CONTENT
        def responseOption2 = question.questionDetailsDto.options.get(1)
        responseOption2.correct
        responseOption2.content == OPTION_1_CONTENT

        and: "the question is changed"
        def result = questionService.findQuestionByKey(2)
        result.getId() != null
        result.getStatus() == Question.Status.AVAILABLE.name()
        result.getTitle() == QUESTION_2_TITLE

        def resOption = result.questionDetailsDto.options.get(0)
        resOption.getContent() == OPTION_2_CONTENT
        !resOption.isCorrect()

        def resOption2 = result.questionDetailsDto.options.get(1)
        resOption2.getContent() == OPTION_1_CONTENT
        resOption2.isCorrect()
    }

    def "updates sorting multiple choice question"() {
        given: "a teacher logged in"
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)
        
        and: "an updated question"
        questionDtoSorting.setTitle(QUESTION_2_TITLE)

        def option = questionSorting.getQuestionDetails().getOptions().get(0)
        option.setOrder(1)
        option.setContent(OPTION_2_CONTENT)
        def options = new ArrayList<OptionDto>()
        options.add(new OptionDto(option))
        def option2 = questionSorting.getQuestionDetails().getOptions().get(1)
        option2.setOrder(2)
        option2.setContent(OPTION_1_CONTENT)
        options.add(new OptionDto(option2))
        questionDtoSorting.getQuestionDetailsDto().setOptions(options)

        when:
        def mapper = new ObjectMapper()
        response = restClient.put(
                path: '/questions/' + questionSorting.getId(),
                body: mapper.writeValueAsString(questionDtoSorting),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == HttpStatus.SC_OK

        and: "question remains existent"
        questionRepository.count() == 3L

        and: "the correct data is sent"
        def question = response.data
        question.id != null
        question.status == Question.Status.AVAILABLE.name()
        question.title == QUESTION_2_TITLE
        question.content == QUESTION_1_CONTENT

        question.questionDetailsDto.options.size() == 2
        def responseOption = question.questionDetailsDto.options.get(0)
        responseOption.order == 1
        responseOption.content == OPTION_2_CONTENT
        def responseOption2 = question.questionDetailsDto.options.get(1)
        responseOption2.order == 2
        responseOption2.content == OPTION_1_CONTENT

        and: "the question is changed"
        def result = questionService.findQuestionByKey(3)
        result.getId() != null
        result.getStatus() == Question.Status.AVAILABLE.name()
        result.getTitle() == QUESTION_2_TITLE
        result.questionDetailsDto.options.size() == 2

        def resOption = result.questionDetailsDto.options.get(0)
        resOption.getContent() == OPTION_2_CONTENT
        resOption.getOrder() == 1

        def resOption2 = result.questionDetailsDto.options.get(1)
        resOption2.getContent() == OPTION_1_CONTENT
        resOption2.getOrder() == 2
    }

    def "updates multiple selection multiple choice question into sorting question"() {
        given: "a teacher logged in"
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)
        
        and: "an updated question"
        questionDtoMultiple.setTitle(QUESTION_2_TITLE)
        questionDtoMultiple.getQuestionDetailsDto().setChoiceType(ChoiceType.SORTING)

        def option = questionMultiple.getQuestionDetails().getOptions().get(0)
        option.setOrder(1)
        option.setContent(OPTION_2_CONTENT)
        def options = new ArrayList<OptionDto>()
        options.add(new OptionDto(option))
        def option2 = questionMultiple.getQuestionDetails().getOptions().get(1)
        option2.setOrder(2)
        option2.setContent(OPTION_1_CONTENT)
        options.add(new OptionDto(option2))
        questionDtoMultiple.getQuestionDetailsDto().setOptions(options)

        when:
        def mapper = new ObjectMapper()        
        response = restClient.put(
                path: '/questions/' + questionMultiple.getId(),
                body: mapper.writeValueAsString(questionDtoMultiple),
                requestContentType: 'application/json'
        )
        then: "check the response status"
        response != null
        response.status == HttpStatus.SC_OK

        and: "question remains existent"
        questionRepository.count() == 3L

        and: "the correct data is sent"
        def question = response.data
        question.id != null
        question.status == Question.Status.AVAILABLE.name()
        question.title == QUESTION_2_TITLE
        question.content == QUESTION_1_CONTENT

        question.questionDetailsDto.options.size() == 2
        def responseOption = question.questionDetailsDto.options.get(0)
        responseOption.order == 1
        responseOption.correct
        responseOption.content == OPTION_2_CONTENT
        def responseOption2 = question.questionDetailsDto.options.get(1)
        responseOption2.order == 2
        !responseOption2.correct
        responseOption2.content == OPTION_1_CONTENT

        and: "the question is changed"
        def result = questionService.findQuestionByKey(2)
        result.getId() != null
        result.getStatus() == Question.Status.AVAILABLE.name()
        result.getTitle() == QUESTION_2_TITLE
        result.questionDetailsDto.options.size() == 2

        def resOption = result.questionDetailsDto.options.get(0)
        resOption.getContent() == OPTION_2_CONTENT
        resOption.getOrder() == 1

        def resOption2 = result.questionDetailsDto.options.get(1)
        resOption2.getContent() == OPTION_1_CONTENT
        resOption2.getOrder() == 2
    }

    def "cannot update multiple choice question for course execution without a teacher logged in"() {
        given: "A changed question"
        questionDtoMultiple.setTitle(QUESTION_2_TITLE)

        when:
        def mapper = new ObjectMapper()
        response = restClient.put(
                path: '/questions/' + questionMultiple.getId(),
                body: mapper.writeValueAsString(questionDtoMultiple),
                requestContentType: 'application/json'
        )

        then: "error 403 is thrown"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

        and: "question remains existent"
        questionRepository.count() == 3L

        and: "the question is unchanged"
        def result = questionService.findQuestionByKey(2)
        result.getId() != null
        result.getStatus() == Question.Status.AVAILABLE.name()
        result.getTitle() == QUESTION_1_TITLE
        result.questionDetailsDto.options.size() == 2

        def resOption = result.questionDetailsDto.options.get(0)
        resOption.getContent() == OPTION_1_CONTENT
        resOption.isCorrect()

        def resOption2 = result.questionDetailsDto.options.get(1)
        resOption2.getContent() == OPTION_2_CONTENT
        !resOption2.isCorrect()

    }

    def "student cannot update question"() {
        given: "a student logged in"
        createdUserLogin(USER_2_USERNAME, USER_2_PASSWORD)

        and: "A changed question"
        questionDtoMultiple.setTitle(QUESTION_2_TITLE)

        when:
        def mapper = new ObjectMapper()
        response = restClient.put(
                path: '/questions/' + questionMultiple.getId(),
                body: mapper.writeValueAsString(questionDtoMultiple),
                requestContentType: 'application/json'
        )

        then: "error 403 is thrown"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

        and: "question remains existent"
        questionRepository.count() == 3L

        and: "the question is unchanged"
        def result = questionService.findQuestionByKey(2)
        result.getId() != null
        result.getStatus() == Question.Status.AVAILABLE.name()
        result.getTitle() == QUESTION_1_TITLE
        result.questionDetailsDto.options.size() == 2

        def resOption = result.questionDetailsDto.options.get(0)
        resOption.getContent() == OPTION_1_CONTENT
        resOption.isCorrect()

        def resOption2 = result.questionDetailsDto.options.get(1)
        resOption2.getContent() == OPTION_2_CONTENT
        !resOption2.isCorrect()
    }

    def "teacher not assigned to course cannot update question"() {
        given: "a demo teacher logged in"
        demoTeacherLogin()

        and: "A changed question"
        questionDtoMultiple.setTitle(QUESTION_2_TITLE)

        when:
        def mapper = new ObjectMapper()
        response = restClient.put(
                path: '/questions/' + questionMultiple.getId(),
                body: mapper.writeValueAsString(questionDtoMultiple),
                requestContentType: 'application/json'
        )

        then: "error 403 is thrown"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

        and: "question remains existent"
        questionRepository.count() == 3L

        and: "the question is unchanged"
        def result = questionService.findQuestionByKey(2)
        result.getId() != null
        result.getStatus() == Question.Status.AVAILABLE.name()
        result.getTitle() == QUESTION_1_TITLE
        result.questionDetailsDto.options.size() == 2

        def resOption = result.questionDetailsDto.options.get(0)
        resOption.getContent() == OPTION_1_CONTENT
        resOption.isCorrect()

        def resOption2 = result.questionDetailsDto.options.get(1)
        resOption2.getContent() == OPTION_2_CONTENT
        !resOption2.isCorrect()
    }


    def cleanup() {
        persistentCourseCleanup()

        userRepository.deleteById(teacher.getId())
        userRepository.deleteById(student.getId())
        courseExecutionRepository.deleteById(courseExecution.getId())

        courseRepository.deleteById(course.getId())
    }
}
