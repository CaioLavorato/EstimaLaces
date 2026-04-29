package com.estimalaces.app.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.estimalaces.app.data.dao.SaleExportRow
import com.estimalaces.app.presentation.asDate
import java.io.File

class CsvExportService(private val context: Context) {
    fun shareSales(rows: List<SaleExportRow>) {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "estimalaces_vendas.csv")
        file.writeText(buildCsv(rows), Charsets.UTF_8)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Exportar planilha"))
    }

    private fun buildCsv(rows: List<SaleExportRow>): String {
        val header = listOf(
            "Data",
            "Cliente",
            "Produto",
            "Tipo",
            "Valor de compra",
            "Valor sugerido",
            "Valor vendido",
            "Lucro",
            "Brinde aplicado",
            "Valor do brinde",
            "Forma de pagamento",
            "Observacoes"
        ).joinToString(";")

        val lines = rows.map { row ->
            listOf(
                row.date.asDate(),
                row.client,
                row.product,
                row.type,
                row.purchaseValue.asCsvNumber(),
                row.suggestedValue.asCsvNumber(),
                row.soldValue.asCsvNumber(),
                row.profit.asCsvNumber(),
                if (row.giftApplied) "Sim" else "Nao",
                row.giftValue.asCsvNumber(),
                row.paymentMethod,
                row.notes
            ).joinToString(";") { it.escapeCsv() }
        }

        return buildString {
            appendLine(header)
            lines.forEach { appendLine(it) }
        }
    }

    private fun Double.asCsvNumber(): String = "%.2f".format(java.util.Locale("pt", "BR"), this)

    private fun String.escapeCsv(): String {
        val cleaned = replace("\"", "\"\"")
        return if (cleaned.contains(";") || cleaned.contains("\n")) "\"$cleaned\"" else cleaned
    }
}
