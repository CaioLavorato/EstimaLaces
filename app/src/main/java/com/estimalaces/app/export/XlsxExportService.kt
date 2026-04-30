package com.estimalaces.app.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.estimalaces.app.data.dao.SaleExportRow
import com.estimalaces.app.presentation.asDate
import java.io.File
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class XlsxExportService(private val context: Context) {
    fun shareSales(rows: List<SaleExportRow>) {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "estimalaces_vendas.xlsx")
        file.outputStream().use { output ->
            ZipOutputStream(output).use { zip ->
                zip.writeEntry("[Content_Types].xml", contentTypesXml())
                zip.writeEntry("_rels/.rels", rootRelsXml())
                zip.writeEntry("xl/workbook.xml", workbookXml())
                zip.writeEntry("xl/_rels/workbook.xml.rels", workbookRelsXml())
                zip.writeEntry("xl/styles.xml", stylesXml())
                zip.writeEntry("xl/worksheets/sheet1.xml", salesSheetXml(rows))
                zip.writeEntry("xl/worksheets/sheet2.xml", summarySheetXml(rows))
            }
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Exportar planilha"))
    }

    private fun ZipOutputStream.writeEntry(name: String, content: String) {
        putNextEntry(ZipEntry(name))
        write(content.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun salesSheetXml(rows: List<SaleExportRow>): String {
        val headers = listOf(
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
            "Tipo do brinde",
            "Produto do brinde",
            "Forma de pagamento",
            "Taxa cartao %",
            "Valor taxa cartao",
            "Observacoes"
        )
        val data = rows.map { row ->
            listOf(
                Cell.Text(row.date.asDate()),
                Cell.Text(row.client),
                Cell.Text(row.product),
                Cell.Text(row.type),
                Cell.Money(row.purchaseValue),
                Cell.Money(row.suggestedValue),
                Cell.Money(row.soldValue),
                Cell.Money(row.profit),
                Cell.Text(if (row.giftApplied) "Sim" else "Nao"),
                Cell.Money(row.giftValue),
                Cell.Text(row.giftType),
                Cell.Text(row.giftProduct),
                Cell.Text(row.paymentMethod),
                Cell.Number(row.cardFeePercent),
                Cell.Money(row.cardFeeValue),
                Cell.Text(row.notes)
            )
        }
        return worksheetXml(
            title = "EstimaLaces - Vendas",
            subtitle = "Seu lucro comeca no preco certo.",
            headers = headers,
            rows = data,
            widths = listOf(14, 22, 28, 16, 16, 16, 16, 14, 18, 16, 16, 22, 18, 14, 16, 30)
        )
    }

    private fun summarySheetXml(rows: List<SaleExportRow>): String {
        val totalSold = rows.sumOf { it.soldValue }
        val totalCost = rows.sumOf { it.purchaseValue }
        val profit = rows.sumOf { it.profit }
        val gifts = rows.sumOf { it.giftValue }
        val cardFees = rows.sumOf { it.cardFeeValue }
        val summaryRows = listOf(
            listOf(Cell.Text("Quantidade vendida"), Cell.Number(rows.size.toDouble())),
            listOf(Cell.Text("Total vendido"), Cell.Money(totalSold)),
            listOf(Cell.Text("Total gasto"), Cell.Money(totalCost)),
            listOf(Cell.Text("Lucro"), Cell.Money(profit)),
            listOf(Cell.Text("Brindes em valor"), Cell.Money(gifts)),
            listOf(Cell.Text("Taxas de cartao"), Cell.Money(cardFees))
        )
        return worksheetXml(
            title = "EstimaLaces - Resumo",
            subtitle = "Resumo das vendas exportadas",
            headers = listOf("Indicador", "Valor"),
            rows = summaryRows,
            widths = listOf(28, 18)
        )
    }

    private fun worksheetXml(
        title: String,
        subtitle: String,
        headers: List<String>,
        rows: List<List<Cell>>,
        widths: List<Int>
    ): String {
        val columnXml = widths.mapIndexed { index, width ->
            val column = index + 1
            """<col min="$column" max="$column" width="$width" customWidth="1"/>"""
        }.joinToString("")

        val titleRow = rowXml(1, listOf(Cell.Text(title)), style = 1)
        val subtitleRow = rowXml(2, listOf(Cell.Text(subtitle)), style = 2)
        val headerRow = rowXml(4, headers.map { Cell.Text(it) }, style = 3)
        val bodyRows = rows.mapIndexed { index, cells ->
            rowXml(index + 5, cells, style = null)
        }.joinToString("")
        val lastColumn = columnName(headers.size)

        return xml(
            """
            <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
              <cols>$columnXml</cols>
              <sheetData>$titleRow$subtitleRow$headerRow$bodyRows</sheetData>
              <mergeCells count="2">
                <mergeCell ref="A1:${lastColumn}1"/>
                <mergeCell ref="A2:${lastColumn}2"/>
              </mergeCells>
              <autoFilter ref="A4:${lastColumn}${(rows.size + 4).coerceAtLeast(4)}"/>
              <freezePanes/>
            </worksheet>
            """.trimIndent()
        )
    }

    private fun rowXml(rowNumber: Int, cells: List<Cell>, style: Int?): String {
        val cellsXml = cells.mapIndexed { index, cell ->
            cellXml("${columnName(index + 1)}$rowNumber", cell, style)
        }.joinToString("")
        return """<row r="$rowNumber">$cellsXml</row>"""
    }

    private fun cellXml(ref: String, cell: Cell, style: Int?): String {
        val styleAttr = when {
            style != null -> """ s="$style""""
            cell is Cell.Money -> """ s="4""""
            else -> ""
        }
        return when (cell) {
            is Cell.Text -> """<c r="$ref"$styleAttr t="inlineStr"><is><t>${cell.value.xmlEscape()}</t></is></c>"""
            is Cell.Number -> """<c r="$ref"$styleAttr><v>${cell.value.asRawNumber()}</v></c>"""
            is Cell.Money -> """<c r="$ref"$styleAttr><v>${cell.value.asRawNumber()}</v></c>"""
        }
    }

    private fun columnName(index: Int): String {
        var value = index
        val name = StringBuilder()
        while (value > 0) {
            val remainder = (value - 1) % 26
            name.insert(0, ('A'.code + remainder).toChar())
            value = (value - 1) / 26
        }
        return name.toString()
    }

    private fun contentTypesXml(): String = xml(
        """
        <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
          <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
          <Default Extension="xml" ContentType="application/xml"/>
          <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
          <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
          <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
          <Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
        </Types>
        """.trimIndent()
    )

    private fun rootRelsXml(): String = xml(
        """
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
          <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
        </Relationships>
        """.trimIndent()
    )

    private fun workbookXml(): String = xml(
        """
        <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
          <sheets>
            <sheet name="Vendas" sheetId="1" r:id="rId1"/>
            <sheet name="Resumo" sheetId="2" r:id="rId2"/>
          </sheets>
        </workbook>
        """.trimIndent()
    )

    private fun workbookRelsXml(): String = xml(
        """
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
          <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
          <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/>
          <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
        </Relationships>
        """.trimIndent()
    )

    private fun stylesXml(): String = xml(
        """
        <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
          <numFmts count="1">
            <numFmt numFmtId="164" formatCode="&quot;R$&quot; #,##0.00"/>
          </numFmts>
          <fonts count="4">
            <font><sz val="11"/><name val="Calibri"/></font>
            <font><b/><sz val="18"/><color rgb="FF1F1E24"/><name val="Calibri"/></font>
            <font><i/><sz val="11"/><color rgb="FFC85C73"/><name val="Calibri"/></font>
            <font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>
          </fonts>
          <fills count="3">
            <fill><patternFill patternType="none"/></fill>
            <fill><patternFill patternType="solid"><fgColor rgb="FFFFFFFF"/><bgColor indexed="64"/></patternFill></fill>
            <fill><patternFill patternType="solid"><fgColor rgb="FFC85C73"/><bgColor indexed="64"/></patternFill></fill>
          </fills>
          <borders count="2">
            <border><left/><right/><top/><bottom/><diagonal/></border>
            <border><left style="thin"><color rgb="FFE5D6D9"/></left><right style="thin"><color rgb="FFE5D6D9"/></right><top style="thin"><color rgb="FFE5D6D9"/></top><bottom style="thin"><color rgb="FFE5D6D9"/></bottom><diagonal/></border>
          </borders>
          <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
          <cellXfs count="5">
            <xf numFmtId="0" fontId="0" fillId="1" borderId="1" xfId="0" applyFill="1" applyBorder="1"/>
            <xf numFmtId="0" fontId="1" fillId="1" borderId="0" xfId="0" applyFont="1" applyFill="1"/>
            <xf numFmtId="0" fontId="2" fillId="1" borderId="0" xfId="0" applyFont="1" applyFill="1"/>
            <xf numFmtId="0" fontId="3" fillId="2" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1"/>
            <xf numFmtId="164" fontId="0" fillId="1" borderId="1" xfId="0" applyNumberFormat="1" applyFill="1" applyBorder="1"/>
          </cellXfs>
          <cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles>
        </styleSheet>
        """.trimIndent()
    )

    private fun xml(content: String): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>$content"""

    private fun String.xmlEscape(): String = replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    private fun Double.asRawNumber(): String = String.format(Locale.US, "%.2f", this)

    private sealed class Cell {
        data class Text(val value: String) : Cell()
        data class Number(val value: Double) : Cell()
        data class Money(val value: Double) : Cell()
    }
}
