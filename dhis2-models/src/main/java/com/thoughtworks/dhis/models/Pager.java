
package com.thoughtworks.dhis.models;

import lombok.Data;

@Data
public class Pager {

    private Integer page;
    private Integer pageCount;
    private Integer total;
    private String nextPage;


}
