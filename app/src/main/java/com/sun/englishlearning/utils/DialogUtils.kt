package com.sun.englishlearning.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.sun.englishlearning.R

object DialogUtils {

    /**
     * Show error dialog with title, message and OK button
     */
    fun showErrorDialog(
        context: Context,
        title: String,
        message: String,
        onPositiveClick: (() -> Unit)? = null
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                onPositiveClick?.invoke()
            }
            .setIcon(R.drawable.ic_error)
            .setCancelable(false)
            .show()
    }

    /**
     * Show error dialog with default error title
     */
    fun showErrorDialog(
        context: Context,
        message: String,
        onPositiveClick: (() -> Unit)? = null
    ) {
        showErrorDialog(
            context = context,
            title = context.getString(R.string.dialog_error_title),
            message = message,
            onPositiveClick = onPositiveClick
        )
    }

    /**
     * Show confirmation dialog
     */
    fun showConfirmationDialog(
        context: Context,
        title: String,
        message: String,
        positiveButtonText: String,
        negativeButtonText: String,
        onPositiveClick: () -> Unit,
        onNegativeClick: (() -> Unit)? = null
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { _, _ ->
                onPositiveClick()
            }
            .setNegativeButton(negativeButtonText) { dialog, _ ->
                onNegativeClick?.invoke() ?: dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Show info dialog with OK button
     */
    fun showInfoDialog(
        context: Context,
        title: String,
        message: String,
        onPositiveClick: (() -> Unit)? = null
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                onPositiveClick?.invoke()
            }
            .setIcon(R.drawable.ic_info)
            .setCancelable(false)
            .show()
    }
}
