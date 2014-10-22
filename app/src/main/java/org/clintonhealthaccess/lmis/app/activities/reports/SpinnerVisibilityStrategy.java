package org.clintonhealthaccess.lmis.app.activities.reports;

import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

@SuppressWarnings("ResourceType")
public abstract class SpinnerVisibilityStrategy {

    public int startingMonth() {
        return View.VISIBLE;
    }

    public int endingMonth() {
        return View.VISIBLE;
    }

    public int startingYear() {
        return View.VISIBLE;
    }

    public int endingYear() {
        return View.VISIBLE;
    }

    public static SpinnerVisibilityStrategy allVisible = new SpinnerVisibilityStrategy() {
    };

    public static SpinnerVisibilityStrategy startVisible = new SpinnerVisibilityStrategy() {
        @Override
        public int endingMonth() {
            return View.INVISIBLE;
        }

        @Override
        public int endingYear() {
            return View.INVISIBLE;
        }
    };

    public void applyVisibilityStrategy(Spinner spinnerStartingMonth, Spinner spinnerEndingMonth, Spinner spinnerStartingYear, Spinner spinnerEndingYear, TextView textViewEndingMonth, TextView textViewEndingYear) {
        spinnerStartingMonth.setVisibility(startingMonth());
        spinnerEndingMonth.setVisibility(endingMonth());
        spinnerStartingYear.setVisibility(startingYear());
        spinnerEndingYear.setVisibility(endingYear());
        textViewEndingMonth.setVisibility(endingMonth());
        textViewEndingYear.setVisibility(endingYear());
    }
}


