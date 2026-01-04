package com.xy.mapper;

import com.xy.pojo.DailyVegetablePrices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DateAndPriceTest {
    @Autowired
    DailyVegetablePricesMapper d;
    @Test
    public void selectAll(){
        for (DailyVegetablePrices dailyVegetablePrices : d.selectAll()) {
            System.out.printf(dailyVegetablePrices.toString());
        }
    }

    @Test
    public void selectByName(){
        for (DailyVegetablePrices dailyVegetablePrices : d.selectByName("大白菜")) {
            System.out.printf(dailyVegetablePrices.toString());
        }
    }
}
