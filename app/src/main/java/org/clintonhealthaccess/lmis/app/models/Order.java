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

package org.clintonhealthaccess.lmis.app.models;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

import org.clintonhealthaccess.lmis.app.services.OrderItemSaver;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class Order implements Serializable {

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false)
    private String srvNumber;

    private List<OrderItem> orderItems = newArrayList();

    @DatabaseField(foreign = true, canBeNull = false, foreignAutoRefresh = true)
    private OrderType orderType;

    @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = "yyyy-MM-dd")
    private Date created;

    public Order() {
        //Orm lite likes
        created = new Date();
    }

    public Order(String srvNumber) {
        this.srvNumber = srvNumber;
        created = new Date();
    }

    public void addItem(OrderItem orderItem) {
        orderItems.add(orderItem);
    }

    public boolean has(OrderItem item) {
        return orderItems.contains(item);
    }

    public String getSrvNumber() {
        return srvNumber;
    }

    public void saveOrderItems(OrderItemSaver saver) {
        for (OrderItem item : orderItems) {
            item.setOrder(this);
            saver.saveOrderItem(item);
        }
    }

    public List<OrderItem> getItems() {
        return orderItems;

    }

    public void setSrvNumber(String srvNumber) {
        this.srvNumber = srvNumber;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public Date getCreated() {
        return created;
    }
}
