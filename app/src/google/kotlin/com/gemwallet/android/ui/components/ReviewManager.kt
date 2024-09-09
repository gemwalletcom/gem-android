package com.gemwallet.android.ui.components

import android.app.Activity
import android.content.Context
import android.util.Log
import com.gemwallet.android.getActivity
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import kotlinx.coroutines.runBlocking

class ReviewManager(val context: Context) {
//    private val reviewManager = FakeReviewManager(context)
    private val reviewManager = ReviewManagerFactory.create(context)

    fun open() {
        reviewManager.requestReviewFlow().addOnCompleteListener {
            if (it.isSuccessful) {
                reviewManager.launchReviewFlow(context.getActivity()!!, it.result)
            }
        }
    }
}