package io.j99.idea.vue.component

import com.intellij.openapi.components.AbstractProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.xml.XmlAttributeDescriptor

import java.util.ArrayList
import java.util.HashMap
import kotlin.properties.Delegates

/**
 * Created by android on 15-12-28.
 */
class VueComponent private constructor(project: Project) : AbstractProjectComponent(project) {
    var directiveNames: Array<String?> = arrayOf(
            "text",
            "html",
            "if",
            "show",
            "else",
            "for",
            "on",
            "bind",
            "model",
            "ref",
            "el",
            "pre",
            "cloak"
    )
    var jsEventNames: Array<String?> = arrayOf(
            "click",
            "abort",
            "blur",
            "change",
            "dblclick",
            "keydown",
            "keypress",
            "keyup",
            "load",
            "keyup",
            "mousedown",
            "mousemove",
            "mouseout",
            "mouseover",
            "mouseup",
            "reset",
            "resize",
            "select",
            "submit",
            "unload"

    )
    companion object factory{
        fun getInstance(project : Project?) : VueComponent {
            return project?.getComponent(VueComponent::class.java)!!
        }
    }
    var attrs: MutableList<VueAttributeDescriptor> =arrayListOf()
    var attrArray: Array<XmlAttributeDescriptor> by Delegates.notNull()
    var attrLookup :HashMap<String, VueAttributeDescriptor> = hashMapOf()


    override fun initComponent() {
        var attrsNames = directiveNames.map { name -> VueAttributeDescriptor("v-" + name)}
        var eventNames= jsEventNames.map { name -> VueAttributeDescriptor("@" + name)}
        attrs.addAll(attrsNames);
        attrs.addAll(eventNames);
        attrs.forEach {
            descriptor -> attrLookup.put(descriptor.name!!, descriptor)
        }
        attrArray = attrs.toTypedArray()
    }

    override fun getComponentName(): String {
        return "VueProjectComponent"
    }

}
