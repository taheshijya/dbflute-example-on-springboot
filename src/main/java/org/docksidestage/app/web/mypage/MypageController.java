package org.docksidestage.app.web.mypage;

import java.util.List;
import java.util.stream.Collectors;

import org.dbflute.cbean.result.ListResultBean;
import org.docksidestage.app.application.security.MemberUserDetail;
import org.docksidestage.dbflute.exbhv.ProductBhv;
import org.docksidestage.dbflute.exentity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author fukutake
 */
@Controller
@RequestMapping("/mypage")
public class MypageController {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger logger = LoggerFactory.getLogger(MypageController.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Autowired
    private ProductBhv productBhv;

    // ===================================================================================
    //                                                                              Entry
    //                                                                             =======
    @RequestMapping("")
    @Transactional
    public String index(@AuthenticationPrincipal MemberUserDetail userDetail, Model model) {

        List<MypageProductBean> recentProducts = mappingToProducts(selectRecentProductList(userDetail));
        List<MypageProductBean> highPriceProducts = mappingToProducts(selectHighPriceProductList(userDetail));

        model.addAttribute("recentProducts", recentProducts);
        model.addAttribute("highPriceProducts", highPriceProducts);
        return "mypage/mypage";
    }

    // ===================================================================================
    //                                                                              Select
    //                                                                              ======
    private ListResultBean<Product> selectRecentProductList(MemberUserDetail userDetail) {
        ListResultBean<Product> productList = productBhv.selectList(cb -> {
            cb.specify().derivedPurchase().max(purchaseCB -> {
                purchaseCB.specify().columnPurchaseDatetime();
            }, Product.ALIAS_latestPurchaseDate);
            cb.query().existsPurchase(purchaseCB -> {
                purchaseCB.query().setMemberId_Equal(userDetail.getMember().getMemberId());
            });
            cb.query().addSpecifiedDerivedOrderBy_Desc(Product.ALIAS_latestPurchaseDate);
            cb.query().addOrderBy_ProductId_Asc();
            cb.fetchFirst(3);
        });
        return productList;
    }

    private ListResultBean<Product> selectHighPriceProductList(MemberUserDetail userDetail) {
        ListResultBean<Product> productList = productBhv.selectList(cb -> {
            cb.query().existsPurchase(purchaseCB -> {
                purchaseCB.query().setMemberId_Equal(userDetail.getMember().getMemberId());
            });
            cb.query().addOrderBy_RegularPrice_Desc();
            cb.fetchFirst(3);
        });
        return productList;
    }

    // ===================================================================================
    //                                                                             Mapping
    //                                                                             =======
    private List<MypageProductBean> mappingToProducts(List<Product> productList) {
        return productList.stream().map(product -> new MypageProductBean(product)).collect(Collectors.toList());
    }
}
