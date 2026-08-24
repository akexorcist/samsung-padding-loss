package dev.akexorcist.samsung.edge2edge.issue

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import dev.akexorcist.samsung.edge2edge.issue.databinding.ActivityMinimalBinding

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
        val expected = Math.round(16f * resources.displayMetrics.density)
        val text = buildString {
            appendLine("row: padLeft=${row.paddingLeft}px padTop=${row.paddingTop}px expected=${expected}px")
            append(if (row.paddingLeft == expected) "OK" else ">>> REPRODUCED: padding lost")
        }
        Log.i(TAG, "[minimal] $text")
        binding.tvMinimalReadout.text = text
        binding.tvMinimalReadout.postDelayed({ readout() }, 1_500)
    }

    companion object {
        const val TAG = "SamsungPaddingLoss"
    }
}
