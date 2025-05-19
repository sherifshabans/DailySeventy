package com.elsharif.dailyseventy.presentaion.components

import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


@Composable
fun ShareTextIcon(text: String) {
    val context = LocalContext.current
    val appName = context.applicationInfo.loadLabel(context.packageManager).toString()
    val shareText = "$text\n\nبواسطة تطبيق $appName"

    IconButton(

        onClick = {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Share Text",
            tint = Color.Black // or MaterialTheme.colorScheme.primary
        )
    }
}
