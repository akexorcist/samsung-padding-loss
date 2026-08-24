package dev.akexorcist.samsung.edge2edge.issue

import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import dev.akexorcist.samsung.edge2edge.issue.databinding.ActivityMinimalBinding
import kotlin.math.roundToInt

/**
 * Minimal reproduction of XML-declared padding being zeroed on Samsung devices.
 *
 * `android:fitsSystemWindows="true"` set at the THEME level (see `SamsungPaddingLossTheme`
 * in themes.xml), combined with resizing this window into freeform/pop-up mode on a Samsung
 * device, zeroes the paddingHorizontal/paddingVertical this LinearLayout declares in XML. No
 * custom view, no design-system component, and no RecyclerView are needed - a bare LinearLayout
 * with a TextView reproduces it.
 *
 * Steps: install, launch in a freeform/pop-up window, then drag-resize it. The panel reports
 * OK or REPRODUCED; the same is logged under tag [TAG].
 */
class MinimalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMinimalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMinimalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.post { readout() }
    }

    private fun readout() {
        val row = binding.paddedRow
        val expected = (16f * resources.displayMetrics.density).roundToInt()
        val cfg = resources.configuration
        val text = buildString {
            appendLine("MultiWindow          = $isInMultiWindowMode")
            appendLine("Resolution (px)      = ${windowBoundsPx()}")
            appendLine("Resolution (dp)      = ${cfg.screenWidthDp} x ${cfg.screenHeightDp}")
            appendLine("Density              = ${resources.displayMetrics.density}")
            appendLine("Button Padding")
            appendLine("  • Left Padding     = ${row.paddingLeft} px")
            appendLine("  • Top Padding      = ${row.paddingTop} px")
            appendLine("  • Expected Padding = $expected px")
            append("Bug Reproduced?      = ${row.paddingLeft != expected}")
        }
        Log.i(TAG, text)
        binding.tvMinimalReadout.text = text
    }

    private fun windowBoundsPx(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            "${bounds.width()} x ${bounds.height()}"
        } else {
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(metrics)
            "${metrics.widthPixels} x ${metrics.heightPixels}"
        }

    companion object {
        const val TAG = "SamsungPaddingLoss"
    }
}
