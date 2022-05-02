package com.adaptivebiotech.cora.dto.report;

import static com.adaptivebiotech.pipeline.utils.TestHelper.CountMeasure.templates;
import static com.adaptivebiotech.pipeline.utils.TestHelper.Locus.BCell;
import static com.adaptivebiotech.pipeline.utils.TestHelper.Locus.IGH;
import static com.adaptivebiotech.pipeline.utils.TestHelper.Locus.IGKL;
import static com.adaptivebiotech.pipeline.utils.TestHelper.Locus.TCRB;
import static com.adaptivebiotech.pipeline.utils.TestHelper.Locus.TCRG;
import static com.adaptivebiotech.test.utils.PageHelper.Compartment.CellFree;
import static com.adaptivebiotech.test.utils.PageHelper.ReportType.clonality;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.gDNA;
import static com.google.common.collect.Iterables.getLast;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.text.WordUtils.capitalize;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.adaptivebiotech.picasso.dto.ReportRender;
import com.adaptivebiotech.pipeline.dto.mrd.ClinicalReport.ResultClone;
import com.adaptivebiotech.pipeline.utils.TestHelper.Locus;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class ClonoSeq {

    public int                 pageSize;
    public int                 pageSizeSHM;
    public ReportRender        report;
    public boolean             isCLIA;
    public boolean             isIVD;
    public boolean             isCE;
    public boolean             isIUO;
    public boolean             isEos;
    public boolean             isCorrected;
    public boolean             isFailed;
    public boolean             isClonality;
    public boolean             isPolyclonal;
    public boolean             isCellfree;
    public boolean             isSHM;
    public boolean             fromOutsideLab;
    public boolean             hasComments;
    public boolean             isClonoSEQV1;
    public boolean             isLifted;
    public Locus               locus;
    public LocalDate           lastV1MrdTestDate;
    public String              sampleType;
    public String              testId;
    public List <ResultClone>  clones;
    public Double              analyticalLimit;
    public String              iuoHeader           = "For Investigational Use Only (IUO) / For Performance Evaluation Only. CAUTION: Investigational device. Limited by United States law to investigational use.";
    public String              ivdHeader           = "For In Vitro Diagnostic Use. Rx Only.";
    public Header              header              = new Header ();
    public Corrected           corrected           = new Corrected ();
    public CriteriaForDominant criteriaForDominant = new CriteriaForDominant ();
    public Appendix            appendix            = new Appendix ();
    public Approval            approval            = new Approval ();
    public String              footer              = "Adaptive Biotechnologies Corporation 1551 Eastlake Ave East, Suite 200, Seattle WA 98102 (888) 552-8988 adaptivebiotech.com";

    public ClonoSeq () {}

    public ClonoSeq (ReportRender report) {
        this.report = report;
        this.isIUO = report.patientInfo.isIuo;
        this.isCorrected = report.patientInfo.isCorrected;
        this.isFailed = report.isFailure;
        this.isClonality = clonality.equals (report.patientInfo.reportType);
        this.isEos = BCell.equals (report.patientInfo.reportLocus);
        this.hasComments = report.commentInfo.comments != null;
        this.isPolyclonal = report.data != null && report.data.clonalityResult == 1;
        this.isClonoSEQV1 = report.patientInfo.isClonoSEQV1;
        this.locus = report.patientInfo.reportLocus;
        this.lastV1MrdTestDate = report.patientInfo.lastV1MrdTestDate;
        this.sampleType = report.patientInfo.reportSpecimenType.label == null ? report.patientInfo.reportSpecimenSource.label : report.patientInfo.reportSpecimenType.label;
        this.testId = report.patientInfo.reportSampleOrderTestId;

        this.clones = report.data != null && report.data.resultClones != null ? report.data.resultClones : new ArrayList <> ();
        this.isCellfree = CellFree.label.equals (report.patientInfo.reportSpecimenCompartment) || clones.parallelStream ()
                                                                                                        .anyMatch (r -> templates.equals (r.history.get (0).countMeasure));
        this.isLifted = clones.parallelStream ().anyMatch (r -> TRUE.equals (r.isLiftedIdClone));
        this.analyticalLimit = ofNullable (report.data).map (d -> d.analyticalLimit).orElse (null);
        this.fromOutsideLab = gDNA.label.equals (sampleType);
        this.isCorrected = report.patientInfo.isCorrected;
        this.isFailed = report.isFailure;
        this.isClonality = clonality.equals (report.patientInfo.reportType);
    }

    public String title () {
        return format ("%s %s REPORT%s",
                       IGH.equals (locus) || IGKL.equals (locus) ? "B-CELL " + locus : TCRB.equals (locus) || TCRG.equals (locus) ? "T-CELL " + locus : "B-CELL",
                       isClonality ? "CLONALITY (ID)" : "TRACKING (MRD)",
                       isCorrected ? " — CORRECTED" : "");
    }

    public String titleShm () {
        return "IGHV MUTATION ANALYSIS REPORT" + (isCorrected ? " — CORRECTED" : "");
    }

    public String allpagesHeader () {
        return format ("%s %s", isIVD ? ivdHeader : isIUO ? iuoHeader : "", header);
    }

    public String getComments () {
        return format ("ADDITIONAL COMMENTS %s",
                       report.commentInfo.comments != null ? report.commentInfo.comments.replace ("\n", " ") : "");
    }

    public String criteriaForDominant () {
        String criteria1 = "The sequence must comprise at least 3% of all like sequences (IGH-involved, IGK, and IGL are considered independently).";
        String criteria2 = "The sequence must comprise at least 0.2% of the total nucleated cells in the sample.";

        if (BCell.equals (locus)) {
            criteriaForDominant.criterias.set (1, criteria1);
            criteriaForDominant.criterias.set (2, criteria2);
        }

        if (!isClonality)
            criteriaForDominant.criterias.add (criteriaForDominant.footnote);

        return join (" ", criteriaForDominant.criterias);
    }

    public String assayMethodsLimitations () {
        List <String> outsideLab = asList ("Note that Adaptive Biotechnologies has assessed the performance of the clonoSEQ Assay based on in-house extraction methods.",
                                           "gDNA extracted by an outside laboratory may be suboptimal in quantity or quality, which could increase the risk of sample failure, reduce sample sensitivity and/or introduce bias in MRD measurement.");
        List <String> texts = new ArrayList <> ();
        texts.add ("ASSAY DESCRIPTION, METHOD AND LIMITATIONS");
        texts.add ("DESCRIPTION");
        if (isEos && isCLIA)
            texts.add (isCellfree ? descEosCellfree () : descEosCellular ());
        else if (TCRB.equals (locus) || TCRG.equals (locus))
            texts.add (descTcr ());
        else if (IGH.equals (locus) || IGKL.equals (locus))
            texts.add (descBcr ());
        else if (isIVD)
            texts.add (descIVD ());
        else
            texts.add (descCE ());

        texts.add ("METHOD");
        if (isEos && isCLIA)
            texts.add (isCellfree ? methodEosCellfree () : methodEosCellular ());
        else if (TCRB.equals (locus) || TCRG.equals (locus))
            texts.add (methodTcr ());
        else if (IGH.equals (locus) || IGKL.equals (locus))
            texts.add (methodBcr ());
        else if (isIVD)
            texts.add (methodIVD ());
        else
            texts.add (methodCE ());

        texts.add (isCellfree ? "GENERAL ASSAY LIMITATIONS" : "LIMITATIONS");
        if (isEos && isCLIA)
            if (isCellfree) {
                texts.add (limitEosCellfree1 ());
                texts.add (limitEosCellfree2 ());
            } else
                texts.add (limitEosCellular ());
        else if (TCRB.equals (locus) || TCRG.equals (locus))
            texts.add (limitTcr ());
        else if (IGH.equals (locus) || IGKL.equals (locus))
            texts.add (limitBcr ());
        else if (isIVD)
            texts.add (limitIVD ());
        else
            texts.add (limitCE ());

        if (fromOutsideLab && !isCellfree)
            texts.addAll (outsideLab);

        return join (" ", texts);
    }

    public String assayMethodsLimitationsSHM () {
        List <String> texts = new ArrayList <> ();
        texts.add ("DESCRIPTION, ASSAY METHOD, PERFORMANCE AND LIMITATIONS");
        texts.add ("DESCRIPTION");
        texts.add (descSHM ());
        texts.add ("METHOD");
        texts.add (methodSHM ());
        texts.add ("PERFORMANCE");
        texts.add (performanceSHM ());
        texts.add ("LIMITATIONS");
        texts.add (limitSHM ());
        return join (" ", texts);
    }

    public String appendixSampleInfo () {
        String sampleH2 = (BCell.equals (locus) ? "" : "ESTIMATED ") + "TOTAL " + (isCellfree ? "VOLUME (mL)" : "NUCLEATED CELLS");
        appendix.sampleInfo.set (2, format (appendix.sampleInfo.get (2), sampleH2));
        appendix.sampleInfo.add (appendix.sampleTable);
        return join (" ", appendix.sampleInfo);
    }

    public String appendixSequenceInfo () {
        String unit = isCellfree ? "(PER mL)" : "(PER MILLION CELLS) ";
        appendix.sequenceInfo.set (1, format (appendix.sequenceInfo.get (1), unit));
        appendix.sequenceInfo.add (appendix.sequenceTable);
        return join (" ", appendix.sequenceInfo);
    }

    public String appendixNotes () {
        if (isClonality && BCell.equals (locus)) {
            appendix.notes.set (4, appendix.notes.get (4).replace ("5 ", ""));
            appendix.notes.remove (getLast (appendix.notes));
            appendix.notes.remove (getLast (appendix.notes));
        }

        if (IGH.equals (locus) || IGKL.equals (locus) || TCRB.equals (locus) || TCRG.equals (locus)) {
            appendix.notes.set (1,
                                "2 Estimated Total Nucleated Cells The estimated total number of nucleated cells calculated within the sample, using the total DNA quantitation and 6.5 pg of DNA per diploid cell.");
            appendix.notes.remove (getLast (appendix.notes));
            appendix.notes.remove (getLast (appendix.notes));
            appendix.notes.remove (getLast (appendix.notes));
        }
        return join (" ", appendix.notes);
    }

    public String appendixReferences () {
        List <String> references = new ArrayList <> (asList ("REFERENCES"));
        if (BCell.equals (locus) && isCLIA)
            references.add (refEosCLIA ());
        if (TCRB.equals (locus) || TCRG.equals (locus))
            references.add (refTcrClia ());
        if (IGH.equals (locus) || IGKL.equals (locus))
            references.add (refBcrClia ());
        if (isIVD)
            references.add (refEosIVD ());
        if (isCE)
            references.add (refEosCE ());
        return join (" ", references);
    }

    public String approval () {
        approval.sections.set (1, format (approval.sections.get (1), approval.technician));
        approval.sections.set (3, format (approval.sections.get (3), approval.dateTime));

        if (approval.signatureImage == null)
            approval.sections.remove (2);

        return join (" ", approval.sections);
    }

    public String approvalDisclaimer () {
        approval.disclaimer.set (0, format (approval.disclaimer.get (0), approval.directorName));
        if (isIVD)
            approval.disclaimer.remove (getLast (approval.disclaimer));

        if (isIUO)
            approval.disclaimer.add (approval.iuo);

        return join (" ", approval.disclaimer);
    }

    public static class Header {

        public String patientName;
        public String DOB;
        public String medicalRecord;
        public String gender;
        public String reportDt;
        public String orderNum;
        public String patientId;
        public String diagnosisCode;
        public String specimen;
        public String collectionDt;
        public String receivedDt;
        public String sampleId;
        public String icdCodes;
        public String orderingPhysician;
        public String institution;

        @Override
        public String toString () {
            return format ("PATIENT NAME%s DATE OF BIRTH%s MEDICAL RECORD #%s GENDER%s REPORT DATE %s ORDER # %s%s%s SPECIMEN TYPE / SPECIMEN SOURCE %s COLLECTION DATE %s DATE RECEIVED %s SAMPLE ID %s",
                           patientName != null ? " " + capitalize (patientName) : "",
                           DOB != null ? " " + DOB : "",
                           medicalRecord != null ? " " + medicalRecord : "",
                           gender != null ? " " + gender : "",
                           reportDt,
                           orderNum,
                           patientId != null ? " PATIENT ID " + patientId : "",
                           diagnosisCode != null ? " DIAGNOSIS CODE " + diagnosisCode : "",
                           specimen,
                           collectionDt,
                           receivedDt,
                           sampleId);
        }

        public String frontpageHeader () {
            return format ("%s ORDERING PHYSICIAN %s INSTITUTION %s",
                           icdCodes != null ? "ICD CODE " + icdCodes : "",
                           orderingPhysician,
                           institution);
        }
    }

    public static class Corrected {

        public String reason;
        public String previousReportDt;

        @Override
        public String toString () {
            return format ("REASON FOR CORRECTION %s", reason.replace ("\n", " "));
        }
    }

    public static class CriteriaForDominant {

        public String        header    = "CRITERIA FOR DEFINING \"DOMINANT\" SEQUENCES";
        public String        footnote  = "Note: These criteria are applied when defining the trackable dominant sequence(s) in a high disease load (ID) sample or when identifying new dominant sequences in a follow-up (MRD) sample.";
        public List <String> criterias = new ArrayList <> (
                asList (header,
                        "The sequence must comprise at least 3% of all like sequences.",
                        "The sequence must comprise at least 0.2% of the estimated total nucleated cells in the sample.",
                        "The sequence must be discontinuously distributed (≤5 sequences in the next decade of sequences when ranked by frequency).",
                        "The sequence must be carried by at least 40 estimated genome equivalents in the analyzed sample."));

        public String shm () {
            List <String> criterias = asList ("CRITERIA FOR DEFINING IGHV MUTATION STATUS",
                                              "Dominant IGH sequences are identified as described in the clonoSEQ B-cell Clonality Report and further selected for those representing complete V(D)J rearrangements.",
                                              "Percent mutation is the percentage of nucleotide positions in IGHV segments that differ from the closest corresponding germline V segments per the international ImMunoGeneTics (IMGT) reference database.",
                                              "Unmutated: <=2% deviation from the germline sequence.",
                                              "Mutated: >2% deviation from the germline sequence.",
                                              "Certain V segments have been found to have prognostic implications.");

            return join (" ", criterias);
        }
    }

    public static class Appendix {

        public String        header       = "APPENDIX";
        public String        sampleTable;
        public String        sequenceTable;
        public List <String> sampleInfo   = new ArrayList <> (
                asList (header,
                        "SUPPLEMENTAL SAMPLE INFORMATION",
                        "SAMPLE CLONALITY 1 %s 2 LOCI TOTAL SEQUENCES 3 TOTAL UNIQUE SEQUENCES 4"));
        public List <String> sequenceInfo = new ArrayList <> (
                asList ("SUPPLEMENTAL SEQUENCE INFORMATION",
                        "SEQUENCE LIMIT OF DETECTION %1$s5 LIMIT OF QUANTITATION %1$s6"));
        public List <String> notes        = new ArrayList <> (
                asList ("1 Sample Clonality A measure of the lymphocyte population diversity (distinct lymphocyte clonal sub-populations or \"clones\") comprising the immune repertoire in a given biological sample. Values for clonality vary from 0 to 1. Values close to 1 represent samples with one or a few predominant clones. Values near zero represent a more polyclonal sample.",
                        "2 Total Nucleated Cells The total number of nucleated cells calculated within the sample, based on quantitation of non-immune receptor loci contained in the reaction and the assumption that the DNA content per cell is diploid.",
                        "3 Total Sequences A measure of the number of nucleotide sequences detected in the sample for each defined immune receptor locus.",
                        "4 Total Unique Sequences A measure of the number of unique nucleotide sequences detected in the sample for each defined immune receptor locus.",
                        "5 Limit of Detection (LOD) The lowest level of residual tracked sequence(s) that can be reliably detected by the clonoSEQ Assay in ≥95% of samples tested. LOD is independently calculated for each trackable sequence and hence can vary by sequence based on factors including the amount of input DNA, the uniqueness of the sequence, and/or relative amplification due to nucleotide sequence polymorphism or mutation.",
                        "6 Limit of Quantitation (LOQ) The lowest level of residual tracked sequence(s) that can be reliably quantified by the clonoSEQ Assay (within 70% total RMS error). LOQ is independently calculated for each trackable sequence and hence can vary by sequence based on factors including the amount of input DNA, the uniqueness of the sequence, and/or relative amplification due to nucleotide sequence polymorphism or mutation.",
                        "Limit of Blank (LOB) The level of residual tracked sequence(s) at or below which 95% of true MRD-negative samples will fall. The LOB for the clonoSEQ Assay was determined during analytical validation to be 0."));

        public String shmDefinition () {
            return join (" ",
                         asList (header,
                                 "Somatic Hypermutation (SHM)",
                                 "Somatic hypermutation (SHM) is an endogenous process for increasing antigen receptor diversity that becomes active at a specific stage of normal B cell maturation. Current or past SHM activity in single lymphoid cells or clones is inferred when more than 2% of nucleotides in their rearranged IGHV sequences are different than germline (\"mutated\"). For certain therapeutic regimens, CLL/SLL patients with IGHV mutation-positive status have a distinctly more favorable clinical course than CLL/SLL patients with IGHV unmutated status.",
                                 "Productive/Unproductive",
                                 "These terms designate the predicted peptide translational status of sequences based on the amino acid codon reading frame that corresponds to known V through J amino acid sequences and that does not contain stop codons. V segment names are designated as in the IMGT reference database."));
        }

        public String refSHM () {
            return join (" ",
                         asList ("REFERENCES",
                                 "Ching T, et al. BMC Cancer. 2020; 20:612.",
                                 "Hamblin TJ, et al. Br J Haematol. 2008;140:320-323.",
                                 "Kim A, et al. 2019. Molecular Genetic Aspects of Non-Hodgkin Lymphomas. In: Greer J, et al. editors. Wintrobe's Clinical Hematology. 14th ed. Philadelphia (PA): Wolters Kluwer. p. 1843-1879.",
                                 "Parikh SA, et al. Blood. 2016;127(14):1752-60."));
        }
    }

    public static class Approval {

        public String        header     = "REPORT APPROVAL";
        public String        technician;
        public String        signatureImage;
        public String        dateTime;
        public String        directorName;
        public String        iuo        = "The results should be used only in compliance with the approved investigational study protocol.";
        public List <String> sections   = new ArrayList <> (asList (header,
                                                                    "REVIEWED AND RELEASED BY %s",
                                                                    "SIGNATURE",
                                                                    "DATE & TIME %s"));
        public List <String> disclaimer = new ArrayList <> (
                asList ("This report has been approved by the Clinical Laboratory Director, %s.",
                        "The clonoSEQ Assay is a laboratory service performed at Adaptive Biotechnologies' single site located at 1551 Eastlake Ave E, Seattle, WA 98102.",
                        "This test was developed and its performance characteristics determined by Adaptive Biotechnologies Corporation.",
                        "The laboratory is regulated under CLIA (WA-MTS CLIA# 50D2046518) as qualified to perform high complexity clinical testing.",
                        "The CLIA laboratory-developed test (LDT) service has not been cleared or approved by the US Food and Drug Administration (FDA) for this intended use."));
    }

    private String refEosCLIA () {
        return join (" ",
                     asList ("Armand P, et al. Br J Haematol. 2013; 163:123-126.",
                             "Carlson CS, et al. Nat Commun. 2013;4:2680.",
                             "Faham M, et al. Blood. 2012;120(26):5173-80.",
                             "Kurtz D, et al. Blood. 2015; 125:3679-3687.",
                             "Logan AC, et al. Leukemia. 2013;27(8):1659-65.",
                             "Martinez-Lopez J, et al. Blood. 2014;123(20):3073-9.",
                             "Paietta E. Bone Marrow Transplant. 2002;29(6):459-465.",
                             "Perrot A, et al. Blood. 2018;132(23):2456-2464.",
                             "Pulsipher M, et al. Blood. 2015;125(22):3501-8.",
                             "Rawstron AC, et al. Leukemia. 2016;30(4):929-36.",
                             "Roschewski M, et al. Lancet Oncol. 2015;16:541-49.",
                             "Thompson P, et al. Blood. 2019;134(22):1951-1959.",
                             "Wood B, et al. Blood. 2018; 131(12):1350-1359.",
                             "Wu D, et al. Clin Cancer Res. 2014;20(17):4540-8."));
    }

    private String refEosIVD () {
        return join (" ",
                     asList ("Carlson CS, et al. Nat Commun. 2013;4:2680.",
                             "Faham M, et al. Blood. 2012;120(26):5173-80.",
                             "Kurtz D, et al. Blood. 2015; 125:3679-3687.",
                             "Logan AC, et al. Leukemia. 2013;27(8):1659-65.",
                             "Martinez-Lopez J, et al. Blood. 2014;123(20):3073-9.",
                             "Paietta E. Bone Marrow Transplant. 2002;29(6):459-465.",
                             "Perrot A, et al. Blood. 2018;132(23):2456-2464.",
                             "Pulsipher M, et al. Blood. 2015;125(22):3501-8.",
                             "Rawstron AC, et al. Leukemia. 2016;30(4):929-36.",
                             "Thompson P, et al. Blood. 2019;134(22):1951-1959.",
                             "Wood B, et al. Blood. 2018; 131(12):1350-1359.",
                             "Wu D, et al. Clin Cancer Res. 2014;20(17):4540-8."));
    }

    private String refEosCE () {
        return join (" ",
                     asList ("Armand P, et al. Br J Haematol. 2013; 163:123-126.",
                             "Carlson CS, et al. Nat Commun. 2013;4:2680.",
                             "Faham M, et al. Blood. 2012;120(26):5173-80.",
                             "Kurtz D, et al. Blood. 2015; 125:3679-3687.",
                             "Logan AC, et al. Leukemia. 2013;27(8):1659-65.",
                             "Martinez-Lopez J, et al. Blood. 2014;123(20):3073-9.",
                             "Paietta E. Bone Marrow Transplant. 2002;29(6):459-465.",
                             "Perrot A, et al. Blood. 2018;132(23):2456-2464.",
                             "Pulsipher M, et al. Blood. 2015;125(22):3501-8.",
                             "Rawstron AC, et al. Leukemia. 2016;30(4):929-36.",
                             "Roschewski M, et al. Lancet Oncol. 2015;16:541-49.",
                             "Thompson P, et al. Blood. 2019;134(22):1951-1959.",
                             "Wood B, et al. Blood. 2018; 131(12):1350-1359.",
                             "Wu D, et al. Clin Cancer Res. 2014;20(17):4540-8."));
    }

    private String refTcrClia () {
        return join (" ",
                     asList ("Carlson CS, et al. Nat Commun. 2013;4:2680.",
                             "de Masson A, et al. Sci Transl Med. 2018;10(440).",
                             "Faham M, et al. Blood. 2012;120(26):5173-80.",
                             "Kirsch IR, et al. Sci Transl Med. 2015;7(308):308ra158.",
                             "Paietta E. Bone Marrow Transplant. 2002;29(6):459-465.",
                             "Pulsipher M, et al. Blood. 2015;125(22):3501-8.",
                             "Weng W, et al. Sci Transl Med. 2013;5(214):214ra171.",
                             "Wu D, et al. Clin Cancer Res. 2014;20(17):4540-8."));
    }

    private String refBcrClia () {
        return join (" ",
                     asList ("Armand P, et al. Br J Haematol. 2013; 163:123-126.",
                             "Carlson CS, et al. Nat Commun. 2013;4:2680.",
                             "Faham M, et al. Blood. 2012;120(26):5173-80.",
                             "Kurtz D, et al. Blood. 2015; 125:3679-3687.",
                             "Logan AC, et al. Leukemia. 2013;27(8):1659-65.",
                             "Martinez-Lopez J, et al. Blood. 2014;123(20):3073-9.",
                             "Paietta E. Bone Marrow Transplant. 2002;29(6):459-465.",
                             "Perrot A, et al. Blood. 2018;132(23):2456-2464.",
                             "Pulsipher M, et al. Blood. 2015;125(22):3501-8.",
                             "Rawstron AC, et al. Leukemia. 2016;30(4):929-36.",
                             "Roschewski M, et al. Lancet Oncol. 2015;16:541-49.",
                             "Thompson P, et al. Blood. 2019;134(22):1951-1959.",
                             "Wood B, et al. Blood. 2018; 131(12):1350-1359.",
                             "Wu D, et al. Clin Cancer Res. 2014;20(17):4540-8."));
    }

    private String descEosCellular () {
        return "The clonoSEQ® Assay is intended to identify and quantify rearranged B-cell receptors in DNA extracted from specimens from patients with lymphoid malignancies.";
    }

    private String descEosCellfree () {
        return "The clonoSEQ® Assay is intended to identify and quantify rearranged B-cell receptors in genomic DNA (gDNA, also referred to as cellular DNA) or cell-free DNA (cfDNA) extracted from specimens from patients with lymphoid malignancies.";
    }

    private String descTcr () {
        return "The clonoSEQ® Assay is intended to identify and quantify rearranged T-cell receptors in DNA extracted from specimens from patients with lymphoid malignancies.";
    }

    private String descBcr () {
        return "The clonoSEQ® Assay is intended to identify and quantify rearranged B-cell receptors in DNA extracted from specimens from patients with lymphoid malignancies.";
    }

    private String descIVD () {
        return "The clonoSEQ® Assay is an in vitro diagnostic (IVD) that is intended to identify and quantify rearranged B-cell receptors in DNA extracted from bone marrow samples from patients with B-cell acute lymphoblastic leukemia (ALL) or multiple myeloma (MM), and blood or bone marrow from patients with chronic lymphocytic leukemia (CLL).";
    }

    private String descCE () {
        return join (" ",
                     asList ("The clonoSEQ® Assay B-Cell Reagent Set is an in vitro diagnostic that uses multiplex polymerase chain reaction (PCR) and next-generation sequencing (NGS) to identify and quantify rearranged B-cell receptor gene sequences, including IgH(VDJ), IgH(DJ), IgK, and IgL, and translocated BCL1/IgH(J) and BCL2/IgH(J) sequences in DNA extracted from blood and bone marrow.",
                             "The clonoSEQ® Assay B-Cell Reagent Set determines measurable/minimal residual disease (MRD) and changes in disease burden during and after treatment in B-cell malignancies.",
                             "The test is indicated for use by qualified healthcare professionals for clinical decision-making and in conjunction with other clinicopathological features."));
    }

    private String descSHM () {
        return join (" ",
                     asList ("The IGHV mutation analysis test is a laboratory-developed test that determines the somatic hypermutation status of rearranged immunoglobulin heavy chain (IGH) gene variable (V) segments to inform CLL/SLL patient risk stratification at the time of diagnosis.",
                             "It is a computational algorithm appended to existing analytics of the clonoSEQ® Assay for diagnostic CLL/SLL specimens."));
    }

    private String methodEosCellular () {
        return join (" ",
                     asList ("The clonoSEQ Assay profiles the B-cell repertoire by polymerase chain reaction (PCR) and next-generation sequencing (NGS) and identifies and measures levels of presumptive disease-associated lymphoid cells.",
                             "An initial sample, obtained at the time of diagnosis of lymphoid malignancy, is first analyzed to identify patient-specific sequences at the immunoglobulin heavy-chain locus [IGH; both complete (IGH-VDJ) and incomplete (IGH-DJ) rearrangements], the immunoglobulin κ locus (IGK), the immunoglobulin λ locus (IGL), and IGH-BCL1/2 translocations.",
                             "The same methodology is then applied in follow-up samples to measure the residual level of the previously identified patient-specific sequence(s)."));
    }

    private String methodEosCellfree () {
        return join (" ",
                     asList ("The clonoSEQ Assay profiles the B-cell repertoire by polymerase chain reaction (PCR) and next-generation sequencing (NGS) and identifies and measures levels of presumptive disease-associated lymphoid cells.",
                             "An initial sample, obtained at the time of diagnosis of lymphoid malignancy, is first analyzed to identify patient-specific sequences at the immunoglobulin heavy-chain locus [IGH; both complete (IGH-VDJ) and incomplete (IGH-DJ) rearrangements], the immunoglobulin κ locus (IGK), the immunoglobulin λ locus (IGL), and IGH-BCL1/2 translocations.",
                             "The same methodology is then applied in follow-up samples to measure the residual level of the previously identified patient-specific sequence(s)."));
    }

    private String methodTcr () {
        return join (" ",
                     asList ("The clonoSEQ Assay profiles the T-cell repertoire by polymerase chain reaction (PCR) and next-generation sequencing (NGS) and identifies and measures levels of presumptive disease-associated lymphoid cells.",
                             format ("An initial sample, obtained at the time of diagnosis of lymphoid malignancy, is first analyzed to identify patient-specific sequences at the T-cell receptor %s (%s) locus.",
                                     TCRB.equals (locus) ? "beta" : "gamma",
                                     locus),
                             "The same methodology is then applied in follow-up samples to measure the residual level of the previously identified patient-specific sequence(s)."));
    }

    private String methodBcr () {
        return join (" ",
                     asList ("The clonoSEQ Assay profiles the B-cell repertoire by polymerase chain reaction (PCR) and next-generation sequencing (NGS) and identifies and measures levels of presumptive disease-associated lymphoid cells.",
                             format ("An initial sample, obtained at the time of diagnosis of lymphoid malignancy, is first analyzed to identify patient-specific sequences at the immunoglobulin %s",
                                     IGH.equals (locus) ? "heavy-chain (IGH) locus." : "κ (IGK) locus and immunoglobulin λ (IGL) locus."),
                             "The same methodology is then applied in follow-up samples to measure the residual level of the previously identified patient-specific sequence(s)."));
    }

    private String methodIVD () {
        return join (" ",
                     asList ("The clonoSEQ Assay profiles the B-cell repertoire by polymerase chain reaction (PCR) and next-generation sequencing (NGS) and identifies and measures levels of presumptive disease-associated lymphoid cells.",
                             "An initial sample, obtained at the time of diagnosis of lymphoid malignancy, is first analyzed to identify patient-specific sequences at the immunoglobulin heavy-chain locus [IGH; both complete (IGH-VDJ) and incomplete (IGH-DJ) rearrangements], the immunoglobulin κ locus (IGK), the immunoglobulin λ locus (IGL), and IGH-BCL1/2 translocations.",
                             "The same methodology is then applied in follow-up samples to measure the residual level of the previously identified patient-specific sequence(s)."));
    }

    private String methodCE () {
        return join (" ",
                     asList ("The clonoSEQ® Assay B-Cell Reagent Set profiles the B-cell repertoire by polymerase chain reaction (PCR) and next-generation sequencing (NGS) and identifies and measures levels of presumptive disease-associated lymphoid cells.",
                             "An initial sample, obtained at the time of diagnosis of lymphoid malignancy, is first analyzed to identify patient-specific sequences at the immunoglobulin heavy-chain locus [IGH; both complete (IGH-VDJ) and incomplete (IGH-DJ) rearrangements], the immunoglobulin κ locus (IGK), the immunoglobulin λ locus (IGL), and IGH-BCL1/2 translocations.",
                             "The same methodology is then applied in follow-up samples to measure the residual level of the previously identified patient-specific sequence(s)."));
    }

    private String methodSHM () {
        return join (" ",
                     asList ("Each IGHV dominant sequence identified by the clonoSEQ Assay is assessed computationally for mutation status.",
                             "The most closely related germline IGHV segment is determined based on database comparison.",
                             "Comparison to germline begins with FR2 and extends to the 3' end of the FR3 and does not include CDR3.",
                             "The fraction of nucleotide positions differing from germline is calculated, and productive/unproductive peptide translational status is predicted.",
                             "Sample-level mutation status (unmutated, mutated, or indeterminate) is adjudicated considering the predicted peptide translational status of the dominant IGHV sequence(s) and its mutation level relative to the 98% sequence identity (2% deviation from germline) cutoff."));
    }

    private String performanceSHM () {
        return join (" ",
                     asList ("This assay is clinically and analytically validated for qualitative assessment of unmutated, mutated, and indeterminate status.",
                             "Analytical sensitivity and specificity have been assessed at 95.3% and 98.3%.",
                             "See the clonoSEQ B-Cell Clonality Report for limit of detection information.",
                             "An indeterminate status indicates that the IGHV mutation assessment cannot be definitively determined based on the available sequencing data."));
    }

    private String limitEosCellular () {
        return join (" ",
                     asList ("False positive or false negative results may occur for reasons including, but not limited to: sample mix up, misidentification, and/or contamination; technical and/or biological factors.",
                             "Results may vary by sample source, type or body site/location sampled.",
                             "The assay may overestimate MRD frequencies near the limit of detection."));
    }

    private String limitEosCellfree1 () {
        return join (" ",
                     asList ("False positive or false negative results may occur for reasons including, but not limited to: sample mix up, misidentification, and/or contamination; technical and/or biological factors.",
                             "The assay may overestimate MRD frequencies near the limit of detection."));
    }

    private String limitEosCellfree2 () {
        return join (" ",
                     asList ("CELL-FREE DNA ASSAY LIMITATIONS",
                             "Measured residual disease levels may vary from sample to sample when analyzing cfDNA for reasons including, but not limited to: patient-specific disease process, amount of plasma assessed, cfDNA recovery, technical assay efficiency, and/or sample handling/degradation.",
                             "Patients with detectable disease in a primary tumor sample may not have detectable tumor cfDNA in a plasma sample; the amount of tumor cfDNA in a plasma sample may not correlate with the amount in a primary tumor.",
                             "Any dominant sequence identified in a Clonality (ID) sample that is subsequently detected in a cell-free DNA Tracking (MRD) test and is above the assay's LOB is reported as a residual sequence.",
                             "The LOD and LOQ values associated with each dominant sequence are provided but are not used herein to determine reporting of the presence/absence of residual disease.",
                             "The assay was determined during analytical validation to be quantitatively linear for measurement of B-cell clonal sequences between 10.8 to 1376.4 templates/mL; samples at higher templates/mL were not assessed.",
                             "In addition, the percent agreement for presence/absence of residual sequence detection was ~86% between replicate plasma samples and was ~30% when values approached the LOD."));
    }

    private String limitTcr () {
        return join (" ",
                     asList ("False positive or false negative results may occur for reasons including, but not limited to: sample mix up, misidentification, and/or contamination; technical and/or biological factors.",
                             "Results may vary by sample source, type or body site/location sampled."));
    }

    private String limitBcr () {
        return join (" ",
                     asList ("False positive or false negative results may occur for reasons including, but not limited to: sample mix up, misidentification, and/or contamination; technical and/or biological factors.",
                             "Results may vary by sample source, type or body site/location sampled."));
    }

    private String limitIVD () {
        return join (" ",
                     asList ("False positive or false negative results may occur for reasons including, but not limited to: sample mix up, misidentification, and/or contamination; technical and/or biological factors.",
                             "Results may vary by sample source, type or body site/location sampled.",
                             "The assay may overestimate MRD frequencies near the limit of detection."));
    }

    private String limitCE () {
        return join (" ",
                     asList ("False positive or false negative results may occur for reasons including, but not limited to: sample mix up, misidentification, and/or contamination; technical and/or biological factors.",
                             "Results may vary by sample source, type or body site/location sampled.",
                             "The assay may overestimate MRD frequencies near the limit of detection."));
    }

    private String limitSHM () {
        return join (" ",
                     asList ("IGHV mutation analysis derived from the clonoSEQ Assay may be limited by its use of shorter than full-length IGH locus V segments.",
                             "Identification of the closest germline IGH V segments, calculation of percent mutation values (especially near the 2% cutoff), and final assessment of mutation status may be affected by technical details of the implementation."));
    }
}
