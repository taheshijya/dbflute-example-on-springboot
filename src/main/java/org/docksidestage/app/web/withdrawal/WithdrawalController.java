package org.docksidestage.app.web.withdrawal;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.validation.Valid;

import org.docksidestage.app.application.security.MemberUserDetail;
import org.docksidestage.dbflute.allcommon.CDef.WithdrawalReason;
import org.docksidestage.dbflute.exbhv.MemberBhv;
import org.docksidestage.dbflute.exbhv.MemberWithdrawalBhv;
import org.docksidestage.dbflute.exentity.Member;
import org.docksidestage.dbflute.exentity.MemberWithdrawal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author jflute
 */
@Controller
@RequestMapping("/withdrawal")
public class WithdrawalController {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger logger = LoggerFactory.getLogger(WithdrawalController.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Autowired
    private MemberBhv memberBhv;

    @Autowired
    private MemberWithdrawalBhv memberWithdrawalBhv;

    // ===================================================================================
    //                                                                              Entry
    //                                                                             =======
    @RequestMapping("")
    @Transactional
    public String index(Model model, WithdrawalForm withdrawalForm) {

        model.addAttribute("withdrawalReasonSelectOption", getWithdrawalReasonSelectOption());

        //return "redirect:/product/list";
        return "withdrawal/withdrawal_entry";
    }

    // ===================================================================================
    //                                                                           Execute
    //                                                                           =========
    @RequestMapping("/confirm")
    public ModelAndView list(ModelAndView model, @Valid WithdrawalForm withdrawalForm, BindingResult result) {
        logger.debug("#form: {}", withdrawalForm);
        //model.addAttribute("withdrawalReasonSelectOption", getWithdrawalReasonSelectOption());

        if (result.hasErrors()) {
            logger.debug("has error:" + result.getFieldErrors());
            model.addObject("beans", Collections.emptyList()); // #for_now avoid error

            //return "withdrawal/withdrawal_entry";
            model.setViewName("withdrawal/withdrawal_entry");
        }

        //model.addAttribute("withdrawalForm", withdrawalForm);

        model.setViewName("withdrawal/withdrawal_confirm");
        model.addObject("selectedReason", withdrawalForm.getSelectedReason());
        model.addObject("reasonInput", withdrawalForm.getReasonInput());

        return model;
        //return "withdrawal/withdrawal_confirm";
    }

    @RequestMapping("/done")
    public String done(@AuthenticationPrincipal MemberUserDetail userDetail, Model model, @Valid WithdrawalForm withdrawalForm,
            BindingResult result) {
        logger.debug("#form: {}", withdrawalForm);
        model.addAttribute("withdrawalReasonSelectOption", getWithdrawalReasonSelectOption());

        if (result.hasErrors()) {
            logger.debug("has error:" + result.getFieldErrors());
            model.addAttribute("beans", Collections.emptyList()); // #for_now avoid error
            return "withdrawal/withdrawal_entry";
        }

        Integer memberId = userDetail.getMember().getMemberId();
        insertWithdrawal(withdrawalForm, memberId);
        updateStatusWithdrawal(memberId);

        return "redirect:/logout";
    }

    // ===================================================================================
    //                                                                              Update
    //                                                                              ======
    private void insertWithdrawal(WithdrawalForm form, Integer memberId) {
        MemberWithdrawal withdrawal = new MemberWithdrawal();
        withdrawal.setMemberId(memberId);
        withdrawal.setWithdrawalReasonCodeAsWithdrawalReason(WithdrawalReason.codeOf(form.getSelectedReason()));
        withdrawal.setWithdrawalReasonInputText(form.getReasonInput());
        withdrawal.setWithdrawalDatetime(LocalDateTime.now());
        memberWithdrawalBhv.insert(withdrawal);
    }

    private void updateStatusWithdrawal(Integer memberId) {
        Member member = new Member();
        member.setMemberId(memberId);
        member.setMemberStatusCode_Withdrawal();
        memberBhv.updateNonstrict(member);
    }

    protected Map<String, String> getWithdrawalReasonSelectOption() {

        Map<String, String> withdrawalReasonSelectOption = new LinkedHashMap<String, String>();

        for (WithdrawalReason entry : WithdrawalReason.values()) {
            withdrawalReasonSelectOption.put(entry.code(), entry.alias());
        }

        return withdrawalReasonSelectOption;
    }

}
