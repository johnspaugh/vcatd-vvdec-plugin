/*
 * VCAT (Video Codec Acid Test)
 *
 * SPDX-FileCopyrightText: Copyright (C) 2020-2025 VCAT authors and RoncaTech
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * This file is part of VCAT.
 *
 * VCAT is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VCAT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VCAT. If not, see <https://www.gnu.org/licenses/gpl-3.0.html>.
 *
 * For proprietary/commercial use cases, a written GPL-3.0 waiver or
 * a separate commercial license is required from RoncaTech LLC.
 *
 * All VCAT artwork is owned exclusively by RoncaTech LLC. Use of VCAT logos
 * and artwork is permitted for the purpose of discussing, documenting,
 * or promoting VCAT itself. Any other use requires prior written permission
 * from RoncaTech LLC.
 *
 * Contact: legal@roncatech.com
 */

package com.roncatech.libvcat.vvdec;

import android.content.Context;
import android.os.Handler;

import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.decoder.DecoderException;
import com.google.android.exoplayer2.util.ParsableByteArray;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import com.roncatech.vcat.decoder_plugin_api.ContainerParser;
import com.roncatech.vcat.decoder_plugin_api.Mp4ParserExtension;
import com.roncatech.vcat.decoder_plugin_api.VcatDecoder;
import com.roncatech.vcat.decoder_plugin_api.VideoConfiguration;

import java.util.Collections;
import java.util.List;

/**
 * vcat-d VVC/H.266 decoder plugin (vvdec) on the {@link VcatDecoder} SPI. VVC uses a
 * non-standard MP4 sample entry ({@code vvc1}/{@code vvcC}), so this plugin is also an
 * {@link Mp4ParserExtension} and parses the {@code stsd} itself.
 */
public class VcatVvcdecPlugin implements VcatDecoder, Mp4ParserExtension {

    @Override public int sampleEntry4ccCode(){
        return Util.getIntegerCodeForString("vvc1");
    }

    @Override
    public int codecConfiguration4ccCode(){
        return Util.getIntegerCodeForString("vvcC");
    }

    @Override
    public VideoConfiguration parseStsd(byte[] data){
        return VvcVideoCfgParser.parseStsd(data);
    }

    /** VVC needs custom MP4 stsd parsing, so this decoder advertises itself as its MP4 parser. */
    @Override
    public List<ContainerParser> getSupportedContainerParsers() {
        return Collections.singletonList(this);
    }

    @Override
    public String getId() {
        return "vcat.vvdec";
    }

    @Override
    public String getDisplayName() {
        return getId() + "-" + getVersion();
    }

    @Override
    public String getVersion() {
        // Bump as you ship new builds
        return NativeVvdec.vvdecGetVersion();
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    public static final String mimeType = "video/vvc";

    @Override
    public Renderer createVideoRenderer(
            Context context,
            long allowedJoiningTimeMs,
            Handler eventHandler,
            VideoRendererEventListener eventListener,
            int threads
    ) throws DecoderException {
        // threads is required by SPI (>=1). tileThreads fixed to 4 per your note.
        VvdecProvider vvdec = new VvdecProvider(threads);
        if (vvdec.isAvailable(context)) {
            return vvdec.build(allowedJoiningTimeMs, eventHandler, eventListener);
        }
        throw new DecoderException("Unable to build 'vcat-vvdec' renderer");
    }
}
