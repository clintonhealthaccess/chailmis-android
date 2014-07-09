package org.clintonhealthaccess.lmis.app.services;

import com.j256.ormlite.dao.Dao;

import org.clintonhealthaccess.lmis.app.models.Commodity;
import org.clintonhealthaccess.lmis.app.models.Stock;
import org.clintonhealthaccess.lmis.utils.RobolectricGradleTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class StockServiceTest {

    @Test
    public void shouldGetStockCorrespondingToCommodityFromStockTable() throws SQLException {
        Dao<Stock, String> mockDao = (Dao<Stock, String>)mock(Dao.class);
        StockService stockService = new StockService();
        StockService stockServiceSpy = spy(stockService);
        doReturn(mockDao).when(stockServiceSpy.initialiseDao());

        Commodity commodity = mock(Commodity.class);
        Stock fakeStock = new Stock(commodity, 100);
        when(mockDao.queryForId(anyString())).thenReturn(fakeStock);

        int stockLevel = stockServiceSpy.getStockLevelFor(commodity);
        assertThat(stockLevel, is(100));
    }

}
