/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cylan.jiafeigou.support.block.log;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.cylan.jiafeigou.support.block.BlockCanaryCore;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;


public class PerformanceUtils {
    private static final String TAG = "PerformanceUtils";

    private static int sCoreNum = 0;
    private static long sTotalMemo = 0;

    private PerformanceUtils() {
        throw new InstantiationError("Must not instantiate this class");
    }

    private static HashMap<String, Long> timeCostMap = new HashMap<>();

    /**
     * add by hunt
     *
     * @param tag
     */
    public static void startTrace(String tag) {
        AppLogger.d("startTrace: " + tag);
        timeCostMap.put(tag, System.currentTimeMillis());
    }

    /**
     * add by hunt
     *
     * @param tag
     */
    public static void stopTrace(String tag) {
        Long time = timeCostMap.get(tag);
        if (time != null) {
            AppLogger.d(String.format(Locale.getDefault(), "%s cost:%s ms", tag, (System.currentTimeMillis() - time)));
            timeCostMap.remove(tag);
        }
    }

    /**
     * Get cpu core number
     *
     * @return int cpu core number
     */
    public static int getNumCores() {
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                return Pattern.matches("cpu[0-9]", pathname.getName());
            }
        }

        if (sCoreNum == 0) {
            try {
                // Get directory containing CPU info
                File dir = new File("/sys/devices/system/cpu/");
                // Filter to only list the devices we care about
                File[] files = dir.listFiles(new CpuFilter());
                // Return the number of cores (virtual CPU devices)
                sCoreNum = files.length;
            } catch (Exception e) {
                Log.e(TAG, "getNumCores exception", e);
                sCoreNum = 1;
            }
        }
        return sCoreNum;
    }

    public static long getFreeMemory() {
        ActivityManager am = (ActivityManager) BlockCanaryCore.getContext().getContext().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem / 1024;
    }

    public static long getTotalMemory() {
        if (sTotalMemo == 0) {
            String str1 = "/proc/meminfo";
            String str2;
            String[] arrayOfString;
            long initial_memory = -1;
            FileReader localFileReader = null;
            try {
                localFileReader = new FileReader(str1);
                BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
                str2 = localBufferedReader.readLine();

                if (str2 != null) {
                    arrayOfString = str2.split("\\s+");
                    initial_memory = Integer.valueOf(arrayOfString[1]);
                }
                localBufferedReader.close();

            } catch (IOException e) {
                Log.e(TAG, "getTotalMemory exception = ", e);
            } finally {
                if (localFileReader != null) {
                    try {
                        localFileReader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "close localFileReader exception = ", e);
                    }
                }
            }
            sTotalMemo = initial_memory;
        }
        return sTotalMemo;
    }
}