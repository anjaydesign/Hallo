package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.model.Customer
import com.example.ui.screens.CustomerDetailScreen
import com.example.ui.screens.CustomerFormScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SettingsAndGuideScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CustomerViewModel

sealed class AppScreen {
    object Dashboard : AppScreen()
    data class CustomerForm(val customerToEdit: Customer? = null) : AppScreen()
    data class CustomerDetail(val customer: Customer) : AppScreen()
    object SettingsAndGuide : AppScreen()
}

class MainActivity : ComponentActivity() {

    private val viewModel: CustomerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppNavigation(viewModel: CustomerViewModel) {
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Dashboard) }
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()

    when (val screen = currentScreen) {
        is AppScreen.Dashboard -> {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToAdd = {
                    currentScreen = AppScreen.CustomerForm(customerToEdit = null)
                },
                onNavigateToDetail = { customer ->
                    viewModel.selectCustomer(customer)
                    currentScreen = AppScreen.CustomerDetail(customer = customer)
                },
                onNavigateToSettings = {
                    currentScreen = AppScreen.SettingsAndGuide
                }
            )
        }

        is AppScreen.CustomerForm -> {
            CustomerFormScreen(
                viewModel = viewModel,
                existingCustomer = screen.customerToEdit,
                onNavigateBack = {
                    currentScreen = if (screen.customerToEdit != null) {
                        AppScreen.CustomerDetail(customer = screen.customerToEdit)
                    } else {
                        AppScreen.Dashboard
                    }
                },
                onSaveSuccess = {
                    currentScreen = AppScreen.Dashboard
                }
            )
        }

        is AppScreen.CustomerDetail -> {
            val activeCustomer = selectedCustomer ?: screen.customer
            CustomerDetailScreen(
                customer = activeCustomer,
                viewModel = viewModel,
                onNavigateBack = {
                    viewModel.selectCustomer(null)
                    currentScreen = AppScreen.Dashboard
                },
                onNavigateToEdit = { cust ->
                    currentScreen = AppScreen.CustomerForm(customerToEdit = cust)
                }
            )
        }

        is AppScreen.SettingsAndGuide -> {
            SettingsAndGuideScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    currentScreen = AppScreen.Dashboard
                }
            )
        }
    }
}
