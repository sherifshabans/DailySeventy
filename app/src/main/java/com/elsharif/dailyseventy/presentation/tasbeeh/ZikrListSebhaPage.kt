package com.elsharif.dailyseventy.presentation.tasbeeh

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZikrListSebhaPage(viewModel: TasbeehViewModel, navController: NavController) {
    val zikrOptions = listOf("سبحان الله", "الحمد لله", "لا إله إلا الله", "الله أكبر")
    var selectedZikr by remember { mutableStateOf(zikrOptions.first()) }
    val count by viewModel.getTasbeehCount().collectAsState(initial = 0)

    Scaffold(
        topBar = { DashboardScreenTopBar(Screen.TasbeehList.titleRes, navController) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var expanded by remember { mutableStateOf(false) }

            // ✅ ExposedDropdownMenuBox = Modern ComboBox
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedZikr,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.ChosseZikr)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor() // 👈 مهم عشان القائمة ترتبط بالـ TextField
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    zikrOptions.forEach { zikr ->
                        DropdownMenuItem(
                            text = { Text(zikr) },
                            onClick = {
                                selectedZikr = zikr
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 👇 السبحة
            CustomizableSebhaPage(
                count = count,
                zikrText = selectedZikr,
                onIncrease = { viewModel.increaseTasbeeh() },
                onReset = { viewModel.resetTasbeeh() },
                paddingValues = paddingValues
            )
        }
    }
}
