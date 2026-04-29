package com.estimalaces.app

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.estimalaces.app.presentation.EstimaLacesViewModelFactory
import com.estimalaces.app.presentation.client.ClientScreen
import com.estimalaces.app.presentation.goal.GoalScreen
import com.estimalaces.app.presentation.goal.GoalViewModel
import com.estimalaces.app.presentation.home.HomeScreen
import com.estimalaces.app.presentation.home.HomeViewModel
import com.estimalaces.app.presentation.product.ProductScreen
import com.estimalaces.app.presentation.product.ProductViewModel
import com.estimalaces.app.presentation.report.ReportScreen
import com.estimalaces.app.presentation.report.ReportViewModel
import com.estimalaces.app.presentation.sale.SaleScreen
import com.estimalaces.app.presentation.sale.SaleViewModel
import com.estimalaces.app.presentation.theme.EstimaLacesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = EstimaLacesViewModelFactory((application as EstimaLacesApp).repository)
        setContent {
            EstimaLacesTheme {
                EstimaLacesAppScreen(factory, this)
            }
        }
    }
}

private data class AppDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val destinations = listOf(
    AppDestination("home", "Inicio", Icons.Default.Home),
    AppDestination("products", "Produtos", Icons.Default.ShoppingBag),
    AppDestination("sales", "Vendas", Icons.Default.PointOfSale),
    AppDestination("clients", "Clientes", Icons.Default.People),
    AppDestination("goals", "Metas", Icons.Default.Flag),
    AppDestination("reports", "Relatorios", Icons.Default.Assessment)
)

@Composable
private fun EstimaLacesAppScreen(factory: EstimaLacesViewModelFactory, context: Context) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStack by navController.currentBackStackEntryAsState()
                val currentDestination = backStack?.destination
                destinations.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                HomeScreen(viewModel<HomeViewModel>(factory = factory))
            }
            composable("products") {
                ProductScreen(viewModel<ProductViewModel>(factory = factory))
            }
            composable("sales") {
                SaleScreen(viewModel<SaleViewModel>(factory = factory))
            }
            composable("clients") {
                ClientScreen(viewModel<SaleViewModel>(factory = factory))
            }
            composable("goals") {
                GoalScreen(viewModel<GoalViewModel>(factory = factory))
            }
            composable("reports") {
                ReportScreen(viewModel<ReportViewModel>(factory = factory), context = context)
            }
        }
    }
}
