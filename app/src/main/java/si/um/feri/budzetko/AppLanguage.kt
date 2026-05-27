package si.um.feri.budzetko

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

enum class AppLanguage(val code: String) {
    SLOVENIAN("sl"),
    ENGLISH("en");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.firstOrNull { it.code == code } ?: SLOVENIAN
        }
    }
}

enum class AppCurrency(
    val code: String,
    val symbol: String,
    val eurRate: Double
) {
    EUR("EUR", "€", 1.0),
    USD("USD", "$", 1.1643),
    GBP("GBP", "£", 0.86255);

    companion object {
        fun fromCode(code: String?): AppCurrency {
            return entries.firstOrNull { it.code == code } ?: EUR
        }
    }
}

enum class AppThemeMode(val code: String) {
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromCode(code: String?): AppThemeMode {
            return entries.firstOrNull { it.code == code } ?: LIGHT
        }
    }
}

object AppLanguageStore {
    private const val PREFS_NAME = "budzetko_preferences"
    private const val LANGUAGE_KEY = "app_language"
    private const val CURRENCY_KEY = "app_currency"
    private const val THEME_KEY = "app_theme"

    fun load(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return AppLanguage.fromCode(prefs.getString(LANGUAGE_KEY, AppLanguage.SLOVENIAN.code))
    }

    fun save(context: Context, language: AppLanguage) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(LANGUAGE_KEY, language.code)
            .apply()
    }

    fun loadCurrency(context: Context): AppCurrency {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return AppCurrency.fromCode(prefs.getString(CURRENCY_KEY, AppCurrency.EUR.code))
    }

    fun saveCurrency(context: Context, currency: AppCurrency) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(CURRENCY_KEY, currency.code)
            .apply()
    }

    fun loadTheme(context: Context): AppThemeMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return AppThemeMode.fromCode(prefs.getString(THEME_KEY, AppThemeMode.LIGHT.code))
    }

    fun saveTheme(context: Context, themeMode: AppThemeMode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(THEME_KEY, themeMode.code)
            .apply()
    }

    fun localizedContext(context: Context, language: AppLanguage): Context {
        val locale = Locale.forLanguageTag(language.code)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }
}
