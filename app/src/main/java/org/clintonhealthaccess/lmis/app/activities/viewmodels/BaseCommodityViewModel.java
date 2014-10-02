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

package org.clintonhealthaccess.lmis.app.activities.viewmodels;

import org.clintonhealthaccess.lmis.app.models.Commodity;

import java.io.Serializable;

public class BaseCommodityViewModel implements Serializable {

    protected int quantityEntered;
    private boolean selected;
    private Commodity commodity;

    public BaseCommodityViewModel(Commodity commodity) {
        this.commodity = commodity;
    }

    public BaseCommodityViewModel(Commodity commodity, int quantityEntered) {
        this(commodity);
        this.quantityEntered = quantityEntered;
    }

    public void toggleSelected() {
        selected = !selected;
    }

    public String getName() {
        return commodity.getName();
    }

    public Commodity getCommodity() {
        return commodity;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean stockIsFinished() {
        return commodity.isOutOfStock();
    }

    public int getStockOnHand() {
        return commodity.getStockOnHand();
    }

    public int getQuantityEntered() {
        return quantityEntered;
    }

    public void setQuantityEntered(int quantityEntered) {
        this.quantityEntered = quantityEntered;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseCommodityViewModel)) return false;

        BaseCommodityViewModel that = (BaseCommodityViewModel) o;

        return commodity.equals(that.commodity);

    }

    @Override
    public int hashCode() {
        return commodity.hashCode();
    }
}
