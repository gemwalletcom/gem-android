package com.gemwallet.android.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gemwallet.android.ui.components.list_item.listItem
import com.gemwallet.android.ui.models.ListPosition

@Composable
fun SearchBar(
    query: TextFieldState,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    Row(modifier = modifier.listItem(ListPosition.Single).fillMaxWidth().height(42.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .padding(start = 48.dp, top = 10.dp, end = 40.dp, bottom = 10.dp)
                ,
                textStyle = TextStyle.Default.copy(
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                state = query,
                lineLimits = TextFieldLineLimits.SingleLine,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            if (query.text.isEmpty()) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 48.dp, end = 32.dp),
                    maxLines = 1,
                    text = stringResource(id = android.R.string.search_go),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 14.sp,
                )
            }
            IconButton(onClick = focusRequester::requestFocus) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "search")
            }
            if (query.text.isNotEmpty()) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = {
                        query.clearText()
                    }
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                }
            }
        }
    }
}