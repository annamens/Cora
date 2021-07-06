package com.adaptivebiotech.cora.ui.task;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.openqa.selenium.Keys.ENTER;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.test.utils.PageHelper.LinkType;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class Task extends CoraPage {

    public Task () {
        staticNavBarHeight = 195;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible ("[data-ng-click*='task-save']"));
        assertTrue (waitUntilVisible ("#taskConfig"));
    }

    public void selectTask (String task) {
        assertTrue (clickAndSelectText ("#taskConfig", task));
        assertEquals (getFirstSelectedText ("#taskConfig"), task);
    }

    public void enterTaskName (String name) {
        assertTrue (setText ("[name='taskName']", name));
    }

    public void linkTaskTo (LinkType type, String value) {
        // pick the 1st one
        linkTaskTo (type, value, 1);
    }

    public void linkTaskTo (LinkType type, String value, int idx) {
        assertTrue (clickAndSelectValue ("#relatedLinkType", type.name ()));
        assertTrue (waitUntilVisible (".searchInput"));
        assertTrue (setText (".searchInput", value));
        assertTrue (pressKey (ENTER));

        assertTrue (click (format ("div.matches li:nth-child(%s)", idx + 1)));
        assertTrue (waitUntilVisible (".related-link-summary"));
        assertTrue (waitUntilVisible ("[data-ng-click='ctrl.clearLinkType()']"));
    }

    public void chooseScriptImage (String imageName) {
        assertTrue (setText ("#select_task_definition-selectized", imageName));
        assertTrue (click ("#select_task_definition + .selectize-control .selectize-dropdown-content .selector-item:nth-child(1)"));
    }

    public void enterImage (String imageName) {
        assertTrue (setText ("[name='taskInput_image']", imageName));
    }

    public void selectNumCPUs (String numCPUs) {
        assertTrue (setText ("#select_cpu-selectized", numCPUs));
        assertTrue (click ("#select_cpu + .selectize-control .selectize-dropdown-content .selector-item:nth-child(1)"));
    }

    public void enterCPU (String cpu) {
        assertTrue (setText ("[name='taskInput_cpu']", cpu));
    }

    public void selectRAM (String ram) {
        assertTrue (setText ("#select_ram-selectized", ram));
        assertTrue (click ("#select_ram + .selectize-control .selectize-dropdown-content .option:nth-child(1)"));
    }

    public void enterRAM (String ram) {
        assertTrue (setText ("[name='taskInput_ram']", ram));
    }

    public void enterArgs (String args) {
        assertTrue (setText ("[name='taskInput_args']", args));
    }

    public void attachedFiles (String... files) {
        asList (files).forEach (f -> {
            waitForElement ("#taskFiles").sendKeys (getSystemResource (f).getPath ());
        });
    }

    public void clickRun () {
        assertTrue (click ("[data-ng-click*='task-save']"));
    }

    public void clickTaskStatus () {
        assertTrue (click ("//a[text()='Task Status']"));
        pageLoading ();
    }

    public void clickTaskDetail () {
        assertTrue (click ("//a[text()='Task Detail']"));
        pageLoading ();
    }
}
