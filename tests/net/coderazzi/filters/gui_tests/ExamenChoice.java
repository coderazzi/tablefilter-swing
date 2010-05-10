package net.coderazzi.filters.gui_tests;

import net.coderazzi.filters.gui.editors.ChoiceFilterEditor;

class ExamenChoice implements ChoiceFilterEditor.IChoice {
    enum Fraction {
        ONE_FIFTH, TWO_FIFTH, THREE_FIFTH, FOUR_FIFTH, FIVE_FIFTH
    }

	public static ExamenChoice[] ALL_EXAM_CHOICES = {
        new ExamenChoice(ExamenChoice.Fraction.ONE_FIFTH),
        new ExamenChoice(ExamenChoice.Fraction.TWO_FIFTH),
        new ExamenChoice(ExamenChoice.Fraction.THREE_FIFTH),
        new ExamenChoice(ExamenChoice.Fraction.FOUR_FIFTH),
        new ExamenChoice(ExamenChoice.Fraction.FIVE_FIFTH)
    };

    ExamenChoice.Fraction f;

    ExamenChoice(ExamenChoice.Fraction f) {
        this.f = f;
    }

    public boolean matches(Object value) {
        TestData.ExamInformation info = (TestData.ExamInformation) value;

        if ((info == null) || (info.testsDone == 0))
            return true;

        double d = info.testsPassed / (double) info.testsDone;

        switch (f) {

        case ONE_FIFTH:
            return d <= .2;

        case TWO_FIFTH:
            return (d > .2) && (d <= .4);

        case THREE_FIFTH:
            return (d > .4) && (d <= .6);

        case FOUR_FIFTH:
            return (d > .6) && (d <= .8);

        case FIVE_FIFTH:
            return d > .8;
        }

        return false;
    }

    @Override public String toString() {

        switch (f) {

        case ONE_FIFTH:
            return "< 20/100";

        case TWO_FIFTH:
            return "< 40/100";

        case THREE_FIFTH:
            return "< 60/100";

        case FOUR_FIFTH:
            return "< 80/100";

        case FIVE_FIFTH:
            return "above";
        }

        return null;
    }
}