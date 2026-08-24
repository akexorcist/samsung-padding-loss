# Samsung Padding Loss

Minimal reproduction of a padding-loss bug on Samsung foldables and tablets: padding declared
in XML is silently zeroed after the app window is resized.

## Root cause

**`android:fitsSystemWindows="true"` declared at the theme/window level**, combined with
resizing the activity's window into **freeform/pop-up mode** on a Samsung device (One UI),
zeroes out `android:paddingHorizontal` / `android:paddingVertical` / `android:padding`
declared in XML on views inside that window.

No custom view, no design-system component, and no specific screen structure is required.
A bare `LinearLayout` with a `TextView` reproduces it (`MinimalActivity`).

This was isolated by bisecting a multi-level theme chain attribute by attribute. Every other
attribute in that chain (`statusBarColor`, `windowDrawsSystemBarBackgrounds`,
`windowLightStatusBar`, MaterialComponents vs Material3 vs AppCompat) was confirmed **not**
required — only `android:fitsSystemWindows="true"` at the theme level matters.

## Reproduce

1. Install and launch on a Samsung tablet or foldable (confirmed on a Galaxy Tab S9,
   Android 16, and a Galaxy Z Fold 8).
2. Put the app into freeform/pop-up view (long-press Recents thumbnail → "Open in pop-up
   view", or drag down from the top of the app in multi-window).
3. Drag-resize the floating window (any direction/amount).
4. Read the on-screen panel, or `adb logcat -s SamsungPaddingLoss`.

```
window: multiWindow=true 1104x1408px 415x529dp density=2.625
row: padLeft=0px padTop=0px expected=34px
>>> REPRODUCED: padding lost
```

A window that was never resized (fresh launch, or fullscreen) reads correctly:

```
window: multiWindow=false 1812x2176px 691x829dp density=2.625
row: padLeft=34px padTop=26px expected=34px
OK
```

## Which window modes were tested

`multiWindow` in the readout comes from `Activity.isInMultiWindowMode`, which is `true` for
both split-screen and freeform/pop-up - Android exposes no public API that distinguishes the
two. Record which mode you used alongside the reading.

Confirmed to reproduce:

- freeform/pop-up, after a drag-resize

Confirmed **not** to reproduce:

- a window that was never resized (fresh launch, or fullscreen)

Not yet tested - do not assume either way:

- split-screen, after dragging the divider
- folding/unfolding the device, which also resizes the window
- rotation, and display-size or font-size changes

## What was ruled out along the way

A series of intermediate mocks was tested before isolating the theme attribute:

- Stale density / drawable cache — not it, all density sources agree.
- MotionLayout ConstraintSets — not it, a plain `ImageView`/`LinearLayout` reproduces it.
- Window size or windowing mode alone — not it, exact-bounds matches without
  `fitsSystemWindows` never reproduced.
- A specific custom component or its measure override — not it, reproduces with a bare
  `LinearLayout`.
- Live Compose recomposition, concurrent async state updates, activity recreate with
  `SavedState` restore — tested and not required.

## The fix

Remove `android:fitsSystemWindows="true"` from the theme and move inset handling to the modern
WindowInsets API — `WindowCompat.setDecorFitsSystemWindows` plus an `OnApplyWindowInsetsListener`
on the specific views that need to avoid the system bars, scoped to those views rather than the
whole window.

## Build

```
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb logcat -s SamsungPaddingLoss
```
