package com.SOLTIS.calc

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.util.Locale
import kotlin.math.pow
import androidx.compose.ui.tooling.preview.Preview
import com.SOLTIS.calc.ui.theme.CalCTheme

@Preview(showBackground = true)
@Composable
fun LoanAppPreview() {
    CalCTheme {
        LoanApp()
    }
}

data class LoanResult(
    val monthlyPayment: Double,
    val amount: Double,
    val totalInterest: Double
)

@Composable
fun LoanApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "input") {
        composable("input") {
            LoanInputScreen(onCalculate = { amount, term, isYears, rate ->
                val result = calculateLoan(amount, term, isYears, rate)
                navController.navigate("result/${result.monthlyPayment}/${result.amount}/${result.totalInterest}")
            })
        }
        composable("result/{payment}/{amount}/{interest}") { backStackEntry ->
            val payment = backStackEntry.arguments?.getString("payment")?.toDoubleOrNull() ?: 0.0
            val amount = backStackEntry.arguments?.getString("amount")?.toDoubleOrNull() ?: 0.0
            val interest = backStackEntry.arguments?.getString("interest")?.toDoubleOrNull() ?: 0.0
            LoanResultScreen(
                result = LoanResult(payment, amount, interest),
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanInputScreen(onCalculate: (Double, Double, Boolean, Double) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var term by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var isYears by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Calculadora de Préstamos", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) amount = it },
                label = { Text("Monto del préstamo") },
                prefix = { Text("$ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = term,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) term = it },
                    label = { Text(if (isYears) "Plazo (años)" else "Plazo (meses)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = isYears, onClick = { isYears = true })
                        Text("Años", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = !isYears, onClick = { isYears = false })
                        Text("Meses", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            OutlinedTextField(
                value = rate,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) rate = it },
                label = { Text("Tasa de interés anual (%)") },
                suffix = { Text("%") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val a = amount.toDoubleOrNull() ?: 0.0
                    val t = term.toDoubleOrNull() ?: 0.0
                    val r = rate.toDoubleOrNull() ?: 0.0
                    if (t > 0) {
                        onCalculate(a, t, isYears, r)
                    }
                },
                enabled = amount.isNotBlank() && term.isNotBlank() && rate.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Calcular Cuota", fontSize = 18.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanResultScreen(result: LoanResult, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resultados del Cálculo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    ResultItem("Cuota mensual", String.format(Locale.US, "$ %,.2f", result.monthlyPayment))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ResultItem("Monto del préstamo", String.format(Locale.US, "$ %,.2f", result.amount))
                ResultItem("Intereses totales", String.format(Locale.US, "$ %,.2f", result.totalInterest))
            }
            
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Nuevo Cálculo")
            }
        }
    }
}

@Composable
fun ResultItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun calculateLoan(amount: Double, term: Double, termInYears: Boolean, annualRate: Double): LoanResult {
    val months = if (termInYears) term * 12 else term
    if (months <= 0) return LoanResult(0.0, amount, 0.0)
    
    val monthlyRate = annualRate / 12 / 100
    
    val monthlyPayment = if (monthlyRate == 0.0) {
        amount / months
    } else {
        val powFactor = (1 + monthlyRate).pow(months)
        amount * (monthlyRate * powFactor) / (powFactor - 1)
    }
    
    val totalPaid = monthlyPayment * months
    val totalInterest = totalPaid - amount
    
    return LoanResult(monthlyPayment, amount, totalInterest)
}
