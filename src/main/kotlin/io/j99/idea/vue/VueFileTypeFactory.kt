package io.j99.idea.vue

import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory

/**
 * Created by android on 15-12-25.
 */
class VueFileTypeFactory : FileTypeFactory() {
    override fun createFileTypes(fileTypeConsumer: FileTypeConsumer) {
        for (i in VueFileType.DEFAULT_ASSOCIATED_EXTENSIONS.indices) {
            fileTypeConsumer.consume(VueFileType.INSTANCE, VueFileType.DEFAULT_ASSOCIATED_EXTENSIONS[i])
        }
    }
}
