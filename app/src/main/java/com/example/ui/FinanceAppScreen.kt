package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import com.example.ReportGenerator
import com.example.data.TransactionEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceAppScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "PRIVATE VAULT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Security Shield",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "FinSecure",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                },
                actions = {
                    // Privacy indicator
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "OFFLINE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.setDarkMode(!isDarkMode) },
                        modifier = Modifier.testTag("dark_mode_toggle")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.Star else Icons.Default.Lock,
                            contentDescription = "Toggle Dark Mode"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            val navItemColors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFD0BCFF),
                unselectedIconColor = Color(0xFFCAC4D0),
                selectedTextColor = Color(0xFFD0BCFF),
                unselectedTextColor = Color(0xFFCAC4D0),
                indicatorColor = Color(0xFF4F378B)
            )
            NavigationBar(
                containerColor = Color(0xFF2B2930),
                tonalElevation = 0.dp,
                modifier = Modifier.border(width = 1.dp, color = Color(0xFF49454F), shape = RoundedCornerShape(0.dp))
            ) {
                NavigationBarItem(
                    selected = currentScreen == "dashboard",
                    onClick = { viewModel.navigateTo("dashboard") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    colors = navItemColors,
                    modifier = Modifier.testTag("nav_dashboard")
                )
                NavigationBarItem(
                    selected = currentScreen == "transactions",
                    onClick = { viewModel.navigateTo("transactions") },
                    icon = { Icon(Icons.Default.List, contentDescription = "Transactions") },
                    label = { Text("Ledger") },
                    colors = navItemColors,
                    modifier = Modifier.testTag("nav_transactions")
                )
                NavigationBarItem(
                    selected = currentScreen == "advisor",
                    onClick = { viewModel.navigateTo("advisor") },
                    icon = { Icon(Icons.Default.Star, contentDescription = "AI Advisor") },
                    label = { Text("AI Advisor") },
                    colors = navItemColors,
                    modifier = Modifier.testTag("nav_advisor")
                )
                NavigationBarItem(
                    selected = currentScreen == "settings",
                    onClick = { viewModel.navigateTo("settings") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = navItemColors,
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "dashboard" -> DashboardTab(viewModel = viewModel)
                "transactions" -> LedgerTab(viewModel = viewModel)
                "advisor" -> AIAdvisorTab(viewModel = viewModel)
                "settings" -> SettingsTab(viewModel = viewModel)
            }
        }
    }
}

fun getAvailableMonths(transactions: List<TransactionEntity>): List<String> {
    val monthsSet = mutableSetOf<String>()
    val sdf = SimpleDateFormat("MMM yyyy", Locale.US)
    
    // Always include current month
    monthsSet.add(sdf.format(Date()))
    
    for (tx in transactions) {
        monthsSet.add(sdf.format(Date(tx.timestamp)))
    }
    
    val sortedList = monthsSet.toList().sortedWith { m1, m2 ->
        val date1 = try { sdf.parse(m1) } catch (e: Exception) { Date(0) }
        val date2 = try { sdf.parse(m2) } catch (e: Exception) { Date(0) }
        date2.compareTo(date1) // Sort descending (newest first)
    }
    
    return listOf("All Time") + sortedList
}

@Composable
fun DashboardTab(viewModel: MainViewModel) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()

    var selectedMonth by remember { mutableStateOf("All Time") }
    val availableMonths = remember(transactions) {
        getAvailableMonths(transactions)
    }

    val filteredTransactions = remember(transactions, selectedMonth) {
        if (selectedMonth == "All Time") {
            transactions
        } else {
            val sdf = SimpleDateFormat("MMM yyyy", Locale.US)
            transactions.filter { tx ->
                sdf.format(Date(tx.timestamp)) == selectedMonth
            }
        }
    }

    val totalIncome = filteredTransactions.filter { it.isIncome }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { !it.isIncome }.sumOf { it.amount }
    val selectedMonthFlow = totalIncome - totalExpense

    val overallBalance = remember(transactions) {
        val overallIncome = transactions.filter { it.isIncome }.sumOf { it.amount }
        val overallExpense = transactions.filter { !it.isIncome }.sumOf { it.amount }
        overallIncome - overallExpense
    }

    val recentTransactions = filteredTransactions.take(4)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming Headline
        item {
            Column(modifier = Modifier.padding(bottom = 2.dp)) {
                Text(
                    text = "FinSecure",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Fully private, hardware-local financial tracking.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        // Calendar reporting selector & statement download integration
        item {
            val context = LocalContext.current
            var expandedMonthDropdown by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Period Select Button
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { expandedMonthDropdown = true },
                        modifier = Modifier.fillMaxWidth().testTag("month_select_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Month",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedMonth,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = (-0.2).sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expandedMonthDropdown,
                        onDismissRequest = { expandedMonthDropdown = false }
                    ) {
                        availableMonths.forEach { monthOption ->
                            DropdownMenuItem(
                                text = { Text(monthOption, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    selectedMonth = monthOption
                                    expandedMonthDropdown = false
                                },
                                modifier = Modifier.testTag("month_option_$monthOption")
                            )
                        }
                    }
                }

                // Apple-style Download Statement Button
                Button(
                    onClick = {
                        val uri = ReportGenerator.generateAndSavePdfReport(
                            context = context,
                            transactions = filteredTransactions,
                            selectedPeriod = selectedMonth,
                            currency = currency
                        )
                        if (uri != null) {
                            android.widget.Toast.makeText(
                                context,
                                "Statement successfully saved to Downloads folder",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                "Unable to generate statement file",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("download_statement_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export Report",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Download Statement",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = (-0.2).sp
                    )
                }
            }
        }

        // Summary Balance Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("balance_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedMonth == "All Time") "AVAILABLE VAULT BALANCE" else "EST. NET CASH FLOW ($selectedMonth)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 1.2.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (selectedMonth == "All Time") "OFF-GRID" else "CALENDAR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 8.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatCurrency(if (selectedMonth == "All Time") overallBalance else selectedMonthFlow, currency),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = if (selectedMonth == "All Time") {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            if (selectedMonthFlow >= 0) Color(0xFF2E7D32) else Color(0xFFD84315)
                        }
                    )

                    if (selectedMonth != "All Time") {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Total Vault Portfolio: ${formatCurrency(overallBalance, currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2E7D32))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (selectedMonth == "All Time") "All-Time Income" else "Monthly Income",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = formatCurrency(totalIncome, currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD84315))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (selectedMonth == "All Time") "All-Time Charges" else "Total Expenses",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = formatCurrency(totalExpense, currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD84315)
                            )
                        }
                    }
                }
            }
        }

        // Categorized Expense Tracker
        item {
            val categorisedExpenses = filteredTransactions.filter { !it.isIncome }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Spend Breakdown by Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (categorisedExpenses.isEmpty()) {
                        Text(
                            text = "No recorded expenses. Your categorised breakdown will automatically display here once you add transactions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    } else {
                        categorisedExpenses.entries.forEach { (cat, amt) ->
                            val progress = if (totalExpense > 0) (amt / totalExpense).toFloat() else 0f
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = getCategoryIcon(cat),
                                            contentDescription = cat,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = cat,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text = "${formatCurrency(amt, currency)} (${String.format("%.0f", progress * 100)}%)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Receipts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { viewModel.navigateTo("transactions") },
                    modifier = Modifier.testTag("view_all_ledger_button")
                ) {
                    Text("Manage Ledger")
                }
            }
        }

        if (recentTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Your local transaction log is currently empty.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            items(recentTransactions) { tx ->
                TransactionListItem(transaction = tx, onDelete = { viewModel.deleteTransaction(tx) }, currency = currency)
            }
        }
    }
}

@Composable
fun LedgerTab(viewModel: MainViewModel) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    // Search and filters
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("ALL") } // ALL, INCOME, EXPENSE
    var selectedMonthFilter by remember { mutableStateOf("All Time") }

    val availableMonths = remember(transactions) {
        getAvailableMonths(transactions)
    }

    val filteredTransactions = transactions.filter {
        val matchesSearch = it.title.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.notes.contains(searchQuery, ignoreCase = true)

        val matchesType = when (selectedTypeFilter) {
            "INCOME" -> it.isIncome
            "EXPENSE" -> !it.isIncome
            else -> true
        }

        val matchesMonth = if (selectedMonthFilter == "All Time") {
            true
        } else {
            val sdf = SimpleDateFormat("MMM yyyy", Locale.US)
            sdf.format(Date(it.timestamp)) == selectedMonthFilter
        }

        matchesSearch && matchesType && matchesMonth
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_transaction_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Record")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Off-grid Local Ledger",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Add, modify, and search items. All balances recalculate instantly on-device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search records...") },
                placeholder = { Text("E.g. Walmart, Salary...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ledger_search_field"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("ALL" to "All Streams", "INCOME" to "Earnings", "EXPENSE" to "Charges")
                filters.forEach { (key, label) ->
                    val isSelected = selectedTypeFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTypeFilter = key },
                        label = { Text(label) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("filter_chip_$key")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calendar Month Filter Dropdown
            var expandedMonthFilterDropdown by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calendar Month Segment:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                Box {
                    AssistChip(
                        onClick = { expandedMonthFilterDropdown = true },
                        label = { Text(selectedMonthFilter) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("ledger_month_select_button")
                    )

                    DropdownMenu(
                        expanded = expandedMonthFilterDropdown,
                        onDismissRequest = { expandedMonthFilterDropdown = false }
                    ) {
                        availableMonths.forEach { monthOption ->
                            DropdownMenuItem(
                                text = { Text(monthOption) },
                                onClick = {
                                    selectedMonthFilter = monthOption
                                    expandedMonthFilterDropdown = false
                                },
                                modifier = Modifier.testTag("ledger_month_option_$monthOption")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Records List
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No results matching search filter." else "No records securely logged.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        TransactionListItem(
                            transaction = tx,
                            onDelete = { viewModel.deleteTransaction(tx) },
                            currency = currency
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            currency = currency,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, amount, isIncome, category, notes ->
                viewModel.addTransaction(
                    title = title,
                    amount = amount,
                    isIncome = isIncome,
                    category = category,
                    notes = notes,
                    timestamp = System.currentTimeMillis()
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TransactionListItem(
    transaction: TransactionEntity,
    onDelete: () -> Unit,
    currency: String,
    modifier: Modifier = Modifier
) {
    val dateString = remember(transaction.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(transaction.timestamp))
    }

    val isDark = MaterialTheme.colorScheme.background == ElegantDarkBg
    val tagBg = if (transaction.isIncome) {
        if (isDark) Color(0xFF1B382B) else Color(0xFFE8F5E9)
    } else {
        if (isDark) Color(0xFF422E1B) else Color(0xFFFFF3E0)
    }
    val tagTint = if (transaction.isIncome) {
        if (isDark) Color(0xFF81C784) else Color(0xFF1B5E20)
    } else {
        if (isDark) Color(0xFFFFB74D) else Color(0xFFE65100)
    }
    val amountColor = if (transaction.isIncome) {
        if (isDark) Color(0xFF81C784) else Color(0xFF1B5E20)
    } else {
        if (isDark) Color(0xFFFF8A80) else Color(0xFFC62828)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tx_item_${transaction.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category Tag/Indicator Icon with elegant theme status pairing
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tagBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(transaction.category),
                        contentDescription = transaction.category,
                        tint = tagTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = transaction.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        if (transaction.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${transaction.notes}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                    }
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${if (transaction.isIncome) "+" else "-"}${formatCurrency(transaction.amount, currency)}",
                    fontWeight = FontWeight.ExtraBold,
                    color = amountColor,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_tx_${transaction.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Record",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, isIncome: Boolean, category: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) } // Default expense
    var notes by remember { mutableStateOf("") }

    val incomeCategories = listOf("Salary", "Investment", "Gifts", "Refunds", "Other Earnings")
    val expenseCategories = listOf("Food", "Rent & Lodging", "Utilities", "Transportation", "Leisure", "Healthcare", "Shopping", "Other Expenses")

    var selectedCategory by remember { mutableStateOf(expenseCategories[0]) }
    var categoryExpanded by remember { mutableStateOf(false) }

    // Toggle selected category list on model toggle
    LaunchedEffect(isIncome) {
        selectedCategory = if (isIncome) incomeCategories[0] else expenseCategories[0]
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Cash Event",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Type Switcher (Income or Expense)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    Button(
                        onClick = { isIncome = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isIncome) MaterialTheme.colorScheme.error else Color.Transparent,
                            contentColor = if (!isIncome) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_select_expense"),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Charge")
                    }
                    Button(
                        onClick = { isIncome = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isIncome) Color(0xFF2E7D32) else Color.Transparent,
                            contentColor = if (isIncome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_select_income"),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Earning")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Short Description") },
                    placeholder = { Text("E.g. Target grocery") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_tx_title"),
                    singleLine = true
                )

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($currency)") },
                    placeholder = { Text(if (currency == "UGX") "50000" else "0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_tx_amount"),
                    singleLine = true
                )

                // Category Selection Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("add_tx_category")
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            val items = if (isIncome) incomeCategories else expenseCategories
                            items.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Notes Input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("Extra context...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0) {
                        onConfirm(title, amt, isIncome, selectedCategory, notes)
                    }
                },
                enabled = title.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                modifier = Modifier.testTag("add_tx_confirm")
            ) {
                Text("Lock Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Discard")
            }
        }
    )
}

@Composable
fun AIAdvisorTab(viewModel: MainViewModel) {
    val aiRecommendation by viewModel.aiRecommendation.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val aiError by viewModel.aiError.collectAsStateWithLifecycle()
    val userApiKey by viewModel.userApiKey.collectAsStateWithLifecycle()

    val keyStatusText = if (userApiKey.isNotBlank()) {
        "Secure User Key Installed"
    } else {
        "Using Default Sandbox Server Limits"
    }

    LaunchedEffect(Unit) {
        if (aiRecommendation.isBlank() && !isAiLoading) {
            viewModel.fetchAiRecommendations()
        }
    }

    val elegantGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF4F378B), Color(0xFF381E72))
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "AI Advisor",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Private personal budgeting insights from Google AI.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        // Active advisory security card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (userApiKey.isNotBlank()) Color(0xFF1B382B) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, if (userApiKey.isNotBlank()) Color(0xFF81C784).copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (userApiKey.isNotBlank()) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = "Key status icon",
                        tint = if (userApiKey.isNotBlank()) Color(0xFF81C784) else Color(0xFFFFB74D),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Escrow Policy: $keyStatusText",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (userApiKey.isNotBlank()) Color(0xFF81C784) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Advice Content Viewer (Stunning gradient representation block)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_advice_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.2f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(elegantGradient)
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFD0BCFF))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "AI",
                                    tint = Color(0xFF381E72),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "AI Financial Advisor",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Smart insights enabled via Google AI",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFD0BCFF),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Black.copy(alpha = 0.25f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            if (isAiLoading) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Tailoring custom local analytics securely...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFD0BCFF),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else {
                                val adviceText = aiRecommendation.ifBlank {
                                    "Your local transaction log is secure. Click sync below to generate a real-time spending report using Google AI."
                                }
                                MarkdownText(
                                    text = adviceText,
                                    textColor = Color.White,
                                    secondaryColor = Color(0xFFD0BCFF),
                                    primaryColor = Color(0xFFD0BCFF)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Button
        item {
            Button(
                onClick = { viewModel.fetchAiRecommendations() },
                enabled = !isAiLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (MaterialTheme.colorScheme.background == ElegantDarkBg) Color(0xFFEADDFF) else MaterialTheme.colorScheme.primary,
                    contentColor = if (MaterialTheme.colorScheme.background == ElegantDarkBg) Color(0xFF21005D) else MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_sync_button"),
                shape = CircleShape,
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                if (isAiLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = if (MaterialTheme.colorScheme.background == ElegantDarkBg) Color(0xFF21005D) else MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aggregating Local State...", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "Sync Insights")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ask Advisor", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // Error display
        aiError?.let { err ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Secure AI Gateway Notice",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Core Privacy statement
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "FinSecure analyzes local data securely. Your raw financial ledger never leaves your device.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SettingsTab(viewModel: MainViewModel) {
    val userApiKey by viewModel.userApiKey.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()

    val exportResult by viewModel.exportResult.collectAsStateWithLifecycle()
    val importResult by viewModel.importResult.collectAsStateWithLifecycle()

    var apiKeyInput by remember { mutableStateOf(userApiKey) }
    var exportPasscode by remember { mutableStateOf("") }
    var importPasscode by remember { mutableStateOf("") }
    var importCipherText by remember { mutableStateOf("") }

    val clipboardManager = LocalClipboardManager.current
    var isKeyVisible by remember { mutableStateOf(false) }

    // Sync input when saved key switches
    LaunchedEffect(userApiKey) {
        apiKeyInput = userApiKey
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section header
        item {
            Text(
                text = "Privacy & Configurations",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Modify security parameters, local themes, and manage encrypted data backups.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // 1. Dark Theme Option
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dark UI Theme Option",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Switch color intensity styles",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.setDarkMode(it) },
                        modifier = Modifier.testTag("dark_theme_switch")
                    )
                }
            }
        }

        // 1b. Currency Selection Dropdown Option
        item {
            var currencyExpanded by remember { mutableStateOf(false) }
            val currencyOptions = listOf("UGX", "USD", "EUR", "GBP", "KES", "TZS", "RWF")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Primary Currency Symbol",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Set display currency format. Selected: $currency",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Box {
                        TextButton(
                            onClick = { currencyExpanded = true },
                            modifier = Modifier.testTag("currency_dropdown_button")
                        ) {
                            Text(
                                text = currency,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Choose Currency"
                            )
                        }

                        DropdownMenu(
                            expanded = currencyExpanded,
                            onDismissRequest = { currencyExpanded = false }
                        ) {
                            currencyOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        viewModel.setCurrency(option)
                                        currencyExpanded = false
                                    },
                                    modifier = Modifier.testTag("currency_option_$option")
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Google Gemini API Gateway Integration
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Google AI Secure Key Gateway",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "To request dynamic spending habits analysis without sandbox rate limits, insert your custom Google Gemini API Key. It is written completely to sandboxed Room settings, encrypted locally, and is never exposed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("AIzaSy...") },
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.Close else Icons.Default.Info,
                                    contentDescription = "Toggle Key Unmasking"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_field"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.saveUserApiKey(apiKeyInput.trim())
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("save_api_key_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Gateway settings")
                    }
                }
            }
        }

        // 3. Encrypted Report Backup (Export)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Private Encrypted Export",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Secure your records by converting them into an AES-encrypted Base64 cipher file. Define a strong passcode key. You must keep this passcode safe to recover records later.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = exportPasscode,
                        onValueChange = { exportPasscode = it },
                        label = { Text("Backup Passcode / PIN") },
                        placeholder = { Text("Enter a strong decryption code") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_passcode_field"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.exportEncryptedReport(exportPasscode) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Symmetrically Encrypt & Generate Backup")
                    }

                    // Export Result
                    exportResult?.let { res ->
                        Spacer(modifier = Modifier.height(12.dp))
                        if (res.startsWith("SUCCESS")) {
                            val cipher = res.substringAfter("SUCCESS|")
                            Column {
                                Text(
                                    text = "AES Confidential cipher generated. Copy and save it safely:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(cipher))
                                        }
                                        .testTag("copy_export_container"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = cipher,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 4,
                                            modifier = Modifier.testTag("export_result_text")
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Share cipher",
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "TAP TO SECURELY COPY",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                TextButton(
                                    onClick = { viewModel.clearExportResult() },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Clear Backup String View")
                                }
                            }
                        } else {
                            Text(
                                text = "Confidential Export Error: $res",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 4. Encrypted Report Import
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Confidential Records Restorations",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "Paste the generated AES-encrypted confidential Base64 cipher and enter the same passcode key to reconstruct the SQL transaction database securely.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = importCipherText,
                        onValueChange = { importCipherText = it },
                        label = { Text("AES-encrypted Confidential Cipher") },
                        placeholder = { Text("Paste long ciphertext string...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_cipher_field"),
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = importPasscode,
                        onValueChange = { importPasscode = it },
                        label = { Text("Backup Passcode / PIN") },
                        placeholder = { Text("Enter decryption code") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_passcode_field"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.importEncryptedReport(importCipherText.trim(), importPasscode) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Decrypt & Synchronize Ledger")
                    }

                    // Import Result
                    importResult?.let { res ->
                        Spacer(modifier = Modifier.height(12.dp))
                        if (res.startsWith("SUCCESS")) {
                            val msg = res.substringAfter("SUCCESS|")
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Ok", tint = Color(0xFF2E7D32))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(
                                onClick = {
                                    viewModel.clearImportResult()
                                    importCipherText = ""
                                    importPasscode = ""
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Acknowledge Restoration Success")
                            }
                        } else {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = "Fail", tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = res,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom lightweight Markdown parser to render AI generated advice beautifully
 * without adding third-party parser engines.
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    primaryColor: Color = MaterialTheme.colorScheme.primary
) {
    val lines = text.split("\n")
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lines.forEach { line ->
            val trimmedLine = line.trim()
            when {
                trimmedLine.startsWith("###") -> {
                    Text(
                        text = trimmedLine.removePrefix("###").trim().replace("**", ""),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (textColor == MaterialTheme.colorScheme.onSurfaceVariant) MaterialTheme.colorScheme.onSurface else textColor,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                trimmedLine.startsWith("##") -> {
                    Text(
                        text = trimmedLine.removePrefix("##").trim().replace("**", ""),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                }
                trimmedLine.startsWith("**") && trimmedLine.endsWith("**") -> {
                    Text(
                        text = trimmedLine.removeSurrounding("**").trim(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = secondaryColor,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                trimmedLine.startsWith("-") || trimmedLine.startsWith("*") -> {
                    val rawText = trimmedLine.substring(1).trim()
                    Row(
                        modifier = Modifier.padding(start = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("•  ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = primaryColor)
                        Text(
                            text = parseBoldText(rawText),
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                }
                trimmedLine.matches(Regex("^\\d+\\..*")) -> {
                    val content = trimmedLine.substringAfter(".").trim()
                    val num = trimmedLine.substringBefore(".")
                    Row(
                        modifier = Modifier.padding(start = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("$num.  ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = primaryColor)
                        Text(
                            text = parseBoldText(content),
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                }
                trimmedLine.isNotBlank() -> {
                    Text(
                        text = parseBoldText(trimmedLine),
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor
                    )
                }
                else -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

/**
 * Basic formatter to allow bold sections in regular markdown lines.
 */
@Composable
fun parseBoldText(input: String): AnnotatedString {
    return remember(input) {
        val builder = AnnotatedString.Builder()
        val parts = input.split("**")
        for (index in parts.indices) {
            val part = parts[index]
            if (index % 2 == 1) {
                // Odd parts are bold
                builder.pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold))
                builder.append(part)
                builder.pop()
            } else {
                builder.append(part)
            }
        }
        builder.toAnnotatedString()
    }
}

fun getCategoryIcon(cat: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (cat) {
        "Food" -> Icons.Default.Star  // represent nourishment/food/star-level
        "Housing", "Rent & Lodging" -> Icons.Default.Home
        "Utilities" -> Icons.Default.Settings
        "Transportation" -> Icons.Default.Home
        "Healthcare" -> Icons.Default.Check
        "Leisure", "Entertainment" -> Icons.Default.Star
        "Shopping" -> Icons.Default.Star
        "Salary" -> Icons.Default.CheckCircle
        "Investment" -> Icons.Default.Check
        "Gifts" -> Icons.Default.Star
        "Refunds" -> Icons.Default.Check
        else -> Icons.Default.Info
    }
}

fun formatCurrency(amount: Double, currency: String): String {
    val isWhole = amount % 1.0 == 0.0
    val formattedValue = if (currency == "UGX" || currency == "TZS" || currency == "RWF") {
        String.format(Locale.US, "%,.0f", amount)
    } else {
        if (isWhole) {
            String.format(Locale.US, "%,.0f", amount)
        } else {
            String.format(Locale.US, "%,.2f", amount)
        }
    }
    return "$currency $formattedValue"
}
