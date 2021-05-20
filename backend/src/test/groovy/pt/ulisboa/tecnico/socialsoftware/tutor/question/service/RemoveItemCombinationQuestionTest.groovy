package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Item
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemGroup
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.questionsubmission.domain.QuestionSubmission
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@DataJpaTest
class RemoveItemCombinationQuestionTest extends SpockTest {
    def question
    def item1
    def item2
    def item3
    def itemGroup1
    def itemGroup2
    def itemGroups
    def questionDetails

    def setup() {

        def image = new Image()
        image.setUrl(IMAGE_1_URL)
        image.setWidth(20)
        imageRepository.save(image)

        question = new Question()
        question.setCourse(externalCourse)
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        question.setImage(image)
        questionDetails = new ItemCombinationQuestion()

        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        item1 = new Item()
        item1.setContent(OPTION_1_CONTENT)
        item1.setSequence(0)
        itemRepository.save(item1)
       
        item3 = new Item()
        item3.setContent(OPTION_1_CONTENT)
        item3.setSequence(1)
        itemRepository.save(item3)

        itemGroup1 = new ItemGroup()
        itemGroup1.getItems().add(item1)
        itemGroup1.getItems().add(item3)
        itemGroup1.setSequence(0)
        itemGroup1.setQuestionDetails(questionDetails)
        itemGroupRepository.save(itemGroup1)

        item2 = new Item()
        item2.setContent(OPTION_2_CONTENT)
        item2.setSequence(0)
        def combinations = new HashSet<Integer>()
        combinations.add(item1.getSequence())
        item2.setCombinations(combinations)
        itemRepository.save(item2)

        itemGroup2 = new ItemGroup()
        itemGroup2.getItems().add(item2)
        itemGroup2.setSequence(1)
        itemGroup2.setQuestionDetails(questionDetails)
        itemGroupRepository.save(itemGroup2)

        questionDetails.getItemGroups().add(itemGroup1)
        questionDetails.getItemGroups().add(itemGroup2)
    }

    def "remove an item combination question"() {
        when:
        questionService.removeQuestion(question.getId())

        then: "the question is removeQuestion"
        questionRepository.count() == 0L
        imageRepository.count() == 0L
        itemGroupRepository.count() == 0L
        itemRepository.count() == 0L
    }

    def "remove an item combination question used in a quiz"() {
        given: "a question with answers"
        Quiz quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setAvailableDate(LOCAL_DATE_BEFORE)
        quiz.setCourseExecution(externalCourseExecution)
        quiz.setOneWay(true)
        quizRepository.save(quiz)

        QuizQuestion quizQuestion= new QuizQuestion()
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)
        quizQuestionRepository.save(quizQuestion)

        when:
        questionService.removeQuestion(question.getId())

        then: "the question an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.QUESTION_IS_USED_IN_QUIZ
    }

    def "remove an item combination question that has topics"() {
        given: 'a question with topics'
        def topicDto = new TopicDto()
        topicDto.setName("name1")
        def topicOne = new Topic(externalCourse, topicDto)
        topicDto.setName("name2")
        def topicTwo = new Topic(externalCourse, topicDto)
        question.getTopics().add(topicOne)
        topicOne.getQuestions().add(question)
        question.getTopics().add(topicTwo)
        topicTwo.getQuestions().add(question)
        topicRepository.save(topicOne)
        topicRepository.save(topicTwo)

        when:
        questionService.removeQuestion(question.getId())

        then:
        questionRepository.count() == 0L
        imageRepository.count() == 0L
        itemGroupRepository.count() == 0L
        itemRepository.count() == 0L
        topicRepository.count() == 2L
        topicOne.getQuestions().size() == 0
        topicTwo.getQuestions().size() == 0
    }

    def "remove an item combination question that was submitted"() {
        given: "a student"
        def student = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        userRepository.save(student)

        and: "a questionSubmission"
        def questionSubmission = new QuestionSubmission()
        questionSubmission.setQuestion(question)
        questionSubmission.setSubmitter(student)
        questionSubmission.setCourseExecution(externalCourseExecution)
        questionSubmissionRepository.save(questionSubmission)

        when:
        questionService.removeQuestion(question.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.CANNOT_DELETE_SUBMITTED_QUESTION
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
