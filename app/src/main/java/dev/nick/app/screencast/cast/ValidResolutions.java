package dev.nick.app.screencast.cast;

import java.util.ArrayList;
import java.util.List;

public abstract class ValidResolutions {
    public static final int $[][] = {
            // CEA Resolutions
            {640, 480},
            {720, 480},
            {720, 576},
            {1280, 720},
            {1920, 1080},
            // VESA Resolutions
            {800, 600},
            {1024, 768},
            {1152, 864},
            {1280, 768},
            {1280, 800},
            {1360, 768},
            {1366, 768},
            {1280, 1024},
            //{ 1400, 1050 },
            //{ 1440, 900 },
            //{ 1600, 900 },
            {1600, 1200},
            //{ 1680, 1024 },
            //{ 1680, 1050 },
            {1920, 1200},
            // HH Resolutions
            {800, 480},
            {854, 480},
            {864, 480},
            {640, 360},
            //{ 960, 540 },
            {848, 480}
    };

    public static List<String> string() {
        List<String> out = new ArrayList<>();
        for (int[] re : $) {
            out.add(re[0] + "x" + re[1]);
        }
        return out;
    }
}
