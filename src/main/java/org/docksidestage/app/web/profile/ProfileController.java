package org.docksidestage.app.web.profile;

import java.util.stream.Collectors;

import org.docksidestage.app.application.security.MemberUserDetail;
import org.docksidestage.app.web.profile.ProfileBean.PurchasedProductBean;
import org.docksidestage.dbflute.exbhv.MemberBhv;
import org.docksidestage.dbflute.exentity.Member;
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
@RequestMapping("/profile")
public class ProfileController {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Autowired
    private MemberBhv memberBhv;

    // ===================================================================================
    //                                                                              Entry
    //                                                                             =======
    @RequestMapping("")
    @Transactional
    public String index(@AuthenticationPrincipal MemberUserDetail userDetail, Model model) {

        Member member = selectMember(userDetail);
        ProfileBean bean = mappingToBean(member);

        model.addAttribute("bean", bean);
        return "profile/profile";
    }

    // ===================================================================================
    //                                                                              Select
    //                                                                              ======
    private Member selectMember(MemberUserDetail userDetail) {
        Integer memberId = userDetail.getMember().getMemberId();
        Member member = memberBhv.selectEntity(cb -> {
            cb.setupSelect_MemberStatus();
            cb.setupSelect_MemberServiceAsOne().withServiceRank();
            cb.query().setMemberId_Equal(memberId);
        }).get();
        memberBhv.loadPurchase(member, purCB -> {
            purCB.setupSelect_Product();
            purCB.query().addOrderBy_PurchaseDatetime_Desc();
        });
        return member;
    }

    // ===================================================================================
    //                                                                             Mapping
    //                                                                             =======
    private ProfileBean mappingToBean(Member member) {
        ProfileBean bean = new ProfileBean(member);
        bean.purchaseList = member.getPurchaseList().stream().map(purchase -> {
            return new PurchasedProductBean(purchase);
        }).collect(Collectors.toList());
        return bean;
    }

}
