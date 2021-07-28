package com.adaptivebiotech.cora.test.smoke;

import static com.adaptivebiotech.cora.utils.PageHelper.LinkShipment.SalesforceOrder;
import static com.adaptivebiotech.cora.utils.TestHelper.newPatient;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestPass;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.TubeBox5x5;
import static com.adaptivebiotech.test.utils.PageHelper.LinkType.Project;
import static com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Pending;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.MrdBatchReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.container.AddContainer;
import com.adaptivebiotech.cora.ui.container.ContainerList;
import com.adaptivebiotech.cora.ui.mira.Mira;
import com.adaptivebiotech.cora.ui.mira.MirasList;
import com.adaptivebiotech.cora.ui.order.Batch;
import com.adaptivebiotech.cora.ui.order.Diagnostic;
import com.adaptivebiotech.cora.ui.order.OrderTestsList;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.patient.PatientDetail;
import com.adaptivebiotech.cora.ui.patient.PatientsList;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.cora.ui.shipment.ShipmentList;
import com.adaptivebiotech.cora.ui.task.Task;
import com.adaptivebiotech.cora.ui.task.TaskDetail;
import com.adaptivebiotech.cora.ui.task.TaskList;
import com.adaptivebiotech.cora.ui.task.TaskStatus;
import com.adaptivebiotech.cora.ui.utilities.AuditTool;
import com.adaptivebiotech.cora.ui.utilities.BarcodeComparisonTool;
import com.adaptivebiotech.cora.utils.PageHelper.MiraLab;

public class SampleTest {

    private Map <String, String> map;
    
    @Test
    public void main () {
        System.out.println ("Print");
        map = getMapA();
        
        System.out.println (map);
        
        map = getMapB();
        
        System.out.println (map);
        
        
    }

    private Map <String, String> getMapA () {
        // TODO Auto-generated method stub
        Map <String, String> mapA = new HashMap<> ();
        mapA.put("flag", "true");
        return mapA;
    }
    
    private Map <String, String> getMapB () {
        // TODO Auto-generated method stub
        Map <String, String> mapB = new HashMap<> ();
        mapB.put("flag", "false");
        return mapB;
    }

}
