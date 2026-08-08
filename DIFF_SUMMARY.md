# DIFF_SUMMARY

Running summary of notable changes to `vcatd-vvdec-plugin`, newest first.

---

## 2026-08-07 — Rename `Mp4DecoderPlugin` → `Mp4ParserExtension`

- `VcatVvcdecPlugin` now implements `VcatDecoder, Mp4ParserExtension` (the MP4 `stsd` container-
  parser interface was renamed in the api). Pure rename — no behavior change.
- api dep bumped `1.0.2 → 1.0.3`. Builds against api `1.0.3`.

---

## 2026-08-05 — Migrate to the `VcatDecoder` SPI

- `VcatVvcdecPlugin` now implements **`VcatDecoder` + `Mp4DecoderPlugin`** (was the deprecated
  `VcatDecoderPlugin` + `NonStdDecoderStsdParser`).
- VVC uses a **non-standard MP4 sample entry** (`vvc1` / `vvcC`), so the plugin advertises itself
  as its own MP4 `stsd` parser via `getSupportedContainerParsers()` → `[this]`;
  `sampleEntry4ccCode()` / `codecConfiguration4ccCode()` / `parseStsd()` now come from
  `Mp4DecoderPlugin`. (This is the case dav1d didn't exercise — AV1's `av01` is standard.)
- Dropped `getSupportedProfiles()` and the legacy `mimeType()` accessor; codec MIME is the
  decoder's `getMimeType()` (and `VideoConfiguration.mimeType`, still set by `VvcVideoCfgParser`).
- Reports plugin API **`0.1.0`** (inherited `VcatDecoder` default; legacy decoders report
  `0.0.1`).
- `compileOnly` / `testImplementation` api dep bumped `1.0.1 → 1.0.2` (Maven Local).
- Verified on-device: `vcat.vvdec` registers and its native lib loads; the host info-overlay
  shows `Plugin API: 0.1.0` when vvdec is the selected VVC decoder.

## Initial — VVC/H.266 (vvdec) decoder plugin

- External VVC decoder plugin wrapping Fraunhofer vvdec via JNI. Package
  `com.roncatech.libvcat.vvdec`, native lib `libvcat_vvdec_jni.so`, MIME `video/vvc`, id
  `vcat.vvdec`. vvdec built from source; `dist` task injects `classes.dex` for the host's
  `DexClassLoader`. GPL-3.0-or-later; vvdec is Clear BSD (see `LICENSE`).
