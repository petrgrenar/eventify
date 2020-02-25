package com.eventify.xgrenar.eventify;

import android.content.Context;

public class ResourceUtility {
    /**
     * Returns the drawable id based on the name
     * @param context context
     * @param resourceName resource name
     * @return resource id.
     */
    public static int getResourceDrawableID(Context context, String resourceName){
        return context.getResources().getIdentifier(resourceName,
                "drawable", context.getPackageName());
    }
}
