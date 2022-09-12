/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.api;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestPass;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.emr.TokenData;
import com.adaptivebiotech.test.utils.HttpClientHelper;
import com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class CoraDebugApi extends HttpClientHelper {

    public void login () {
        resetheaders ();
        login (coraTestUser, coraTestPass);
    }

    public void login (String user, String pass) {
        Map <String, String> forms = new HashMap <> ();
        forms.put ("userName", user);
        forms.put ("password", pass);
        formPost (coraTestUrl + "/cora/login", forms);
    }

    public void storeContainer (Containers containers, Container freezer) {
        String url = coraTestUrl + "/cora/debug/storeContainer";
        containers.list.forEach (c -> {
            Map <String, UUID> props = new HashMap <> ();
            props.put ("containerId", c.id);
            props.put ("rootContainerId", freezer.id);
            post (url, body (props));
        });
    }

    public void setWorkflowProperties (OrderTest orderTest, Map <WorkflowProperty, String> properties) {
        String url = coraTestUrl + "/cora/debug/forceWorkflowProperty";
        properties.entrySet ().forEach (wp -> {
            Map <String, Object> props = new HashMap <> ();
            props.put ("propertyName", wp.getKey ().name ());
            props.put ("propertyValue", wp.getValue ());
            props.put ("workflowId", orderTest.workflowId);
            post (url, body (props));
        });
    }

    public void resetAccountLogin (String email) {
        Map <String, String> params = new HashMap <> ();
        params.put ("emails", email);
        post (coraTestUrl + "/cora/debug/resetAccountLoginSubmit", body (params));
    }

    public String renewCoraMemoryCache (TokenData tokenData) {
        Map <String, String> params = new HashMap <> ();
        params.put ("tokenData", mapper.writeValueAsString (tokenData));
        String url = coraTestUrl + "/cora/debug/emrTokenSubmit";
        post (url, body (params));
        return tokenData.id;
    }
}
