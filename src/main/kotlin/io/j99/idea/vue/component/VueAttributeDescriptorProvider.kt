package io.j99.idea.vue.component

import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.xml.XmlAttributeDescriptorsProvider

/**
 * Created by android on 15-12-28.
 */
class VueAttributeDescriptorProvider : XmlAttributeDescriptorsProvider {
    var vueComponent: VueComponent? = null

    override fun getAttributeDescriptors(tag: XmlTag?): Array<XmlAttributeDescriptor>? {
        if (tag == null) {
            return XmlAttributeDescriptor.EMPTY
        }
        if (vueComponent == null) {
            vueComponent = VueComponent.getInstance(tag.project)
        }
        return vueComponent?.attrArray
    }

    override fun getAttributeDescriptor(attrName: String?, tag: XmlTag?): XmlAttributeDescriptor? {
        if (tag != null && attrName != null) {
            if (vueComponent == null) {
                vueComponent = VueComponent.getInstance(tag.project)
            }
            return vueComponent!!.attrLookup[attrName]
        }
        return null
    }

}
