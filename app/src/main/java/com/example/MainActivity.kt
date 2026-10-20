package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CrmPipelineScreen
import com.example.ui.screens.CustomerDetailScreen
import com.example.ui.screens.CustomerFormScreen
import com.example.ui.screens.CustomerMapScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SettingsAndGuideScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.MyRepRedPrimary
import com.example.ui.viewmodel.CustomerViewModel

sealed class AppScreen {
    object MainTabs : AppScreen()
    data class CustomerForm(val customerToEdit: Customer? = null) : AppScreen()
    data class CustomerDetail(val customer: Customer) : AppScreen()
}

enum class NavigationTab(val label: String) {
    DASHBOARD("Beranda"),
    PIPELINE("Pipeline"),
    MAP("Peta"),
    ANALYTICS("Analitik"),
    SETTINGS("Pengaturan")
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
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.MainTabs) }
    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()

    when (val screen = currentScreen) {
        is AppScreen.MainTabs -> {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        NavigationBarItem(
                            selected = currentTab == NavigationTab.DASHBOARD,
                            onClick = { currentTab = NavigationTab.DASHBOARD },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                            label = { Text(NavigationTab.DASHBOARD.label, fontSize = 10.sp, fontWeight = if (currentTab == NavigationTab.DASHBOARD) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MyRepRedPrimary,
                                selectedTextColor = MyRepRedPrimary,
                                indicatorColor = MyRepRedPrimary.copy(alpha = 0.12f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentTab == NavigationTab.PIPELINE,
                            onClick = { currentTab = NavigationTab.PIPELINE },
                            icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Pipeline CRM") },
                            label = { Text(NavigationTab.PIPELINE.label, fontSize = 10.sp, fontWeight = if (currentTab == NavigationTab.PIPELINE) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MyRepRedPrimary,
                                selectedTextColor = MyRepRedPrimary,
                                indicatorColor = MyRepRedPrimary.copy(alpha = 0.12f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentTab == NavigationTab.MAP,
                            onClick = { currentTab = NavigationTab.MAP },
                            icon = { Icon(Icons.Default.LocationOn, contentDescription = "Peta Sebaran") },
                            label = { Text(NavigationTab.MAP.label, fontSize = 10.sp, fontWeight = if (currentTab == NavigationTab.MAP) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MyRepRedPrimary,
                                selectedTextColor = MyRepRedPrimary,
                                indicatorColor = MyRepRedPrimary.copy(alpha = 0.12f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentTab == NavigationTab.ANALYTICS,
                            onClick = { currentTab = NavigationTab.ANALYTICS },
                            icon = { Icon(Icons.Default.Assessment, contentDescription = "Analitik") },
                            label = { Text(NavigationTab.ANALYTICS.label, fontSize = 10.sp, fontWeight = if (currentTab == NavigationTab.ANALYTICS) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MyRepRedPrimary,
                                selectedTextColor = MyRepRedPrimary,
                                indicatorColor = MyRepRedPrimary.copy(alpha = 0.12f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentTab == NavigationTab.SETTINGS,
                            onClick = { currentTab = NavigationTab.SETTINGS },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Pengaturan") },
                            label = { Text(NavigationTab.SETTINGS.label, fontSize = 10.sp, fontWeight = if (currentTab == NavigationTab.SETTINGS) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MyRepRedPrimary,
                                selectedTextColor = MyRepRedPrimary,
                                indicatorColor = MyRepRedPrimary.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (currentTab) {
                        NavigationTab.DASHBOARD -> {
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
                                    currentTab = NavigationTab.SETTINGS
                                }
                            )
                        }

                        NavigationTab.PIPELINE -> {
                            CrmPipelineScreen(
                                viewModel = viewModel,
                                onCustomerClick = { customer ->
                                    viewModel.selectCustomer(customer)
                                    currentScreen = AppScreen.CustomerDetail(customer = customer)
                                }
                            )
                        }

                        NavigationTab.MAP -> {
                            CustomerMapScreen(
                                viewModel = viewModel,
                                onCustomerClick = { customer ->
                                    viewModel.selectCustomer(customer)
                                    currentScreen = AppScreen.CustomerDetail(customer = customer)
                                }
                            )
                        }

                        NavigationTab.ANALYTICS -> {
                            AnalyticsScreen(
                                viewModel = viewModel,
                                onCustomerClick = { customer ->
                                    viewModel.selectCustomer(customer)
                                    currentScreen = AppScreen.CustomerDetail(customer = customer)
                                }
                            )
                        }

                        NavigationTab.SETTINGS -> {
                            SettingsAndGuideScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    currentTab = NavigationTab.DASHBOARD
                                }
                            )
                        }
                    }
                }
            }
        }

        is AppScreen.CustomerForm -> {
            CustomerFormScreen(
                viewModel = viewModel,
                existingCustomer = screen.customerToEdit,
                onNavigateBack = {
                    currentScreen = if (screen.customerToEdit != null) {
                        AppScreen.CustomerDetail(customer = screen.customerToEdit)
                    } else {
                        AppScreen.MainTabs
                    }
                },
                onSaveSuccess = {
                    currentScreen = AppScreen.MainTabs
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
                    currentScreen = AppScreen.MainTabs
                },
                onNavigateToEdit = { cust ->
                    currentScreen = AppScreen.CustomerForm(customerToEdit = cust)
                }
            )
        }
    }
}
