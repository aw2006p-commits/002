package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.LessonCategory
import com.example.data.model.SheikhData
import com.example.data.repository.QuizDataProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("الشيخ سمير مصطفى", appName)
  }

  @Test
  fun `verify lessons and quotes count`() {
    assertTrue(SheikhData.allLessons.isNotEmpty())
    assertTrue(SheikhData.quotes.isNotEmpty())
    assertTrue(SheikhData.seriesList.isNotEmpty())
  }

  @Test
  fun `verify every lesson has a valid quiz generated`() {
    for (lesson in SheikhData.allLessons) {
      val quiz = QuizDataProvider.getQuizForLesson(lesson)
      assertNotNull(quiz)
      assertEquals(lesson.id, quiz.lessonId)
      assertTrue("Quiz should have questions for lesson ${lesson.id}", quiz.questions.isNotEmpty())
      for (question in quiz.questions) {
        assertTrue("Question text should not be empty", question.question.isNotBlank())
        assertTrue("Question should have options", question.options.size >= 2)
        assertTrue("Correct index in valid range", question.correctIndex in question.options.indices)
        assertTrue("Explanation should not be empty", question.explanation.isNotBlank())
      }
    }
  }
}
