package org.docksidestage.app.web.product;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.dbflute.cbean.result.PagingResultBean;
import org.docksidestage.app.web.paging.PagingNavi;
import org.docksidestage.dbflute.allcommon.CDef.ProductStatus;
import org.docksidestage.dbflute.exbhv.ProductBhv;
import org.docksidestage.dbflute.exentity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * @author jflute
 */
@Controller
@RequestMapping("/product")
@SessionAttributes(value = "productSearchForm")
public class ProductController {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Autowired
    private ProductBhv productBhv;

    @ModelAttribute("productSearchForm")
    ProductSearchForm productSearchForm() {
        System.out.println("create productSearchForm");
        return new ProductSearchForm();
    }

    // ===================================================================================
    //                                                                              Entry
    //                                                                             =======
    @RequestMapping("")
    @Transactional
    public String index(Model model, ProductSearchForm productSearchForm) {

        return "index";
    }

    // ===================================================================================
    //                                                                           Show List
    //                                                                           =========
    // http://localhost:8080/member/list?pageNumber=1
    // http://localhost:8080/member/list?pageNumber=sea
    @RequestMapping("/list")
    public String list(Model model, @Valid ProductSearchForm form, BindingResult result) {
        logger.debug("#form: {}", form);
        model.addAttribute("productStatusSelectOption", getProductStatusSelectOption());
        if (result.hasErrors()) {
            logger.debug("has error:" + result.getFieldErrors());
            model.addAttribute("beans", Collections.emptyList()); // #for_now avoid error
            // #hope change type failure message
            // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
            // Failed to convert property value of type
            // java.lang.String to required type java.lang.Integer for property pageNumber;
            // nested exception is java.lang.NumberFormatException: For input string: "sea"
            // _/_/_/_/_/_/_/_/_/_/
            return "product/product_list";
        }
        //        PagingResultBean<Member> page = selectMemberPage(form);

        PagingResultBean<Product> page = selectProductPage(form.getPageNumber(), form);
        List<ProductSearchRowBean> beans = page.stream().map(product -> {
            return mappingToBean(product);
        }).collect(Collectors.toList());

        model.addAttribute("beans", beans);

        // ページング用処理
        PagingNavi pagingNavi = new PagingNavi();
        pagingNavi.prepare(page, op -> {
            op.rangeSize(10);
        });
        model.addAttribute("pagingNavi", pagingNavi);

        return "product/product_list";
    }

    // ===================================================================================
    //                                                                              Select
    //                                                                              ======
    private PagingResultBean<Product> selectProductPage(int pageNumber, ProductSearchForm form) {
        return productBhv.selectPage(cb -> {
            cb.setupSelect_ProductStatus();
            cb.setupSelect_ProductCategory();
            cb.specify().derivedPurchase().max(purchaseCB -> {
                purchaseCB.specify().columnPurchaseDatetime();
            }, Product.ALIAS_latestPurchaseDate);
            if (form.getProductName() != null && !form.getProductName().isEmpty()) {
                cb.query().setProductName_LikeSearch(form.getProductName(), op -> op.likeContain());
            }
            if (form.getPurchaseMemberName() != null && !form.getPurchaseMemberName().isEmpty()) {
                cb.query().existsPurchase(purchaseCB -> {
                    purchaseCB.query().queryMember().setMemberName_LikeSearch(form.getPurchaseMemberName(), op -> op.likeContain());
                });
            }
            if (form.getProductStatus() != null && !form.getProductStatus().isEmpty()) {
                cb.query().setProductStatusCode_Equal_AsProductStatus(ProductStatus.codeOf(form.getProductStatus()));
            }
            cb.query().addOrderBy_ProductName_Asc();
            cb.query().addOrderBy_ProductId_Asc();
            cb.paging(4, pageNumber);
        });
    }

    // ===================================================================================
    //                                                                             Mapping
    //                                                                             =======
    private ProductSearchRowBean mappingToBean(Product product) {
        ProductSearchRowBean bean = new ProductSearchRowBean();
        bean.productId = product.getProductId();
        bean.productName = product.getProductName();
        product.getProductStatus().alwaysPresent(status -> {
            bean.productStatus = status.getProductStatusName();
        });
        product.getProductCategory().alwaysPresent(category -> {
            bean.productCategory = category.getProductCategoryName();
        });
        bean.regularPrice = product.getRegularPrice();
        bean.latestPurchaseDate = product.getLatestPurchaseDate();
        return bean;
    }

    protected Map<String, String> getProductStatusSelectOption() {

        Map<String, String> productStatusSelectOption = new LinkedHashMap<String, String>();

        for (ProductStatus entry : ProductStatus.values()) {
            productStatusSelectOption.put(entry.code(), entry.alias());
        }

        return productStatusSelectOption;
    }

}
