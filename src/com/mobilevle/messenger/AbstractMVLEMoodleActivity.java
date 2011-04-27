package com.mobilevle.messenger;


import com.mobilevle.core.moodle.MoodleVLEHandler;
import com.mobilevle.core.MobileVLECoreException;
import android.os.Bundle;
import android.app.Activity;

/**
 * <p>Initializes a {@link MoodleVLEHandler}</p>
 *
 * @author johnhunsley
 *         Date: 29-Nov-2010
 *         Time: 14:13:56
 */
public abstract class AbstractMVLEMoodleActivity extends Activity {
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
