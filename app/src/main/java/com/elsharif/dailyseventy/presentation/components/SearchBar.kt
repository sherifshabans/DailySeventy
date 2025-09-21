package com.elsharif.dailyseventy.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elsharif.dailyseventy.R
import com.elsharif.dailyseventy.util.clearFocusOnKeyboardDismiss

@Composable
fun SearchBar(
    textValue: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    OutlinedTextField(
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Gray,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedTextColor = Gray,
            focusedTextColor = MaterialTheme.typography.bodyLarge.color
        ),
        value = textValue,
        onValueChange = onTextChange,
        modifier = modifier
            .focusRequester(focusRequester)
            .clearFocusOnKeyboardDismiss(),
        placeholder = { Text(text = stringResource(R.string.search)) },
        shape = RoundedCornerShape(size = 22.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search_icon),
                tint = Gray
            )
        }
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun SearchBarPreview() {
    var searchBarValue by remember { mutableStateOf(TextFieldValue("")) }
    SearchBar(searchBarValue, { searchBarValue = it }, Modifier)

}