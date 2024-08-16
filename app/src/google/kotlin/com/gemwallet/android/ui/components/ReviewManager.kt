package com.gemwallet.android.ui.components

import android.content.Context
import com.gemwallet.android.getActivity
import com.google.android.play.core.review.ReviewManagerFactory

class ReviewManager(val context: Context) {
    val reviewManager = ReviewManagerFactory.create(context)

    fun open() {
        reviewManager.requestReviewFlow().addOnCompleteListener {
            if (it.isSuccessful) {
                reviewManager.launchReviewFlow(context.getActivity()!!, it.result)
            }
        }
    }
}