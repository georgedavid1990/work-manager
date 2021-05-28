/*
 *
 *  * Copyright (c) 2020 Razeware LLC
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 *  * distribute, sublicense, create a derivative work, and/or sell copies of the
 *  * Software in any work that is designed, intended, or marketed for pedagogical or
 *  * instructional purposes related to programming, coding, application development,
 *  * or information technology.  Permission for such use, copying, modification,
 *  * merger, publication, distribution, sublicensing, creation of derivative works,
 *  * or sale is expressly withheld.
 *  *
 *  * This project and source code may use libraries or frameworks that are
 *  * released under various Open-Source licenses. Use of those libraries and
 *  * frameworks are governed by their own individual licenses.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *
 */

package com.raywenderlich.android.workmanager

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.*
import androidx.work.testing.WorkManagerTestInitHelper
import com.raywenderlich.android.workmanager.workers.SampleWorker
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit
import org.hamcrest.CoreMatchers.`is`

class SampleWorkerTest {

  @get:Rule
  var instantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  var workerManagerTestRule = WorkManagerTestRule()

  @Test
  fun testWorkerInitialDelay() {
    val inputData = workDataOf("Worker" to "sampleWorker")
    // 1. Create your WorkRequest using OneTimeWorkRequestBuilder. You set an initial delay of
    // 10 seconds. You’re also setting input data to your request.
    val request = OneTimeWorkRequestBuilder<SampleWorker>()
      .setInitialDelay(10, TimeUnit.SECONDS)
      .setInputData(inputData)
      .build()

    // 2. Create a TestDriver, which will help in simulating the delay. You’re creating an
    // instance of WorkManager, too.
    val testDriver = WorkManagerTestInitHelper.getTestDriver(workerManagerTestRule.targetContext)
    val workManager = workerManagerTestRule.workManager

    // 3. Enqueue your request.
    workManager.enqueue(request).result.get()

    // 4. Simulate the actual delay for your work.
    testDriver?.setInitialDelayMet(request.id)

    // 5. Get work info and output data.
    val workInfo = workManager.getWorkInfoById(request.id).get()

    // 6. Do an assertion to check the succeeded state in your work.
    Assert.assertThat(workInfo.state, `is` (WorkInfo.State.SUCCEEDED))
  }

  @Test
  fun testPeriodicSampleWorker() {
    val inputData = workDataOf("Worker" to "sampleWorker")
    // 1. Create your WorkRequest using PeriodicWorkRequestBuilder, with a time interval of
    // 10 minutes. You’re also setting input data to your request.
    val request = PeriodicWorkRequestBuilder<SampleWorker>(15, TimeUnit.MINUTES)
            .setInputData(inputData)
            .build()

    // 2. Make a testDriver, which will help in simulating the time interval between each work.
    // You’re creating an instance of workManager, too.
    val testDriver = WorkManagerTestInitHelper.getTestDriver(workerManagerTestRule.targetContext)
    val workManager = workerManagerTestRule.workManager

    // 3.Enqueue your request.
    workManager.enqueue(request).result.get()

    // 4. Notify the WorkManager testing framework that the interval’s duration is complete.
    testDriver?.setPeriodDelayMet(request.id)

    // 5. Get work info and output data.
    val workInfo = workManager.getWorkInfoById(request.id).get()

    // 6. Do an assertion to check for the enqueued state for your work.
    Assert.assertThat(workInfo.state, `is`(WorkInfo.State.ENQUEUED))
  }

  @Test
  fun testAllConstraintsAreMet() {
    val inputData = workDataOf("Worker" to "sampleWorker")
    // 1. Creates your network and battery constraints.
    val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

    // 2. Makes your WorkRequest, using OneTimeWorkRequestBuilder. It sets your constraints to
    // the request. It also sets input data to your request.
    val request = OneTimeWorkRequestBuilder<SampleWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()
    val workManager = WorkManager.getInstance(workerManagerTestRule.targetContext)

    // 3. Enqueues your request
    workManager.enqueue(request).result.get()

    // 4. Simulates the network and battery constraints, using WorkManagerTestInitHelper
    WorkManagerTestInitHelper.getTestDriver(workerManagerTestRule.targetContext)
            ?.setAllConstraintsMet(request.id)

    // 5. Gets work info and output data
    val workInfo = workManager.getWorkInfoById(request.id).get()

    // 6. Does an assertion to check for the succeeded state for your work
    Assert.assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))
  }


}