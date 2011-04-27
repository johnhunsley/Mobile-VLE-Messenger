package com.mobilevle.messenger;

import android.app.ListActivity;
import android.os.Bundle;
import com.mobilevle.core.moodle.MoodleVLEHandler;
import com.mobilevle.core.MobileVLECoreException;

/**
 * <p></p>
 *
 * @author johnhunsley
 *         Date: 30-Nov-2010
 *         Time: 21:20:53
 */
public abstract class AbstractMVLEMoodleListActivity extends ListActivity {
    protected MoodleVLEHandler handler;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        try {
            handler = (MoodleVLEHandler)VLEHandlerProvider.provideVLEHandler(this);

        } catch (MobileVLECoreException e) {
            e.printStackTrace();
        }
    }
}
