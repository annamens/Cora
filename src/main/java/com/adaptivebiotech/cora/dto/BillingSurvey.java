package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.equalsOverride;
import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import java.util.List;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class BillingSurvey {

    public String               status;
    public List <Questionnaire> questionnaires;

    public BillingSurvey () {}

    public BillingSurvey (List <Questionnaire> questionnaires) {
        this.questionnaires = questionnaires;
    }

    @Override
    public String toString () {
        return toStringOverride (this);
    }

    @Override
    public boolean equals (Object o) {
        return equalsOverride (this, (BillingSurvey) o);
    }

    public static final class Questionnaire {

        public String        name;
        public String        title;
        public List <String> answers;

        public Questionnaire () {}

        public Questionnaire (String name, List <String> answers) {
            this.name = name;
            this.answers = answers;
        }

        @Override
        public boolean equals (Object o) {
            return equalsOverride (this, (Questionnaire) o, "title");
        }
    }
}
