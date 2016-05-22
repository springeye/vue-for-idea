package io.j99.idea.vue;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Created by apple on 16/2/26.
 */
public class VueFileTypeFactory extends FileTypeFactory {
    @Override
    public void createFileTypes(@NotNull FileTypeConsumer fileTypeConsumer) {
        for (String type:VueFileType.DEFAULT_ASSOCIATED_EXTENSIONS) {
            fileTypeConsumer.consume(VueFileType.INSTANCE, type);
        }
    }
}
