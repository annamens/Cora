package com.adaptivebiotech.cora.dto;

import java.util.List;
import com.adaptivebiotech.cora.dto.HttpResponse.Meta;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class PatientResponse {

    public Meta           meta;
    public List <Patient> objects;

    public Patient get (Patient patient) {
        return objects.parallelStream ().filter (o -> {
            return patient.firstName.equals (o.firstName) && patient.lastName.equals (o.lastName);
        }).findAny ().get ();
    }
}
