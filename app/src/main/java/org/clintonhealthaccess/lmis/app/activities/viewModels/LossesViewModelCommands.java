package org.clintonhealthaccess.lmis.app.activities.viewmodels;

import android.text.Editable;

import static org.clintonhealthaccess.lmis.app.utils.ViewHelpers.getIntFromString;

public class LossesViewModelCommands {

    public static interface Command {
        public void execute(LossesCommodityViewModel viewModel, Editable editable);
    }

    public static class SetWastageCommand implements Command {
        @Override
        public void execute(LossesCommodityViewModel viewModel, Editable editable) {
            viewModel.setWastages(getIntFromString(editable.toString()));
        }
    }

    public static class SetDamagesCommand implements Command {
        @Override
        public void execute(LossesCommodityViewModel viewModel, Editable editable) {
            viewModel.setDamages(getIntFromString(editable.toString()));
        }
    }

    public static class SetExpiriesCommand implements Command {
        @Override
        public void execute(LossesCommodityViewModel viewModel, Editable editable) {
            viewModel.setExpiries(getIntFromString(editable.toString()));
        }
    }

    public static class SetMissingCommand implements Command {
        @Override
        public void execute(LossesCommodityViewModel viewModel, Editable editable) {
            viewModel.setMissing(getIntFromString(editable.toString()));
        }
    }
}
