/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.lunchtray

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lunchtray.datasource.DataSource
import com.example.lunchtray.model.MenuItem
import com.example.lunchtray.ui.AccompanimentMenuScreen
import com.example.lunchtray.ui.CheckoutScreen
import com.example.lunchtray.ui.EntreeMenuScreen
import com.example.lunchtray.ui.OrderViewModel
import com.example.lunchtray.ui.SideDishMenuScreen
import com.example.lunchtray.ui.StartOrderScreen
import com.example.lunchtray.ui.theme.LunchTrayTheme

enum class ScreenMenu(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Entree(title = R.string.choose_entree),
    SideDish(title = R.string.choose_side_dish),
    Accompaniment(title = R.string.choose_accompaniment),
    Checkout(title = R.string.order_checkout)
}
@Preview(showBackground = true)
@Composable
fun PreviewLunchTryApp() {
    LunchTrayTheme {
        LunchTrayApp()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    canReturnBack: Boolean,
    currentScreenMenu: ScreenMenu,
    onBackButton: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = { Text(stringResource(currentScreenMenu.title)) },
        modifier = modifier,
        navigationIcon = {
            if (canReturnBack) {
                IconButton(onClick = onBackButton) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunchTrayApp(
    navController: NavHostController = rememberNavController(),
    viewModel: OrderViewModel = viewModel(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreenMenu = ScreenMenu.valueOf(
        backStackEntry?.destination?.route ?: ScreenMenu.Start.name
    )
    val canReturnBack = navController.previousBackStackEntry != null

    Scaffold(
        topBar = {
            AppBar(
                canReturnBack = canReturnBack,
                currentScreenMenu = currentScreenMenu,
                onBackButton = { navController.navigateUp() },
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        NavHost(
            navController = navController,
            startDestination = ScreenMenu.Start.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = ScreenMenu.Start.name) {
                StartOrderScreen(
                    onStartOrderButtonClicked = { navController.navigate(ScreenMenu.Entree.name) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(route = ScreenMenu.Entree.name) {
                EntreeMenuScreen(
                    options = DataSource.entreeMenuItems,
                    onCancelButtonClicked = backToStart(navController),
                    onNextButtonClicked = { navController.navigate(ScreenMenu.SideDish.name) },
                    onSelectionChanged = {item: MenuItem.EntreeItem -> viewModel.updateEntree(item)}
                )
            }
            composable(route = ScreenMenu.SideDish.name) {
                SideDishMenuScreen(
                    options = DataSource.sideDishMenuItems,
                    onCancelButtonClicked = backToStart(navController),
                    onNextButtonClicked = { navController.navigate(ScreenMenu.Accompaniment.name) },
                    onSelectionChanged = {item: MenuItem.SideDishItem -> }
                )
            }
            composable(route = ScreenMenu.Accompaniment.name) {
                AccompanimentMenuScreen(
                    options = DataSource.accompanimentMenuItems,
                    onCancelButtonClicked = backToStart(navController),
                    onNextButtonClicked = { navController.navigate(ScreenMenu.Checkout.name) },
                    onSelectionChanged = {item: MenuItem.AccompanimentItem -> }
                )
            }
            composable(route = ScreenMenu.Checkout.name) {
                CheckoutScreen(
                    orderUiState = uiState,
                    onNextButtonClicked = backToStart(navController),
                    onCancelButtonClicked = backToStart(navController)
                )
            }
        }
    }
}
