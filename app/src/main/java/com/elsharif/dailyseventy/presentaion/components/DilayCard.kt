package com.elsharif.dailyseventy.presentaion.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DailyCard(
) {

    var count :Int =0
    var content :String ="أستغفر الله"
    var description :String=""
    ElevatedCard(
        modifier = Modifier
    ) {

        val list2 = listOf("")

        //DropdownExample("ذِكرك", list2)

        Row {


        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(text = content, style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(5.dp))
            Text(text =description, style = MaterialTheme.typography.bodySmall.copy(fontSize = 30.sp))

            IconButton(
                onClick = {
                    count--
                }
            ) {
                CircularProgressIndicator(
                    progress = { count.toFloat() /(count+100).toFloat() },
                    modifier = Modifier.size(150.dp),
                    color = Color(0xffEF2679),
                    strokeWidth = 4.dp,
                    trackColor = Color(0xFFFFFFFF),
                )
            }
        }

    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyTopBar(title:String) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Navigate to Back Screen"
                )
            }
        },
        title = { Text(text = "وذّكر", style = MaterialTheme.typography.headlineSmall) },
    )

}

@Composable
fun dropdownExample(
    labelText: String,
    items: List<String>
):Pair<String,String>{

    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    onClick = {
                        selectedItem = item
                        expanded = false
                    },
                    text = {
                        Text(text = item)
                    }
                )
            }
        }
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Drop down")
        }

        Text(selectedItem, modifier = Modifier.padding(8.dp), maxLines = 2)



        Text(labelText,modifier=Modifier.padding(8.dp), maxLines = 2)
    }

    return selectedItem to labelText
}