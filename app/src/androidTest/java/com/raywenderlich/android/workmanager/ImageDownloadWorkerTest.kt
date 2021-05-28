package com.raywenderlich.android.workmanager

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.testing.TestListenableWorkerBuilder
import com.raywenderlich.android.workmanager.workers.ImageDownloadWorker
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class ImageDownloadWorkerTest {

    // 1. You get InstantTaskExecutorRule, which swaps the background executor used by the
    // Architecture Components with a different one that executes each task synchronously.
    // You also get WorkManagerTestRule, which initializes WorkManager and also provides a context.
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var workerManagerTestRule = WorkManagerTestRule()

    // 2. This is the actual test, which is to test your ImageDownloadWorker. You’re using
    // TestListenableWorkerBuilder, which runs your Worker. In this case, your Worker is a CoroutineWorker.
    @Test
    fun testDownloadWork() {
        // Create Work Request
        val work = TestListenableWorkerBuilder<ImageDownloadWorker>(workerManagerTestRule.targetContext).build()
        runBlocking {
            val result = work.doWork()
            // Assert
            Assert.assertNotNull(result)
        }
    }

    // Note: WorkManager tests need the Android platform to run.
    // This is the reason your tests are in the androidTest directory.
}
