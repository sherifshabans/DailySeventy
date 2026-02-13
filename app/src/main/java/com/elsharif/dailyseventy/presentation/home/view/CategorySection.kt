package com.elsharif.dailyseventy.presentation.home.view

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.presentation.home.model.listOfCategories

@Composable
fun CategorySection(navController: NavController){
    Column {
        Text(
            text = stringResource(R.string.muslimzikr),
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(12.dp)
        )

        LazyRow {

            items(listOfCategories.size){ index->
                CategoryItem(index,navController)
            }
        }
    }
}

@Composable
fun CategoryItem(
    index: Int,
    navController: NavController
) {

    var isSelected by remember { mutableStateOf(false) }
    val category= listOfCategories[index]

    var color = if (isSelected) Color.Red else Color.Gray
    var lastPaddingIndex=0.dp
    if(index== listOfCategories.size-1){
        lastPaddingIndex=16.dp
    }
    Box(modifier = Modifier.padding(start = 16.dp, end = lastPaddingIndex)) {


        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(25.dp))
                .background(MaterialTheme.colorScheme.secondary)
                .size(120.dp)
                .clickable { navController.navigate(category.route) }
                .padding(13.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {



            Image(
                painter = painterResource(id = category.iconRes),
                contentDescription = category.title,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .size(56.dp) // same size as old Box
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(8.dp) // same padding inside old Box

            )

            Text(
                text = stringResource(category.routeInt),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

        }
    }
}
