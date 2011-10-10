package org.gridman.coherence;

import com.tangosol.run.xml.SimpleElement;
import com.tangosol.run.xml.XmlElement;

import java.util.Iterator;

/**
 * @author Jonathan Knight
 */
public class XmlUtils {

    public static void removeElement(XmlElement xmlElement, String... elementsToRemove) {
        removeElement(xmlElement, 0, elementsToRemove);
    }

    @SuppressWarnings({"unchecked"})
    private static void removeElement(XmlElement xmlElement, int index, String... elementsToRemove) {
        if (elementsToRemove.length == 0) {
            return;
        }

        if ("*".equals(elementsToRemove[index])) {
            xmlElement.getElementList().clear();
        } else {
            Iterator<SimpleElement> it = xmlElement.getElements(elementsToRemove[index]);
            index++;

            if (index >= elementsToRemove.length) {
                while (it.hasNext()) {
                    it.next();
                    it.remove();
                }
            } else {
                while (it.hasNext()) {
                    XmlElement child = it.next();
                    removeElement(child, index, elementsToRemove);
                }
            }
        }
    }
}
