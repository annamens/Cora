/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto;

import java.util.Arrays;
import java.util.List;

/**
 * @author Srinivas Annameni
 *         <a href="mailto:sannameni@adaptivebiotech.com">sannameni@adaptivebiotech.com</a>
 */
public class WorkListColumnHeaders {
    // Order Lists tab column headers list
    public static final List <String> agingFreshSpecimens                                              = Arrays.asList ("order_type",
                                                                                                                        "order",
                                                                                                                        "accountname",
                                                                                                                        "shipment",
                                                                                                                        "days_remaining",
                                                                                                                        "specimen_type",
                                                                                                                        "ship_status",
                                                                                                                        "arrived",
                                                                                                                        "collected",
                                                                                                                        "expected_test",
                                                                                                                        "physician",
                                                                                                                        "order_notes");

    public static final List <String> awaitingApprovalClinicalTrial                                    = Arrays.asList ("intake_complete",
                                                                                                                        "shipment_arrived",
                                                                                                                        "order_type",
                                                                                                                        "account",
                                                                                                                        "specimen_type",
                                                                                                                        "specimen_stabilization_window",
                                                                                                                        "order_number",
                                                                                                                        "shipment_number",
                                                                                                                        "order_notes");

    public static final List <String> awaitingResolutionClinicalTrial                                  = Arrays.asList ("intake_complete",
                                                                                                                        "shipment_arrived",
                                                                                                                        "order_type",
                                                                                                                        "acct",
                                                                                                                        "specimen_type",
                                                                                                                        "specimen_stabilization_window",
                                                                                                                        "order_num",
                                                                                                                        "shipment_num",
                                                                                                                        "discrepancies",
                                                                                                                        "resolutions");

    public static final List <String> billingHolds                                                     = Arrays.asList ("order_type",
                                                                                                                        "order",
                                                                                                                        "physician",
                                                                                                                        "accountname",
                                                                                                                        "collected",
                                                                                                                        "arrived",
                                                                                                                        "expected_test",
                                                                                                                        "note",
                                                                                                                        "specimen_stabilization_window");

    public static final List <String> cancelledOrders                                                  = Arrays.asList ("shipment_number",
                                                                                                                        "arrival_date",
                                                                                                                        "order_type",
                                                                                                                        "order_number",
                                                                                                                        "specimen_approval",
                                                                                                                        "specimen_approved_date",
                                                                                                                        "sample_type",
                                                                                                                        "order_notes");

    public static final List <String> dORAPasswordResetLinks                                           = Arrays.asList ("login_email",
                                                                                                                        "expires",
                                                                                                                        "reset_url");

    public static final List <String> frozenSpecimenstoExpiration                                      = Arrays.asList ("order_type",
                                                                                                                        "order",
                                                                                                                        "accountname",
                                                                                                                        "days_to_180",
                                                                                                                        "specimen_stabilization_window",
                                                                                                                        "days_since_collection",
                                                                                                                        "specimen_type",
                                                                                                                        "collected",
                                                                                                                        "expected_test",
                                                                                                                        "physician",
                                                                                                                        "patient_code",
                                                                                                                        "order_notes");
    public static final List <String> ordersFailedActivation                                           = Arrays.asList ("order_type",
                                                                                                                        "order_number",
                                                                                                                        "Last modified",
                                                                                                                        "Provider",
                                                                                                                        "Account name",
                                                                                                                        "specimen_stabilization_window",
                                                                                                                        "Order note");

    public static final List <String> pending3rdPartyBloodDrawRequest                                  = Arrays.asList ("created",
                                                                                                                        "specimen_delivery_type",
                                                                                                                        "account_name",
                                                                                                                        "physician",
                                                                                                                        "order_number",
                                                                                                                        "order_type",
                                                                                                                        "order_notes");

    public static final List <String> rushOrders                                                       = Arrays.asList ("due_date",
                                                                                                                        "order_type",
                                                                                                                        "order_number",
                                                                                                                        "specimen_number",
                                                                                                                        "patient_code",
                                                                                                                        "reason",
                                                                                                                        "physician",
                                                                                                                        "order_status",
                                                                                                                        "sample",
                                                                                                                        "status");

    public static final List <String> tDetectAwaitingActivation                                        = Arrays.asList ("order_created",
                                                                                                                        "shipment_arrived",
                                                                                                                        "account",
                                                                                                                        "provider",
                                                                                                                        "order_notes",
                                                                                                                        "order_number",
                                                                                                                        "shipment_number");

    public static final List <String> tDetectAwaitingApproval                                          = Arrays.asList ("order_created",
                                                                                                                        "shipment_arrived",
                                                                                                                        "account",
                                                                                                                        "provider",
                                                                                                                        "order_number",
                                                                                                                        "shipment_number",
                                                                                                                        "order_notes");

    public static final List <String> tDetectAwaitingResolution                                        = Arrays.asList ("order_modified",
                                                                                                                        "order_created",
                                                                                                                        "shipment_arrived",
                                                                                                                        "acct",
                                                                                                                        "order_num",
                                                                                                                        "shipment_num",
                                                                                                                        "discrepancies",
                                                                                                                        "resolutions");

    public static final List <String> workflowsonhold                                                  = Arrays.asList ("order_number",
                                                                                                                        "order_type",
                                                                                                                        "due_date",
                                                                                                                        "workflow_name",
                                                                                                                        "held_at_stages",
                                                                                                                        "current_stage_name",
                                                                                                                        "order_notes");

    public static final List <String> clonoSEQActiveorCompletedDiagnosticDateSignedafterActivationDate = Arrays.asList ("order_number",
                                                                                                                        "Activated date",
                                                                                                                        "Signed date",
                                                                                                                        "patient_code",
                                                                                                                        "Provider",
                                                                                                                        "Account",
                                                                                                                        "Order note");

    public static final List <String> clonoSEQActiveorCompletedDiagnosticLOMNalert                     = Arrays.asList ("order_number",
                                                                                                                        "Alert last updated",
                                                                                                                        "Times sent",
                                                                                                                        "patient_code",
                                                                                                                        "Provider",
                                                                                                                        "Account");

    public static final List <String> clonoSEQActiveorCompletedDiagnosticMissingDateSigned             = Arrays.asList ("Activated date",
                                                                                                                        "order_number",
                                                                                                                        "patient_code",
                                                                                                                        "Provider",
                                                                                                                        "Account",
                                                                                                                        "Order note",
                                                                                                                        "Specimen Approval Date",
                                                                                                                        "Billing type");

    public static final List <String> clonoSEQAwaitingActivation                                       = Arrays.asList ("order_created",
                                                                                                                        "order_modified",
                                                                                                                        "shipment_arrived",
                                                                                                                        "account",
                                                                                                                        "provider",
                                                                                                                        "specimen_stabilization_window",
                                                                                                                        "order_notes",
                                                                                                                        "order_number",
                                                                                                                        "shipment_number");

    public static final List <String> clonoSEQAwaitingApproval                                         = Arrays.asList ("intake_complete",
                                                                                                                        "shipment_arrived",
                                                                                                                        "account",
                                                                                                                        "specimen_type",
                                                                                                                        "specimen_stabilization_window",
                                                                                                                        "order_number",
                                                                                                                        "shipment_number",
                                                                                                                        "order_notes");

    public static final List <String> clonoSEQAwaitingResolution                                       = Arrays.asList ("intake_complete",
                                                                                                                        "shipment_arrived",
                                                                                                                        "acct",
                                                                                                                        "specimen_type",
                                                                                                                        "specimen_stabilization_window",
                                                                                                                        "order_num",
                                                                                                                        "shipment_num",
                                                                                                                        "discrepancies",
                                                                                                                        "resolutions");

    public static final List <String> clonoSEQIDMRDSKUMismatch                                         = Arrays.asList ("order_created",
                                                                                                                        "shipment_arrived",
                                                                                                                        "acct",
                                                                                                                        "order_num",
                                                                                                                        "id_order_num",
                                                                                                                        "mrd_workflow_name",
                                                                                                                        "id_workflow_name",
                                                                                                                        "shipment_num",
                                                                                                                        "patient_code",
                                                                                                                        "order_notes");

    public static final List <String> clonoSEQMRDReflexNeeded                                          = Arrays.asList ("order_number",
                                                                                                                        "order_type",
                                                                                                                        "physician",
                                                                                                                        "account",
                                                                                                                        "due_date",
                                                                                                                        "held_at_stages",
                                                                                                                        "current_stage_name",
                                                                                                                        "patient_id",
                                                                                                                        "order_notes");

    public static final List <String> clonoSEQMRDwithActiveID                                          = Arrays.asList ("created",
                                                                                                                        "physician",
                                                                                                                        "account",
                                                                                                                        "patient_code",
                                                                                                                        "order_number",
                                                                                                                        "latest_active_id",
                                                                                                                        "all_active_id",
                                                                                                                        "specimen_stabilization_window",
                                                                                                                        "days_since_collection",
                                                                                                                        "order_notes");

    public static final List <String> clonoSEQMRDwithCompletedID                                       = Arrays.asList ("modified",
                                                                                                                        "modified_by",
                                                                                                                        "physician",
                                                                                                                        "order_number",
                                                                                                                        "patient_code",
                                                                                                                        "notes");

    public static final List <String> clonoSEQMRDwithNoID                                              = Arrays.asList ("created",
                                                                                                                        "accountname",
                                                                                                                        "physician",
                                                                                                                        "order_number",
                                                                                                                        "shipment_number",
                                                                                                                        "shipment_status",
                                                                                                                        "specimen_number",
                                                                                                                        "specimen_type",
                                                                                                                        "collection_date",
                                                                                                                        "extract_hold_date");

    public static final List <String> clonoSEQPathologyRequestPendingPlacement                         = Arrays.asList ("account",
                                                                                                                        "provider",
                                                                                                                        "order_created_by",
                                                                                                                        "order_number",
                                                                                                                        "order_creation_date",
                                                                                                                        "order_notes",
                                                                                                                        "specimen_delivery_type");

    public static final List <String> clonoSEQPatientswithmultipleMRDsinClarity                        = Arrays.asList ("patient_code",
                                                                                                                        "samples",
                                                                                                                        "orders");

    public static final List <String> clonoSEQPendingDiagnosticwithQCtests                             = Arrays.asList ("order",
                                                                                                                        "specimen_number",
                                                                                                                        "patient_code",
                                                                                                                        "created_by",
                                                                                                                        "order_notes");

    public static final List <String> clonoSEQReflexOrders                                             = Arrays.asList ("created",
                                                                                                                        "account",
                                                                                                                        "provider",
                                                                                                                        "order_name",
                                                                                                                        "order_number");

    public static final List <String> clonoSEQYoungPatients                                            = Arrays.asList ("due_date",
                                                                                                                        "order_number",
                                                                                                                        "specimen_number",
                                                                                                                        "patient_code",
                                                                                                                        "patient_age",
                                                                                                                        "physician",
                                                                                                                        "order_status",
                                                                                                                        "sample",
                                                                                                                        "status");
    // Order tests list tab column header lists
    public static final List <String> auroraUploadColumn                                               = Arrays.asList ("updated_date",
                                                                                                                        "experiment_list_name",
                                                                                                                        "lab_name",
                                                                                                                        "stage",
                                                                                                                        "status",
                                                                                                                        "substatus",
                                                                                                                        "created_date",
                                                                                                                        "created_by",
                                                                                                                        "finished_date",
                                                                                                                        "progress",
                                                                                                                        "history");

    public static final List <String> cDxBcellclonalityAutoReleaseeligible                             = Arrays.asList ("order_num",
                                                                                                                        "patient_code",
                                                                                                                        "due_date",
                                                                                                                        "icd_codes",
                                                                                                                        "no_results_available",
                                                                                                                        "poly_clonal",
                                                                                                                        "num_order_alerts",
                                                                                                                        "num_order_test_alerts",
                                                                                                                        "custom_comments",
                                                                                                                        "num_igk_sequences",
                                                                                                                        "num_igh_sequences",
                                                                                                                        "num_igl_sequences");

    public static final List <String> cDxBcellclonalityNotAutoReleaseeligible                          = Arrays.asList ("order_num",
                                                                                                                        "patient_code",
                                                                                                                        "due_date",
                                                                                                                        "icd_codes",
                                                                                                                        "no_results_available",
                                                                                                                        "poly_clonal",
                                                                                                                        "num_order_alerts",
                                                                                                                        "num_order_test_alerts",
                                                                                                                        "custom_comments",
                                                                                                                        "num_igk_sequences",
                                                                                                                        "num_igh_sequences",
                                                                                                                        "num_igl_sequences");
    public static final List <String> diagnosticpendingcorrectedreports                                = Arrays.asList ("order_number",
                                                                                                                        "order_type",
                                                                                                                        "patient_code",
                                                                                                                        "original_report_release_date",
                                                                                                                        "original_report_release_by",
                                                                                                                        "corrected_report_initiated_date",
                                                                                                                        "corrected_report_initiated_by",
                                                                                                                        "order_notes",
                                                                                                                        "report_notes");

    public static final List <String> tDxAutoQCPass                                                    = Arrays.asList ("order_num",
                                                                                                                        "patient_code",
                                                                                                                        "order_test_due_date",
                                                                                                                        "qc_flags",
                                                                                                                        "num_order_alerts",
                                                                                                                        "num_order_test_alerts",
                                                                                                                        "report_results",
                                                                                                                        "sample_type",
                                                                                                                        "clinical_qc_comments",
                                                                                                                        "age",
                                                                                                                        "last_northqc_substatus_code");

    public static final List <String> tDxCOVIDAutoReleaseeligible                                      = Arrays.asList ("order_num",
                                                                                                                        "patient_code",
                                                                                                                        "due_date",
                                                                                                                        "report_results",
                                                                                                                        "custom_comments");

    public static final List <String> tDxCOVIDNotAutoReleaseeligible                                   = Arrays.asList ("order_num",
                                                                                                                        "patient_code",
                                                                                                                        "due_date",
                                                                                                                        "report_results",
                                                                                                                        "custom_comments");

    public static final List <String> tDxNotAutoQCeligible                                             = Arrays.asList ("order_num",
                                                                                                                        "patient_code",
                                                                                                                        "order_test_due_date",
                                                                                                                        "qc_flags",
                                                                                                                        "num_order_alerts",
                                                                                                                        "num_order_test_alerts",
                                                                                                                        "report_results",
                                                                                                                        "sample_type",
                                                                                                                        "clinical_qc_comments",
                                                                                                                        "age",
                                                                                                                        "last_northqc_substatus_code");
    // Shipments tab column header lists
    public static final List <String> batchShipmentsAllInventoryBackfill                               = Arrays.asList ("shipment_number",
                                                                                                                        "arrival_date",
                                                                                                                        "shipment_status_type",
                                                                                                                        "salesforce_accession",
                                                                                                                        "linked_number",
                                                                                                                        "modified",
                                                                                                                        "modified_by",
                                                                                                                        "order_category");

    public static final List <String> batchShipmentsActivatedOrdersstillinBSMFreezers                  = Arrays.asList ("shipment_number",
                                                                                                                        "created",
                                                                                                                        "location",
                                                                                                                        "order_name",
                                                                                                                        "order_number",
                                                                                                                        "order_category");

    public static final List <String> batchShipmentsAgingFreshSpecimen                                 = Arrays.asList ("order",
                                                                                                                        "shipment",
                                                                                                                        "fresh_specimens",
                                                                                                                        "days_to_90",
                                                                                                                        "days_since_start",
                                                                                                                        "specimen_types",
                                                                                                                        "ship_status",
                                                                                                                        "order_category");

    public static final List <String> batchShipmentsAwaitingAccessionComplete                          = Arrays.asList ("shipment_number",
                                                                                                                        "arrival_date",
                                                                                                                        "linked_number",
                                                                                                                        "order_name",
                                                                                                                        "order_category",
                                                                                                                        "modified",
                                                                                                                        "modified_by");

    public static final List <String> batchShipmentsAwaitingActivation                                 = Arrays.asList ("shipment_number",
                                                                                                                        "arrival_date",
                                                                                                                        "linked_number",
                                                                                                                        "order_name",
                                                                                                                        "order_category",
                                                                                                                        "modified",
                                                                                                                        "modified_by");

    public static final List <String> batchShipmentsAwaitingLabelVerification                          = Arrays.asList ("shipment_number",
                                                                                                                        "arrival_date",
                                                                                                                        "linked_number",
                                                                                                                        "order_name",
                                                                                                                        "order_category",
                                                                                                                        "modified",
                                                                                                                        "labeling_completed_by",
                                                                                                                        "num_outstanding_containers",
                                                                                                                        "num_unresolved_discrepancies");

    public static final List <String> batchShipmentsAwaitingLabeling                                   = Arrays.asList ("shipment_number",
                                                                                                                        "arrival_date",
                                                                                                                        "linked_number",
                                                                                                                        "order_name",
                                                                                                                        "order_category",
                                                                                                                        "modified",
                                                                                                                        "modified_by",
                                                                                                                        "num_outstanding_containers");

    public static final List <String> batchShipmentsAwaitingResolutions                                = Arrays.asList ("shipment_number",
                                                                                                                        "arrival_date",
                                                                                                                        "shipment_status_type",
                                                                                                                        "linked_number",
                                                                                                                        "order_name",
                                                                                                                        "order_category",
                                                                                                                        "modified",
                                                                                                                        "modified_by");

    public static final List <String> batchShipmentsAwaitingSpecimenApproval                           = Arrays.asList ("shipment_number",
                                                                                                                        "arrival_date",
                                                                                                                        "shipment_status_type",
                                                                                                                        "linked_number",
                                                                                                                        "order_category",
                                                                                                                        "modified",
                                                                                                                        "modified_by");

    public static final List <String> batchShipmentsDiscontinued                                       = Arrays.asList ("shipment_number",
                                                                                                                        "arrival_date",
                                                                                                                        "linked_number",
                                                                                                                        "order_name",
                                                                                                                        "order_category",
                                                                                                                        "modified",
                                                                                                                        "modified_by",
                                                                                                                        "shipment_substatus_type");

    public static final List <String> batchShipmentsExtractandHoldstillinBSMFreezers                   = Arrays.asList ("shipment_number",
                                                                                                                        "arrival_date",
                                                                                                                        "shipment_status_type",
                                                                                                                        "linked_number",
                                                                                                                        "order_category",
                                                                                                                        "modified",
                                                                                                                        "modified_by");

    public static final List <String> batchShipmentsReadyforIntake                                     = Arrays.asList ("shipment_number",
                                                                                                                        "arrival_date",
                                                                                                                        "linked_number",
                                                                                                                        "order_name",
                                                                                                                        "order_category",
                                                                                                                        "modified",
                                                                                                                        "modified_by",
                                                                                                                        "num_unresolved_discrepancies");

    public static final List <String> diagnosticShipmentsAwaitingLabelVerification                     = Arrays.asList ("shipment_number",
                                                                                                                        "arrival_date",
                                                                                                                        "order_number",
                                                                                                                        "order_name",
                                                                                                                        "order_type",
                                                                                                                        "modified",
                                                                                                                        "labeling_completed_by",
                                                                                                                        "num_outstanding_containers");

    public static final List <String> diagnosticShipmentsAwaitingLabeling                              = Arrays.asList ("shipment_number",
                                                                                                                        "arrival_date",
                                                                                                                        "order_number",
                                                                                                                        "order_name",
                                                                                                                        "modified",
                                                                                                                        "modified_by",
                                                                                                                        "num_outstanding_containers");

    public static final List <String> diagnosticShipmentsBlockedfromIntake                             = Arrays.asList ("high_priority",
                                                                                                                        "shipment_arrival_date",
                                                                                                                        "intake_status",
                                                                                                                        "shipment_modified",
                                                                                                                        "modified_by",
                                                                                                                        "shipment_number",
                                                                                                                        "order_number",
                                                                                                                        "container_type",
                                                                                                                        "note");

    public static final List <String> diagnosticShipmentsBlockedfromIntakeCOMPLETED                    = Arrays.asList ("intake_status",
                                                                                                                        "modified_pretty",
                                                                                                                        "modified_by",
                                                                                                                        "shipment_number",
                                                                                                                        "order_number",
                                                                                                                        "container_type",
                                                                                                                        "sample_type",
                                                                                                                        "note");

    public static final List <String> diagnosticShipmentsBlockedfromIntakeCTHOLD                       = Arrays.asList ("shipment_arrival_date",
                                                                                                                        "intake_status",
                                                                                                                        "shipment_modified",
                                                                                                                        "modified_by",
                                                                                                                        "shipment_number",
                                                                                                                        "order_number",
                                                                                                                        "container_type",
                                                                                                                        "sample_type",
                                                                                                                        "note");

    public static final List <String> diagnosticShipmentsBlockedfromIntakeDISPENSE                     = Arrays.asList ("intake_status",
                                                                                                                        "modified_pretty",
                                                                                                                        "modified_by",
                                                                                                                        "shipment_number",
                                                                                                                        "order_number",
                                                                                                                        "container_type",
                                                                                                                        "sample_type",
                                                                                                                        "note");

    public static final List <String> diagnosticShipmentsBlockedfromIntakeHOLD                         = Arrays.asList ("shipment_arrival_date",
                                                                                                                        "intake_status",
                                                                                                                        "shipment_modified",
                                                                                                                        "modified_by",
                                                                                                                        "shipment_number",
                                                                                                                        "order_number",
                                                                                                                        "container_type",
                                                                                                                        "sample_type",
                                                                                                                        "note");

    public static final List <String> diagnosticShipmentsBlockedfromIntakeTDX                          = Arrays.asList ("shipment_arrival_date",
                                                                                                                        "intake_status",
                                                                                                                        "shipment_modified",
                                                                                                                        "modified_by",
                                                                                                                        "shipment_number",
                                                                                                                        "order_number",
                                                                                                                        "container_type",
                                                                                                                        "sample_type",
                                                                                                                        "note");

    public static final List <String> diagnosticShipmentsBlockedfromIntakeTDXHOLD                      = Arrays.asList ("shipment_arrival_date",
                                                                                                                        "intake_status",
                                                                                                                        "shipment_modified",
                                                                                                                        "modified_by",
                                                                                                                        "shipment_number",
                                                                                                                        "order_number",
                                                                                                                        "container_type",
                                                                                                                        "sample_type",
                                                                                                                        "note");

    public static final List <String> diagnosticShipmentsReadyforIntake                                = Arrays.asList ("intake_status",
                                                                                                                        "created",
                                                                                                                        "shipment_number",
                                                                                                                        "order_number",
                                                                                                                        "specimen_stabilization_window",
                                                                                                                        "shipment_note");

    public static final List <String> receivedInventorylast21days                                      = Arrays.asList ("sfdc_order_name",
                                                                                                                        "shipment_number",
                                                                                                                        "arrival_date",
                                                                                                                        "order_number",
                                                                                                                        "order_category",
                                                                                                                        "holding_container_type",
                                                                                                                        "holding_container_name",
                                                                                                                        "holding_co",
                                                                                                                        "position",
                                                                                                                        "container_type",
                                                                                                                        "container_name",
                                                                                                                        "container_co",
                                                                                                                        "container_created_date",
                                                                                                                        "sample_type",
                                                                                                                        "specimen_name",
                                                                                                                        "asid",
                                                                                                                        "location",
                                                                                                                        "operator",
                                                                                                                        "intake_completed_date");

    public static final List <String> specimensDiagnosticActivatedOrdersstillinBSMFreezers             = Arrays.asList ("created",
                                                                                                                        "account",
                                                                                                                        "provider",
                                                                                                                        "location",
                                                                                                                        "order_name",
                                                                                                                        "order_number",
                                                                                                                        "shipment_number");

    public static final List <String> specimensDiagnosticExtractandHoldstillinBSMFreezers              = Arrays.asList ("created",
                                                                                                                        "account",
                                                                                                                        "provider",
                                                                                                                        "location",
                                                                                                                        "order_name",
                                                                                                                        "order_number",
                                                                                                                        "shipment_number");

    public static List <String> getColumnlist (String worklistitem) {
        switch (worklistitem) {

        case "Aging Fresh Specimens":
            return agingFreshSpecimens;
        case "Awaiting Approval Clinical Trial (PHI)":
            return awaitingApprovalClinicalTrial;
        case "Awaiting Resolution Clinical Trial (PHI)":
            return awaitingResolutionClinicalTrial;
        case "Billing Holds (PHI)":
            return billingHolds;
        case "Cancelled Orders (PHI)":
            return cancelledOrders;
        case "DORA Password Reset Links (PHI)":
            return dORAPasswordResetLinks;
        case "Orders Failed Activation":
            return ordersFailedActivation;
        case "Pending 3rd Party Blood Draw Request (PHI)":
            return pending3rdPartyBloodDrawRequest;
        case "Rush Orders (PHI)":
            return rushOrders;
        case "T-Detect - Awaiting Activation":
            return tDetectAwaitingActivation;
        case "T-Detect - Awaiting Approval (PHI)":
            return tDetectAwaitingApproval;
        case "T-Detect - Awaiting Resolution (PHI)":
            return tDetectAwaitingResolution;
        case "Workflows on hold (PHI)":
            return workflowsonhold;
        case "clonoSEQ - Active or Completed Diagnostic - Date Signed after Activation Date (PHI)":
            return clonoSEQActiveorCompletedDiagnosticDateSignedafterActivationDate;
        case "clonoSEQ - Active or Completed Diagnostic - LOMN alert (PHI)":
            return clonoSEQActiveorCompletedDiagnosticLOMNalert;
        case "clonoSEQ - Active or Completed Diagnostic - Missing Date Signed (PHI)":
            return clonoSEQActiveorCompletedDiagnosticMissingDateSigned;
        case "clonoSEQ - Awaiting Activation (PHI)":
            return clonoSEQAwaitingActivation;
        case "clonoSEQ - Awaiting Approval (PHI)":
            return clonoSEQAwaitingApproval;
        case "clonoSEQ - Awaiting Resolution (PHI)":
            return clonoSEQAwaitingResolution;
        case "clonoSEQ - ID-MRD SKU Mismatch":
            return clonoSEQIDMRDSKUMismatch;
        case "clonoSEQ - MRD Reflex Needed (PHI)":
            return clonoSEQMRDReflexNeeded;
        case "clonoSEQ - MRD with Active ID (PHI)":
            return clonoSEQMRDwithActiveID;
        case "clonoSEQ - MRD with Completed ID (PHI)":
            return clonoSEQMRDwithCompletedID;
        case "clonoSEQ - MRD with No ID":
            return clonoSEQMRDwithNoID;
        case "clonoSEQ - Pathology Request Pending Placement (PHI)":
            return clonoSEQPathologyRequestPendingPlacement;
        case "clonoSEQ - Patients with multiple MRDs in Clarity":
            return clonoSEQPatientswithmultipleMRDsinClarity;
        case "clonoSEQ - Pending Diagnostic with QC tests":
            return clonoSEQPendingDiagnosticwithQCtests;
        case "clonoSEQ - Reflex Orders":
            return clonoSEQReflexOrders;
        case "clonoSEQ - Young Patients (PHI)":
            return clonoSEQYoungPatients;
        case "Aurora Uploads":
            return auroraUploadColumn;
        case "CDx B cell clonality Auto Release eligible (PHI)":
            return cDxBcellclonalityAutoReleaseeligible;
        case "CDx B cell clonality Not Auto Release eligible (PHI)":
            return cDxBcellclonalityNotAutoReleaseeligible;
        case "Diagnostic - pending corrected reports (PHI)":
            return diagnosticpendingcorrectedreports;
        case "TDx Auto QC Pass (PHI)":
            return tDxAutoQCPass;
        case "TDx COVID Auto Release eligible (PHI)":
            return tDxCOVIDAutoReleaseeligible;
        case "TDx COVID Not Auto Release eligible (PHI)":
            return tDxCOVIDNotAutoReleaseeligible;
        case "TDx Not Auto QC eligible (PHI)":
            return tDxNotAutoQCeligible;
        case "Batch Shipments - All Inventory Backfill":
            return batchShipmentsAllInventoryBackfill;
        case "Batch Shipments Activated Orders still in BSM Freezers":
            return batchShipmentsActivatedOrdersstillinBSMFreezers;
        case "Batch Shipments Aging Fresh Specimen (Days to 90)":
            return batchShipmentsAgingFreshSpecimen;
        case "Batch Shipments Awaiting Accession Complete":
            return batchShipmentsAwaitingAccessionComplete;
        case "Batch Shipments Awaiting Activation":
            return batchShipmentsAwaitingActivation;
        case "Batch Shipments Awaiting Label Verification":
            return batchShipmentsAwaitingLabelVerification;
        case "Batch Shipments Awaiting Labeling":
            return batchShipmentsAwaitingLabeling;
        case "Batch Shipments Awaiting Resolutions":
            return batchShipmentsAwaitingResolutions;
        case "Batch Shipments Awaiting Specimen Approval":
            return batchShipmentsAwaitingSpecimenApproval;
        case "Batch Shipments Discontinued":
            return batchShipmentsDiscontinued;
        case "Batch Shipments Extract and Hold still in BSM Freezers":
            return batchShipmentsExtractandHoldstillinBSMFreezers;
        case "Batch Shipments Ready for Intake":
            return batchShipmentsReadyforIntake;
        case "Diagnostic Shipments - Awaiting Label Verification":
            return diagnosticShipmentsAwaitingLabelVerification;
        case "Diagnostic Shipments - Awaiting Labeling":
            return diagnosticShipmentsAwaitingLabeling;
        case "Diagnostic Shipments - Blocked from Intake (PHI)":
            return diagnosticShipmentsBlockedfromIntake;
        case "Diagnostic Shipments - Blocked from Intake COMPLETED (PHI)":
            return diagnosticShipmentsBlockedfromIntakeCOMPLETED;
        case "Diagnostic Shipments - Blocked from Intake CTHOLD (PHI)":
            return diagnosticShipmentsBlockedfromIntakeCTHOLD;
        case "Diagnostic Shipments - Blocked from Intake DISPENSE (PHI)":
            return diagnosticShipmentsBlockedfromIntakeDISPENSE;
        case "Diagnostic Shipments - Blocked from Intake HOLD (PHI)":
            return diagnosticShipmentsBlockedfromIntakeHOLD;
        case "Diagnostic Shipments - Blocked from Intake TDX (PHI)":
            return diagnosticShipmentsBlockedfromIntakeTDX;
        case "Diagnostic Shipments - Blocked from Intake TDXHOLD (PHI)":
            return diagnosticShipmentsBlockedfromIntakeTDXHOLD;
        case "Diagnostic Shipments - Ready for Intake (PHI)":
            return diagnosticShipmentsReadyforIntake;
        case "Received Inventory - last 21 days":
            return receivedInventorylast21days;
        case "Specimens - Diagnostic Activated Orders still in BSM Freezers":
            return specimensDiagnosticActivatedOrdersstillinBSMFreezers;
        case "Specimens - Diagnostic Extract and Hold still in BSM Freezers":
            return specimensDiagnosticExtractandHoldstillinBSMFreezers;
        default:
            break;
        }
        return null;
    }

}
