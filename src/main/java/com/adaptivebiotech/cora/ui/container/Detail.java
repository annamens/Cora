package com.adaptivebiotech.cora.ui.container;

import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.getContainerType;
import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.testng.Assert.assertTrue;
import java.util.List;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.ui.cora.CoraPage;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class Detail extends CoraPage {

    private final String top        = "[containers='ctrl.containerDetail.container']";
    private final String attachment = ".attachments-table-row:nth-child(%d)";

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".navbar"));
        assertTrue (waitUntilVisible ("[role='tablist']"));
        assertTrue (isTextInElement ("[role='tablist'] .active", "Details"));
        assertTrue (waitUntilVisible (".container-details"));
        assertTrue (waitUntilVisible (top));
        pageLoading ();
    }

    public void gotoHistory () {
        assertTrue (click ("[ui-sref*='main.container.details.history']"));
        pageLoading ();
    }

    public Container parseHoldingDetail () {
        String childs = "[ng-repeat='child in ctrl.childContainers']";
        Container container = parsePrimaryDetail ();
        container.children = waitForElements (childs).stream ().map (el -> {
            Container c = new Container ();
            c.id = getConId (getAttribute (el, "[ng-bind='child.container.containerNumber']", "href"));
            c.containerNumber = getText (el, "[ng-bind='child.container.containerNumber']");
            c.containerType = getContainerType (getText (el, "[ng-bind*='containerTypeDisplayName']"));
            c.name = getText (el, "[ng-bind*='child.container.displayName']");
            c.root = container;
            return c;
        }).collect (toList ());
        return container;
    }

    public Container parseChildDetail () {
        String holding = "[containers='ctrl.parentContainer']";
        Container root = new Container ();
        root.id = getConId (getAttribute (holding + " [ng-bind*='containerNumber']", "href"));
        root.containerNumber = getText (holding + " [ng-bind*='containerNumber']");
        root.containerType = getContainerType (getText (holding + " [ng-bind*='containerTypeDisplayName']"));
        if (isElementVisible (holding + " [ng-bind*='displayName']"))
            root.name = getText (holding + " [ng-bind*='displayName']");
        if (isElementVisible (holding + " [ng-bind*='barcode']"))
            root.barcode = getText (holding + " [ng-bind*='barcode']");
        root.location = getText (holding + " [ng-bind*='location']");
        Container container = parsePrimaryDetail ();
        container.root = root;
        return container;
    }

    public Container parsePrimaryDetail () {
        Container container = new Container ();
        container.containerNumber = getText (top + " [ng-bind*='containerNumber']");
        container.containerType = getContainerType (getText (top + " [ng-bind*='containerTypeDisplayName']"));
        if (isElementVisible (top + " [ng-bind*='displayName']"))
            container.name = getText (top + " [ng-bind*='displayName']");
        if (isElementVisible (top + " [ng-bind*='barcode']"))
            container.barcode = getText (top + " [ng-bind*='barcode']");
        if (isElementVisible (top + " [ng-bind*='depleted']"))
            container.depleted = toBoolean (getText (top + " [ng-bind*='depleted']"));
        container.location = getText (top + " [ng-bind*='location']");
        return container;
    }

    public void uploadAttachments (String... files) {
        String attachments = asList (files).parallelStream ()
                                           .map (f -> getSystemResource (f).getPath ())
                                           .collect (joining ("\n"));
        assertTrue (click ("[attachment-upload='ctrl.containerDetail.container.attachments'] button"));
        waitForElement ("input[ngf-select*='ctrl.onUpload']").sendKeys (attachments);
        pageLoading ();
    }

    public String getFileExtErr () {
        return getText ("[ng-show='ctrl.errorPattern']");
    }

    public String getMaxFileErr () {
        return getText ("[ng-show='ctrl.errorMaxFiles']");
    }

    public void viewAttachment (int idx) {
        String file = getText (format (attachment, idx) + " [ng-bind='attachment.name']");
        assertTrue (click (format (attachment, idx) + " [ng-click*='ctrl.onPreview']"));
        assertTrue (isTextInElement (popupTitle, file));
    }

    public void deleteAttachment (int idx) {
        assertTrue (click (format (attachment, idx) + " .glyphicon-trash"));
        assertTrue (isTextInElement (popupTitle, "Delete Attachment"));
        clickPopupOK ();
    }

    public List <String> getDetailHistory () {
        return getTextList (".container-details .ab-panel:nth-child(5) li");
    }
}
