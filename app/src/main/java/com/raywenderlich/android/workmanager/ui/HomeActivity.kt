/*
 * Copyright (c) 2020 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.workmanager.ui

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.work.*
import com.raywenderlich.android.workmanager.R
import com.raywenderlich.android.workmanager.databinding.ActivityHomeBinding
import com.raywenderlich.android.workmanager.workers.ImageDownloadWorker
import java.util.*
import java.util.concurrent.TimeUnit

class HomeActivity : AppCompatActivity() {

  private lateinit var activityHomeBinding: ActivityHomeBinding
  private val workManager by lazy {
    WorkManager.getInstance(applicationContext)
  }

  private val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .setRequiresStorageNotLow(true)
    .setRequiresBatteryNotLow(true)
    .build()

  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.AppTheme)
    super.onCreate(savedInstanceState)
    activityHomeBinding = ActivityHomeBinding.inflate(layoutInflater)
    setContentView(activityHomeBinding.root)
    activityHomeBinding.tvWorkInfo.visibility = View.GONE

    requestStoragePermissions()

    activityHomeBinding.btnImageDownload.setOnClickListener {
      showLottieAnimation()
      activityHomeBinding.downloadLayout.visibility = View.GONE
      createOneTimeWorkRequest()
      //createPeriodicWorkRequest()
      //createDelayedWorkRequest()
    }

    activityHomeBinding.btnQueryWork.setOnClickListener {
      queryWorkInfo()
    }

  }

  private fun createOneTimeWorkRequest() {
    val imageWorker = OneTimeWorkRequestBuilder<ImageDownloadWorker>()
      .setConstraints(constraints)
      .addTag("imageWork")
      .build()

    workManager.enqueueUniqueWork(
      "oneTimeImageDownload",
      ExistingWorkPolicy.KEEP,
      imageWorker
    )

    observeWork(imageWorker.id)
  }

  private fun createPeriodicWorkRequest() {
    val imageWorker = PeriodicWorkRequestBuilder<ImageDownloadWorker>(
      15, TimeUnit.MINUTES)
      .setConstraints(constraints)
      .addTag("imageWork")
      .build()

    workManager.enqueueUniquePeriodicWork(
      "periodicImageDownload",
      ExistingPeriodicWorkPolicy.KEEP,
      imageWorker
    )

    observeWork(imageWorker.id)
  }

  private fun createDelayedWorkRequest() {
    val imageWorker = OneTimeWorkRequestBuilder<ImageDownloadWorker>()
      .setConstraints(constraints)
      .setInitialDelay(30, TimeUnit.SECONDS)
      .addTag("imageWork")
      .build()

    workManager.enqueueUniqueWork(
      "delayedImageDownload",
      ExistingWorkPolicy.KEEP,
      imageWorker
    )

    observeWork(imageWorker.id)
  }

  private fun observeWork(id: UUID) {
    workManager.getWorkInfoByIdLiveData(id)
      .observe(this, observeWorkInfo)
  }

  private fun queryWorkInfo() {
    val workQuery = WorkQuery.Builder
      .fromTags(listOf("imageWork"))
      .addStates(listOf(WorkInfo.State.SUCCEEDED))
      .addUniqueWorkNames(
        listOf("oneTimeImageDownload", "delayedImageDownload", "periodicImageDownload")
      )
      .build()

    workManager.getWorkInfosLiveData(workQuery)
      .observe(this, observeWorkInfoList)
  }

  private val observeWorkInfo = Observer<WorkInfo> { info ->
    if (info != null && info.state.isFinished) {
      hideLottieAnimation()
      activityHomeBinding.downloadLayout.visibility = View.VISIBLE
      val uriResult = info.outputData.getString("IMAGE_URI")
      if (uriResult != null) showDownloadedImage(uriResult.toUri())
    }
  }

  private val observeWorkInfoList = Observer<List<WorkInfo>> { workInfoList ->
    activityHomeBinding.tvWorkInfo.visibility = View.VISIBLE
    activityHomeBinding.tvWorkInfo.text =
      resources.getQuantityString(R.plurals.text_work_desc, workInfoList.size, workInfoList.size)
  }

  private fun requestStoragePermissions() {
    requestMultiplePermissions.launch(
      arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
      )
    )
  }

  private val requestMultiplePermissions =
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
    }

  private fun showLottieAnimation() {
    activityHomeBinding.animationView.visibility = View.VISIBLE
    activityHomeBinding.animationView.playAnimation()

  }

  private fun hideLottieAnimation() {
    activityHomeBinding.animationView.visibility = View.GONE
    activityHomeBinding.animationView.cancelAnimation()

  }

  private fun showDownloadedImage(resultUri: Uri?) {
    activityHomeBinding.completeLayout.visibility = View.VISIBLE
    activityHomeBinding.downloadLayout.visibility = View.GONE
    hideLottieAnimation()
    activityHomeBinding.imgDownloaded.setImageURI(resultUri)
  }
}
