package dev.nick.app.screencast.content;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class TransactionSafeActivity extends AppCompatActivity {

    protected Fragment mShowingFragment;

    /**
     * Show fragment page by replace the given containerId, if you have data to set
     * give a bundle.
     *
     * @param containerId The id to replace.
     * @param fragment    The fragment to show.
     * @param bundle      The data of the fragment if it has.
     */
    protected boolean placeFragment(final int containerId,
                                    Fragment fragment, Bundle bundle) {
        return placeFragment(containerId, fragment, bundle, true);
    }

    /**
     * Show fragment page by replace the given containerId, if you have data to set
     * give a bundle.
     *
     * @param containerId The id to replace.
     * @param f           The fragment to show.
     * @param bundle      The data of the fragment if it has.
     * @param animate     True if you want to animate the fragment.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    protected boolean placeFragment(final int containerId,
                                    Fragment f, Bundle bundle, boolean animate) {

        if (isDestroyed() || f == null) {
            return false;
        }

        if (bundle != null) {
            f.setArguments(bundle);
        }

        if (!animate) {
            getSupportFragmentManager().beginTransaction()
                    .replace(containerId, f).commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(containerId, f).commit();
        }
        mShowingFragment = f;
        return true;
    }

    /**
     * Remove a fragment that is attached, with animation.
     *
     * @param f The fragment to remove.
     * @return True if successfully removed.
     * @see #removeFragment(Fragment, boolean)
     */
    protected boolean removeFragment(final Fragment f) {
        return removeFragment(f, true);
    }

    /**
     * Remove a fragment that is attached.
     *
     * @param f       The fragment to remove.
     * @param animate True if you want to animate the fragment.
     * @return True if successfully removed.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    protected boolean removeFragment(final Fragment f, boolean animate) {

        if (!isDestroyed() || f == null) {
            return false;
        }

        if (!animate) {
            getSupportFragmentManager().beginTransaction().remove(f).commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .remove(f)
                    .commit();//TODO Ignore the result?
        }
        mShowingFragment = null;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }
}
