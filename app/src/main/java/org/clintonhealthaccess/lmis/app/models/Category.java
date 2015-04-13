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

import com.google.common.collect.ImmutableList;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@DatabaseTable(tableName = "commodity_categories")
public class Category implements Serializable, Comparable<Category> {
    @DatabaseField(uniqueIndex = true, generatedId = true)
    private long id;

    @DatabaseField(canBeNull = false)
    private String lmisId;

    @DatabaseField(canBeNull = false)
    private String name;

    @ForeignCollectionField(eager = false)
    private ForeignCollection<Commodity> commoditiesCollection;

    private List<Commodity> transientCommodities = new ArrayList<>();

    public Category() {
        // ormlite likes it
    }

    public Category(String name) {
        this(name, name);
    }

    public Category(String lmisId, String name) {
        this.lmisId = lmisId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Commodity> getCommodities() {
        if (commoditiesCollection == null) {
            return newArrayList();
        }
        return ImmutableList.copyOf(commoditiesCollection);
    }

    public void setCommodities(List<Commodity> commodities) {
        this.transientCommodities = commodities;
    }

    public void setLmisId(String lmisId) {
        this.lmisId = lmisId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Commodity> getTransientCommodities() {
        return transientCommodities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Category)) {
            return false;
        }

        Category category = (Category) o;

        return !(name != null ? !name.equals(category.name) : category.name != null);
    }

    @Override
    public int hashCode() {
        return 31 * (name != null ? name.hashCode() : 0);
    }

    public void addTransientCommodity(Commodity commodity) {
        transientCommodities.add(commodity);
    }

    @Override
    public int compareTo(Category another) {
        return this.getName().compareToIgnoreCase(another.getName());
    }

    @Override
    public String toString() {
        return "[ " + id + " " + lmisId+ " " + name + " SavedCommodities: " + commoditiesCollection + " UNsaved" + transientCommodities + " ]";
    }
}
