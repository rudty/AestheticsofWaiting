package com.viewpagerindicator;

public interface IconPagerAdapter {
    /**
     * Get g_icon representing the page at {@code index} in the adapter.
     */
    int getIconResId(int index);

    // From PagerAdapter
    int getCount();
}
