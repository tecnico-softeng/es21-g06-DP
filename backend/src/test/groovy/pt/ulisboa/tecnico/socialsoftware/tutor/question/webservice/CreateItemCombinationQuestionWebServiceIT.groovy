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
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateItemCombinationQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def course
    def courseExecution
    def teacher
    def student
    def questionDto
    def incorrectQuestionDto
    def response
    def itemDto1
    def itemDto2

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

        questionDto = new QuestionDto()
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)

        itemDto1 = new ItemDto()
        itemDto2 = new ItemDto()
        itemDto1.setContent(OPTION_1_CONTENT)
        itemDto2.setContent(OPTION_2_CONTENT)
        itemDto1.setSequence(1);
        itemDto2.setSequence(1);

        itemDto1.getCombinations().add(1)

        def itemGroupDto1 = new ItemGroupDto()
        itemGroupDto1.getItems().add(itemDto1)
        itemGroupDto1.setSequence(0)

        def itemGroupDto2 = new ItemGroupDto()
        itemGroupDto2.getItems().add(itemDto2)
        itemGroupDto2.setSequence(1)

        questionDto.getQuestionDetailsDto().getItemGroups().add(itemGroupDto1)
        questionDto.getQuestionDetailsDto().getItemGroups().add(itemGroupDto2)

        incorrectQuestionDto = new QuestionDto()
        incorrectQuestionDto.setTitle(QUESTION_2_TITLE)
        incorrectQuestionDto.setContent(QUESTION_2_CONTENT)
        incorrectQuestionDto.setStatus(Question.Status.AVAILABLE.name())
        incorrectQuestionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto ())
    }

    def "create item combination question for course execution"() {   
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
        response.status == 200
        and: "if it responds with the correct question"
        def respResult = response.data
        respResult.id != null
        respResult.title == questionDto.getTitle()
        respResult.content == questionDto.getContent()
        respResult.status == Question.Status.AVAILABLE.name()
        respResult.questionDetailsDto.type == "item_combination"
        respResult.questionDetailsDto.itemGroups.size() == 2
        respResult.questionDetailsDto.itemGroups.get(0).items.size() == 1
        respResult.questionDetailsDto.itemGroups.get(0).items.get(0).content == itemDto1.getContent()
        respResult.questionDetailsDto.itemGroups.get(0).items.get(0).combinations.size() == 1
        respResult.questionDetailsDto.itemGroups.get(0).items.get(0).combinations.contains(1)
        and: "if the correct question is in the repository"
        questionRepository.count() == 1L
        def repoResult = questionRepository.findAll().get(0)
        repoResult.getId() != null
        repoResult.getTitle() == questionDto.getTitle()
        repoResult.getContent() == questionDto.getContent()
        repoResult.getStatus() == Question.Status.AVAILABLE
        def repoItemComb = questionService.findQuestionById(repoResult.id)
        repoItemComb.questionDetailsDto.itemGroups.size() == 2
        repoItemComb.questionDetailsDto.itemGroups.get(0).items.size() == 1
        repoItemComb.questionDetailsDto.itemGroups.get(0).items.get(0).content == itemDto1.getContent()
        repoItemComb.questionDetailsDto.itemGroups.get(0).items.get(0).combinations.size() == 1
        repoItemComb.questionDetailsDto.itemGroups.get(0).items.get(0).combinations.contains(1)
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
        e.response.data["message"] == ErrorMessage.TWO_AND_ONLY_TWO_ITEM_GROUPS_NEEDED.label
    }


    def "cannot create item combination question for course execution without a user logged in"() {
        given: "no user logged in"     

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
    }

    def "cannot create item combination question for course execution as a student"() {
        given: "a student logged in"     
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)   

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
    }

    def "cannot create item combination question for course execution as a teacher not assigned to course"() {
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
    }

    def cleanup() {
        persistentCourseCleanup()

        userRepository.deleteById(teacher.getId())
        userRepository.deleteById(student.getId())
        courseExecutionRepository.deleteById(courseExecution.getId())

        courseRepository.deleteById(course.getId())
    }
}
