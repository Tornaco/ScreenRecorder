/*
 * Copyright (C) 2013 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.nick.app.screencast.cast;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.CamcorderProfile;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.text.TextUtils;
import android.view.Surface;

import org.xml.sax.Attributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;
import safesax.Element;
import safesax.ElementListener;
import safesax.Parsers;
import safesax.RootElement;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
abstract class EncoderDevice {

    Context context;
    private MediaProjection projection;
    private int width;
    private int height;
    private MediaCodec venc;
    private VirtualDisplay virtualDisplay;
    // Standard resolution tables, removed values that aren't multiples of 8
    private int validResolutions[][] = ValidResolutions.$;

    private Logger logger;

    EncoderDevice(Context context, int width, int height) {
        this.context = context;
        this.width = width;
        this.height = height;
        this.logger = LoggerManager.getLogger(getClass());
    }

    void setProjection(MediaProjection projection) {
        this.projection = projection;
    }

    VirtualDisplay registerVirtualDisplay(Context context, String name, int originalWidth,
                                          int originalHeight, int densityDpi) {
        assert virtualDisplay == null;
        Surface surface = createDisplaySurface();
        if (surface == null)
            return null;
        return projection.createVirtualDisplay(name,
                width, height, 1,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface, null /*Callbacks*/, null /*Handler*/);
    }

    public void stop() {
        if (venc != null) {
            try {
                venc.signalEndOfInputStream();
            } catch (Exception ignored) {
            }
            venc = null;
        }
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }
    }

    private void destroyDisplaySurface(MediaCodec venc) {
        if (venc == null)
            return;
        // release this surface
        try {
            venc.stop();
            venc.release();
        } catch (Exception e) {
        }
        // see if this device is still in use
        if (this.venc != venc)
            return;
        // display is done, kill it
        this.venc = null;

        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }
    }

    protected abstract EncoderRunnable onSurfaceCreated(MediaCodec venc);

    private Surface createDisplaySurface() {
        if (venc != null) {
            // signal any old crap to end
            try {
                venc.signalEndOfInputStream();
            } catch (Exception e) {
            }
            venc = null;
        }

        int maxWidth;
        int maxHeight;
        int bitrate;

        try {
            File mediaProfiles = new File("/system/etc/media_profiles.xml");
            FileInputStream fin = new FileInputStream(mediaProfiles);
            byte[] bytes = new byte[(int) mediaProfiles.length()];
            fin.read(bytes);
            String xml = new String(bytes);
            RootElement root = new RootElement("MediaSettings");
            Element encoder = root.requireChild("VideoEncoderCap");
            final ArrayList<VideoEncoderCap> encoders = new ArrayList<VideoEncoderCap>();
            encoder.setElementListener(new ElementListener() {
                @Override
                public void end() {
                }

                @Override
                public void start(Attributes attributes) {
                    if (!TextUtils.equals(attributes.getValue("name"), "h264"))
                        return;
                    encoders.add(new VideoEncoderCap(attributes));
                }
            });
            Parsers.parse(new StringReader(xml), root.getContentHandler());
            if (encoders.size() != 1)
                throw new Exception("derp");

            VideoEncoderCap v = encoders.get(0);
            maxWidth = v.maxFrameWidth;
            maxHeight = v.maxFrameHeight;
            bitrate = v.maxBitRate;
        } catch (Exception e) {
            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);

            if (profile == null)
                profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);

            if (profile == null) {
                maxWidth = 640;
                maxHeight = 480;
                bitrate = 2000000;
            } else {
                maxWidth = profile.videoFrameWidth;
                maxHeight = profile.videoFrameHeight;
                bitrate = profile.videoBitRate;
            }
        }

        int max = Math.max(maxWidth, maxHeight);
        int min = Math.min(maxWidth, maxHeight);
        int resConstraint = context.getResources().getInteger(
                R.integer.config_maxDimension);

        double ratio;
        boolean resizeNeeded = false;
        int orientation = SettingsProvider.get().orientation();
        boolean landscape = orientation == Orientations.L;

        // Figure orientation and ratio first
        if (width > height) {
            // landscape
            landscape = true;
            ratio = (double) width / (double) height;
            if (resConstraint >= 0 && height > resConstraint) {
                min = resConstraint;
            }
            if (width > max || height > min) {
                resizeNeeded = true;
            }
        } else {
            // portrait
            ratio = (double) height / (double) width;
            if (resConstraint >= 0 && width > resConstraint) {
                min = resConstraint;
            }
            if (height > max || width > min) {
                resizeNeeded = true;
            }
        }

        if (resizeNeeded) {
            boolean matched = false;
            for (int[] resolution : validResolutions) {
                // All res are in landscape. Find the highest match
                if (resolution[0] <= max && resolution[1] <= min &&
                        (!matched || (resolution[0] > (landscape ? width : height)))
                        ) {
                    if (((double) resolution[0] / (double) resolution[1]) == ratio) {
                        // Got a valid one
                        if (landscape) {
                            width = resolution[0];
                            height = resolution[1];
                        } else {
                            height = resolution[0];
                            width = resolution[1];
                        }
                        matched = true;
                    }
                }
            }
            if (!matched) {
                // Scale requested or no match found. Go for the lowest... :(
                width = landscape ? 640 : 480;
                height = landscape ? 480 : 640;
                LoggerManager.getLogger(getClass()).info("Using lowest res.");
            }
        }


        MediaFormat video = MediaFormat.createVideoFormat("video/avc", width, height);

        video.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);

        video.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        video.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        video.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 3);

        // create a surface from the encoder
        logger.debug("Starting encoder at " + width + "x" + height);
        try {
            venc = MediaCodec.createEncoderByType("video/avc");
        } catch (IOException e) {
            logger.trace("Can't create AVC encoder!", e);
        }
        venc.configure(video, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        Surface surface = venc.createInputSurface();
        venc.start();

        EncoderRunnable runnable = onSurfaceCreated(venc);
        new Thread(runnable, "Encoder").start();
        return surface;
    }

    private static class VideoEncoderCap {
        int maxFrameWidth;
        int maxFrameHeight;
        int maxBitRate;

        public VideoEncoderCap(Attributes attributes) {
            maxFrameWidth = Integer.valueOf(attributes.getValue("maxFrameWidth"));
            maxFrameHeight = Integer.valueOf(attributes.getValue("maxFrameHeight"));
            maxBitRate = Integer.valueOf(attributes.getValue("maxBitRate"));
        }
    }

    abstract class EncoderRunnable implements Runnable {
        MediaCodec venc;

        public EncoderRunnable(MediaCodec venc) {
            this.venc = venc;
        }

        abstract void encode() throws Exception;

        protected void cleanup() {
            destroyDisplaySurface(venc);
            venc = null;
        }

        @Override
        final public void run() {
            try {
                encode();
            } catch (Exception e) {
                logger.trace("Encoder error", e);
            }
            cleanup();
            logger.debug("=======ENCODING COMPLETE=======");
        }
    }
}
