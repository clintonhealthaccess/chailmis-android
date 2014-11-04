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

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "receives")
public class Receive implements Serializable {
    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @ForeignCollectionField(eager = true, maxEagerLevel = 2)
    private ForeignCollection<ReceiveItem> receiveItemsCollection;

    private List<ReceiveItem> receiveItems = new ArrayList<>();


    @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = "yyyy-MM-dd")
    private Date created;

    private Allocation allocation;
    @DatabaseField(canBeNull = false)
    private String source;

    public Receive() {
        setDateCreated();
    }

    public Receive(String source) {
        this(source, new Date());
    }

    public Receive(String source, Date date) {
        this.setSource(source);
        this.created = date;
    }


    public Receive(String source, Allocation allocation) {
        this.source = source;
        this.allocation = allocation;
        setDateCreated();
    }

    public void addReceiveItem(ReceiveItem receiveItem) {
        receiveItem.setReceive(this);
        this.receiveItems.add(receiveItem);
    }

    private void setDateCreated() {
        this.created = new Date();
    }

    public boolean isRecievingFromLGA() {
        return source.contains("LGA");
    }
}
