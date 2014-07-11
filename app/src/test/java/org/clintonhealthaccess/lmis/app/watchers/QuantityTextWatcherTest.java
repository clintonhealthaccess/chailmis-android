package org.clintonhealthaccess.lmis.app.watchers;

import android.text.TextWatcher;
import android.widget.EditText;

import com.google.inject.AbstractModule;

import org.clintonhealthaccess.lmis.app.activities.viewModels.CommodityViewModel;
import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.services.StockService;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

import static android.text.Editable.Factory;
import static org.clintonhealthaccess.lmis.utils.TestInjectionUtil.setUpInjection;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.application;

@RunWith(RobolectricGradleTestRunner.class)
public class QuantityTextWatcherTest {

    private TextWatcher watcher;
    private EditText editText;
    StockService mockStockService;

    @Before
    public void setUp() throws Exception {
        mockStockService = mock(StockService.class);
        setUpInjection(this, new AbstractModule() {
            @Override
            protected void configure() {
                bind(StockService.class).toInstance(mockStockService);
            }
        });
        editText = spy(new EditText(application));
        watcher = new QuantityTextWatcher(editText, new CommodityViewModel(new Commodity("")));
    }

    @Test
    public void shouldShowErrorIfQuantityIsGreaterThanStockAvailable() throws Exception {
        when(mockStockService.getStockLevelFor(Matchers.<Commodity>anyObject())).thenReturn(2);
        watcher.afterTextChanged(new Factory().newEditable("12"));
        verify(editText).setError(anyString());

    }

    @Test
    public void shouldShowErrorWithRemainingStockIfQuantityIsGreaterThanStockAvailable() throws Exception {
        when(mockStockService.getStockLevelFor(Matchers.<Commodity>anyObject())).thenReturn(2);
        watcher.afterTextChanged(new Factory().newEditable("12"));
        assertThat(editText.getError().toString(), containsString("(2)"));

    }

    @Test
    public void shouldNotShowErrorIfQuantityIsLessThanStockAvailable() throws Exception {
        when(mockStockService.getStockLevelFor(Matchers.<Commodity>anyObject())).thenReturn(77);
        watcher.afterTextChanged(new Factory().newEditable("4"));
        verify(editText, never()).setError(anyString());
    }

    @Test
    public void shouldNotShowErrorIfQuantityIsEqualToStockAvailable() throws Exception {
        when(mockStockService.getStockLevelFor(Matchers.<Commodity>anyObject())).thenReturn(4);
        watcher.afterTextChanged(new Factory().newEditable("4"));
        verify(editText, never()).setError(anyString());
    }


}