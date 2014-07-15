package org.clintonhealthaccess.lmis.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.PeriodicSync;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.models.User;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowContentResolver;

import java.util.List;

import roboguice.inject.InjectResource;

import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class SyncManagerTest {
    @Inject
    private SyncManager syncManager;

    @Inject
    private AccountManager accountManager;

    @InjectResource(R.string.sync_account_type)
    private String syncAccountType;

    @InjectResource(R.string.sync_content_authority)
    private String syncContentAuthority;

    @Before
    public void setUp() throws Exception {
        setUpInjection(this);
    }

    @Test
    public void testCreateSyncAccount() throws Exception {
        assertThat(accountManager.getAccounts().length, is(0));

        User user = new User("test_user", "password");
        syncManager.createSyncAccount(user);

        assertThat(accountManager.getAccounts().length, is(1));
        Account newAccount = accountManager.getAccounts()[0];
        assertThat(newAccount.name, is(user.getUsername()));
        assertThat(newAccount.type, is(syncAccountType));
    }

    @Test
    public void testKickOffPeriodicalSync() throws Exception {
        User user = new User("test_user", "password");
        assertThat(getPeriodicSyncs(user).size(), is(0));

        syncManager.createSyncAccount(user);
        syncManager.kickOff();

        assertThat(getPeriodicSyncs(user).size(), is(1));
        assertThat(getPeriodicSyncs(user).get(0).period, is(60l));
    }

    private List<PeriodicSync> getPeriodicSyncs(User user) {
        Account account = new Account(user.getUsername(), syncAccountType);
        return ShadowContentResolver.getPeriodicSyncs(account, syncContentAuthority);
    }
}