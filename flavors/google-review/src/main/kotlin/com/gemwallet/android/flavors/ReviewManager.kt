package com.gemwallet.android.flavors

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory

class ReviewManager() {
    fun open(activity: Activity) {
        //    private val reviewManager = FakeReviewManager(context)
        val reviewManager = ReviewManagerFactory.create(activity)
        reviewManager.requestReviewFlow().addOnCompleteListener {
            if (it.isSuccessful) {
                reviewManager.launchReviewFlow(activity, it.result)
            }
        }
    }
}