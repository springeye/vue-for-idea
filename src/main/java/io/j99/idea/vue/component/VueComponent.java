package io.j99.idea.vue.component;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.xml.XmlAttributeDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by apple on 16/2/25.
 */
public class VueComponent extends AbstractProjectComponent {
    private static List<String> directiveNames = Arrays.asList("click",
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
            "unload");
    private static List<String> jsEventNames = Arrays.asList("click",
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
            "unload");
    private ArrayList<VueAttributeDescriptor> attrs=new ArrayList();
    private XmlAttributeDescriptor[] attrArray;
    private HashMap<String, VueAttributeDescriptor> attrLookup=new HashMap<>();
    private VueComponent(Project project) {
        super(project);
    }

    public ArrayList<VueAttributeDescriptor> getAttrs() {
        return attrs;
    }

    public XmlAttributeDescriptor[] getAttrArray() {
        return attrArray;
    }

    public HashMap<String, VueAttributeDescriptor> getAttrLookup() {
        return attrLookup;
    }

    @Override
    public void initComponent() {
        super.initComponent();
        directiveNames.forEach((name)->
                attrs.add(new VueAttributeDescriptor("v-"+name))
        );
        jsEventNames.forEach((name)->
                attrs.add(new VueAttributeDescriptor("@"+name))
        );
        attrs.forEach((descriptor)->
                attrLookup.put(descriptor.getName(),descriptor)
        );
        attrArray=new XmlAttributeDescriptor[attrs.size()];
        attrArray=attrs.toArray(attrArray);
    }

    @NotNull
    @Override
    public String getComponentName() {
        return  VueComponent.class.getSimpleName();
    }

    public static VueComponent getInstance(Project project) {
        return project.getComponent(VueComponent.class);
    }
}
