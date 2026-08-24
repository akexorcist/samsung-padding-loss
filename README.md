# Samsung Padding Loss

XML-declared padding is silently zeroed on Samsung foldables when the app runs on the unfolded
inner display. Confirmed on **Galaxy Z Fold 8** and **Z Fold 7**.

## Cause

`android:fitsSystemWindows="true"` declared at the **theme/window level**. That alone zeroes
`android:padding`, `paddingHorizontal` and `paddingVertical` on views inside that window — no
custom view or design-system component needed, a bare `LinearLayout` reproduces it.

Bisected from a multi-level theme chain: every other attribute (`statusBarColor`,
`windowDrawsSystemBarBackgrounds`, `windowLightStatusBar`, MaterialComponents vs Material3 vs
AppCompat) was ruled out, as were stale density/drawable cache, MotionLayout, Compose
recomposition, custom components, and window size alone.

## Reproduce

1. Install on a Z Fold 8 or Z Fold 7.
2. Unfold and launch on the inner display.
3. Read the on-screen panel, or `adb logcat -s SamsungPaddingLoss`.

```
MultiWindow          = false
Resolution (px)      = 1848 x 2448
Resolution (dp)      = 704 x 933
Density              = 2.625
Button Padding
  • Left Padding     = 0 px
  • Top Padding      = 0 px
  • Expected Padding = 42 px
Bug Reproduced?      = true
```

`MultiWindow = false` — fullscreen, no split-screen, pop-up, or resize involved. Folded, the
same build reads `Left Padding = 42 px` and `Bug Reproduced? = false`.

## Fix

Remove `android:fitsSystemWindows="true"` from the theme and handle insets with
`WindowCompat.setDecorFitsSystemWindows` plus an `OnApplyWindowInsetsListener` on the specific
views that need it.

## Build

```
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb logcat -s SamsungPaddingLoss
```

## License

Apache License 2.0 — see [LICENSE](LICENSE). Copyright 2026 Akexorcist.
