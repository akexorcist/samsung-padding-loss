package dev.akexorcist.samsung.edge2edge.issue

import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import dev.akexorcist.samsung.edge2edge.issue.databinding.ActivityMinimalBinding
import kotlin.math.roundToInt

/**
 * The fixed counterpart of the repro on `main`.
 *
 * `android:fitsSystemWindows="true"` has been removed from the theme. Insets are applied here
 * instead, with [WindowCompat.setDecorFitsSystemWindows] plus an [ViewCompat] inset listener on
 * the root view only. The padded row keeps the paddingHorizontal/paddingVertical it declares in
 * XML, so the readout reports `Bug Reproduced? = false` on a Samsung device too.
 */
class MinimalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMinimalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMinimalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarInsets()
        binding.root.post { readout() }
    }

    /**
     * Adds the system bar insets on top of the root's XML-declared padding, scoped to this one
     * view. The declared values are captured first so repeated inset dispatches never accumulate.
     */
    private fun applySystemBarInsets() {
        val root = binding.root
        val left = root.paddingLeft
        val top = root.paddingTop
        val right = root.paddingRight
        val bottom = root.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
            val bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom)
            WindowInsetsCompat.CONSUMED
        }
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
