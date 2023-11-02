package com.example.lunchtray

import androidx.navigation.NavController

fun backToStart(navController: NavController): () -> Unit {
    return {
        navController.popBackStack(ScreenMenu.Start.name, false)
    }
}