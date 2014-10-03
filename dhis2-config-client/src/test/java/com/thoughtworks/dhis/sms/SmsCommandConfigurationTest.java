/*
 * Copyright (c) 2014, Thoughtworks Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

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
