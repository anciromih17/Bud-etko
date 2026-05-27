package si.um.feri.budzetko.currency

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import si.um.feri.budzetko.AppCurrency

val LocalAppCurrency = staticCompositionLocalOf { AppCurrency.EUR }

object MoneyFormatter {
    fun format(amountInEur: Double, currency: AppCurrency): String {
        val convertedAmount = amountInEur * currency.eurRate
        return when (currency) {
            AppCurrency.EUR -> "%.2f%s".format(convertedAmount, currency.symbol)
            AppCurrency.USD,
            AppCurrency.GBP -> "%s%.2f".format(currency.symbol, convertedAmount)
        }
    }

    fun formatPlain(amountInEur: Double, currency: AppCurrency): String {
        return "%.2f".format(amountInEur * currency.eurRate)
    }

    fun toBaseEur(amountInSelectedCurrency: Double, currency: AppCurrency): Double {
        return amountInSelectedCurrency / currency.eurRate
    }
}

@Composable
fun formatCurrencyAmount(amountInEur: Double): String {
    return MoneyFormatter.format(amountInEur, LocalAppCurrency.current)
}

@Composable
fun currentCurrencySymbol(): String {
    return LocalAppCurrency.current.symbol
}
