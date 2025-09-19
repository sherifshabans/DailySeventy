package com.elsharif.dailyseventy.presentation.tasbeeh

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.presentation.components.DashboardScreenTopBar
import com.elsharif.dailyseventy.util.Screen

@Composable
fun CustomZikrSebhaPage(viewModel: TasbeehViewModel,navController: NavController) {


    var customZikr by remember { mutableStateOf("اذكر الله") }
    val count by viewModel.getTasbeehCount().collectAsState(initial = 0)

    Scaffold(
        topBar = { DashboardScreenTopBar(Screen.TasbeehCustom.titleRes,navController) }
    ) { paddingValues ->

        Column(
            modifier = Modifier.padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField(
                modifier = Modifier.padding(top = 16.dp),
                value = customZikr,
                onValueChange = { customZikr = it },
                label = { Text(text = stringResource(R.string.write_zirk)) }
            )

            CustomizableSebhaPage(
                count = count,
                zikrText = customZikr,
                onIncrease = { viewModel.increaseTasbeeh() },
                onReset = { viewModel.resetTasbeeh() },
                paddingValues = paddingValues
            )
        }
    }
}
