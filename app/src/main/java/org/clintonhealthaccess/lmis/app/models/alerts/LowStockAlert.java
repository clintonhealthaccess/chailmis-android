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

package org.clintonhealthaccess.lmis.app.models.alerts;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.clintonhealthaccess.lmis.app.models.Commodity;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@DatabaseTable
public class LowStockAlert {

    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Commodity commodity;

    @DatabaseField(canBeNull = false)
    private Date dateCreated;

    @DatabaseField(canBeNull = false)
    private boolean disabled;

    @DatabaseField(canBeNull = true)
    private Date dateDisabled;

    public LowStockAlert(Commodity commodity) {
        this.commodity = commodity;
        this.disabled = false;
        this.dateCreated = new Date();
    }

    public LowStockAlert() {
        //Orm likes this
    }

    public Commodity getCommodity(){
        return commodity;
    }

    public Date getDateCreated(){
        return dateCreated;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void setDateDisabled(Date dateDisabled) {
        this.dateDisabled = dateDisabled;
    }
}
