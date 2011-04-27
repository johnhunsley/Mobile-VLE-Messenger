package com.mobilevle.messenger;

import com.mobilevle.core.VLEHandler;
import com.mobilevle.core.VLEHandlerFactory;
import com.mobilevle.core.MobileVLECoreException;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.io.InputStream;
import java.io.IOException;

import android.util.Log;
import android.content.res.Resources;
import android.content.Context;

/**
 * <p>Provides an instance of a {@link VLEHandler} as defined in the vlehandler.properties file
 * available from a static context within activities in the mobile vle app</p>
 *
 * @author johnhunsley
 *         Date: 12-Nov-2010
 *         Time: 00:20:35
 */
public class VLEHandlerProvider {
    public static VLEHandler handler;
    private static final String VLE_HANDLER_PROP_KEY = "vlehandler";

    /**
     * <p>Loads the vle handler properties from the vlehandler.properties file. Creates a
     * new {@link VLEHandler} implementation as defined by the required property 'vlehandler'.
     * Adds all the other property values, which pertain to the vle handler implementation, to
     * a Map containing both key and value and uses the vlehander value and the map to get
     * an instance of the {@link VLEHandler} from the {@link VLEHandlerFactory}</p>
     * @param context
     * @return {@link VLEHandler}
     * @throws MobileVLECoreException if no vlehandler property is defined int he vlehandler.props file
     */
    public static VLEHandler provideVLEHandler(final Context context) throws MobileVLECoreException {

        if(handler == null) {
            VLEHandlerFactory factory = VLEHandlerFactory.getInstance();

            try {
                Resources resources = context.getResources();
                InputStream rawResource = resources.openRawResource(R.raw.vlehandler);
                Properties properties = new Properties();
                properties.load(rawResource);
                Log.i("VLEHandlerProvider", "The properties are now loaded");
                Log.i("VLEHandlerProvider", "properties: " + properties);
                Set keys = properties.keySet();
                Map<String, String> params = new HashMap<String, String>();

                for(Object key : keys) {
                    String keyStr = (String)key;
                    String property = properties.getProperty(keyStr);
                    params.put(keyStr, property);
                }

                String VLEHandlerType = properties.getProperty(VLE_HANDLER_PROP_KEY);

                if(VLEHandlerType == null || VLEHandlerType.length() < 1)
                    throw new MobileVLECoreException("No vle handler type defined in vlehandler.properties");

                handler = factory.getVLEHandler(VLEHandlerType, params);
                Log.i("VLEHandlerProvider", "Loaded VLEHandler "+handler.getClass().getName());

            } catch (MobileVLECoreException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return handler;
    }
}
