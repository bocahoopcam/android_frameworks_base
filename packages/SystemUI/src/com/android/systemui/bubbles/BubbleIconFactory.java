/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.systemui.bubbles;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;

import com.android.launcher3.icons.BaseIconFactory;
import com.android.launcher3.icons.BitmapInfo;
import com.android.launcher3.icons.ShadowGenerator;
import com.android.systemui.R;

/**
 * Factory for creating normalized bubble icons.
 * We are not using Launcher's IconFactory because bubbles only runs on the UI thread,
 * so there is no need to manage a pool across multiple threads.
 */
public class BubbleIconFactory extends BaseIconFactory {

    protected BubbleIconFactory(Context context) {
        super(context, context.getResources().getConfiguration().densityDpi,
                context.getResources().getDimensionPixelSize(R.dimen.individual_bubble_size));
    }

    int getBadgeSize() {
        return mContext.getResources().getDimensionPixelSize(
                com.android.launcher3.icons.R.dimen.profile_badge_size);
    }
    /**
     * Returns the drawable that the developer has provided to display in the bubble.
     */
    Drawable getBubbleDrawable(@NonNull final Context context,
            @Nullable final ShortcutInfo shortcutInfo, @Nullable final Icon ic) {
        if (shortcutInfo != null) {
            LauncherApps launcherApps =
                    (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
            int density = context.getResources().getConfiguration().densityDpi;
            return launcherApps.getShortcutIconDrawable(shortcutInfo, density);
        } else {
            if (ic != null) {
                if (ic.getType() == Icon.TYPE_URI
                        || ic.getType() == Icon.TYPE_URI_ADAPTIVE_BITMAP) {
                    context.grantUriPermission(context.getPackageName(),
                            ic.getUri(),
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                return ic.loadDrawable(context);
            }
            return null;
        }
    }

    /**
     * Returns a {@link BitmapInfo} for the app-badge that is shown on top of each bubble. This
     * will include the workprofile indicator on the badge if appropriate.
     */
    BitmapInfo getBadgeBitmap(Drawable userBadgedAppIcon) {
        Bitmap userBadgedBitmap = createIconBitmap(
                userBadgedAppIcon, 1f, getBadgeSize());

        Canvas c = new Canvas();
        ShadowGenerator shadowGenerator = new ShadowGenerator(getBadgeSize());
        c.setBitmap(userBadgedBitmap);
        shadowGenerator.recreateIcon(Bitmap.createBitmap(userBadgedBitmap), c);
        return createIconBitmap(userBadgedBitmap);
    }

    /**
     * Returns a {@link BitmapInfo} for the entire bubble icon including the badge.
     */
    BitmapInfo getBubbleBitmap(Drawable bubble, BitmapInfo badge) {
        BitmapInfo bubbleIconInfo = createBadgedIconBitmap(bubble,
                null /* user */,
                true /* shrinkNonAdaptiveIcons */);

        badgeWithDrawable(bubbleIconInfo.icon,
                new BitmapDrawable(mContext.getResources(), badge.icon));
        return bubbleIconInfo;
    }
}
