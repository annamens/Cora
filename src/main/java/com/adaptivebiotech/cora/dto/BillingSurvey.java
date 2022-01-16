package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import java.util.List;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class BillingSurvey {

    public List <Questionnaire> questionnaires;

    @Override
    public String toString () {
        return toStringOverride (this);
    }

    public static final class Questionnaire {

        public String        name;
        public String        title;
        public AnswerType    answerType;
        public List <String> answers;
        public String        other;

        public Questionnaire () {}

        public Questionnaire (String name, AnswerType answerType, List <String> answers) {
            this.name = name;
            this.answerType = answerType;
            this.answers = answers;
        }
    }

    public enum AnswerType {
        radio, checkbox, other
    }
}
