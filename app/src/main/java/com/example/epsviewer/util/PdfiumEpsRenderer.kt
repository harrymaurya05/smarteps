package com.example.epsviewer.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.shockwave.pdfium.PdfiumCore
import timber.log.Timber
import java.io.File
import java.io.InputStream

/**
 * Enhanced EPS Renderer using PdfiumAndroid
 *
 * This renderer attempts to handle EPS files that can be processed as PDF:
 * 1. Direct PDF rendering (for EPS files that are actually embedded PDFs)
 * 2. PostScript to PDF conversion (basic)
 *
 * PdfiumAndroid provides native PDF rendering powered by Google's PDFium.
 */
class PdfiumEpsRenderer(private val context: Context) {

    private val pdfiumCore = PdfiumCore(context)

    /**
     * Try to render EPS as PDF using Pdfium
     *
     * This works for:
     * - EPS files with embedded PDF
     * - EPS files that can be interpreted as PDF
     */
    fun renderToBitmap(
        epsInputStream: InputStream,
        boundingBox: EpsParser.EpsBoundingBox,
        scale: Float
    ): Bitmap? {
        var tempFile: File? = null

        try {
            // Save EPS to temp file
            tempFile = File.createTempFile("eps_pdf_", ".eps", context.cacheDir)
            tempFile.outputStream().use { output ->
                epsInputStream.copyTo(output)
            }

            Timber.d("PdfiumRenderer: Attempting to render EPS as PDF")

            // Try to open as PDF
            val fd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)

            try {
                val pdfDocument = pdfiumCore.newDocument(fd)

                if (pdfDocument != null) {
                    try {
                        val pageCount = pdfiumCore.getPageCount(pdfDocument)
                        if (pageCount > 0) {
                            pdfiumCore.openPage(pdfDocument, 0)

                            val width = (boundingBox.width * scale).toInt().coerceAtLeast(100)
                            val height = (boundingBox.height * scale).toInt().coerceAtLeast(100)

                            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            pdfiumCore.renderPageBitmap(
                                pdfDocument,
                                bitmap,
                                0, // pageIndex
                                0, 0, // x, y offset
                                width, height
                            )

                            pdfiumCore.closeDocument(pdfDocument)
                            fd.close()

                            Timber.i("PdfiumRenderer: Successfully rendered EPS as PDF")
                            return bitmap
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "PdfiumRenderer: Error rendering page")
                        pdfiumCore.closeDocument(pdfDocument)
                    }
                }
            } catch (e: Exception) {
                Timber.d(e, "PdfiumRenderer: File is not a valid PDF")
            }

            fd?.close()
            return null

        } catch (e: Exception) {
            Timber.w(e, "PdfiumRenderer: Error processing EPS")
            return null
        } finally {
            tempFile?.delete()
        }
    }

    /**
     * Try to convert EPS to PDF for rendering
     *
     * This is a basic implementation. For full PostScript support,
     * use Ghostscript or the PostScript interpreter.
     */
    fun tryConvertEpsToPdf(epsFile: File, outputPdf: File): Boolean {
        try {
            // Check if file already contains PDF data
            val content = epsFile.readText(Charsets.ISO_8859_1)

            // Some EPS files have embedded PDF (EPS with PDF preview)
            if (content.contains("%PDF-")) {
                Timber.d("PdfiumRenderer: Found embedded PDF in EPS")

                // Extract PDF portion
                val pdfStart = content.indexOf("%PDF-")
                if (pdfStart >= 0) {
                    val pdfContent = content.substring(pdfStart)
                    outputPdf.writeText(pdfContent, Charsets.ISO_8859_1)
                    return true
                }
            }

            Timber.d("PdfiumRenderer: No embedded PDF found")
            return false

        } catch (e: Exception) {
            Timber.e(e, "PdfiumRenderer: Error converting EPS to PDF")
            return false
        }
    }

    fun close() {
        // Cleanup if needed
    }
}

