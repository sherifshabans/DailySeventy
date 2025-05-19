package com.elsharif.dailyseventy.presentaion.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elsharif.dailyseventy.domain.model.Zakker
import com.elsharif.dailyseventy.presentaion.zekr.ZekkrViewModel
import com.elsharif.dailyseventy.ui.theme.RadiusContainer


@Composable
fun CountCard(
     modifier: Modifier=Modifier,
     zekkr:Zakker,
) {

    var localCount by remember { mutableStateOf(0) }
    var count by remember { mutableStateOf(zekkr.count.toInt()) }
    val defaultColor =   MaterialTheme.colorScheme.secondaryContainer
    var dominantColor by remember {
        mutableStateOf(defaultColor)
    }


    Box(
        modifier = modifier.padding(
            bottom = 8.dp,
            start = 8.dp,
            end = 8.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(RadiusContainer.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondaryContainer,
                            dominantColor
                        )
                    )
                ).clickable {
                    if (count > 0) {  // Prevent negative values
                        count -= 1
                    }
                    localCount+=1
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(12.dp),
                text = zekkr.content,
                style = MaterialTheme.typography.bodyLarge,
           //     maxLines = 5
            )

            Spacer(modifier = Modifier.height(6.dp))

            if(zekkr.description.isNotBlank()){
                Text(
                    modifier = Modifier.padding(12.dp),
                    //   maxLines = 2,
                    text = zekkr.description,   // Remove single quotes,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(6.dp))

            }

            if(zekkr.reference.isNotBlank()){
                Text(
                    modifier = Modifier.padding(12.dp),
                    maxLines = 2,
                    text = zekkr.reference,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(6.dp))

            }

            Row{

                Box(
                    modifier = Modifier
                        .size(40.dp).padding(4.dp), // Set a fixed size for alignment
                    contentAlignment = Alignment.BottomStart // Center the content inside
                ) {
                    val shareText = "${zekkr.content}\n\n${zekkr.description}"


                    ShareTextIcon(text = shareText)

                }
                Box(
                modifier = Modifier
                    .size(40.dp).padding(4.dp), // Set a fixed size for alignment
                contentAlignment = Alignment.Center // Center the content inside
            ) {
                // Circular Progress Indicator
                CircularProgressIndicator(
                    progress = { localCount.toFloat() / zekkr.count.toFloat() },
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.inverseSurface,
                    strokeWidth = 4.dp,
                    trackColor = MaterialTheme.colorScheme.inverseOnSurface,
                )

                // Number of remaining counts inside the progress indicator
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    color = Color.Black
                )

                // Clickable IconButton to decrement count
                IconButton(
                    onClick = {
                        if (count > 0) {  // Prevent negative values
                            count -= 1
                        }
                        localCount+=1
                    },
                    modifier = Modifier.size(35.dp) // Ensure it matches the progress size
                ) {
                    Box(modifier = Modifier.fillMaxSize()) // Empty box to handle click inside the circle
                }
            }
        }

        }


    }
}