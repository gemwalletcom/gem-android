package com.gemwallet.android.ui.components.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.designsystem.padding16

@Composable
fun Scene(
    title: String,
    backHandle: Boolean = false,
    padding: PaddingValues = PaddingValues(horizontal = 0.dp),
    onClose: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    mainAction: (@Composable () -> Unit)? = null,
    mainActionPadding: PaddingValues = PaddingValues(start = padding16, top = padding16, end = padding16, bottom = padding16),
    snackbar: SnackbarHostState? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scene(
        title = {
            Text(
                modifier = Modifier,
                text = title,
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis
            )
        },
        backHandle,
        padding,
        onClose,
        actions,
        mainAction,
        mainActionPadding,
        snackbar,
        content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Scene(
    title: @Composable () -> Unit,
    backHandle: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    onClose: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    mainAction: (@Composable () -> Unit)? = null,
    mainActionPadding: PaddingValues = PaddingValues(padding16),
    snackbar: SnackbarHostState? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    BackHandler(backHandle) {
        onClose?.invoke()
    }
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = title,
                navigationIcon = {
                    if (onClose != null) {
                        IconButton(onClick = onClose) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
                        }
                    }
                },
                actions = actions,
            )
        },
        bottomBar = {
            if (mainAction != null) {
                Box(modifier = Modifier.padding(mainActionPadding)) {
                    mainAction()
                }
            }
        },
        snackbarHost = {
            if (snackbar != null) {
                SnackbarHost(hostState = snackbar)
            }
        }
    ) {
        Box(
            modifier = Modifier
                .padding(top = it.calculateTopPadding())
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(contentPadding)
                        .weight(1f),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        content()
                        Spacer16()
                    }
                }
            }
        }
    }
}