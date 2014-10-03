package com.thoughtworks.dhis.sms;

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SmsCommandConfigurationTest {
    @Test
    public void shouldCreateSmsCommandFromDataSet() throws Exception {
        List<SmsCommand> allSmsCommands = SmsCommand.all();
        assertThat(allSmsCommands.size(), is(15));
        assertThat(allSmsCommands.get(6).getName(), is("5521e503834"));

        SmsCommand smsCommand = allSmsCommands.get(0);
        assertThat(smsCommand.getName(), is("934c6871948"));
        assertThat(smsCommand.getDataSetName(), is("LMIS Anelgesics ORDERED ROUTINE"));
        assertThat(smsCommand.getDataEntries().size(), is(9));

        SmsDataEntry smsDataEntry = smsCommand.getDataEntries().get(0);
        assertThat(smsDataEntry.getName(), is("Paracetamol_15ml_Drops x 1 REASON_FOR_ORDER"));
        assertThat(smsDataEntry.getShortCode(), is("c6b7551cac4"));
    }

    @Ignore
    @Test
    public void shouldConfigureSmsCommandInServer() throws Exception {
        SmsCommand smsCommand = SmsCommand.all().get(0);
        SmsCommand.publish(newArrayList(smsCommand));
    }

    @Ignore
    @Test
    public void shouldConfigureAllSmsCommands() {
        List<SmsCommand> allSmsCommands = SmsCommand.all();
        SmsCommand.publish(allSmsCommands);
    }
}
