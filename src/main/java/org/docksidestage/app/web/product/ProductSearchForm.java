/*
 * Copyright 2015-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.docksidestage.app.web.product;

import org.hibernate.validator.constraints.Length;

/**
 * @author jflute
 */
public class ProductSearchForm {

    // #simple_for_example just for validtion example
    @Length(max = 10)
    private String productName;

    //public CDef.ProductStatus productStatus;
    private String productStatus;

    // #simple_for_example just for validtion example
    @Length(max = 5)
    private String purchaseMemberName;

    // -----------------------------------------------------
    //                                                Paging
    //                                                ------
    private Integer pageNumber = 1;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(String productStatus) {
        this.productStatus = productStatus;
    }

    public String getPurchaseMemberName() {
        return purchaseMemberName;
    }

    public void setPurchaseMemberName(String purchaseMemberName) {
        this.purchaseMemberName = purchaseMemberName;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

}
