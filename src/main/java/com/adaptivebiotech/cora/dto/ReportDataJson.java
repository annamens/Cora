package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import java.util.List;
import com.adaptivebiotech.cora.utils.PageHelper.MutationStatus;

/**
 * @author jpatel
 *         <a href="mailto:jpatel@adaptivebiotech.com">jpatel@adaptivebiotech.com</a>
 */
public final class ReportDataJson {

    public int             version;
    public boolean         isFailure;
    public ShmReportResult shmReportResult;

    @Override
    public String toString () {
        try {
            return mapper.writeValueAsString (this);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    public static final class ShmSequenceList {
        public String  locus;
        public String  sequence;
        public double  percentMutation;
        public boolean productive;
        public String  vSegment;
    }

    public static final class ShmReportResult {
        public List <ShmSequenceList> shmSequenceList;
        public MutationStatus         mutationStatus;
    }
}
