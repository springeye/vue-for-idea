package io.j99.idea.vue;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.util.ResourceBundle;

/**
 * Created by android on 15-12-25.
 */
public class VueBundle {
    @NonNls
    public static final String LOG_ID = "#com.wix.vue";


    /** The {@link ResourceBundle} path. */
    @NonNls
    protected static final String BUNDLE_NAME = "io.j99.idea.vue.localization.strings";
    /**
     * The {@link ResourceBundle} instance.
     *
     * @see #BUNDLE_NAME
     */
    protected static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    /** {@link VueBundle} is a non-instantiable static class. */
    private VueBundle() {
    }
    /**
     * Load a {@link String} from the {@link #BUNDLE} {@link ResourceBundle}.
     *
     * @param key    the key of the resource.
     * @param params the optional parameters for the specific resource.
     * @return the {@link String} value or {@code null} if no resource found for the key.
     */
    public static String message(@PropertyKey(resourceBundle = BUNDLE_NAME) String key, Object... params) {
        return CommonBundle.message(BUNDLE, key, params);
    }

    /**
     * Load a {@link String} from the {@link #BUNDLE} {@link ResourceBundle}.
     *
     * @param key    the key of the resource.
     * @param params the optional parameters for the specific resource.
     * @return the {@link String} value or an empty {@link String} if no resource found for the key.
     */
    public static String messageOrBlank(@PropertyKey(resourceBundle = BUNDLE_NAME) String key, Object... params) {
        return CommonBundle.messageOrDefault(BUNDLE, key, "", params);
    }
}
