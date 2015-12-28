package io.j99.idea.vue.component

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.html.dtd.HtmlAttributeDescriptorImpl
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.xml.impl.BasicXmlAttributeDescriptor
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor

/**
 * Created by android on 15-12-28.
 */
class VueAttributeDescriptor(name: String) : AnyXmlAttributeDescriptor(name)
