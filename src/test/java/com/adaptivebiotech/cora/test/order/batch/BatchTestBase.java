/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.order.batch;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.openOutputStream;
import static org.apache.poi.ss.usermodel.CellType.STRING;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class BatchTestBase extends CoraBaseBrowser {

    protected void resetBatchOrder (String orderNumber) {
        String query = "update cora.orders set salesforce_order_id=null, salesforce_order_number=null where order_number='%s'";
        coraDb.executeUpdate (format (query, orderNumber));
    }

    protected void prepManifestFile (String template,
                                     String manifestFile,
                                     Map <String, Map <String, String>> sampleName) {
        int maxRow = 20;
        int maxCell = 78;
        try (FileInputStream inputStream = new FileInputStream (getSystemResource (template).getPath ());
                Workbook workbook = WorkbookFactory.create (inputStream);
                FileOutputStream outputStream = openOutputStream (new File (manifestFile))) {

            Sheet sheet = workbook.getSheetAt (0);
            for (int rowIdx = 0; rowIdx < maxRow; ++rowIdx)
                for (int cellIdx = 0; cellIdx < maxCell; ++cellIdx)
                    if (sheet.getRow (rowIdx) != null) {
                        Cell cell = sheet.getRow (rowIdx).getCell (cellIdx);
                        sampleName.entrySet ().forEach (m -> {
                            if (cell != null && cell.getCellType ().equals (STRING) && m.getKey ()
                                                                                        .equals (cell.getStringCellValue ()))
                                cell.setCellValue (m.getValue ().get ("workflow"));
                        });
                    }

            workbook.write (outputStream);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }
}
