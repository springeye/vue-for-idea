package io.j99.idea.vue.component;

import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import org.jetbrains.annotations.Nullable;

/**
 * Created by apple on 16/2/25.
 */
public class VueAttributeDescriptorProvider implements XmlAttributeDescriptorsProvider {
    private VueComponent vueComponent;
    @Override
    public XmlAttributeDescriptor[] getAttributeDescriptors(XmlTag xmlTag) {
        if (xmlTag == null) {
            return XmlAttributeDescriptor.EMPTY;
        }
        if (vueComponent == null) {
            vueComponent = VueComponent.getInstance(xmlTag.getProject());
        }
        return vueComponent.getAttrArray();
    }

    @Nullable
    @Override
    public XmlAttributeDescriptor getAttributeDescriptor(String attrName, XmlTag tag) {
        if (tag != null && attrName != null) {
            if (vueComponent == null) {
                vueComponent = VueComponent.getInstance(tag.getProject());
            }
            return vueComponent.getAttrLookup().get(attrName);
        }
        return null;
    }
}
